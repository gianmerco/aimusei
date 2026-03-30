package it.prismaprogetti.aimusei.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.leonardo.aiservice.content.ByteArrayImage;
import com.leonardo.aiservice.content.ImageContent;
import com.leonardo.aiservice.content.ImageMapContent;

@Service
public class PDFService {
	
	
	public byte[] generateDocument(ImageMapContent content) {
	    
	    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	        
	        // Ottieni le entry (chiave-valore) dalla mappa
	        List<Map.Entry<String, ImageContent>> entries = content.getValue();
	        
	        PdfWriter writer = new PdfWriter(baos);
	        PdfDocument pdfDoc = new PdfDocument(writer);
	        Document document = new Document(pdfDoc);
	        
	        // Imposta il formato A4
	        pdfDoc.setDefaultPageSize(PageSize.A4);
	        
	        // Dimensioni delle celle (A4 con margini)
	        float pageWidth = PageSize.A4.getWidth() - 72;
	        float pageHeight = PageSize.A4.getHeight() - 72;
	        
	        float cellWidth = pageWidth / 4;
	        float cellHeight = pageHeight / 8;
	        
	        int imagesPerPage = 32; // 8 righe × 4 colonne
	        int totalImages = entries.size();
	        int totalPages = (int) Math.ceil((double) totalImages / imagesPerPage);
	        
	        for (int page = 0; page < totalPages; page++) {
	            if (page > 0) {
	                document.add(new com.itextpdf.layout.element.AreaBreak());
	            }
	            
	            // Crea una nuova tabella per questa pagina
	            float[] columnWidths = {1f, 1f, 1f, 1f};
	            Table table = new Table(UnitValue.createPercentArray(columnWidths));
	            table.setWidth(UnitValue.createPercentValue(100));
	            
	            int startIndex = page * imagesPerPage;
	            int endIndex = Math.min(startIndex + imagesPerPage, totalImages);
	            
	            int cellsInCurrentRow = 0;
	            int cellsAddedOnPage = 0;
	            
	            for (int i = startIndex; i < endIndex; i++) {
	                Map.Entry<String, ImageContent> entry = entries.get(i);
	                
	                if ("NEWLINE".equals(entry.getKey())) {
	                    // Completa la riga corrente con celle vuote
	                    int remaining = (4 - cellsInCurrentRow % 4) % 4;
	                    for (int j = 0; j < remaining; j++) {
	                        table.addCell(createEmptyCell(cellWidth, cellHeight));
	                        cellsAddedOnPage++;
	                    }
	                    cellsInCurrentRow = 0;
	                } else {
	                    addImageToTable(table, entry.getKey(), entry.getValue(), cellWidth, cellHeight);
	                    cellsInCurrentRow++;
	                    cellsAddedOnPage++;
	                }
	            }
	            
	            // Completa l'ultima riga e la pagina con celle vuote
	            int remaining = imagesPerPage - cellsAddedOnPage;
	            for (int i = 0; i < remaining; i++) {
	                table.addCell(createEmptyCell(cellWidth, cellHeight));
	            }
	            
	            document.add(table);
	        }
	        
	        addLicensePage(document);
	        
	        document.close();
	        return baos.toByteArray();
	        
	    } catch (IOException e) {
	        throw new RuntimeException("Errore nella generazione del PDF", e);
	    }
	}
	
	private void addLicensePage(Document document) {
	    // Aggiungi un'interruzione di pagina per separare il contenuto
	    document.add(new AreaBreak());
	    
	    String licenseText = "I simboli pittografici utilizzati sono di proprietà del governo di Aragona e sono stati creati da Sergio Palao per ARASAAC (http://www.arasaac.org), che li distribuisce sotto Licenza Creative Commons BY-NC-SA.";
	    
	    Paragraph licenseParagraph = new Paragraph(licenseText)
	            .setFontSize(10)
	            .setTextAlignment(TextAlignment.CENTER)
	            .setMarginTop(20);
	    
	    document.add(licenseParagraph);
	}
	
	private void addImageToTable(Table table, String key, ImageContent imageContent, float cellWidth, float cellHeight) {
		try {
			// Crea l'immagine a partire dal byte array
			ImageData imageData = ImageDataFactory.create(((ByteArrayImage) imageContent).getValue());
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
			Paragraph caption = new Paragraph(key)
					.setFontSize(10)
					.setTextAlignment(TextAlignment.CENTER)
					.setMargin(0)
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
	
	private Cell createEmptyCell(float width, float height) {
		Cell cell = new Cell();
		cell.setWidth(width);
		cell.setHeight(height);
		cell.setPadding(0);
		cell.setMargin(0);
		cell.setBorder(null);
		return cell;
	}
	
}