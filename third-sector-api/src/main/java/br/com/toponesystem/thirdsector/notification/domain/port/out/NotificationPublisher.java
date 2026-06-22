package br.com.toponesystem.thirdsector.notification.domain.port.out;

import br.com.toponesystem.thirdsector.notification.domain.model.EmailNotification;

public interface NotificationPublisher {

    void publish(EmailNotification notification);
}
