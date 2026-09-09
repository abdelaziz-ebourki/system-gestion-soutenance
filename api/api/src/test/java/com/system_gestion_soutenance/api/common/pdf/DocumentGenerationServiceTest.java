package com.system_gestion_soutenance.api.common.pdf;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.common.exception.PdfGenerationException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class DocumentGenerationServiceTest {

	@Mock
	private SpringTemplateEngine templateEngine;

	@Mock
	private ResourceLoader resourceLoader;

	@Test
	void generatePdf_whenTemplateEngineThrows_throwsOriginalException() {
		when(templateEngine.process(eq("documents/bad"), any(Context.class)))
				.thenThrow(new RuntimeException("Template error"));

		DocumentGenerationService service = new DocumentGenerationService(templateEngine, resourceLoader);

		assertThrows(RuntimeException.class, () -> service.generatePdf("bad", Map.of()));
	}
}
