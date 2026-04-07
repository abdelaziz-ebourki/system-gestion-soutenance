# Diagrammes UML

## Système de Gestion des Soutenances et des Jurys

> **Projet :** PFE Licence Informatique  
> **Date :** Avril 2026

---

## 1. Diagramme de Cas d'Utilisation (Use Case)

```mermaid
graph LR
    %% Acteurs
    subgraph Acteurs
        Admin([👤 Administrateur])
        Coord([👤 Coordinateur])
        Enseignant([👤 Enseignant / Jury])
        Etudiant([👤 Étudiant])
    end

    %% Cas d'utilisation
    subgraph "Administration & Auth"
        UC1(Se connecter)
        UC2(Gérer les comptes utilisateurs)
        UC3(Gérer les rôles)
    end

    subgraph "Gestion des Référentiels"
        UC4(Gérer les étudiants)
        UC5(Gérer les enseignants)
        UC6(Gérer les salles)
        UC7(Gérer les projets / PFE)
    end

    subgraph "Planification des Soutenances"
        UC8(Créer une session)
        UC9(Affecter un jury)
        UC10(Détecter les conflits)
        UC11(Valider le planning)
        UC12(Consulter le planning)
    end

    subgraph "Évaluation & Documents"
        UC13(Générer les convocations)
        UC14(Générer les fiches d'éval)
        UC15(Télécharger convocation)
        UC16(Saisir les notes)
        UC17(Enregistrer la décision)
        UC18(Consulter les évaluations)
    end

    subgraph "Pilotage"
        UC19(Consulter le Dashboard)
    end

    %% Relations Administrateur
    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Admin --> UC6

    %% Relations Coordinateur
    Coord --> UC1
    Coord --> UC4
    Coord --> UC5
    Coord --> UC7
    Coord --> UC8
    Coord --> UC9
    Coord --> UC11
    Coord --> UC12
    Coord --> UC13
    Coord --> UC14
    Coord --> UC18
    Coord --> UC19

    %% Relations Enseignant
    Enseignant --> UC1
    Enseignant --> UC12
    Enseignant --> UC16
    Enseignant --> UC17

    %% Relations Étudiant
    Etudiant --> UC1
    Etudiant --> UC12
    Etudiant --> UC15

    %% Include relations
    UC8 -.->|«include»| UC10
    UC9 -.->|«include»| UC10
    UC13 -.->|«include»| UC8
    UC14 -.->|«include»| UC8
```

---

## 2. Diagramme de Classes

```mermaid
classDiagram
    class Utilisateur {
        +Long id
        +String nom
        +String prenom
        +String email
        +String motDePasseHash
        +RoleEnum role
        +Boolean actif
        +seConnecter()
        +seDeconnecter()
    }

    class Etudiant {
        +Long id
        +String nom
        +String prenom
        +String cne
        +String filiere
        +String niveau
        +Utilisateur compte
    }

    class Enseignant {
        +Long id
        +String nom
        +String prenom
        +String email
        +String specialite
        +List~Disponibilite~ disponibilites
    }

    class Projet {
        +Long id
        +String titre
        +String description
        +String type
        +Etudiant etudiant
        +Enseignant encadrant
    }

    class Salle {
        +Long id
        +String nom
        +Integer capacite
        +Boolean disponible
        +verifierDisponibilite(date, heure)
    }

    class Soutenance {
        +Long id
        +Date date
        +Time heureDebut
        +Integer dureeMinutes
        +StatutEnum statut
        +Projet projet
        +Salle salle
        +Jury jury
        +List~Evaluation~ evaluations
        +planifier()
        +annuler()
        +verifierConflits()
    }

    class Jury {
        +Long id
        +Enseignant president
        +Enseignant rapporteur
        +Enseignant examinateur
        +Enseignant encadrant
        +Soutenance soutenance
        +verifierDisponibilites()
    }

    class Evaluation {
        +Long id
        +Float note
        +String remarques
        +DecisionEnum decision
        +Boolean validee
        +Enseignant evaluateur
        +Soutenance soutenance
        +valider()
        +modifier()
    }

    class Convocation {
        +Long id
        +TypeConvocationEnum type
        +Date dateGeneration
        +byte[] documentPDF
        +Soutenance soutenance
        +generer()
        +telecharger()
    }

    class RoleEnum {
        <<enumeration>>
        ADMINISTRATEUR
        COORDINATEUR
        ENSEIGNANT
        ETUDIANT
    }

    class StatutEnum {
        <<enumeration>>
        PLANIFIEE
        EN_COURS
        TERMINEE
        ANNULEE
    }

    class DecisionEnum {
        <<enumeration>>
        ADMIS
        ADMIS_AVEC_CORRECTIONS
        AJOURNE
    }

    %% Relations
    Utilisateur "1" -- "0..1" Etudiant : correspond à
    Utilisateur "1" -- "0..1" Enseignant : correspond à
    Etudiant "1" -- "1" Projet : possède
    Projet "1" -- "1" Enseignant : encadré par
    Soutenance "1" -- "1" Projet : concerne
    Soutenance "1" -- "1" Salle : se déroule dans
    Soutenance "1" -- "1" Jury : possède
    Jury "1" -- "1..4" Enseignant : composé de
    Soutenance "1" -- "0..*" Evaluation : reçoit
    Evaluation "1" -- "1" Enseignant : saisie par
    Soutenance "1" -- "0..*" Convocation : génère
```

---

## 3. Diagramme de Séquence — Planification d'une Soutenance

```mermaid
sequenceDiagram
    actor Coord as Coordinateur
    participant UI as Interface Web (React)
    participant API as API REST (Spring Boot)
    participant DB as Base de données

    Coord->>UI: Ouvre le formulaire de planification
    UI->>API: GET /api/etudiants?statut=non-planifie
    API->>DB: SELECT étudiants sans soutenance
    DB-->>API: Liste étudiants
    API-->>UI: JSON étudiants

    UI->>API: GET /api/salles/disponibles?date=X&heure=Y
    API->>DB: SELECT salles libres au créneau
    DB-->>API: Salles disponibles
    API-->>UI: JSON salles

    Coord->>UI: Remplit le formulaire (étudiant, date, salle, durée)
    UI->>API: POST /api/soutenances (données formulaire)

    API->>API: Vérifier conflit de salle
    API->>API: Vérifier double planification étudiant

    alt Conflit détecté
        API-->>UI: 409 Conflict + message d'erreur
        UI-->>Coord: Afficher alerte conflit
    else Aucun conflit
        API->>DB: INSERT soutenance
        DB-->>API: OK + id
        API-->>UI: 201 Created + soutenance
        UI-->>Coord: Confirmation + redirection planning
    end
```

---

## 4. Diagramme de Séquence — Affectation d'un Jury

```mermaid
sequenceDiagram
    actor Coord as Coordinateur
    participant UI as Interface Web (React)
    participant API as API REST (Spring Boot)
    participant DB as Base de données

    Coord->>UI: Sélectionne une soutenance
    UI->>API: GET /api/enseignants/disponibles?soutenanceId=X
    API->>DB: SELECT enseignants sans conflit à ce créneau
    DB-->>API: Liste enseignants disponibles
    API-->>UI: JSON enseignants

    Coord->>UI: Assigne président, rapporteur, examinateur
    UI->>API: POST /api/jurys (soutenanceId, membres)

    API->>API: Vérifier disponibilité de chaque membre
    API->>API: Vérifier qu'encadrant ≠ président

    alt Conflit détecté
        API-->>UI: 409 Conflict + détail du conflit
        UI-->>Coord: Alerte membre déjà occupé
    else OK
        API->>DB: INSERT jury + membres
        DB-->>API: OK
        API->>API: Générer convocations automatiquement
        API->>DB: INSERT convocations (étudiant + jurys)
        API-->>UI: 201 Created + jury
        UI-->>Coord: Jury affecté, convocations générées
    end
```

---

## 5. Diagramme de Séquence — Saisie d'une Évaluation

```mermaid
sequenceDiagram
    actor Jury as Membre du Jury
    participant UI as Interface Web (React)
    participant API as API REST (Spring Boot)
    participant DB as Base de données

    Jury->>UI: Accède à sa liste de soutenances
    UI->>API: GET /api/soutenances?juryMembre=userId
    API->>DB: SELECT soutenances du membre
    DB-->>API: Liste soutenances
    API-->>UI: JSON soutenances

    Jury->>UI: Sélectionne une soutenance terminée
    Jury->>UI: Remplit note + remarques + décision

    UI->>API: POST /api/evaluations (soutenanceId, note, remarques, decision)
    API->>API: Vérifier que la soutenance est en statut TERMINEE
    API->>API: Vérifier que l'évaluation n'est pas encore clôturée

    API->>DB: INSERT évaluation
    DB-->>API: OK
    API-->>UI: 201 Created
    UI-->>Jury: Évaluation enregistrée avec succès

    Note over API,DB: Si tous les membres ont évalué,<br/>statut soutenance → CLOTUREE
```

---

## 6. Diagramme d'État — Cycle de vie d'une Soutenance

```mermaid
stateDiagram-v2
    [*] --> CREEE : Coordinateur crée la session

    CREEE --> PLANIFIEE : Date + salle assignées\n(aucun conflit détecté)
    CREEE --> CREEE : Conflit détecté → correction requise

    PLANIFIEE --> JURY_AFFECTE : Jury composé\net convocations générées

    JURY_AFFECTE --> EN_COURS : Date de soutenance atteinte

    EN_COURS --> TERMINEE : Soutenance effectuée

    TERMINEE --> EVALUEE : Notes saisies par tous\nles membres du jury

    EVALUEE --> CLOTUREE : Validation coordinateur\n(PV généré)

    PLANIFIEE --> ANNULEE : Annulation administrative
    JURY_AFFECTE --> ANNULEE : Annulation administrative
    EN_COURS --> ANNULEE : Cas exceptionnel

    ANNULEE --> [*]
    CLOTUREE --> [*]
```

---

## 7. Schéma Relationnel de la Base de Données

```mermaid
erDiagram
    %% --- Entités Utilisateurs ---
    UTILISATEUR ||--o| ETUDIANT : "correspond à"
    UTILISATEUR ||--o| ENSEIGNANT : "correspond à"
    
    UTILISATEUR {
        bigint id PK
        varchar nom
        varchar prenom
        varchar email UK
        varchar mot_de_passe_hash
        varchar role
        boolean actif
    }

    ETUDIANT {
        bigint id PK
        varchar nom
        varchar prenom
        varchar cne UK
        varchar filiere
        bigint utilisateur_id FK
    }

    ENSEIGNANT {
        bigint id PK
        varchar nom
        varchar prenom
        varchar email UK
        varchar specialite
        bigint utilisateur_id FK
    }

    %% --- Entités Académiques ---
    ETUDIANT ||--|| PROJET : "possède"
    ENSEIGNANT ||--o| PROJET : "encadre"
    PROJET ||--o| SOUTENANCE : "concerne"
    
    PROJET {
        bigint id PK
        varchar titre
        text description
        varchar type
        bigint etudiant_id FK
        bigint encadrant_id FK
    }

    SALLE {
        bigint id PK
        varchar nom
        int capacite
        boolean disponible
    }

    %% --- Entités Soutenance & Jury ---
    SALLE ||--o| SOUTENANCE : "accueille"
    SOUTENANCE ||--o| JURY : "possède"
    ENSEIGNANT ||--o| JURY : "membre de"
    
    SOUTENANCE {
        bigint id PK
        date date_soutenance
        time heure_debut
        int duree_minutes
        varchar statut
        bigint projet_id FK
        bigint salle_id FK
    }

    JURY {
        bigint id PK
        bigint soutenance_id FK
        bigint president_id FK
        bigint rapporteur_id FK
        bigint examinateur_id FK
        bigint encadrant_id FK
    }

    %% --- Entités Post-Soutenance ---
    SOUTENANCE ||--o| EVALUATION : "reçoit"
    ENSEIGNANT ||--o| EVALUATION : "saisit"
    SOUTENANCE ||--o| CONVOCATION : "génère"
    UTILISATEUR ||--o| CONVOCATION : "reçoit"

    EVALUATION {
        bigint id PK
        float note
        text remarques
        varchar decision
        bigint evaluateur_id FK
        bigint soutenance_id FK
    }

    CONVOCATION {
        bigint id PK
        varchar type
        timestamp date_generation
        bytea document_pdf
        bigint soutenance_id FK
        bigint destinataire_id FK
    }
```

---

## 8. Architecture des Composants Frontend (React)

```mermaid
graph LR
    App[App.jsx] --> Router[React Router]

    subgraph "Authentification & Accueil"
        Router --> Auth[login — AuthPage]
        Router --> Dashboard[dashboard — DashboardPage]
    end

    subgraph "Gestion des Données"
        Router --> Students[etudiants — EtudiantsPage]
        Router --> Teachers[enseignants — EnseignantsPage]
        Router --> Rooms[salles — SallesPage]
    end

    subgraph "Planification & Soutenances"
        Router --> Planning[planning — PlanningPage]
        Router --> Defenses[soutenances — SoutenancesPage]
        Router --> Jurys[jurys — JurysPage]
    end

    subgraph "Résultats & Documents"
        Router --> Evals[evaluations — EvaluationsPage]
        Router --> Docs[documents — DocumentsPage]
    end

    %% Détails des composants
    Dashboard --> DashStats[Stats]
    Dashboard --> DashCalendar[MiniCal]
    
    Planning --> PlanCalendar[Calendrier]
    
    Defenses --> DefenseForm[FormSoutenance]
    DefenseForm --> ConflictChecker[DetecteurConflits]

    Jurys --> JuryForm[FormJury]

    subgraph "Couche Services (Axios/Logic)"
        AuthService[authService.js]
        ApiService[apiService.js]
        PdfService[pdfService.js]
    end

    App --> AuthService
    DefenseForm --> ApiService
    JuryForm --> ApiService
    Docs --> PdfService
```

---

## 9. Récapitulatif des Diagrammes produits

| Diagramme                   | Type UML       | Objectif                            |
| --------------------------- | -------------- | ----------------------------------- |
| Cas d'utilisation           | Use Case       | Identifier les acteurs et fonctions |
| Diagramme de classes        | Structurel     | Modéliser les entités et relations  |
| Séquence — Planification    | Comportemental | Flux de création d'une soutenance   |
| Séquence — Affectation jury | Comportemental | Flux de composition d'un jury       |
| Séquence — Évaluation       | Comportemental | Flux de saisie des notes            |
| Diagramme d'état            | Comportemental | Cycle de vie d'une soutenance       |
| Schéma relationnel (ER)     | Données        | Structure de la base de données     |
| Architecture composants     | Structurel     | Organisation du frontend React      |
