package com.bmci.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bmci.demo.model.Somelec;

public interface SomelecRepository extends JpaRepository<Somelec, String> {
    Optional<Somelec> findByCompteurElectricite(String compteurElectricite);
}
