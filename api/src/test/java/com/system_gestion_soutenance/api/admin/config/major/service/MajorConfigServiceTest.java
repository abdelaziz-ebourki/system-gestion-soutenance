package com.system_gestion_soutenance.api.admin.config.major.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.major.dto.CreateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.dto.UpdateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.dto.MajorDto;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class MajorConfigServiceTest {

	@Mock
	private MajorRepository majorRepository;
	@Mock
	private StudentRepository studentRepository;
	@Mock
	private DepartmentRepository departmentRepository;
	@Mock
	private ConfigMapper configMapper;
	@InjectMocks
	private MajorConfigService majorConfigService;

	@Test
	void findAll_returnsAll() {
		when(majorRepository.findAll()).thenReturn(List.of(new Major()));
		assertEquals(1, majorConfigService.findAll().size());
	}

	@Test
	void create_success() {
		when(majorRepository.findByName("GL")).thenReturn(Optional.empty());
		when(majorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		Major result = majorConfigService.create(new CreateMajorRequest("GL", null));
		assertEquals("GL", result.getName());
	}

	@Test
	void create_duplicate_throws() {
		when(majorRepository.findByName("GL")).thenReturn(Optional.of(new Major()));
		assertThrows(InvalidBusinessStateException.class,
				() -> majorConfigService.create(new CreateMajorRequest("GL", null)));
	}

	@Test
	void update_success() {
		Major major = new Major();
		major.setId(1L);
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(majorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		Major result = majorConfigService.update(1L, new CreateMajorRequest("IIR", null));
		assertEquals("IIR", result.getName());
	}

	@Test
	void delete_withStudents_throws() {
		Major major = new Major();
		major.setId(1L);
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(studentRepository.findByMajorId(1L))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.user.entity.Student()));
		assertThrows(ResourceConflictException.class, () -> majorConfigService.delete(1L));
	}

	@Test
	void delete_success() {
		Major major = new Major();
		major.setId(1L);
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(studentRepository.findByMajorId(1L)).thenReturn(List.of());
		majorConfigService.delete(1L);
		verify(majorRepository).deleteById(1L);
	}

	@Test
	void findAll_withPagination_returnsEnrichedMajorDtos() {
		Major major = new Major();
		major.setId(1L);
		major.setName("GL");

		Page<Major> page = new PageImpl<>(List.of(major));
		when(majorRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
		when(configMapper.toMajorDto(major)).thenReturn(new MajorDto(1L, "GL", null, null, 0L));
		when(studentRepository.countByMajorId(1L)).thenReturn(5L);

		var result = majorConfigService.findAll(0, 10);

		assertEquals(1, result.items().size());
		assertEquals(5L, result.items().get(0).studentCount());
	}

	@Test
	void create_withDepartmentId_setsDepartment() {
		when(majorRepository.findByName("GL")).thenReturn(Optional.empty());
		Department dept = new Department();
		dept.setId(1L);
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(majorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Major result = majorConfigService.create(new CreateMajorRequest("GL", 1L));

		assertEquals("GL", result.getName());
		assertNotNull(result.getDepartment());
	}

	@Test
	void create_withInvalidDepartment_throwsException() {
		when(majorRepository.findByName("GL")).thenReturn(Optional.empty());
		when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(InvalidBusinessStateException.class,
				() -> majorConfigService.create(new CreateMajorRequest("GL", 99L)));
	}

	@Test
	void update_withDepartmentId_updatesDepartment() {
		Major major = new Major();
		major.setId(1L);
		Department dept = new Department();
		dept.setId(2L);

		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(departmentRepository.findById(2L)).thenReturn(Optional.of(dept));
		when(majorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Major result = majorConfigService.update(1L, new CreateMajorRequest("IIR", 2L));

		assertEquals("IIR", result.getName());
		assertNotNull(result.getDepartment());
	}

	@Test
	void update_withNullDepartmentId_clearsDepartment() {
		Major major = new Major();
		major.setId(1L);
		major.setDepartment(new Department());

		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(majorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Major result = majorConfigService.update(1L, new CreateMajorRequest("IIR", null));

		assertNull(result.getDepartment());
	}

	@Test
	void updatePartial_withName_updatesNameOnly() {
		Major major = new Major();
		major.setId(1L);

		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(majorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Major result = majorConfigService.updatePartial(1L, new UpdateMajorRequest("NewName", null));

		assertEquals("NewName", result.getName());
	}

	@Test
	void updatePartial_withDepartmentId_updatesDepartment() {
		Major major = new Major();
		major.setId(1L);
		Department dept = new Department();
		dept.setId(3L);

		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(departmentRepository.findById(3L)).thenReturn(Optional.of(dept));
		when(majorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Major result = majorConfigService.updatePartial(1L, new UpdateMajorRequest(null, 3L));

		assertNotNull(result.getDepartment());
	}

	@Test
	void updatePartial_withInvalidDepartment_throwsException() {
		Major major = new Major();
		major.setId(1L);

		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(InvalidBusinessStateException.class,
				() -> majorConfigService.updatePartial(1L, new UpdateMajorRequest(null, 99L)));
	}

	@Test
	void delete_notFound_throwsException() {
		when(majorRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> majorConfigService.delete(99L));
	}
}
