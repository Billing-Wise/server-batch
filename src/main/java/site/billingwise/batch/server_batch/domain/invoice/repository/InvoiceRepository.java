package site.billingwise.batch.server_batch.domain.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Invoice i WHERE i.contract = :contract AND MONTH(i.contractDate) = :month AND YEAR(i.contractDate) = :year")
    boolean existsByContractAndMonthAndYear(@Param("contract") Contract contract, @Param("month") int month, @Param("year") int year);
}