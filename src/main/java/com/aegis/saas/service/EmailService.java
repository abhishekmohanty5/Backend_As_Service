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
        // Premium, mobile-responsive SaaS email template
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    /* Reset and base styles for email clients */
                    body { 
                        margin: 0; 
                        padding: 0; 
                        width: 100%% !important; 
                        -webkit-text-size-adjust: 100%%; 
                        -ms-text-size-adjust: 100%%; 
                        background-color: #f8fafc; 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    }
                    img { border: 0; height: auto; line-height: 100%%; outline: none; text-decoration: none; }
                    table { border-collapse: collapse !important; }
                    .wrapper { width: 100%%; table-layout: fixed; background-color: #f8fafc; padding-bottom: 40px; }
                    .main { 
                        background-color: #ffffff; 
                        margin: 20px auto; 
                        width: 100%%; 
                        max-width: 600px; 
                        border-radius: 12px; 
                        overflow: hidden; 
                        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
                        border: 1px solid #e2e8f0;
                    }
                    .header { 
                        background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); 
                        padding: 40px 32px; 
                        text-align: center; 
                    }
                    .logo { 
                        color: #ffffff; 
                        font-size: 24px; 
                        font-weight: 700; 
                        letter-spacing: -0.025em; 
                        margin: 0; 
                        text-decoration: none;
                    }
                    .content { padding: 40px 32px; color: #334155; line-height: 1.6; }
                    h1 { color: #0f172a; font-size: 24px; font-weight: 700; margin-top: 0; margin-bottom: 16px; }
                    p { margin-top: 0; margin-bottom: 16px; font-size: 16px; }
                    .footer { padding: 32px; text-align: center; color: #64748b; font-size: 14px; background-color: #f1f5f9; }
                    .footer a { color: #2563eb; text-decoration: none; }
                    .btn-container { margin-top: 32px; margin-bottom: 32px; text-align: center; }
                    .btn { 
                        background-color: #2563eb; 
                        color: #ffffff !important; 
                        padding: 14px 28px; 
                        border-radius: 8px; 
                        font-weight: 600; 
                        text-decoration: none; 
                        display: inline-block;
                        transition: background-color 0.2s;
                    }
                    .fallback-link { font-size: 12px; color: #94a3b8; word-break: break-all; margin-top: 24px; }
                    .divider { height: 1px; background-color: #e2e8f0; margin: 32px 0; }
                    .badge {
                        display: inline-block;
                        background: rgba(59, 130, 246, 0.1);
                        color: #3b82f6;
                        padding: 4px 12px;
                        border-radius: 9999px;
                        font-size: 12px;
                        font-weight: 600;
                        text-transform: uppercase;
                        margin-bottom: 16px;
                    }
                    @media only screen and (max-width: 600px) {
                        .main { margin: 0 !important; border-radius: 0 !important; }
                        .content { padding: 32px 24px !important; }
                    }
                </style>
            </head>
            <body>
                <div class="wrapper">
                    <div class="main">
                        <div class="header">
                            <div class="logo">AegisInfra</div>
                        </div>
                        <div class="content">
                            %s
                        </div>
                        <div class="footer">
                            &copy; 2026 <strong>AegisInfra</strong>. All rights reserved.<br>
                            <div style="margin-top: 8px;">
                                <a href="mailto:support@aegisinfra.com">Support</a> &bull; 
                                <a href="https://aegisinfra.me/terms">Terms</a> &bull; 
                                <a href="https://aegisinfra.me/privacy">Privacy</a>
                            </div>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, body);
    }
}
