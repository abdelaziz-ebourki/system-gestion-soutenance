package com.system_gestion_soutenance.api.admin.department.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.department.dto.CreateDepartmentRequest;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.repository.FacultyRepository;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
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
class DepartmentServiceTest {

	@Mock
	private DepartmentRepository departmentRepository;
	@Mock
	private TeacherRepository teacherRepository;
	@Mock
	private RoomRepository roomRepository;
	@Mock
	private FacultyRepository facultyRepository;

	@InjectMocks
	private DepartmentService departmentService;

	@Test
	void findAll_returnsAll() {
		when(departmentRepository.findAll()).thenReturn(List.of(new Department()));
		assertEquals(1, departmentService.findAll().size());
	}

	@Test
	void findById_existing_returnsDepartment() {
		Department dept = new Department();
		dept.setId(1L);
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		assertEquals(1L, departmentService.findById(1L).getId());
	}

	@Test
	void findById_missing_throws() {
		when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> departmentService.findById(99L));
	}

	@Test
	void create_success() {
		Faculty faculty = new Faculty();
		faculty.setId(1L);
		when(departmentRepository.findByName("New Dept")).thenReturn(Optional.empty());
		when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));
		when(departmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		CreateDepartmentRequest req = new CreateDepartmentRequest("New Dept", "ND", null, 1L);
		Department result = departmentService.create(req);

		assertEquals("New Dept", result.getName());
	}

	@Test
	void create_duplicateName_throws() {
		when(departmentRepository.findByName("Dup")).thenReturn(Optional.of(new Department()));
		assertThrows(ResponseStatusException.class,
				() -> departmentService.create(new CreateDepartmentRequest("Dup", "D", null, 1L)));
	}

	@Test
	void create_withHead_success() {
		Faculty faculty = new Faculty();
		faculty.setId(1L);
		Teacher head = new Teacher();
		head.setId(1L);
		when(departmentRepository.findByName("Dept")).thenReturn(Optional.empty());
		when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));
		when(teacherRepository.findById(1L)).thenReturn(Optional.of(head));
		when(departmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		CreateDepartmentRequest req = new CreateDepartmentRequest("Dept", "D", 1L, 1L);
		Department result = departmentService.create(req);

		assertNotNull(result.getHead());
	}

	@Test
	void delete_withTeachers_throws() {
		Department dept = new Department();
		dept.setId(1L);
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(teacherRepository.findByDepartmentId(1L)).thenReturn(List.of(new Teacher()));

		assertThrows(ResponseStatusException.class, () -> departmentService.delete(1L));
		verify(departmentRepository, never()).delete(any());
	}
}
