package com.kartoflane.ftl.errorchecker.processing;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.JDOMParseException;
import org.jdom2.located.LocatedElement;

import bsh.EvalError;

import com.kartoflane.common.utils.Predicate;
import com.kartoflane.ftl.errorchecker.core.CheckerContext;
import com.kartoflane.ftl.errorchecker.core.Database;
import com.kartoflane.ftl.errorchecker.core.ErrorInstance;
import com.kartoflane.ftl.errorchecker.core.FilePointer;
import com.kartoflane.ftl.errorchecker.core.IssueSeverity;
import com.kartoflane.ftl.errorchecker.ui.components.IconStates;
import com.kartoflane.ftl.errorchecker.utils.ParseUtils;
import com.kartoflane.ftl.layout.FTLLayoutParseException;
import com.kartoflane.ftl.layout.LayoutObject;
import com.kartoflane.ftl.layout.ShipLayout;
import com.kartoflane.ftl.layout.located.LocatedLayoutObject;


public class ValidationManager {

	private static final Logger log = LogManager.getLogger(ValidationManager.class);
	private static final int initialCapacity = 100;

	private final List<ErrorInstance> emptyList = new ArrayList<ErrorInstance>();

	private final Database db;

	private Map<String, LocatedElement> fileXMLMap;
	private Map<String, ShipLayout> fileLayoutMap;
	private Map<String, List<ErrorInstance>> fileIssueMap;
	private Map<String, List<ErrorInstance>> fileIssueViewMap;

	public ValidationManager(Database db) {
		this.db = db;
	}

	public void init() {
		fileXMLMap = new HashMap<String, LocatedElement>(initialCapacity);
		fileLayoutMap = new HashMap<String, ShipLayout>(initialCapacity);
		fileIssueMap = new HashMap<String, List<ErrorInstance>>(initialCapacity);
		fileIssueViewMap = new HashMap<String, List<ErrorInstance>>(initialCapacity);
	}

	public void clear() {
		if (!isInitialized())
			return;

		fileXMLMap.clear();
		fileXMLMap = null;

		fileLayoutMap.clear();
		fileLayoutMap = null;

		for (List<ErrorInstance> list : fileIssueMap.values())
			list.clear();
		fileIssueMap.clear();
		fileIssueMap = null;

		fileIssueViewMap.clear();
		fileIssueViewMap = null;

		System.gc();
	}

	public boolean isInitialized() {
		return fileXMLMap != null;
	}

	/*
	 * ========================
	 * NOTE: Data access methods
	 * ========================
	 */

	public List<Element> findXMLElementsMatching(Predicate<Element> filter) {
		List<Element> result = new ArrayList<Element>();

		for (Element e : fileXMLMap.values()) {
			findXMLRecursive(e, filter, result);
		}

		return result;
	}

	private void findXMLRecursive(Element current, Predicate<Element> filter, List<Element> list) {
		if (filter == null || filter.accept(current)) {
			list.add(current);
		}

		for (Element e : current.getChildren()) {
			findXMLRecursive(e, filter, list);
		}
	}

	private synchronized void storeLayout(String innerPath, ShipLayout layout) throws IllegalArgumentException {
		if (innerPath == null || layout == null)
			throw new IllegalArgumentException("Arguments must not be null!");

		fileLayoutMap.put(innerPath, layout);
	}
	
	public boolean hasLayout(String innerPath) {
		return fileLayoutMap.containsKey(innerPath);
	}

	public ShipLayout getLayout(String innerPath) {
		if (!fileLayoutMap.containsKey(innerPath))
			throw new IllegalArgumentException(String.format("Attempted to retrieve layout for file '%s', but none was found!", innerPath));
		return fileLayoutMap.get(innerPath);
	}

	private synchronized void storeRootXMLElement(String innerPath, LocatedElement element) throws IllegalArgumentException {
		if (innerPath == null || element == null)
			throw new IllegalArgumentException("Arguments must not be null!");

		fileXMLMap.put(innerPath, element);
	}

	public LocatedElement getRootXMLElement(String innerPath) {
		return fileXMLMap.get(innerPath);
	}

	private synchronized void storeIssue(String innerPath, ErrorInstance ii) throws IllegalArgumentException {
		if (innerPath == null || ii == null)
			throw new IllegalArgumentException("Arguments must not be null!");

		List<ErrorInstance> issueList = fileIssueMap.get(innerPath);
		if (issueList == null) {
			issueList = new ArrayList<ErrorInstance>();
			fileIssueMap.put(innerPath, issueList);
		}

		issueList.add(ii);
	}

	public List<ErrorInstance> listIssues(String innerPath) throws IllegalArgumentException {
		if (innerPath == null)
			throw new IllegalArgumentException("Argument must not be null!");
		if (!fileIssueMap.containsKey(innerPath))
			return emptyList;

		List<ErrorInstance> result = fileIssueViewMap.get(innerPath);
		if (result == null) {
			result = Collections.unmodifiableList(fileIssueMap.get(innerPath));
			fileIssueViewMap.put(innerPath, result);
		}

		return result;
	}

	public IconStates getHighestSeverity(FilePointer fi) {
		if (db.isIgnoredName(fi.getPack().getName(), fi.getName()))
			return IconStates.EMPTY;

		String innerPath = fi.getInnerPath();
		if (!fileXMLMap.containsKey(innerPath) && !fileLayoutMap.containsKey(innerPath)) {
			return IconStates.NOTPARSED;
		}
		else if (!fileIssueMap.containsKey(innerPath)) {
			return IconStates.EMPTY;
		}
		else {
			List<ErrorInstance> list = fileIssueMap.get(innerPath);
			if (list == null || list.isEmpty()) {
				return IconStates.EMPTY;
			}

			IssueSeverity severity = IssueSeverity.INFO;
			for (ErrorInstance ii : list) {
				if (ii == null) {
					throw new RuntimeException("Issue list contains a null reference: threading error.");
				}

				if (severity.compareTo(ii.getSeverity()) < 0) {
					severity = ii.getSeverity();
				}
			}

			if (severity == IssueSeverity.ERROR) {
				return IconStates.ERROR;
			}
			else if (severity == IssueSeverity.WARN) {
				return IconStates.WARNING;
			}
			else {
				return IconStates.INFO;
			}
		}
	}

	/*
	 * ===================
	 * NOTE: Loader methods
	 * ===================
	 */

	public void loadXML(FilePointer fi) throws IOException, JDOMParseException {
		InputStream is = null;
		try {
			is = fi.getInputStream();
			Document doc = ParseUtils.readStreamXML(is);

			if (doc != null) {
				LocatedElement root = (LocatedElement) doc.getRootElement();
				storeRootXMLElement(fi.getInnerPath(), root);
			}
		}
		finally {
			if (is != null)
				is.close();
		}
	}

	public void loadTXT(FilePointer fi) throws IOException, FTLLayoutParseException {
		InputStream is = null;
		try {
			is = fi.getInputStream();
			ShipLayout layout = ParseUtils.readStreamFTLLayout(is);

			if (layout != null) {
				storeLayout(fi.getInnerPath(), layout);
			}
		}
		finally {
			if (is != null)
				is.close();
		}
	}

	/*
	 * =======================
	 * NOTE: Validation methods
	 * =======================
	 */

	public void runDataScripts(CheckerContext context) {
		for (IValidationScript dataScript : db.listDataScripts()) {
			if (Thread.currentThread().isInterrupted()) {
				return;
			}

			try {
				dataScript.cacheData(context);
			}
			catch (EvalError e) {
				log.error("Error occured while executing data script callback: " + db.getScriptName(dataScript), e);
			}
		}
	}

	private ErrorInstance constructIssue(Error error, int line, int col) {
		if (error.hasComment()) {
			return new ErrorInstance(db.getErrorSeverity(error.getErrorId()), error.getErrorId(), error.getComment(), line, col);
		}
		else {
			return new ErrorInstance(db.getErrorSeverity(error.getErrorId()), error.getErrorId(), line, col);
		}
	}

	/**
	 * 
	 * @param fi
	 *            the file in which the element is located
	 * @param e
	 *            the currently validated xml element
	 */
	public void validateElement(CheckerContext context, FilePointer fi, Object e) {
		if (e == null)
			throw new IllegalArgumentException("Argument must not be null!");

		if (e instanceof LocatedElement == false && e instanceof LocatedLayoutObject == false) {
			throw new IllegalArgumentException("Argument must be either a LocatedElement or LocatedLayoutObject: " + e);
		}

		List<IValidationScript> validationScripts = new ArrayList<IValidationScript>();

		if (e instanceof LocatedElement) {
			LocatedElement le = (LocatedElement) e;
			String nameAttribute = le.getAttributeValue("name");

			if (nameAttribute != null && db.isIgnoredName(le.getName(), nameAttribute)) {
				return;
			}

			validationScripts.addAll(db.getValidationScripts(Database.XML_SCRIPT, Database.ALL_TAG));
			validationScripts.addAll(db.getValidationScripts(Database.XML_SCRIPT, le.getName()));

			for (IValidationScript script : validationScripts) {
				try {
					Set<Error> result = script.validate(context, le);
					for (Error er : result) {
						storeIssue(fi.getInnerPath(), constructIssue(er, le.getLine(), le.getColumn()));
					}
				}
				catch (Exception ex) {
					log.error("Error occured while executing script callback: " + db.getScriptName(script), ex);
				}
			}

			for (Element element : le.getChildren()) {
				validateElement(context, fi, (LocatedElement) element);
			}
		}
		else if (e instanceof LayoutObject) {
			LayoutObject lo = (LayoutObject) e;

			validationScripts.addAll(db.getValidationScripts(Database.TXT_SCRIPT, Database.ALL_TAG));
			validationScripts.addAll(db.getValidationScripts(Database.TXT_SCRIPT, lo.getType().toString()));

			for (IValidationScript script : validationScripts) {
				try {
					Set<Error> result = script.validate(context, lo);
					for (Error er : result) {
						int line = lo instanceof LocatedLayoutObject ? ((LocatedLayoutObject) lo).getLine() : -1;
						storeIssue(fi.getInnerPath(), constructIssue(er, line, 0));
					}
				}
				catch (Exception ex) {
					log.error("Error occured while executing script callback: " + db.getScriptName(script), ex);
				}
			}
		}
	}
}
