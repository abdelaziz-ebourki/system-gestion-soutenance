# Système de gestion des soutenances et des jurys (PFE)

Ce dépôt contient la **documentation**, les **spécifications de conception** et la **planification** du "Système de gestion des soutenances et des jurys". Il s'agit d'un projet de fin d'études (PFE) réalisé à la **Faculté des Sciences Ben M'Sick (FSBM)** de l'Université Hassan II de Casablanca, pour l'année universitaire 2025-2026.

## Aperçu du Projet

L'objectif est de développer une plateforme web complète pour automatiser et centraliser l'organisation des soutenances universitaires, incluant la planification des créneaux, l'affectation des jurys, la gestion des salles et la génération automatique des documents administratifs.

### Écosystème du Projet

Le système est divisé en trois dépôts distincts :

1.  **[system-gestion-soutenance-api](https://github.com/abdelaziz-ebourki/system-gestion-soutenance-api)** : Le backend développé avec **Spring Boot (Java)** et **MySQL**.
2.  **[system-gestion-soutenance-ui](https://github.com/abdelaziz-ebourki/system-gestion-soutenance-ui)** : L'interface utilisateur développée avec **React.js**, **Tailwind CSS** et **Shadcn UI**.
3.  **system-gestion-soutenance-docs** (ce dépôt) : Sources LaTeX des rapports et diagrammes UML.

---

## Structure du Dépôt

```text
.
├── assets/             # Logos, ressources visuelles et captures d'écran
├── diagrams/
│   ├── src/            # Sources PlantUML (.puml)
│   └── svg/            # Diagrammes exportés en SVG
├── packages/           # Packages LaTeX personnalisés (ex: tikz-uml)
├── reports/
│   ├── cahier-des-charges/ # Source LaTeX du Cahier des Charges (CDC)
│   └── conception/         # Source LaTeX du Rapport de Conception
└── tools/              # Scripts utilitaires
```

---

## Travailler avec la Documentation

### Diagrammes UML (PlantUML)

Tous les diagrammes sont conçus avec **PlantUML**.

- Les sources se trouvent dans `diagrams/src/`.
- Ils utilisent un thème personnalisé défini dans `diagrams/src/puml-theme-pfe.puml`.
- Pour les mettre à jour dans les rapports, exportez-les en format **SVG** dans le dossier `diagrams/svg/`.

### Rapports LaTeX

Les rapports utilisent une classe `article` avec un préambule personnalisé (`preamble.tex`) partagé ou spécifique à chaque document.

- **Inclusion de diagrammes** : Utilisez la commande personnalisée `\pumlsvg[width]{filename}` (sans extension) pour inclure un diagramme depuis le dossier SVG.
- **Compilation** : Recommandé avec `pdflatex` ou `latexmk`. Assurez-vous d'avoir Inkscape installé si vous utilisez le package `svg` pour la conversion automatique.
