package com.kartoflane.ftl.errorchecker.core;

import com.kartoflane.common.awt.graphics.AWTCache;
import com.kartoflane.common.script.BSHScriptManager;
import com.kartoflane.common.utils.FileManager;
import com.kartoflane.ftl.errorchecker.processing.ValidationManager;


/**
 * A means to pass the numerous managers around, especially to callbacks.
 *
 * This should be preferred over globals.
 * It's not necessary to set every value.
 *
 * It might make sense for an object of limited scope to take context as an
 * arg in its constructor and remember it. But if possible, individual
 * methods should never cache an old context passed to them.
 */
public class CheckerContext {

	protected FileManager fileManager = null;
	protected AWTCache cache = null;
	protected Database database = null;
	protected ValidationManager validator = null;
	protected BSHScriptManager scriptManager = null;

	public CheckerContext() {
	}

	public CheckerContext(FileManager fm, AWTCache ch, Database db, ValidationManager vm, BSHScriptManager sm) {
		fileManager = fm;
		cache = ch;
		database = db;
		validator = vm;
		scriptManager = sm;
	}

	public void setFileManager(FileManager fm) {
		fileManager = fm;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public void setCache(AWTCache ch) {
		cache = ch;
	}

	public AWTCache getCache() {
		return cache;
	}

	public void setDatabase(Database db) {
		database = db;
	}

	public Database getDatabase() {
		return database;
	}

	public void setValidationManager(ValidationManager vm) {
		validator = vm;
	}

	public ValidationManager getValidationManager() {
		return validator;
	}

	public void setScriptManager(BSHScriptManager sm) {
		scriptManager = sm;
	}

	public BSHScriptManager getScriptManager() {
		return scriptManager;
	}
}
