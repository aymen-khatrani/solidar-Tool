package ejb.entites;

import jakarta.persistence.*;
import java.util.List;
import java.io.Serializable;

@Entity
@Table(name = "equipement")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_equipement")
public class Equipement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private Double valeur;

    @ManyToOne
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Particulier proprietaire;

    @ManyToOne
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @OneToMany(mappedBy = "equipement")
    private List<Emprunt> emprunts;

    public Equipement() {}

    public Equipement(String nom, Double valeur, Particulier proprietaire, Categorie categorie) {
        this.nom = nom;
        this.valeur = valeur;
        this.proprietaire = proprietaire;
        this.categorie = categorie;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Double getValeur() { return valeur; }
    public void setValeur(Double valeur) { this.valeur = valeur; }

    public Particulier getProprietaire() { return proprietaire; }
    public void setProprietaire(Particulier proprietaire) { this.proprietaire = proprietaire; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public List<Emprunt> getEmprunts() { return emprunts; }
    public void setEmprunts(List<Emprunt> emprunts) { this.emprunts = emprunts; }

    @Override
    public String toString() {
        return "Equipement{id=" + id + ", nom='" + nom + "', valeur=" + valeur + "}";
    }
}
