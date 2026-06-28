package br.com.toponesystem.thirdsector.notification;

import br.com.toponesystem.thirdsector.PlanFixtures;
import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import br.com.toponesystem.thirdsector.notification.domain.model.EmailNotification;
import br.com.toponesystem.thirdsector.notification.domain.port.out.EmailSender;
import br.com.toponesystem.thirdsector.notification.domain.port.out.NotificationPublisher;
import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationIntegrationTest extends AbstractIntegrationTest {

    static final KafkaContainer KAFKA;

    static {
        KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));
        KAFKA.start();
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    static final List<SentEmail> sentEmails = new CopyOnWriteArrayList<>();
    static volatile CountDownLatch emailLatch;

    record SentEmail(String recipient, String subject, String htmlBody) {}

    @TestConfiguration
    static class TestMailConfig {

        @Bean
        @Primary
        EmailSender capturingEmailSender() {
            return (recipient, subject, htmlBody) -> {
                sentEmails.add(new SentEmail(recipient, subject, htmlBody));
                if (emailLatch != null) {
                    emailLatch.countDown();
                }
            };
        }
    }

    @Autowired
    private NotificationPublisher publisher;

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @Autowired
    private PlanFixtures planFixtures;

    @BeforeEach
    void setUp() {
        sentEmails.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        emailLatch = null;
    }

    private static String validCnpj(long seed) {
        var root = String.format("%08d0001", Math.abs(seed) % 100_000_000);
        int[] w1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] w2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int s1 = 0, s2 = 0;
        for (int i = 0; i < 12; i++) {
            int d = root.charAt(i) - '0';
            s1 += d * w1[i];
            s2 += d * w2[i];
        }
        int d1 = 11 - (s1 % 11);
        if (d1 >= 10) d1 = 0;
        s2 += d1 * 2;
        int d2 = 11 - (s2 % 11);
        if (d2 >= 10) d2 = 0;
        return root + d1 + d2;
    }

    @Test
    void sendsEmailWithMunicipalityBranding() throws Exception {
        var subdomain = "notify-branding-" + System.nanoTime();
        municipalityRepository.save(
                new Municipality("Maringá", validCnpj(System.nanoTime()), subdomain,
                        planFixtures.basicPlanId(), "https://cdn.example.com/maringa-logo.png"));

        emailLatch = new CountDownLatch(1);

        TenantContext.setCurrentTenant(subdomain);
        try {
            publisher.publish(new EmailNotification(
                    "recipient@example.com",
                    "Assunto de Teste",
                    "test-notification",
                    Map.of(),
                    subdomain
            ));
        } finally {
            TenantContext.clear();
        }

        assertThat(emailLatch.await(15, TimeUnit.SECONDS)).isTrue();
        assertThat(sentEmails).hasSize(1);

        var email = sentEmails.get(0);
        assertThat(email.subject()).isEqualTo("Assunto de Teste");
        assertThat(email.recipient()).isEqualTo("recipient@example.com");
        assertThat(email.htmlBody()).contains("Maringá");
        assertThat(email.htmlBody()).contains("https://cdn.example.com/maringa-logo.png");
        assertThat(email.htmlBody()).contains("Notificação de Teste");
        assertThat(email.htmlBody()).contains("Third Sector Platform");
    }

    @Test
    void sendsEmailWithoutLogoWhenMunicipalityHasNoLogo() throws Exception {
        var subdomain = "notify-nologo-" + System.nanoTime();
        municipalityRepository.save(
                new Municipality("Londrina", validCnpj(System.nanoTime()), subdomain,
                        planFixtures.intermediatePlanId(), null));

        emailLatch = new CountDownLatch(1);

        TenantContext.setCurrentTenant(subdomain);
        try {
            publisher.publish(new EmailNotification(
                    "londrina@example.com",
                    "Notificação Londrina",
                    "test-notification",
                    Map.of(),
                    subdomain
            ));
        } finally {
            TenantContext.clear();
        }

        assertThat(emailLatch.await(15, TimeUnit.SECONDS)).isTrue();
        assertThat(sentEmails).hasSize(1);

        var email = sentEmails.get(0);
        assertThat(email.htmlBody()).contains("Londrina");
        assertThat(email.htmlBody()).contains("Notificação de Teste");
    }

    @Test
    void sendsEmailsForMultipleTenantsWithCorrectIsolation() throws Exception {
        var subdomainA = "notify-multi-a-" + System.nanoTime();
        var subdomainB = "notify-multi-b-" + System.nanoTime();
        municipalityRepository.save(
                new Municipality("Curitiba", validCnpj(System.nanoTime()), subdomainA,
                        planFixtures.enterprisePlanId(), "https://cdn.example.com/curitiba-logo.png"));
        municipalityRepository.save(
                new Municipality("Cascavel", validCnpj(System.nanoTime()), subdomainB,
                        planFixtures.basicPlanId(), "https://cdn.example.com/cascavel-logo.png"));

        emailLatch = new CountDownLatch(2);

        TenantContext.setCurrentTenant(subdomainA);
        try {
            publisher.publish(new EmailNotification(
                    "curitiba@example.com", "Curitiba Notification",
                    "test-notification", Map.of(), subdomainA));
        } finally {
            TenantContext.clear();
        }

        TenantContext.setCurrentTenant(subdomainB);
        try {
            publisher.publish(new EmailNotification(
                    "cascavel@example.com", "Cascavel Notification",
                    "test-notification", Map.of(), subdomainB));
        } finally {
            TenantContext.clear();
        }

        assertThat(emailLatch.await(15, TimeUnit.SECONDS)).isTrue();
        assertThat(sentEmails).hasSize(2);

        var curitibaEmail = sentEmails.stream()
                .filter(e -> e.recipient().equals("curitiba@example.com")).findFirst().orElseThrow();
        var cascavelEmail = sentEmails.stream()
                .filter(e -> e.recipient().equals("cascavel@example.com")).findFirst().orElseThrow();

        assertThat(curitibaEmail.htmlBody()).contains("Curitiba");
        assertThat(curitibaEmail.htmlBody()).contains("https://cdn.example.com/curitiba-logo.png");
        assertThat(curitibaEmail.htmlBody()).doesNotContain("Cascavel");
        assertThat(curitibaEmail.htmlBody()).doesNotContain("cascavel-logo");

        assertThat(cascavelEmail.htmlBody()).contains("Cascavel");
        assertThat(cascavelEmail.htmlBody()).contains("https://cdn.example.com/cascavel-logo.png");
        assertThat(cascavelEmail.htmlBody()).doesNotContain("Curitiba");
    }
}
