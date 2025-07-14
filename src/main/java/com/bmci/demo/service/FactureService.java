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

@Service
public class FactureService {

    @Autowired private SnapshotSoldeRepository snapshotRepo;
    @Autowired private FactureDetecteeRepository factureRepo;
    @Autowired private BanqueDigitalRepository banqueRepo;
    @Autowired private SndeRepository sndeRepo;
    @Autowired private SomelecRepository somelecRepo;

    public void sauvegarderSnapshotSoldes() {
        LocalDate aujourdHui = LocalDate.now();
        List<BanqueDigital> clients = banqueRepo.findAll();

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

        System.out.println("[✔] Snapshot terminé pour " + clients.size() + " clients à " + aujourdHui);
    }

    public void envoyerNotificationsParBatch(int tailleBatch) {
        int page = 0;
        Page<FactureDetectee> pageFactures;
        int totalEnvoyees = 0;

        do {
            Pageable pageable = PageRequest.of(page, tailleBatch);
            pageFactures = factureRepo.findByStatutNotification(StatutNotification.EN_ATTENTE, pageable);

            List<FactureDetectee> toUpdate = pageFactures.getContent().stream().peek(f -> {
                banqueRepo.findById(f.getClientId()).ifPresent(bd -> {
                    String tel = bd.getTelephone();
                    System.out.println("📲 SMS vers " + tel + ": " + f.getMessage());
                    f.setStatutNotification(StatutNotification.ENVOYE);
                });
            }).collect(Collectors.toList());

            factureRepo.saveAll(toUpdate);
            totalEnvoyees += toUpdate.size();
            page++;
        } while (!pageFactures.isEmpty());

        System.out.println("[✔] " + totalEnvoyees + " notifications envoyées.");
    }

    public void purgerAnciennesDonnees() {
        LocalDate limite = LocalDate.now().minusDays(15);
        snapshotRepo.deleteByDateSnapshotBefore(limite);
        System.out.println("[🧹] Purge des snapshots avant " + limite + " effectuée.");
    }

    public void detecterFactures() {
        List<BanqueDigital> clients = banqueRepo.findAll();

        int compteur = 0;

        for (BanqueDigital client : clients) {
            Long clientId = client.getClientId();

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
            }

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
            }
        }

        System.out.println("[✔] Détection test OK : " + compteur + " factures détectées.");
    }
}
