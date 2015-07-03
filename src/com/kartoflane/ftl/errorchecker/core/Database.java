package com.kartoflane.ftl.errorchecker.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bsh.EvalError;
import bsh.NameSpace;

import com.fasterxml.jackson.databind.JsonNode;
import com.kartoflane.common.script.BSHScriptManager;
import com.kartoflane.common.utils.IOUtils;
import com.kartoflane.ftl.errorchecker.processing.IValidationScript;
import com.kartoflane.ftl.errorchecker.utils.ParseUtils;


public class Database {

	public static final String ALL_TAG = "<all>";
	public static final String XML_SCRIPT = "XML";
	public static final String TXT_SCRIPT = "TXT";

	private static final Logger log = LogManager.getLogger(Database.class);
	private static final int initialCapacity = 50;

	private List<FilePointer> dataFileList = null;
	private List<FilePointer> resFileList = null;
	private Map<String, IValidationScript> loadedScripts = null;
	private Map<String, Set<IValidationScript>> xmlScriptMap = null;
	private Map<String, Set<IValidationScript>> txtScriptMap = null;
	private Set<IValidationScript> dataScripts = null;
	private Map<String, Set<String>> ignoredNameMap = null;
	private Map<String, String> errorDescriptionMap = null;
	private Map<String, IssueSeverity> errorSeverityMap = null;

	public Database() {
	}

	public void init(List<FilePointer> dataFiles, List<FilePointer> resFiles) {
		if (dataFiles == null || resFiles == null)
			throw new IllegalArgumentException("Arguments must not be null.");

		dataFileList = dataFiles;
		resFileList = resFiles;
		xmlScriptMap = new HashMap<String, Set<IValidationScript>>(initialCapacity);
		txtScriptMap = new HashMap<String, Set<IValidationScript>>(initialCapacity);
		dataScripts = new HashSet<IValidationScript>();
		ignoredNameMap = new HashMap<String, Set<String>>();
		errorDescriptionMap = new HashMap<String, String>(initialCapacity);
		errorSeverityMap = new HashMap<String, IssueSeverity>(initialCapacity);
	}

	public void clear() {
		if (!isInitialized())
			return;

		dataFileList.clear();
		dataFileList = null;

		resFileList.clear();
		resFileList = null;

		loadedScripts.clear();
		loadedScripts = null;

		for (Set<IValidationScript> set : xmlScriptMap.values())
			set.clear();
		xmlScriptMap.clear();
		xmlScriptMap = null;

		for (Set<IValidationScript> set : txtScriptMap.values())
			set.clear();
		txtScriptMap.clear();
		txtScriptMap = null;

		dataScripts.clear();
		dataScripts = null;

		for (Set<String> set : ignoredNameMap.values())
			set.clear();
		ignoredNameMap.clear();
		ignoredNameMap = null;

		errorDescriptionMap.clear();
		errorDescriptionMap = null;

		errorSeverityMap.clear();
		errorSeverityMap = null;

		System.gc();
	}

	public boolean isInitialized() {
		return xmlScriptMap != null;
	}

	/*
	 * =================================
	 * NOTE: Getters & setters for fields
	 * =================================
	 */

	private void addErrorDescription(IssueSeverity severity, String issueName, String message) throws IllegalArgumentException {
		if (issueName == null || message == null)
			throw new IllegalArgumentException("Arguments must not be null!");

		errorDescriptionMap.put(issueName, message);
		errorSeverityMap.put(issueName, severity);
	}

	public String getErrorDescription(String issueName) throws IllegalArgumentException {
		if (!errorDescriptionMap.containsKey(issueName))
			throw new IllegalArgumentException(String.format("No issue description found for the following name: %s", issueName));

		return errorDescriptionMap.get(issueName);
	}

	public IssueSeverity getErrorSeverity(String issueName) throws IllegalArgumentException {
		if (!errorDescriptionMap.containsKey(issueName))
			throw new IllegalArgumentException(String.format("No issue description found for the following name: %s", issueName));

		return errorSeverityMap.get(issueName);
	}

	private void addDataScript(IValidationScript script) throws IllegalArgumentException {
		if (script == null)
			throw new IllegalArgumentException("Argument must not be null!");
		if (dataScripts.contains(script))
			return;
		dataScripts.add(script);
	}

	public Set<IValidationScript> listDataScripts() {
		return dataScripts;
	}

	private void addValidationScript(String scriptType, String tag, IValidationScript script) throws IllegalArgumentException {
		if (scriptType == null || tag == null || script == null)
			throw new IllegalArgumentException("Arguments must not be null!");

		Map<String, Set<IValidationScript>> map = null;
		if (scriptType == XML_SCRIPT) {
			map = xmlScriptMap;
		}
		else if (scriptType == TXT_SCRIPT) {
			map = txtScriptMap;
		}
		else {
			throw new IllegalArgumentException("Unknown script type: " + scriptType);
		}

		Set<IValidationScript> set = map.get(tag);
		if (set == null) {
			set = new HashSet<IValidationScript>();
			map.put(tag, set);
		}

		set.add(script);
	}

	public Set<IValidationScript> getValidationScripts(String scriptType, String tag) {
		if (scriptType == null || tag == null)
			throw new IllegalArgumentException("Arguments must not be null!");

		Map<String, Set<IValidationScript>> map = null;
		if (scriptType == XML_SCRIPT) {
			map = xmlScriptMap;
		}
		else if (scriptType == TXT_SCRIPT) {
			map = txtScriptMap;
		}
		else {
			throw new IllegalArgumentException("Unknown script type: " + scriptType);
		}

		if (!map.containsKey(tag)) {
			return new HashSet<IValidationScript>();
		}

		return new HashSet<IValidationScript>(map.get(tag));
	}

	private void addIgnoredName(String key, String ignoredName) throws IllegalArgumentException {
		if (key == null || ignoredName == null)
			throw new IllegalArgumentException("Arguments must not be null!");

		Set<String> set = ignoredNameMap.get(key);
		if (set == null) {
			set = new HashSet<String>();
			ignoredNameMap.put(key, set);
		}

		set.add(ignoredName);
	}

	public boolean isIgnoredName(String context, String name) {
		if (!ignoredNameMap.containsKey(context))
			return false;
		return ignoredNameMap.get(context).contains(name);
	}

	public List<FilePointer> listDataFiles() {
		return dataFileList;
	}

	public List<FilePointer> listResourceFiles() {
		return resFileList;
	}

	public FilePointer getFile(String innerPath) {
		innerPath = normalize(innerPath);

		if (innerPath.startsWith("data/")) {
			for (FilePointer fi : dataFileList) {
				if (fi.getInnerPath().equals(innerPath))
					return fi;
			}
		}
		else if (innerPath.startsWith("img/") || innerPath.startsWith("audio/") || innerPath.startsWith("fonts/")) {
			for (FilePointer fi : resFileList) {
				if (fi.getInnerPath().equals(innerPath))
					return fi;
			}
		}
		else {
			for (FilePointer fi : dataFileList) {
				if (fi.getInnerPath().equals(innerPath))
					return fi;
			}
			for (FilePointer fi : resFileList) {
				if (fi.getInnerPath().equals(innerPath))
					return fi;
			}
		}

		return null;
	}

	public String getScriptName(IValidationScript ivs) {
		for (Map.Entry<String, IValidationScript> e : loadedScripts.entrySet()) {
			if (e.getValue().equals(ivs))
				return e.getKey();
		}
		return null;
	}

	/*
	 * ===================
	 * NOTE: Loader methods
	 * ===================
	 */

	public void loadIgnoredNames() throws Exception {
		JsonNode root;
		JsonNode currentNode;

		File ignoredFiles = new File("database/ignored_files.json");
		root = ParseUtils.readFileJson(ignoredFiles);
		String[] archiveNames = { "data.dat", "resource.dat" };

		for (String archive : archiveNames) {
			currentNode = root.get(archive);

			for (JsonNode node : currentNode) {
				addIgnoredName(archive, node.asText());
			}
		}

		File ignoredNames = new File("database/ignored_names.json");
		root = ParseUtils.readFileJson(ignoredNames);

		Iterator<Entry<String, JsonNode>> nodeIterator = root.fields();
		while (nodeIterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
			String nodeName = entry.getKey();
			currentNode = entry.getValue();

			for (JsonNode node : currentNode) {
				addIgnoredName(nodeName, node.asText());
			}
		}
	}

	public void loadErrorDescriptions() throws Exception {
		File descriptions = new File("database/error_descriptions.json");
		JsonNode root = ParseUtils.readFileJson(descriptions);

		JsonNode currentNode;
		IssueSeverity currentSeverity;

		String[] severityLevels = { "error", "warn", "info" };
		for (String s : severityLevels) {
			currentNode = root.get(s);
			currentSeverity = IssueSeverity.valueOf(s.toUpperCase());

			Iterator<Entry<String, JsonNode>> nodeIterator = currentNode.fields();
			while (nodeIterator.hasNext()) {
				Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
				addErrorDescription(currentSeverity, entry.getKey(), entry.getValue().asText());
			}
		}
	}

	public void loadScripts(BSHScriptManager scriptManager) throws Exception {
		String scriptsPath = "database/scripts";
		File scriptsFolder = new File(scriptsPath);
		if (!scriptsFolder.exists()) {
			throw new FileNotFoundException(scriptsFolder.getPath());
		}

		loadedScripts = new HashMap<String, IValidationScript>();
		loadScriptsRecursive(scriptManager, loadedScripts, scriptsFolder);

		File validation = new File("database/validation_scripts.json");
		JsonNode root = ParseUtils.readFileJson(validation);

		for (JsonNode node : root.get("preload")) {
			String identifier = normalize(scriptsPath + "/" + node.asText());
			IValidationScript script = (IValidationScript) loadedScripts.get(identifier);

			if (script == null) {
				log.warn(String.format("Could not find data script '%s'.", identifier));
			}
			else {
				addDataScript(script);
				log.trace(String.format("Registered data script '%s'.", identifier));
			}
		}

		Iterator<Entry<String, JsonNode>> nodeIterator = root.get("xml").fields();
		while (nodeIterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();

			for (JsonNode node : entry.getValue()) {
				String identifier = normalize(scriptsPath + "/" + node.asText());
				IValidationScript script = (IValidationScript) loadedScripts.get(identifier);

				if (script == null) {
					log.warn(String.format("Could not find script '%s' declared for entry '%s'.", identifier, entry.getKey()));
				}
				else {
					addValidationScript(XML_SCRIPT, entry.getKey(), script);
					log.trace(String.format("Registered script '%s' for entry '%s'.", identifier, entry.getKey()));
				}
			}
		}

		nodeIterator = root.get("txt").fields();
		while (nodeIterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();

			for (JsonNode node : entry.getValue()) {
				String identifier = normalize(scriptsPath + "/" + node.asText());
				IValidationScript script = (IValidationScript) loadedScripts.get(identifier);

				if (script == null) {
					log.warn(String.format("Could not find script '%s' declared for entry '%s'.", identifier, entry.getKey()));
				}
				else {
					addValidationScript(TXT_SCRIPT, entry.getKey(), script);
					log.trace(String.format("Registered script '%s' for entry '%s'.", identifier, entry.getKey()));
				}
			}
		}
	}

	/*
	 * ===================
	 * NOTE: Helper methods
	 * ===================
	 */

	/**
	 * Determines whether it is safe to read data from the archives.
	 * 
	 * @return true if the database has passed the verification, false otherwise.
	 * 
	 */
	public boolean verify() {
		FilePointer fp = getFile( "img/nullResource.png" );
		InputStream is = null;
		try {
			is = fp.getInputStream();
			return ImageIO.read( is ) != null;
		}
		catch ( Exception e ) {
			return false;
		}
		finally {
			try {
				if ( is != null )
					is.close();
			}
			catch ( Exception e ) {
			}
		}
	}

	/**
	 * Finds unused and undeclared error descriptions.
	 */
	public void debugAssertErrorDescriptions() {
		Pattern ptrnError = Pattern.compile("new Error\\(\"(.*?)\"");
		SortedSet<String> used = new TreeSet<String>();
		used.addAll(errorDescriptionMap.keySet());

		System.out.println("\nUndeclared error descriptions:");
		for (String scriptPath : loadedScripts.keySet()) {
			try {
				File f = new File(scriptPath);
				String content = IOUtils.readFile(f);

				for (String ed : errorDescriptionMap.keySet()) {
					if (!used.contains(ed))
						continue;
					if (content.contains(ed))
						used.remove(ed);
				}
				
				Matcher m = ptrnError.matcher(content);
				while (m.find()) {
					String err = m.group(1);
					if (!errorDescriptionMap.containsKey(err)) {
						System.err.printf("  %s in %s%n", err, scriptPath);
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (used.size() > 0) {
			System.out.println("\nUnused error descriptions:");
			for (String ed : used) {
				System.out.println("  " + ed);
			}
		}
		
		System.out.println();
	}

	private void loadScriptsRecursive(BSHScriptManager scriptManager,
			Map<String, IValidationScript> scriptMap, File cur) throws Exception {
		if (cur.isFile()) {
			String identifier = cur.getPath().replace("\\", "/");
			try {
				NameSpace ns = scriptManager.load(cur);
				scriptMap.put(identifier, scriptManager.getInterface(ns, IValidationScript.class));
				log.trace("Loaded script " + identifier);
			}
			catch (EvalError e) {
				log.error(String.format("Error while evaluating script file '%s'.%n", identifier), e);
			}
		}
		else {
			for (File f : cur.listFiles()) {
				loadScriptsRecursive(scriptManager, scriptMap, f);
			}
		}
	}

	private String normalize(String path) {
		String result = path.replace("\\", "/").replaceAll("/{2,}", "/");
		if (result.startsWith("/"))
			result = result.substring(1);
		return result;
	}
}
