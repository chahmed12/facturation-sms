package com.bmci.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TachesPlanifiees {

    @Autowired
    private FactureService factureService;

    // Toutes les 2 minutes : snapshot
    @Scheduled(cron = "0 */2 * * * *")
    public void snapshot() {
        System.out.println(" Snapshot en cours...");
        factureService.sauvegarderSnapshotSoldes();
    }

    // Toutes les 3 minutes : détection
    @Scheduled(cron = "0 */3 * * * *")
    public void detecter() {
        System.out.println(" Détection en cours...");
        factureService.detecterFactures();
    }

    // Toutes les 4 minutes : notification
    @Scheduled(cron = "0 */4 * * * *")
    public void notifier() {
        System.out.println(" Notification en cours...");
        factureService.envoyerNotificationsParBatch(1000);
    }

    // Toutes les 30 minutes : purge
    @Scheduled(cron = "0 */30 * * * *")
    public void purger() {
        factureService.purgerAnciennesDonnees();
    }
}
