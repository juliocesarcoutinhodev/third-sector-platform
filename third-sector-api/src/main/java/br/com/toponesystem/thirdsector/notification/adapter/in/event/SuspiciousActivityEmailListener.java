package br.com.toponesystem.thirdsector.notification.adapter.in.event;

import br.com.toponesystem.thirdsector.auth.domain.model.SuspiciousTokenReuseDetectedEvent;
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
class SuspiciousActivityEmailListener {

    private final NotificationPublisher notificationPublisher;

    @ApplicationModuleListener
    void onSuspiciousTokenReuse(SuspiciousTokenReuseDetectedEvent event) {
        log.warn("Suspicious token reuse detected for user={}, familyId={}",
                event.getUserEmail(), event.getFamilyId());

        var notification = new EmailNotification(
                event.getUserEmail(),
                "Alerta de Segurança — Atividade Suspeita Detectada",
                "suspicious-activity-detected",
                Map.of("userName", (Object) event.getUserName()),
                event.getTenantId()
        );

        notificationPublisher.publish(notification);
    }
}
