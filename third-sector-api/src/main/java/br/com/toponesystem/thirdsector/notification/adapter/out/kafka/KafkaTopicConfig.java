package br.com.toponesystem.thirdsector.notification.adapter.out.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class KafkaTopicConfig {

    static final String TOPIC = "notification.email";

    @Bean
    NewTopic notificationEmailTopic() {
        return new NewTopic(TOPIC, 3, (short) 1);
    }
}
