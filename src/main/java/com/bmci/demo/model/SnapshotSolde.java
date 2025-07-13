package com.bmci.demo.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SnapshotSolde")
public class SnapshotSolde {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;
    private String typeFacture; // "EAU" ou "ELECTRICITE"
    private Double solde;
    private LocalDate dateSnapshot;


    public Long getClientId()
    {
        return clientId;
    }
    public String getTypeFacture()
    {
        return typeFacture;
    }

    public Double getSolde()
    {
        return solde;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public void setTypeFacture(String typeFacture) {
        this.typeFacture = typeFacture;
    }
    public void setSolde(Double solde) {
        this.solde = solde;
    }

    public void setDateSnapshot(LocalDate dateSnapshot) {
        this.dateSnapshot = dateSnapshot;
    }
    
}
