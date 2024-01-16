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

    private static int paymentsDateComparator(Payment p1, Payment p2) {
        return p1.getPaymentDate().compareTo(p2.getPaymentDate());
    }

    private static int paymentsItemComparator(Payment payment) {
        return payment.getPaymentItems().size();
    }

    private static boolean getPaymentsWithOneItem(Payment payment) {
        return payment.getPaymentItems().size() == 1;
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie rosnąco
     */
    List<Payment> findPaymentsSortedByDateAsc() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .sorted(PaymentService::paymentsDateComparator)
                .toList();
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie malejąco
     */
    List<Payment> findPaymentsSortedByDateDesc() {
        List<Payment> paymentsSortedByDateAsc = findPaymentsSortedByDateAsc();
        List<Payment> reversedList = new ArrayList<>(paymentsSortedByDateAsc);
        Collections.reverse(reversedList);
        return reversedList;

    }
    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów rosnąco
     */

    List<Payment> findPaymentsSortedByItemCountAsc() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .sorted(Comparator.comparingInt(PaymentService::paymentsItemComparator))
                .toList();

    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów malejąco
     */
    List<Payment> findPaymentsSortedByItemCountDesc() {
        List<Payment> paymentsSortedByItemCountAsc = findPaymentsSortedByItemCountAsc();
        List<Payment> reversedList = new ArrayList<>(paymentsSortedByItemCountAsc);
        Collections.reverse(reversedList);
        return reversedList;
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
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> YearMonth.from(payment.getPaymentDate()).equals(dateTimeProvider.yearMonthNow()))
                .toList();
    }

    /*
    Znajdź i zwróć płatności dla ostatnich X dni
     */
    List<Payment> findPaymentsForGivenLastDays(int days) {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(payment -> payment.getPaymentDate().isBefore(dateTimeProvider.zonedDateTimeNow()))
                .filter(payment -> payment.getPaymentDate().isAfter(dateTimeProvider.zonedDateTimeNow().minusDays(days)))
                .toList();
    }

    /*
    Znajdź i zwróć płatności z jednym elementem
     */
    Set<Payment> findPaymentsWithOnePaymentItem() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .filter(PaymentService::getPaymentsWithOneItem)
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
                .filter(payment -> calculatePaymentTotalPrice(payment).compareTo(BigDecimal.valueOf(value)) > 0)
                .collect(Collectors.toSet());
    }

    private BigDecimal calculatePaymentTotalPrice(Payment payment) {
        return payment.getPaymentItems().stream()
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
