package com.spraed.flyingsaucer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.w3c.dom.DOMException;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

public class PDFGenerator {

	public static void main(String[] args) throws IOException,
			DocumentException, ParseException {

		// enable logging
		// System.getProperties().setProperty("xr.util-logging.loggingEnabled",
		// "true");
		// XRLog.setLoggingEnabled(true);

		// get options
		Options options = new Options();
		options.addOption("html", true, "HTML path");
		options.addOption("pdf", true, "PDF path");
		options.addOption("encoding", true, "Encoding");
		options.addOption("fontPaths", true, "CSV font paths");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		String tmpFile = cmd.getOptionValue("html");
		String pdfFile = cmd.getOptionValue("pdf");
		String encoding = cmd.getOptionValue("encoding");
		String fontPaths = cmd.getOptionValue("fontPaths");

		// read html file directories from tmp file
		InputStream tmpInputStream = new FileInputStream(tmpFile);

		InputStream dataStream = new DataInputStream(tmpInputStream);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				dataStream));

		List<String> htmlFiles = new ArrayList<String>();
		String strLine;
		while ((strLine = reader.readLine()) != null) {
			htmlFiles.add(strLine);
		}

		// close buffered reader
		reader.close();

		// get output stream
		OutputStream os = new FileOutputStream(pdfFile);

		// create pdf renderer
		ITextRenderer renderer = new ITextRenderer();

		if (fontPaths != null) {
			configureFonts(renderer, fontPaths.split(","));
		}

		int index = 0;
		for (String htmlInputFile : htmlFiles) {
			// get file input stream
			InputStream htmlInputStream = new FileInputStream(htmlInputFile);

			// get file output stream for a tidy html
			File htmlOutputFile = File.createTempFile("tidy", ".html");
			OutputStream htmlOutputStream = new FileOutputStream(htmlOutputFile);

			// clean up HTML with jTidy
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setHideComments(true);
			tidy.setShowWarnings(false);
			tidy.setQuiet(true);
			tidy.setForceOutput(true);

//			// add unknown tags
//			Properties props = new Properties();
//			props.setProperty("new-empty-tags", "o:p");
//			tidy.setConfigurationFromProps(props);

			tidy.setInputEncoding(encoding);
			tidy.setOutputEncoding(encoding);
			tidy.parse(htmlInputStream, htmlOutputStream);

			// close html input and output stream
			htmlInputStream.close();
			htmlOutputStream.close();

			try {
				// render pdf from tidy html
				renderer.setDocument(htmlOutputFile);
				renderer.layout();

				// check if pdf has to be created, iterate index afterwards
				if (index == 0) {
					renderer.createPDF(os, false);
				} else {
					renderer.writeNextDocument();
				}
				index++;
			}

			finally {
				// delete html output file
				htmlOutputFile.delete();
			}
		}

		renderer.finishPDF();
		os.close();

	}

	protected static void configureFonts(ITextRenderer renderer,
			String[] fontPaths) throws DOMException, DocumentException,
			IOException {

		ITextFontResolver resolver = renderer.getFontResolver();

		for (String fontPath : fontPaths) {
			resolver.addFont(fontPath, true);
		}
	}

}
