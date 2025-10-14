package com.massey.texteditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.Desktop;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import org.yaml.snakeyaml.Yaml;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Advanced Text Editor with comprehensive functionality
 * Supports multiple file formats, syntax highlighting, and PDF conversion
 */
public class TextEditor extends JFrame {
    
    private JTextPane textPane;
    private JTextArea textArea; // Keep for backward compatibility with tests
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JLabel dateTimeLabel;
    private JFileChooser fileChooser;
    private File currentFile;
    private boolean isModified = false;
    
    // Syntax highlighting
    private StyledDocument styledDocument;
    private Map<String, Color> syntaxColors;
    private Set<String> javaKeywords;
    private Set<String> pythonKeywords;
    private Set<String> jsKeywords;
    
    // Configuration
    private Properties config;
    private static final String CONFIG_FILE = "/config.yml";
    
    // Menu items
    private JMenuItem newItem, openItem, saveItem, saveAsItem, exitItem;
    private JMenuItem cutItem, copyItem, pasteItem, selectAllItem;
    private JMenuItem searchItem, timeDateItem, aboutItem, printItem;
    private JMenuItem pdfConvertItem, rtfOpenItem, odtOpenItem;
    
    public TextEditor() {
        initializeConfiguration();
        initializeComponents();
        setupMenuBar();
        setupLayout();
        setupEventHandlers();
        updateDateTime();
        
        setTitle("Advanced Text Editor - New Document");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        // Add window listener for close confirmation
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    private void initializeConfiguration() {
        config = new Properties();
        try {
            // Try to load YAML configuration
            Yaml yaml = new Yaml();
            InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (input != null) {
                Map<String, Object> yamlData = yaml.load(input);
                if (yamlData != null) {
                    // Extract font settings
                    if (yamlData.containsKey("font")) {
                        Map<String, Object> fontConfig = (Map<String, Object>) yamlData.get("font");
                        config.setProperty("font.name", String.valueOf(fontConfig.getOrDefault("name", "Consolas")));
                        config.setProperty("font.size", String.valueOf(fontConfig.getOrDefault("size", "12")));
                    } else {
                        config.setProperty("font.name", "Consolas");
                        config.setProperty("font.size", "12");
                    }
                    
                    // Extract window settings
                    if (yamlData.containsKey("window")) {
                        Map<String, Object> windowConfig = (Map<String, Object>) yamlData.get("window");
                        config.setProperty("window.title", String.valueOf(windowConfig.getOrDefault("title", "Advanced Text Editor")));
                    } else {
                        config.setProperty("window.title", "Advanced Text Editor");
                    }
                    
                    config.setProperty("file.encoding", "UTF-8");
                    input.close();
                }
            } else {
                // Use default configuration if config file not found
                config.setProperty("font.name", "Consolas");
                config.setProperty("font.size", "12");
                config.setProperty("window.title", "Advanced Text Editor");
                config.setProperty("file.encoding", "UTF-8");
            }
        } catch (Exception e) {
            // Use default configuration on any error
            config.setProperty("font.name", "Consolas");
            config.setProperty("font.size", "12");
            config.setProperty("window.title", "Advanced Text Editor");
            config.setProperty("file.encoding", "UTF-8");
        }
    }
    
    private void initializeComponents() {
        // Initialize syntax highlighting
        initializeSyntaxHighlighting();
        
        // Initialize text pane with syntax highlighting
        textPane = new JTextPane();
        styledDocument = textPane.getStyledDocument();
        textPane.setFont(new Font(config.getProperty("font.name", "Consolas"), 
                                 Font.PLAIN, 
                                 Integer.parseInt(config.getProperty("font.size", "12"))));
        
        // Initialize text area for backward compatibility with tests
        textArea = new JTextArea();
        textArea.setFont(textPane.getFont());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        // Initialize scroll pane
        scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Initialize status labels
        statusLabel = new JLabel("Ready");
        dateTimeLabel = new JLabel();
        
        // Initialize file chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "java", "py", "js", "cpp", "c"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
    }
    
    private void initializeSyntaxHighlighting() {
        // Initialize syntax colors
        syntaxColors = new HashMap<>();
        syntaxColors.put("keyword", new Color(127, 0, 85)); // Dark red
        syntaxColors.put("string", new Color(42, 0, 255)); // Blue
        syntaxColors.put("comment", new Color(63, 127, 95)); // Green
        syntaxColors.put("number", new Color(0, 0, 192)); // Navy blue
        syntaxColors.put("operator", new Color(0, 0, 0)); // Black
        
        // Initialize Java keywords
        javaKeywords = new HashSet<>();
        String[] javaKeywordArray = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null"
        };
        javaKeywords.addAll(Arrays.asList(javaKeywordArray));
        
        // Initialize Python keywords
        pythonKeywords = new HashSet<>();
        String[] pythonKeywordArray = {
            "False", "None", "True", "and", "as", "assert", "break", "class", "continue", "def",
            "del", "elif", "else", "except", "finally", "for", "from", "global", "if", "import",
            "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try",
            "while", "with", "yield", "async", "await"
        };
        pythonKeywords.addAll(Arrays.asList(pythonKeywordArray));
        
        // Initialize JavaScript keywords
        jsKeywords = new HashSet<>();
        String[] jsKeywordArray = {
            "abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else",
            "enum", "eval", "export", "extends", "false", "final", "finally", "float", "for",
            "function", "goto", "if", "implements", "import", "in", "instanceof", "int", "interface",
            "let", "long", "native", "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "true", "try", "typeof", "var", "void", "volatile", "while", "with", "yield"
        };
        jsKeywords.addAll(Arrays.asList(jsKeywordArray));
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        newItem = new JMenuItem("New", KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        
        openItem = new JMenuItem("Open", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        
        saveItem = new JMenuItem("Save", KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        
        saveAsItem = new JMenuItem("Save As...");
        printItem = new JMenuItem("Print", KeyEvent.VK_P);
        printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        
        exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        cutItem = new JMenuItem("Cut", KeyEvent.VK_T);
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        
        copyItem = new JMenuItem("Copy", KeyEvent.VK_C);
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        
        pasteItem = new JMenuItem("Paste", KeyEvent.VK_P);
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        
        selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);
        
        // Search Menu
        JMenu searchMenu = new JMenu("Search");
        searchMenu.setMnemonic(KeyEvent.VK_S);
        
        searchItem = new JMenuItem("Find...", KeyEvent.VK_F);
        searchItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        
        timeDateItem = new JMenuItem("Time/Date", KeyEvent.VK_D);
        timeDateItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        
        searchMenu.add(searchItem);
        searchMenu.addSeparator();
        searchMenu.add(timeDateItem);
        
        // Advanced Menu (for additional file formats)
        JMenu advancedMenu = new JMenu("Advanced");
        advancedMenu.setMnemonic(KeyEvent.VK_A);
        
        rtfOpenItem = new JMenuItem("Open RTF File...");
        odtOpenItem = new JMenuItem("Open ODT File...");
        pdfConvertItem = new JMenuItem("Convert to PDF...");
        
        advancedMenu.add(rtfOpenItem);
        advancedMenu.add(odtOpenItem);
        advancedMenu.addSeparator();
        advancedMenu.add(pdfConvertItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(searchMenu);
        menuBar.add(advancedMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void setupLayout() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(dateTimeLabel, BorderLayout.EAST);
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        add(scrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Document listener for modification tracking
        textPane.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                setModified(true);
                // Apply syntax highlighting after text changes
                SwingUtilities.invokeLater(() -> applySyntaxHighlighting());
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                setModified(true);
                // Apply syntax highlighting after text changes
                SwingUtilities.invokeLater(() -> applySyntaxHighlighting());
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                setModified(true);
            }
        });
        
        // File menu handlers
        newItem.addActionListener(e -> createNewDocument());
        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());
        saveAsItem.addActionListener(e -> saveAsFile());
        printItem.addActionListener(e -> printDocument());
        exitItem.addActionListener(e -> exitApplication());
        
        // Edit menu handlers
        cutItem.addActionListener(e -> textPane.cut());
        copyItem.addActionListener(e -> textPane.copy());
        pasteItem.addActionListener(e -> textPane.paste());
        selectAllItem.addActionListener(e -> textPane.selectAll());
        
        // Search menu handlers
        searchItem.addActionListener(e -> showSearchDialog());
        timeDateItem.addActionListener(e -> insertTimeDate());
        
        // Advanced menu handlers
        rtfOpenItem.addActionListener(e -> openRTFFile());
        odtOpenItem.addActionListener(e -> openODTFile());
        pdfConvertItem.addActionListener(e -> convertToPDF());
        
        // Help menu handlers
        aboutItem.addActionListener(e -> showAboutDialog());
    }
    
    private void createNewDocument() {
        if (confirmSave()) {
            textPane.setText("");
            currentFile = null;
            isModified = false;
            updateTitle();
            statusLabel.setText("New document created");
        }
    }
    
    private void openFile() {
        if (confirmSave()) {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (readFile(file)) {
                    currentFile = file;
                    isModified = false;
                    updateTitle();
                    statusLabel.setText("File opened: " + file.getName());
                }
            }
        }
    }
    
    private boolean readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            textPane.setText(content.toString());
            // Apply syntax highlighting based on file extension
            applySyntaxHighlighting();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void saveFile() {
        if (currentFile == null) {
            saveAsFile();
        } else {
            if (writeFile(currentFile)) {
                isModified = false;
                updateTitle();
                statusLabel.setText("File saved: " + currentFile.getName());
            }
        }
    }
    
    private void saveAsFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            if (writeFile(file)) {
                currentFile = file;
                isModified = false;
                updateTitle();
                statusLabel.setText("File saved: " + file.getName());
            }
        }
    }
    
    private boolean writeFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(textPane.getText());
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void printDocument() {
        try {
            textPane.print();
            statusLabel.setText("Document printed");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error printing: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exitApplication() {
        if (confirmSave()) {
            System.exit(0);
        }
    }
    
    private boolean confirmSave() {
        if (isModified) {
            int result = JOptionPane.showConfirmDialog(this, 
                "The document has been modified. Do you want to save changes?",
                "Confirm Save", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                saveFile();
                return !isModified;
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        return true;
    }
    
    private void showSearchDialog() {
        String searchText = JOptionPane.showInputDialog(this, "Find:", "Search", 
                                                       JOptionPane.PLAIN_MESSAGE);
        if (searchText != null && !searchText.isEmpty()) {
            searchText(searchText);
        }
    }
    
    private void searchText(String searchText) {
        String content = textPane.getText();
        int index = content.indexOf(searchText);
        if (index >= 0) {
            textPane.setSelectionStart(index);
            textPane.setSelectionEnd(index + searchText.length());
            textPane.requestFocusInWindow();
            statusLabel.setText("Found: " + searchText);
        } else {
            JOptionPane.showMessageDialog(this, "Text not found: " + searchText, 
                                        "Search Result", JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("Text not found");
        }
    }
    
    private void insertTimeDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = now.format(formatter);
        
        int caretPosition = textArea.getCaretPosition();
        textArea.insert(dateTime, caretPosition);
        statusLabel.setText("Time and date inserted");
    }
    
    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        dateTimeLabel.setText(now.format(formatter));
        
        // Update every second
        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.setRepeats(true);
        timer.start();
    }
    
    private void updateTitle() {
        String title = config.getProperty("window.title", "Advanced Text Editor");
        if (currentFile != null) {
            title += " - " + currentFile.getName();
        } else {
            title += " - New Document";
        }
        if (isModified) {
            title += " *";
        }
        setTitle(title);
    }
    
    private void setModified(boolean modified) {
        isModified = modified;
        updateTitle();
    }
    
    private void openRTFFile() {
        if (confirmSave()) {
            // Set file filter for RTF files
            fileChooser.setFileFilter(new FileNameExtensionFilter("RTF Files", "rtf"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (readRTFFile(file)) {
                    currentFile = file;
                    isModified = false;
                    updateTitle();
                    statusLabel.setText("RTF file opened: " + file.getName());
                }
            }
            // Reset file filter to default
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "java", "py", "js", "cpp", "c"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
        }
    }
    
    private boolean readRTFFile(File file) {
        try {
            RTFEditorKit rtfKit = new RTFEditorKit();
            DefaultStyledDocument document = new DefaultStyledDocument();
            
            try (FileInputStream fis = new FileInputStream(file)) {
                rtfKit.read(fis, document, 0);
                textPane.setDocument(document);
                styledDocument = document;
                return true;
            }
        } catch (IOException | BadLocationException e) {
            JOptionPane.showMessageDialog(this, "Error reading RTF file: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void openODTFile() {
        if (confirmSave()) {
            // Set file filter for ODT files
            fileChooser.setFileFilter(new FileNameExtensionFilter("ODT Files", "odt"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (readODTFile(file)) {
                    currentFile = file;
                    isModified = false;
                    updateTitle();
                    statusLabel.setText("ODT file opened: " + file.getName());
                }
            }
            // Reset file filter to default
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "java", "py", "js", "cpp", "c"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
        }
    }
    
    private boolean readODTFile(File file) {
        try {
            // ODT files are ZIP archives containing XML content
            java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(file);
            java.util.zip.ZipEntry contentEntry = zipFile.getEntry("content.xml");
            
            if (contentEntry == null) {
                JOptionPane.showMessageDialog(this, "Invalid ODT file: missing content.xml", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Read the XML content
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(zipFile.getInputStream(contentEntry), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            // Simple XML parsing to extract text content
            String xmlContent = content.toString();
            String extractedText = extractTextFromODTXML(xmlContent);
            
            textArea.setText(extractedText);
            zipFile.close();
            return true;
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading ODT file: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private String extractTextFromODTXML(String xmlContent) {
        StringBuilder text = new StringBuilder();
        
        // Simple regex-based extraction of text content
        // This is a basic implementation - a full ODT parser would be more complex
        java.util.regex.Pattern textPattern = java.util.regex.Pattern.compile(">([^<]+)<");
        java.util.regex.Matcher matcher = textPattern.matcher(xmlContent);
        
        while (matcher.find()) {
            String textContent = matcher.group(1).trim();
            if (!textContent.isEmpty() && !textContent.startsWith("<") && !textContent.startsWith("office:")) {
                text.append(textContent).append(" ");
            }
        }
        
        // Clean up the extracted text
        String result = text.toString().replaceAll("\\s+", " ").trim();
        return result.isEmpty() ? "[ODT file content could not be extracted]" : result;
    }
    
    private void convertToPDF() {
        if (textPane.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No content to convert. Please add some text first.", 
                                        "No Content", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Set up file chooser for saving PDF
        fileChooser.setSelectedFile(new File("document.pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Ensure .pdf extension
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            
            // Check if file exists and confirm overwrite
            if (file.exists()) {
                int result = JOptionPane.showConfirmDialog(this, 
                    "File already exists. Do you want to overwrite it?", 
                    "Confirm Overwrite", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
                
                if (result != JOptionPane.YES_OPTION) {
                    // Reset file filter to default
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "java", "py", "js", "cpp", "c"));
                    fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
                    return;
                }
            }
            
            if (convertTextToPDF(file)) {
                statusLabel.setText("PDF created: " + file.getName());
                
                // Ask if user wants to open the PDF
                int openResult = JOptionPane.showConfirmDialog(this, 
                    "PDF file created successfully. Do you want to open it?", 
                    "PDF Created", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (openResult == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this, "Could not open PDF file: " + e.getMessage(), 
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        
        // Reset file filter to default
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "java", "py", "js", "cpp", "c"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));
    }
    
    private boolean convertTextToPDF(File file) {
        try {
            // Create a new PDF document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            // Create a font
            PDFont font = PDType1Font.HELVETICA;
            float fontSize = 12;
            float leading = 1.5f * fontSize;
            
            // Create content stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
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
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(font, fontSize);
                    currentY = startY;
                }
            }
            
            contentStream.close();
            document.save(file);
            document.close();
            
            return true;
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error creating PDF: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void applySyntaxHighlighting() {
        try {
            String text = textPane.getText();
            String fileName = currentFile != null ? currentFile.getName().toLowerCase() : "";
            
            // Clear existing styles
            styledDocument.setCharacterAttributes(0, text.length(), 
                javax.swing.text.SimpleAttributeSet.EMPTY, true);
            
            // Determine file type and apply highlighting
            if (fileName.endsWith(".java")) {
                highlightJava(text);
            } else if (fileName.endsWith(".py")) {
                highlightPython(text);
            } else if (fileName.endsWith(".js")) {
                highlightJavaScript(text);
            }
            
        } catch (Exception e) {
            // Silently ignore highlighting errors to prevent disrupting editing
        }
    }
    
    private void highlightJava(String text) {
        highlightKeywords(text, javaKeywords, syntaxColors.get("keyword"));
        highlightStrings(text, syntaxColors.get("string"));
        highlightComments(text, syntaxColors.get("comment"));
        highlightNumbers(text, syntaxColors.get("number"));
    }
    
    private void highlightPython(String text) {
        highlightKeywords(text, pythonKeywords, syntaxColors.get("keyword"));
        highlightStrings(text, syntaxColors.get("string"));
        highlightComments(text, syntaxColors.get("comment"));
        highlightNumbers(text, syntaxColors.get("number"));
    }
    
    private void highlightJavaScript(String text) {
        highlightKeywords(text, jsKeywords, syntaxColors.get("keyword"));
        highlightStrings(text, syntaxColors.get("string"));
        highlightComments(text, syntaxColors.get("comment"));
        highlightNumbers(text, syntaxColors.get("number"));
    }
    
    private void highlightKeywords(String text, Set<String> keywords, java.awt.Color color) {
        javax.swing.text.SimpleAttributeSet keywordAttr = new javax.swing.text.SimpleAttributeSet();
        keywordAttr.addAttribute(javax.swing.text.StyleConstants.Foreground, color);
        keywordAttr.addAttribute(javax.swing.text.StyleConstants.Bold, true);
        
        for (String keyword : keywords) {
            String pattern = "\\b" + keyword + "\\b";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            
            while (m.find()) {
                styledDocument.setCharacterAttributes(m.start(), m.end() - m.start(), keywordAttr, true);
            }
        }
    }
    
    private void highlightStrings(String text, java.awt.Color color) {
        javax.swing.text.SimpleAttributeSet stringAttr = new javax.swing.text.SimpleAttributeSet();
        stringAttr.addAttribute(javax.swing.text.StyleConstants.Foreground, color);
        
        // Double quotes
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"[^\"]*\"");
        java.util.regex.Matcher m = p.matcher(text);
        
        while (m.find()) {
            styledDocument.setCharacterAttributes(m.start(), m.end() - m.start(), stringAttr, true);
        }
        
        // Single quotes
        p = java.util.regex.Pattern.compile("'[^']*'");
        m = p.matcher(text);
        
        while (m.find()) {
            styledDocument.setCharacterAttributes(m.start(), m.end() - m.start(), stringAttr, true);
        }
    }
    
    private void highlightComments(String text, java.awt.Color color) {
        javax.swing.text.SimpleAttributeSet commentAttr = new javax.swing.text.SimpleAttributeSet();
        commentAttr.addAttribute(javax.swing.text.StyleConstants.Foreground, color);
        commentAttr.addAttribute(javax.swing.text.StyleConstants.Italic, true);
        
        // Single line comments
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("//.*$", java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher m = p.matcher(text);
        
        while (m.find()) {
            styledDocument.setCharacterAttributes(m.start(), m.end() - m.start(), commentAttr, true);
        }
        
        // Multi-line comments
        p = java.util.regex.Pattern.compile("/\\*.*?\\*/", java.util.regex.Pattern.DOTALL);
        m = p.matcher(text);
        
        while (m.find()) {
            styledDocument.setCharacterAttributes(m.start(), m.end() - m.start(), commentAttr, true);
        }
    }
    
    private void highlightNumbers(String text, java.awt.Color color) {
        javax.swing.text.SimpleAttributeSet numberAttr = new javax.swing.text.SimpleAttributeSet();
        numberAttr.addAttribute(javax.swing.text.StyleConstants.Foreground, color);
        
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\b\\d+\\.?\\d*\\b");
        java.util.regex.Matcher m = p.matcher(text);
        
        while (m.find()) {
            styledDocument.setCharacterAttributes(m.start(), m.end() - m.start(), numberAttr, true);
        }
    }

    private void showAboutDialog() {
        String aboutMessage = "Advanced Text Editor\n\n" +
                            "Developed by: [Your Name] & [Partner Name]\n" +
                            "Massey University - 159.251 Software Design and Construction\n" +
                            "Version 1.0\n\n" +
                            "Features:\n" +
                            "- Basic text editing\n" +
                            "- File operations (New, Open, Save, Print)\n" +
                            "- Edit operations (Cut, Copy, Paste)\n" +
                            "- Search functionality\n" +
                            "- Time and Date insertion\n" +
                            "- RTF and ODT support\n" +
                            "- PDF conversion\n" +
                            "- Syntax highlighting for .java, .py, .js files";
        
        JOptionPane.showMessageDialog(this, aboutMessage, "About Advanced Text Editor", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            TextEditor editor = new TextEditor();
            editor.setVisible(true);
        });
    }
}