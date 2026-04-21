package ejb.entites;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "equipement_electrique")
@DiscriminatorValue("ELECTRIQUE")
public class EquipementElectrique extends Equipement implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean filaire;

    public EquipementElectrique() {
        super();
    }

    public EquipementElectrique(String nom, Double valeur, Boolean filaire, Particulier proprietaire, Categorie categorie) {
        super(nom, valeur, proprietaire, categorie);
        this.filaire = filaire;
    }

    public Boolean getFilaire() { return filaire; }
    public void setFilaire(Boolean filaire) { this.filaire = filaire; }

    @Override
    public String toString() {
        return "EquipementElectrique{id=" + getId() + ", nom='" + getNom() + "', filaire=" + filaire + "}";
    }
}
