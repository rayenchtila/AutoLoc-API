package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.service;

import java.util.List;
import tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.Entity.Client;

public interface IClientService {

    Client create(Client client);

    Client findById(Long id);

    List<Client> findAll();

    Client update(Long id, Client client);

    void deleteById(Long id);
}
