package com.bmci.demo.repository;


import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bmci.demo.enums.StatutNotification;
import com.bmci.demo.enums.TypeFacture;
import com.bmci.demo.model.FactureDetectee;

public interface FactureDetecteeRepository extends JpaRepository<FactureDetectee, Long> {
    //List<FactureDetectee> findByStatutNotification(String statut);
    Page<FactureDetectee> findByStatutNotification(StatutNotification statut, Pageable pageable);
    boolean existsByClientIdAndTypeFactureAndDateDetection(Long clientId, TypeFacture typeFacture, LocalDate dateDetection);
}