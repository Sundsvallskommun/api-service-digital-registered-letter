package se.sundsvall.digitalregisteredletter.service.util;

import generated.se.sundsvall.templating.RenderResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import org.apache.commons.lang3.function.Failable;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.openpdf.text.Document;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfSmartCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.pdfbox.io.RandomAccessReadBuffer.createBufferFromStream;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public final class InvoicePdfMerger {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoicePdfMerger.class);
	private static final String MERGE_ERROR_MESSAGE = "A problem occurred during merge of PDF:s. %s.";

	private InvoicePdfMerger() {}

	public static OutputStream mergePdfs(final List<AttachmentEntity> attachments, final RenderResponse response) {

		try {
			final var merger = initializePDFMergerUtility();

			final var receipt = new ByteArrayInputStream(Base64.getDecoder().decode(response.getOutput()));

			merger.addSource(createBufferFromStream(receipt));

			Failable.stream(toInputStreams(attachments))
				.forEach(inputStream -> merger.addSource(createBufferFromStream(inputStream)));

			merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());

			return ofNullable(compress((ByteArrayOutputStream) merger.getDestinationStream()))
				.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "No content available for receipt"));
		} catch (final Exception e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, MERGE_ERROR_MESSAGE.formatted(e.getMessage()));
		}
	}

	private static PDFMergerUtility initializePDFMergerUtility() {
		final var merger = new PDFMergerUtility();
		merger.setDestinationStream(new ByteArrayOutputStream());
		return merger;
	}

	private static List<InputStream> toInputStreams(final List<AttachmentEntity> attachments) {
		return ofNullable(attachments).orElse(emptyList())
			.stream()
			.map(attachmentEntity -> {
				try {
					return attachmentEntity.getContent().getBinaryStream();
				} catch (final SQLException e) {
					throw new IllegalStateException("Unable to open content stream for attachment with id '%s'".formatted(attachmentEntity.getId()), e);
				}
			})
			.toList();
	}

	private static ByteArrayOutputStream compress(final ByteArrayOutputStream outputStream) {

		try (final var pdfReader = new PdfReader(outputStream.toByteArray());
			final var document = new Document()) {

			final var result = new ByteArrayOutputStream();
			final var pdfSmartCopy = new PdfSmartCopy(document, result);
			pdfSmartCopy.setFullCompression();
			document.open();

			for (int pageNumber = 1; pageNumber <= pdfReader.getNumberOfPages(); pageNumber++) {
				final var page = pdfSmartCopy.getImportedPage(pdfReader, pageNumber);
				pdfSmartCopy.addPage(page);
			}
			pdfSmartCopy.close();
			return result;

		} catch (final IOException e) {
			LOGGER.warn("A problem occurred during compression of PDF. {}", e.getMessage());
			return outputStream;
		}
	}

}
