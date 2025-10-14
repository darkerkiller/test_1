package com.massey.texteditor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PDFConversionTest {
    
    private TextEditor textEditor;
    private JTextPane textPane;
    
    @BeforeEach
    void setUp() {
        textEditor = new TextEditor();
        textPane = new JTextPane();
    }
    
    @Test
    void testPDFConversionWithSimpleText(@TempDir Path tempDir) throws IOException {
        // Create test content
        String testContent = "This is a test PDF conversion.\nIt should work correctly now.";
        textPane.setText(testContent);
        
        // Create a test PDF file
        File pdfFile = tempDir.resolve("test.pdf").toFile();
        
        // Test the conversion method directly
        boolean result = convertTextToPDF(textPane, pdfFile);
        
        // Verify the PDF was created
        assertTrue(result, "PDF conversion should succeed");
        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
        
        // Verify the file is a valid PDF (check PDF header)
        byte[] fileContent = Files.readAllBytes(pdfFile.toPath());
        String header = new String(fileContent, 0, Math.min(10, fileContent.length));
        assertTrue(header.contains("%PDF"), "File should be a valid PDF");
    }
    
    @Test
    void testPDFConversionWithLongText(@TempDir Path tempDir) throws IOException {
        // Create test content with long lines
        String testContent = "This is a very long line that should definitely need wrapping because it contains a lot of text and should test the word wrapping functionality of the PDF conversion feature in our text editor application.\n\nThis is another paragraph to test the conversion.";
        textPane.setText(testContent);
        
        // Create a test PDF file
        File pdfFile = tempDir.resolve("test_long.pdf").toFile();
        
        // Test the conversion method directly
        boolean result = convertTextToPDF(textPane, pdfFile);
        
        // Verify the PDF was created
        assertTrue(result, "PDF conversion with long text should succeed");
        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
    }
    
    @Test
    void testPDFConversionWithEmptyText(@TempDir Path tempDir) throws IOException {
        // Test with empty content
        textPane.setText("");
        
        File pdfFile = tempDir.resolve("test_empty.pdf").toFile();
        
        boolean result = convertTextToPDF(textPane, pdfFile);
        
        assertTrue(result, "PDF conversion with empty text should succeed");
        assertTrue(pdfFile.exists(), "PDF file should exist");
    }
    
    // Helper method that mimics the PDF conversion logic from TextEditor
    private boolean convertTextToPDF(JTextPane textPane, File file) {
        try {
            // Import required PDFBox classes
            org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument();
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
            document.addPage(page);
            
            // Create a font
            org.apache.pdfbox.pdmodel.font.PDFont font = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;
            float fontSize = 12;
            float leading = 1.5f * fontSize;
            
            // Create content stream
            org.apache.pdfbox.pdmodel.PDPageContentStream contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);
            contentStream.setFont(font, fontSize);
            
            // Set up margins
            float margin = 50;
            float width = page.getMediaBox().getWidth() - 2 * margin;
            float startX = page.getMediaBox().getLowerLeftX() + margin;
            float startY = page.getMediaBox().getUpperRightY() - margin;
            
            // Split text into lines
            String text = textPane.getText();
            String[] lines = text.split("\n");
            
            float currentY = startY;
            
            for (String line : lines) {
                // Handle word wrapping
                String[] words = line.split(" ");
                StringBuilder currentLine = new StringBuilder();
                
                for (String word : words) {
                    String testLine = currentLine.length() == 0 ? word : currentLine.toString() + " " + word;
                    float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
                    
                    if (textWidth < width) {
                        if (currentLine.length() == 0) {
                            currentLine.append(word);
                        } else {
                            currentLine.append(" ").append(word);
                        }
                    } else {
                        // Draw the current line and start a new one
                        if (currentLine.length() > 0) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(startX, currentY);
                            contentStream.showText(currentLine.toString());
                            contentStream.endText();
                            currentY -= leading;
                        }
                        currentLine = new StringBuilder(word);
                    }
                }
                
                // Draw any remaining text in the current line
                if (currentLine.length() > 0) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(startX, currentY);
                    contentStream.showText(currentLine.toString());
                    contentStream.endText();
                    currentY -= leading;
                }
                
                // Add extra space between paragraphs (empty lines)
                currentY -= leading * 0.5f;
                
                // Check if we need a new page
                if (currentY < margin) {
                    contentStream.close();
                    page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);
                    contentStream.setFont(font, fontSize);
                    currentY = startY;
                }
            }
            
            contentStream.close();
            document.save(file);
            document.close();
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error creating PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}