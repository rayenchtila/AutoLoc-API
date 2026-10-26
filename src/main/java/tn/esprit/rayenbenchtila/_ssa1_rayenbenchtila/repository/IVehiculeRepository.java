package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Vehicule;

public interface IVehiculeRepository extends JpaRepository<Vehicule, Long> {
}
