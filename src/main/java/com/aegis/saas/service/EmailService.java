package com.aegis.saas.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@aegisinfra.me}")
    private String fromEmail;

    /**
     * Send a professional HTML email
     */
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "AegisInfra");
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Wrap the content in a professional template
            String htmlContent = wrapInProfessionalTemplate(subject, content);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Professional email sent successfully to: {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send professional email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String wrapInProfessionalTemplate(String title, String body) {
        // Modern, premium email template
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f9fafb; margin: 0; padding: 40px 10px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1); border: 1px solid #edf2f7; }
                    .header { background: linear-gradient(135deg, #0f172a, #1e293b); color: #ffffff; padding: 40px 32px; text-align: center; }
                    .logo { font-size: 28px; font-weight: 800; letter-spacing: -1px; margin-bottom: 8px; }
                    .badge { display: inline-block; padding: 4px 12px; background: rgba(59, 130, 246, 0.2); color: #60a5fa; border-radius: 99px; font-size: 12px; font-weight: 600; text-transform: uppercase; margin-bottom: 20px; }
                    .content { padding: 40px 32px; line-height: 1.7; color: #334155; font-size: 16px; }
                    .footer { padding: 32px; text-align: center; font-size: 13px; color: #94a3b8; background-color: #f8fafc; border-top: 1px solid #f1f5f9; }
                    .highlight { color: #2563eb; font-weight: 600; }
                    .divider { height: 1px; background-color: #f1f5f9; margin: 24px 0; }
                    a { color: #2563eb; text-decoration: none; font-weight: 600; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="badge">SECURE INFRASTRUCTURE</div>
                        <div class="logo">AegisInfra</div>
                        <h2 style="margin: 0; font-weight: 500; opacity: 0.9; font-size: 20px;">%s</h2>
                    </div>
                    <div class="content">
                        %s
                    </div>
                    <div class="footer">
                        &copy; 2026 <strong>AegisInfra</strong>. Leading the way in cloud reliability.<br>
                        This is an automated message to keep you informed about your account security and services.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, body.replace("\n", "<br>"));
    }
}
