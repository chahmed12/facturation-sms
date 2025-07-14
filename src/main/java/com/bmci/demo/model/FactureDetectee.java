package com.bmci.demo.model;

import com.bmci.demo.enums.TypeFacture;
import com.bmci.demo.enums.StatutNotification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FactureDetectee")
public class FactureDetectee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;

    @Enumerated(EnumType.STRING)
    private TypeFacture typeFacture;

    private Double montantFacture;

    private LocalDate dateDetection;

    @Enumerated(EnumType.STRING)
    private StatutNotification statutNotification;

    private String message;
}
