package com.spraed.flyingsaucer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.w3c.tidy.Tidy;

public class PDFGenerator {

	public static void main(String[] args) throws IOException, ParseException {
		CommandLine cmd = defineOptions(args);

		String htmlContent = readHtmlFiles(cmd.getOptionValue("html"), cmd.getOptionValue("encoding"));

		OutputStream os = new FileOutputStream(cmd.getOptionValue("pdf"));

		try {
			PdfRendererBuilder rendererBuilder = new PdfRendererBuilder();
			String fontPaths = cmd.getOptionValue("fontPaths");
			if (fontPaths != null) {
				configureFonts(rendererBuilder, fontPaths.split(","));
			}

			rendererBuilder.withHtmlContent(htmlContent, null);
			rendererBuilder.toStream(os);
			rendererBuilder.run();
		} finally {
			os.close();
		}
	}

	private static void configureFonts(PdfRendererBuilder rendererBuilder, String[] fontPaths) {
		for (String fontPath : fontPaths) {
			rendererBuilder.useFont(new File(fontPath), "MyFont");
		}
	}

	private static CommandLine defineOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("html", true, "HTML path");
		options.addOption("pdf", true, "PDF path");
		options.addOption("encoding", true, "Encoding");
		options.addOption("fontPaths", true, "CSV font paths");

		CommandLineParser parser = new PosixParser();
		return parser.parse(options, args);
	}

	private static String readHtmlFiles(String htmlListFilePath, String encoding) throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(htmlListFilePath), encoding));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			String htmlFilePath = line.trim();
			String htmlContent = readHtmlFile(htmlFilePath, encoding);
			sb.append(htmlContent);
		}
		reader.close();

		return tidyHtml(sb.toString(), encoding);
	}

	private static String readHtmlFile(String htmlFilePath, String encoding) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(htmlFilePath), encoding));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();

		return tidyHtml(sb.toString(), encoding);
	}

	private static String tidyHtml(String htmlContent, String encoding) {
		htmlContent = htmlContent.replaceAll("<!DOCTYPE[^>]*>", "");

		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setHideComments(true);
		tidy.setShowWarnings(false);
		tidy.setQuiet(true);
		tidy.setForceOutput(true);
		tidy.setInputEncoding(encoding);
		tidy.setOutputEncoding(encoding);

		StringWriter writer = new StringWriter();
		tidy.parse(new StringReader(htmlContent), writer);

		return writer.toString();
	}
}
