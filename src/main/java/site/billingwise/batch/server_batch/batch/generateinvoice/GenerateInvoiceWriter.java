package site.billingwise.batch.server_batch.batch.generateinvoice;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.invoice.PaymentStatus;
import site.billingwise.batch.server_batch.domain.invoice.repository.InvoiceRepository;
import site.billingwise.batch.server_batch.domain.invoice.repository.PaymentStatusRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class GenerateInvoiceWriter implements ItemWriter<Contract> {

    private final PaymentStatusRepository paymentStatusRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public void write(Chunk<? extends Contract> chunk) throws Exception {
        LocalDate now = LocalDate.now();
        LocalDate nextMonth = now.plusMonths(1);
        int nextMonthValue = nextMonth.getMonthValue();
        int yearValue  = nextMonth.getYear();

        PaymentStatus paymentStatusUnpaid  = paymentStatusRepository.findByName("미납")
                .orElseThrow(() -> new IllegalArgumentException("결제 미납 상태가 존재하지 않습니다."));

        for (Contract contract : chunk) {
            boolean exists = invoiceRepository.existsByContractAndMonthAndYear(contract, nextMonthValue, yearValue);

            if (!exists) {
                LocalDate setContractDate = LocalDate.of(yearValue, nextMonthValue, contract.getContractCycle());
                Invoice invoice = Invoice.builder()
                        .contract(contract)
                        .invoiceType(contract.getInvoiceType())
                        .paymentType(contract.getPaymentType())
                        .paymentStatus(paymentStatusUnpaid)
                        .chargeAmount(contract.getItemPrice() * contract.getItemAmount())
                        .contractDate(setContractDate.atStartOfDay())
                        .dueDate(contract.getIsSubscription() ? setContractDate.atStartOfDay() : setContractDate.plusDays(contract.getPaymentDueCycle()).atStartOfDay())
                        .build();
                invoiceRepository.save(invoice);
            }
        }
    }
}