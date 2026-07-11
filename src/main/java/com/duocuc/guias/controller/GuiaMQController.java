package com.duocuc.guias.controller;

import com.duocuc.guias.model.GuiaProcesadaMQ;
import com.duocuc.guias.repository.GuiaProcesadaMQRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador del Consumidor RabbitMQ — Semana 8.
 *
 * Expone el endpoint para consultar las guías procesadas
 * por el consumidor de la Cola 1 y guardadas en la tabla guias_procesadas_mq.
 *
 * GET /api/guias/procesadas-mq → requiere JWT con rol "admin"
 */
@RestController
@RequestMapping("/api/guias")
@RequiredArgsConstructor
public class GuiaMQController {

    private final GuiaProcesadaMQRepository repository;

    /**
     * GET /api/guias/procesadas-mq
     * Lista todas las guías procesadas por el consumidor de la Cola 1.
     * Requiere JWT con rol "admin".
     */
    @GetMapping("/procesadas-mq")
    public ResponseEntity<List<GuiaProcesadaMQ>> listarGuiasProcesadas() {
        return ResponseEntity.ok(repository.findAll());
    }
}
