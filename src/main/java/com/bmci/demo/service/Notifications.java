package com.bmci.demo.service;

import com.bmci.demo.model.FactureDetectee;

public interface Notifications {
    void envoyerNotification(FactureDetectee facture, String telephone);
}
