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

    public String getTelephone()
    {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCompteurEau() {
        return compteurEau;
    }

    public void setCompteurEau(String compteurEau) {
        this.compteurEau = compteurEau;
    }

    public String getCompteurElectricite() {
        return compteurElectricite;
    }

    public void setCompteurElectricite(String compteurElectricite) {
        this.compteurElectricite = compteurElectricite;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }


}


