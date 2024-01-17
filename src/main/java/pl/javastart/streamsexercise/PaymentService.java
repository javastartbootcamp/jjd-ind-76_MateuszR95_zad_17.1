package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    public static class PaymentDateComparator implements Comparator<Payment> {
        @Override
        public int compare(Payment p1, Payment p2) {
            return p1.getPaymentDate().compareTo(p2.getPaymentDate());
        }
    }
    
    public static class PaymentItemsComparator implements Comparator<Payment> {
        @Override
        public int compare(Payment p1, Payment p2) {
            return Integer.compare(p1.getPaymentItemsSize(), p2.getPaymentItemsSize());
        }
    }
    /*
    Znajdź i zwróć płatności posortowane po dacie rosnąco
     */

    List<Payment> findPaymentsSortedByDateAsc() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .sorted(new PaymentDateComparator())
                .toList();
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie malejąco
     */
    List<Payment> findPaymentsSortedByDateDesc() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .sorted(new PaymentDateComparator().reversed())
                .toList();
    }
    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów rosnąco
     */

    List<Payment> findPaymentsSortedByItemCountAsc() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .sorted(new PaymentItemsComparator())
                .toList();
    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów malejąco
     */
    List<Payment> findPaymentsSortedByItemCountDesc() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .sorted(new PaymentItemsComparator().reversed())
                .toList();
    }

    /*
    Znajdź i zwróć płatności dla wskazanego miesiąca
     */
    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> YearMonth.from(payment.getPaymentDate()).equals(yearMonth))
                .toList();
    }

    /*
    Znajdź i zwróć płatności dla aktualnego miesiąca
     */
    List<Payment> findPaymentsForCurrentMonth() {
        return findPaymentsForGivenMonth(dateTimeProvider.yearMonthNow());
    }

    /*
    Znajdź i zwróć płatności dla ostatnich X dni
     */
    List<Payment> findPaymentsForGivenLastDays(int days) {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> payment.getPaymentDate().isAfter(dateTimeProvider.zonedDateTimeNow().minusDays(days)))
                .toList();
    }

    /*
    Znajdź i zwróć płatności z jednym elementem
     */
    Set<Payment> findPaymentsWithOnePaymentItem() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(Payment::isPaymentsWithOneItem)
                .collect(Collectors.toSet());
    }

    /*
    Znajdź i zwróć nazwy produktów sprzedanych w aktualnym miesiącu
     */
    Set<String> findProductsSoldInCurrentMonth() {
        List<Payment> paymentsForCurrentMonth  = findPaymentsForCurrentMonth();

        return paymentsForCurrentMonth.stream()
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());
    }

    /*
    Policz i zwróć sumę sprzedaży dla wskazanego miesiąca
     */
    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        List<Payment> paymentsForGivenMonth = findPaymentsForGivenMonth(yearMonth);
        return paymentsForGivenMonth.stream()
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    Policz i zwróć sumę przyznanych rabatów dla wskazanego miesiąca
     */
    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        List<Payment> paymentsForGivenMonth = findPaymentsForGivenMonth(yearMonth);

        return paymentsForGivenMonth.stream()
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(paymentItem -> paymentItem.getRegularPrice().subtract(paymentItem.getFinalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    /*
    Znajdź i zwróć płatności dla użytkownika z podanym mailem
     */
    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> payment.getUser().getEmail().equalsIgnoreCase(userEmail))
                .map(Payment::getPaymentItems)
                .flatMap(Collection::stream)
                .toList();
    }

    /*
    Znajdź i zwróć płatności, których wartość przekracza wskazaną granicę
     */
    Set<Payment> findPaymentsWithValueOver(int value) {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> payment.getTotalPrice().compareTo(BigDecimal.valueOf(value)) > 0)
                .collect(Collectors.toSet());
    }

}
