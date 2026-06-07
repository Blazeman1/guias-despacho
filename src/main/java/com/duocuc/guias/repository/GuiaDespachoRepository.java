package com.duocuc.guias.repository;

import com.duocuc.guias.model.GuiaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuiaDespachoRepository extends JpaRepository<GuiaDespacho, Long> {

    Optional<GuiaDespacho> findByNumeroGuia(String numeroGuia);

    List<GuiaDespacho> findByTransportista(String transportista);

    List<GuiaDespacho> findByFechaDespacho(LocalDate fecha);

    List<GuiaDespacho> findByTransportistaAndFechaDespacho(String transportista, LocalDate fecha);

    boolean existsByNumeroGuia(String numeroGuia);
}
