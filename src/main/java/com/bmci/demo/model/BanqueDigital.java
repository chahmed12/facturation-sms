package com.bmci.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "masrivi")
public class BanqueDigital {
    @Id
    private Long clientId;

    private String compteurEau;
    private String compteurElectricite;
    private String telephone;
}


