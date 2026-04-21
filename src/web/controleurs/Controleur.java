package web.controleurs;

import java.io.IOException;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ejb.entites.EtatEmprunt;
import ejb.sessions.IAdminLocal;
import ejb.sessions.EmailInconnuException;
import ejb.sessions.EmpruntInconnuException;

@WebServlet("/modifierEtat")
public class Controleur extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private IAdminLocal admin;

    private static final String VUE_FORMULAIRE = "/vueForm.jsp";
    private static final String VUE_RESULTAT   = "/resultatForm.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher(VUE_FORMULAIRE).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email          = request.getParameter("email");
        String codeEmpruntStr = request.getParameter("codeEmprunt");
        String nouvelEtatStr  = request.getParameter("newEtat");

        // Validation minimale
        if (email == null || email.trim().isEmpty()
                || codeEmpruntStr == null || codeEmpruntStr.trim().isEmpty()
                || nouvelEtatStr  == null || nouvelEtatStr.trim().isEmpty()) {
            request.setAttribute("erreur", "Tous les champs sont obligatoires.");
            getServletContext().getRequestDispatcher(VUE_FORMULAIRE).forward(request, response);
            return;
        }

        Long codeEmprunt;
        try {
            codeEmprunt = Long.parseLong(codeEmpruntStr.trim());
        } catch (NumberFormatException e) {
            request.setAttribute("erreur", "Le code emprunt doit être un nombre entier.");
            getServletContext().getRequestDispatcher(VUE_FORMULAIRE).forward(request, response);
            return;
        }

        EtatEmprunt nouvelEtat;
        try {
            nouvelEtat = EtatEmprunt.valueOf(nouvelEtatStr.trim());
        } catch (IllegalArgumentException e) {
            request.setAttribute("erreur", "État invalide : " + nouvelEtatStr);
            getServletContext().getRequestDispatcher(VUE_FORMULAIRE).forward(request, response);
            return;
        }

        // Paramètres transmis à la vue résultat
        request.setAttribute("email",       email.trim());
        request.setAttribute("codeEmprunt", codeEmprunt);
        request.setAttribute("nouvelEtat",  nouvelEtat);

        try {
            admin.modifierEtatEmprunt(email.trim(), codeEmprunt, nouvelEtat);
            request.setAttribute("succes", true);
        } catch (EmailInconnuException e) {
            request.setAttribute("succes", false);
            request.setAttribute("erreur", "Email inconnu : " + email.trim());
        } catch (EmpruntInconnuException e) {
            request.setAttribute("succes", false);
            request.setAttribute("erreur",
                "Modification refusée : vérifiez l'identifiant de l'emprunt, "
                + "l'état actuel et vos droits (loueur/emprunteur).");
        } catch (Exception e) {
            request.setAttribute("succes", false);
            request.setAttribute("erreur", "Erreur inattendue : " + e.getMessage());
        }

        getServletContext().getRequestDispatcher(VUE_RESULTAT).forward(request, response);
    }
}
