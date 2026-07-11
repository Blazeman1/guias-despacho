package com.duocuc.guias.repository;

import com.duocuc.guias.model.GuiaProcesadaMQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuiaProcesadaMQRepository extends JpaRepository<GuiaProcesadaMQ, Long> {
}
