# Cahier des Charges
## Système de Gestion des Soutenances et des Jurys

> **Établissement :** Université / Département Informatique  
> **Encadrant :** Pr. Mohammed AIT DAOUD  
> **Niveau :** Licence Informatique — PFE  
> **Version :** 1.0  
> **Date :** Avril 2026  

---

## Table des Matières

1. [Contexte du projet](#1-contexte-du-projet)
2. [Intitulé du projet](#2-intitulé-du-projet)
3. [Problématique](#3-problématique)
4. [Objectifs du projet](#4-objectifs-du-projet)
5. [Périmètre du projet](#5-périmètre-du-projet)
6. [Public cible](#6-public-cible)
7. [Acteurs du système](#7-acteurs-du-système)
8. [Besoins fonctionnels](#8-besoins-fonctionnels)
9. [Besoins non fonctionnels](#9-besoins-non-fonctionnels)
10. [Architecture technique proposée](#10-architecture-technique-proposée)
11. [Modules du système](#11-modules-du-système)
12. [Contraintes du projet](#12-contraintes-du-projet)
13. [Répartition du travail](#13-répartition-du-travail)
14. [Modèle de données simplifié](#14-modèle-de-données-simplifié)
15. [Livrables attendus](#15-livrables-attendus)
16. [Planning indicatif](#16-planning-indicatif)
17. [Critères de réussite](#17-critères-de-réussite)
18. [Évolutions possibles](#18-évolutions-possibles)
19. [Conclusion](#19-conclusion)

---

## 1. Contexte du projet

Dans les établissements d'enseignement supérieur, l'organisation des soutenances de projets de fin d'études, mémoires et thèses mobilise plusieurs acteurs : administration, enseignants, encadrants, jurys et étudiants.

Cette gestion est souvent réalisée de manière manuelle à l'aide de fichiers Excel, emails et documents papier, ce qui peut entraîner des erreurs, des conflits d'horaires, des oublis et une mauvaise traçabilité.

Le projet vise à développer une **application web** permettant de centraliser et d'automatiser la gestion des soutenances, depuis la planification jusqu'à l'affectation des jurys, la saisie des évaluations et la génération des documents administratifs.

---

## 2. Intitulé du projet

**Conception et développement d'un système web de gestion des soutenances et des jurys**

---

## 3. Problématique

La planification des soutenances nécessite de coordonner plusieurs contraintes : disponibilité des enseignants, affectation des jurys, répartition des salles, horaires, listes des étudiants, génération des convocations et fiches d'évaluation.

Lorsque cette organisation est manuelle, elle devient lourde, peu fiable et difficile à suivre. Il est donc nécessaire de mettre en place une application web centralisée permettant d'améliorer l'efficacité, la traçabilité et la qualité de gestion de ce processus académique.

---

## 4. Objectifs du projet

### 4.1 Objectif général

Développer une application web permettant de gérer de manière centralisée l'organisation des soutenances et des jurys dans un établissement universitaire.

### 4.2 Objectifs spécifiques

- Gérer les étudiants, encadrants, membres de jury et responsables pédagogiques
- Planifier les soutenances par date, horaire et salle
- Affecter les jurys aux soutenances
- Éviter les conflits de planning
- Générer automatiquement les convocations et les fiches d'évaluation
- Permettre la saisie des notes et observations
- Produire des états récapitulatifs et rapports administratifs

---

## 5. Périmètre du projet

### Inclus dans le périmètre

| Fonctionnalité | Priorité |
|---|---|
| Gestion des utilisateurs | Haute |
| Gestion des étudiants et projets | Haute |
| Gestion des enseignants et jurys | Haute |
| Planification des soutenances | Haute |
| Affectation des jurys | Haute |
| Réservation des salles | Haute |
| Détection de conflits horaires simples | Moyenne |
| Génération de convocations | Moyenne |
| Génération de fiches d'évaluation | Moyenne |
| Saisie des notes et délibérations simples | Moyenne |
| Tableaux de bord et exports | Basse |

### Exclus du périmètre

- Visioconférence intégrée
- Signature électronique avancée
- Authentification institutionnelle complexe (SSO)
- Gestion financière
- Intégration complète avec un ERP universitaire
- Optimisation automatique avancée par IA

---

## 6. Public cible

L'application est destinée à :
- L'administration pédagogique
- Les chefs de département
- Les coordinateurs de filière
- Les enseignants encadrants
- Les membres de jury
- Les étudiants

---

## 7. Acteurs du système

### 7.1 Administrateur
- Gère les comptes utilisateurs
- Configure les soutenances
- Gère les salles, périodes et paramètres

### 7.2 Responsable pédagogique / Coordinateur
- Crée les sessions de soutenance
- Affecte les jurys
- Valide les plannings
- Consulte les rapports

### 7.3 Enseignant / Membre de jury
- Consulte ses convocations
- Consulte les informations des étudiants
- Saisit les notes et observations

### 7.4 Étudiant
- Consulte la date, l'heure, la salle et le jury
- Télécharge sa convocation si nécessaire

---

## 8. Besoins fonctionnels

### 8.1 Gestion des utilisateurs
- Création de comptes
- Connexion et déconnexion
- Gestion des rôles : administrateur, coordinateur, enseignant/jury, étudiant

### 8.2 Gestion des étudiants
- Ajout d'étudiants (manuel et import CSV/Excel optionnel)
- Association à un projet/mémoire/PFE
- Affichage : nom, CNE, filière, niveau, titre du projet, encadrant

### 8.3 Gestion des enseignants et jurys
- Ajout et gestion des enseignants
- Identification des disponibilités
- Composition d'un jury : président, rapporteur, examinateur, encadrant

### 8.4 Gestion des salles
- Ajout de salles avec capacité
- Consultation de disponibilité
- Affectation à une soutenance

### 8.5 Planification des soutenances
- Création de sessions (date, heure, durée, salle)
- Association étudiant/projet
- Affichage du planning global

### 8.6 Affectation des jurys
- Affectation d'un jury à chaque soutenance
- Vérification de non-chevauchement
- Équilibrage de la répartition si possible

### 8.7 Gestion des conflits
Détection de : conflit de salle, conflit d'horaire pour un enseignant, double planification d'un étudiant, doublons. Le système affiche une alerte avant validation.

### 8.8 Consultation des plannings
Filtrage par : date, salle, enseignant, filière, étudiant

### 8.9 Génération de documents
Documents : convocations étudiants/jurys, planning général, fiches d'évaluation, liste de présence, PV simplifié. Formats : PDF, impression directe, export Excel/CSV.

### 8.10 Saisie des évaluations
- Saisie d'une note et remarques
- Décision : admis / admis avec corrections / ajourné
- Validation avant clôture

### 8.11 Tableau de bord
Affiche : total soutenances, jurys mobilisés, salles utilisées, soutenances planifiées par jour, soutenances évaluées vs restantes.

### 8.12 Recherche et filtres
Recherche : étudiant, enseignant. Filtrage : date, salle, filière, statut.

---

## 9. Besoins non fonctionnels

| Critère | Exigences |
|---|---|
| **Ergonomie** | Simple, claire, accessible sur ordinateur, responsive si possible |
| **Sécurité** | Authentification, hash des mots de passe, RBAC, protection des données |
| **Performance** | Affichage rapide des plannings, recherche et génération PDF rapide |
| **Fiabilité** | Pas de perte de données, gestion des erreurs, messages clairs |
| **Maintenabilité** | Code structuré, documenté, versionné Git, modulaire |

---

## 10. Architecture technique proposée

### 10.1 Architecture générale

Architecture web en 3 couches :

```
┌─────────────────────────────────────┐
│           FRONTEND (React.js)        │
│     Interfaces, Planning, Dashboard  │
└────────────────┬────────────────────┘
                 │ HTTP / REST API
┌────────────────▼────────────────────┐
│         BACKEND (Spring Boot)        │
│  Auth, Logique métier, Conflits, PDF │
└────────────────┬────────────────────┘
                 │ JPA / Hibernate
┌────────────────▼────────────────────┐
│       BASE DE DONNÉES (PostgreSQL)   │
│   Users, Étudiants, Soutenances...   │
└─────────────────────────────────────┘
```

### 10.2 Frontend — React.js

**Responsabilités :**
- Interfaces d'administration
- Écrans de planning (calendrier)
- Formulaires de création/modification
- Tableau de bord

**Stack :**
- React.js 18+
- React Router v6
- Axios (appels API)
- Tailwind CSS
- React Query (gestion état serveur)
- React-PDF / jsPDF (génération documents côté client)

### 10.3 Backend — Spring Boot

**Responsabilités :**
- Authentification JWT
- Logique métier (gestion des entités)
- Détection de conflits
- Génération de documents PDF (iText / JasperReports)
- API REST

**Stack :**
- Java 17+
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- Maven / Gradle

**Pourquoi Spring Boot ?**
Spring Boot est particulièrement adapté à ce projet car il offre une intégration native avec JPA/Hibernate pour la modélisation relationnelle complexe (jurys, soutenances, conflits), une sécurité robuste avec Spring Security, et un écosystème mature pour la génération PDF et les exports.

### 10.4 Base de données — PostgreSQL

**Entités stockées :** utilisateurs, étudiants, enseignants, salles, soutenances, jurys, évaluations

---

## 11. Modules du système

| Module | Composants |
|---|---|
| **M1 — Auth & Rôles** | Connexion, profils, droits d'accès |
| **M2 — Gestion académique** | Étudiants, projets, encadrants, enseignants |
| **M3 — Planification** | Sessions, horaires, salles, planning |
| **M4 — Gestion des jurys** | Affectation, composition, consultation |
| **M5 — Évaluation** | Fiches, notes, observations, décisions |
| **M6 — Génération documentaire** | Convocations, plannings, PV, exports |
| **M7 — Tableau de bord** | Statistiques, synthèse, suivi |

---

## 12. Contraintes du projet

- Projet destiné à un **PFE de Licence** — doit rester simple et démontrable
- Nombre de rôles limité pour éviter une complexité excessive
- Détection de conflits au moins basique mais fiable
- Génération PDF limitée à quelques documents essentiels
- Système utilisable dans un département ou une filière

---

## 13. Répartition du travail

| Domaine | Tâches |
|---|---|
| **Frontend** | Interfaces utilisateur, tableau de bord, pages planning, formulaires |
| **Backend** | API REST, gestion des utilisateurs, logique métier, planification, règles de validation |
| **BDD / Documents** | Modélisation, génération PDF, exports, rapports, tests d'intégration |

---

## 14. Modèle de données simplifié

Voir le document `UML_Diagrammes.md` pour les diagrammes détaillés.

**Entités principales :** Utilisateur, Étudiant, Enseignant, Projet, Salle, Soutenance, Jury, Évaluation, Convocation

**Relations clés :**
- Un étudiant → un projet
- Un projet → un encadrant (Enseignant)
- Une soutenance → un projet + une salle + plusieurs membres de jury
- Une soutenance → une ou plusieurs évaluations

---

## 15. Livrables attendus

- [x] Cahier des charges (ce document)
- [ ] Étude de l'existant
- [ ] Diagrammes UML (cas d'utilisation, séquence, classes)
- [ ] Maquettes de l'interface (prototype React)
- [ ] Schéma relationnel de la base
- [ ] Application web fonctionnelle
- [ ] Manuel utilisateur
- [ ] Rapport final
- [ ] Présentation de soutenance

---

## 16. Planning indicatif

| Phase | Activités | Durée estimée |
|---|---|---|
| **Phase 1 — Analyse** | Recueil besoins, étude existant, CDC | 2 semaines |
| **Phase 2 — Conception** | UML, maquettes, modèle données, architecture | 2 semaines |
| **Phase 3 — Développement** | Auth, utilisateurs, académique, planification, jurys | 4 semaines |
| **Phase 4 — Fonctions avancées** | Conflits, PDF, évaluations, dashboard | 2 semaines |
| **Phase 5 — Tests** | Tests fonctionnels, corrections, validation | 1 semaine |
| **Phase 6 — Finalisation** | Documentation, démo, soutenance | 1 semaine |

---

## 17. Critères de réussite

Le projet sera considéré comme réussi si :
- Les utilisateurs peuvent être gérés par rôle
- Les étudiants, enseignants et salles peuvent être enregistrés
- Une soutenance peut être planifiée correctement
- Un jury peut être affecté sans conflit simple
- Les convocations peuvent être générées
- Les évaluations peuvent être saisies
- Le planning est consultable et exploitable

---

## 18. Évolutions possibles

À moyen terme :
- Notifications email automatiques
- Import Excel avancé
- Génération automatique optimisée des plannings
- Module de visioconférence
- Signature électronique
- Archivage des soutenances passées
- Statistiques annuelles par filière
- Intégration avec le système d'information de l'établissement

---

## 19. Conclusion

Le système de gestion des soutenances et des jurys répond à un besoin administratif et académique réel. En centralisant la planification, l'affectation des jurys et la génération documentaire dans une application web moderne (React.js + Spring Boot + PostgreSQL), ce projet permettra de réduire significativement la charge administrative tout en améliorant la fiabilité et la traçabilité du processus de soutenance.
