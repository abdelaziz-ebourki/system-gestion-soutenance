package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.repository.TeacherRankRepository;
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
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;

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
	private TeacherRankRepository teacherRankRepository;
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
				null, null);
		userProfileService.updateBasicInfo(user, request);

		assertEquals("NewLast", user.getLastName());
		assertEquals("NewFirst", user.getFirstName());
	}

	@Test
	void updateBasicInfo_PartialUpdate() {
		UpdateUserRequest request = new UpdateUserRequest("NewLast", null, null, null, null, null, null, null, null,
				null);
		userProfileService.updateBasicInfo(user, request);

		assertEquals("NewLast", user.getLastName());
		assertEquals("First", user.getFirstName());
	}

	@Test
	void updateStudentProfile_Success() {
		UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, "CNE456", null, 1L, 1L, null, null);
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
		UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, null, 1L, null, null, null);
		when(majorRepository.findById(1L)).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userProfileService.updateStudentProfile(student, request));
		assertEquals("Filière introuvable", ex.getMessage());
	}

	@Test
	void updateTeacherProfile_Success() {
		UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, null, null, null, 1L, 1L);
		TeacherRank teacherRank = new TeacherRank();
		Department dept = new Department();

		when(teacherRankRepository.findById(1L)).thenReturn(Optional.of(teacherRank));
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));

		userProfileService.updateTeacherProfile(teacher, request);

		assertEquals(teacherRank, teacher.getTeacherRank());
		assertEquals(dept, teacher.getDepartment());
	}

	@Test
	void updateTeacherProfile_DepartmentNotFound_ThrowsException() {
		UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, null, null, null, null, 1L);
		when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userProfileService.updateTeacherProfile(teacher, request));
		assertEquals("Département introuvable", ex.getMessage());
	}
}
