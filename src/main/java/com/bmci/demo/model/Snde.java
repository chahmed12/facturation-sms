package com.bmci.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "snde")
public class Snde {

    @Id
    private String compteurEau;

    private Double solde;

}

