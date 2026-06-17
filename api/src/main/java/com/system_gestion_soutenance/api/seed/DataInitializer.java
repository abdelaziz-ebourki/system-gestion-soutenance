package com.system_gestion_soutenance.api.seed;

import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.repository.AuditLogRepository;

import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.repository.TeacherRankRepository;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.repository.FacultyRepository;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;

import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.entity.NotificationType;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.repository.StudentDocumentRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationStatus;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.user.entity.Coordinator;
import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

	private static final String PASSWORD = "$2a$10$AY8qLutbqcPipSj1wCrt8.kqsfsezlmzkPa3YXE5Y7blECJqNj0ei";

	private final MajorRepository majorRepo;
	private final LevelRepository levelRepo;
	private final TeacherRankRepository teacherRankRepo;
	private final FacultyRepository facultyRepo;
	private final DepartmentRepository departmentRepo;
	private final UserRepository userRepo;
	private final TeacherRepository teacherRepo;
	private final StudentRepository studentRepo;
	private final RoomRepository roomRepo;
	private final JuryRoleTemplateRepository juryRoleTemplateRepo;
	private final DefenseSessionRepository defenseSessionRepo;
	private final ProjectRepository projectRepo;
	private final DefenseRepository defenseRepo;
	private final GroupRepository groupRepo;
	private final UnavailabilityRepository unavailabilityRepo;
	private final EvaluationRepository evaluationRepo;
	private final StudentDocumentRepository studentDocumentRepo;
	private final NotificationRepository notificationRepo;
	private final AuditLogRepository auditLogRepo;

	@SuppressWarnings("checkstyle:ParameterNumber")
	public DataInitializer(MajorRepository majorRepo, LevelRepository levelRepo, TeacherRankRepository teacherRankRepo,
			FacultyRepository facultyRepo, DepartmentRepository departmentRepo, UserRepository userRepo,
			TeacherRepository teacherRepo, StudentRepository studentRepo, RoomRepository roomRepo,
			JuryRoleTemplateRepository juryRoleTemplateRepo, DefenseSessionRepository defenseSessionRepo,
			ProjectRepository projectRepo, DefenseRepository defenseRepo, GroupRepository groupRepo,
			UnavailabilityRepository unavailabilityRepo, EvaluationRepository evaluationRepo,
			StudentDocumentRepository studentDocumentRepo, NotificationRepository notificationRepo,
			AuditLogRepository auditLogRepo) {
		this.majorRepo = majorRepo;
		this.levelRepo = levelRepo;
		this.teacherRankRepo = teacherRankRepo;
		this.facultyRepo = facultyRepo;
		this.departmentRepo = departmentRepo;
		this.userRepo = userRepo;
		this.teacherRepo = teacherRepo;
		this.studentRepo = studentRepo;
		this.roomRepo = roomRepo;
		this.juryRoleTemplateRepo = juryRoleTemplateRepo;
		this.defenseSessionRepo = defenseSessionRepo;
		this.projectRepo = projectRepo;
		this.defenseRepo = defenseRepo;
		this.groupRepo = groupRepo;
		this.unavailabilityRepo = unavailabilityRepo;
		this.evaluationRepo = evaluationRepo;
		this.studentDocumentRepo = studentDocumentRepo;
		this.notificationRepo = notificationRepo;
		this.auditLogRepo = auditLogRepo;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (majorRepo.count() > 0)
			return;

		// Phase 2: Faculty
		Faculty f1 = new Faculty();
		f1.setName("Faculté des Sciences Ben M'Sik");
		f1.setCode("FSBM");
		f1 = facultyRepo.save(f1);

		// Phase 3: Departments
		Department dInfo = departmentRepo.save(new Department(null, "Informatique", "INFO", null, f1));
		Department dMath = departmentRepo.save(new Department(null, "Mathématiques", "MATH", null, f1));
		Department dPhys = departmentRepo.save(new Department(null, "Physique", "PHYS", null, f1));
		Department dBio = departmentRepo.save(new Department(null, "Biologie", "BIO", null, f1));

		// Phase 4: Reference data
		Major m1 = majorRepo.save(new Major(null, "Génie Informatique", dInfo));
		Major m2 = majorRepo.save(new Major(null, "Génie Industriel", dMath));
		Major m3 = majorRepo.save(new Major(null, "Génie Civil", dPhys));
		Major m4 = majorRepo.save(new Major(null, "Génie Électrique", dBio));
		Major m5 = majorRepo.save(new Major(null, "Management", dInfo));
		List<Major> majors = List.of(m1, m2, m3, m4, m5);

		Level n1 = levelRepo.save(new Level(null, "Licence"));
		Level n2 = levelRepo.save(new Level(null, "Master"));
		Level n3 = levelRepo.save(new Level(null, "Doctorat"));
		List<Level> levels = List.of(n1, n2, n3);

		TeacherRank g1 = teacherRankRepo.save(new TeacherRank(null, "PES"));
		TeacherRank g2 = teacherRankRepo.save(new TeacherRank(null, "PH"));
		TeacherRank g3 = teacherRankRepo.save(new TeacherRank(null, "PA"));
		List<TeacherRank> teacherRanks = List.of(g1, g2, g3);

		// Phase 6: Users
		User admin = new User();
		admin.setEmail("admin@univh2c.ma");
		admin.setPassword(PASSWORD);
		admin.setRole(Role.ADMIN);
		admin.setLastName("Ahmadi");
		admin.setFirstName("Mohamed");
		admin.setActive(true);
		admin = saveUser(admin);

		Coordinator coord = new Coordinator();
		coord.setLastName("Ouchen");
		coord.setFirstName("Yassin");
		coord.setEmail("coord@univh2c.ma");
		coord.setPassword(PASSWORD);
		coord.setRole(Role.COORDINATOR);
		coord.setActive(true);
		coord = userRepo.save(coord);

		Teacher teacherT3 = saveTeacher("Ben Ali", "Ali", "teacher@univh2c.ma", teacherRanks.get(0), dInfo);
		Teacher teacherT4 = saveTeacher("Alami", "Moussa", "moussa@univh2c.ma", teacherRanks.get(1), dMath);
		Teacher teacherT5 = saveTeacher("El Ghazi", "Hassan", "hassan@univh2c.ma", teacherRanks.get(2), dPhys);
		Teacher teacherT6 = saveTeacher("Benkirane", "Jamila", "jamila@univh2c.ma", teacherRanks.get(0), dBio);
		Teacher teacherT7 = saveTeacher("El Ouafi", "Rachid", "rachid@univh2c.ma", teacherRanks.get(1), dInfo);
		Teacher teacherT8 = saveTeacher("El Fekkak", "Khadija", "khadija@univh2c.ma", teacherRanks.get(2), dMath);
		Teacher teacherT9 = saveTeacher("Ben Omar", "Nabil", "nabil@univh2c.ma", teacherRanks.get(0), dPhys);
		Teacher teacherT10 = saveTeacher("El Kholti", "Samira", "samira@univh2c.ma", teacherRanks.get(1), dBio);
		Teacher teacherT11 = saveTeacher("El Idrissi", "Abdellah", "abdellah@univh2c.ma", teacherRanks.get(2), dInfo);
		Teacher teacherT12 = saveTeacher("Bensouda", "Fatiha", "fatiha@univh2c.ma", teacherRanks.get(0), dMath);
		Teacher teacherT13 = saveTeacher("El Mourabit", "Karim", "karim@univh2c.ma", teacherRanks.get(1), dPhys);
		Teacher teacherT14 = saveTeacher("El Hassani", "Latifa", "latifa@univh2c.ma", teacherRanks.get(2), dBio);

		List<Teacher> teachers = List.of(teacherT3, teacherT4, teacherT5, teacherT6, teacherT7, teacherT8, teacherT9,
				teacherT10, teacherT11, teacherT12, teacherT13, teacherT14);

		// Phase 7: Update department heads
		dInfo.setHead(teacherT6);
		dMath.setHead(teacherT7);
		dPhys.setHead(teacherT8);
		dBio.setHead(teacherT9);
		departmentRepo.saveAll(List.of(dInfo, dMath, dPhys, dBio));

		// Phase 8: Students (100)
		String[][] studentData = {{"Khalid", "Mohamed", "student", "E13000999"},
				{"Benali", "Salma", "student1", "E1300001"}, {"Fassi", "Yassine", "student2", "E1300002"},
				{"Tazi", "Fatima", "student3", "E1300003"}, {"Mansouri", "Mehdi", "student4", "E1300004"},
				{"Radi", "Sofia", "student5", "E1300005"}, {"Idrissi", "Omar", "student6", "E1300006"},
				{"Bennani", "Hajar", "student7", "E1300007"}, {"Kettani", "Khalid", "student8", "E1300008"},
				{"Amrani", "Layla", "student9", "E1300009"}, {"Lahlou", "Zakaria", "student10", "E1300010"},
				{"Sekkat", "Nadia", "student11", "E1300011"}, {"Guessous", "Hamza", "student12", "E1300012"},
				{"Filali", "Zineb", "student13", "E1300013"}, {"Skalli", "Anas", "student14", "E1300014"},
				{"Kadiri", "Meryem", "student15", "E1300015"}, {"Belkora", "Reda", "student16", "E1300016"},
				{"Mernissi", "Chaimae", "student17", "E1300017"}, {"Berrada", "Ayoub", "student18", "E1300018"},
				{"El Hachimi", "Ibtissam", "student19", "E1300019"}, {"El Amrani", "Youssef", "student20", "E1300020"},
				{"Bouazza", "Khaoula", "student21", "E1300021"}, {"Chaoui", "Rachid", "student22", "E1300022"},
				{"Dahbi", "Sara", "student23", "E1300023"}, {"El Fassi", "Hassan", "student24", "E1300024"},
				{"Guedira", "Amina", "student25", "E1300025"}, {"Hamidi", "Ahmed", "student26", "E1300026"},
				{"Jaidi", "Samira", "student27", "E1300027"}, {"Kabbaj", "Mohamed", "student28", "E1300028"},
				{"Lamrini", "Nawal", "student29", "E1300029"}, {"Alami", "Amine", "student30", "E1300030"},
				{"Benali", "Salma", "student31", "E1300031"}, {"Fassi", "Yassine", "student32", "E1300032"},
				{"Tazi", "Fatima", "student33", "E1300033"}, {"Mansouri", "Mehdi", "student34", "E1300034"},
				{"Radi", "Sofia", "student35", "E1300035"}, {"Idrissi", "Omar", "student36", "E1300036"},
				{"Bennani", "Hajar", "student37", "E1300037"}, {"Kettani", "Khalid", "student38", "E1300038"},
				{"Amrani", "Layla", "student39", "E1300039"}, {"Lahlou", "Zakaria", "student40", "E1300040"},
				{"Sekkat", "Nadia", "student41", "E1300041"}, {"Guessous", "Hamza", "student42", "E1300042"},
				{"Filali", "Zineb", "student43", "E1300043"}, {"Skalli", "Anas", "student44", "E1300044"},
				{"Kadiri", "Meryem", "student45", "E1300045"}, {"Belkora", "Reda", "student46", "E1300046"},
				{"Mernissi", "Chaimae", "student47", "E1300047"}, {"Berrada", "Ayoub", "student48", "E1300048"},
				{"El Hachimi", "Ibtissam", "student49", "E1300049"}, {"El Amrani", "Youssef", "student50", "E1300050"},
				{"Bouazza", "Khaoula", "student51", "E1300051"}, {"Chaoui", "Rachid", "student52", "E1300052"},
				{"Dahbi", "Sara", "student53", "E1300053"}, {"El Fassi", "Hassan", "student54", "E1300054"},
				{"Guedira", "Amina", "student55", "E1300055"}, {"Hamidi", "Ahmed", "student56", "E1300056"},
				{"Jaidi", "Samira", "student57", "E1300057"}, {"Kabbaj", "Mohamed", "student58", "E1300058"},
				{"Lamrini", "Nawal", "student59", "E1300059"}, {"Alami", "Amine", "student60", "E1300060"},
				{"Benali", "Salma", "student61", "E1300061"}, {"Fassi", "Yassine", "student62", "E1300062"},
				{"Tazi", "Fatima", "student63", "E1300063"}, {"Mansouri", "Mehdi", "student64", "E1300064"},
				{"Radi", "Sofia", "student65", "E1300065"}, {"Idrissi", "Omar", "student66", "E1300066"},
				{"Bennani", "Hajar", "student67", "E1300067"}, {"Kettani", "Khalid", "student68", "E1300068"},
				{"Amrani", "Layla", "student69", "E1300069"}, {"Lahlou", "Zakaria", "student70", "E1300070"},
				{"Sekkat", "Nadia", "student71", "E1300071"}, {"Guessous", "Hamza", "student72", "E1300072"},
				{"Filali", "Zineb", "student73", "E1300073"}, {"Skalli", "Anas", "student74", "E1300074"},
				{"Kadiri", "Meryem", "student75", "E1300075"}, {"Belkora", "Reda", "student76", "E1300076"},
				{"Mernissi", "Chaimae", "student77", "E1300077"}, {"Berrada", "Ayoub", "student78", "E1300078"},
				{"El Hachimi", "Ibtissam", "student79", "E1300079"}, {"El Amrani", "Youssef", "student80", "E1300080"},
				{"Bouazza", "Khaoula", "student81", "E1300081"}, {"Chaoui", "Rachid", "student82", "E1300082"},
				{"Dahbi", "Sara", "student83", "E1300083"}, {"El Fassi", "Hassan", "student84", "E1300084"},
				{"Guedira", "Amina", "student85", "E1300085"}, {"Hamidi", "Ahmed", "student86", "E1300086"},
				{"Jaidi", "Samira", "student87", "E1300087"}, {"Kabbaj", "Mohamed", "student88", "E1300088"},
				{"Lamrini", "Nawal", "student89", "E1300089"}, {"Alami", "Amine", "student90", "E1300090"},
				{"Benali", "Salma", "student91", "E1300091"}, {"Fassi", "Yassine", "student92", "E1300092"},
				{"Tazi", "Fatima", "student93", "E1300093"}, {"Mansouri", "Mehdi", "student94", "E1300094"},
				{"Radi", "Sofia", "student95", "E1300095"}, {"Idrissi", "Omar", "student96", "E1300096"},
				{"Bennani", "Hajar", "student97", "E1300097"}, {"Kettani", "Khalid", "student98", "E1300098"},
				{"Amrani", "Layla", "student99", "E1300099"},};

		int[] studentMajorIndices = {0, 1, 2, 3, 4, 0, 1, 2, 3, 4};
		int[] studentLevelIndices = {1, 1, 2, 0, 1, 2, 0, 1, 2, 0};

		List<Student> students = new ArrayList<>();
		for (int i = 0; i < studentData.length; i++) {
			String fn = studentData[i][1];
			String ln = studentData[i][0];
			String email = studentData[i][2] + "@univh2c.ma";
			String cne = studentData[i][3];
			String codeApogee = "APG" + String.format("%05d", i + 1);
			Major major = majors.get(studentMajorIndices[i % 10]);
			Level level = levels.get(studentLevelIndices[i % 10]);
			students.add(saveStudent(ln, fn, email, cne, codeApogee, major, level));
		}

		List<Student> studentsList = students;

		// Phase 9: Rooms
		List<Room> rooms = roomRepo
				.saveAll(List.of(new Room(null, "Amphi A", 200, dInfo), new Room(null, "Amphi B", 150, dInfo),
						new Room(null, "Salle 101", 50, dInfo), new Room(null, "Salle 102", 40, dMath),
						new Room(null, "Salle 103", 30, dMath), new Room(null, "Salle 201", 45, dMath),
						new Room(null, "Salle 202", 35, dPhys), new Room(null, "Salle 203", 25, dPhys),
						new Room(null, "Salle 301", 60, dPhys), new Room(null, "Salle 302", 35, dBio),
						new Room(null, "Labo Info", 25, dInfo), new Room(null, "Salle Réunion", 20, dBio)));

		// Phase 11: Jury Role Templates
		JuryRoleTemplate jrtPfe = juryRoleTemplateRepo.save(new JuryRoleTemplate(null, "Template PFE", DefenseType.PFE,
				List.of(new TemplateRole("Président", 1, 30), new TemplateRole("Rapporteur", 1, 35),
						new TemplateRole("Examinateur", 1, 35))));
		JuryRoleTemplate jrtMemoire = juryRoleTemplateRepo.save(new JuryRoleTemplate(null, "Template Mémoire",
				DefenseType.MEMOIRE, List.of(new TemplateRole("Président", 1, 30),
						new TemplateRole("Rapporteur", 1, 35), new TemplateRole("Examinateur", 2, 35))));
		JuryRoleTemplate jrtThese = juryRoleTemplateRepo.save(new JuryRoleTemplate(null, "Template Thèse",
				DefenseType.THESE, List.of(new TemplateRole("Président", 1, 25), new TemplateRole("Rapporteur", 2, 30),
						new TemplateRole("Examinateur", 2, 45))));

		// Phase 12: Defense Sessions
		Map<String, Integer> coeffs = new LinkedHashMap<>();
		coeffs.put("Président", 30);
		coeffs.put("Rapporteur", 35);
		coeffs.put("Examinateur", 35);

		DefenseSession ds1 = new DefenseSession();
		ds1.setName("Soutenance PFE Printemps 2025");
		ds1.setDefenseType(DefenseType.PFE);
		ds1.setStatus(DefenseSessionStatus.COMPLETED);
		ds1.setMaxGroupSize(3);
		ds1.setDefenseDuration(30);
		ds1.setBreakDuration(10);
		ds1.setSubmissionDeadline(LocalDate.of(2025, 5, 15));
		ds1.setEvaluationCoefficients(coeffs);
		ds1.setJuryRoleTemplate(jrtPfe);
		ds1.setStartDate(LocalDate.of(2025, 6, 1));
		ds1.setEndDate(LocalDate.of(2025, 6, 30));
		ds1.setFrozen(true);
		ds1.setAllowSupervisorInJury(false);
		ds1.setResultsPublished(false);
		ds1.setApprovedBy(admin.getId());
		ds1.setApprovedAt(LocalDateTime.of(2025, 5, 10, 9, 0));
		ds1.setStartTime("08:00");
		ds1.setEndTime("18:00");
		ds1.setGroupCreationStartDate("2026-03-01");
		ds1.setGroupCreationEndDate("2026-05-01");
		ds1.setRapportCoefficient(30);
		ds1.setSoutenanceCoefficient(70);
		ds1 = defenseSessionRepo.save(ds1);
		DefenseSession ds2 = new DefenseSession();
		ds2.setName("Soutenance PFE Automne 2025");
		ds2.setDefenseType(DefenseType.PFE);
		ds2.setStatus(DefenseSessionStatus.COMPLETED);
		ds2.setMaxGroupSize(3);
		ds2.setDefenseDuration(30);
		ds2.setBreakDuration(10);
		ds2.setSubmissionDeadline(LocalDate.of(2025, 12, 15));
		ds2.setEvaluationCoefficients(coeffs);
		ds2.setJuryRoleTemplate(jrtPfe);
		ds2.setStartDate(LocalDate.of(2026, 1, 5));
		ds2.setEndDate(LocalDate.of(2026, 1, 25));
		ds2.setFrozen(true);
		ds2.setAllowSupervisorInJury(false);
		ds2.setResultsPublished(false);
		ds2.setApprovedBy(admin.getId());
		ds2.setApprovedAt(LocalDateTime.of(2025, 12, 10, 9, 0));
		ds2.setStartTime("08:00");
		ds2.setEndTime("18:00");
		ds2.setGroupCreationStartDate("2026-03-01");
		ds2.setGroupCreationEndDate("2026-05-01");
		ds2.setRapportCoefficient(30);
		ds2.setSoutenanceCoefficient(70);
		ds2 = defenseSessionRepo.save(ds2);
		DefenseSession ds3 = new DefenseSession();
		ds3.setName("Soutenance PFE Printemps 2026");
		ds3.setDefenseType(DefenseType.PFE);
		ds3.setStatus(DefenseSessionStatus.ACTIVE);
		ds3.setMaxGroupSize(3);
		ds3.setDefenseDuration(30);
		ds3.setBreakDuration(10);
		ds3.setSubmissionDeadline(LocalDate.of(2026, 6, 1));
		ds3.setEvaluationCoefficients(coeffs);
		ds3.setJuryRoleTemplate(jrtPfe);
		ds3.setStartDate(LocalDate.of(2026, 6, 15));
		ds3.setEndDate(LocalDate.of(2026, 7, 10));
		ds3.setFrozen(false);
		ds3.setAllowSupervisorInJury(false);
		ds3.setResultsPublished(false);
		ds3.setStartTime("08:00");
		ds3.setEndTime("18:00");
		ds3.setGroupCreationStartDate("2026-03-01");
		ds3.setGroupCreationEndDate("2026-05-01");
		ds3.setRapportCoefficient(30);
		ds3.setSoutenanceCoefficient(70);
		ds3.setGroupFormationStartDate(LocalDate.of(2026, 6, 1));
		ds3.setGroupFormationEndDate(LocalDate.of(2026, 7, 5));
		ds3 = defenseSessionRepo.save(ds3);
		DefenseSession ds4 = new DefenseSession();
		ds4.setName("Soutenance Mémoire Printemps 2026");
		ds4.setDefenseType(DefenseType.MEMOIRE);
		ds4.setStatus(DefenseSessionStatus.SCHEDULED);
		ds4.setMaxGroupSize(4);
		ds4.setDefenseDuration(45);
		ds4.setBreakDuration(15);
		ds4.setSubmissionDeadline(LocalDate.of(2026, 6, 1));
		ds4.setEvaluationCoefficients(coeffs);
		ds4.setJuryRoleTemplate(jrtMemoire);
		ds4.setStartDate(LocalDate.of(2026, 6, 20));
		ds4.setEndDate(LocalDate.of(2026, 7, 15));
		ds4.setFrozen(false);
		ds4.setAllowSupervisorInJury(false);
		ds4.setResultsPublished(false);
		ds4.setApprovedBy(admin.getId());
		ds4.setApprovedAt(LocalDateTime.of(2026, 6, 10, 9, 0));
		ds4.setStartTime("08:00");
		ds4.setEndTime("18:00");
		ds4.setGroupCreationStartDate("2026-03-01");
		ds4.setGroupCreationEndDate("2026-05-01");
		ds4.setRapportCoefficient(30);
		ds4.setSoutenanceCoefficient(70);
		ds4 = defenseSessionRepo.save(ds4);
		Map<String, Integer> coeffs2 = new LinkedHashMap<>();
		coeffs2.put("Président", 25);
		coeffs2.put("Rapporteur", 30);
		coeffs2.put("Examinateur", 45);
		DefenseSession ds5 = new DefenseSession();
		ds5.setName("Soutenance Thèse Printemps 2026");
		ds5.setDefenseType(DefenseType.THESE);
		ds5.setStatus(DefenseSessionStatus.SCHEDULED);
		ds5.setMaxGroupSize(1);
		ds5.setDefenseDuration(60);
		ds5.setBreakDuration(20);
		ds5.setSubmissionDeadline(LocalDate.of(2026, 5, 15));
		ds5.setEvaluationCoefficients(coeffs2);
		ds5.setJuryRoleTemplate(jrtThese);
		ds5.setStartDate(LocalDate.of(2026, 6, 10));
		ds5.setEndDate(LocalDate.of(2026, 7, 5));
		ds5.setFrozen(false);
		ds5.setAllowSupervisorInJury(false);
		ds5.setResultsPublished(false);
		ds5.setApprovedBy(admin.getId());
		ds5.setApprovedAt(LocalDateTime.of(2026, 5, 20, 9, 0));
		ds5.setStartTime("08:00");
		ds5.setEndTime("18:00");
		ds5.setGroupCreationStartDate("2026-03-01");
		ds5.setGroupCreationEndDate("2026-05-01");
		ds5.setRapportCoefficient(30);
		ds5.setSoutenanceCoefficient(70);
		ds5 = defenseSessionRepo.save(ds5);
		DefenseSession ds6 = new DefenseSession();
		ds6.setName("Soutenance Rattrapage 2026");
		ds6.setDefenseType(DefenseType.PFE);
		ds6.setStatus(DefenseSessionStatus.DRAFT);
		ds6.setMaxGroupSize(3);
		ds6.setDefenseDuration(30);
		ds6.setBreakDuration(10);
		ds6.setSubmissionDeadline(LocalDate.of(2026, 8, 15));
		ds6.setEvaluationCoefficients(coeffs);
		ds6.setJuryRoleTemplate(jrtPfe);
		ds6.setStartDate(LocalDate.of(2026, 9, 1));
		ds6.setEndDate(LocalDate.of(2026, 9, 15));
		ds6.setFrozen(false);
		ds6.setAllowSupervisorInJury(false);
		ds6.setResultsPublished(false);
		ds6.setStartTime("08:00");
		ds6.setEndTime("18:00");
		ds6.setGroupCreationStartDate("2026-03-01");
		ds6.setGroupCreationEndDate("2026-05-01");
		ds6.setRapportCoefficient(30);
		ds6.setSoutenanceCoefficient(70);
		ds6 = defenseSessionRepo.save(ds6);

		// Phase 13: Projects
		record ProjSeed(String title, String desc, String dtype, ProjectStatus status, Teacher sup) {
		}
		ProjSeed[] projSeeds = {
				new ProjSeed("Système de Gestion des Soutenances",
						"Développement d'une plateforme web pour la gestion des soutenances de PFE.", "PFE",
						ProjectStatus.PENDING, teacherT3),
				new ProjSeed("Application E-commerce Mobile",
						"Application mobile de vente en ligne avec recommandations personnalisées.", "PFE",
						ProjectStatus.APPROVED, teacherT4),
				new ProjSeed("Plateforme IoT pour Smart Campus",
						"Plateforme IoT connectant les capteurs du campus universitaire.", "PFE", ProjectStatus.PENDING,
						teacherT5),
				new ProjSeed("Analyse Prédictive des Données Climatiques",
						"Analyse et prédiction des données climatiques avec deep learning.", "MEMOIRE",
						ProjectStatus.APPROVED, teacherT6),
				new ProjSeed("Chatbot Intelligent pour Services Universitaires",
						"Chatbot intelligent basé sur NLP pour les services universitaires.", "THESE",
						ProjectStatus.APPROVED, teacherT7),
				new ProjSeed("Système de Recommandation de Cours",
						"Système de recommandation de cours basé sur les compétences.", "PFE", ProjectStatus.PENDING,
						teacherT8),
				new ProjSeed("Application Blockchain pour Diplômes",
						"Application blockchain pour la vérification des diplômes.", "PFE", ProjectStatus.APPROVED,
						teacherT9),
				new ProjSeed("Outil de Visualisation de Données Génomiques",
						"Outil de visualisation interactive de données génomiques.", "MEMOIRE", ProjectStatus.APPROVED,
						teacherT10),
				new ProjSeed("Assistant Virtuel pour Bibliothèque",
						"Assistant virtuel pour la gestion de bibliothèque universitaire.", "PFE",
						ProjectStatus.PENDING, teacherT11),
				new ProjSeed("Système de Détection de Fraude Academique",
						"Système de détection de fraude académique par machine learning.", "THESE",
						ProjectStatus.APPROVED, teacherT12),
				new ProjSeed("Robot Autonome de Livraison",
						"Conception d'un robot autonome pour la livraison intra-campus.", "PFE", ProjectStatus.PENDING,
						teacherT13),
				new ProjSeed("Application de Réalité Augmentée pour l'Anatomie",
						"Application mobile de réalité augmentée pour l'étude de l'anatomie.", "PFE",
						ProjectStatus.APPROVED, teacherT14),
				new ProjSeed("Plateforme de E-Learning Adaptatif",
						"Plateforme d'apprentissage adaptatif utilisant l'IA.", "MEMOIRE", ProjectStatus.APPROVED,
						teacherT3),
				new ProjSeed("Système de Reconnaissance Faciale",
						"Système de reconnaissance faciale pour la sécurité du campus.", "PFE", ProjectStatus.PENDING,
						teacherT4),
				new ProjSeed("Optimisation des Transports Urbains",
						"Optimisation des transports urbains avec algorithmes génétiques.", "THESE",
						ProjectStatus.APPROVED, teacherT5),
				new ProjSeed("Jeu Sérieux pour l'Apprentissage de la Programmation",
						"Jeu sérieux pour l'apprentissage de la programmation orientée objet.", "PFE",
						ProjectStatus.PENDING, teacherT6),
				new ProjSeed("Application de Télémédecine", "Application de télémédecine avec consultation en ligne.",
						"MEMOIRE", ProjectStatus.APPROVED, teacherT7),
				new ProjSeed("Système de Gestion d'Énergie Solaire",
						"Système de gestion intelligente de l'énergie solaire.", "PFE", ProjectStatus.APPROVED,
						teacherT8),
				new ProjSeed("Outil d'Analyse de Sentiments Twitter",
						"Outil d'analyse des sentiments sur Twitter en temps réel.", "THESE", ProjectStatus.PENDING,
						teacherT9),
				new ProjSeed("Plateforme de Crowdfunding pour Projets Étudiants",
						"Plateforme de crowdfunding dédiée aux projets étudiants.", "PFE", ProjectStatus.APPROVED,
						teacherT10),
				new ProjSeed("Système de Détection d'Intrusions Réseau",
						"Système de détection d'intrusions basé sur l'apprentissage automatique.", "PFE",
						ProjectStatus.PENDING, teacherT11),
				new ProjSeed("Application de Suivi de Présence NFC",
						"Application de suivi de présence utilisant la technologie NFC.", "MEMOIRE",
						ProjectStatus.APPROVED, teacherT12),
				new ProjSeed("Générateur Automatique de Comptes Rendus",
						"Générateur automatique de comptes rendus de réunions.", "PFE", ProjectStatus.PENDING,
						teacherT13),
				new ProjSeed("Plateforme de Mentorat Étudiant",
						"Plateforme de mentorat connectant étudiants et professionnels.", "THESE",
						ProjectStatus.APPROVED, teacherT14),
				new ProjSeed("Système de Notation Automatique de Copies",
						"Système de notation automatique de copies avec deep learning.", "PFE", ProjectStatus.APPROVED,
						teacherT3),};

		List<Project> projects = new ArrayList<>();
		for (ProjSeed ps : projSeeds) {
			Project p = new Project();
			p.setTitle(ps.title);
			p.setDescription(ps.desc);
			p.setDefenseType(ps.dtype);
			p.setStatus(ps.status);
			p.setSupervisor(ps.sup);
			projects.add(projectRepo.save(p));
		}

		// Phase 15: Defenses (Unified Jury and Slots)
		int[] defenseProjectIds = {1, 3, 4, 6, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23, 24};
		String[][] juryCompositions = {{"Président", "Rapporteur", "Examinateur"}, {"Président", "Examinateur"},
				{"Président", "Rapporteur", "Examinateur", "Membre"}, {"Président", "Rapporteur"},
				{"Président", "Examinateur", "Membre"}, {"Président", "Rapporteur", "Examinateur"},
				{"Président", "Examinateur"}, {"Président", "Rapporteur", "Examinateur", "Membre"},
				{"Président", "Rapporteur"}, {"Président", "Examinateur", "Membre"},
				{"Président", "Rapporteur", "Examinateur"}, {"Président", "Examinateur"},
				{"Président", "Rapporteur", "Examinateur", "Membre"}, {"Président", "Rapporteur"},
				{"Président", "Examinateur", "Membre"}};
		List<Defense> defenses = new ArrayList<>();
		for (int i = 0; i < defenseProjectIds.length; i++) {
			Project project = projects.get(defenseProjectIds[i]);
			Defense d = new Defense();
			d.setProject(project);
			d.setDate(LocalDate.now());
			d.setTime(LocalTime.of(9, 0));
			d.setRoom(rooms.get(i % rooms.size()));

			List<JuryMember> members = new ArrayList<>();
			String[] roles = juryCompositions[i % juryCompositions.length];
			for (int j = 0; j < roles.length; j++) {
				members.add(
						new JuryMember(null, teachers.get((i + j) % teachers.size()), roles[j], d, null, null, null));
			}
			d.setMembers(members);

			defenses.add(defenseRepo.save(d));
		}

		// Phase 16: Groups
		record GrpSeed(String name, int projIdx, int sessionIdx, int[] studentIdxs) {
		}
		DefenseSession[] dsArr = {ds1, ds2, ds3, ds4, ds5, ds6};
		GrpSeed[] grpSeeds = {new GrpSeed("Groupe Alpha", 0, 0, new int[]{0, 1}),
				new GrpSeed("Groupe Beta", 1, 1, new int[]{3, 4, 5}),
				new GrpSeed("Groupe Gamma", 2, 2, new int[]{7, 8, 9, 10}),
				new GrpSeed("Groupe Delta", 3, 3, new int[]{11, 12}),
				new GrpSeed("Groupe Epsilon", 4, 4, new int[]{15, 16, 17}),
				new GrpSeed("Groupe Zeta", 5, 5, new int[]{19, 20, 21, 22}),
				new GrpSeed("Groupe Eta", 6, 0, new int[]{23, 24}),
				new GrpSeed("Groupe Theta", 7, 1, new int[]{27, 28, 29}),
				new GrpSeed("Groupe Iota", 8, 2, new int[]{31, 32, 33, 34}),
				new GrpSeed("Groupe Kappa", 9, 3, new int[]{35, 36}),
				new GrpSeed("Groupe Lambda", 10, 4, new int[]{39, 40, 41}),
				new GrpSeed("Groupe Mu", 11, 5, new int[]{43, 44, 45, 46}),
				new GrpSeed("Groupe Nu", 12, 0, new int[]{47, 48}),
				new GrpSeed("Groupe Xi", 13, 1, new int[]{51, 52, 53}),
				new GrpSeed("Groupe Omicron", 14, 2, new int[]{55, 56, 57, 58}),
				new GrpSeed("Groupe Pi", 15, 3, new int[]{59, 60}),
				new GrpSeed("Groupe Rho", 16, 4, new int[]{63, 64, 65}),
				new GrpSeed("Groupe Sigma", 17, 5, new int[]{67, 68, 69, 70}),
				new GrpSeed("Groupe Tau", 18, 0, new int[]{71, 72}),
				new GrpSeed("Groupe Upsilon", 19, 1, new int[]{75, 76, 77}),
				new GrpSeed("Groupe Phi", 20, 2, new int[]{79, 80, 81, 82}),
				new GrpSeed("Groupe Chi", 21, 3, new int[]{83, 84}),
				new GrpSeed("Groupe Psi", 22, 4, new int[]{87, 88, 89}),
				new GrpSeed("Groupe Omega", 23, 5, new int[]{91, 92, 93, 94}),
				new GrpSeed("Groupe A1", 24, 0, new int[]{95, 96}), new GrpSeed("Groupe A2", 0, 1, new int[]{0, 1, 2}),
				new GrpSeed("Groupe B1", 1, 2, new int[]{3, 4, 5, 6}), new GrpSeed("Groupe B2", 2, 3, new int[]{7, 8}),
				new GrpSeed("Groupe C1", 3, 4, new int[]{11, 12, 13}),
				new GrpSeed("Groupe C2", 4, 5, new int[]{15, 16, 17, 18}),
				new GrpSeed("Groupe D1", 5, 0, new int[]{19, 20}),
				new GrpSeed("Groupe D2", 6, 1, new int[]{23, 24, 25}),
				new GrpSeed("Groupe E1", 7, 2, new int[]{27, 28, 29, 30}),
				new GrpSeed("Groupe E2", 8, 3, new int[]{31, 32}),
				new GrpSeed("Groupe F1", 9, 4, new int[]{35, 36, 37}),
				new GrpSeed("Groupe F2", 10, 5, new int[]{39, 40, 41, 42}),
				new GrpSeed("Groupe G1", 11, 0, new int[]{43, 44}),
				new GrpSeed("Groupe G2", 12, 1, new int[]{47, 48, 49}),
				new GrpSeed("Groupe H1", 13, 2, new int[]{51, 52, 53, 54}),
				new GrpSeed("Groupe H2", 14, 3, new int[]{55, 56}),};
		for (GrpSeed gs : grpSeeds) {
			Group g = new Group();
			g.setGroupName(gs.name);
			g.setProject(projects.get(gs.projIdx));
			g.setStudents(new ArrayList<>());
			g.setDefenseSession(dsArr[gs.sessionIdx]);
			g.setLeaderId(studentsList.get(gs.studentIdxs[0]).getId());
			g.setStatus(com.system_gestion_soutenance.api.coordinator.group.entity.GroupStatus.ACTIVE);
			for (int si : gs.studentIdxs) {
				g.getStudents().add(studentsList.get(si));
			}
			groupRepo.save(g);
		}

		// Phase 18: Unavailabilities
		List<Unavailability> unavails = new ArrayList<>();
		int[][] unavailData = {{0, 4, 2026, 6, 16, 0, 1}, {0, 5, 2026, 6, 17, 0, 1, 2}, {0, 6, 2026, 6, 19, 0},
				{0, 7, 2026, 6, 22, 0, 1}, {0, 8, 2026, 6, 24, 0, 1, 2}, {0, 9, 2026, 6, 25, 0},
				{1, 10, 2026, 6, 15, 0, 1}, {1, 11, 2026, 6, 16, 0, 1, 2}, {1, 12, 2026, 6, 18, 0},
				{1, 13, 2026, 6, 19, 0, 1}, {1, 14, 2026, 6, 23, 0, 1, 2}, {1, 15, 2026, 6, 24, 0},
				{1, 16, 2026, 6, 26, 0, 1}, {2, 17, 2026, 6, 15, 0, 1, 2}, {2, 18, 2026, 6, 17, 0},
				{2, 19, 2026, 6, 18, 0, 1}, {2, 20, 2026, 6, 22, 0, 1, 2}, {2, 21, 2026, 6, 23, 0},
				{2, 22, 2026, 6, 25, 0, 1}, {2, 23, 2026, 6, 26, 0, 1, 2}, {3, 24, 2026, 6, 16, 0},
				{3, 25, 2026, 6, 17, 0, 1}, {3, 26, 2026, 6, 19, 0, 1, 2}, {3, 27, 2026, 6, 22, 0},
				{3, 28, 2026, 6, 24, 0, 1}, {3, 29, 2026, 6, 25, 0, 1, 2}, {4, 30, 2026, 6, 15, 0},
				{4, 31, 2026, 6, 16, 0, 1}, {4, 32, 2026, 6, 18, 0, 1, 2}, {4, 33, 2026, 6, 19, 0},
				{4, 34, 2026, 6, 23, 0, 1}, {4, 35, 2026, 6, 24, 0, 1, 2}, {4, 36, 2026, 6, 26, 0},
				{5, 37, 2026, 6, 15, 0, 1}, {5, 38, 2026, 6, 17, 0, 1, 2}, {5, 39, 2026, 6, 18, 0},
				{5, 40, 2026, 6, 22, 0, 1}, {5, 41, 2026, 6, 23, 0, 1, 2}, {5, 42, 2026, 6, 25, 0},
				{5, 43, 2026, 6, 26, 0, 1}, {6, 44, 2026, 6, 16, 0, 1, 2}, {6, 45, 2026, 6, 17, 0},
				{6, 46, 2026, 6, 19, 0, 1}, {6, 47, 2026, 6, 22, 0, 1, 2}, {6, 48, 2026, 6, 24, 0},
				{6, 49, 2026, 6, 25, 0, 1}, {7, 50, 2026, 6, 15, 0, 1, 2}, {7, 51, 2026, 6, 16, 0},
				{7, 52, 2026, 6, 18, 0, 1}, {7, 53, 2026, 6, 19, 0, 1, 2}, {7, 54, 2026, 6, 23, 0},
				{7, 55, 2026, 6, 24, 0, 1}, {7, 56, 2026, 6, 26, 0, 1, 2}, {8, 57, 2026, 6, 15, 0},
				{8, 58, 2026, 6, 17, 0, 1}, {8, 59, 2026, 6, 18, 0, 1, 2}, {8, 60, 2026, 6, 22, 0},
				{8, 61, 2026, 6, 23, 0, 1}, {8, 62, 2026, 6, 25, 0, 1, 2}, {8, 63, 2026, 6, 26, 0},
				{9, 64, 2026, 6, 16, 0, 1}, {9, 65, 2026, 6, 17, 0, 1, 2}, {9, 66, 2026, 6, 19, 0},
				{9, 67, 2026, 6, 22, 0, 1}, {9, 68, 2026, 6, 24, 0, 1, 2}, {9, 69, 2026, 6, 25, 0},
				{10, 70, 2026, 6, 15, 0, 1}, {10, 71, 2026, 6, 17, 0, 1, 2}, {10, 72, 2026, 6, 18, 0},
				{10, 73, 2026, 6, 22, 0, 1}, {10, 74, 2026, 6, 23, 0, 1, 2}, {10, 75, 2026, 6, 25, 0},
				{10, 76, 2026, 6, 26, 0, 1}, {11, 77, 2026, 6, 16, 0, 1, 2}, {11, 78, 2026, 6, 17, 0},
				{11, 79, 2026, 6, 19, 0, 1}, {11, 80, 2026, 6, 22, 0, 1, 2}, {11, 81, 2026, 6, 24, 0},
				{11, 82, 2026, 6, 25, 0, 1},};
		String[][] unavailSlots = {{"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"}, {"08:00-10:00", "10:00-12:00"},
				{"08:00-10:00", "10:00-12:00", "14:00-16:00"}, {"08:00-10:00"},};
		for (int i = 0; i < unavailData.length; i++) {
			int[] ud = unavailData[i];
			List<String> slots = new ArrayList<>();
			for (int s = 6; s < ud.length; s++) {
				slots.add(unavailSlots[ud[s]][0]);
			}
			Unavailability u = new Unavailability();
			u.setTeacherId(teachers.get(ud[0]).getId());
			u.setDate(java.time.LocalDate.of(ud[2], ud[3], ud[4]));
			u.setSlots(slots);
			unavails.add(u);
		}
		unavailabilityRepo.saveAll(unavails);

		// Phase 19: Evaluations
		record EvalSeed(Long tid, Long dsid, Long pid, String role, Double score, EvaluationStatus status,
				LocalDateTime submitted) {
		}
		List<EvalSeed> evalSeeds = Arrays.asList(
				new EvalSeed(teachers.get(0).getId(), ds1.getId(), projects.get(0).getId(), "Président", 16.4,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(1).getId(), ds2.getId(), projects.get(1).getId(), "Rapporteur", 10.3,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(2).getId(), ds3.getId(), projects.get(2).getId(), "Examinateur", 12.8,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(3).getId(), ds4.getId(), projects.get(3).getId(), "Président", 12.2,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(4).getId(), ds5.getId(), projects.get(4).getId(), "Rapporteur", null,
						EvaluationStatus.PENDING, null),
				new EvalSeed(teachers.get(5).getId(), ds1.getId(), projects.get(5).getId(), "Examinateur", null,
						EvaluationStatus.PENDING, null),
				new EvalSeed(teachers.get(6).getId(), ds2.getId(), projects.get(6).getId(), "Président", 17.4,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(7).getId(), ds3.getId(), projects.get(7).getId(), "Rapporteur", 16.8,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(8).getId(), ds4.getId(), projects.get(8).getId(), "Examinateur", 18.9,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(9).getId(), ds5.getId(), projects.get(9).getId(), "Président", null,
						EvaluationStatus.PENDING, null),
				new EvalSeed(teachers.get(10).getId(), ds1.getId(), projects.get(10).getId(), "Rapporteur", 10.9,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(11).getId(), ds2.getId(), projects.get(11).getId(), "Examinateur", null,
						EvaluationStatus.PENDING, null),
				new EvalSeed(teachers.get(0).getId(), ds3.getId(), projects.get(12).getId(), "Président", 14.2,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(1).getId(), ds4.getId(), projects.get(13).getId(), "Rapporteur", 10.3,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(2).getId(), ds5.getId(), projects.get(14).getId(), "Examinateur", 12.2,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(3).getId(), ds1.getId(), projects.get(15).getId(), "Président", null,
						EvaluationStatus.PENDING, null),
				new EvalSeed(teachers.get(4).getId(), ds2.getId(), projects.get(16).getId(), "Rapporteur", 15.1,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(5).getId(), ds3.getId(), projects.get(17).getId(), "Examinateur", 10.3,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(6).getId(), ds4.getId(), projects.get(18).getId(), "Président", 12.0,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(7).getId(), ds5.getId(), projects.get(19).getId(), "Rapporteur", 16.5,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(8).getId(), ds1.getId(), projects.get(20).getId(), "Examinateur", 15.4,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(9).getId(), ds2.getId(), projects.get(21).getId(), "Président", 12.2,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(10).getId(), ds3.getId(), projects.get(22).getId(), "Rapporteur", 15.9,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(11).getId(), ds4.getId(), projects.get(23).getId(), "Examinateur", 18.1,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(0).getId(), ds5.getId(), projects.get(24).getId(), "Président", 10.1,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(1).getId(), ds1.getId(), projects.get(0).getId(), "Rapporteur", null,
						EvaluationStatus.PENDING, null),
				new EvalSeed(teachers.get(2).getId(), ds2.getId(), projects.get(1).getId(), "Examinateur", 18.1,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(3).getId(), ds3.getId(), projects.get(2).getId(), "Président", 17.0,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)),
				new EvalSeed(teachers.get(4).getId(), ds4.getId(), projects.get(3).getId(), "Rapporteur", null,
						EvaluationStatus.PENDING, null),
				new EvalSeed(teachers.get(5).getId(), ds5.getId(), projects.get(4).getId(), "Examinateur", 13.4,
						EvaluationStatus.SUBMITTED, LocalDateTime.of(2026, 6, 20, 10, 0)));
		for (EvalSeed es : evalSeeds) {
			Defense defense = defenseRepo.findByProject(projectRepo.findById(es.pid).orElse(null)).orElse(null);
			if (defense == null) {
				continue;
			}
			evaluationRepo.save(new Evaluation(null, es.tid, es.dsid, defense, es.role,
					com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationType.SOUTENANCE, es.score,
					"Évaluation complète.", es.status, es.submitted, null));
		}

		// Phase 20: Student Documents
		record DocSeed(Long sid, String name, String type, String status, LocalDateTime submitted) {
		}
		List<DocSeed> docSeeds = Arrays.asList(
				new DocSeed(studentsList.get(0).getId(), "Rapport PFE", "report", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(1).getId(), "Fiche de synthese", "summary", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(2).getId(), "Presentation", "presentation", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(3).getId(), "Poster", "poster", "missing", null),
				new DocSeed(studentsList.get(4).getId(), "Rapport technique", "technical_report", "missing", null),
				new DocSeed(studentsList.get(5).getId(), "Manuel utilisateur", "user_manual", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(6).getId(), "Code source", "source_code", "missing", null),
				new DocSeed(studentsList.get(7).getId(), "Annexes", "appendices", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(8).getId(), "Rapport PFE", "report", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(9).getId(), "Fiche de synthese", "summary", "missing", null),
				new DocSeed(studentsList.get(10).getId(), "Presentation", "presentation", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(11).getId(), "Poster", "poster", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(12).getId(), "Rapport technique", "technical_report", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(13).getId(), "Manuel utilisateur", "user_manual", "missing", null),
				new DocSeed(studentsList.get(14).getId(), "Code source", "source_code", "missing", null),
				new DocSeed(studentsList.get(15).getId(), "Annexes", "appendices", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(16).getId(), "Rapport PFE", "report", "missing", null),
				new DocSeed(studentsList.get(17).getId(), "Fiche de synthese", "summary", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(18).getId(), "Presentation", "presentation", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(19).getId(), "Poster", "poster", "missing", null),
				new DocSeed(studentsList.get(20).getId(), "Rapport technique", "technical_report", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(21).getId(), "Manuel utilisateur", "user_manual", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(22).getId(), "Code source", "source_code", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(23).getId(), "Annexes", "appendices", "missing", null),
				new DocSeed(studentsList.get(24).getId(), "Rapport PFE", "report", "missing", null),
				new DocSeed(studentsList.get(25).getId(), "Fiche de synthese", "summary", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(26).getId(), "Presentation", "presentation", "missing", null),
				new DocSeed(studentsList.get(27).getId(), "Poster", "poster", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(28).getId(), "Rapport technique", "technical_report", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(29).getId(), "Manuel utilisateur", "user_manual", "missing", null),
				new DocSeed(studentsList.get(30).getId(), "Code source", "source_code", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(31).getId(), "Annexes", "appendices", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(32).getId(), "Rapport PFE", "report", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(33).getId(), "Fiche de synthese", "summary", "missing", null),
				new DocSeed(studentsList.get(34).getId(), "Presentation", "presentation", "missing", null),
				new DocSeed(studentsList.get(35).getId(), "Poster", "poster", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(36).getId(), "Rapport technique", "technical_report", "missing", null),
				new DocSeed(studentsList.get(37).getId(), "Manuel utilisateur", "user_manual", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(38).getId(), "Code source", "source_code", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(39).getId(), "Annexes", "appendices", "missing", null),
				new DocSeed(studentsList.get(40).getId(), "Rapport PFE", "report", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(41).getId(), "Fiche de synthese", "summary", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(42).getId(), "Presentation", "presentation", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(43).getId(), "Poster", "poster", "missing", null),
				new DocSeed(studentsList.get(44).getId(), "Rapport technique", "technical_report", "missing", null),
				new DocSeed(studentsList.get(45).getId(), "Manuel utilisateur", "user_manual", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(46).getId(), "Code source", "source_code", "missing", null),
				new DocSeed(studentsList.get(47).getId(), "Annexes", "appendices", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(48).getId(), "Rapport PFE", "report", "submitted",
						LocalDateTime.of(2026, 5, 25, 14, 0)),
				new DocSeed(studentsList.get(49).getId(), "Fiche de synthese", "summary", "missing", null));
		for (DocSeed doc : docSeeds) {
			String fp = doc.submitted != null
					? "/uploads/" + studentsList.get((int) (doc.sid - studentsList.get(0).getId())) + "/"
							+ doc.name.toLowerCase().replace(" ", "_") + ".pdf"
					: null;
			studentDocumentRepo.save(new StudentDocument(null, doc.sid, doc.name, doc.type,
					java.time.LocalDate.parse("2026-06-01"), doc.status, doc.submitted, fp));
		}

		// Phase 21: Notifications
		record NotifSeed(NotificationType type, String title, String msg, int day, int month, boolean read,
				String actor) {
		}
		NotifSeed[] notifSeeds = {
				new NotifSeed(NotificationType.INFO, "Nouvelle soutenance programmée",
						"Message détaillé pour nouvelle soutenance programmée.", 10, 5, true, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.WARNING, "Soumission de document",
						"Message détaillé pour soumission de document.", 11, 5, true, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.SUCCESS, "Rappel: date limite approche",
						"Message détaillé pour rappel: date limite approche.", 12, 5, true, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.ERROR, "Changement de jury", "Message détaillé pour changement de jury.",
						13, 5, true, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.REMINDER, "Note publiée", "Message détaillé pour note publiée.", 14, 5,
						true, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.INFO, "Soutenance annulée", "Message détaillé pour soutenance annulée.",
						15, 5, true, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.WARNING, "Nouveau projet ajouté",
						"Message détaillé pour nouveau projet ajouté.", 16, 5, true, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.SUCCESS, "Date de soutenance modifiée",
						"Message détaillé pour date de soutenance modifiée.", 17, 5, true, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.ERROR, "Feedback disponible",
						"Message détaillé pour feedback disponible.", 18, 5, true, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.REMINDER, "Inscription confirmée",
						"Message détaillé pour inscription confirmée.", 19, 5, true, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.INFO, "Session ouverte", "Message détaillé pour session ouverte.", 20, 5,
						true, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.WARNING, "Compte rendu disponible",
						"Message détaillé pour compte rendu disponible.", 21, 5, true, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.SUCCESS, "Nouvelle soutenance programmée",
						"Message détaillé pour nouvelle soutenance programmée.", 22, 5, true, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.ERROR, "Soumission de document",
						"Message détaillé pour soumission de document.", 23, 5, true, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.REMINDER, "Rappel: date limite approche",
						"Message détaillé pour rappel: date limite approche.", 24, 5, true, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.INFO, "Changement de jury", "Message détaillé pour changement de jury.",
						25, 5, false, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.WARNING, "Note publiée", "Message détaillé pour note publiée.", 26, 5,
						false, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.SUCCESS, "Soutenance annulée",
						"Message détaillé pour soutenance annulée.", 27, 5, false, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.ERROR, "Nouveau projet ajouté",
						"Message détaillé pour nouveau projet ajouté.", 28, 5, false, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.REMINDER, "Date de soutenance modifiée",
						"Message détaillé pour date de soutenance modifiée.", 29, 5, false, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.INFO, "Feedback disponible",
						"Message détaillé pour feedback disponible.", 10, 5, false, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.WARNING, "Inscription confirmée",
						"Message détaillé pour inscription confirmée.", 11, 5, false, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.SUCCESS, "Session ouverte", "Message détaillé pour session ouverte.", 12,
						5, false, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.ERROR, "Compte rendu disponible",
						"Message détaillé pour compte rendu disponible.", 13, 5, false, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.REMINDER, "Nouvelle soutenance programmée",
						"Message détaillé pour nouvelle soutenance programmée.", 14, 5, false, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.INFO, "Soumission de document",
						"Message détaillé pour soumission de document.", 15, 5, false, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.WARNING, "Rappel: date limite approche",
						"Message détaillé pour rappel: date limite approche.", 16, 5, false, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.SUCCESS, "Changement de jury",
						"Message détaillé pour changement de jury.", 17, 5, false, "coord@univh2c.ma"),
				new NotifSeed(NotificationType.ERROR, "Note publiée", "Message détaillé pour note publiée.", 18, 5,
						false, "admin@univh2c.ma"),
				new NotifSeed(NotificationType.REMINDER, "Soutenance annulée",
						"Message détaillé pour soutenance annulée.", 19, 5, false, "coord@univh2c.ma"),};
		for (NotifSeed ns : notifSeeds) {
			notificationRepo.save(new AppNotification(null, ns.type, ns.title, ns.msg,
					LocalDateTime.of(2026, ns.month, ns.day, 8 + (ns.day % 8), 0), ns.read, null, "/dashboard",
					ns.actor));
		}

		// Phase 22: Audit Logs
		auditLogRepo.save(new AuditLog(null, "CREATE", "DefenseSession", ds3.getId(), "coord@univh2c.ma",
				"Création de la session de soutenance 'Soutenance PFE Printemps 2026'",
				LocalDateTime.of(2026, 3, 10, 9, 15)));
		auditLogRepo.save(new AuditLog(null, "UPDATE", "DefenseSession", ds3.getId(), "coord@univh2c.ma",
				"Modification des paramètres de la session 'Soutenance PFE Printemps 2026' (durée de soutenance: 30min)",
				LocalDateTime.of(2026, 3, 12, 10, 30)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "Project", projects.get(0).getId(), "coord@univh2c.ma",
				"Création du projet 'Système de Gestion des Soutenances' pour l'étudiant Khalid Mohamed",
				LocalDateTime.of(2026, 3, 15, 11, 0)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "Project", projects.get(1).getId(), "coord@univh2c.ma",
				"Création du projet 'Application E-commerce Mobile' pour le groupe de Benali Salma et Fassi Yassine",
				LocalDateTime.of(2026, 3, 18, 14, 20)));
		auditLogRepo.save(new AuditLog(null, "UPDATE_STATUS", "Project", projects.get(1).getId(), "admin@univh2c.ma",
				"Approbation du projet 'Application E-commerce Mobile' — statut changé de PENDING vers APPROVED",
				LocalDateTime.of(2026, 3, 20, 9, 0)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "Group", null, "coord@univh2c.ma",
				"Création du groupe 'Groupe Alpha' (2 étudiants) pour le projet 'Système de Gestion des Soutenances'",
				LocalDateTime.of(2026, 3, 22, 15, 45)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "Group", null, "coord@univh2c.ma",
				"Création du groupe 'Groupe Beta' (3 étudiants) pour le projet 'Application E-commerce Mobile'",
				LocalDateTime.of(2026, 3, 22, 16, 0)));
		auditLogRepo.save(new AuditLog(null, "UPDATE", "DefenseSession", ds1.getId(), "admin@univh2c.ma",
				"Clôture de la session 'Soutenance PFE Printemps 2025' — passage au statut COMPLETED",
				LocalDateTime.of(2026, 4, 1, 8, 30)));
		auditLogRepo.save(new AuditLog(null, "APPROVE", "DefenseSession", ds4.getId(), "admin@univh2c.ma",
				"Approbation de la session 'Soutenance Mémoire Printemps 2026' — statut changé vers SCHEDULED",
				LocalDateTime.of(2026, 4, 5, 9, 15)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "Defense", null, "coord@univh2c.ma",
				"Planification de la soutenance du projet 'Système de Gestion des Soutenances' en salle Amphi A",
				LocalDateTime.of(2026, 4, 10, 10, 0)));
		auditLogRepo.save(new AuditLog(null, "UPDATE", "Teacher", teacherT3.getId(), "admin@univh2c.ma",
				"Mise à jour du département de l'enseignant Ben Ali — transfert vers le département Informatique",
				LocalDateTime.of(2026, 4, 12, 11, 30)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "JuryRoleTemplate", jrtPfe.getId(), "admin@univh2c.ma",
				"Création du template jury 'Template PFE' avec 3 rôles (Président, Rapporteur, Examinateur)",
				LocalDateTime.of(2026, 4, 15, 14, 0)));
		auditLogRepo.save(new AuditLog(null, "SUBMIT", "StudentDocument", null, "student@univh2c.ma",
				"Soumission du document 'Rapport PFE' par l'étudiant Khalid Mohamed",
				LocalDateTime.of(2026, 5, 25, 14, 10)));
		auditLogRepo.save(new AuditLog(null, "SUBMIT", "StudentDocument", null, "student1@univh2c.ma",
				"Soumission du document 'Fiche de synthèse' par l'étudiant Benali Salma",
				LocalDateTime.of(2026, 5, 25, 14, 25)));
		auditLogRepo.save(new AuditLog(null, "PUBLISH", "Evaluation", null, "teacher@univh2c.ma",
				"Publication de l'évaluation du projet 'Système de Gestion des Soutenances' — note: 16.4/20",
				LocalDateTime.of(2026, 6, 1, 10, 0)));
		auditLogRepo.save(new AuditLog(null, "PUBLISH", "Evaluation", null, "moussa@univh2c.ma",
				"Publication de l'évaluation du projet 'Application E-commerce Mobile' — note: 10.3/20",
				LocalDateTime.of(2026, 6, 1, 10, 30)));
		auditLogRepo.save(new AuditLog(null, "UPDATE_STATUS", "Project", projects.get(5).getId(), "admin@univh2c.ma",
				"Rejet du projet 'Système de Recommandation de Cours' — motif: insuffisance de détails techniques",
				LocalDateTime.of(2026, 6, 3, 9, 45)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "Defense", null, "coord@univh2c.ma",
				"Planification de la soutenance du projet 'Application E-commerce Mobile' en salle Salle 101",
				LocalDateTime.of(2026, 6, 5, 11, 15)));
		auditLogRepo.save(new AuditLog(null, "FREEZE", "DefenseSession", ds3.getId(), "admin@univh2c.ma",
				"Gel de la session 'Soutenance PFE Printemps 2026' — les notes ne peuvent plus être modifiées",
				LocalDateTime.of(2026, 6, 10, 8, 0)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "Student", studentsList.get(95).getId(), "admin@univh2c.ma",
				"Création de l'étudiant Amrani Layla (codeApogee: APG00096) — niveau Master, filière Génie Informatique",
				LocalDateTime.of(2026, 6, 12, 15, 30)));
		auditLogRepo.save(new AuditLog(null, "UPDATE", "Room", rooms.get(0).getId(), "admin@univh2c.ma",
				"Mise à jour de la salle 'Amphi A' — capacité modifiée de 200 à 220 places",
				LocalDateTime.of(2026, 6, 14, 10, 0)));
		auditLogRepo.save(new AuditLog(null, "DELETE", "DefenseSession", ds6.getId(), "coord@univh2c.ma",
				"Suppression de la session 'Soutenance Rattrapage 2026' — pas de groupes assignés",
				LocalDateTime.of(2026, 6, 15, 16, 0)));
		auditLogRepo.save(new AuditLog(null, "APPROVE", "Project", projects.get(8).getId(), "admin@univh2c.ma",
				"Approbation du projet 'Assistant Virtuel pour Bibliothèque' — statut changé vers APPROVED",
				LocalDateTime.of(2026, 6, 16, 9, 30)));
		auditLogRepo.save(new AuditLog(null, "UPDATE", "DefenseSession", ds3.getId(), "coord@univh2c.ma",
				"Modification de la date de début de la session 'Soutenance PFE Printemps 2026' au 1er juillet 2026",
				LocalDateTime.of(2026, 6, 18, 14, 0)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "Notification", null, "coord@univh2c.ma",
				"Envoi de notification de rappel aux étudiants pour la soumission des documents avant le 1er juillet",
				LocalDateTime.of(2026, 6, 20, 11, 0)));
		auditLogRepo.save(new AuditLog(null, "UPDATE_STATUS", "Project", projects.get(15).getId(), "admin@univh2c.ma",
				"Approbation du projet 'Jeu Sérieux pour l'Apprentissage de la Programmation' — statut changé vers APPROVED",
				LocalDateTime.of(2026, 6, 22, 10, 15)));
		auditLogRepo.save(new AuditLog(null, "ASSIGN", "Defense", null, "coord@univh2c.ma",
				"Affectation de l'enseignant El Ghazi Hassan comme Président du jury pour la soutenance du projet 'Plateforme IoT pour Smart Campus'",
				LocalDateTime.of(2026, 6, 25, 9, 0)));
		auditLogRepo.save(new AuditLog(null, "UPDATE", "Student", studentsList.get(2).getId(), "admin@univh2c.ma",
				"Mise à jour du niveau de l'étudiant Fassi Yassine — passage de Licence à Master",
				LocalDateTime.of(2026, 6, 27, 15, 30)));
		auditLogRepo.save(new AuditLog(null, "CREATE", "Faculty", f1.getId(), "admin@univh2c.ma",
				"Création de la faculté 'Faculté des Sciences Ben M'Sik' (code: FSBM)",
				LocalDateTime.of(2026, 3, 5, 8, 0)));
		auditLogRepo.save(new AuditLog(null, "UPDATE", "Department", dInfo.getId(), "admin@univh2c.ma",
				"Désignation de Benkirane Jamila comme chef du département Informatique",
				LocalDateTime.of(2026, 3, 7, 10, 0)));

		System.out.println(">>> Seed data inserted successfully <<<");
	}

	private User saveUser(User user) {
		return userRepo.save(user);
	}

	private Teacher saveTeacher(String lastName, String firstName, String email, TeacherRank teacherRank,
			Department dept) {
		Teacher t = new Teacher();
		t.setLastName(lastName);
		t.setFirstName(firstName);
		t.setEmail(email);
		t.setPassword(PASSWORD);
		t.setRole(Role.TEACHER);
		t.setActive(true);
		t.setTeacherRank(teacherRank);
		t.setDepartment(dept);
		return teacherRepo.save(t);
	}

	private Student saveStudent(String lastName, String firstName, String email, String cne, String codeApogee,
			Major major, Level level) {
		Student s = new Student();
		s.setLastName(lastName);
		s.setFirstName(firstName);
		s.setEmail(email);
		s.setPassword(PASSWORD);
		s.setRole(Role.STUDENT);
		s.setActive(true);
		s.setCne(cne);
		s.setCodeApogee(codeApogee);
		s.setMajor(major);
		s.setLevel(level);
		return studentRepo.save(s);
	}
}
