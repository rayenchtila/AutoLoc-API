package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String ressource, Long id) {
        super(ressource + " introuvable avec l'identifiant " + id);
    }
}
