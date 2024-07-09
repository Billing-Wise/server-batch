package site.billingwise.batch.server_batch.domain.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.billingwise.batch.server_batch.domain.invoice.PaymentStatus;

import java.util.Optional;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Long> {
    Optional<PaymentStatus> findByName(String name);
}
