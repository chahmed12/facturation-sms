package com.bmci.demo.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.bmci.demo.enums.TypeFacture;
import com.bmci.demo.enums.StatutNotification;
import com.bmci.demo.model.*;
import com.bmci.demo.repository.*;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FactureService {

    @Autowired private SnapshotSoldeRepository snapshotRepo;
    @Autowired private FactureDetecteeRepository factureRepo;
    @Autowired private BanqueDigitalRepository banqueRepo;
    @Autowired private SndeRepository sndeRepo;
    @Autowired private SomelecRepository somelecRepo;
    @Autowired private Notifications notifications;

    private static final Logger logger = LoggerFactory.getLogger(FactureService.class);

/*********************************************************************************************** */

    @Transactional
    public void sauvegarderSnapshotSoldes() {
        LocalDate aujourdHui = LocalDate.now();
        List<BanqueDigital> clients = banqueRepo.findAll();
        logger.info("💾 Début snapshot des soldes pour {} clients", clients.size());
        for (BanqueDigital client : clients) {
            // Eau
            sndeRepo.findByCompteurEau(client.getCompteurEau()).ifPresent(snde -> {
                SnapshotSolde snap = new SnapshotSolde();
                snap.setClientId(client.getClientId());
                snap.setTypeFacture(TypeFacture.EAU);
                snap.setSolde(snde.getSolde());
                snap.setDateSnapshot(aujourdHui);
                snapshotRepo.save(snap);
            });

            // Électricité
            somelecRepo.findByCompteurElectricite(client.getCompteurElectricite()).ifPresent(somelec -> {
                SnapshotSolde snap = new SnapshotSolde();
                snap.setClientId(client.getClientId());
                snap.setTypeFacture(TypeFacture.ELECTRICITE);
                snap.setSolde(somelec.getSolde());
                snap.setDateSnapshot(aujourdHui);
                snapshotRepo.save(snap);
            });
        }

        logger.info("✅ Snapshot terminé à {}", aujourdHui);
    }


/*********************************************************************************************** */
@Transactional
public void envoyerNotificationsParBatch(int tailleBatch) {
    int page = 0;
    Page<FactureDetectee> pageFactures;
    int totalEnvoyees = 0;
    logger.info("📤 Début envoi des notifications (batch size = {})", tailleBatch);
    do {
        // Récupération des factures en attente de notification
        Pageable pageable = PageRequest.of(page, tailleBatch);
        pageFactures = factureRepo.findByStatutNotification(StatutNotification.EN_ATTENTE, pageable);
        if (pageFactures.isEmpty()) {
            break; // Sortir si aucune facture à traiter
        }
        // Envoi des notifications
        logger.info("📤 Envoi des notifications pour la page {} ({} factures)",
                page + 1, pageFactures.getNumberOfElements());
        // Pour chaque facture, envoyer la notification
        // et mettre à jour le statut

        // Utilisation de la méthode peek pour éviter de créer une liste intermédiaire
        // et pour mettre à jour le statut de la facture
        // Correction : utiliser le repository pour trouver le téléphone du client
        // et envoyer la notification

        List<FactureDetectee> toUpdate = pageFactures.getContent().stream().peek(f -> {
            banqueRepo.findById(f.getClientId()).ifPresent(bd -> {
                String tel = bd.getTelephone();
                notifications.envoyerNotification(f, tel);
                // Mettre à jour le statut de la facture après l'envoi
                // de la notification
                // Log de l'envoi de la notification
                logger.info(" Notification envoyée pour la facture ID: {}", f.getId());
                f.setStatutNotification(StatutNotification.ENVOYE);
                
            });
            
        }).collect(Collectors.toList());
        // Vérification si la liste toUpdate est vide

        if (toUpdate.isEmpty()) {
            break; // Sortir si aucune facture à traiter
        }
        // Mise à jour des factures
        factureRepo.saveAll(toUpdate);
        totalEnvoyees += toUpdate.size();
        page++;
    } while (!pageFactures.isEmpty());


    logger.info(" {} notifications envoyées.", totalEnvoyees);
}

/*********************************************************************************************** */


    @Transactional
    public void purgerAnciennesDonnees() {
        logger.info("🧹 Début de la purge des données anciennes...");
        
        LocalDate limite = LocalDate.now().minusDays(15);
        snapshotRepo.deleteByDateSnapshotBefore(limite);
        logger.info("✅ Purge effectuée.");
        // Purge des factures détectées
    }


/*********************************************************************************************** */
    @Transactional
    public void detecterFactures() {

        logger.info("🔍 Début de la détection des factures...");
        List<BanqueDigital> clients = banqueRepo.findAll();

        int compteur = 0;
        logger.info("Détection des factures pour {} clients", clients.size());
        // Pour chaque client, vérifier les snapshots des soldes
        // pour les types de factures EAU et ELECTRICITE
        // et détecter les différences de solde
        // pour générer une facture détectée
        for (BanqueDigital client : clients) {
            Long clientId = client.getClientId();

            boolean dejaDetectee = factureRepo.existsByClientIdAndTypeFactureAndDateDetection(clientId, TypeFacture.EAU, LocalDate.now());
            if (!dejaDetectee) {
                // EAU
                List<SnapshotSolde> snapsEau = snapshotRepo.findTop2ByClientIdAndTypeFactureOrderByDateSnapshotDesc(clientId, TypeFacture.EAU);
                if (snapsEau.size() == 2 && snapsEau.get(0).getSolde() > snapsEau.get(1).getSolde()) {
                    double diff = snapsEau.get(0).getSolde() - snapsEau.get(1).getSolde();
                    FactureDetectee facture = new FactureDetectee();
                    facture.setClientId(clientId);
                    facture.setTypeFacture(TypeFacture.EAU);
                    facture.setMontantFacture(diff);
                    facture.setDateDetection(LocalDate.now());
                    facture.setStatutNotification(StatutNotification.EN_ATTENTE);
                    facture.setMessage("Nouvelle facture EAU: " + diff + " MRO");
                    factureRepo.save(facture);
                    compteur++;
                    logger.info("📌 Facture détectée : client {} - EAU: {} MRO", clientId, diff);
                }
                

                // Vérifier si une facture a déjà été détectée pour ce client et ce type de facture
                // ELECTRICITE
                List<SnapshotSolde> snapsElec = snapshotRepo.findTop2ByClientIdAndTypeFactureOrderByDateSnapshotDesc(clientId, TypeFacture.ELECTRICITE);
                if (snapsElec.size() == 2 && snapsElec.get(0).getSolde() > snapsElec.get(1).getSolde()) {
                    double diff = snapsElec.get(0).getSolde() - snapsElec.get(1).getSolde();
                    FactureDetectee facture = new FactureDetectee();
                    facture.setClientId(clientId);
                    facture.setTypeFacture(TypeFacture.ELECTRICITE);
                    facture.setMontantFacture(diff);
                    facture.setDateDetection(LocalDate.now());
                    facture.setStatutNotification(StatutNotification.EN_ATTENTE);
                    facture.setMessage("Nouvelle facture ELECTRICITE: " + diff + " MRO");
                    factureRepo.save(facture);
                    compteur++;
                    logger.info("📌 Facture détectée : client {} - ELECTRICITE: {} MRO", clientId, diff);
                }
            }

        } logger.info("✅ Détection terminée : {} factures détectées.", compteur);
    }
}
