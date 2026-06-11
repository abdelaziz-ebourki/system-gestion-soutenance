package com.system_gestion_soutenance.api.common.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

class PdfGenerationServiceTest {

	private final TemplateEngine templateEngine = org.mockito.Mockito.mock(TemplateEngine.class);
	private final PdfGenerationService service = new PdfGenerationService(templateEngine);

	@Test
	void generatePdf_withValidHtml_returnsPdfBytes() {
		when(templateEngine.process(eq("documents/test"), org.mockito.ArgumentMatchers.any(Context.class)))
				.thenReturn("""
						<!DOCTYPE html>
						<html>
						<head><style>@page{size:A4;margin:20mm;}</style></head>
						<body><h1>Test</h1></body>
						</html>""");

		byte[] result = service.generatePdf("test", Map.of());

		assertTrue(result.length > 0);
		assertArrayEquals(new byte[]{0x25, 0x50, 0x44, 0x46}, java.util.Arrays.copyOf(result, 4));
	}

	@Test
	void generatePdf_withEmptyBody_returnsPdfBytes() {
		when(templateEngine.process(eq("documents/empty"), org.mockito.ArgumentMatchers.any(Context.class)))
				.thenReturn("""
						<!DOCTYPE html>
						<html>
						<head><style>@page{size:A4;margin:20mm;}</style></head>
						<body></body>
						</html>""");

		byte[] result = service.generatePdf("empty", Map.of());

		assertTrue(result.length > 0);
	}

	@Test
	void generatePdf_preservesTemplateNameInContext() {
		when(templateEngine.process(eq("documents/student-convocation"),
				org.mockito.ArgumentMatchers.any(Context.class))).thenReturn("""
						<!DOCTYPE html>
						<html>
						<head><style>@page{size:A4;margin:20mm;}</style></head>
						<body><p>Convocation</p></body>
						</html>""");

		byte[] result = service.generatePdf("student-convocation", Map.of("studentName", "Alice"));

		assertTrue(result.length > 0);
	}
}
