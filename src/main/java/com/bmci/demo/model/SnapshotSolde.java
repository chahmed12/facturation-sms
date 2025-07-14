package com.bmci.demo.model;

import com.bmci.demo.enums.TypeFacture;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SnapshotSolde")
public class SnapshotSolde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;

    @Enumerated(EnumType.STRING)
    private TypeFacture typeFacture;

    private Double solde;

    private LocalDate dateSnapshot;

    // Les getters/setters générés par Lombok suffisent, pas besoin de redondance
}
