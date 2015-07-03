package com.kartoflane.ftl.errorchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.vhati.modmanager.core.ComparableVersion;
import net.vhati.modmanager.core.FTLUtilities;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.kartoflane.common.awt.graphics.AWTCache;
import com.kartoflane.common.script.BSHScriptManager;
import com.kartoflane.common.swing.SwingUIUtils;
import com.kartoflane.common.utils.FileManager;
import com.kartoflane.ftl.errorchecker.core.CheckerConfig;
import com.kartoflane.ftl.errorchecker.core.CheckerContext;
import com.kartoflane.ftl.errorchecker.core.Database;
import com.kartoflane.ftl.errorchecker.processing.ValidationManager;
import com.kartoflane.ftl.errorchecker.ui.CheckerFrame;


public class ErrorChecker {

	private static final Logger log = LogManager.getLogger(ErrorChecker.class);

	public static final String APP_NAME = "FTL Error Checker";
	public static final ComparableVersion APP_VERSION = new ComparableVersion("0.2 alpha");
	public static final String APP_URL = "http://www.google.com/";
	public static final String APP_AUTHOR = "kartoFlane";

	public static void main(String[] args) {
		// Ensure all popups are triggered from the event dispatch thread
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				guiInit();
			}
		});
	}

	private static void guiInit() {
		log.info(String.format("%s v%s", APP_NAME, APP_VERSION));
		log.info(String.format("%s %s", System.getProperty("os.name"), System.getProperty("os.version")));
		log.info(String.format("%s, %s, %s", System.getProperty("java.vm.name"), System.getProperty("java.version"), System.getProperty("os.arch")));
		System.out.println();

		if (new File("./database/").exists() == false) {
			String currentPath = new File(".").getAbsoluteFile().getParentFile().getAbsolutePath();

			SwingUIUtils.showErrorDialog(String.format("ErrorChecker could not find its own folder.\n" +
					"Currently in: %s\n\nRun one of the following instead of the jar...\n" +
					"Windows: checker.exe\nLinux/OSX: checker.command or checker-cli.sh", currentPath));
			log.error(String.format("ErrorChecker could not find its own folder (Currently in \"%s\"), exiting.", currentPath));
			System.exit(1);
		}

		File configFile = new File("checker.cfg");
		CheckerConfig config = new CheckerConfig(configFile);

		// Read the config file.
		InputStream in = null;
		try {
			if (configFile.exists()) {
				log.trace("Loading properties from config file.");
				in = new FileInputStream(configFile);
				config.load(new InputStreamReader(in, "UTF-8"));
			}
		}
		catch (IOException e) {
			log.error("Error loading config.", e);
			SwingUIUtils.showErrorDialog("Error loading config from " + configFile.getPath());
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (IOException e) {
			}
		}

		// Setup logging level (override log4j2.xml since we allow the user to select logging level)
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration logCfg = ctx.getConfiguration();
		LoggerConfig loggerConfig = logCfg.getLoggerConfig("com.kartoflane");
		loggerConfig.setLevel(Level.valueOf(config.getProperty(CheckerConfig.LOGGING_LEVEL)));
		ctx.updateLoggers();

		// Setup look-and-Feel
		boolean useNativeGUI = config.getPropertyAsBoolean(CheckerConfig.USE_NATIVE_GUI);

		if (useNativeGUI) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				log.trace("Using system Look and Feel");
			}
			catch (Exception e) {
				log.error("Error setting system Look and Feel.", e);
			}
		}

		// FTL Resources Path.
		File datsDir = null;
		String datsPath = config.getProperty(CheckerConfig.FTL_DAT_PATH);

		if (datsPath.length() > 0) {
			log.info("Using FTL dats path from config: " + datsPath);
			datsDir = new File(datsPath);
			if (FTLUtilities.isDatsDirValid(datsDir) == false) {
				log.error("The config's ftl_dats_path does not exist, or it lacks data.dat.");
				datsDir = null;
			}
		}
		else {
			log.trace("No FTL dats path previously set.");
		}

		// Find/prompt for the path to set in the config.
		if (datsDir == null) {
			datsDir = FTLUtilities.findDatsDir();
			if (datsDir != null) {
				int response = JOptionPane.showConfirmDialog(null, "FTL resources were found in:\n" +
						datsDir.getPath() + "\nIs this correct?", "Confirm",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION)
					datsDir = null;
			}

			if (datsDir == null) {
				log.trace("FTL dats path was not located automatically. Prompting user for location.");
				datsDir = FTLUtilities.promptForDatsDir(null);
			}

			if (datsDir != null) {
				config.setProperty(CheckerConfig.FTL_DAT_PATH, datsDir.getAbsolutePath());
				log.info("FTL dats located at: " + datsDir.getAbsolutePath());
			}
		}

		if (datsDir == null) {
			SwingUIUtils.showErrorDialog("FTL resources were not found.\nThe ErrorChecker will now exit.");
			log.trace("No FTL dats path found, exiting.");
			System.exit(1);
		}

		FileManager fileManager = new FileManager();
		Database db = new Database();
		CheckerContext context = new CheckerContext(fileManager, new AWTCache(fileManager), db, new ValidationManager(db), new BSHScriptManager(false));

		try {
			CheckerFrame frame = new CheckerFrame(config, context);
			frame.init();
			frame.setVisible(true);
		}
		catch (Exception e) {
			log.error("Exception while creating CheckerFrame.", e);
			System.exit(1);
		}
	}
}
