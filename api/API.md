# API Documentation — Système Gestion Soutenance

**Base URL:** `http://localhost:8080`

**Authentication:** Bearer JWT token via `Authorization` header or `jwt_token` cookie.

**Common Response Wrappers:**

- `ApiResponse<T>` — standard wrapper for most endpoints
- `PaginatedResponse<T>` — paginated list wrapper
- Direct entities/DTOs for certain config endpoints

---

## Common Schemas

### `ApiResponse<T>`

| Field | Type | Description |
|---|---|---|
| `success` | boolean | Operation success flag |
| `message` | string | Human-readable message (French) |
| `data` | T \| null | Response payload |
| `timestamp` | string (ISO-8601) | Response timestamp |
| `errors` | string[] \| null | Validation/error details |

### `PaginatedResponse<T>`

| Field | Type | Description |
|---|---|---|
| `items` | T[] | Page items |
| `total` | long | Total item count |
| `pageCount` | int | Total pages |
| `currentPage` | int | Current page (0-based) |
| `size` | int | Page size |

### `UserDto`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | Long | ✓ | Unique user ID |
| `email` | string | ✓ | Email address |
| `role` | string | ✓ | `ADMIN`, `COORDINATOR`, `TEACHER`, `STUDENT` |
| `lastName` | string | ✓ | Last name |
| `firstName` | string | ✓ | First name |
| `isActive` | boolean | ✓ | Account active status |
| `cne` | string | | Student CNE (students only) |
| `majorId` | Long | | Major ID (students only) |
| `majorName` | string | | Major name (students only) |
| `levelId` | Long | | Level ID (students only) |
| `levelName` | string | | Level name (students only) |
| `gradeId` | Long | | Grade ID (teachers only) |
| `gradeName` | string | | Grade name (teachers only) |
| `departmentId` | Long | | Department ID (teachers only) |
| `departmentName` | string | | Department name (teachers only) |

### Enums

| Enum | Values |
|---|---|
| `Role` | `ADMIN`, `COORDINATOR`, `TEACHER`, `STUDENT` |
| `DefenseType` | `PFE`, `MEMOIRE`, `THESE` |
| `DefenseSessionStatus` | `DRAFT`, `ACTIVE`, `SCHEDULED`, `COMPLETED`, `ARCHIVED` |
| `EvaluationStatus` | `PENDING`, `SUBMITTED` |
| `NotificationType` | `SUCCESS`, `WARNING`, `ERROR`, `INFO`, `REMINDER` |

---

## Auth Endpoints

### `POST /api/auth/login`

Authenticate a user.

**Request body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `email` | string | ✓ | Email address (format: email) |
| `password` | string | ✓ | Password |

**Response `200`:** `LoginResponse`

| Field | Type | Description |
|---|---|---|
| `user` | UserDto | Authenticated user info |
| `token` | string | JWT token (2h expiry) |
| `expiresAt` | long | Token expiry timestamp (ms) |

Also sets `jwt_token` cookie (HttpOnly, SameSite=Lax, 2h).

**Response `401`:** `{"message": "Invalid credentials (email or password incorrect)"}`

---

### `POST /api/auth/forgot-password`

Request a password reset link. Always returns 200 to prevent email enumeration.

**Request body:**

| Field | Type | Required |
|---|---|---|
| `email` | string | ✓ (format: email) |

**Response `200`:** `ApiResponse<Void>` — `"Si cet email existe, un lien de réinitialisation a été envoyé."`

---

### `POST /api/auth/reset-password`

Reset password using a valid token.

**Request body:**

| Field | Type | Required |
|---|---|---|
| `token` | string | ✓ |
| `password` | string | ✓ |

**Response `200`:** `ApiResponse<Void>` — `"Mot de passe réinitialisé avec succès."`

---

### `POST /api/auth/verify-account`

Verify a new account (sets password and activates).

**Request body:**

| Field | Type | Required |
|---|---|---|
| `token` | string | ✓ |
| `password` | string | ✓ |

**Response `200`:** `ApiResponse<Void>` — `"Compte vérifié avec succès."`
**Response `400`:** Invalid token
**Response `404`:** User not found

---

## Admin Endpoints

### `GET /api/admin/users`

List users (paginated, filterable).

**Query params:**

| Param | Type | Default | Description |
|---|---|---|---|
| `role` | string | | Filter by role |
| `page` | int | `0` | Page number (0-based) |
| `limit` | int | `10` | Page size |
| `search` | string | | Search term |

**Response `200`:** `PaginatedResponse<UserDto>`
**Response `400`:** Invalid query parameters

---

### `POST /api/admin/users`

Create a new user.

**Request body:** `CreateUserRequest`

| Field | Type | Required | Description |
|---|---|---|---|
| `lastName` | string | ✓ | Last name |
| `firstName` | string | ✓ | First name |
| `email` | string | ✓ | Email (format: email) |
| `role` | string | | `STUDENT`, `TEACHER`, `COORDINATOR`, `ADMIN` |
| `cne` | string | | CNE (students) |
| `majorId` | Long | | Major ID (students) |
| `levelId` | Long | | Level ID (students) |
| `gradeId` | Long | | Grade ID (teachers) |
| `departmentId` | Long | | Department ID (teachers) |

**Response `201`:** `UserDto`
**Response `400`:** Invalid user data
**Response `409`:** User already exists

---

### `POST /api/admin/users/bulk`

Bulk create users.

**Request body:** `BulkCreateRequest`

| Field | Type | Required | Description |
|---|---|---|---|
| `users` | BulkUserEntry[] | ✓ | List of users |
| `role` | string | ✓ | Role for all users |

`BulkUserEntry`:

| Field | Type | Required |
|---|---|---|
| `lastName` | string | ✓ |
| `firstName` | string | ✓ |
| `email` | string | ✓ |
| `cne` | string | |
| `majorName` | string | |
| `levelName` | string | |
| `gradeName` | string | |
| `departmentName` | string | |

**Response `201`:** `UserDto[]`
**Response `400`:** Invalid bulk data

---

### `PUT /api/admin/users/{id}`

Update an existing user.

**Path params:** `id` (Long) — User ID

**Request body:** `UpdateUserRequest` (all fields optional)

| Field | Type | Description |
|---|---|---|
| `lastName` | string | Last name |
| `firstName` | string | First name |
| `email` | string | Email |
| `role` | string | Role |
| `cne` | string | CNE |
| `majorId` | Long | Major ID |
| `levelId` | Long | Level ID |
| `gradeId` | Long | Grade ID |
| `departmentId` | Long | Department ID |

**Response `200`:** `UserDto`
**Response `400`:** Invalid update data
**Response `404`:** User not found

---

### `DELETE /api/admin/users/{id}`

Delete a user.

**Path params:** `id` (Long) — User ID

**Response `204`:** No content
**Response `404`:** User not found

---

### `GET /api/admin/stats`

Get global statistics.

**Response `200`:** `ApiResponse<GlobalStatsResponse>`

| Field | Type | Description |
|---|---|---|
| `totalStudents` | long | Total student count |
| `totalTeachers` | long | Total teacher count |
| `totalDepartments` | long | Total department count |
| `totalRooms` | long | Total room count |
| `totalDefenseSessions` | long | Total defense sessions |

---

### `GET /api/admin/rooms`

List rooms (paginated).

**Query params:**

| Param | Type | Default |
|---|---|---|
| `page` | int | `0` |
| `limit` | int | `10` |

**Response `200`:** `ApiResponse<PaginatedResponse<RoomResponse>>`

`RoomResponse`:

| Field | Type | Description |
|---|---|---|
| `id` | Long | Room ID |
| `name` | string | Room name |
| `capacity` | int | Room capacity |
| `departmentId` | Long | Department ID |

---

### `POST /api/admin/rooms`

Create a room.

**Request body:** `CreateRoomRequest`

| Field | Type | Required |
|---|---|---|
| `name` | string | ✓ |
| `capacity` | int | ✓ (> 0) |
| `departmentId` | Long | ✓ |

**Response `201`:** `ApiResponse<RoomResponse>`
**Response `400`:** Invalid room data

---

### `POST /api/admin/rooms/bulk`

Bulk create rooms.

**Request body:** `BulkRoomRequest`

| Field | Type | Required |
|---|---|---|
| `rooms` | RoomEntry[] | ✓ (non-empty) |

`RoomEntry`: same as `CreateRoomRequest`

**Response `201`:** `ApiResponse<RoomResponse[]>`
**Response `400`:** Invalid bulk data

---

### `PUT /api/admin/rooms/{id}`

Update a room.

**Path params:** `id` (Long)

**Request body:** `CreateRoomRequest`

**Response `200`:** `ApiResponse<RoomResponse>`
**Response `400`:** Invalid update data
**Response `404`:** Room not found

---

### `DELETE /api/admin/rooms/{id}`

Delete a room.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Room not found

---

### `GET /api/admin/faculties`

List all faculties.

**Response `200`:** `ApiResponse<FacultyDto[]>`

`FacultyDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `name` | string |
| `code` | string |
| `deanId` | Long (nullable) |
| `logoUrl` | string (nullable) |

---

### `GET /api/admin/faculties/{id}`

Get faculty by ID.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<FacultyDto>`
**Response `404`:** Faculty not found

---

### `POST /api/admin/faculties`

Create a faculty.

**Request body:** `CreateFacultyRequest`

| Field | Type | Required |
|---|---|---|
| `name` | string | ✓ |
| `code` | string | ✓ |
| `deanId` | Long | |
| `logoUrl` | string | |

**Response `201`:** `ApiResponse<FacultyDto>`
**Response `400`:** Invalid faculty data

---

### `PUT /api/admin/faculties/{id}`

Update a faculty.

**Path params:** `id` (Long)

**Request body:** `CreateFacultyRequest`

**Response `200`:** `ApiResponse<FacultyDto>`
**Response `400`:** Invalid update data
**Response `404`:** Faculty not found

---

### `DELETE /api/admin/faculties/{id}`

Delete a faculty.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Faculty not found

---

### `GET /api/admin/departments`

List all departments.

**Response `200`:** `ApiResponse<DepartmentResponse[]>`

`DepartmentResponse`:

| Field | Type |
|---|---|
| `id` | Long |
| `name` | string |
| `code` | string |
| `headId` | Long (nullable) |
| `facultyId` | Long (nullable) |
| `facultyName` | string (nullable) |

---

### `GET /api/admin/departments/{id}`

Get department by ID.

**Response `200`:** `ApiResponse<DepartmentResponse>`
**Response `404`:** Department not found

---

### `POST /api/admin/departments`

Create a department.

**Request body:** `CreateDepartmentRequest`

| Field | Type | Required |
|---|---|---|
| `name` | string | ✓ |
| `code` | string | ✓ |
| `headId` | Long | |
| `facultyId` | Long | |

**Response `201`:** `ApiResponse<DepartmentResponse>`
**Response `400`:** Invalid department data

---

### `PUT /api/admin/departments/{id}`

Update a department.

**Path params:** `id` (Long)

**Request body:** `CreateDepartmentRequest`

**Response `200`:** `ApiResponse<DepartmentResponse>`
**Response `400`:** Invalid update data
**Response `404`:** Department not found

---

### `DELETE /api/admin/departments/{id}`

Delete a department.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Department not found

---

### `GET /api/admin/config/levels`

List all academic levels.

**Response `200`:** `ApiResponse<LevelDto[]>`

`LevelDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `name` | string |

---

### `POST /api/admin/config/levels`

Create a level.

**Request body:** `CreateLevelRequest`

| Field | Type | Required |
|---|---|---|
| `name` | string | ✓ |

**Response `201`:** `ApiResponse<LevelDto>`
**Response `400`:** Invalid level data

---

### `PUT /api/admin/config/levels/{id}`

Update a level.

**Path params:** `id` (Long)

**Request body:** `CreateLevelRequest`

**Response `200`:** `ApiResponse<LevelDto>`
**Response `400`:** Invalid update data
**Response `404`:** Level not found

---

### `DELETE /api/admin/config/levels/{id}`

Delete a level.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Level not found

---

### `GET /api/admin/config/majors`

List all majors.

**Response `200`:** `ApiResponse<MajorDto[]>`

`MajorDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `name` | string |

---

### `POST /api/admin/config/majors`

Create a major.

**Request body:** `CreateMajorRequest`

| Field | Type | Required |
|---|---|---|
| `name` | string | ✓ |

**Response `201`:** `ApiResponse<MajorDto>`
**Response `400`:** Invalid major data

---

### `PUT /api/admin/config/majors/{id}`

Update a major.

**Path params:** `id` (Long)

**Request body:** `CreateMajorRequest`

**Response `200`:** `ApiResponse<MajorDto>`
**Response `400`:** Invalid update data
**Response `404`:** Major not found

---

### `DELETE /api/admin/config/majors/{id}`

Delete a major.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Major not found

---

### `GET /api/admin/config/grades`

List all grades.

**Response `200`:** `ApiResponse<GradeDto[]>`

`GradeDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `name` | string |

---

### `POST /api/admin/config/grades`

Create a grade.

**Request body:** `CreateGradeRequest`

| Field | Type | Required |
|---|---|---|
| `name` | string | ✓ |

**Response `201`:** `ApiResponse<GradeDto>`
**Response `400`:** Invalid grade data

---

### `PUT /api/admin/config/grades/{id}`

Update a grade.

**Path params:** `id` (Long)

**Request body:** `CreateGradeRequest`

**Response `200`:** `ApiResponse<GradeDto>`
**Response `404`:** Grade not found

---

### `DELETE /api/admin/config/grades/{id}`

Delete a grade.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Grade not found

---

### `GET /api/admin/config/jury-role-templates`

List all jury role templates.

**Response `200`:** `ApiResponse<JuryRoleTemplateDto[]>`

`JuryRoleTemplateDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `name` | string |
| `defenseType` | string |
| `roles` | TemplateRoleDto[] |

`TemplateRoleDto`:

| Field | Type |
|---|---|
| `name` | string |
| `count` | int |
| `coefficient` | int |

---

### `POST /api/admin/config/jury-role-templates`

Create a jury role template.

**Request body:** `CreateJuryRoleTemplateRequest`

| Field | Type | Required |
|---|---|---|
| `name` | string | ✓ |
| `defenseType` | string | ✓ |
| `roles` | RoleEntry[] | ✓ (non-empty) |

`RoleEntry`:

| Field | Type | Required |
|---|---|---|
| `name` | string | ✓ |
| `count` | int | |
| `coefficient` | int | |

**Response `201`:** `ApiResponse<JuryRoleTemplateDto>`
**Response `400`:** Invalid template data

---

### `PUT /api/admin/config/jury-role-templates/{id}`

Update a jury role template.

**Path params:** `id` (Long)

**Request body:** `CreateJuryRoleTemplateRequest`

**Response `200`:** `ApiResponse<JuryRoleTemplateDto>`
**Response `400`:** Invalid update data
**Response `404`:** Template not found

---

### `DELETE /api/admin/config/jury-role-templates/{id}`

Delete a jury role template.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Template not found

---

### `GET /api/admin/config/general`

Get general settings.

**Response `200`:** `GeneralSettings` (direct entity)

| Field | Type |
|---|---|
| `id` | Long |
| `institutionName` | string |
| `institutionLogoUrl` | string |
| `timezone` | string |
| `dateFormat` | string |
| `setupCompleted` | boolean |

---

### `PUT /api/admin/config/general`

Update general settings.

**Request body:** `UpdateGeneralSettingsRequest`

| Field | Type |
|---|---|
| `institutionName` | string |
| `institutionLogoUrl` | string |
| `timezone` | string |
| `dateFormat` | string |
| `setupCompleted` | boolean |

**Response `200`:** `GeneralSettings`

---

### `GET /api/admin/config/email`

Get email configuration.

**Response `200`:** `EmailConfig` (direct entity, password is write-only)

| Field | Type |
|---|---|
| `id` | Long |
| `host` | string |
| `port` | int |
| `username` | string |
| `senderName` | string |
| `senderEmail` | string |
| `encryption` | string |

---

### `PUT /api/admin/config/email`

Update email configuration.

**Request body:** `UpdateEmailConfigRequest`

| Field | Type | Required |
|---|---|---|
| `host` | string | ✓ |
| `port` | int | ✓ (≥ 1) |
| `username` | string | |
| `password` | string | |
| `senderName` | string | |
| `senderEmail` | string | |
| `encryption` | string | |

**Response `200`:** `EmailConfig`

---

### `GET /api/admin/config/documents`

Get document configuration.

**Response `200`:** `DocumentConfig` (direct entity)

| Field | Type |
|---|---|
| `id` | Long |
| `maxFileSizeMb` | int |
| `allowedExtensions` | string |
| `versionLimit` | int |

---

### `PUT /api/admin/config/documents`

Update document configuration.

**Request body:** `UpdateDocumentConfigRequest`

| Field | Type | Required |
|---|---|---|
| `maxFileSizeMb` | int | ✓ (≥ 1) |
| `allowedExtensions` | string | ✓ |
| `versionLimit` | int | ✓ (≥ 1) |

**Response `200`:** `DocumentConfig`

---

### `GET /api/admin/config/settings`

Get defense settings.

**Response `200`:** `DefenseSettings` (direct entity)

| Field | Type |
|---|---|
| `id` | Long |
| `startTime` | string |
| `endTime` | string |
| `defenseDuration` | int |
| `breakDuration` | int |
| `groupCreationStartDate` | string |
| `groupCreationEndDate` | string |

---

### `PUT /api/admin/config/settings`

Update defense settings.

**Request body:** `UpdateDefenseSettingsRequest`

| Field | Type | Required |
|---|---|---|
| `startTime` | string | ✓ |
| `endTime` | string | ✓ |
| `defenseDuration` | int | ✓ (≥ 1) |
| `breakDuration` | int | ✓ (≥ 0) |
| `groupCreationStartDate` | string | |
| `groupCreationEndDate` | string | |

**Response `200`:** `DefenseSettings`

---

### `GET /api/admin/audit-logs`

List audit logs (paginated).

**Query params:**

| Param | Type | Default |
|---|---|---|
| `page` | int | `0` |
| `limit` | int | `20` |

**Response `200`:** `ApiResponse<PaginatedResponse<AuditLogDto>>`

`AuditLogDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `action` | string |
| `entity` | string |
| `entityId` | Long |
| `performedByEmail` | string |
| `details` | string |
| `timestamp` | string (ISO-8601) |

---

### `POST /api/admin/audit-logs`

Create an audit log entry.

**Request body:** `AuditLogRequest`

| Field | Type | Required |
|---|---|---|
| `action` | string | ✓ |
| `entity` | string | ✓ |
| `entityId` | Long | ✓ |
| `performedByEmail` | string | ✓ |
| `details` | string | |

**Response `201`:** `ApiResponse<AuditLogDto>`
**Response `400`:** Invalid log data

---

## Coordinator Endpoints

### `GET /api/coordinator/users`

List users filtered by role (paginated).

**Query params:**

| Param | Type | Default | Required |
|---|---|---|---|
| `role` | string | | ✓ (`STUDENT` or `TEACHER`) |
| `page` | int | `0` | |
| `limit` | int | `5000` | |
| `search` | string | | |

**Response `200`:** `PaginatedResponse<UserDto>`
**Response `400`:** Invalid query parameters

---

### `GET /api/coordinator/groups`

List all student groups.

**Response `200`:** `ApiResponse<GroupResponse[]>`

`GroupResponse`:

| Field | Type |
|---|---|
| `id` | Long |
| `groupName` | string |
| `projectId` | Long |
| `memberCount` | int |
| `studentNames` | string[] |

---

### `POST /api/coordinator/groups`

Create a student group.

**Request body:** `CreateGroupRequest`

| Field | Type | Required |
|---|---|---|
| `groupName` | string | ✓ |
| `projectId` | Long | ✓ |
| `studentIds` | Long[] | |
| `sessionId` | Long | |

**Response `201`:** `ApiResponse<GroupResponse>`
**Response `400`:** Invalid group data

---

### `DELETE /api/coordinator/groups/{id}`

Delete a group.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Group not found

---

### `GET /api/coordinator/projects`

List all projects.

**Response `200`:** `ApiResponse<ProjectResponse[]>`

`ProjectResponse`:

| Field | Type |
|---|---|
| `id` | Long |
| `title` | string |
| `description` | string |
| `defenseType` | string |
| `groupId` | Long (nullable) |
| `supervisorName` | string |
| `studentNames` | string[] |

---

### `POST /api/coordinator/projects`

Create a project.

**Request body:** `CreateProjectRequest`

| Field | Type | Required |
|---|---|---|
| `title` | string | ✓ |
| `description` | string | ✓ |
| `supervisorId` | Long | ✓ |
| `defenseType` | string | ✓ |
| `studentIds` | Long[] | |

**Response `201`:** `ApiResponse<ProjectResponse>`
**Response `400`:** Invalid project data

---

### `PUT /api/coordinator/projects/{id}`

Update a project.

**Path params:** `id` (Long)

**Request body:** `UpdateProjectRequest`

| Field | Type |
|---|---|
| `title` | string |
| `description` | string |
| `defenseType` | string |

**Response `200`:** `ApiResponse<ProjectResponse>`
**Response `400`:** Invalid update data
**Response `404`:** Project not found

---

### `DELETE /api/coordinator/projects/{id}`

Delete a project.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Project not found

---

### `GET /api/coordinator/juries`

List all juries.

**Response `200`:** `ApiResponse<JuryResponse[]>`

`JuryResponse`:

| Field | Type |
|---|---|
| `id` | Long |
| `projectId` | Long |
| `projectTitle` | string |
| `defenseType` | string |
| `templateId` | Long |
| `templateName` | string |
| `members` | MemberResponse[] |

`MemberResponse`:

| Field | Type |
|---|---|
| `roleName` | string |
| `teacherId` | Long |
| `teacherName` | string |

---

### `POST /api/coordinator/juries`

Create a jury.

**Request body:** `CreateJuryRequest`

| Field | Type | Required |
|---|---|---|
| `projectId` | Long | ✓ |
| `templateId` | Long | ✓ |
| `members` | MemberEntry[] | ✓ |

`MemberEntry`:

| Field | Type | Required |
|---|---|---|
| `teacherId` | Long | ✓ |
| `roleName` | string | ✓ |

**Response `201`:** `ApiResponse<JuryResponse>`
**Response `400`:** Invalid jury data

---

### `PUT /api/coordinator/juries/{id}`

Update a jury.

**Path params:** `id` (Long)

**Request body:** `UpdateJuryRequest`

| Field | Type |
|---|---|
| `projectId` | Long |
| `templateId` | Long |
| `members` | MemberEntry[] |

**Response `200`:** `ApiResponse<JuryResponse>`
**Response `400`:** Invalid update data
**Response `404`:** Jury not found

---

### `DELETE /api/coordinator/juries/{id}`

Delete a jury.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Jury not found

---

### `GET /api/coordinator/defense-sessions`

List all defense sessions.

**Response `200`:** `ApiResponse<DefenseSessionDto[]>`

`DefenseSessionDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `name` | string |
| `defenseType` | string |
| `status` | string |
| `maxGroupSize` | int |
| `defenseDuration` | int |
| `breakDuration` | int |
| `submissionDeadline` | string (date) |
| `evaluationCoefficients` | Map<string, int> |
| `juryRoleTemplateId` | Long |
| `startDate` | string (date) |
| `endDate` | string (date) |

---

### `POST /api/coordinator/defense-sessions`

Create a defense session.

**Request body:** `CreateDefenseSessionRequest`

| Field | Type | Required |
|---|---|---|
| `name` | string | ✓ |
| `defenseType` | string | ✓ |
| `status` | string | |
| `maxGroupSize` | int | |
| `defenseDuration` | int | |
| `breakDuration` | int | |
| `submissionDeadline` | string (date) | |
| `evaluationCoefficients` | Map<string, int> | |
| `juryRoleTemplateId` | Long | |
| `startDate` | string (date) | ✓ |
| `endDate` | string (date) | ✓ |

**Response `201`:** `ApiResponse<DefenseSessionDto>`
**Response `400`:** Invalid session data

---

### `PUT /api/coordinator/defense-sessions/{id}`

Update a defense session.

**Path params:** `id` (Long)

**Request body:** `CreateDefenseSessionRequest`

**Response `200`:** `ApiResponse<DefenseSessionDto>`
**Response `404`:** Session not found

---

### `DELETE /api/coordinator/defense-sessions/{id}`

Delete a defense session.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Session not found

---

### `POST /api/coordinator/defense-sessions/{id}/transition`

Transition a defense session status.

**Path params:** `id` (Long)

**Request body:** `StatusTransitionRequest`

| Field | Type | Required |
|---|---|---|
| `toStatus` | string | ✓ |

**Response `200`:** `ApiResponse<DefenseSessionDto>`

---

### `GET /api/coordinator/schedules`

Get the current schedule.

**Response `200`:** `ApiResponse<ScheduleResponse[]>`

`ScheduleResponse`:

| Field | Type |
|---|---|
| `id` | Long |
| `title` | string |
| `date` | string |
| `time` | string |
| `projectId` | Long |
| `roomId` | Long |
| `roomName` | string |
| `projectTitle` | string |
| `studentNames` | string[] |
| `role` | string |
| `status` | string |

---

### `POST /api/coordinator/schedules`

Save a new schedule.

**Request body:** `ScheduleRequest`

| Field | Type | Required |
|---|---|---|
| `defenseSessionId` | Long | ✓ |
| `slots` | SlotAssignmentRequest[] | ✓ |

`SlotAssignmentRequest`:

| Field | Type | Required |
|---|---|---|
| `title` | string | |
| `date` | string | ✓ |
| `time` | string | ✓ |
| `projectId` | Long | |
| `roomId` | Long | |

**Response `200`:** `ApiResponse<ScheduleResponse[]>` (with conflicts as warning)
**Response `400`:** Conflicts detected (error severity)

---

### `POST /api/coordinator/schedules/generation`

Auto-generate a schedule proposal.

**Request body:** `DefenseSessionIdRequest`

| Field | Type | Required |
|---|---|---|
| `defenseSessionId` | Long | ✓ |

**Response `200`:** `ApiResponse<ScheduleResponse[]>`
**Response `400`:** Invalid session ID

---

### `PATCH /api/coordinator/schedules/publication`

Publish the schedule.

**Request body:** `DefenseSessionIdRequest`

| Field | Type | Required |
|---|---|---|
| `defenseSessionId` | Long | ✓ |

**Response `200`:** `ApiResponse<Void>`
**Response `404`:** Session not found

---

### `POST /api/coordinator/defenses/{id}/cancel`

Cancel a scheduled defense.

**Path params:** `id` (Long)

**Response `200`:** `ApiResponse<Void>`

---

### `GET /api/coordinator/grades`

Get all grades with weighted averages.

**Response `200`:** `ApiResponse<GradeWeightedAverageResponse[]>`

`GradeWeightedAverageResponse`:

| Field | Type |
|---|---|
| `projectId` | Long |
| `projectTitle` | string |
| `defenseDate` | string |
| `status` | string |
| `finalScore` | Double (nullable) |
| `evaluationCoefficients` | Map<string, int> |
| `individualScores` | IndividualScoreResponse[] |

`IndividualScoreResponse`:

| Field | Type |
|---|---|
| `roleName` | string |
| `teacherName` | string |
| `score` | Double (nullable) |

---

### `GET /api/coordinator/stats`

Get coordinator statistics.

**Response `200`:** `ApiResponse<CoordinatorStatsResponse>`

| Field | Type |
|---|---|
| `totalProjects` | long |
| `totalGroups` | long |
| `totalJuries` | long |
| `scheduledDefenses` | long |

---

### `GET /api/coordinator/unavailability`

List all teacher unavailability records.

**Response `200`:** `UnavailabilityDto[]` (direct list, not wrapped)

`UnavailabilityDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `teacherId` | Long |
| `date` | string |
| `slots` | string[] |

---

### `POST /api/coordinator/conflicts/validate`

Validate a proposed schedule for conflicts.

**Request body:** `ValidateScheduleRequest`

| Field | Type | Required |
|---|---|---|
| `defenseSessionId` | Long | ✓ |
| `schedule` | SlotAssignmentRequest[] | ✓ |

**Response `200`:** `ApiResponse<ConflictDetailResponse[]>`

`ConflictDetailResponse`:

| Field | Type |
|---|---|
| `type` | string |
| `severity` | string |
| `message` | string |
| `slot` | string |
| `suggestedResolution` | string |

---

### `POST /api/coordinator/documents/evaluation-sheets`

Get evaluation sheets data.

**Request body:** `ProjectIdRequest`

| Field | Type | Required |
|---|---|---|
| `projectId` | Long | ✓ |

**Response `200`:** `ApiResponse<EvaluationSheetResponse[]>`

`EvaluationSheetResponse`:

| Field | Type |
|---|---|
| `projectId` | Long |
| `projectTitle` | string |
| `studentNames` | string[] |
| `supervisorName` | string |
| `date` | string |
| `time` | string |
| `roomName` | string |
| `juryMembers` | JuryMemberResponse[] |
| `evaluationCoefficients` | Map<string, int> |

---

### `POST /api/coordinator/documents/attendance-lists`

Get attendance list data.

**Request body:** `SessionRequest`

| Field | Type | Required |
|---|---|---|
| `defenseSessionId` | Long | ✓ |

**Response `200`:** `ApiResponse<AttendanceListResponse>`

`AttendanceListResponse`:

| Field | Type |
|---|---|
| `defenseSessionName` | string |
| `slots` | SlotDetails[] |

`SlotDetails` (coordinator doc variant):

| Field | Type |
|---|---|
| `date` | string |
| `time` | string |
| `roomName` | string |
| `projectTitle` | string |
| `studentNames` | string[] |

---

### `POST /api/coordinator/documents/jury-convocations`

Get jury convocation data.

**Request body:** `ProjectIdRequest`

**Response `200`:** `ApiResponse<JuryConvocationResponse[]>`

`JuryConvocationResponse`:

| Field | Type |
|---|---|
| `teacherName` | string |
| `role` | string |
| `projectTitle` | string |
| `studentNames` | string[] |
| `date` | string |
| `time` | string |
| `roomName` | string |
| `defenseSessionName` | string |

---

### `POST /api/coordinator/documents/schedule`

Get printable schedule data.

**Request body:** `SessionRequest`

**Response `200`:** `ApiResponse<ScheduleDocResponse>`

`ScheduleDocResponse`:

| Field | Type |
|---|---|
| `defenseSessionName` | string |
| `slots` | SlotDetails[] |

---

### `POST /api/coordinator/documents/proces-verbal`

Get proces-verbal (PV) data.

**Request body:** `ProjectIdRequest`

**Response `200`:** `ApiResponse<ProcesVerbalResponse>`

`ProcesVerbalResponse`:

| Field | Type |
|---|---|
| `settings` | Settings |
| `grade` | GradeDetails |
| `studentNames` | string[] |
| `supervisorName` | string |
| `juryMembers` | JuryMemberDetails[] |

`Settings`:

| Field | Type |
|---|---|
| `institutionName` | string |
| `institutionLogoUrl` | string |
| `timezone` | string |
| `dateFormat` | string |

`GradeDetails`:

| Field | Type |
|---|---|
| `projectId` | Long |
| `projectTitle` | string |
| `finalScore` | double |
| `decision` | string |

`JuryMemberDetails`:

| Field | Type |
|---|---|
| `roleName` | string |
| `teacherName` | string |

---

## Teacher Endpoints

### `GET /api/teacher/evaluations`

List evaluations assigned to the connected teacher.

**Requires:** Authentication (TEACHER role)

**Response `200`:** `ApiResponse<EvaluationResponse[]>`

`EvaluationResponse`:

| Field | Type |
|---|---|
| `id` | Long |
| `projectId` | Long |
| `projectTitle` | string |
| `finalGrade` | Double (nullable) |
| `comment` | string |
| `status` | string (`PENDING`/`SUBMITTED`) |

---

### `POST /api/teacher/evaluations/{id}`

Submit evaluation score.

**Path params:** `id` (Long) — Evaluation ID

**Request body:** `EvaluationSubmitRequest`

| Field | Type | Required |
|---|---|---|
| `score` | number | (0.0–20.0) |
| `comment` | string | |

**Response `200`:** `ApiResponse<EvaluationResponse>`
**Response `400`:** Invalid evaluation data
**Response `404`:** Evaluation not found

---

### `GET /api/teacher/schedules`

Get the connected teacher's defense schedule.

**Requires:** Authentication (TEACHER role)

**Response `200`:** `ApiResponse<TeacherScheduleResponse>`

`TeacherScheduleResponse`:

| Field | Type |
|---|---|
| `slots` | SlotDetails[] |

`SlotDetails` (teacher variant):

| Field | Type |
|---|---|
| `id` | Long |
| `projectId` | Long |
| `projectTitle` | string |
| `studentNames` | string[] |
| `date` | string |
| `startTime` | string |
| `endTime` | string |
| `roomName` | string |
| `role` | string |
| `status` | string |

---

### `GET /api/teacher/stats`

Get the connected teacher's personal statistics.

**Requires:** Authentication (TEACHER role)

**Response `200`:** `ApiResponse<TeacherStatsResponse>`

| Field | Type |
|---|---|
| `upcomingDefenses` | int |
| `pendingEvaluations` | long |
| `declaredUnavailabilitySlots` | long |
| `juryAssignments` | long |

---

### `GET /api/teacher/unavailabilities`

Get the connected teacher's unavailability slots.

**Requires:** Authentication (TEACHER role)

**Response `200`:** `ApiResponse<TeacherUnavailabilityResponse>`

| Field | Type |
|---|---|
| `slotsByDate` | Map<string, string[]> |

---

### `POST /api/teacher/unavailabilities`

Save/update the connected teacher's unavailability slots.

**Requires:** Authentication (TEACHER role)

**Request body:** `TeacherUnavailabilityRequest`

| Field | Type | Required |
|---|---|---|
| `slots` | UnavailabilitySlotRequest[] | ✓ (non-empty) |

`UnavailabilitySlotRequest`:

| Field | Type | Required |
|---|---|---|
| `date` | string | ✓ |
| `slots` | string[] | ✓ |

**Response `200`:** `ApiResponse<TeacherUnavailabilityResponse>`
**Response `400`:** Invalid unavailability data

---

## Student Endpoints

### `GET /api/student/groups`

Get the connected student's group workspace.

**Requires:** Authentication (STUDENT role)

**Response `200`:** `ApiResponse<StudentGroupWorkspaceResponse>`

`StudentGroupWorkspaceResponse`:

| Field | Type |
|---|---|
| `currentGroup` | GroupDetailsResponse (nullable) |
| `availableGroups` | AvailableGroupResponse[] |
| `groupCreationStartDate` | string |
| `groupCreationEndDate` | string |
| `isGroupCreationOpen` | boolean |

`GroupDetailsResponse`:

| Field | Type |
|---|---|
| `id` | Long |
| `groupName` | string |
| `projectTitle` | string |
| `supervisorName` | string |
| `members` | GroupMemberResponse[] |

`GroupMemberResponse`:

| Field | Type |
|---|---|
| `id` | Long |
| `fullName` | string |
| `email` | string |
| `role` | string (`leader`/`member`) |

`AvailableGroupResponse`:

| Field | Type |
|---|---|
| `id` | Long |
| `groupName` | string |
| `memberCount` | int |

---

### `POST /api/student/groups`

Create a new group for the connected student.

**Requires:** Authentication (STUDENT role)

**Response `201`:** `ApiResponse<GroupDetailsResponse>`
**Response `400`:** Creation period closed or invalid request

---

### `POST /api/student/groups/{id}/members`

Join an existing group.

**Path params:** `id` (Long) — Group ID

**Requires:** Authentication (STUDENT role)

**Response `200`:** `ApiResponse<GroupDetailsResponse>`
**Response `400`:** Group full or already in a group
**Response `404`:** Group not found

---

### `GET /api/student/defenses`

Get the connected student's defense info.

**Requires:** Authentication (STUDENT role)

**Response `200`:** `ApiResponse<StudentDefenseResponse>`

`StudentDefenseResponse`:

| Field | Type |
|---|---|
| `projectTitle` | string |
| `projectDescription` | string |
| `supervisorName` | string |
| `juryMembers` | JuryMemberResponse[] |
| `date` | string |
| `startTime` | string |
| `endTime` | string |
| `roomName` | string |
| `status` | string |
| `convocationUrl` | string |
| `result` | string (nullable) |

`JuryMemberResponse` (student variant):

| Field | Type |
|---|---|
| `name` | string |
| `role` | string |

---

### `GET /api/student/convocations`

Generate and download the connected student's convocation PDF.

**Requires:** Authentication (STUDENT role)

**Response `200`:** PDF binary (`application/pdf`)
**Response `404`:** Convocation not available or not scheduled

---

### `GET /api/student/documents`

List the connected student's documents.

**Requires:** Authentication (STUDENT role)

**Response `200`:** `ApiResponse<StudentDocumentDto[]>`

`StudentDocumentDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `studentId` | Long |
| `name` | string |
| `type` | string |
| `deadline` | string |
| `status` | string |
| `submittedAt` | string (ISO-8601, nullable) |
| `filePath` | string |

---

### `POST /api/student/documents/{id}/attachments`

Upload a file attachment to a document.

**Path params:** `id` (Long) — Document ID

**Requires:** Authentication (STUDENT role)

**Request:** Multipart form with `file` field

**Response `200`:** `ApiResponse<StudentDocumentDto>`
**Response `400`:** Invalid file or request
**Response `404`:** Document not found

---

### `GET /api/student/stats`

Get the connected student's personal statistics.

**Requires:** Authentication (STUDENT role)

**Response `200`:** `ApiResponse<StudentStatsResponse>`

| Field | Type |
|---|---|
| `documentCount` | int |
| `missingDocuments` | long |
| `groupMembers` | int |
| `defenseStatus` | string |

---

## Notification Endpoints

### `GET /api/notifications`

List all notifications (ordered by timestamp descending).

**Requires:** Authentication (any role)

**Response `200`:** `AppNotificationDto[]` (direct list)

`AppNotificationDto`:

| Field | Type |
|---|---|
| `id` | Long |
| `type` | string (`SUCCESS`, `WARNING`, `ERROR`, `INFO`, `REMINDER`) |
| `title` | string |
| `message` | string |
| `timestamp` | string (ISO-8601) |
| `read` | boolean |
| `actionLink` | string (nullable) |
| `actor` | string (nullable) |

---

### `PATCH /api/notifications/{id}/read`

Mark a notification as read.

**Requires:** Authentication (any role)

**Path params:** `id` (Long)

**Response `204`:** No content

---

### `PATCH /api/notifications/read-all`

Mark all notifications as read.

**Requires:** Authentication (any role)

**Response `204`:** No content

---

### `POST /api/notifications/{id}/send-email`

Manually trigger email delivery for a notification.

**Requires:** Authentication (ADMIN or COORDINATOR role)

**Path params:** `id` (Long)

**Response `204`:** No content
