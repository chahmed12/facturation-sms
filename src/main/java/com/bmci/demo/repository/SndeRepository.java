package com.bmci.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bmci.demo.model.Snde;

public interface SndeRepository extends JpaRepository<Snde, String> {
    Optional<Snde> findByCompteurEau(String compteurEau);
}
