package com.system_gestion_soutenance.api.integration;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void loginAndAccessProtectedResource_success() {
		Map<String, String> loginRequest = Map.of("email", "admin@univh2c.ma", "password", "1234");

		ResponseEntity<Map<String, Object>> loginResponse = restTemplate.exchange("/api/auth/login", HttpMethod.POST,
				new HttpEntity<>(loginRequest), new ParameterizedTypeReference<Map<String, Object>>() {
				});

		assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
		assertNotNull(loginResponse.getBody().get("user"));

		List<String> cookies = loginResponse.getHeaders().getOrDefault(HttpHeaders.SET_COOKIE, List.of());
		String jwt = null;
		for (String cookie : cookies) {
			if (cookie.contains("jwt_token=")) {
				jwt = cookie.split("jwt_token=")[1].split(";")[0];
				break;
			}
		}
		assertNotNull(jwt, "JWT cookie not found in Set-Cookie headers: " + cookies);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Cookie", "jwt_token=" + jwt);
		ResponseEntity<String> protectedResponse = restTemplate.exchange("/api/admin/stats", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);

		assertEquals(HttpStatus.OK, protectedResponse.getStatusCode());
	}
}
