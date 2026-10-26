# Atelier 4 — Couche Service, IoC et injection de dépendances (notes)

## A. Modes d'injection comparés (étape 2)

| Critère | Injection par constructeur (`@RequiredArgsConstructor` + champ `final`) | Injection par attribut (`@Autowired` sur le champ) |
|---|---|---|
| Champ `final` possible | Oui | Non |
| Dépendance visible dans la signature de la classe | Oui — paramètre du constructeur généré par Lombok | Non — cachée dans le corps de la classe |
| Utilisable avec `new` hors conteneur, sans dépendance `null` | Oui — le constructeur impose la dépendance | Non — le champ reste `null` tant que le conteneur ne l'a pas injecté |
| Signalé par SonarQube for IDE | Non | Oui — règle **S6813** *"Field injection is not recommended"* |

Convention retenue pour tous les services AutoLoc : injection par constructeur,
champs `final`, dépendance typée par l'interface du repository.

## B. Services

| Service | Dépendances injectées | Mode | Justification |
|---|---|---|---|
| ContratServiceImpl | IContratRepository | Constructeur | Seule dépendance nécessaire ; la cascade vers `Paiement` est gérée par JPA (entité), pas par le service. |
| AgenceServiceImpl | IAgenceRepository | Constructeur | CRUD direct sur l'agence. |
| ClientServiceImpl | IClientRepository | Constructeur | CRUD direct sur le client. |
| EmployeServiceImpl | IEmployeRepository | Constructeur | CRUD direct sur l'employé. |
| EquipementServiceImpl | IEquipementRepository | Constructeur | CRUD direct sur l'équipement. |
| MaintenanceServiceImpl | IMaintenanceRepository | Constructeur | CRUD direct sur la maintenance. |
| ReservationServiceImpl | IReservationRepository | Constructeur | CRUD direct sur la réservation. |
| VehiculeServiceImpl | IVehiculeRepository | Constructeur | CRUD direct sur le véhicule. |
| PaiementServiceImpl | IPaiementRepository | Constructeur | Lecture seule (`findById`/`findAll`) : `Paiement` est l'enfant d'une composition `Contrat → Paiement` (cascade + orphanRemoval), donc créé/retiré uniquement via le `Contrat`. |

Aucun service ne dépend d'un autre service (pas de dépendance circulaire).
`@Autowired` n'est posé nulle part : chaque classe n'a qu'un seul constructeur,
Spring l'utilise automatiquement.

## C. Messages d'erreur (étape 3)

**Message 1 — `NoSuchBeanDefinitionException` sur `IContratService`**
Deux causes probables côté `ContratServiceImpl` :
1. l'annotation `@Service` a été oubliée ou mal importée sur la classe ;
2. la classe se trouve dans un package hors de `tn.esprit.rayenbenchtila._ssa1_rayenbenchtila`
   (hors du scan déclenché par `@SpringBootApplication`).
Correction : ajouter `@Service` et vérifier l'emplacement du fichier.

**Message 2 — `NoUniqueBeanDefinitionException` (`emailNotificateur`, `smsNotificateur`)**
Deux corrections possibles :
1. poser `@Primary` **sur la classe** de l'implémentation retenue par défaut (ex. `EmailNotificateur`) ;
2. poser `@Qualifier("smsNotificateur")` **sur le paramètre du constructeur** du
   consommateur, pour choisir explicitement l'autre bean sans toucher aux classes de notification.

**Message 3 — cycle `clientServiceImpl` ↔ `reservationServiceImpl`**
Avec l'injection par constructeur, construire `A` exige que `B` soit déjà construit,
et construire `B` exige que `A` soit déjà construit : aucun des deux objets ne peut
jamais être complet en premier. Spring ne peut pas « boucher le trou » comme il le
ferait avec un setter (objet partiellement construit) : il détecte le cycle et lève
`BeanCurrentlyInCreationException` au lieu de démarrer avec un bean incomplet.
Piste de conception : extraire la responsabilité partagée entre `Client` et
`Reservation` dans un service tiers (ou faire en sorte qu'un seul des deux services
appelle l'autre, jamais les deux).

## D. Anomalies SonarQube for IDE

Le plugin IntelliJ (SonarLint) n'a pas pu être exécuté dans cet environnement (pas
d'IDE graphique disponible ici) ; anomalie 1 a été réellement trouvée et corrigée
par relecture manuelle du code existant, anomalies 2 et 3 correspondent aux
expériences pédagogiques guidées de l'étape 2 et de l'étape 4.2 de l'atelier — à
confirmer visuellement dans le panneau SonarQube for IDE une fois le plugin disponible.

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| `import java.util.*;` inutilisé dans `Entity/Paiement.java` | S1128 *"Unused imports should be removed"* | Import supprimé. |
| Remplacement temporaire de `@RequiredArgsConstructor`/champ `final` par `@Autowired` sur le champ (expérience étape 2) | S6813 *"Field injection is not recommended"* | Non conservé dans le code livré : injection par constructeur partout. |
| Remplacement temporaire de `ResourceNotFoundException` par `RuntimeException` dans `findById` (expérience étape 4.2) | S112 *"Generic exceptions should never be thrown"* | Non conservé dans le code livré : `ResourceNotFoundException` utilisée dans tous les services. |

## E. Questions de compréhension

1. **Où est le `new` de `ContratServiceImpl` et qui l'exécute ?**
   Il n'y a aucun `new ContratServiceImpl(...)` dans le code applicatif. C'est le
   conteneur Spring (`ApplicationContext`, créé par `SpringApplication.run`) qui
   l'instancie au démarrage, parce que la classe est annotée `@Service` et se
   trouve dans le package scanné.

2. **Pourquoi un contrôleur dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ?**
   Pour dépendre d'une abstraction, pas d'une implémentation concrète : l'implémentation
   peut être remplacée, mockée en test ou changée sans modifier le contrôleur
   (faible couplage, inversion de dépendance).

3. **Pourquoi un service singleton doit-il rester sans état ? Exemple de bug.**
   Il est partagé par toutes les requêtes concurrentes. Un attribut mutable propre
   à un appel (ex. un « contrat courant » stocké dans un champ non `final`) serait
   lu/écrasé en même temps par deux threads traitant deux clients différents :
   un client verrait les données d'un autre (race condition).

4. **Pourquoi `update` charge-t-il l'entité existante avant d'appeler `save` au lieu de lui passer directement l'objet reçu ?**
   L'objet reçu peut avoir des associations non renseignées (ex. collection vide
   ou `null`). Le passer tel quel à `save` écraserait toutes les colonnes et, avec
   `cascade`/`orphanRemoval` (ex. `Contrat.paiements`), supprimerait les lignes
   liées. Charger l'existant permet de ne recopier que les attributs modifiables
   voulus, sans toucher à l'identifiant ni aux collections gérées par cascade.

5. **`@Component` / `@Bean` ; `@Primary` / `@Qualifier` ; pourquoi `IPaiementService` n'expose-t-il ni création, ni modification, ni suppression ?**
   - `@Component` (et ses spécialisations `@Service`/`@Repository`/`@Controller`) :
     posée sur une classe qu'on possède, détectée par le scan automatique.
   - `@Bean` : posée sur une méthode d'une classe `@Configuration`, pour déclarer
     explicitement un bean qu'on ne peut pas annoter soi-même (bibliothèque, JDK)
     ou qui nécessite une construction particulière.
   - `@Primary` : désigne un candidat par défaut parmi plusieurs beans du même
     type, résolu automatiquement sans précision au point d'injection.
   - `@Qualifier` : choix explicite, au point d'injection, du bean nommé voulu.
   - `Paiement` est l'enfant d'une composition `Contrat → Paiement`
     (`cascade = ALL`, `orphanRemoval = true`) : dans ce modèle, l'enfant n'est
     normalement modifié qu'à travers son parent, pour garantir la cohérence —
     `IPaiementService` reste donc volontairement en lecture seule.
