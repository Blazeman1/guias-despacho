package com.duocuc.guias.dto;

import com.duocuc.guias.model.GuiaDespacho;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GuiaDTO {

    @Data
    public static class CrearGuiaRequest {
        @NotBlank(message = "El transportista es obligatorio")
        private String transportista;

        @NotBlank(message = "El destinatario es obligatorio")
        private String destinatario;

        @NotBlank(message = "La dirección de destino es obligatoria")
        private String direccionDestino;

        @NotBlank(message = "La descripción de la carga es obligatoria")
        private String descripcionCarga;

        private Double pesoKg;

        @NotNull(message = "La fecha de despacho es obligatoria")
        private LocalDate fechaDespacho;
    }

    @Data
    public static class ActualizarGuiaRequest {
        private String destinatario;
        private String direccionDestino;
        private String descripcionCarga;
        private Double pesoKg;
        private LocalDate fechaDespacho;
        private GuiaDespacho.EstadoGuia estado;
    }

    @Data
    public static class GuiaResponse {
        private Long id;
        private String numeroGuia;
        private String transportista;
        private String destinatario;
        private String direccionDestino;
        private String descripcionCarga;
        private Double pesoKg;
        private LocalDate fechaDespacho;
        private GuiaDespacho.EstadoGuia estado;
        private String rutaEfs;
        private String claveS3;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaActualizacion;

        public static GuiaResponse from(GuiaDespacho guia) {
            GuiaResponse r = new GuiaResponse();
            r.setId(guia.getId());
            r.setNumeroGuia(guia.getNumeroGuia());
            r.setTransportista(guia.getTransportista());
            r.setDestinatario(guia.getDestinatario());
            r.setDireccionDestino(guia.getDireccionDestino());
            r.setDescripcionCarga(guia.getDescripcionCarga());
            r.setPesoKg(guia.getPesoKg());
            r.setFechaDespacho(guia.getFechaDespacho());
            r.setEstado(guia.getEstado());
            r.setRutaEfs(guia.getRutaEfs());
            r.setClaveS3(guia.getClaveS3());
            r.setFechaCreacion(guia.getFechaCreacion());
            r.setFechaActualizacion(guia.getFechaActualizacion());
            return r;
        }
    }

    @Data
    public static class HistorialResponse {
        private String transportista;
        private LocalDate fecha;
        private long totalGuias;
        private java.util.List<GuiaResponse> guias;
    }
}
