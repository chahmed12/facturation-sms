package com.bmci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bmci.demo.model.BanqueDigital;

public interface BanqueDigitalRepository extends JpaRepository<BanqueDigital, Long> {}
