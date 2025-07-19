package com.bmci.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "somelec")
public class Somelec {

    @Id
    private String compteurElectricite;

    private Double solde;


}

