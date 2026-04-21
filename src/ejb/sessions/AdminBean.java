package ejb.sessions;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import ejb.entites.Particulier;
import ejb.entites.Categorie;
import ejb.entites.Equipement;
import ejb.entites.EquipementElectrique;
import ejb.entites.Emprunt;
import ejb.entites.EtatEmprunt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Stateless
public class AdminBean implements IAdminRemote, IAdminLocal {

    @PersistenceContext(unitName = "solidar_tool")
    private EntityManager em;

    private static final double RAYON_KM              = 5.0;
    private static final int    CREDITS_CREATION       = 50;
    private static final int    CREDITS_NOUVEL_EQUIPEMENT = 2;
    private static final int    PENALITE_BASE          = 2;
    private static final int    PENALITE_VALEUR        = 1;
    private static final int    PENALITE_ELECTRIQUE    = 1;
    private static final int    PENALITE_SANSFIL       = 1;

    // ─── Création particulier ───────────────────────────────────────────────

    @Override
    public Particulier creerParticulier(String email, String adresse, Double latitude, Double longitude)
            throws EmailDejaCreeException {
        TypedQuery<Long> q = em.createQuery(
            "SELECT COUNT(p) FROM Particulier p WHERE p.email = :email", Long.class);
        q.setParameter("email", email);
        if (q.getSingleResult() > 0) {
            throw new EmailDejaCreeException();
        }
        Particulier p = new Particulier(email, adresse, longitude, latitude, CREDITS_CREATION);
        em.persist(p);
        return p;
    }

    // ─── Création équipements ───────────────────────────────────────────────

    @Override
    public Equipement creerEquipementNonElectrique(String emailProprietaire, String nom, Double valeur,
            String categorieNom) throws EmailInconnuException, CategorieInconnueException {
        Particulier proprietaire = findParticulierByEmail(emailProprietaire);
        Categorie categorie = findCategorieByNom(categorieNom);
        Equipement e = new Equipement(nom, valeur, proprietaire, categorie);
        em.persist(e);
        proprietaire.setNbCredit(proprietaire.getNbCredit() + CREDITS_NOUVEL_EQUIPEMENT);
        return e;
    }

    @Override
    public Equipement creerEquipementElectrique(String emailProprietaire, String nom, Double valeur,
            String categorieNom, Boolean filaire) throws EmailInconnuException, CategorieInconnueException {
        Particulier proprietaire = findParticulierByEmail(emailProprietaire);
        Categorie categorie = findCategorieByNom(categorieNom);
        EquipementElectrique e = new EquipementElectrique(nom, valeur, filaire, proprietaire, categorie);
        em.persist(e);
        proprietaire.setNbCredit(proprietaire.getNbCredit() + CREDITS_NOUVEL_EQUIPEMENT);
        return e;
    }

    // ─── Recherche de proximité ─────────────────────────────────────────────

    @Override
    public List<Equipement> getEquipementsProches(Double latitude, Double longitude, String categorieNom)
            throws CategorieInconnueException {
        findCategorieByNom(categorieNom);
        TypedQuery<Equipement> query = em.createQuery(
            "SELECT e FROM Equipement e WHERE e.categorie.nom = :cat", Equipement.class);
        query.setParameter("cat", categorieNom);
        List<Equipement> tous = query.getResultList();

        List<Equipement> proches = new ArrayList<>();
        for (Equipement e : tous) {
            Particulier proprio = e.getProprietaire();
            double distance = Utils.calculerDistance(latitude, longitude,
                    proprio.getLatitude(), proprio.getLongitude());
            if (distance <= RAYON_KM) {
                proches.add(e);
            }
        }
        return proches;
    }

    // ─── Demande d'emprunt ──────────────────────────────────────────────────

    @Override
    public Emprunt demanderEmprunt(String emailEmprunteur, Long idEquipement, LocalDate dateEmprunt)
            throws EmailInconnuException, EmpruntDejaCreeException {

        Particulier emprunteur = findParticulierByEmail(emailEmprunteur);

        Equipement equipement = em.find(Equipement.class, idEquipement);
        if (equipement == null) {
            throw new EmpruntDejaCreeException();
        }

        // Ne peut pas emprunter son propre équipement
        if (equipement.getProprietaire().getId().equals(emprunteur.getId())) {
            throw new EmpruntDejaCreeException();
        }

        // La demande doit se faire au moins 1 jour avant
        if (!dateEmprunt.isAfter(LocalDate.now())) {
            throw new EmpruntDejaCreeException();
        }

        // Même équipement non déjà demandé pour ce jour (états actifs)
        List<EtatEmprunt> etatsActifs = Arrays.asList(
            EtatEmprunt.EN_ATTENTE, EtatEmprunt.ACCEPTE, EtatEmprunt.EMPRUNTE);
        TypedQuery<Long> doubleCheck = em.createQuery(
            "SELECT COUNT(em) FROM Emprunt em " +
            "WHERE em.equipement.id = :eqId AND em.dateEmprunt = :date AND em.etat IN :etats",
            Long.class);
        doubleCheck.setParameter("eqId", idEquipement);
        doubleCheck.setParameter("date", dateEmprunt);
        doubleCheck.setParameter("etats", etatsActifs);
        if (doubleCheck.getSingleResult() > 0) {
            throw new EmpruntDejaCreeException();
        }

        // Éligibilité : 1. l'emprunteur a proposé au moins un équipement
        TypedQuery<Long> nbEquipQuery = em.createQuery(
            "SELECT COUNT(e) FROM Equipement e WHERE e.proprietaire.id = :id", Long.class);
        nbEquipQuery.setParameter("id", emprunteur.getId());
        if (nbEquipQuery.getSingleResult() == 0) {
            throw new EmpruntDejaCreeException();
        }

        // Éligibilité : 2. solde de crédits strictement positif
        if (emprunteur.getNbCredit() <= 0) {
            throw new EmpruntDejaCreeException();
        }

        // Éligibilité : 3. aucun équipement emprunté non retourné en retard
        // (état EMPRUNTE dont la date d'emprunt est passée)
        TypedQuery<Long> overdueQuery = em.createQuery(
            "SELECT COUNT(em) FROM Emprunt em " +
            "WHERE em.emprunteur.id = :id AND em.etat = :etat AND em.dateEmprunt < :today",
            Long.class);
        overdueQuery.setParameter("id", emprunteur.getId());
        overdueQuery.setParameter("etat", EtatEmprunt.EMPRUNTE);
        overdueQuery.setParameter("today", LocalDate.now());
        if (overdueQuery.getSingleResult() > 0) {
            throw new EmpruntDejaCreeException();
        }

        Emprunt emprunt = new Emprunt(dateEmprunt, null, EtatEmprunt.EN_ATTENTE, emprunteur, equipement);
        em.persist(emprunt);
        return emprunt;
    }

    // ─── Consultation emprunts ──────────────────────────────────────────────

    @Override
    public List<Emprunt> getEmpruntsDeParticulier(String email) throws EmailInconnuException {
        findParticulierByEmail(email);
        TypedQuery<Emprunt> query = em.createQuery(
            "SELECT em FROM Emprunt em WHERE em.equipement.proprietaire.email = :email",
            Emprunt.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    // ─── Modification d'état d'emprunt ──────────────────────────────────────

    @Override
    public void modifierEtatEmprunt(String email, Long idEmprunt, EtatEmprunt nouvelEtat)
            throws EmailInconnuException, EmpruntInconnuException {

        Particulier acteur = findParticulierByEmail(email);
        Emprunt emprunt = em.find(Emprunt.class, idEmprunt);
        if (emprunt == null) {
            throw new EmpruntInconnuException();
        }

        EtatEmprunt etatActuel = emprunt.getEtat();
        Particulier loueur     = emprunt.getLoueur();
        Particulier emprunteur = emprunt.getEmprunteur();
        LocalDate   de         = emprunt.getDateEmprunt(); // date prévue
        LocalDate   dj         = LocalDate.now();           // date du jour

        boolean estLoueur     = loueur.getId().equals(acteur.getId());
        boolean estEmprunteur = emprunteur.getId().equals(acteur.getId());

        switch (nouvelEtat) {

            case REFUSE:
                // Loueur uniquement, depuis EN_ATTENTE, avant la date d'emprunt (dj < de)
                if (!estLoueur || etatActuel != EtatEmprunt.EN_ATTENTE || !dj.isBefore(de)) {
                    throw new EmpruntInconnuException();
                }
                // -1 crédit au loueur
                loueur.setNbCredit(Math.max(0, loueur.getNbCredit() - 1));
                break;

            case ACCEPTE:
                // Loueur uniquement, depuis EN_ATTENTE, avant la date d'emprunt (dj < de)
                if (!estLoueur || etatActuel != EtatEmprunt.EN_ATTENTE || !dj.isBefore(de)) {
                    throw new EmpruntInconnuException();
                }
                // +2 crédits au loueur
                loueur.setNbCredit(loueur.getNbCredit() + 2);
                break;

            case EMPRUNTE:
                // Loueur uniquement, depuis ACCEPTE, le jour de l'emprunt (dj = de)
                if (!estLoueur || etatActuel != EtatEmprunt.ACCEPTE || !dj.isEqual(de)) {
                    throw new EmpruntInconnuException();
                }
                // +1 crédit à l'emprunteur
                emprunteur.setNbCredit(emprunteur.getNbCredit() + 1);
                break;

            case RETOURNE:
                // Emprunteur uniquement, depuis EMPRUNTE, le jour ou après (dj >= de)
                if (!estEmprunteur || etatActuel != EtatEmprunt.EMPRUNTE || dj.isBefore(de)) {
                    throw new EmpruntInconnuException();
                }
                emprunt.setDateRetour(dj);
                // +1 crédit à l'emprunteur si retourné le jour prévu (dj = de)
                if (dj.isEqual(de)) {
                    emprunteur.setNbCredit(emprunteur.getNbCredit() + 1);
                }
                break;

            case ETAT_FINAL:
                // Loueur ou emprunteur, depuis REFUSE/EN_ATTENTE/ACCEPTE/RETOURNE, après la date (dj > de)
                if (!estLoueur && !estEmprunteur) {
                    throw new EmpruntInconnuException();
                }
                if (!dj.isAfter(de)) {
                    throw new EmpruntInconnuException();
                }
                if (etatActuel != EtatEmprunt.REFUSE
                        && etatActuel != EtatEmprunt.EN_ATTENTE
                        && etatActuel != EtatEmprunt.ACCEPTE
                        && etatActuel != EtatEmprunt.RETOURNE) {
                    throw new EmpruntInconnuException();
                }
                // Effets sur les crédits selon l'état précédent
                if (etatActuel == EtatEmprunt.EN_ATTENTE) {
                    // Demande expirée sans réponse : -2 crédits au loueur
                    int p = Math.min(2, loueur.getNbCredit());
                    loueur.setNbCredit(loueur.getNbCredit() - p);
                } else if (etatActuel == EtatEmprunt.ACCEPTE) {
                    // Acceptée mais non réalisée : -2 crédits à l'emprunteur
                    int p = Math.min(2, emprunteur.getNbCredit());
                    emprunteur.setNbCredit(emprunteur.getNbCredit() - p);
                } else if (etatActuel == EtatEmprunt.RETOURNE) {
                    // Appliquer la fonction de coût si retour tardif
                    calculerEtAppliquerPenalites(emprunt, loueur, emprunteur);
                }
                // REFUSE → ETAT_FINAL : pas de changement de crédits
                break;

            default:
                throw new EmpruntInconnuException();
        }

        emprunt.setEtat(nouvelEtat);
    }

    // ─── Méthodes privées ───────────────────────────────────────────────────

    private void calculerEtAppliquerPenalites(Emprunt emprunt, Particulier loueur,
            Particulier emprunteur) {

        LocalDate dateRetour  = emprunt.getDateRetour();
        LocalDate dateEmprunt = emprunt.getDateEmprunt();

        // Pas de pénalité si retourné à temps (le jour prévu ou avant)
        if (dateRetour == null || !dateRetour.isAfter(dateEmprunt)) {
            return;
        }

        // Retour en retard : calcul des pénalités
        int penalite = PENALITE_BASE;
        if (emprunt.getEquipement().getValeur() > 100.0) {
            penalite += PENALITE_VALEUR;
        }
        if (emprunt.getEquipement() instanceof EquipementElectrique) {
            penalite += PENALITE_ELECTRIQUE;
            EquipementElectrique eq = (EquipementElectrique) emprunt.getEquipement();
            if (Boolean.FALSE.equals(eq.getFilaire())) {
                penalite += PENALITE_SANSFIL;
            }
        }

        int penaliteEffective = Math.min(penalite, emprunteur.getNbCredit());
        emprunteur.setNbCredit(emprunteur.getNbCredit() - penaliteEffective);
        loueur.setNbCredit(loueur.getNbCredit() + penaliteEffective);
    }

    private Particulier findParticulierByEmail(String email) throws EmailInconnuException {
        try {
            TypedQuery<Particulier> q = em.createQuery(
                "SELECT p FROM Particulier p WHERE p.email = :email", Particulier.class);
            q.setParameter("email", email);
            return q.getSingleResult();
        } catch (NoResultException e) {
            throw new EmailInconnuException();
        }
    }

    private Categorie findCategorieByNom(String nom) throws CategorieInconnueException {
        try {
            TypedQuery<Categorie> q = em.createQuery(
                "SELECT c FROM Categorie c WHERE c.nom = :nom", Categorie.class);
            q.setParameter("nom", nom);
            return q.getSingleResult();
        } catch (NoResultException e) {
            throw new CategorieInconnueException();
        }
    }
}
