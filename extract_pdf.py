import PyPDF2
import os

pdf_path = r'c:\Users\1\Desktop\Assignment2\159251 Assignment 1.pdf'

try:
    with open(pdf_path, 'rb') as file:
        pdf_reader = PyPDF2.PdfReader(file)
        text = ""
        for i, page in enumerate(pdf_reader.pages):
            page_text = page.extract_text()
            text += f"\n--- Page {i+1} ---\n"
            text += page_text
        print(text)
        
        # Also save to a text file for easier reading
        with open('assignment_requirements.txt', 'w', encoding='utf-8') as output_file:
            output_file.write(text)
            
except Exception as e:
    print(f"Error reading PDF: {e}")