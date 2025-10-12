# Advanced Text Editor

A feature-rich Java-based text editor with support for multiple file formats, syntax highlighting, and PDF conversion capabilities.

## Features

### Core Functionality
- **Text Editing**: Full-featured text editing with undo/redo support
- **File Operations**: Open, save, and create text files
- **Search & Replace**: Find and replace functionality with case-sensitive options
- **Word Count**: Real-time word and character counting
- **Dark Mode**: Toggle between light and dark themes
- **Font Customization**: Adjustable font size and family

### Advanced Features
- **RTF Support**: Open and save Rich Text Format files
- **ODT Support**: Open OpenDocument Text files
- **PDF Conversion**: Convert text documents to PDF format
- **Syntax Highlighting**: Support for Java (.java), Python (.py), and JavaScript (.js) files
- **Print Support**: Print documents directly from the editor

### Development Features
- **Maven Build System**: Professional project structure
- **Unit Testing**: Comprehensive test suite with JUnit 5
- **Code Quality**: PMD static analysis integration
- **CI/CD Pipeline**: GitHub Actions workflows for automated testing and releases
- **Docker Support**: Containerized deployment ready

## Requirements

- Java 11 or higher
- Maven 3.6+
- Python 3.6+ (for PDF conversion feature)
- Python libraries: `reportlab`, `python-docx` (installed automatically)

## Installation

### Option 1: Build from Source
```bash
# Clone the repository
git clone <repository-url>
cd Advanced-Text-Editor

# Build the project
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package
```

### Option 2: Download Pre-built JAR
Download the latest release from the [Releases](https://github.com/your-username/Advanced-Text-Editor/releases) page.

## Usage

### Running the Application
```bash
# Using Maven
mvn exec:java

# Or run the packaged JAR
java -jar target/AdvancedTextEditor-1.0-SNAPSHOT.jar
```

### File Operations
- **New File**: Ctrl+N or File → New
- **Open File**: Ctrl+O or File → Open
- **Save File**: Ctrl+S or File → Save
- **Save As**: Ctrl+Shift+S or File → Save As
- **Print**: Ctrl+P or File → Print

### Text Operations
- **Find**: Ctrl+F or Edit → Find
- **Replace**: Ctrl+H or Edit → Replace
- **Select All**: Ctrl+A or Edit → Select All
- **Word Count**: View → Word Count

### Format Operations
- **Font**: Format → Font to change font family and size
- **Dark Mode**: View → Dark Mode to toggle theme

### Special Features
- **PDF Conversion**: File → Convert to PDF
- **Syntax Highlighting**: Automatically applied for supported file types

## File Format Support

| Format | Extension | Read | Write | Notes |
|--------|-----------|------|-------|-------|
| Plain Text | .txt | ✅ | ✅ | Default format |
| Rich Text | .rtf | ✅ | ✅ | Preserves formatting |
| OpenDocument | .odt | ✅ | ❌ | Read-only support |
| Java | .java | ✅ | ✅ | Syntax highlighting |
| Python | .py | ✅ | ✅ | Syntax highlighting |
| JavaScript | .js | ✅ | ✅ | Syntax highlighting |

## Development

### Project Structure
```
Advanced-Text-Editor/
├── src/main/java/com/massey/texteditor/
│   └── TextEditor.java          # Main application class
├── src/test/java/com/massey/texteditor/
│   └── TextEditorTest.java      # Unit tests
├── src/main/resources/
│   └── config.yml               # Application configuration
├── .github/workflows/
│   ├── ci.yml                   # CI/CD pipeline
│   └── release.yml              # Release automation
├── pom.xml                      # Maven configuration
├── Dockerfile                   # Docker configuration
└── README.md                    # This file
```

### Building and Testing
```bash
# Compile the project
mvn compile

# Run unit tests
mvn test

# Run PMD code quality analysis
mvn pmd:pmd

# Package the application
mvn package

# Clean build artifacts
mvn clean
```

### Code Quality
The project uses PMD for static code analysis. Configuration is in `pmd-ruleset.xml` with custom rules for:
- Performance optimization
- Error prevention
- Code style enforcement
- Design pattern compliance

### CI/CD Pipeline
GitHub Actions workflows provide:
- **Continuous Integration**: Automated testing on every push
- **Code Quality Checks**: PMD analysis with reporting
- **Automated Releases**: Version tagging and artifact publishing
- **Multi-platform Testing**: Ubuntu, Windows, and macOS support

## Configuration

### Application Settings
Configuration file: `src/main/resources/config.yml`
```yaml
app:
  name: "Advanced Text Editor"
  version: "1.0.0"
  
editor:
  defaultFont: "Monospaced"
  defaultFontSize: 12
  darkMode: false
  
pdf:
  pythonScript: "extract_pdf.py"
  
syntax:
  enabled: true
  supportedExtensions: [".java", ".py", ".js"]
```

### Environment Variables
- `JAVA_HOME`: Java installation directory
- `MAVEN_HOME`: Maven installation directory (for development)

## Docker Support

### Building Docker Image
```bash
docker build -t advanced-text-editor .
```

### Running with Docker
```bash
# Run with GUI support (Linux)
docker run -it --rm \
  -e DISPLAY=$DISPLAY \
  -v /tmp/.X11-unix:/tmp/.X11-unix \
  advanced-text-editor

# Run with volume mounting
docker run -it --rm \
  -v $(pwd)/documents:/app/documents \
  advanced-text-editor
```

## Troubleshooting

### Common Issues

1. **PDF Conversion Fails**
   - Ensure Python is installed and accessible
   - Install required Python packages: `pip install reportlab python-docx`
   - Check that `extract_pdf.py` is in the project root

2. **Syntax Highlighting Not Working**
   - Verify file extension is `.java`, `.py`, or `.js`
   - Check that syntax highlighting is enabled in configuration

3. **File Opening Issues**
   - Ensure file permissions are correct
   - Check file format is supported
   - Verify file is not corrupted

4. **Build Failures**
   - Update Maven dependencies: `mvn clean install -U`
   - Check Java version compatibility (requires Java 11+)
   - Verify Maven version (requires 3.6+)

### Performance Optimization
- For large files (>1MB), consider increasing JVM heap size
- Use appropriate file formats (RTF is slower than plain text)
- Disable syntax highlighting for very large files

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and add tests
4. Run the test suite: `mvn test`
5. Check code quality: `mvn pmd:pmd`
6. Commit your changes: `git commit -am 'Add new feature'`
7. Push to the branch: `git push origin feature-name`
8. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Changelog

### Version 1.0.0 (Current)
- Initial release with core text editing functionality
- RTF and ODT file support
- PDF conversion capabilities
- Syntax highlighting for Java, Python, and JavaScript
- Dark mode and font customization
- Comprehensive test suite
- CI/CD pipeline with GitHub Actions
- Docker containerization support

## Support

For issues, questions, or contributions, please:
- Open an issue on GitHub
- Check existing issues for solutions
- Review the documentation
- Contact the maintainers

---

**Note**: This project was developed as part of the 159251 course assignment and demonstrates professional software development practices including testing, code quality analysis, and automated deployment pipelines.**