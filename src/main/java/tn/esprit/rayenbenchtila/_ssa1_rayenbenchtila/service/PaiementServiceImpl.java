package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Paiement;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.exception.ResourceNotFoundException;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.repository.IPaiementRepository;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements IPaiementService {

    private final IPaiementRepository paiementRepository;

    @Override
    public Paiement findById(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", id));
    }

    @Override
    public List<Paiement> findAll() {
        return paiementRepository.findAll();
    }
}
