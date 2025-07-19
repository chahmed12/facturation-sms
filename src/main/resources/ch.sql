-- Création de la base de données
CREATE DATABASE IF NOT EXISTS facturation_sms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE facturation_sms;

-- Table principale : banque_digital
CREATE TABLE banque_digital (
    client_id VARCHAR(50) PRIMARY KEY,
    compteur_eau VARCHAR(50),
    compteur_electricite VARCHAR(50),
    telephone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_compteur_eau (compteur_eau),
    INDEX idx_compteur_electricite (compteur_electricite),
    INDEX idx_telephone (telephone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table SNDE (soldes eau)
CREATE TABLE snde (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compteur_eau VARCHAR(50) NOT NULL,
    solde DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    date_maj TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_compteur_eau (compteur_eau),
    INDEX idx_date_maj (date_maj),
    FOREIGN KEY (compteur_eau) REFERENCES banque_digital(compteur_eau) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table SOMELEC (soldes électricité)
CREATE TABLE somelec (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compteur_electricite VARCHAR(50) NOT NULL,
    solde DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    date_maj TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_compteur_electricite (compteur_electricite),
    INDEX idx_date_maj (date_maj),
    FOREIGN KEY (compteur_electricite) REFERENCES banque_digital(compteur_electricite) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table snapshot_soldes
CREATE TABLE snapshot_soldes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(50) NOT NULL,
    type_facture ENUM('EAU', 'ELECTRICITE') NOT NULL,
    solde DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    date_snapshot DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_client_type_date (client_id, type_facture, date_snapshot),
    INDEX idx_date_snapshot (date_snapshot),
    FOREIGN KEY (client_id) REFERENCES banque_digital(client_id) ON DELETE CASCADE,
    UNIQUE KEY uk_client_type_date (client_id, type_facture, date_snapshot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table factures_detectees
CREATE TABLE factures_detectees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(50) NOT NULL,
    type_facture ENUM('EAU', 'ELECTRICITE') NOT NULL,
    montant_facture DECIMAL(15,2) NOT NULL,
    date_detection TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut_notification ENUM('EN_ATTENTE', 'ENVOYE', 'ECHEC') DEFAULT 'EN_ATTENTE',
    message TEXT,
    tentatives_envoi INT DEFAULT 0,
    derniere_tentative TIMESTAMP NULL,
    erreur_message TEXT,
    
    INDEX idx_client_id (client_id),
    INDEX idx_statut_notification (statut_notification),
    INDEX idx_date_detection (date_detection),
    INDEX idx_type_facture (type_facture),
    FOREIGN KEY (client_id) REFERENCES banque_digital(client_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table pour les logs de traitement (pour la traçabilité)
CREATE TABLE traitement_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_traitement ENUM('SNAPSHOT', 'DETECTION', 'NOTIFICATION', 'PURGE') NOT NULL,
    statut ENUM('DEBUT', 'SUCCES', 'ERREUR') NOT NULL,
    message TEXT,
    nombre_traites INT DEFAULT 0,
    duree_ms BIGINT DEFAULT 0,
    date_traitement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_type_traitement (type_traitement),
    INDEX idx_date_traitement (date_traitement),
    INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Données de test
INSERT INTO banque_digital (client_id, compteur_eau, compteur_electricite, telephone) VALUES
('CLIENT001', 'EAU001', 'ELEC001', '+22222222222'),
('CLIENT002', 'EAU002', 'ELEC002', '+22222222223'),
('CLIENT003', 'EAU003', 'ELEC003', '+22222222224'),
('CLIENT004', 'EAU004', 'ELEC004', '+22222222225'),
('CLIENT005', 'EAU005', 'ELEC005', '+22222222226');

-- Données initiales pour les soldes
INSERT INTO snde (compteur_eau, solde) VALUES
('EAU001', 50000.00),
('EAU002', 75000.00),
('EAU003', 30000.00),
('EAU004', 45000.00),
('EAU005', 60000.00);

INSERT INTO somelec (compteur_electricite, solde) VALUES
('ELEC001', 80000.00),
('ELEC002', 95000.00),
('ELEC003', 70000.00),
('ELEC004', 55000.00),
('ELEC005', 85000.00);

-- Vues pour faciliter les requêtes
CREATE VIEW v_clients_complets AS
SELECT 
    bd.client_id,
    bd.telephone,
    bd.compteur_eau,
    bd.compteur_electricite,
    s.solde as solde_eau,
    so.solde as solde_electricite
FROM banque_digital bd
LEFT JOIN snde s ON bd.compteur_eau = s.compteur_eau
LEFT JOIN somelec so ON bd.compteur_electricite = so.compteur_electricite;

-- Procédures stockées pour optimiser les performances
DELIMITER //

CREATE PROCEDURE sp_purge_old_data(IN retention_days INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Purge des snapshots anciens
    DELETE FROM snapshot_soldes 
    WHERE date_snapshot < DATE_SUB(CURDATE(), INTERVAL retention_days DAY);
    
    -- Purge des factures anciennes déjà traitées
    DELETE FROM factures_detectees 
    WHERE date_detection < DATE_SUB(NOW(), INTERVAL retention_days DAY)
    AND statut_notification IN ('ENVOYE', 'ECHEC');
    
    -- Purge des logs anciens
    DELETE FROM traitement_logs 
    WHERE date_traitement < DATE_SUB(NOW(), INTERVAL retention_days DAY);
    
    COMMIT;
END//

DELIMITER ;