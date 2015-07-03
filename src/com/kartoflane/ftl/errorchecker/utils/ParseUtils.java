package com.kartoflane.ftl.errorchecker.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.vhati.modmanager.core.SloppyXMLParser;

import org.jdom2.Document;
import org.jdom2.input.JDOMParseException;
import org.jdom2.located.LocatedJDOMFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartoflane.common.utils.IOUtils;
import com.kartoflane.ftl.layout.FTLLayoutParseException;
import com.kartoflane.ftl.layout.FTLLayoutParser;
import com.kartoflane.ftl.layout.ShipLayout;
import com.kartoflane.ftl.layout.located.LocatedFTLLayoutFactory;


public class ParseUtils {

	private ParseUtils() {
		// Static class -- disallow instantiation.
		throw new RuntimeException("Attempted to instantiate static class.");
	}

	/**
	 * Interprets the string as XML.
	 * 
	 * @param contents
	 *            the string to be interpreted
	 * @return the XML document representing the string
	 * 
	 * @throws JDOMParseException
	 *             when an exception occurs while parsing XML
	 */
	public static Document parseXML(String contents) throws JDOMParseException {
		if (contents == null)
			throw new IllegalArgumentException("Parsed string must not be null.");

		SloppyXMLParser parser = new SloppyXMLParser(new LocatedJDOMFactory());

		return parser.build(contents);
	}

	/**
	 * Reads the stream's contents as string, and then tries to interpret that as XML.
	 * 
	 * Important: this method does not close the stream.
	 */
	public static Document readStreamXML(InputStream is) throws IOException, JDOMParseException {
		IOUtils.DecodeResult dr = IOUtils.decodeText(is, null);
		String contents = dr.text;
		return parseXML(contents);
	}

	public static Document readFileXML(File f) throws JDOMParseException, IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			return readStreamXML(fis);
		}
		finally {
			fis.close();
		}
	}

	/**
	 * Interprets the string as Json.
	 * 
	 * @param contents
	 *            the string to be interpreted
	 * @return the Json node representing the string
	 * 
	 * @throws JsonProcessingException
	 *             when an exception occurs while parsing Json
	 * @throws IOException
	 *             when an IO error occurs
	 */
	public static JsonNode parseJson(String contents) throws JsonProcessingException, IOException {
		if (contents == null)
			throw new IllegalArgumentException("Parsed string must not be null.");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		return mapper.readTree(contents);
	}

	/**
	 * Reads the stream's contents as string, and then tries to interpret that as Json.
	 * 
	 * Important: this method does not close the stream.
	 */
	public static JsonNode readStreamJson(InputStream is) throws IOException, JsonProcessingException {
		IOUtils.DecodeResult dr = IOUtils.decodeText(is, null);
		String contents = dr.text;
		return parseJson(contents);
	}

	public static JsonNode readFileJson(File f) throws IOException, JsonProcessingException {
		if (f == null)
			throw new IllegalArgumentException("Parsed file must not be null.");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		return mapper.readTree(f);
	}

	public static ShipLayout parseFTLLayout(String contents) throws FTLLayoutParseException {
		if (contents == null)
			throw new IllegalArgumentException("Parsed string must not be null.");

		FTLLayoutParser parser = new FTLLayoutParser(new LocatedFTLLayoutFactory());

		return parser.build(contents);
	}

	/**
	 * Reads the stream's contents as string, and then tries to interpret that as FTL txt layout file.
	 * 
	 * Important: this method does not close the stream.
	 */
	public static ShipLayout readStreamFTLLayout(InputStream is) throws FTLLayoutParseException, IOException {
		IOUtils.DecodeResult dr = IOUtils.decodeText(is, null);
		String contents = dr.text;
		return parseFTLLayout(contents);
	}

	public static ShipLayout readFileFTLLayout(File f) throws FTLLayoutParseException, IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			return readStreamFTLLayout(fis);
		}
		finally {
			fis.close();
		}
	}
}
