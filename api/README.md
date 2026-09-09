# Système de Gestion de Soutenances — API

API RESTful pour la gestion des soutenances de projets de fin d'études.

---

## Stack

- **Spring Boot 3.4.6** (Java 17)
- **Spring Data JPA** + **H2** (dev)
- **Spring Security** + **JWT** (jjwt 0.12.6)
- **Spring Mail** (SMTP transactionnel — fallback mock)
- **Spring Boot Actuator** (health check)
- **Lombok**
- **Springdoc OpenAPI** (Swagger UI)

---

## Démarrage

### Avec Docker (recommandé)

```bash
docker compose up --build
```

L'API est disponible sur `http://localhost:8080`.

### Sans Docker

```bash
cd api
JWT_SECRET="dev-secret-change-in-production" ./mvnw spring-boot:run
```

## Variables d'environnement

| Variable | Requise | Description |
|---|---|---|
| `JWT_SECRET` | ✓ | Clé de signature JWT (min. 256 bits). L'application refuse de démarrer si absente. |
| `APP_ENCRYPTION_KEY` | | Clé de chiffrement des données sensibles (ex. mot de passe SMTP). Défaut : `change-me-in-production-use-a-64-char-hex-key!!` |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | | Configuration SMTP pour les emails transactionnels. Absent = mode mock. |

### Consoles

| Outil | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| H2 Console | http://localhost:8080/h2-console (`jdbc:h2:mem:defensedb`) |
| Health Check | http://localhost:8080/actuator/health |

---

## Email

L'envoi d'emails transactionnels est configurable via variables d'environnement.

**Sans configuration SMTP** — les emails sont loggés dans la console (mode mock) :
```
[Mock Email] To: user@example.com | Subject: ... | Body: ...
```

**Avec SMTP** (exemple Gmail) :
```bash
SMTP_HOST=smtp.gmail.com \
SMTP_PORT=587 \
SMTP_USERNAME=votre.email@gmail.com \
SMTP_PASSWORD=votre-mot-de-passe \
docker compose up --build
```

**Emails envoyés :**
- Réinitialisation de mot de passe (`POST /api/auth/forgot-password`)

---

## Authentification

### `POST /api/login`

Authentifie un utilisateur et retourne un JWT.

**Request :**
```json
{ "email": "admin@univh2c.ma", "password": "1234" }
```

**Response 200 :**
```json
{
  "user": { "id": "1", "email": "admin@univh2c.ma", "role": "ADMIN", "lastName": "Ahmadi", "firstName": "Mohamed", "isActive": true, ... },
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresAt": 1779215552096
}
```

**Response 401 :**
```json
{ "message": "Identifiants invalides (E-mail ou mot de passe incorrect)" }
```

### `POST /api/auth/verify-account`

Active un compte après inscription (définit le mot de passe).

**Request :** `{ "token": "...", "password": "new-password" }`

### `POST /api/auth/forgot-password`

Demande un lien de réinitialisation (toujours 200 — anti-énumération).

**Request :** `{ "email": "..." }`

### `POST /api/auth/reset-password`

Réinitialise le mot de passe avec un token valide.

**Request :** `{ "token": "...", "password": "new-password" }`

---

## Utilisateurs de test (seed data)

| Email | Mot de passe | Rôle |
|---|---|---|
| admin@univh2c.ma | 1234 | ADMIN |
| coord@univh2c.ma | 1234 | COORDINATOR |
| teacher@univh2c.ma | 1234 | TEACHER |
| moussa@univh2c.ma | 1234 | TEACHER |
| student@univh2c.ma | 1234 | STUDENT |
| student1..50@univh2c.ma | 1234 | STUDENT |

---

## Modules et endpoints

Chaque module est accessible via un préfixe d'URL protégé par rôle.

### Admin (`/api/admin/` — rôle `ADMIN`)

| Méthode | Path | Description |
|---|---|---|
| GET/POST | `/api/admin/users` | Liste (paginée) / Créer un utilisateur |
| PUT/DELETE | `/api/admin/users/:id` | Modifier / Supprimer un utilisateur |
| POST | `/api/admin/users/bulk` | Import en masse |
| GET | `/api/admin/students` | Liste des étudiants |
| GET | `/api/admin/teachers` | Liste des enseignants |
| GET | `/api/admin/coordinators` | Liste des coordinateurs |
| GET | `/api/admin/users/teachers-list` | Tous les enseignants (non paginé) |
| GET | `/api/admin/users/students-list` | Tous les étudiants (non paginé) |
| GET/POST/PUT/DELETE | `/api/admin/departments` | Départements |
| GET/POST/PUT/DELETE | `/api/admin/sessions` | Sessions globales |
| GET/POST/PUT/DELETE | `/api/admin/rooms` | Salles |
| POST | `/api/admin/rooms/bulk` | Import de salles en masse |
| GET/POST/PUT/DELETE | `/api/admin/defense-sessions` | Sessions de soutenance |
| GET/POST/PUT/DELETE | `/api/admin/config/majors` | Filières |
| GET/POST/PUT/DELETE | `/api/admin/config/levels` | Niveaux |
| GET/POST/PUT/DELETE | `/api/admin/config/grades` | Grades |
| GET/POST/PUT/DELETE | `/api/admin/config/jury-role-templates` | Templates de rôles jury |
| GET/POST | `/api/admin/config/settings` | Paramètres des soutenances |
| GET/PUT | `/api/admin/config/general` | Paramètres généraux |
| GET/PUT | `/api/admin/config/defense-types` | Configuration des types de soutenance |
| GET/PUT | `/api/admin/config/documents` | Configuration des documents |
| GET | `/api/admin/stats` | Statistiques |
| GET/POST | `/api/admin/audit-logs` | Journal d'audit |

### Coordinateur (`/api/coordinator/` — rôle `COORDINATOR`)

| Méthode | Path | Description |
|---|---|---|
| GET/POST/PUT/DELETE | `/api/coordinator/projects` | Projets (avec étudiants + encadrant) |
| GET/POST/PUT/DELETE | `/api/coordinator/juries` | Jurys (membres dynamiques via template) |
| GET/POST/DELETE | `/api/coordinator/groups` | Groupes d'étudiants |
| GET/POST | `/api/coordinator/schedule` | Planning (créneaux) |
| POST | `/api/coordinator/schedule/auto-generate` | Génération automatique du planning |
| POST | `/api/coordinator/schedule/publish` | Publication du planning |
| POST | `/api/coordinator/schedule/validate` | Validation des conflits |
| GET/POST/PUT | `/api/coordinator/unavailability` | Indisponibilités des enseignants |
| GET/POST | `/api/coordinator/defense-sessions` | Sessions de soutenance + transition |
| POST | `/api/coordinator/defense-sessions/:id/transition` | Transition de statut |
| POST | `/api/coordinator/defenses/:id/cancel` | Annulation d'une soutenance |
| GET | `/api/coordinator/grades` | Notes (moyennes pondérées) |
| GET | `/api/coordinator/stats` | Statistiques |
| POST | `/api/coordinator/documents/evaluation-sheets` | Données pour fiches d'évaluation |
| POST | `/api/coordinator/documents/attendance-lists` | Données pour listes de présence |
| POST | `/api/coordinator/documents/jury-convocations` | Données pour convocations jury |
| POST | `/api/coordinator/documents/schedule` | Données pour planning imprimable |

### Enseignant (`/api/teacher/` — rôle `TEACHER`)

| Méthode | Path | Description |
|---|---|---|
| GET | `/api/teacher/schedule` | Planning de l'enseignant connecté |
| GET/POST | `/api/teacher/evaluations` | Évaluations (listes + soumission) |
| GET/POST | `/api/teacher/unavailability` | Indisponibilités de l'enseignant |
| GET | `/api/teacher/stats` | Statistiques personnelles |

### Étudiant (`/api/student/` — rôle `STUDENT`)

| Méthode | Path | Description |
|---|---|---|
| GET/POST | `/api/student/group` | Espace groupe (création) |
| POST | `/api/student/group/:id/join` | Rejoindre un groupe |
| GET | `/api/student/defense` | Informations sur la soutenance |
| GET | `/api/student/documents` | Liste des documents |
| POST | `/api/student/documents/:id/upload` | Upload d'un document (multipart) |
| GET | `/api/student/convocation` | Convocation PDF |
| GET | `/api/student/stats` | Statistiques personnelles |

### Notifications (`/api/notifications/` — authentifié)

| Méthode | Path |
|---|---|
| GET | `/api/notifications` |
| PATCH | `/api/notifications/:id/read` |
| PATCH | `/api/notifications/read-all` |

---

## Moteur de détection de conflits

Disponible via `POST /api/coordinator/schedule/validate`.

8 vérifications :

| # | Vérification | Gravité |
|---|---|---|
| 1 | Projet déjà planifié | erreur |
| 2 | Créneau occupé | erreur |
| 3 | Capacité de salle insuffisante | erreur |
| 4 | Date hors session | erreur |
| 5 | Enseignant double-booké | erreur |
| 6 | Conflit d'encadrant | avertissement |
| 7 | Intervalle insuffisant | avertissement |
| 8 | Enseignant indisponible | erreur |

---

## Calcul des notes

`GET /api/coordinator/grades` retourne la moyenne pondérée :

```
note_finale = Σ(score × coefficient) / Σ(coefficient)
```

- Scores sur 20
- Coefficients en pourcentages (0–100) définis par le template de rôles
- Statuts : `completed` (tous soumis), `pending` (partiel), `no_evaluations` (aucun)
- Décision : ≥ 10 → "Admis", < 10 → "Ajourné"

---

## Architecture du code

```
api/src/main/java/com/system_gestion_soutenance/api/
  admin/           → Gestion des utilisateurs, salles, sessions, configuration
  auth/            → Authentification JWT, login, vérification, reset
  common/          → Configuration partagée (sécurité, DTOs paginés)
  coordinator/     → Projets, jurys, groupes, planning, conflits, notes, documents
  notification/    → Notifications push + service email
  student/         → Groupes, défense, documents, convocation
  teacher/         → Évaluations, indisponibilités, planning
  user/            → Entités utilisateur (User/Student/Teacher/Coordinator)
```

Chaque module suit une structure en couches :
```
domain/
  controller/    → REST endpoints
  service/       → Logique métier
  repository/    → Accès données (Spring Data JPA)
  entity/        → Entités JPA
  dto/           → Records de requête/réponse
```

---

## Configuration email (`.env` support)

```bash
# .env
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=...
SMTP_PASSWORD=...
```

```bash
docker compose --env-file .env up --build
```
