package com.system_gestion_soutenance.api.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
				.setControllerAdvice(new GlobalExceptionHandler()).build();
	}

	@Test
	void handleResponseStatusException() throws Exception {
		mockMvc.perform(get("/test/response-status")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Not found"));
	}

	@Test
	void handleResponseStatusException_withDifferentStatus() throws Exception {
		mockMvc.perform(get("/test/response-status/bad-request")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Bad request"));
	}

	@Test
	void handleValidationException() throws Exception {
		mockMvc.perform(post("/test/validate").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.email").value("must not be blank"));
	}

	@RestController
	static class TestController {

		@GetMapping("/test/response-status")
		ResponseEntity<Map<String, String>> throwNotFound() {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		@GetMapping("/test/response-status/bad-request")
		ResponseEntity<Map<String, String>> throwBadRequest() {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
		}

		@PostMapping("/test/validate")
		ResponseEntity<Void> validate(@Valid @RequestBody TestRequest request) {
			return ResponseEntity.ok().build();
		}

		record TestRequest(@NotBlank @Email String email) {
		}
	}
}
