package it.prismaprogetti.aimusei.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.leonardo.aiservice.content.Base64Image;
import com.leonardo.aiservice.content.ByteArrayImage;
import com.leonardo.aiservice.content.ImageContent;
import com.leonardo.aiservice.content.ImageMapContent;

@Service
public class PDFService {

	public byte[] generateDocument(ImageMapContent content) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

			List<Map.Entry<String, ImageContent>> entries = content.getValue();

			PdfWriter writer = new PdfWriter(baos);
			PdfDocument pdfDoc = new PdfDocument(writer);
			pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE,
					new FirstPageHeaderHandler("Testo generato con linee guida ARASAAC"));
			Document document = new Document(pdfDoc);
			pdfDoc.setDefaultPageSize(PageSize.A4);

			float pageWidth = PageSize.A4.getWidth() - 72;
			float pageHeight = PageSize.A4.getHeight() - 72;
			float cellWidth = pageWidth / 4;
			float cellHeight = pageHeight / 8;

			final int COLUMNS = 4;
			final int CELLS_PER_PAGE = 32; // 8 righe × 4 colonne

			boolean isFirstPage = true;
			Table currentTable = null;
			int cellsOnPage = 0;
			int currentCol = 0; // posizione colonna corrente (0-3)

			for (Map.Entry<String, ImageContent> entry : entries) {

				if ("NEWLINE".equals(entry.getKey())) {
					// Se siamo già a inizio riga non c'è niente da paddare
					if (currentCol == 0 || currentTable == null)
						continue;

					// Completa la riga corrente con celle vuote
					int padding = COLUMNS - currentCol;
					for (int j = 0; j < padding; j++) {
						currentTable.addCell(createEmptyCell(cellWidth, cellHeight));
						cellsOnPage++;
					}
					currentCol = 0;

					// Se dopo il padding la pagina è piena, la chiudiamo
					if (cellsOnPage >= CELLS_PER_PAGE) {
						document.add(currentTable);
						currentTable = null;
						cellsOnPage = 0;
					}

				} else {
					// Se non c'è una tabella attiva, apriamo una nuova pagina
					if (currentTable == null) {
						if (!isFirstPage) {
							document.add(new AreaBreak());
						}
						isFirstPage = false;
						currentTable = createTable();
						cellsOnPage = 0;
						currentCol = 0;
					}

					addImageToTable(currentTable, entry.getKey(), entry.getValue(), cellWidth, cellHeight);
					cellsOnPage++;
					currentCol = (currentCol + 1) % COLUMNS;

					// Pagina piena → la chiudiamo (la prossima entry aprirà la successiva)
					if (cellsOnPage >= CELLS_PER_PAGE) {
						document.add(currentTable);
						currentTable = null;
					}
				}
			}

			// Flush dell'ultima pagina (se non ancora svuotata)
			if (currentTable != null) {
				int remaining = CELLS_PER_PAGE - cellsOnPage;
				for (int i = 0; i < remaining; i++) {
					currentTable.addCell(createEmptyCell(cellWidth, cellHeight));
				}
				document.add(currentTable);
			}

			addLicensePage(document);
			document.close();
			return baos.toByteArray();

		} catch (IOException e) {
			throw new RuntimeException("Errore nella generazione del PDF", e);
		}
	}

//Helper estratto per non duplicare il codice
	private Table createTable() {
		Table table = new Table(UnitValue.createPercentArray(new float[] { 1f, 1f, 1f, 1f }));
		table.setWidth(UnitValue.createPercentValue(100));
		return table;
	}

	private void addLicensePage(Document document) {
		// Aggiungi un'interruzione di pagina per separare il contenuto
		document.add(new AreaBreak());

		String licenseText = "I simboli pittografici utilizzati sono di proprietà del governo di Aragona e sono stati creati da Sergio Palao per ARASAAC (http://www.arasaac.org), che li distribuisce sotto Licenza Creative Commons BY-NC-SA.";

		Paragraph licenseParagraph = new Paragraph(licenseText).setFontSize(10).setTextAlignment(TextAlignment.CENTER)
				.setMarginTop(20);

		document.add(licenseParagraph);
	}

	private void addImageToTable(Table table, String key, ImageContent imageContent, float cellWidth,
			float cellHeight) {
		try {
			ImageData imageData = extractImageData(imageContent);

			Image image = new Image(imageData);

			// Dimensioni originali dell'immagine (in punti, assumendo 1 pixel = 1 punto)
			float originalWidth = image.getImageWidth();
			float originalHeight = image.getImageHeight();

			// Altezza riservata per la didascalia (testo)
			float textHeight = 15f; // sufficiente per una riga con font 10
			float availableImageHeight = cellHeight - textHeight;

			// Calcola il fattore di scala per adattare l'immagine allo spazio disponibile
			float scale = Math.min(cellWidth / originalWidth, availableImageHeight / originalHeight);
			float newWidth = originalWidth * scale;
			float newHeight = originalHeight * scale;

			// Applica le nuove dimensioni
			image.setWidth(newWidth);
			image.setHeight(newHeight);

			// Crea un paragrafo per centrare l'immagine orizzontalmente
			Paragraph imageParagraph = new Paragraph();
			imageParagraph.add(image);
			imageParagraph.setTextAlignment(TextAlignment.CENTER);
			imageParagraph.setMargin(0);
			imageParagraph.setPadding(0);

			// Crea la didascalia con la chiave
			Paragraph caption = new Paragraph(key).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMargin(0)
					.setPadding(0);

			// Crea la cella con le dimensioni fisse
			Cell cell = new Cell();
			cell.setWidth(cellWidth);
			cell.setHeight(cellHeight);
			cell.setPadding(0);
			cell.setMargin(0);
			cell.setBorder(null);

			// Aggiunge prima l'immagine centrata, poi la didascalia
			cell.add(imageParagraph);
			cell.add(caption);

			table.addCell(cell);

		} catch (Exception e) {
			// In caso di errore, cella vuota
			Cell emptyCell = createEmptyCell(cellWidth, cellHeight);
			table.addCell(emptyCell);
		}
	}

	private ImageData extractImageData(ImageContent imageContent) {
		if (imageContent instanceof ByteArrayImage bai) {
			return ImageDataFactory.create(bai.getValue());
		}
		if (imageContent instanceof Base64Image b64i) {
			return ImageDataFactory.create(Base64.getDecoder().decode(b64i.getValue()));
		}
		throw new IllegalArgumentException("Tipo di immagine non supportato: " + imageContent.getClass().getName());
	}

	private Cell createEmptyCell(float width, float height) {
		Cell cell = new Cell();
		cell.setWidth(width);
		cell.setHeight(height);
		cell.setPadding(0);
		cell.setMargin(0);
		cell.setBorder(null);
		return cell;
	}

	private static class FirstPageHeaderHandler implements IEventHandler {
		private final String headerText;

		public FirstPageHeaderHandler(String headerText) {
			this.headerText = headerText;
		}

		@Override
		public void handleEvent(Event event) {
			PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
			PdfDocument pdfDoc = docEvent.getDocument();
			PdfPage page = docEvent.getPage();

			// Applica solo alla prima pagina
			if (pdfDoc.getPageNumber(page) != 1) {
				return;
			}

			Rectangle pageSize = page.getPageSize();
			PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

			// Crea un canvas per il layout (usa le coordinate assolute)
			try (Canvas canvas = new Canvas(pdfCanvas, pageSize)) {
				Paragraph header = new Paragraph(headerText).setFontSize(7)
						.setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLACK);

				// Posiziona in alto al centro
				canvas.showTextAligned(header, pageSize.getWidth() / 2, // x = centro orizzontale
						pageSize.getTop() - 15, // y = 30 punti dal bordo superiore
						TextAlignment.CENTER, VerticalAlignment.TOP);
			}
		}

	}
}