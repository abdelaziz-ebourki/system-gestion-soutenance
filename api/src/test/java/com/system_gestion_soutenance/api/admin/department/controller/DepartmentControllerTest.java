package com.system_gestion_soutenance.api.admin.department.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.service.DepartmentService;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DepartmentController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class DepartmentControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private DepartmentService departmentService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.ConfigMapper configMapper;

	@Test
	void findAll_returnsList() throws Exception {
		when(departmentService.findAll(0, 10))
				.thenReturn(new PaginatedResponse<>(List.of(new Department()), 1, 1, 0, 10));
		mockMvc.perform(get("/api/admin/departments")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.items").isArray());
	}

	@Test
	void create_returns201() throws Exception {
		when(departmentService.create(any())).thenReturn(new Department());
		mockMvc.perform(post("/api/admin/departments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Dept\",\"code\":\"D1\",\"facultyId\":1}")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void update_returns200() throws Exception {
		when(departmentService.update(anyLong(), any())).thenReturn(new Department());
		mockMvc.perform(put("/api/admin/departments/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Upd\",\"code\":\"UP\",\"facultyId\":1}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void delete_returns200() throws Exception {
		doNothing().when(departmentService).delete(1L);
		mockMvc.perform(delete("/api/admin/departments/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void patch_returns200() throws Exception {
		when(departmentService.updatePartial(anyLong(), any())).thenReturn(new Department());
		mockMvc.perform(patch("/api/admin/departments/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Upd\",\"code\":\"UP\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}
}
