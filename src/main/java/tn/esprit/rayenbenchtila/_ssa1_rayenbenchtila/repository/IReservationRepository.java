package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Reservation;

public interface IReservationRepository extends JpaRepository<Reservation, Long> {
}
