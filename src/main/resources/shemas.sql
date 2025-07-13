
CREATE TABLE masrivi (
    client_id INT PRIMARY KEY,
    compteur_eau VARCHAR(100),
    compteur_electricite VARCHAR(100),
    telephone VARCHAR(20)
);

CREATE TABLE SnapshotSolde (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT,
    type_facture VARCHAR(20),
    solde DOUBLE,
    date_snapshot DATE,
    CONSTRAINT fk_snapshot_client FOREIGN KEY (client_id) REFERENCES masrivi(client_id) ON DELETE CASCADE
);

CREATE TABLE FactureDetectee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT,
    type_facture VARCHAR(20),
    montant_facture DOUBLE,
    date_detection DATE,
    statut_notification VARCHAR(20),
    message TEXT,
    CONSTRAINT fk_facture_client FOREIGN KEY (client_id) REFERENCES masrivi(client_id) ON DELETE CASCADE
);

CREATE TABLE snde (
    compteur_eau VARCHAR(100) PRIMARY KEY,
    solde DOUBLE,
    CONSTRAINT fk_snde_compteur FOREIGN KEY (compteur_eau) REFERENCES masrivi(compteur_eau) ON DELETE CASCADE
);

CREATE TABLE somelec (
    compteur_electricite VARCHAR(100) PRIMARY KEY,
    solde DOUBLE,
    CONSTRAINT fk_somelec_compteur FOREIGN KEY (compteur_electricite) REFERENCES masrivi(compteur_electricite) ON DELETE CASCADE
);

-- Indexes

---masrivi
CREATE INDEX idx_masrivi_compteur_eau ON masrivi(compteur_eau);
CREATE INDEX idx_masrivi_compteur_electricite ON masrivi(compteur_electricite);
CREATE INDEX idx_masrivi_telephone ON masrivi(telephone);
--snapshot solde
CREATE INDEX idx_snapshot_client_id ON SnapshotSolde(client_id);
CREATE INDEX idx_snapshot_type_facture ON SnapshotSolde(type_facture);
CREATE INDEX idx_snapshot_date_snapshot ON SnapshotSolde(date_snapshot);
-- Facture detectee
CREATE INDEX idx_facture_client_id ON FactureDetectee(client_id);
CREATE INDEX idx_facture_type_facture ON FactureDetectee(type_facture);
CREATE INDEX idx_facture_date_detection ON FactureDetectee(date_detection);
CREATE INDEX idx_facture_statut_notification ON FactureDetectee(statut_notification);
-- Snde and Somelec
CREATE INDEX idx_snde_compteur_eau ON snde(compteur_eau);
CREATE INDEX idx_somelec_compteur_electricite ON somelec(compteur_electricite);
