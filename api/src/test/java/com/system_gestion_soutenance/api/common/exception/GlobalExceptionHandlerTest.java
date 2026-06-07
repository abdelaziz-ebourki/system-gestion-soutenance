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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
				.setControllerAdvice(new GlobalExceptionHandler()).build();
	}

	@Test
	void handleEntityNotFoundException() throws Exception {
		mockMvc.perform(get("/test/entity-not-found")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Not found"));
	}

	@Test
	void handleInvalidBusinessStateException() throws Exception {
		mockMvc.perform(get("/test/invalid-business-state")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Bad request"));
	}

	@Test
	void handleValidationException() throws Exception {
		mockMvc.perform(post("/test/validate").contentType(MediaType.APPLICATION_JSON).content("{\"email\": \"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]").value("email: must not be blank"));
	}

	@Test
	void handleInvalidBusinessStateException_withNullMessage_returnsDefaultMessage() throws Exception {
		mockMvc.perform(get("/test/invalid-business-state/null-message")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Erreur"));
	}

	@Test
	void handleGeneralException_returns500() throws Exception {
		mockMvc.perform(get("/test/unhandled")).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("Une erreur interne est survenue."));
	}

	@RestController
	static class TestController {

		@GetMapping("/test/entity-not-found")
		ResponseEntity<Map<String, String>> throwNotFound() {
			throw new EntityNotFoundException("Not found");
		}

		@GetMapping("/test/invalid-business-state")
		ResponseEntity<Map<String, String>> throwBadRequest() {
			throw new InvalidBusinessStateException("Bad request");
		}

		@GetMapping("/test/invalid-business-state/null-message")
		ResponseEntity<Map<String, String>> throwNullMessage() {
			throw new InvalidBusinessStateException(null);
		}

		@GetMapping("/test/unhandled")
		ResponseEntity<Map<String, String>> throwUnhandled() {
			throw new RuntimeException("boom");
		}

		@PostMapping("/test/validate")
		ResponseEntity<Void> validate(@Valid @RequestBody TestRequest request) {
			return ResponseEntity.ok().build();
		}

		record TestRequest(@NotBlank @Email String email) {
		}
	}
}
