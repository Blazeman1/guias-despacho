package com.duocuc.guias.service;

import com.duocuc.guias.config.RabbitMQConfig;
import com.duocuc.guias.model.GuiaDespacho;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Productor RabbitMQ — Semana 8.
 *
 * Envía los datos de una guía de despacho a la Cola 1 (guias-cola-principal)
 * a través del exchange guias-exchange con routing key guias-key.
 *
 * Si el consumidor falla al procesar el mensaje, RabbitMQ lo reenvía
 * automáticamente a la Cola 2 (guias-cola-errores) mediante el DLX configurado.
 */
@Service
public class ProductorGuiasService {

    private static final Logger log = LoggerFactory.getLogger(ProductorGuiasService.class);

    private final RabbitTemplate rabbitTemplate;

    public ProductorGuiasService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Envía los datos de la guía a la Cola 1.
     * Serializado automáticamente como JSON por Jackson2JsonMessageConverter.
     */
    public void enviarGuia(GuiaDespacho guia) {
        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("numeroGuia",       guia.getNumeroGuia());
        mensaje.put("transportista",    guia.getTransportista());
        mensaje.put("destinatario",     guia.getDestinatario());
        mensaje.put("direccionDestino", guia.getDireccionDestino());
        mensaje.put("descripcionCarga", guia.getDescripcionCarga());
        mensaje.put("pesoKg",           guia.getPesoKg());
        mensaje.put("fechaDespacho",    guia.getFechaDespacho() != null
                ? guia.getFechaDespacho().toString() : null);
        mensaje.put("claveS3",          guia.getClaveS3());
        mensaje.put("estado",           guia.getEstado() != null
                ? guia.getEstado().name() : null);

        log.info("Enviando guía {} a la cola {}", guia.getNumeroGuia(), RabbitMQConfig.COLA_PRINCIPAL);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PRINCIPAL,
                RabbitMQConfig.ROUTING_KEY,
                mensaje
        );

        log.info("Guía {} enviada exitosamente a guias-exchange -> guias-cola-principal",
                guia.getNumeroGuia());
    }
}
