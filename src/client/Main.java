package client;

import ejb.entites.Emprunt;
import ejb.entites.Equipement;
import ejb.entites.Particulier;
import ejb.sessions.CategorieInconnueException;
import ejb.sessions.EmailDejaCreeException;
import ejb.sessions.EmailInconnuException;
import ejb.sessions.EmpruntDejaCreeException;
import ejb.sessions.IAdminRemote;
import java.time.LocalDate;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Main {

    public static void main(String[] args) {
        InitialContext ctx = null;

        try {
            ctx = new InitialContext();
            System.out.println("Accès au service distant");

            IAdminRemote admin = (IAdminRemote) ctx.lookup(
                "ejb:solidar_tool/solidar_toolSessions/AdminBean!ejb.sessions.IAdminRemote"
            );

            System.out.println("=== Création des particuliers ===");

            Particulier bastien = admin.creerParticulier(
                "Bastien.Cazaux@polytech-lille.fr",
                "Polytech",
                50.60773717826645,
                3.1353520973453652
            );

            Particulier louis = admin.creerParticulier(
                "Louis.Roussel@univ-lille.fr",
                "CRIStAL",
                50.60709033783188,
                3.13722964358617
            );

            Particulier olivier = admin.creerParticulier(
                "Olivier.Caron@univ-lille.fr",
                "Halle Vallin",
                50.60993636922798,
                3.1350302322755135
            );

            System.out.println("Particuliers créés :");
            System.out.println("- " + bastien.getEmail());
            System.out.println("- " + louis.getEmail());
            System.out.println("- " + olivier.getEmail());

            System.out.println("\n=== Création des équipements ===");

            Equipement velo = admin.creerEquipementElectrique(
                bastien.getEmail(),
                "vélo bleu ciel électrique",
                2200.0,
                "Sport",
                false
            );

            Equipement tondeuse = admin.creerEquipementElectrique(
                louis.getEmail(),
                "tondeuse électrique",
                450.0,
                "Jardinage",
                true
            );

            Equipement raquette = admin.creerEquipementNonElectrique(
                olivier.getEmail(),
                "raquette orange",
                120.0,
                "Sport"
            );

            System.out.println("Équipements créés :");
            System.out.println("- " + velo.getId() + " | " + velo.getNom());
            System.out.println("- " + tondeuse.getId() + " | " + tondeuse.getNom());
            System.out.println("- " + raquette.getId() + " | " + raquette.getNom());

            System.out.println("\n=== Équipements proches de Louis dans la catégorie Sport ===");

            List<Equipement> equipementsProches = admin.getEquipementsProches(
                louis.getLatitude(),
                louis.getLongitude(),
                "Sport"
            );

            for (Equipement e : equipementsProches) {
                System.out.println(
                    "Id : " + e.getId()
                    + " | Nom : " + e.getNom()
                    + " | Valeur : " + e.getValeur()
                );
            }

            System.out.println("\n=== Création des demandes d'emprunt ===");

            admin.demanderEmprunt(
                louis.getEmail(),
                raquette.getId(),
                LocalDate.of(2026, 4, 10)
            );

            admin.demanderEmprunt(
                olivier.getEmail(),
                velo.getId(),
                LocalDate.of(2026, 4, 15)
            );

            System.out.println("Demandes d'emprunt créées avec succès.");

            System.out.println("\n=== Emprunts des équipements de Bastien ===");

            List<Emprunt> emprunts = admin.getEmpruntsDeParticulier(bastien.getEmail());
            for (Emprunt e : emprunts) {
                System.out.println(
                    "Id : " + e.getId()
                    + " | Équipement : " + e.getEquipement().getNom()
                    + " | État : " + e.getEtat()
                    + " | Date : " + e.getDateEmprunt()
                );
            }

        } catch (NamingException e) {
            System.err.println("Erreur JNDI : " + e.getMessage());
            e.printStackTrace();
        } catch (EmailDejaCreeException e) {
            System.err.println("Email déjà utilisé : " + e.getMessage());
            e.printStackTrace();
        } catch (EmailInconnuException e) {
            System.err.println("Email inconnu : " + e.getMessage());
            e.printStackTrace();
        } catch (CategorieInconnueException e) {
            System.err.println("Catégorie inconnue : " + e.getMessage());
            e.printStackTrace();
        } catch (EmpruntDejaCreeException e) {
            System.err.println("Emprunt impossible (doublon, date invalide, ou conditions non remplies) : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
