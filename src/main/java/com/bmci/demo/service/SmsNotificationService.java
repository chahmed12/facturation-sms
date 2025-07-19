package com.bmci.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bmci.demo.model.FactureDetectee;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SmsNotificationService implements Notifications {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);

    @Value("${sms.api.url}")
    private String smsApiUrl;

    @Value("${security.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void envoyerNotification(FactureDetectee facture, String telephone) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("numero", telephone);
        logger.info("📤 Appel API SMS vers {}", telephone);
        payload.put("message", facture.getMessage());

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(smsApiUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                facture.setStatutNotification(com.bmci.demo.enums.StatutNotification.ENVOYE);
                logger.info("✅ SMS envoyé à {}", telephone);
            } else {
                facture.setStatutNotification(com.bmci.demo.enums.StatutNotification.ECHEC);
                logger.warn("⚠️ SMS non délivré à {}, statut HTTP: {}", telephone, response.getStatusCode());
            }
        } catch (Exception e) {
            facture.setStatutNotification(com.bmci.demo.enums.StatutNotification.ECHEC);
            logger.error("❌ Erreur lors de l'envoi du SMS à {}: {}", telephone, e.getMessage());
            e.printStackTrace();
        }
    }
}
