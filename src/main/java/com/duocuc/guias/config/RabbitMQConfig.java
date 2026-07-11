package com.duocuc.guias.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

/**
 * Configuración de RabbitMQ — Semana 8 (Sumativa).
 *
 * Cola 1: guias-cola-principal
 *   - Recibe todas las guías de despacho creadas (productor).
 *   - El consumidor lee de aquí y persiste en BD tabla guias_procesadas_mq.
 *   - Si el procesamiento falla → el mensaje va automáticamente a Cola 2 (DLX).
 *
 * Cola 2: guias-cola-errores (Dead Letter Queue)
 *   - Recibe los mensajes que fallaron en guias-cola-principal.
 *   - Actúa como buffer de errores para análisis y reintento posterior.
 *
 * Exchange principal: guias-exchange (Direct)
 *   - Enruta mensajes a guias-cola-principal con routing key "guias-key".
 *
 * Exchange DLX: guias-dlx-exchange (Direct)
 *   - Recibe mensajes fallidos y los enruta a guias-cola-errores.
 */
@Configuration
public class RabbitMQConfig {

    // Cola 1 — principal
    public static final String COLA_PRINCIPAL    = "guias-cola-principal";
    public static final String EXCHANGE_PRINCIPAL = "guias-exchange";
    public static final String ROUTING_KEY        = "guias-key";

    // Cola 2 — Dead Letter Queue (errores)
    public static final String COLA_ERRORES      = "guias-cola-errores";
    public static final String DLX_EXCHANGE      = "guias-dlx-exchange";
    public static final String DLX_ROUTING_KEY   = "guias-error-key";

    // ── Cola 2: Dead Letter Queue (se declara primero porque Cola 1 la referencia) ──

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue colaErrores() {
        return QueueBuilder.durable(COLA_ERRORES).build();
    }

    @Bean
    public Binding dlxBinding(Queue colaErrores, DirectExchange dlxExchange) {
        return BindingBuilder.bind(colaErrores)
                .to(dlxExchange)
                .with(DLX_ROUTING_KEY);
    }

    // ── Cola 1: principal con DLX configurado ──

    @Bean
    public DirectExchange exchangePrincipal() {
        return new DirectExchange(EXCHANGE_PRINCIPAL);
    }

    /**
     * Cola principal con Dead Letter Exchange configurado.
     * Si el consumidor lanza una excepción no recuperable,
     * RabbitMQ reenvía el mensaje automáticamente a guias-dlx-exchange
     * → guias-cola-errores.
     */
    @Bean
    public Queue colaPrincipal() {
        return QueueBuilder.durable(COLA_PRINCIPAL)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding bindingPrincipal(Queue colaPrincipal, DirectExchange exchangePrincipal) {
        return BindingBuilder.bind(colaPrincipal)
                .to(exchangePrincipal)
                .with(ROUTING_KEY);
    }

    // ── Conversor JSON y RabbitTemplate ──

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Configura el contenedor de listeners con política de reintentos:
     * - Máximo 3 intentos de procesamiento
     * - Si los 3 fallan → RejectAndDontRequeueRecoverer rechaza el mensaje
     * - RabbitMQ detecta el rechazo y activa el x-dead-letter-exchange
     * - El mensaje va automáticamente a guias-cola-errores (DLQ)
     *
     * Sin esta configuración, Spring AMQP reintenta indefinidamente en loop.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());

        RetryOperationsInterceptor retryInterceptor =
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .backOffOptions(1000, 2.0, 5000)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build();

        factory.setAdviceChain(retryInterceptor);
        return factory;
    }
}
