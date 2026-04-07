# Étude de l'Existant
## Système de Gestion des Soutenances et des Jurys

> **Projet :** PFE Licence Informatique  
> **Date :** Avril 2026  

---

## 1. Introduction

Avant de concevoir le nouveau système, il est essentiel d'analyser les pratiques actuelles de gestion des soutenances dans les établissements universitaires. Cette étude vise à identifier les forces, les faiblesses et les opportunités d'amélioration des méthodes existantes.

---

## 2. Processus actuel (sans système dédié)

### 2.1 Flux de travail typique

La gestion des soutenances suit généralement le processus suivant dans la majorité des établissements sans système dédié :

```
Coordinateur                Enseignants              Étudiants
     │                           │                       │
     ├── Collecte des dispo. ────►│                       │
     │   (emails / formulaires)   │                       │
     │◄── Réponses ───────────────┤                       │
     │                           │                       │
     ├── Planification manuelle   │                       │
     │   (Excel / papier)         │                       │
     │                           │                       │
     ├── Envoi convocations ──────┼──────────────────────►│
     │   (email ou affichage)     │                       │
     │                           │                       │
     ├── Soutenance               │                       │
     │                           │                       │
     ├── Collecte fiches ─────────►│                      │
     │   d'évaluation papier      │                      │
     │                           │                       │
     └── Saisie des résultats     │                       │
         (Excel → archivage)      │                       │
```

### 2.2 Outils utilisés actuellement

| Outil | Usage | Limites |
|---|---|---|
| **Microsoft Excel** | Suivi des soutenances, listes étudiants, notes | Pas de contrôle des conflits, erreurs manuelles fréquentes |
| **Email** | Communication entre acteurs, envoi de convocations | Pas de traçabilité, informations éparpillées |
| **Documents Word/papier** | Convocations, fiches d'évaluation, PV | Saisie manuelle, pas de cohérence des données |
| **Affichage physique** | Communication des plannings aux étudiants | Délai, risque d'informations obsolètes |

---

## 3. Analyse des dysfonctionnements

### 3.1 Problèmes identifiés

#### a) Conflits et erreurs de planification
- Un enseignant peut être convoqué dans deux salles au même horaire sans qu'aucun système ne le détecte
- Une salle peut être réservée deux fois pour le même créneau
- Un étudiant peut être planifié deux fois par erreur

#### b) Manque de traçabilité
- Les modifications de dernière minute ne sont pas toujours communiquées
- Il n'existe pas d'historique centralisé des décisions
- Les évaluations papier peuvent se perdre

#### c) Inefficacité opérationnelle
- La collecte des disponibilités des enseignants est un processus long et fastidieux
- La génération manuelle des convocations est répétitive et sujette aux erreurs
- La compilation des résultats nécessite une saisie multiple (fiche papier → Excel)

#### d) Accessibilité limitée
- Les étudiants doivent se déplacer ou consulter des affichages pour connaître leur planning
- Les enseignants n'ont pas de vue centralisée de leurs engagements de jury

### 3.2 Tableau synthétique des problèmes

| Problème | Impact | Fréquence |
|---|---|---|
| Conflits d'horaires non détectés | Élevé | Fréquent |
| Perte de documents d'évaluation | Élevé | Occasionnel |
| Informations obsolètes affichées | Moyen | Fréquent |
| Doublons dans les listes | Moyen | Fréquent |
| Délais dans la communication | Moyen | Très fréquent |
| Erreurs de saisie des notes | Élevé | Occasionnel |

---

## 4. Solutions existantes sur le marché

### 4.1 Systèmes universitaires généraux (ERP)

| Solution | Description | Adéquation |
|---|---|---|
| **Apogée (Amue)** | Système de gestion des étudiants utilisé dans les universités françaises | Trop généraliste, gestion des soutenances non native |
| **Aurion** | ERP académique couvrant scolarité, stages, PFE | Module soutenance limité, coût élevé, complexité |
| **Koha / Aleph** | Systèmes bibliothèques universitaires | Hors sujet |

### 4.2 Outils de planification générique

| Solution | Description | Limites pour ce besoin |
|---|---|---|
| **Doodle** | Sondages de disponibilité | Pas de gestion des jurys, pas de génération PDF |
| **Google Calendar** | Calendrier partagé | Pas de logique métier académique, pas de convocations |
| **Microsoft Teams / Outlook** | Gestion de réunions | Pas adapté à la logique jury/soutenance |

### 4.3 Conclusion de l'analyse du marché

**Aucune solution existante ne répond spécifiquement aux besoins suivants réunis :**
- Gestion complète du cycle de soutenance (planification → évaluation)
- Composition et affectation de jurys avec détection de conflits
- Génération automatique de convocations et fiches d'évaluation
- Adaptation au contexte marocain (filières, CNE, structure universitaire locale)

---

## 5. Opportunités identifiées

Le nouveau système devra capitaliser sur les opportunités suivantes :

1. **Centralisation** : une seule source de vérité pour toutes les informations
2. **Automatisation** : génération automatique des convocations et détection des conflits
3. **Accessibilité** : accès web pour tous les acteurs (coordinateurs, jurys, étudiants)
4. **Traçabilité** : historique complet des modifications et des évaluations
5. **Réduction des erreurs** : validation automatique avant enregistrement

---

## 6. Synthèse — Analyse SWOT de l'existant

```
┌─────────────────────────┬─────────────────────────┐
│       FORCES            │       FAIBLESSES         │
│                         │                          │
│ • Processus connu des   │ • Pas de détection de   │
│   acteurs               │   conflits automatique   │
│ • Pas de coût logiciel  │ • Saisie multiple        │
│ • Flexibilité manuelle  │ • Risque de perte de     │
│                         │   données                │
│                         │ • Communication lente    │
├─────────────────────────┼─────────────────────────┤
│    OPPORTUNITÉS         │       MENACES            │
│                         │                          │
│ • Numérisation possible │ • Résistance au          │
│   avec React + Spring   │   changement             │
│ • Amélioration UX pour  │ • Données sensibles      │
│   tous les acteurs      │   (RGPD-like)            │
│ • Génération PDF        │ • Dépendance technique   │
│   automatisée           │   à maintenir            │
└─────────────────────────┴─────────────────────────┘
```

---

## 7. Conclusion

L'étude de l'existant confirme la nécessité de développer un système dédié. Les processus manuels actuels sont sources d'erreurs, inefficaces et inadaptés à la complexité croissante de l'organisation des soutenances. Le projet proposé apportera une valeur ajoutée significative en automatisant les tâches répétitives, en centralisant l'information et en garantissant la cohérence des données.
