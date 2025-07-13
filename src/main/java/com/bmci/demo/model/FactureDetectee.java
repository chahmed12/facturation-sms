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
@Table(name = "FactureDetectee")
public class FactureDetectee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;
    private String typeFacture;
    private Double montantFacture;
    private LocalDate dateDetection;
    private String statutNotification;
    private String message;


    public void setClientId(Long ClientId)
    {
        this.clientId = ClientId;
    }

    public void setTypeFacture(String typeFacture)
    {
        this.typeFacture = typeFacture;
    }

    public void setMontantFacture(Double montantFacture)
    {
        this.montantFacture = montantFacture;
    }

    public void setDateDetection(LocalDate dateDetection)
    {
        this.dateDetection = dateDetection;
    }

    public void setStatutNotification(String statutNotification)
    {
        this.statutNotification = statutNotification;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getTypeFacture() {
        return typeFacture;
    }

    public Double getMontantFacture() {
        return montantFacture;
    }

    public LocalDate getDateDetection() {
        return dateDetection;
    }

    public String getStatutNotification() {
        return statutNotification;
    }

    public String getMessage() {
        return message;
    }




}
