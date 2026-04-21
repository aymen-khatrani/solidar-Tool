package ejb.entites;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "particulier")
public class Particulier implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String adresse;
    private Double longitude;
    private Double latitude;
    private Integer nbCredit;

    @OneToMany(mappedBy = "proprietaire")
    private List<Equipement> equipements;

    @OneToMany(mappedBy = "emprunteur")
    private List<Emprunt> empruntsCommeEmprunteur;

    public Particulier() {}

    public Particulier(String email, String adresse, Double longitude, Double latitude, Integer nbCredit) {
        this.email = email;
        this.adresse = adresse;
        this.longitude = longitude;
        this.latitude = latitude;
        this.nbCredit = nbCredit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Integer getNbCredit() { return nbCredit; }
    public void setNbCredit(Integer nbCredit) { this.nbCredit = nbCredit; }

    public List<Equipement> getEquipements() { return equipements; }
    public void setEquipements(List<Equipement> equipements) { this.equipements = equipements; }

    public List<Emprunt> getEmpruntsCommeEmprunteur() { return empruntsCommeEmprunteur; }
    public void setEmpruntsCommeEmprunteur(List<Emprunt> empruntsCommeEmprunteur) { this.empruntsCommeEmprunteur = empruntsCommeEmprunteur; }
    
    public String toString() {
        return "Particulier{id=" + id + ", email='" + email + ", adresse='" + adresse + "', nbCredit=" + nbCredit + "}";
    }
}
