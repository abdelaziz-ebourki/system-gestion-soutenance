# PRD — Plateforme Web de Gestion des Soutenances Académiques

# 1. Présentation générale

## 1.1 Contexte

Dans les établissements d’enseignement supérieur, l’organisation des soutenances de projets de fin d’études (PFE), mémoires et thèses implique plusieurs acteurs : administration, coordinateurs pédagogiques, enseignants, encadrants, jurys et étudiants.

Dans de nombreux cas, cette gestion est encore réalisée manuellement à l’aide de fichiers Excel, de documents papier et d’échanges par courriel. Cette approche entraîne plusieurs problèmes :

- conflits d’horaires ;
- erreurs de planification ;
- mauvaise répartition des jurys ;
- manque de traçabilité ;
- difficulté de suivi ;
- perte de temps administratif.

Le présent projet vise à concevoir et développer une plateforme web centralisée permettant d’automatiser et simplifier la gestion complète des soutenances académiques.

---

# 2. Intitulé du projet

Conception et développement d’une plateforme web de gestion des soutenances académiques et des jurys.

---

# 3. Objectifs du système

Le système doit permettre :

- la gestion centralisée des étudiants, enseignants et structures académiques ;
- la création et gestion des groupes ;
- l’affectation des projets ;
- l’affectation des encadrants ;
- la constitution des jurys ;
- la planification automatique et manuelle des soutenances ;
- la détection des conflits ;
- la gestion des évaluations ;
- la génération automatique des documents administratifs ;
- l’envoi de notifications ;
- l’archivage des sessions.

---

# 4. Portée fonctionnelle

Le système doit être réutilisable par plusieurs universités.

Il doit supporter :

- plusieurs universités ;
- plusieurs facultés ;
- plusieurs départements ;
- plusieurs filières ;
- plusieurs sessions académiques.

Le système est conçu comme une plateforme multi-tenant avec **isolation physique (schémas/bases de données séparés par université)** pour garantir une sécurité totale des données.

---

# 5. Concepts métier

## 5.1 Session globale

Une session globale représente une période académique générale.

Exemples :

- Session normale 2026
- Session de rattrapage 2026

Une session globale contient plusieurs soutenances.

---

## 5.2 Session de soutenance

Une session de soutenance représente une période spécifique de planification des soutenances.

Elle possède notamment :

- une date de début ;
- une date de fin ;
- des règles de modification ;
- des contraintes de planification.

---

## 5.3 Groupe

Un groupe représente :

- soit un groupe d’étudiants ;
- soit un étudiant individuel.

Un groupe :

- appartient à une seule session ;
- possède un seul projet ;
- ne peut contenir qu’un nombre limité d’étudiants ;
- **possède un encadrant principal et éventuellement des co-encadrants (enseignants ou externes).**

---

## 5.4 Projet

Le terme projet désigne :

- un PFE ;
- un mémoire ;
- une thèse.

Un projet peut exister sans groupe affecté.

---

# 6. Acteurs du système

## 6.1 Administrateur (Tenant Admin)

L’administrateur possède les privilèges de gestion de son institution (université, faculté ou école).

Responsabilités principales :

- gérer les facultés ;
- gérer les départements ;
- gérer les filières ;
- gérer les utilisateurs ;
- importer les données depuis des fichiers CSV/Excel ;
- définir les rôles internes ;
- configurer les paramètres de l'institution ;
- gérer les salles ;
- définir les contraintes de soutenance ;
- gérer les sessions globales ;
- définir les règles de modification ;
- activer l’envoi des invitations ;
- gérer les audits.

---

## 6.2 Coordinateur pédagogique

Le coordinateur est responsable de l’organisation académique des soutenances.

Responsabilités principales :

- gérer les projets ;
- affecter les projets aux groupes ;
- affecter les encadrants ;
- constituer les jurys ;
- planifier les soutenances ;
- modifier les planifications ;
- publier le planning ;
- gérer les conflits ;
- consulter les statistiques ;
- générer les convocations ;
- générer les PV.

---

## 6.3 Enseignant

L’enseignant peut :

- déclarer ses indisponibilités ;
- consulter son planning ;
- consulter les informations des soutenances ;
- consulter les projets ;
- accéder aux documents déposés ;
- participer aux jurys ;
- évaluer les étudiants ;
- ajouter des remarques.

---

## 6.4 Encadrant

L’encadrant représente un rôle académique associé à un projet.

Un encadrant peut être :

- un enseignant ;
- un étudiant doctorant ;
- un étudiant en master.

**Note sur les encadrants externes :** Ils accèdent au système via des **liens d’accès sécurisés (tokens temporels)** sans nécessiter de création de compte complet.

---

## 6.5 Étudiant

L’étudiant peut :

- créer ou rejoindre un groupe ;
- consulter ses informations ;
- consulter les informations du projet ;
- consulter le planning ;
- déposer les documents ;
- télécharger sa convocation ;
- consulter les résultats ;
- recevoir des notifications.

---

# 7. Gestion des jurys

La structure du jury est configurable (Président, rapporteurs, examinateurs, encadrants, membres externes). Le système interdit le cumul de rôles dans une même soutenance et l'affectation simultanée d'un enseignant à deux soutenances.

---

# 8. Planification des soutenances

Le système supporte une planification automatique et manuelle.

- **Moteur de planification :** Approche par **heuristique de priorité** pour résoudre les conflits en respectant les contraintes strictes (salle, disponibilité).

---

# 9. Gestion des évaluations

Les membres internes du jury attribuent des notes et remarques.

- **Décisions :** Admis, Admis avec corrections, Ajourné.
- **Calcul :** Moyenne pondérée avec coefficients personnalisables.

---

# 10. Gestion documentaire

Supporte PDF, PPTX, DOCX, CSV, XLSX, PUML, ZIP. Respect strict des dates limites de dépôt.

---

# 11. Authentification et sécurité

- **RBAC** (Contrôle d'accès basé sur les rôles).
- **Audit Logging** complet de toutes les opérations critiques.

---

# 12. Notifications

Email, notifications internes, et rappels automatiques pour les étapes clés du workflow (dépôts, affectations, modifications).

---

# 13. Cycle de vie d’une soutenance

États : Brouillon, Planifiée, Publiée, Terminée, Archivée (immutable).

---

# 14. Règles métier principales

- Un étudiant : 1 groupe/session.
- Un groupe : 1 projet.
- Un enseignant : 1 département.
- Pas de double affectation horaire pour les enseignants.
- Immutabilité des sessions archivées.

---

# 15. Perspectives techniques

- **Frontend :** Typescript, React avec Tailwind CSS.
- **Backend :** Spring Boot, API REST, Java.
- **Base de données :** Mysql.
- **Sécurité :** JWT, RBAC, isolation multi-tenante.

# 16. Architecture des pages

## Administrateur

- Dashboard principal
- Gestion Universités/Facultés/Départements
- Gestion Utilisateurs (import/export)
- Configuration Sessions Globales
- Audit des logs

## Coordinateur

- Dashboard de gestion (projets & groupes)
- Planificateur de soutenances (vue calendrier)
- Gestionnaire de jurys
- Interface de validation des plannings
- Rapport et Statistiques

## Enseignant / Encadrant

- Dashboard personnel
- Planning des soutenances
- Gestion des indisponibilités
- Interface d'évaluation
- Espace de dépôt (encadrants)

## Étudiant

- Dashboard Étudiant
- Gestion du groupe
- Dépôt de fichiers
- Convocation (Vue et téléchargement)
- Résultats
