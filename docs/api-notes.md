# Atelier 5 — API REST, Postman et SpringDoc (notes)

Remarque générale : `application.properties` fixe `server.port=8082` (et non 8080) et
`server.servlet.context-path=/api`. Toutes les URL ci-dessous et dans la collection
Postman utilisent donc `http://localhost:8082/api/...`.

## A. Endpoints

| Contrôleur | Verbe et route | Service appelé | Succès | Erreurs |
|---|---|---|---|---|
| AgenceController | GET /api/agences | findAll() | 200 | — |
| AgenceController | GET /api/agences/{id} | findById(id) | 200 | 404 |
| AgenceController | POST /api/agences | create(agence) | 201 | 400 |
| AgenceController | PUT /api/agences/{id} | update(id, agence) | 200 | 404, 400 |
| AgenceController | DELETE /api/agences/{id} | deleteById(id) | 204 | 404 |
| ClientController | GET/GET{id}/POST/PUT/DELETE /api/clients | idem CRUD | 200/201/204 | 404, 400 |
| EmployeController | GET/GET{id}/POST/PUT/DELETE /api/employes | idem CRUD | 200/201/204 | 404, 400 |
| VehiculeController | GET/GET{id}/POST/PUT/DELETE /api/vehicules | idem CRUD | 200/201/204 | 404, 400 |
| EquipementController | GET/GET{id}/POST/PUT/DELETE /api/equipements | idem CRUD | 200/201/204 | 404, 400 |
| ReservationController | GET/GET{id}/POST/PUT/DELETE /api/reservations | idem CRUD | 200/201/204 | 404, 400 |
| ContratController | GET/GET{id}/POST/PUT/DELETE /api/contrats | idem CRUD | 200/201/204 | 404, 400 |
| MaintenanceController | GET/GET{id}/POST/PUT/DELETE /api/maintenances | idem CRUD | 200/201/204 | 404, 400 |
| PaiementController | GET /api/paiements | findAll() | 200 | — |
| PaiementController | GET /api/paiements/{id} | findById(id) | 200 | 404 |
| PaiementController | POST/PUT/DELETE /api/paiements | *(aucune méthode mappée)* | — | **405** (Spring MVC, aucune action du contrôleur) |

## B. Annotations Jackson

| Association | Annotation retenue | Justification |
|---|---|---|
| `Contrat.paiements` ↔ `Paiement.contrat` | `@JsonManagedReference("contrat-paiements")` côté `Contrat.paiements`, `@JsonBackReference("contrat-paiements")` côté `Paiement.contrat` | Seule vraie composition du modèle (`cascade = ALL`, `orphanRemoval = true`) : le paiement se crée **dans** le JSON du contrat (étape 5). La paire managed/back reference évite la boucle de sérialisation **et** fait renseigner automatiquement `Paiement.contrat` par Jackson à la désérialisation — indispensable pour que la cascade `PERSIST` écrive le bon `contrat_id`. |
| `Agence.employes`, `Agence.vehicules` | `@JsonIgnore` (déjà en place) | Enfant créé seul, référence son parent par id (`{"agence": {"idAgence": 1}}`) ; la liste inverse ne sert pas à la création donc elle est juste masquée en sortie. |
| `Client.reservations` | `@JsonIgnore` | idem — la réservation référence son client par id, jamais l'inverse. |
| `Equipement.vehicules` (ManyToMany, côté `mappedBy`) | `@JsonIgnore` | Le véhicule est le côté propriétaire (`@JoinTable`) ; la liste inverse n'est pas nécessaire en sortie. |
| `Vehicule.reservations`, `Vehicule.maintenances` | `@JsonIgnore` | Même logique : réservation/maintenance référencent le véhicule par id. |
| `Reservation.contrat` (OneToOne, côté `mappedBy`) | `@JsonIgnore` | Le `Contrat` référence sa `Reservation` (`Contrat.reservation`, propriétaire) ; la liste inverse est masquée pour éviter la boucle `Reservation → Contrat → Reservation`. |
| `Reservation.client`, `Reservation.vehicule` | *(aucune annotation)* | Attributs propriétaires, nécessaires pour que le POST du corps `{"client": {...}, "vehicule": {...}}` fonctionne à la lecture — **ne jamais** mettre `@JsonBackReference` ici (la propriété serait ignorée à la désérialisation et la réservation serait créée sans client, cf. avertissement de l'atelier). |

**Différence `@JsonBackReference` / `@JsonIgnore`** : les deux masquent le champ en
sortie (sérialisation). Mais `@JsonBackReference` reste actif en entrée
(désérialisation) — Jackson l'utilise pour relier automatiquement l'enfant à son
parent le temps de la requête — alors que `@JsonIgnore` masque le champ dans les
deux sens : rien ne peut être lu, ni écrit, via ce champ.

## C. Postman et Swagger

- **Postman** : client qui exécute réellement les requêtes HTTP (GET/POST/PUT/DELETE)
  contre l'application lancée localement, avec assertions (`pm.test`) rejouables
  d'une exécution à l'autre. Exemple concret : la requête « Consulter un contrat
  inexistant » (`GET /api/contrats/9999`) vérifie `pm.response.to.have.status(404)`
  et permet de lire le corps JSON exact renvoyé par `GlobalExceptionHandler`
  (`statut`, `message`, `horodatage`).
- **Swagger UI / SpringDoc** : ne fait aucun appel tant qu'on ne clique pas sur
  *Try it out* ; documente le contrat de l'API généré à partir des contrôleurs
  annotés (`@Tag`, `@Operation`, `@ApiResponses`) et expose le schéma JSON de
  chaque entité (section *Schemas*), utile pour explorer l'API sans écrire de
  requête à la main. Exemple concret : le groupe « Paiements » affiche uniquement
  les deux `GET`, ce qui documente visuellement le caractère lecture-seule du
  contrôleur sans avoir à lire le code.
- Les deux sont complémentaires : Postman **vérifie** le comportement réel,
  Swagger UI **documente et explore** le contrat de l'API.

## D. Anomalies SonarQube for IDE

Le plugin IntelliJ (SonarLint) n'a pas pu être exécuté dans cet environnement (pas
d'IDE graphique disponible ici) ; relecture manuelle alignée sur le référentiel
SonarJava :

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| Entités JPA exposées directement comme corps de requête/réponse (`@RequestBody Contrat`, retour `Contrat`) | Pas de règle Sonar dédiée, mais risque documenté par l'atelier lui-même (couplage API/base, champs internes visibles, ex. `idContrat` modifiable en écriture) | Non corrigée : **volontaire et provisoire** pour cet atelier (section 1.3 du sujet) — la correction propre (DTO) est l'objet du Chapitre 10. |
| Injection par attribut (`@Autowired` sur un champ de contrôleur) | S6813 *"Field injection is not recommended"* | Non présente dans le code livré : tous les contrôleurs utilisent l'injection par constructeur (`@RequiredArgsConstructor` + champ `final`), comme les services de l'Atelier 4. |
| Import inutile potentiel sur un import wildcard (`org.springframework.web.bind.annotation.*`) | Pas une anomalie Sonar réelle (wildcard volontaire, toutes les annotations `@GetMapping`/`@PostMapping`/… sont utilisées dans chaque contrôleur) — vérifié par relecture, pas de faux import. | Rien à corriger ; mentionné pour mémoire car souvent signalé à tort. |

## E. Questions de compréhension

1. **Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du contrôleur à appeler ?**
   Le `DispatcherServlet` (point d'entrée unique de Spring MVC) reçoit chaque
   requête HTTP. Il délègue au `HandlerMapping`, qui associe la paire
   (verbe HTTP, URL) à la méthode annotée correspondante (`@GetMapping`,
   `@PostMapping`, etc.) grâce aux préfixes `@RequestMapping` de la classe et au
   `context-path` global.

2. **Pourquoi `ContratController` dépend-il de `IContratService` et non de `ContratRepository` ni de `ContratServiceImpl` ?**
   Un contrôleur ne doit pas contenir de logique métier ni accéder directement à
   la persistance (contrôleur « mince ») : il délègue au service. Dépendre de
   l'**interface** du service (et non de son implémentation) garde le contrôleur
   découplé d'une implémentation concrète, remplaçable sans le modifier.

3. **Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente donne-t-elle 404 et non 500 ?**
   201 *Created* signale qu'une nouvelle ressource a été créée (le corps contient
   la ressource avec son identifiant généré). 204 *No Content* signale un succès
   sans corps à renvoyer (la ressource n'existe plus). Une ressource absente est
   une situation **attendue et gérée** par le code métier
   (`ResourceNotFoundException`, traduite en 404 par `GlobalExceptionHandler`) :
   ce n'est pas une erreur serveur imprévue (500), qui signalerait un bug ou une
   panne.

4. **Pourquoi `PaiementController` n'expose-t-il que des `GET` ? Quelle réponse reçoit un POST `/api/paiements` ?**
   `Paiement` est l'enfant d'une composition `Contrat → Paiement`
   (`cascade = ALL`, `orphanRemoval = true`) : il n'existe jamais seul, il se
   crée/modifie/supprime uniquement à travers son `Contrat`. Comme aucune méthode
   n'est mappée sur `POST /api/paiements`, Spring MVC répond automatiquement
   **405 Method Not Allowed** (l'URL existe pour `GET`, mais pas pour `POST`).

5. **Quels risques présente l'exposition directe des entités JPA ? Pourquoi `Contrat.paiements` utilise-t-il `@JsonManagedReference` alors que `Agence.vehicules` utilise `@JsonIgnore` ?**
   Risques : couplage fort entre le contrat d'API et le schéma de base (un
   changement de colonne casse le client), fuite de champs internes/sensibles,
   sur-exposition en écriture (un client pourrait envoyer `idContrat` ou une
   collection entière à la création). `Contrat.paiements` utilise
   `@JsonManagedReference` car les paiements **se créent dans le JSON du
   contrat** (composition, cascade `PERSIST`) : Jackson doit pouvoir les
   désérialiser et relier leur `contrat` automatiquement. `Agence.vehicules`
   utilise `@JsonIgnore` car un véhicule se crée **seul**, en référençant son
   agence par un identifiant (`{"agence": {"idAgence": 1}}`) : la liste inverse
   n'a aucun rôle à la désérialisation, elle est seulement masquée en sortie
   pour éviter la boucle.
