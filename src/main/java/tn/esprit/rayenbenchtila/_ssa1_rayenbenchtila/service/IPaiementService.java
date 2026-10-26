package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.service;

import java.util.List;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Paiement;

public interface IPaiementService {

    // Lecture seule : la création, la modification et la suppression d'un paiement
    // passent par le Contrat (cascade + orphanRemoval sur Contrat.paiements)

    Paiement findById(Long id);

    List<Paiement> findAll();
}
