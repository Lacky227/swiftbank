import joblib
import numpy as np
import pandas as pd
import logging
import re
import csv
import os
import tensorflow as tf
from tensorflow.keras.models import load_model
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, ConfigDict
from typing import List, Optional
from datetime import datetime
import lime
import lime.lime_tabular
import py_eureka_client.eureka_client as eureka_client

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'

EUREKA_SERVER = os.getenv('EUREKA_SERVER_URL', 'http://swiftbank-registry:8761/eureka')
APP_NAME = "FRAUD-DETECTION-SERVICE"
INSTANCE_PORT = 8000
INSTANCE_HOST = os.getenv('HOSTNAME', 'swiftbank-fraud')


@asynccontextmanager
async def lifespan(app: FastAPI):
    await eureka_client.init_async(
        eureka_server=EUREKA_SERVER,
        app_name=APP_NAME,
        instance_port=INSTANCE_PORT,
        instance_host=INSTANCE_HOST,
        renewal_interval_in_secs=30,
        duration_in_secs=90
    )
    logger.info("Eureka registration successful")
    yield
    await eureka_client.stop_async()


app = FastAPI(
    title="Fraud Detection API",
    description="API для виявлення шахрайських транзакцій (Neural Network + LIME)",
    version="2.0.0",
    lifespan=lifespan
)


class Transaction(BaseModel):
    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "amount": 150.50, "currency": "USD", "device_type": "mobile", "ip_country": "US",
                "time_of_day": 14, "day_of_week": 3, "account_age_days": 365, "transactions_today": 2,
                "avg_amount_7d": 120.00, "time_since_last_txn": 3600, "is_proxy_vpn": 0,
                "merchant_risk_score": 25.5, "customer_country": "US", "language": "en",
                "card_expiry_month": 12, "card_expiry_year": 2027
            }
        }
    )
    amount: float
    currency: str
    device_type: str
    ip_country: str
    time_of_day: int
    day_of_week: int
    account_age_days: int
    transactions_today: int
    avg_amount_7d: float
    time_since_last_txn: int
    is_proxy_vpn: int
    merchant_risk_score: float
    customer_country: str
    language: str
    card_expiry_month: int
    card_expiry_year: int


class RiskFeature(BaseModel):
    feature: str
    value: float
    importance: float = 0.0
    risk_score: float


class LimeExplanationFeature(BaseModel):
    feature_name: str
    raw_feature: str
    feature_value: str
    impact_score: float
    description: str


class FraudPrediction(BaseModel):
    is_fraud: bool
    fraud_probability: float
    risk_level: str
    top_risk_features: List[RiskFeature]
    recommendation: str
    lime_details: List[LimeExplanationFeature]


class FeedbackData(BaseModel):
    transaction_id: str
    original_features: Transaction
    model_prediction: bool
    human_verdict: bool
    comment: Optional[str] = None


class ModelLoader:
    _instance = None
    _model = None
    _vectorizer_scaler = None
    _transform_matrix = None
    _vector_feature_names = None
    _encoded_feature_names = None
    _metadata = None
    _training_data_sample = None
    _lime_explainer = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(ModelLoader, cls).__new__(cls)
            cls._instance._load_components()
            cls._instance._initialize_explainer()
        return cls._instance

    def _load_components(self):
        try:
            BASE_PATH = os.getenv('MODEL_PATH', '/app/model_data')
            logger.info(f"Завантаження компонентів з: {BASE_PATH}")

            self._model = load_model(f'{BASE_PATH}/models/fraud_nn_model.keras')
            self._vectorizer_scaler = joblib.load(f'{BASE_PATH}/utils/vectorizer_scaler.pkl')
            self._transform_matrix = joblib.load(f'{BASE_PATH}/utils/transform_matrix.pkl')
            self._vector_feature_names = joblib.load(f'{BASE_PATH}/utils/vector_feature_names.pkl')
            self._encoded_feature_names = joblib.load(f'{BASE_PATH}/utils/encoded_feature_names.pkl')
            self._metadata = joblib.load(f'{BASE_PATH}/models/model_metadata.pkl')
            self._training_data_sample = joblib.load(f'{BASE_PATH}/utils/training_data_sample.pkl')

            logger.info("Keras модель та компоненти успішно завантажено!")
        except Exception as e:
            logger.error(f"Помилка завантаження файлів: {e}")
            raise

    def _initialize_explainer(self):
        try:
            logger.info("Ініціалізація LIME Explainer...")
            self._lime_explainer = lime.lime_tabular.LimeTabularExplainer(
                training_data=self._training_data_sample.values,
                feature_names=self._encoded_feature_names,
                class_names=['Не шахрайство', 'Шахрайство'],
                mode='classification',
                verbose=False
            )
        except Exception as e:
            logger.error(f"Помилка ініціалізації LIME: {e}")
            raise

    @property
    def model(self):
        return self._model

    @property
    def vectorizer_scaler(self):
        return self._vectorizer_scaler

    @property
    def transform_matrix(self):
        return self._transform_matrix

    @property
    def vector_feature_names(self):
        return self._vector_feature_names

    @property
    def encoded_feature_names(self):
        return self._encoded_feature_names

    @property
    def metadata(self):
        return self._metadata

    @property
    def lime_explainer(self):
        return self._lime_explainer


model_loader = ModelLoader()


def vectorize_transaction(transaction: Transaction) -> tuple:
    df = pd.DataFrame([transaction.model_dump()])
    categorical_features = model_loader.metadata['categorical_features']
    for col in categorical_features:
        df[col] = df[col].astype(str)

    df_encoded = pd.get_dummies(df, columns=categorical_features, drop_first=True)

    for feature in model_loader.encoded_feature_names:
        if feature not in df_encoded.columns:
            df_encoded[feature] = 0
    df_encoded = df_encoded[model_loader.encoded_feature_names]

    X_scaled = model_loader.vectorizer_scaler.transform(df_encoded.values)
    X_vectorized = np.dot(X_scaled, model_loader.transform_matrix)
    return X_vectorized, df_encoded


def get_top_risk_features(X_vectorized: np.ndarray) -> List[RiskFeature]:
    model = model_loader.model
    risk_features = []
    vector_feature_names = model_loader.vector_feature_names
    weights, _ = model.layers[0].get_weights()
    feature_importance_proxy = np.mean(np.abs(weights), axis=1)

    for i, name in enumerate(vector_feature_names):
        value = X_vectorized[0, i]
        imp = feature_importance_proxy[i]
        risk_score = imp * abs(value)

        if risk_score > 0.001:
            risk_features.append(RiskFeature(
                feature=str(name),
                value=float(value),
                importance=float(imp),
                risk_score=float(risk_score)
            ))

    risk_features.sort(key=lambda x: x.risk_score, reverse=True)
    return risk_features[:5]


def get_risk_level_and_recommendation(probability: float) -> tuple:
    if probability < 0.15:
        return "LOW", "Транзакція виглядає безпечною."
    elif probability < 0.75:
        return "MEDIUM", "Середній ризик. Потребує перевірки аналітиком."
    else:
        return "HIGH", "Високий ризик! Рекомендовано блокування."


def get_readable_feature_name(feature_code: str) -> str:
    feature_name_map = {
        'amount': 'Сума транзакції', 'time_of_day': 'Час доби', 'day_of_week': 'День тижня',
        'account_age_days': 'Вік акаунту', 'transactions_today': 'К-сть транзакцій',
        'avg_amount_7d': 'Середня сума (7д)', 'time_since_last_txn': 'Час з ост. транзакції',
        'is_proxy_vpn': 'VPN/Proxy', 'merchant_risk_score': 'Ризик мерчанта',
        'card_expiry_month': 'Місяць карти', 'card_expiry_year': 'Рік карти',
        'currency': 'Валюта', 'device_type': 'Пристрій', 'ip_country': 'Країна IP',
        'customer_country': 'Країна клієнта', 'language': 'Мова'
    }
    for key, readable in feature_name_map.items():
        if key in feature_code:
            return readable
    return feature_code.replace("_", " ").title()


def generate_lime_explanation_struct(X_encoded: pd.DataFrame) -> List[LimeExplanationFeature]:
    lime_features = []
    explainer = model_loader.lime_explainer

    def predict_fn_keras(x):
        x_df = pd.DataFrame(x, columns=model_loader.encoded_feature_names)
        x_scaled = model_loader.vectorizer_scaler.transform(x_df)
        x_vectorized = np.dot(x_scaled, model_loader.transform_matrix)
        probs_fraud = model_loader.model.predict(x_vectorized, verbose=0)
        return np.hstack([1 - probs_fraud, probs_fraud])

    try:
        explanation = explainer.explain_instance(
            X_encoded.iloc[0].values, predict_fn_keras, num_features=10, labels=(1,)
        )

        for feature_condition, weight in explanation.as_list(label=1):
            readable_name = get_readable_feature_name(feature_condition)
            raw_name_match = re.search(r"([a-zA-Z_]+)", feature_condition)
            raw_name = raw_name_match.group(1) if raw_name_match else "unknown"

            description = f"{readable_name}: фактор ризику (вплив +{weight:.2f})" if weight > 0 else f"{readable_name}: фактор довіри (вплив {weight:.2f})"

            lime_features.append(LimeExplanationFeature(
                feature_name=readable_name,
                raw_feature=raw_name,
                feature_value="Див. деталі",
                impact_score=round(weight, 4),
                description=description
            ))

    except Exception as e:
        logger.error(f"LIME Error: {e}")

    return lime_features


@app.get("/")
async def root():
    return {
        "service": "Fraud Detection API (Neural Network)",
        "model_status": "Active" if model_loader.model else "Error"
    }


@app.get("/health")
async def health_check():
    return {"status": "healthy", "timestamp": datetime.now().isoformat()}


@app.post("/predict", response_model=FraudPrediction)
async def predict_fraud(transaction: Transaction):
    try:
        logger.info(f"Transaction check: amount={transaction.amount}, country={transaction.ip_country}")

        X_vectorized, X_encoded = vectorize_transaction(transaction)
        prediction_arr = model_loader.model.predict(X_vectorized, verbose=0)
        probability = float(prediction_arr[0][0])
        is_fraud = bool(probability >= 0.5)

        risk_level, recommendation = get_risk_level_and_recommendation(probability)
        top_risk_features = get_top_risk_features(X_vectorized)
        lime_details = generate_lime_explanation_struct(X_encoded)

        return FraudPrediction(
            is_fraud=is_fraud,
            fraud_probability=round(probability, 4),
            risk_level=risk_level,
            top_risk_features=top_risk_features,
            recommendation=recommendation,
            lime_details=lime_details
        )

    except Exception as e:
        logger.error(f"Prediction error: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Internal Error: {str(e)}")


@app.post("/feedback")
async def collect_feedback(feedback: FeedbackData):
    try:
        file_path = './data/feedback_dataset.csv'
        file_exists = os.path.isfile(file_path)

        with open(file_path, 'a', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            if not file_exists:
                headers = list(feedback.original_features.model_dump().keys())
                headers.extend(['model_prediction', 'human_verdict', 'comment', 'timestamp'])
                writer.writerow(headers)

            row_data = list(feedback.original_features.model_dump().values())
            row_data.extend([
                feedback.model_prediction,
                feedback.human_verdict,
                feedback.comment,
                datetime.now().isoformat()
            ])
            writer.writerow(row_data)

        return {"status": "success", "message": "Feedback saved successfully"}

    except Exception as e:
        logger.error(f"Feedback error: {e}")
        raise HTTPException(status_code=500, detail="Could not save feedback")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)