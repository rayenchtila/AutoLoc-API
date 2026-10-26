# Atelier 3 — Couche Repository (notes)

## A. Choix d'interface

Toutes les interfaces du projet AutoLoc étendent `JpaRepository<Entité, Long>` :
application JPA complète (CRUD, listes typées `List`, tri, pagination, `flush`),
retenue comme convention du projet plutôt que `CrudRepository` (qui renvoie des
`Iterable`) ou `Repository` (qui oblige à recopier chaque signature).

| Interface | Étend | Justification |
|---|---|---|
| IAgenceRepository | JpaRepository<Agence, Long> | CRUD complet, `findAll` en `List`. |
| IClientRepository | JpaRepository<Client, Long> | idem. |
| IContratRepository | JpaRepository<Contrat, Long> | CRUD complet ; `deleteById` déclenche la cascade + `orphanRemoval` vers `Paiement` via le contexte de persistance. |
| IEmployeRepository | JpaRepository<Employe, Long> | CRUD complet. |
| IEquipementRepository | JpaRepository<Equipement, Long> | CRUD complet. |
| IMaintenanceRepository | JpaRepository<Maintenance, Long> | CRUD complet. |
| IPaiementRepository | JpaRepository<Paiement, Long> | Créé pour permettre la lecture ; en pratique seules `findById`/`findAll` sont exposées par la couche service — la création/suppression d'un paiement passe par le `Contrat` (composition). |
| IReservationRepository | JpaRepository<Reservation, Long> | CRUD complet. |
| IVehiculeRepository | JpaRepository<Vehicule, Long> | CRUD complet. |

Aucune interface n'est annotée `@Repository` : inutile sur une interface Spring Data,
le proxy est généré automatiquement au démarrage (log attendu :
`Found 9 JPA repository interfaces`).

## B. Anomalies SonarQube for IDE

Le plugin IntelliJ (SonarLint) n'a pas pu être exécuté dans cet environnement
(pas d'IDE graphique disponible ici). Les anomalies ci-dessous ont donc été
relevées par relecture manuelle alignée sur le référentiel de règles SonarJava ;
la ligne 1 a été effectivement corrigée dans le code, les lignes 2 et 3
correspondent aux expériences pédagogiques guidées de l'Atelier 4 (à confirmer
visuellement dans le panneau SonarQube for IDE une fois le plugin disponible).

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| `import java.util.*;` inutilisé dans `Entity/Paiement.java` (aucun type de `java.util` n'est utilisé dans la classe) | S1128 *"Unused imports should be removed"* | Import supprimé. |
| Injection par attribut (`@Autowired private IContratRepository ...`) au lieu du constructeur | S6813 *"Field injection is not recommended"* | Non présente dans le code livré : tous les services utilisent l'injection par constructeur (`@RequiredArgsConstructor` + champs `final`). |
| Levée d'une exception générique `RuntimeException` au lieu d'une exception métier dédiée | S112 *"Generic exceptions should never be thrown"* | Non présente dans le code livré : `ResourceNotFoundException` est utilisée systématiquement à la place. |

## C. Points de vigilance CRUD (`CrudRepository`/`JpaRepository`)

- `save(entity)` : `persist` (INSERT) si l'id est `null`, `merge` (SELECT puis UPDATE)
  si l'id est renseigné — toujours réutiliser l'objet retourné par `save`.
- `deleteById(id)` : ne lève aucune exception si l'id n'existe pas (comportement
  Spring Data 3) — c'est pourquoi chaque service vérifie `existsById` avant de
  supprimer, et lève `ResourceNotFoundException` sinon.
- Les suppressions « en lot » (`deleteAllInBatch`, `deleteAllByIdInBatch`) ne sont
  utilisées nulle part dans le projet : elles contournent le contexte de
  persistance et casseraient la cascade `Contrat → Paiement`.
