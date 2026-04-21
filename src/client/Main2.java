package client;

import ejb.entites.EtatEmprunt;
import ejb.sessions.IAdminRemote;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Client Java pour modifier l'état d'un emprunt.
 * Invoqué via : ant run-modifier-etat
 * États possibles : EN_ATTENTE, REFUSE, ACCEPTE, EMPRUNTE, RETOURNE, ETAT_FINAL
 */
public class Main2 {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage : Main2 <email> <idEmprunt> <nouvelEtat>");
            return;
        }

        String email       = args[0];
        String etatSaisi   = args[2];  // conservé pour l'affichage
        Long   idEmprunt;
        EtatEmprunt nouvelEtat;

        try {
            idEmprunt = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Erreur : l'identifiant d'emprunt doit être un nombre entier.");
            return;
        }

        // Normalisation : accepte ACCEPTEE/REFUSEE en plus de ACCEPTE/REFUSE
        String etatNormalise = etatSaisi.toUpperCase()
                .replace("ACCEPTEE", "ACCEPTE")
                .replace("REFUSEE",  "REFUSE");
        try {
            nouvelEtat = EtatEmprunt.valueOf(etatNormalise);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : état inconnu '" + etatSaisi + "'.");
            System.err.println("États valides : EN_ATTENTE, REFUSE, ACCEPTE, EMPRUNTE, RETOURNE, ETAT_FINAL");
            return;
        }

        IAdminRemote admin;
        try {
            InitialContext ctx = new InitialContext();
            admin = (IAdminRemote) ctx.lookup(
                "ejb:solidar_tool/solidar_toolSessions/AdminBean!ejb.sessions.IAdminRemote");
        } catch (NamingException e) {
            System.err.println("Erreur JNDI : " + e.getMessage());
            return;
        }

        System.out.println("Demande de passage d'état à " + etatSaisi + " initiée par " + email);
        System.out.println("Emprunt no " + idEmprunt);

        try {
            admin.modifierEtatEmprunt(email, idEmprunt, nouvelEtat);
            System.out.println("Résultat: l'emprunt a été modifié");
        } catch (ejb.sessions.EmailInconnuException e) {
            System.out.println("Résultat: erreur - email inconnu '" + email + "'");
        } catch (ejb.sessions.EmpruntInconnuException e) {
            System.out.println("Résultat: erreur - modification refusée (acteur non autorisé, état ou date invalide)");
        }
    }
}
