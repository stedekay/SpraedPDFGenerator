package com.spraed.flyingsaucer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

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

		String htmlFile = cmd.getOptionValue("html");
		String pdfFile = cmd.getOptionValue("pdf");
		String encoding = cmd.getOptionValue("encoding");
		
		// get file input stream
		String htmlInputFile = htmlFile;
		InputStream htmlInputStream = new FileInputStream(htmlInputFile);
		
		// get file output stream for a tidy html
		File htmlOutputFile = new File(System.getProperty("java.io.tmpdir") + "tidy" + UUID.randomUUID() + ".html");
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

		// get output stream
		OutputStream os = new FileOutputStream(pdfFile);
		String url = htmlOutputFile.toURI().toURL().toString();

		// render pdf from tidy html
		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocument(url);
		renderer.layout();
		renderer.createPDF(os);

		os.close();
		
		// delete tidy html file
		htmlInputStream.close();
		htmlOutputStream.close();
		htmlOutputFile.delete();
	}
}
