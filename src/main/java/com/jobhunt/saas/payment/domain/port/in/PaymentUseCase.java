package com.jobhunt.saas.payment.domain.port.in;

import com.jobhunt.saas.payment.application.dto.MockPaymentRequest;
import com.jobhunt.saas.payment.application.dto.MockPaymentResponse;

public interface PaymentUseCase {
    MockPaymentResponse processPayment(MockPaymentRequest request);
}
