package com.duocuc.guias.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla guias_procesadas_mq.
 *
 * Almacena los datos de las guías de despacho procesadas
 * por el consumidor de la Cola 1 (guias-cola-principal).
 * Es una tabla distinta a guias_despacho, tal como pide la sumativa.
 */
@Entity
@Table(name = "guias_procesadas_mq")
public class GuiaProcesadaMQ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String numeroGuia;

    @Column(nullable = false, length = 200)
    private String transportista;

    @Column(nullable = false, length = 200)
    private String destinatario;

    @Column(nullable = false, length = 300)
    private String direccionDestino;

    @Column(nullable = false, length = 500)
    private String descripcionCarga;

    private Double pesoKg;

    private LocalDate fechaDespacho;

    @Column(nullable = false)
    private LocalDateTime fechaProcesadaMQ;

    @Column(nullable = false, length = 50)
    private String estadoMQ; // "PROCESADO"

    @Column(length = 300)
    private String claveS3;

    public GuiaProcesadaMQ() {}

    public GuiaProcesadaMQ(String numeroGuia, String transportista, String destinatario,
                            String direccionDestino, String descripcionCarga, Double pesoKg,
                            LocalDate fechaDespacho, String claveS3) {
        this.numeroGuia       = numeroGuia;
        this.transportista    = transportista;
        this.destinatario     = destinatario;
        this.direccionDestino = direccionDestino;
        this.descripcionCarga = descripcionCarga;
        this.pesoKg           = pesoKg;
        this.fechaDespacho    = fechaDespacho;
        this.claveS3          = claveS3;
        this.fechaProcesadaMQ = LocalDateTime.now();
        this.estadoMQ         = "PROCESADO";
    }

    public Long getId()                        { return id; }
    public String getNumeroGuia()              { return numeroGuia; }
    public String getTransportista()           { return transportista; }
    public String getDestinatario()            { return destinatario; }
    public String getDireccionDestino()        { return direccionDestino; }
    public String getDescripcionCarga()        { return descripcionCarga; }
    public Double getPesoKg()                  { return pesoKg; }
    public LocalDate getFechaDespacho()        { return fechaDespacho; }
    public LocalDateTime getFechaProcesadaMQ() { return fechaProcesadaMQ; }
    public String getEstadoMQ()               { return estadoMQ; }
    public String getClaveS3()                 { return claveS3; }

    public void setId(Long id)                               { this.id = id; }
    public void setNumeroGuia(String v)                      { this.numeroGuia = v; }
    public void setTransportista(String v)                   { this.transportista = v; }
    public void setDestinatario(String v)                    { this.destinatario = v; }
    public void setDireccionDestino(String v)                { this.direccionDestino = v; }
    public void setDescripcionCarga(String v)                { this.descripcionCarga = v; }
    public void setPesoKg(Double v)                          { this.pesoKg = v; }
    public void setFechaDespacho(LocalDate v)                { this.fechaDespacho = v; }
    public void setFechaProcesadaMQ(LocalDateTime v)         { this.fechaProcesadaMQ = v; }
    public void setEstadoMQ(String v)                        { this.estadoMQ = v; }
    public void setClaveS3(String v)                         { this.claveS3 = v; }
}
