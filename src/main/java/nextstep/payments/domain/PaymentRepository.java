package nextstep.payments.domain;

public interface PaymentRepository {

    Payment save(Payment payment);

    Payment findById(Long id);

    Payment findBySessionAndUser(Long sessionId, Long userId);
}
