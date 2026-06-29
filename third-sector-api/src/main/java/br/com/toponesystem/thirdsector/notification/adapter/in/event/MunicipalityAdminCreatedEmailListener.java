package br.com.toponesystem.thirdsector.notification.adapter.in.event;

import br.com.toponesystem.thirdsector.municipality.domain.model.MunicipalityAdminCreatedEvent;
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
class MunicipalityAdminCreatedEmailListener {

    private final NotificationPublisher notificationPublisher;

    @ApplicationModuleListener
    void onMunicipalityAdminCreated(MunicipalityAdminCreatedEvent event) {
        log.info("Municipality admin created for tenant={} — sending credentials email to {}",
                event.getTenantId(), event.getAdminEmail());

        var notification = new EmailNotification(
                event.getAdminEmail(),
                "Suas credenciais de acesso — " + event.getMunicipalityName(),
                "municipality-admin-created",
                Map.of(
                        "adminName", (Object) event.getAdminName(),
                        "adminEmail", event.getAdminEmail(),
                        "temporaryPassword", event.getTemporaryPassword(),
                        "municipalityName", event.getMunicipalityName()
                ),
                event.getTenantId()
        );

        notificationPublisher.publish(notification);
    }
}
