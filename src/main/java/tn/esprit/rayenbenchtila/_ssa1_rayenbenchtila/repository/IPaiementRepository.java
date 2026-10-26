package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Paiement;

public interface IPaiementRepository extends JpaRepository<Paiement, Long> {
}
