package com.duocuc.guias.controller;

import com.duocuc.guias.dto.GuiaDTO;
import com.duocuc.guias.service.GuiaDespachoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/guias")
@RequiredArgsConstructor
public class GuiaDespachoController {

    private final GuiaDespachoService service;

    /**
     * POST /api/guias
     * Criterios 1 + 2: Crea la guía, la guarda en EFS y la sube automáticamente a S3.
     */
    @PostMapping
    public ResponseEntity<GuiaDTO.GuiaResponse> crearGuia(
            @Valid @RequestBody GuiaDTO.CrearGuiaRequest request) throws IOException {
        GuiaDTO.GuiaResponse response = service.crearYSubirGuia(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * GET /api/guias/{numeroGuia}
     * Obtiene los metadatos de una guía específica.
     */
    @GetMapping("/{numeroGuia}")
    public ResponseEntity<GuiaDTO.GuiaResponse> obtenerGuia(
            @PathVariable String numeroGuia) {
        return ResponseEntity.ok(service.obtenerGuia(numeroGuia));
    }

    /**
     * GET /api/guias/{numeroGuia}/descargar
     * Criterio 4: Descarga el PDF desde S3 con validación de existencia.
     */
    @GetMapping("/{numeroGuia}/descargar")
    public ResponseEntity<byte[]> descargarGuia(
            @PathVariable String numeroGuia) {
        byte[] pdf = service.descargarGuia(numeroGuia);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", numeroGuia + ".pdf");
        headers.setContentLength(pdf.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    /**
     * PUT /api/guias/{numeroGuia}
     * Criterio 3: Modifica la guía y actualiza el PDF en S3.
     */
    @PutMapping("/{numeroGuia}")
    public ResponseEntity<GuiaDTO.GuiaResponse> actualizarGuia(
            @PathVariable String numeroGuia,
            @RequestBody GuiaDTO.ActualizarGuiaRequest request) throws IOException {
        return ResponseEntity.ok(service.actualizarGuia(numeroGuia, request));
    }

    /**
     * DELETE /api/guias/{numeroGuia}
     * Elimina la guía de S3, EFS y base de datos.
     */
    @DeleteMapping("/{numeroGuia}")
    public ResponseEntity<Map<String, String>> eliminarGuia(
            @PathVariable String numeroGuia) {
        service.eliminarGuia(numeroGuia);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Guía eliminada correctamente",
                "numeroGuia", numeroGuia
        ));
    }

    /**
     * GET /api/guias/historial
     * Criterio 5: Consulta el historial filtrado por transportista y/o fecha.
     *
     * Ejemplos:
     *   GET /api/guias/historial?transportista=TransportesXYZ
     *   GET /api/guias/historial?fecha=2024-01-15
     *   GET /api/guias/historial?transportista=TransportesXYZ&fecha=2024-01-15
     *   GET /api/guias/historial  (retorna todas)
     */
    @GetMapping("/historial")
    public ResponseEntity<List<GuiaDTO.GuiaResponse>> consultarHistorial(
            @RequestParam(required = false) String transportista,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<GuiaDTO.GuiaResponse> historial = service.consultarHistorial(transportista, fecha);
        return ResponseEntity.ok(historial);
    }

    /**
     * GET /api/guias/health
     * Endpoint de salud para verificar que la aplicación está activa.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "estado", "UP",
                "servicio", "Sistema de Gestión de Guías de Despacho",
                "version", "1.0.0"
        ));
    }
}
