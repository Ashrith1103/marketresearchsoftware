package TrialFiles;

import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFWriterExample {

    public static void main(String[] args) {
        try 
        {
            // Create a PDF document object
            Document document = new Document();
            // Create a PDF writer to write data to the PDF document
            PdfWriter.getInstance(document, new FileOutputStream("output.pdf"));
            // Open the PDF document
            document.open();
            // Write data to the PDF document
            document.add(new Paragraph("Hello, World!"));
            // Close the PDF document
            document.close();

        } 
        catch (Exception e) 
        {
            System.out.println("Error");
        }
    }
}
