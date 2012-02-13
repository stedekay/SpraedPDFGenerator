package de.enterio.flyingsaucer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

public class PDFGenerator {

	public static void main(String[] args) throws IOException,
			DocumentException {
		
		// get file input stream
		String htmlInputFile = args[0];
		InputStream htmlInputStream = new FileInputStream(htmlInputFile);
		
		// get file output stream for a tidy html
		File htmlOutputFile = new File(System.getProperty("java.io.tmpdir") + "tidy" + UUID.randomUUID() + ".html");
		OutputStream htmlOutputStream = new FileOutputStream(htmlOutputFile);
		
		// clean up html
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setHideComments(true);
		tidy.setShowWarnings(false);
		tidy.parse(htmlInputStream, htmlOutputStream);
		
//		String url = htmlOutputFile.toURI().toURL().toString();

		// set pdf file
		String outputFile = "output.pdf";
		
		if(args.length > 1) {
			outputFile = args[1];
		}
		
		OutputStream os = new FileOutputStream(outputFile);

		// render pdf from tidy html
		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocument(htmlOutputFile);
		renderer.layout();
		renderer.createPDF(os);

		os.close();
		
		// delete tidy html file
		htmlInputStream.close();
		htmlOutputStream.close();
//		htmlOutputFile.delete();
	}
}
