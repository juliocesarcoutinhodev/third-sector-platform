package br.com.toponesystem.thirdsector.notification.adapter.out.mail;

import br.com.toponesystem.thirdsector.notification.domain.exception.EmailSendFailedException;
import br.com.toponesystem.thirdsector.notification.domain.port.out.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class SpringMailEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    public void send(String recipient, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendFailedException("Failed to send email to " + recipient, e);
        }
    }
}
