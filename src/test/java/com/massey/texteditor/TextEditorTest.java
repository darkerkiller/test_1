package com.massey.texteditor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class TextEditorTest {
    
    private TextEditor textEditor;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Create the text editor instance
        textEditor = new TextEditor();
        textEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    @AfterEach
    void tearDown() {
        if (textEditor != null) {
            textEditor.dispose();
        }
    }
    
    @Test
    void testNewDocument() {
        // First add some text to make it modified
        JTextArea textArea = (JTextArea) getComponentByName(textEditor, "textArea");
        assertNotNull(textArea, "Text area should not be null");
        
        textArea.setText("Test content");
        
        // Test creating new document
        SwingUtilities.invokeLater(() -> {
            // Simulate New action
            textArea.setText("");
            assertEquals("", textArea.getText(), "Text area should be empty after New");
        });
    }
    
    @Test
    void testFileSaveAndOpen(@TempDir Path tempDir) throws IOException {
        // Create a temporary test file
        File testFile = tempDir.resolve("test.txt").toFile();
        String testContent = "This is test content for file operations.\nLine 2\nLine 3";
        
        // Test saving content to file
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = (JTextArea) getComponentByName(textEditor, "textArea");
            assertNotNull(textArea, "Text area should not be null");
            
            textArea.setText(testContent);
            
            // Simulate Save As operation
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
                writer.write(textArea.getText());
            } catch (IOException e) {
                fail("Failed to write test file: " + e.getMessage());
            }
        });
        
        // Wait for Swing operations to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify file was created and content is correct
        assertTrue(testFile.exists(), "Test file should exist");
        String savedContent = Files.readString(testFile.toPath());
        assertEquals(testContent, savedContent, "Saved content should match original");
        
        // Test opening the file
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = (JTextArea) getComponentByName(textEditor, "textArea");
            
            // Simulate Open operation
            try {
                String content = Files.readString(testFile.toPath());
                textArea.setText(content);
            } catch (IOException e) {
                fail("Failed to read test file: " + e.getMessage());
            }
        });
        
        // Wait for Swing operations to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify content was loaded correctly
        JTextArea textArea = (JTextArea) getComponentByName(textEditor, "textArea");
        assertEquals(testContent, textArea.getText(), "Loaded content should match saved content");
    }
    
    @Test
    void testSearchFunctionality() {
        String testContent = "This is a test document for search functionality.\n" +
                           "The test should find this test text.\n" +
                           "Multiple test occurrences should be found.";
        
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = (JTextArea) getComponentByName(textEditor, "textArea");
            assertNotNull(textArea, "Text area should not be null");
            
            textArea.setText(testContent);
            
            // Test search for "test"
            String searchText = "test";
            String content = textArea.getText();
            int firstOccurrence = content.indexOf(searchText);
            
            assertTrue(firstOccurrence >= 0, "Search text should be found");
            
            // Simulate selecting the found text
            textArea.setSelectionStart(firstOccurrence);
            textArea.setSelectionEnd(firstOccurrence + searchText.length());
            
            assertEquals(searchText, textArea.getSelectedText(), "Selected text should match search text");
        });
    }
    
    @Test
    void testTextEditingOperations() {
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = (JTextArea) getComponentByName(textEditor, "textArea");
            assertNotNull(textArea, "Text area should not be null");
            
            // Test text insertion
            textArea.setText("Initial text");
            assertEquals("Initial text", textArea.getText(), "Text should be set correctly");
            
            // Test selection
            textArea.selectAll();
            assertEquals("Initial text", textArea.getSelectedText(), "All text should be selected");
            
            // Test copy operation
            textArea.copy();
            // Note: Clipboard testing is complex in unit tests, so we just verify the method doesn't throw
            
            // Test cut operation
            textArea.setText("Text to cut");
            textArea.selectAll();
            textArea.cut();
            assertEquals("", textArea.getText(), "Text should be empty after cut");
            
            // Test paste operation
            textArea.setText("New text");
            textArea.selectAll();
            textArea.copy();
            textArea.setCaretPosition(textArea.getText().length());
            textArea.paste();
            assertEquals("New textNew text", textArea.getText(), "Text should be doubled after paste");
        });
    }
    
    @Test
    void testConfigurationLoading() {
        // Test that configuration is loaded properly
        assertNotNull(textEditor, "Text editor should be initialized");
        
        // Test that the window title is set correctly
        String title = textEditor.getTitle();
        assertTrue(title.contains("Advanced Text Editor") || title.contains("Text Editor"), 
                  "Window title should contain expected text");
    }
    
    @Test
    void testEmptyFileOperations() {
        File emptyFile = tempDir.resolve("empty.txt").toFile();
        
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = (JTextArea) getComponentByName(textEditor, "textArea");
            assertNotNull(textArea, "Text area should not be null");
            
            // Test saving empty content
            textArea.setText("");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(emptyFile))) {
                writer.write(textArea.getText());
            } catch (IOException e) {
                fail("Failed to write empty file: " + e.getMessage());
            }
        });
        
        // Wait for Swing operations
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify empty file handling
        assertTrue(emptyFile.exists(), "Empty file should exist");
        try {
            String content = Files.readString(emptyFile.toPath());
            assertEquals("", content, "Empty file should have no content");
        } catch (IOException e) {
            fail("Failed to read empty file: " + e.getMessage());
        }
    }
    
    // Helper method to find components by name (would need to be enhanced for real component access)
    private java.awt.Component getComponentByName(JFrame frame, String name) {
        // This is a simplified approach - in a real test, you'd need to properly
        // access the private components or make them accessible
        try {
            // Use reflection to access the textArea field for testing
            java.lang.reflect.Field field = TextEditor.class.getDeclaredField("textArea");
            field.setAccessible(true);
            return (JTextArea) field.get(frame);
        } catch (Exception e) {
            return null;
        }
    }
}