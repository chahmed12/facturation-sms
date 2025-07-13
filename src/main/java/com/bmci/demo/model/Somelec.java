package com.bmci.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "somelec")
public class Somelec {

    @Id
    private String compteurElectricite;

    private Double solde;

    // Getters et Setters
    public String getCompteurElectricite() {
        return compteurElectricite;
    }

    public void setCompteurElectricite(String compteurElectricite) {
        this.compteurElectricite = compteurElectricite;
    }

    public Double getSolde() {
        return solde;
    }

    public void setSolde(Double solde) {
        this.solde = solde;
    }
}

