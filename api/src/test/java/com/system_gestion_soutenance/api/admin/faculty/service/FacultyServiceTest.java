package com.system_gestion_soutenance.api.admin.faculty.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.faculty.dto.CreateFacultyRequest;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.repository.FacultyRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FacultyServiceTest {

	@Mock
	private FacultyRepository facultyRepository;
	@Mock
	private DepartmentRepository departmentRepository;
	@Mock
	private TeacherRepository teacherRepository;

	@InjectMocks
	private FacultyService facultyService;

	private static Faculty createFaculty(Long id, String name, String code, Teacher dean, String logoUrl) {
		Faculty f = new Faculty();
		f.setId(id);
		f.setName(name);
		f.setCode(code);
		f.setDean(dean);
		f.setLogoUrl(logoUrl);
		return f;
	}

	@Test
	void findAll_returnsAll() {
		when(facultyRepository.findAll()).thenReturn(List.of(new Faculty()));
		assertEquals(1, facultyService.findAll().size());
	}

	@Test
	void findById_existing_returnsFaculty() {
		Faculty faculty = createFaculty(1L, "FS", "FS", null, null);
		when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));
		assertEquals(1L, facultyService.findById(1L).getId());
	}

	@Test
	void findById_missing_throws() {
		when(facultyRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> facultyService.findById(99L));
	}

	@Test
	void create_success() {
		when(facultyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		CreateFacultyRequest req = new CreateFacultyRequest("Faculté des Sciences", "FS", null, null);
		Faculty result = facultyService.create(req);

		assertEquals("Faculté des Sciences", result.getName());
		assertEquals("FS", result.getCode());
	}

	@Test
	void update_success() {
		Teacher dean = new Teacher();
		dean.setId(2L);
		when(teacherRepository.findById(2L)).thenReturn(Optional.of(dean));

		Faculty existing = createFaculty(1L, "Old", "O", null, null);
		when(facultyRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(facultyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		CreateFacultyRequest req = new CreateFacultyRequest("New", "N", 2L, "logo.png");
		Faculty result = facultyService.update(1L, req);

		assertEquals("New", result.getName());
		assertEquals("N", result.getCode());
		assertEquals(2L, result.getDeanId());
		assertEquals("logo.png", result.getLogoUrl());
	}

	@Test
	void update_notFound_throws() {
		when(facultyRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class,
				() -> facultyService.update(99L, new CreateFacultyRequest("N", "N", null, null)));
	}

	@Test
	void delete_success() {
		Faculty faculty = createFaculty(1L, "FS", "FS", null, null);
		when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));
		when(departmentRepository.findByFaculty_Id(1L)).thenReturn(List.of());

		facultyService.delete(1L);

		verify(facultyRepository).delete(faculty);
	}

	@Test
	void delete_notFound_throws() {
		when(facultyRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> facultyService.delete(99L));
		verify(facultyRepository, never()).delete(any());
	}

	@Test
	void delete_withDepartments_throws() {
		Faculty faculty = createFaculty(1L, "FS", "FS", null, null);
		when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));
		when(departmentRepository.findByFaculty_Id(1L)).thenReturn(List.of(new Department()));

		assertThrows(ResponseStatusException.class, () -> facultyService.delete(1L));
		verify(facultyRepository, never()).delete(any());
	}
}
