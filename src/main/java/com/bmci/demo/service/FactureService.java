package com.bmci.demo.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

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
                snap.setTypeFacture("EAU");
                snap.setSolde(snde.getSolde());
                snap.setDateSnapshot(aujourdHui);
                snapshotRepo.save(snap);
            });

            // Électricité
            somelecRepo.findByCompteurElectricite(client.getCompteurElectricite()).ifPresent(somelec -> {
                SnapshotSolde snap = new SnapshotSolde();
                snap.setClientId(client.getClientId());
                snap.setTypeFacture("ELECTRICITE");
                snap.setSolde(somelec.getSolde());
                snap.setDateSnapshot(aujourdHui);
                snapshotRepo.save(snap);
            });
        }

        System.out.println("[✔] Snapshot terminé pour " + clients.size() + " clients à " + aujourdHui);
    }

    /*public void detecterFactures() {
        LocalDate aujourdhui = LocalDate.now();
        LocalDate hier = aujourdhui.minusDays(1);

        List<SnapshotSolde> snapshotsJ = snapshotRepo.findByDateSnapshot(aujourdhui);
        List<SnapshotSolde> snapshotsJ1 = snapshotRepo.findByDateSnapshot(hier);

        Map<String, Double> soldeHierMap = new HashMap<>();
        for (SnapshotSolde s : snapshotsJ1) {
            String key = s.getClientId() + "_" + s.getTypeFacture();
            soldeHierMap.put(key, s.getSolde());
        }

        int compteur = 0;
        for (SnapshotSolde s : snapshotsJ) {
            String key = s.getClientId() + "_" + s.getTypeFacture();
            Double ancienSolde = soldeHierMap.getOrDefault(key, 0.0);

            double diff = s.getSolde() - ancienSolde;
            if (diff > 0.1) {
                FactureDetectee facture = new FactureDetectee();
                facture.setClientId(s.getClientId());
                facture.setTypeFacture(s.getTypeFacture());
                facture.setMontantFacture(diff);
                facture.setDateDetection(aujourdhui);
                facture.setStatutNotification("EN_ATTENTE");
                facture.setMessage("Nouvelle facture " + s.getTypeFacture() + ": " + diff + " MRO");
                factureRepo.save(facture);
                compteur++;
            }
        }

        System.out.println("[✔] Détection terminée : " + compteur + " factures détectées.");
    }*/

    public void envoyerNotificationsParBatch(int tailleBatch) {
        int page = 0;
        Page<FactureDetectee> pageFactures;
        int totalEnvoyees = 0;

        do {
            Pageable pageable = PageRequest.of(page, tailleBatch);
            pageFactures = factureRepo.findByStatutNotification("EN_ATTENTE", pageable);

            List<FactureDetectee> toUpdate = pageFactures.getContent().stream().peek(f -> {
                banqueRepo.findById(f.getClientId()).ifPresent(bd -> {
                    String tel = bd.getTelephone();
                    System.out.println("📲 SMS vers " + tel + ": " + f.getMessage());
                    f.setStatutNotification("ENVOYE");
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
    // Récupère tous les clients ayant au moins 2 snapshots (triés)
    List<BanqueDigital> clients = banqueRepo.findAll();

    int compteur = 0;

    for (BanqueDigital client : clients) {
        Long clientId = client.getClientId();

        // EAU
        List<SnapshotSolde> snapsEau = snapshotRepo.findTop2ByClientIdAndTypeFactureOrderByDateSnapshotDesc(clientId, "EAU");
        if (snapsEau.size() == 2 && snapsEau.get(0).getSolde() > snapsEau.get(1).getSolde()) {
            double diff = snapsEau.get(0).getSolde() - snapsEau.get(1).getSolde();
            FactureDetectee facture = new FactureDetectee();
            facture.setClientId(clientId);
            facture.setTypeFacture("EAU");
            facture.setMontantFacture(diff);
            facture.setDateDetection(LocalDate.now());
            facture.setStatutNotification("EN_ATTENTE");
            facture.setMessage("Nouvelle facture EAU: " + diff + " MRO");
            factureRepo.save(facture);
            compteur++;
        }

        // ELECTRICITE
        List<SnapshotSolde> snapsElec = snapshotRepo.findTop2ByClientIdAndTypeFactureOrderByDateSnapshotDesc(clientId, "ELECTRICITE");
        if (snapsElec.size() == 2 && snapsElec.get(0).getSolde() > snapsElec.get(1).getSolde()) {
            double diff = snapsElec.get(0).getSolde() - snapsElec.get(1).getSolde();
            FactureDetectee facture = new FactureDetectee();
            facture.setClientId(clientId);
            facture.setTypeFacture("ELECTRICITE");
            facture.setMontantFacture(diff);
            facture.setDateDetection(LocalDate.now());
            facture.setStatutNotification("EN_ATTENTE");
            facture.setMessage("Nouvelle facture ELECTRICITE: " + diff + " MRO");
            factureRepo.save(facture);
            compteur++;
        }
    }

    System.out.println("[✔] Détection test OK : " + compteur + " factures détectées.");
}

}
