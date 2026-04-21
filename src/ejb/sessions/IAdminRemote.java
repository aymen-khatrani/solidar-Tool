package ejb.sessions;

import jakarta.ejb.Remote;
import ejb.entites.Particulier;
import ejb.entites.Equipement;
import ejb.entites.Emprunt;
import ejb.entites.EtatEmprunt;
import java.time.LocalDate;
import java.util.List;

@Remote
public interface IAdminRemote {

    Particulier creerParticulier(String email, String adresse, Double latitude, Double longitude)
        throws EmailDejaCreeException;

    Equipement creerEquipementNonElectrique(String emailProprietaire, String nom, Double valeur, String categorieNom)
        throws EmailInconnuException, CategorieInconnueException;

    Equipement creerEquipementElectrique(String emailProprietaire, String nom, Double valeur, String categorieNom, Boolean filaire)
        throws EmailInconnuException, CategorieInconnueException;

    List<Equipement> getEquipementsProches(Double latitude, Double longitude, String categorieNom)
        throws CategorieInconnueException;

    Emprunt demanderEmprunt(String emailEmprunteur, Long idEquipement, LocalDate dateEmprunt)
        throws EmailInconnuException, EmpruntDejaCreeException;

    List<Emprunt> getEmpruntsDeParticulier(String email)
        throws EmailInconnuException;

    void modifierEtatEmprunt(String email, Long idEmprunt, EtatEmprunt nouvelEtat)
        throws EmailInconnuException, EmpruntInconnuException;
}
