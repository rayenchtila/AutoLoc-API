package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Contrat;

public interface IContratRepository extends JpaRepository<Contrat, Long> {
}
