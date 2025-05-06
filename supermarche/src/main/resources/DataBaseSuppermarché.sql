-- Creation de la base de données
CREATE DATABASE supermarche;
USE supermarche;

-- Table des utilisateurs
CREATE TABLE utilisateurs (
    utilisateur_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_utilisateur VARCHAR(255) NOT NULL,
    email_utilisateur VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe_utilisateur VARCHAR(255) NOT NULL,
    role_utilisateur ENUM('ADMIN', 'GERANT', 'STAFF') NOT NULL,
    date_creation_utilisateur TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des activations/désactivations des utilisateurs
CREATE TABLE activation_utilisateurs (
    activation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    modifie_par_id BIGINT NOT NULL,
    statut_avant ENUM('actif', 'inactif') NOT NULL,
    statut_apres ENUM('actif', 'inactif') NOT NULL,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(utilisateur_id) ON DELETE CASCADE,
    FOREIGN KEY (modifie_par_id) REFERENCES utilisateurs(utilisateur_id) ON DELETE CASCADE
);

-- Table des catégories de produits
CREATE TABLE categories (
    categorie_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_categorie VARCHAR(255) NOT NULL
);

-- Table des produits
CREATE TABLE produits (
    produit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_produit VARCHAR(255) NOT NULL,
    description_produit TEXT,
    prix_produit DECIMAL(10,2) NOT NULL,
    stock_produit INT NOT NULL DEFAULT 0,
    image_url_produit VARCHAR(255),
    categorie_id BIGINT,
    FOREIGN KEY (categorie_id) REFERENCES categories(categorie_id) ON DELETE SET NULL
);

-- Table des ventes
CREATE TABLE ventes (
    vente_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_vente TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    montant_total_vente DECIMAL(10,2) NOT NULL,
    staf_id BIGINT,
    FOREIGN KEY (staf_id) REFERENCES utilisateurs(utilisateur_id) ON DELETE SET NULL
);

-- Table des détails des ventes
CREATE TABLE details_vente (
    detail_vente_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vente_id BIGINT,
    produit_id BIGINT,
    quantite_vendue INT NOT NULL,
    prix_unitaire_vendu DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (vente_id) REFERENCES ventes(vente_id) ON DELETE CASCADE,
    FOREIGN KEY (produit_id) REFERENCES produits(produit_id) ON DELETE CASCADE
);

-- Table des alertes de stock
CREATE TABLE stocks_alertes (
    alerte_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    produit_id BIGINT,
    seuil_stock_minimum INT NOT NULL,
    date_alerte_stock TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (produit_id) REFERENCES produits(produit_id) ON DELETE CASCADE
);

-- Table des reçus
CREATE TABLE reçus (
    reçu_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vente_id BIGINT,
    contenu_reçu TEXT NOT NULL,
    FOREIGN KEY (vente_id) REFERENCES ventes(vente_id) ON DELETE CASCADE
);
