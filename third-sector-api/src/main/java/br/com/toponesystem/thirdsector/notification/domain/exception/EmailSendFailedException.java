package br.com.toponesystem.thirdsector.notification.domain.exception;

public class EmailSendFailedException extends RuntimeException {

    public EmailSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
