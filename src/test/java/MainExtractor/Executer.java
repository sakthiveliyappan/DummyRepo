package MainExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.text.PDFTextStripper;


public class Executer  {

	// This is used to create a separate text file in which the contents are got from pdf.

	public static void main(String[] args) {
		BufferedWriter wr;
		PDDocument pd;		
		String string = null;
        try { 

            File input = new File(
					"C:\\Users\\Hp\\Desktop\\TriplePulse\\vijayanathan.pdf"); // The
																																					// extract
			File output = new File("vijayanathan1.txt"); // The text file where you are going to store the extracted data
			pd = PDDocument.load(input);
			
            PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
            ///pdfTextStripper.setStartPage(0);
           // pdfTextStripper.setEndPage(1);
            pdfTextStripper.setSortByPosition(true);
            
            pdfTextStripper.getText(pd);
            System.out.println(pdfTextStripper.getText(pd));
            
			wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
			pdfTextStripper.writeText(pd, wr);
			
			BufferedReader b = new BufferedReader(new FileReader(output));
			String currentLine = null;

			while ((currentLine = b.readLine()) != null) {
				//System.out.println("currentLine : "+ currentLine);
				if (currentLine.trim().length() > 0) {
					//System.out.println(" added currentLine : "+ currentLine.trim());
				}
			}
			
			
			if (pd != null) {
				pd.close();
			}
			wr.close();

            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        };
		
	}
	
	

}
