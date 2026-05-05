package it.prismaprogetti.aimusei.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

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
import com.itextpdf.kernel.pdf.WriterProperties;
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

			  WriterProperties writerProperties = new WriterProperties()
		                .setFullCompressionMode(true)
		                .setCompressionLevel(6)
		                .useSmartMode();
			  
			  
			PdfWriter writer = new PdfWriter(baos,writerProperties);
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
	    	
	        // ✅ Ridimensiona i byte PRIMA di creare ImageData
	        byte[] resizedBytes = resizeImage(imageContent, (int) cellWidth, (int) (cellHeight - 15f));

	        ImageData imageData = ImageDataFactory.create(resizedBytes);
	        Image image = new Image(imageData);

	        image.scaleToFit(cellWidth, cellHeight - 15f);

	        Paragraph imageParagraph = new Paragraph();
	        imageParagraph.add(image);
	        imageParagraph.setTextAlignment(TextAlignment.CENTER);
	        imageParagraph.setMargin(0);
	        imageParagraph.setPadding(0);

	        Paragraph caption = new Paragraph(key)
	                .setFontSize(10)
	                .setTextAlignment(TextAlignment.CENTER)
	                .setMargin(0)
	                .setPadding(0);

	        Cell cell = new Cell();
	        cell.setWidth(cellWidth);
	        cell.setHeight(cellHeight);
	        cell.setPadding(0);
	        cell.setMargin(0);
	        cell.setBorder(null);

	        cell.add(imageParagraph);
	        cell.add(caption);
	        table.addCell(cell);

	    } catch (Exception e) {
	        table.addCell(createEmptyCell(cellWidth, cellHeight));
	    }
	}

	/**
	 * Ridimensiona l'immagine in memoria prima di inserirla nel PDF.
	 * maxWidth e maxHeight sono in punti PDF (1 punto ≈ 0.352 mm).
	 * Moltiplichiamo per un factor per avere abbastanza risoluzione ma non troppa.
	 */
	private byte[] resizeImage(ImageContent imageContent, int maxWidthPt, int maxHeightPt) throws Exception {
	    // ✅ Estrai i byte grezzi direttamente dall'ImageContent
	    byte[] originalBytes;
	    if (imageContent instanceof ByteArrayImage bai) {
	        originalBytes = bai.getValue();
	    } else if (imageContent instanceof Base64Image b64i) {
	        originalBytes = Base64.getDecoder().decode(b64i.getValue());
	    } else {
	        throw new IllegalArgumentException("Tipo di immagine non supportato: " + imageContent.getClass().getName());
	    }

	    ByteArrayInputStream bais = new ByteArrayInputStream(originalBytes);
	    BufferedImage original = ImageIO.read(bais);

	    if (original == null) return originalBytes; // fallback

	    float dpi = 150f;
	    int maxWidthPx  = (int) (maxWidthPt  / 72f * dpi);
	    int maxHeightPx = (int) (maxHeightPt / 72f * dpi);

	    int origW = original.getWidth();
	    int origH = original.getHeight();

	    // Se l'immagine è già abbastanza piccola, non ridimensionare
	    if (origW <= maxWidthPx && origH <= maxHeightPx) {
	        return originalBytes;
	    }

	    float scale = Math.min((float) maxWidthPx / origW, (float) maxHeightPx / origH);
	    int newW = Math.max(1, (int) (origW * scale));
	    int newH = Math.max(1, (int) (origH * scale));

	    BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2d = resized.createGraphics();
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.drawImage(original, 0, 0, newW, newH, null);
	    g2d.dispose();

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
	    ImageWriteParam param = writer.getDefaultWriteParam();
	    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	    param.setCompressionQuality(0.85f);
	    writer.setOutput(ImageIO.createImageOutputStream(baos));
	    writer.write(null, new IIOImage(resized, null, null), param);
	    writer.dispose();

	    return baos.toByteArray();
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