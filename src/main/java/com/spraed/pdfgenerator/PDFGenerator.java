package com.spraed.pdfgenerator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.w3c.tidy.Tidy;

import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;

public class PDFGenerator {

	public static void main(String[] args) throws IOException, ParseException {
		setLogLevel();

		CommandLine cmd = defineOptions(args);
		List<String> htmlFiles = getHtmlFiles(cmd.getOptionValue("html"));

		String encoding = cmd.getOptionValue("encoding");
		OutputStream os = new FileOutputStream(cmd.getOptionValue("pdf"));
		PDDocument doc = new PDDocument();

		try {
			for (String htmlFile : htmlFiles) {
				PdfRendererBuilder builder = new PdfRendererBuilder();
				builder.withHtmlContent(readHtmlFile(htmlFile, encoding), null);
				builder.usePDDocument(doc);
				PdfBoxRenderer renderer = builder.buildPdfRenderer();
				renderer.createPDFWithoutClosing();
				renderer.close();
			}

			doc.save(os);
		} finally {
			os.close();
			doc.close();
		}
	}

	private static void setLogLevel() {
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setLevel(Level.SEVERE);
		for (Handler handler : rootLogger.getHandlers()) {
			handler.setLevel(Level.SEVERE);
		}

		XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, Level.WARNING));
	}

	private static CommandLine defineOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("html", true, "HTML path");
		options.addOption("pdf", true, "PDF path");
		options.addOption("encoding", true, "Encoding");

		CommandLineParser parser = new PosixParser();
		return parser.parse(options, args);
	}

	private static List<String> getHtmlFiles(String htmlPathsFile) throws IOException {
		InputStream tmpInputStream = new FileInputStream(htmlPathsFile);
		InputStream dataStream = new DataInputStream(tmpInputStream);
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));

		List<String> htmlFiles = new ArrayList<String>();
		String strLine;
		while ((strLine = reader.readLine()) != null) {
			htmlFiles.add(strLine);
		}

		reader.close();

		return htmlFiles;
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
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setHideComments(true);
		tidy.setShowWarnings(false);
		tidy.setQuiet(true);
		tidy.setForceOutput(true);
		tidy.setInputEncoding(encoding);
		tidy.setOutputEncoding(encoding);
		tidy.setDocType("omit");

		StringWriter writer = new StringWriter();
		tidy.parse(new StringReader(htmlContent), writer);

		return writer.toString();
	}
}
