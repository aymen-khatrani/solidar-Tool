-- Initialisation des catégories d'équipements solidartool
INSERT INTO categorie (nom) SELECT 'Sport'     WHERE NOT EXISTS (SELECT 1 FROM categorie WHERE nom = 'Sport');
INSERT INTO categorie (nom) SELECT 'Bricolage' WHERE NOT EXISTS (SELECT 1 FROM categorie WHERE nom = 'Bricolage');
INSERT INTO categorie (nom) SELECT 'Jardinage' WHERE NOT EXISTS (SELECT 1 FROM categorie WHERE nom = 'Jardinage');
