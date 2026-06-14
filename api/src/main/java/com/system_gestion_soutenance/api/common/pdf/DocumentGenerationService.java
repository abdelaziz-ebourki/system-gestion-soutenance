package com.system_gestion_soutenance.api.common.pdf;

import com.lowagie.text.DocumentException;
import com.system_gestion_soutenance.api.common.exception.PdfGenerationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;
@SuppressWarnings("PMD")

@Service
public class DocumentGenerationService {

	private static final Logger LOG = LoggerFactory.getLogger(DocumentGenerationService.class);
	private static final String FONT = "classpath:/static/fonts/DejaVuSans.ttf";
	private static final String FONT_BOLD = "classpath:/static/fonts/DejaVuSans-Bold.ttf";

	private final SpringTemplateEngine templateEngine;
	private final ResourceLoader resourceLoader;

	public DocumentGenerationService(SpringTemplateEngine templateEngine, ResourceLoader resourceLoader) {
		this.templateEngine = templateEngine;
		this.resourceLoader = resourceLoader;
	}

	public byte[] generatePdf(String templateName, Map<String, Object> data) {
		Context context = new Context(Locale.FRENCH, data);
		String html = templateEngine.process("documents/" + templateName, context);

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ITextRenderer renderer = new ITextRenderer();
			renderer.getFontResolver().addFont(getFontPath(FONT), true);
			renderer.getFontResolver().addFont(getFontPath(FONT_BOLD), true);
			renderer.setDocumentFromString(html);
			renderer.layout();
			renderer.createPDF(baos);
			return baos.toByteArray();
		} catch (DocumentException | IOException e) {
			LOG.error("Failed to generate PDF for template: {}", templateName, e);
			throw new PdfGenerationException("Erreur lors de la generation du PDF", e);
		}
	}

	private String getFontPath(String classpathResource) throws IOException {
		Resource resource = resourceLoader.getResource(classpathResource);
		if (resource.isFile()) {
			return resource.getFile().getAbsolutePath();
		}
		Path tempFile = Files.createTempFile("font-", ".ttf");
		tempFile.toFile().deleteOnExit();
		try (InputStream is = resource.getInputStream(); OutputStream os = Files.newOutputStream(tempFile)) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = is.read(buffer)) != -1) {
				os.write(buffer, 0, read);
			}
		}
		return tempFile.toAbsolutePath().toString();
	}
}
