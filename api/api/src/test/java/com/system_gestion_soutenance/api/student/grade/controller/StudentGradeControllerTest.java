package com.system_gestion_soutenance.api.student.grade.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.service.PdfGenerationService;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StudentGradeController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class StudentGradeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DefenseRepository defenseRepository;

	@MockitoBean
	private GroupRepository groupRepository;

	@MockitoBean
	private SecurityService securityService;

	@MockitoBean
	private PdfGenerationService pdfGenerationService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	private Group group;
	private Project project;
	private Defense defense;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				new com.system_gestion_soutenance.api.user.entity.User(), null, List.of()));

		project = new Project();
		project.setId(1L);
		project.setTitle("Projet Test");

		Student student = new Student();
		student.setId(1L);
		student.setFirstName("Alice");
		student.setLastName("Martin");

		group = new Group();
		group.setId(10L);
		group.setProject(project);
		group.setStudents(List.of(student));

		defense = new Defense();
		defense.setId(100L);
		defense.setProject(project);
		defense.setDate(LocalDate.of(2026, 6, 15));
		defense.setFinalScore(15.5);
		defense.setMention("Très Bien");
		defense.setMembers(List.of());
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getMyGrade_whenPublished_returnsGrade() throws Exception {
		when(securityService.getCurrentUserId()).thenReturn(1L);
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		mockMvc.perform(get("/api/student/grade")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.projectTitle").value("Projet Test"))
				.andExpect(jsonPath("$.data.finalScore").value(15.5))
				.andExpect(jsonPath("$.data.mention").value("Très Bien"))
				.andExpect(jsonPath("$.data.status").value("published"));
	}

	@Test
	void getMyGrade_whenPending_returnsPendingStatus() throws Exception {
		defense.setFinalScore(null);
		defense.setMention(null);

		when(securityService.getCurrentUserId()).thenReturn(1L);
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		mockMvc.perform(get("/api/student/grade")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.finalScore").isEmpty())
				.andExpect(jsonPath("$.data.status").value("pending"));
	}

	@Test
	void getMyGrade_whenNoGroup_returnsNotFound() throws Exception {
		when(securityService.getCurrentUserId()).thenReturn(1L);
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/student/grade")).andExpect(status().isNotFound());
	}

	@Test
	void getMyGrade_whenNoProject_returnsNotFound() throws Exception {
		group.setProject(null);

		when(securityService.getCurrentUserId()).thenReturn(1L);
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));

		mockMvc.perform(get("/api/student/grade")).andExpect(status().isNotFound());
	}

	@Test
	void getCertificate_returnsPdf() throws Exception {
		when(securityService.getCurrentUserId()).thenReturn(1L);
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));
		when(pdfGenerationService.generatePdf(eq("certificate"), any())).thenReturn(new byte[]{1, 2, 3});

		mockMvc.perform(get("/api/student/grade/certificate")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void getCertificate_whenScoreNull_returnsNotFound() throws Exception {
		defense.setFinalScore(null);

		when(securityService.getCurrentUserId()).thenReturn(1L);
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		mockMvc.perform(get("/api/student/grade/certificate")).andExpect(status().isNotFound());
	}
}
