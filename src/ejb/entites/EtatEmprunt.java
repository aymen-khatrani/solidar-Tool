package ejb.entites;

import java.io.Serializable;

public enum EtatEmprunt implements Serializable {
    EN_ATTENTE,   // demande en attente de réponse du loueur
    REFUSE,        // loueur a refusé la demande
    ACCEPTE,       // loueur a accepté la demande
    EMPRUNTE,      // loueur a remis l'équipement à l'emprunteur
    RETOURNE,      // emprunteur a retourné l'équipement
    ETAT_FINAL     // clôture (avec calcul pénalités si retard)
}
