package com.system_gestion_soutenance.api.common.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.system_gestion_soutenance.api.common.exception.PdfGenerationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class PdfGenerationService {

	private static final String FONT = "static/fonts/DejaVuSans.ttf";
	private static final String FONT_BOLD = "static/fonts/DejaVuSans-Bold.ttf";

	private final TemplateEngine templateEngine;
	private final File fontFile;
	private final File fontBoldFile;

	public PdfGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
		try {
			this.fontFile = extractFont(FONT);
			this.fontBoldFile = extractFont(FONT_BOLD);
		} catch (IOException e) {
			throw new PdfGenerationException("Failed to load PDF fonts", e);
		}
	}

	public byte[] generatePdf(String templateName, Map<String, Object> data) {
		Context context = new Context();
		context.setVariables(data);

		String htmlContent = templateEngine.process("documents/" + templateName, context);

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withHtmlContent(htmlContent, "/");
			builder.useFont(fontFile, "DejaVu Sans");
			builder.useFont(fontBoldFile, "DejaVu Sans", 700, null, true);
			builder.toStream(outputStream);
			builder.run();
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new PdfGenerationException("Échec de la génération du document PDF");
		}
	}

	private File extractFont(String classpath) throws IOException {
		ClassPathResource resource = new ClassPathResource(classpath);
		File tempFile = Files.createTempFile("font-", ".ttf").toFile();
		tempFile.deleteOnExit();
		try (InputStream is = resource.getInputStream()) {
			Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return tempFile;
	}
}
