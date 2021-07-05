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

	public static void main(String[] args) throws IOException, DocumentException, ParseException {
		CommandLine cmd = defineOptions(args);

		InputStream tmpInputStream = new FileInputStream(cmd.getOptionValue("html"));
		InputStream dataStream = new DataInputStream(tmpInputStream);
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));

		List<String> htmlFiles = new ArrayList<String>();
		String strLine;
		while ((strLine = reader.readLine()) != null) {
			htmlFiles.add(strLine);
		}

		reader.close();

		OutputStream os = new FileOutputStream(cmd.getOptionValue("pdf"));
		ITextRenderer renderer = new ITextRenderer();

		String fontPaths = cmd.getOptionValue("fontPaths");
		if (fontPaths != null) {
			configureFonts(renderer, fontPaths.split(","));
		}

		int index = 0;
		for (String htmlInputFile : htmlFiles) {
			File htmlOutputFile = tidyHtml(htmlInputFile, cmd.getOptionValue("encoding"));

			try {
				renderer.setDocument(htmlOutputFile);
				renderer.layout();

				if (index == 0) {
					renderer.createPDF(os, false);
				} else {
					renderer.writeNextDocument();
				}
				index++;
			} finally {
				htmlOutputFile.delete();
			}
		}

		renderer.finishPDF();
		os.close();

	}

	protected static void configureFonts(ITextRenderer renderer, String[] fontPaths)
			throws DOMException, DocumentException, IOException {

		ITextFontResolver resolver = renderer.getFontResolver();

		for (String fontPath : fontPaths) {
			resolver.addFont(fontPath, true);
		}
	}

	private static CommandLine defineOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("html", true, "HTML path");
		options.addOption("pdf", true, "PDF path");
		options.addOption("encoding", true, "Encoding");
		options.addOption("fontPaths", true, "CSV font paths");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		return cmd;
	}

	private static File tidyHtml(String htmlInputFile, String encoding) throws IOException {
		InputStream htmlInputStream = new FileInputStream(htmlInputFile);
		File htmlOutputFile = File.createTempFile("tidy", ".html");
		OutputStream htmlOutputStream = new FileOutputStream(htmlOutputFile);

		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setHideComments(true);
		tidy.setShowWarnings(false);
		tidy.setQuiet(true);
		tidy.setForceOutput(true);

		tidy.setInputEncoding(encoding);
		tidy.setOutputEncoding(encoding);
		tidy.parse(htmlInputStream, htmlOutputStream);

		htmlInputStream.close();
		htmlOutputStream.close();

		return htmlOutputFile;
	}
}
