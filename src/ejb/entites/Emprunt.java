package ejb.entites;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "emprunt")
public class Emprunt implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateEmprunt;
    private LocalDate dateRetour;

    @Enumerated(EnumType.STRING)
    private EtatEmprunt etat;

    @ManyToOne
    @JoinColumn(name = "emprunteur_id", nullable = false)
    private Particulier emprunteur;

    @ManyToOne
    @JoinColumn(name = "loueur_id", nullable = false)
    private Particulier loueur;

    @ManyToOne
    @JoinColumn(name = "equipement_id", nullable = false)
    private Equipement equipement;

    public Emprunt() {}

    public Emprunt(LocalDate dateEmprunt, LocalDate dateRetour, EtatEmprunt etat,
                   Particulier emprunteur, Equipement equipement) {
        this.dateEmprunt = dateEmprunt;
        this.dateRetour = dateRetour;
        this.etat = etat;
        this.emprunteur = emprunteur;
        this.equipement = equipement;
        this.loueur = equipement.getProprietaire();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDateEmprunt() { return dateEmprunt; }
    public void setDateEmprunt(LocalDate dateEmprunt) { this.dateEmprunt = dateEmprunt; }

    public LocalDate getDateRetour() { return dateRetour; }
    public void setDateRetour(LocalDate dateRetour) { this.dateRetour = dateRetour; }

    public EtatEmprunt getEtat() { return etat; }
    public void setEtat(EtatEmprunt etat) { this.etat = etat; }

    public Particulier getEmprunteur() { return emprunteur; }
    public void setEmprunteur(Particulier emprunteur) { this.emprunteur = emprunteur; }

    public Particulier getLoueur() { return loueur; }
    public void setLoueur(Particulier loueur) { this.loueur = loueur; }

    public Equipement getEquipement() { return equipement; }
    public void setEquipement(Equipement equipement) { this.equipement = equipement; }

    @Override
    public String toString() {
        return "Emprunt{id=" + id + ", etat=" + etat + ", dateEmprunt=" + dateEmprunt + ", dateRetour=" + dateRetour + "}";
    }
}
