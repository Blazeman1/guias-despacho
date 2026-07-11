package com.duocuc.guias.service;

import com.duocuc.guias.config.RabbitMQConfig;
import com.duocuc.guias.model.GuiaProcesadaMQ;
import com.duocuc.guias.repository.GuiaProcesadaMQRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Consumidor RabbitMQ — Semana 8.
 *
 * Escucha la Cola 1 (guias-cola-principal) y persiste cada guía
 * recibida en la tabla guias_procesadas_mq de la base de datos.
 *
 * Si el procesamiento falla (excepción no controlada), RabbitMQ
 * reenvía automáticamente el mensaje a la Cola 2 (guias-cola-errores)
 * gracias al Dead Letter Exchange configurado en RabbitMQConfig.
 *
 * También escucha la Cola 2 para registrar en logs los mensajes
 * de error para análisis y depuración posterior.
 */
@Service
public class ConsumidorGuiasService {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorGuiasService.class);

    private final GuiaProcesadaMQRepository repository;

    public ConsumidorGuiasService(GuiaProcesadaMQRepository repository) {
        this.repository = repository;
    }

    /**
     * Consumidor de Cola 1: guias-cola-principal.
     * Procesa el mensaje y persiste en la tabla guias_procesadas_mq.
     * Si lanza excepción → el mensaje va a guias-cola-errores automáticamente.
     */
    @RabbitListener(queues = RabbitMQConfig.COLA_PRINCIPAL)
    public void procesarGuia(Map<String, Object> mensaje) {
        log.info("Mensaje recibido desde {}: numeroGuia={}",
                RabbitMQConfig.COLA_PRINCIPAL, mensaje.get("numeroGuia"));

        // Si numeroGuia es null (mensaje malformado), lanza excepción
        // → RabbitMQ detecta el fallo y redirige a guias-cola-errores (DLQ)
        String numeroGuia = (String) mensaje.get("numeroGuia");
        if (numeroGuia == null) {
            throw new IllegalArgumentException(
                "Mensaje malformado: falta el campo 'numeroGuia'. "
                + "Redirigiendo a DLQ guias-cola-errores.");
        }

        String transportista    = (String) mensaje.get("transportista");
        String destinatario     = (String) mensaje.get("destinatario");
        String direccionDestino = (String) mensaje.get("direccionDestino");
        String descripcionCarga = (String) mensaje.get("descripcionCarga");
        Double pesoKg = mensaje.get("pesoKg") != null
                ? Double.parseDouble(mensaje.get("pesoKg").toString()) : null;
        String claveS3 = (String) mensaje.get("claveS3");

        LocalDate fechaDespacho = null;
        if (mensaje.get("fechaDespacho") != null) {
            fechaDespacho = LocalDate.parse(mensaje.get("fechaDespacho").toString());
        }

        GuiaProcesadaMQ guiaMQ = new GuiaProcesadaMQ(
                numeroGuia, transportista, destinatario,
                direccionDestino, descripcionCarga, pesoKg,
                fechaDespacho, claveS3
        );

        GuiaProcesadaMQ guardada = repository.save(guiaMQ);
        log.info("Guía {} guardada en BD (guias_procesadas_mq) con ID: {}",
                numeroGuia, guardada.getId());
    }

    /**
     * Consumidor de Cola 2: guias-cola-errores (Dead Letter Queue).
     * Registra en logs los mensajes que fallaron en la Cola 1
     * para análisis y depuración posterior.
     */
    @RabbitListener(queues = RabbitMQConfig.COLA_ERRORES)
    public void procesarError(Map<String, Object> mensajeError) {
        log.error("Mensaje en COLA DE ERRORES (DLQ): numeroGuia={}, datos={}",
                mensajeError.get("numeroGuia"), mensajeError);
    }
}
