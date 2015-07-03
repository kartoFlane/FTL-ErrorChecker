package com.kartoflane.ftl.errorchecker.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;


@SuppressWarnings("serial")
public class CheckerConfig extends Properties {

	// @formatter:off
	public static final String USE_NATIVE_GUI =	"use_native_gui";
	public static final String FTL_DAT_PATH =	"ftl_dats_path";
	public static final String REM_GEOMETRY =	"remember_geometry";
	public static final String GEOMETRY =		"geometry";

	public static final String UPDATE_DATABASE =	"update_db";
	public static final String UPDATE_APPLICATION =	"update_app";

	public static final String HIDE_EMPTY_FILES =	"hide_empty_files";
	public static final String LOGGING_LEVEL =		"log_level";

	public static final String VIEWER_FONT_STYLE =	"viewer_font_style";
	public static final String VIEWER_FONT_SIZE = 	"viewer_font_size";
	public static final String VIEWER_TAB_SIZE = 	"viewer_tab_size";
	public static final String VIEWER_WRAP_TEXT =	"viewer_wrap";

	public static final String PARSER_MAX_THREADS =		"parser_max_threads";
	public static final String PARSER_TIMEOUT_VAL =		"parser_timeout_val";
	public static final String PARSER_TIMEOUT_UNIT =	"parser_timeout_unit";

	public static final String VALIDATE_MAX_THREADS =	"validate_max_threads";
	public static final String VALIDATE_TIMEOUT_VAL =	"validate_timeout_val";
	public static final String VALIDATE_TIMEOUT_UNIT =	"validate_timeout_unit";

	public static final String FONT_STYLE_DEFAULT =	"System Default";
	// @formatter:on

	private final File configFile;

	public CheckerConfig(File configFile) {
		this.configFile = configFile;
	}

	/**
	 * Returns a copy of an existing CheckerConfig object.
	 */
	public CheckerConfig(CheckerConfig srcConfig) {
		this.configFile = srcConfig.getConfigFile();
		putAll(srcConfig);
	}

	public File getConfigFile() {
		return configFile;
	}

	public int getPropertyAsInt(String key) {
		String s = super.getProperty(key);
		if (s != null && s.matches("^\\d+$"))
			return Integer.parseInt(s);
		else
			return getPropertyDefaultValueInteger(key);
	}

	public boolean getPropertyAsBoolean(String key) {
		String s = super.getProperty(key);
		if (s == null)
			return getPropertyDefaultValueBoolean(key);
		try {
			return Boolean.parseBoolean(s);
		}
		catch (Exception e) {
			return getPropertyDefaultValueBoolean(key);
		}
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		return getProperty(key);
	}

	@Override
	public String getProperty(String key) {
		String s = super.getProperty(key);
		if (s == null) {
			return getPropertyDefaultValueString(key);
		}

		return s;
	}

	public void writeConfig() throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(configFile);
			String configComments = "";
			configComments += "\n";
			configComments += String.format(" %-20s- If true, the program will try to resemble the native GUI. Default: %s.%n",
					USE_NATIVE_GUI, getPropertyDefaultValueBoolean(USE_NATIVE_GUI));
			configComments += String.format(" %-20s- The path to FTL's resources folder. If invalid, you'll be prompted.%n", FTL_DAT_PATH);
			configComments += String.format(" %-20s- If true, window geometry will be saved on exit and restored on startup. Default: %s.%n",
					REM_GEOMETRY, getPropertyDefaultValueBoolean(REM_GEOMETRY));

			configComments += String.format(" %-20s- If a number greater than 0, check for new database every N days. Default: %s.%n",
					UPDATE_DATABASE, getPropertyDefaultValueInteger(UPDATE_DATABASE));
			configComments += String.format(" %-20s- If a number greater than 0, check for newer app versions every N days. Default: %s.%n",
					UPDATE_APPLICATION, getPropertyDefaultValueInteger(UPDATE_APPLICATION));

			configComments += String.format(" %-20s- If true, files that don't have any problems will not be shown in the tree. Default: %s.%n",
					HIDE_EMPTY_FILES, getPropertyDefaultValueBoolean(HIDE_EMPTY_FILES));

			configComments += String.format(" %-20s- The name of the logical font used in the text viewer.%n", VIEWER_FONT_STYLE);
			configComments += String.format(" %-20s- Size of the font used in the viewer.%n", VIEWER_FONT_SIZE);
			configComments += String.format(" %-20s- Indentation level of a single tab character. Default: %s.%n",
					VIEWER_TAB_SIZE, getPropertyDefaultValueInteger(VIEWER_TAB_SIZE));
			configComments += String.format(" %-20s- If true, text in the viewer will be wrapped. Default: %s.%n",
					VIEWER_WRAP_TEXT, getPropertyDefaultValueBoolean(VIEWER_WRAP_TEXT));

			configComments += String.format(" %-20s- Amount of time the parser has to finish parsing all files. Default: %s.%n",
					PARSER_TIMEOUT_VAL, getPropertyDefaultValueInteger(PARSER_TIMEOUT_VAL));
			configComments += String.format(" %-20s- The unit of time used for the above value. Default: %s.%n",
					PARSER_TIMEOUT_UNIT, getPropertyDefaultValueString(PARSER_TIMEOUT_UNIT));

			configComments += String.format(" %-20s- Amount of time the program has to finish validating all files. Default: %s.%n",
					PARSER_TIMEOUT_VAL, getPropertyDefaultValueInteger(VALIDATE_TIMEOUT_VAL));
			configComments += String.format(" %-20s- The unit of time used for the above value. Default: %s.%n",
					PARSER_TIMEOUT_UNIT, getPropertyDefaultValueString(VALIDATE_TIMEOUT_UNIT));

			configComments += "\n";
			configComments += String.format(" %-20s- Last saved position/size/etc of the main window.%n", GEOMETRY);

			OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
			store(writer, configComments);
			writer.flush();
		}
		finally {
			try {
				if (out != null)
					out.close();
			}
			catch (IOException e) {
			}
		}
	}

	public static int getPropertyDefaultValueInteger(String key) {
		if (UPDATE_DATABASE.equals(key)) {
			return 7;
		}
		else if (UPDATE_APPLICATION.equals(key)) {
			return 7;
		}
		else if (VIEWER_FONT_SIZE.equals(key)) {
			return UIManager.getFont("Label.font").getSize();
		}
		else if (VIEWER_TAB_SIZE.equals(key)) {
			return 4;
		}
		else if (PARSER_MAX_THREADS.equals(key)) {
			return 2;
		}
		else if (VALIDATE_MAX_THREADS.equals(key)) {
			return 2;
		}
		else if (PARSER_TIMEOUT_VAL.equals(key)) {
			return 30;
		}
		else if (VALIDATE_TIMEOUT_VAL.equals(key)) {
			return 30;
		}

		throw new IllegalArgumentException("Not an integer property: " + key);
	}

	public static boolean getPropertyDefaultValueBoolean(String key) {
		if (USE_NATIVE_GUI.equals(key) || REM_GEOMETRY.equals(key)) {
			return true;
		}
		else if (HIDE_EMPTY_FILES.equals(key) || VIEWER_WRAP_TEXT.equals(key)) {
			return false;
		}

		throw new IllegalArgumentException("Not a boolean property: " + key);
	}

	public static String getPropertyDefaultValueString(String key) {
		if (FTL_DAT_PATH.equals(key)) {
			return "";
		}
		else if (VIEWER_FONT_STYLE.equals(key)) {
			return FONT_STYLE_DEFAULT;
		}
		else if (PARSER_TIMEOUT_UNIT.equals(key)) {
			return TimeUnit.SECONDS.toString();
		}
		else if (VALIDATE_TIMEOUT_UNIT.equals(key)) {
			return TimeUnit.SECONDS.toString();
		}
		else if (GEOMETRY.equals(key)) {
			return null;
		}
		else if (LOGGING_LEVEL.equals(key)) {
			return "INFO";
		}

		throw new IllegalArgumentException("Not a string property: " + key);
	}
}