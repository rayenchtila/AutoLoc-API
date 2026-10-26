package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Equipement;

public interface IEquipementRepository extends JpaRepository<Equipement, Long> {
}
