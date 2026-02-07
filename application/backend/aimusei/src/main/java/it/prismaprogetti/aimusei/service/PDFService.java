package it.prismaprogetti.aimusei.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.leonardo.aiservice.content.ImageListContent;

@Service
public class PDFService {
	
	@Value("${images:}")
	private String[] imagesArray;
	
	public byte[] generateDocument(ImageListContent content) {
		// TODO
		return null;
	}

    public byte[] generateImagesMock() {
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        	
			List<String> images = List.of(imagesArray);
        	
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
            
            int imagesPerPage = 32; // 8 row × 4 column
            int totalImages = images.size();
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
                
                // Aggiungi esattamente 32 celle per pagina
                for (int i = startIndex; i < startIndex + imagesPerPage; i++) {
                    if (i < endIndex) {
                        // Cella con immagine
                        addImageToTable(table, images.get(i), cellWidth, cellHeight);
                    } else {
                        // Cella vuota per completare la pagina
                        Cell emptyCell = createEmptyCell(cellWidth, cellHeight);
                        table.addCell(emptyCell);
                    }
                }
                
                document.add(table);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Errore nella generazione del PDF", e);
        }
    }
    
    private void addImageToTable(Table table, String base64Image, float cellWidth, float cellHeight) {
        try {
            // Rimuove l'header base64 se presente
            String imageDataString = base64Image;
            if (base64Image.contains(",")) {
                imageDataString = base64Image.split(",")[1];
            }
            
            // Crea l'immagine dal base64
            ImageData imageData = ImageDataFactory.create(
                java.util.Base64.getDecoder().decode(imageDataString)
            );
            Image image = new Image(imageData);
            
            // Ridimensiona l'immagine per riempire completamente la cella
            image.setAutoScale(true);
            image.setWidth(cellWidth);
            image.setHeight(cellHeight);
            
            // Crea la cella e aggiungi l'immagine
            Cell cell = createCellWithContent(image, cellWidth, cellHeight);
            table.addCell(cell);
            
        } catch (Exception e) {
            // Se c'è un errore con l'immagine, aggiungi una cella vuota
            Cell emptyCell = createEmptyCell(cellWidth, cellHeight);
            table.addCell(emptyCell);
        }
    }
    
    private Cell createCellWithContent(Image image, float width, float height) {
        Cell cell = new Cell();
        cell.setWidth(width);
        cell.setHeight(height);
        cell.setPadding(0);
        cell.setMargin(0);
        cell.setBorder(null); // Rimuove i bordi per un look più pulito
        cell.add(image);
        return cell;
    }
    
    private Cell createEmptyCell(float width, float height) {
        Cell cell = new Cell();
        cell.setWidth(width);
        cell.setHeight(height);
        cell.setPadding(0);
        cell.setMargin(0);
        cell.setBorder(null); // Rimuove i bordi
        return cell;
    }

}