package ejb.sessions;

import jakarta.ejb.Local;
import ejb.entites.EtatEmprunt;

@Local
public interface IAdminLocal {

    void modifierEtatEmprunt(String email, Long idEmprunt, EtatEmprunt nouvelEtat)
        throws EmailInconnuException, EmpruntInconnuException;
}
