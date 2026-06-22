package br.com.toponesystem.thirdsector.notification.adapter.in.event;

import br.com.toponesystem.thirdsector.auth.domain.model.PasswordResetRequestedEvent;
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
class PasswordResetEmailListener {

    private final NotificationPublisher notificationPublisher;

    @ApplicationModuleListener
    void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        log.info("Password reset requested for {}", event.getUserEmail());

        var notification = new EmailNotification(
                event.getUserEmail(),
                "Redefinição de Senha — Plataforma do Terceiro Setor",
                "password-reset",
                Map.of(
                        "userName", (Object) event.getUserName(),
                        "resetToken", (Object) event.getResetToken()
                ),
                event.getTenantId()
        );

        notificationPublisher.publish(notification);
    }
}
