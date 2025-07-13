package com.bmci.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bmci.demo.model.SnapshotSolde;

public interface SnapshotSoldeRepository extends JpaRepository<SnapshotSolde, Long> {
    List<SnapshotSolde> findByDateSnapshot(LocalDate date);
    void deleteByDateSnapshotBefore(LocalDate date);
    List<SnapshotSolde> findTop2ByClientIdAndTypeFactureOrderByDateSnapshotDesc(Long clientId, String typeFacture);

} 