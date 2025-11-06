package com.lsb.webshop.repository;

import com.lsb.webshop.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionCode(String transactionCode);

}
