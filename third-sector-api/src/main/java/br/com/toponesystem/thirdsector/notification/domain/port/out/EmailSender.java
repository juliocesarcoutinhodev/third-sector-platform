package br.com.toponesystem.thirdsector.notification.domain.port.out;

public interface EmailSender {

    void send(String recipient, String subject, String htmlBody);
}
