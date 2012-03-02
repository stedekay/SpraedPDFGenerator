package com.spraed.flyingsaucer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

public class PDFGenerator {

	public static void main(String[] args) throws IOException,
			DocumentException, ParseException {

		// get options
		Options options = new Options();
		options.addOption("html", true, "HTML path");
		options.addOption("pdf", true, "PDF path");
		options.addOption("encoding", true, "Encoding");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		String htmlFiles = cmd.getOptionValue("html");
		String pdfFile = cmd.getOptionValue("pdf");
		String encoding = cmd.getOptionValue("encoding");

		String[] files = htmlFiles.split(", ");

		// get output stream
		OutputStream os = new FileOutputStream(pdfFile);
		
		// create pdf renderer
		ITextRenderer renderer = new ITextRenderer();

		for (int i = 0; i < files.length; i++) {
			// get file input stream
			String htmlInputFile = files[i];
			InputStream htmlInputStream = new FileInputStream(htmlInputFile);

			// get file output stream for a tidy html
			File htmlOutputFile = File.createTempFile("tidy", ".html");
			OutputStream htmlOutputStream = new FileOutputStream(htmlOutputFile);

			// clean up html
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setHideComments(true);
			tidy.setShowWarnings(false);
			tidy.setQuiet(true);
			tidy.setInputEncoding(encoding);
			tidy.setOutputEncoding(encoding);
			tidy.parse(htmlInputStream, htmlOutputStream);
			
			// close html input and output stream
			htmlInputStream.close();
			htmlOutputStream.close();

			// render pdf from tidy html
			renderer.setDocument(htmlOutputFile);
			renderer.layout();
			
			if(i == 0) {
				renderer.createPDF(os, false);
			} else {
				renderer.writeNextDocument();
			}
			
			// delete html output file
			htmlOutputFile.delete();
		}
		
		renderer.finishPDF();
		os.close();

	}
}
