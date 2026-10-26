package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.service;

import java.util.List;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Vehicule;

public interface IVehiculeService {

    Vehicule create(Vehicule vehicule);

    Vehicule findById(Long id);

    List<Vehicule> findAll();

    Vehicule update(Long id, Vehicule vehicule);

    void deleteById(Long id);
}
