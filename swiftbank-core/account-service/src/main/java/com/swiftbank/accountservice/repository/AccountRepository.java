package com.swiftbank.accountservice.repository;

import com.swiftbank.accountservice.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsAccountByUserId(Long userId);
    Optional<List<Account>> findAccountsByUserId(Long userId);
    boolean existsAccountByNumber(String number);
    Optional<Account> findAccountByNumber(String number);
}
