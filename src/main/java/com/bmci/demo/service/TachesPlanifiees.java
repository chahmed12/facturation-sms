package com.bmci.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TachesPlanifiees {
    
    private static final Logger logger = LoggerFactory.getLogger(TachesPlanifiees.class);
    @Autowired
    private FactureService factureService;

    // Toutes les 2 minutes : snapshot
    @Scheduled(cron = "0 */2 * * * *")
    public void snapshot() {
        logger.info("🕑 [Tâche] Snapshot déclenchée");
        factureService.sauvegarderSnapshotSoldes();
    }

    // Toutes les 3 minutes : détection
    @Scheduled(cron = "0 */3 * * * *")
    public void detecter() {
        logger.info("🕒 [Tâche] Détection déclenchée");
        factureService.detecterFactures();
    }

    // Toutes les 4 minutes : notification
    @Scheduled(cron = "0 */4 * * * *")
    public void notifier() {
        logger.info("🕓 [Tâche] Notification déclenchée");
        factureService.envoyerNotificationsParBatch(1000);
    }

    // Toutes les 30 minutes : purge
    @Scheduled(cron = "0 */30 * * * *")
    public void purger() {
        logger.info("🕞 [Tâche] Purge déclenchée");
        factureService.purgerAnciennesDonnees();
    }
}
