package com.system_gestion_soutenance.api.integration;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void loginAndAccessProtectedResource_success() {
		// 1. Login
		Map<String, String> loginRequest = Map.of("email", "admin@univh2c.ma", "password", "1234");

		ResponseEntity<Map<String, Object>> loginResponse = restTemplate.exchange("/api/login", HttpMethod.POST,
				new HttpEntity<>(loginRequest), new ParameterizedTypeReference<Map<String, Object>>() {
				});

		assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
		String token = (String) loginResponse.getBody().get("token");
		assertNotNull(token);

		// 2. Access protected resource with token
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		ResponseEntity<String> protectedResponse = restTemplate.exchange("/api/admin/stats", HttpMethod.GET, entity,
				String.class);

		assertEquals(HttpStatus.OK, protectedResponse.getStatusCode());
	}
}
