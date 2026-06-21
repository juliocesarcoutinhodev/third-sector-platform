package br.com.toponesystem.thirdsector.notification.adapter.in.event;

import br.com.toponesystem.thirdsector.auth.domain.model.UserRegisteredEvent;
import br.com.toponesystem.thirdsector.notification.domain.model.EmailNotification;
import br.com.toponesystem.thirdsector.notification.domain.port.out.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
class UserRegisteredEmailListener {

    private final NotificationPublisher notificationPublisher;

    @ApplicationModuleListener
    void onUserRegistered(UserRegisteredEvent event) {
        log.info("User registered: {} — publishing welcome email", event.getUserEmail());

        var notification = new EmailNotification(
                event.getUserEmail(),
                "Bem-vindo(a) à Plataforma do Terceiro Setor",
                "user-registered",
                Map.of("userName", (Object) event.getUserName()),
                event.getTenantId()
        );

        notificationPublisher.publish(notification);
    }
}
