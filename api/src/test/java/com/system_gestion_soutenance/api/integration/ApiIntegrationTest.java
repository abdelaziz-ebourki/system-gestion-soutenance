package com.system_gestion_soutenance.api.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {

	@LocalServerPort
	private int port;

	private RestClient client() {
		return RestClient.builder().baseUrl("http://localhost:" + port).build();
	}

	@Test
	void loginAndAccessProtectedResource_success() {
		Map<String, String> loginRequest = Map.of("email", "admin@univh2c.ma", "password", "1234");

		ResponseEntity<Map<String, Object>> loginResponse = client().post().uri("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON).body(loginRequest).retrieve()
				.toEntity(new ParameterizedTypeReference<Map<String, Object>>() {
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

		String finalJwt = jwt;
		ResponseEntity<String> protectedResponse = client().get().uri("/api/admin/stats")
				.cookies(cookiesMap -> cookiesMap.add("jwt_token", finalJwt)).retrieve().toEntity(String.class);

		assertEquals(HttpStatus.OK, protectedResponse.getStatusCode());
	}
}
