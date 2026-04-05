package com.pehlione.kafka.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMailConfig {

    @Bean
    public DirectExchange mailExchange(@Value("${app.rabbitmq.mail.exchange}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public DirectExchange mailDeadLetterExchange(@Value("${app.rabbitmq.mail.dlx}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue mailQueue(@Value("${app.rabbitmq.mail.queue}") String queue,
                           @Value("${app.rabbitmq.mail.dlx}") String deadLetterExchange,
                           @Value("${app.rabbitmq.mail.dlq-routing-key}") String deadLetterRoutingKey) {
        return QueueBuilder.durable(queue)
                .deadLetterExchange(deadLetterExchange)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }

    @Bean
    public Queue mailRetryQueue(@Value("${app.rabbitmq.mail.retry-queue}") String queue,
                                @Value("${app.rabbitmq.mail.retry-delay-ms}") long retryDelayMs,
                                @Value("${app.rabbitmq.mail.exchange}") String exchange,
                                @Value("${app.rabbitmq.mail.routing-key}") String routingKey) {
        return QueueBuilder.durable(queue)
                .ttl((int) retryDelayMs)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey(routingKey)
                .build();
    }

    @Bean
    public Queue mailDeadLetterQueue(@Value("${app.rabbitmq.mail.dlq}") String queue) {
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    public Binding mailBinding(Queue mailQueue,
                               DirectExchange mailExchange,
                               @Value("${app.rabbitmq.mail.routing-key}") String routingKey) {
        return BindingBuilder.bind(mailQueue).to(mailExchange).with(routingKey);
    }

    @Bean
    public Binding mailRetryBinding(@Value("${app.rabbitmq.mail.retry-routing-key}") String routingKey,
                                    Queue mailRetryQueue,
                                    DirectExchange mailExchange) {
        return BindingBuilder.bind(mailRetryQueue).to(mailExchange).with(routingKey);
    }

    @Bean
    public Binding mailDeadLetterBinding(@Value("${app.rabbitmq.mail.dlq-routing-key}") String routingKey,
                                         Queue mailDeadLetterQueue,
                                         DirectExchange mailDeadLetterExchange) {
        return BindingBuilder.bind(mailDeadLetterQueue).to(mailDeadLetterExchange).with(routingKey);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory manualAckRabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
