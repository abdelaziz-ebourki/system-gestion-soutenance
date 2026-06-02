package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.repository.GradeRepository;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.user.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

	@Mock
	private MajorRepository majorRepository;
	@Mock
	private LevelRepository levelRepository;
	@Mock
	private GradeRepository gradeRepository;
	@Mock
	private DepartmentRepository departmentRepository;

	@InjectMocks
	private UserProfileService userProfileService;

	private User user;
	private Student student;
	private Teacher teacher;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setFirstName("First");
		user.setLastName("Last");

		student = new Student();
		student.setCne("CNE123");

		teacher = new Teacher();
	}

	@Test
	void updateBasicInfo_Success() {
		UpdateUserRequest request = new UpdateUserRequest("NewLast", "NewFirst", null, null, null, null, null, null,
				null);
		userProfileService.updateBasicInfo(user, request);

		assertEquals("NewLast", user.getLastName());
		assertEquals("NewFirst", user.getFirstName());
	}

	@Test
	void updateBasicInfo_PartialUpdate() {
		UpdateUserRequest request = new UpdateUserRequest("NewLast", null, null, null, null, null, null, null, null);
		userProfileService.updateBasicInfo(user, request);

		assertEquals("NewLast", user.getLastName());
		assertEquals("First", user.getFirstName());
	}

	@Test
	void updateStudentProfile_Success() {
		UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, "CNE456", 1L, 1L, null, null);
		Major major = new Major();
		Level level = new Level();

		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(levelRepository.findById(1L)).thenReturn(Optional.of(level));

		userProfileService.updateStudentProfile(student, request);

		assertEquals("CNE456", student.getCne());
		assertEquals(major, student.getMajor());
		assertEquals(level, student.getLevel());
	}

	@Test
	void updateStudentProfile_MajorNotFound_ThrowsException() {
		UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, 1L, null, null, null);
		when(majorRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userProfileService.updateStudentProfile(student, request));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Filière introuvable", ex.getReason());
	}

	@Test
	void updateTeacherProfile_Success() {
		UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, null, null, 1L, 1L);
		Grade grade = new Grade();
		Department dept = new Department();

		when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));

		userProfileService.updateTeacherProfile(teacher, request);

		assertEquals(grade, teacher.getGrade());
		assertEquals(dept, teacher.getDepartment());
	}

	@Test
	void updateTeacherProfile_DepartmentNotFound_ThrowsException() {
		UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, null, null, null, 1L);
		when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userProfileService.updateTeacherProfile(teacher, request));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Département introuvable", ex.getReason());
	}
}
