package com.jobhunt.saas.notification.domain.port.in;

public interface SendEmailUseCase {
    void sendEmail(String to, String subject, String text);
}
