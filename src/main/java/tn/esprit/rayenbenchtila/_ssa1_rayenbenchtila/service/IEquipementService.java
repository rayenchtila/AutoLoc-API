package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.service;

import java.util.List;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Equipement;

public interface IEquipementService {

    Equipement create(Equipement equipement);

    Equipement findById(Long id);

    List<Equipement> findAll();

    Equipement update(Long id, Equipement equipement);

    void deleteById(Long id);
}
