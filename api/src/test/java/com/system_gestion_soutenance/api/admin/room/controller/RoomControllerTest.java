package com.system_gestion_soutenance.api.admin.room.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.admin.room.dto.RoomResponse;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.service.RoomService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.RoomMapper;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RoomController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class RoomControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private RoomService roomService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private RoomMapper roomMapper;

	@Test
	void findAll_returnsPaginated() throws Exception {
		when(roomService.findAll(0, 10)).thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 10));
		mockMvc.perform(get("/api/admin/rooms")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items").isArray());
	}

	@Test
	void create_returns201() throws Exception {
		when(roomService.create(any())).thenReturn(new Room());
		when(roomMapper.toDto(any())).thenReturn(new RoomResponse(1L, "Salle 1", 30, 1L));
		mockMvc.perform(post("/api/admin/rooms").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Salle 1\",\"capacity\":30,\"departmentId\":1}")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void bulkCreate_returns201() throws Exception {
		when(roomService.bulkCreate(any())).thenReturn(List.of());
		mockMvc.perform(post("/api/admin/rooms/bulk").contentType(MediaType.APPLICATION_JSON)
				.content("{\"rooms\":[{\"name\":\"S1\",\"capacity\":20,\"departmentId\":1}]}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void update_returns200() throws Exception {
		when(roomService.update(anyLong(), any())).thenReturn(new Room());
		when(roomMapper.toDto(any())).thenReturn(new RoomResponse(1L, "Upd", 25, 1L));
		mockMvc.perform(put("/api/admin/rooms/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Upd\",\"capacity\":25,\"departmentId\":1}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void delete_returns200() throws Exception {
		doNothing().when(roomService).delete(1L);
		mockMvc.perform(delete("/api/admin/rooms/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void patch_returns200() throws Exception {
		when(roomService.updatePartial(anyLong(), any())).thenReturn(new Room());
		when(roomMapper.toDto(any())).thenReturn(new RoomResponse(1L, "Upd", 25, 1L));
		mockMvc.perform(
				patch("/api/admin/rooms/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Upd\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
	}
}
