package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.service;

import java.util.List;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Agence;

public interface IAgenceService {

    Agence create(Agence agence);

    Agence findById(Long id);

    List<Agence> findAll();

    Agence update(Long id, Agence agence);

    void deleteById(Long id);
}
