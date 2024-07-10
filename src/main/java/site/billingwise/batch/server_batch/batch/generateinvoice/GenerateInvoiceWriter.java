package site.billingwise.batch.server_batch.batch.generateinvoice;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import site.billingwise.batch.server_batch.domain.contract.Contract;
import site.billingwise.batch.server_batch.domain.invoice.Invoice;
import site.billingwise.batch.server_batch.domain.invoice.PaymentStatus;
import site.billingwise.batch.server_batch.domain.invoice.repository.InvoiceRepository;
import site.billingwise.batch.server_batch.domain.invoice.repository.PaymentStatusRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;


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
                LocalDateTime dueDate = calculateDueDate(contract, setContractDate);

                Invoice invoice = Invoice.builder()
                        .contract(contract)
                        .invoiceType(contract.getInvoiceType())
                        .paymentType(contract.getPaymentType())
                        .paymentStatus(paymentStatusUnpaid)
                        .chargeAmount(contract.getItemPrice() * contract.getItemAmount())
                        .contractDate(setContractDate.atStartOfDay())
                        .dueDate(dueDate)
                        .build();
                invoiceRepository.save(invoice);
            }
        }
    }

    private LocalDateTime calculateDueDate(Contract contract, LocalDate setContractDate) {
        if (contract.getPaymentType().getName().equals("납부자 결제")) {
            return setContractDate.plusDays(contract.getPaymentDueCycle()).atStartOfDay();
        }
        return setContractDate.atStartOfDay();
    }
}