package com.bmci.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "snde")
public class Snde {

    @Id
    private String compteurEau;

    private Double solde;

    // Getters et Setters
    public String getCompteurEau() {
        return compteurEau;
    }

    public void setCompteurEau(String compteurEau) {
        this.compteurEau = compteurEau;
    }

    public Double getSolde() {
        return solde;
    }

    public void setSolde(Double solde) {
        this.solde = solde;
    }
}

