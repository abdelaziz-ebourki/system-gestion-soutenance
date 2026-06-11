package com.system_gestion_soutenance.api.common.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.system_gestion_soutenance.api.common.exception.PdfGenerationException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class PdfGenerationService {

	private final TemplateEngine templateEngine;

	public PdfGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public byte[] generatePdf(String templateName, Map<String, Object> data) {
		Context context = new Context();
		context.setVariables(data);

		String htmlContent = templateEngine.process("documents/" + templateName, context);

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withHtmlContent(htmlContent, "/");
			builder.toStream(outputStream);
			builder.run();
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new PdfGenerationException("Échec de la génération du document PDF");
		}
	}
}
