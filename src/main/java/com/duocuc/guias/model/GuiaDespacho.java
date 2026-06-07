package com.duocuc.guias.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "guias_despacho")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiaDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroGuia;

    @Column(nullable = false)
    private String transportista;

    @Column(nullable = false)
    private String destinatario;

    @Column(nullable = false)
    private String direccionDestino;

    @Column(nullable = false)
    private String descripcionCarga;

    private Double pesoKg;

    @Column(nullable = false)
    private LocalDate fechaDespacho;

    @Enumerated(EnumType.STRING)
    private EstadoGuia estado;

    // Rutas de almacenamiento
    private String rutaEfs;      // Ruta temporal en EFS
    private String claveS3;      // Clave del objeto en S3

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) estado = EstadoGuia.PENDIENTE;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum EstadoGuia {
        PENDIENTE, GENERADA, SUBIDA_S3, ENTREGADA, CANCELADA
    }
}
