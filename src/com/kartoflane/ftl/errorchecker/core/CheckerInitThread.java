package com.kartoflane.ftl.errorchecker.core;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kartoflane.ftl.errorchecker.ui.CheckerFrame;


/**
 * Performs I/O-related setup for ManagerFrame in the background.
 *
 * Reads cached local metadata.
 * Rescans the "mods/" folder.
 * Reads saved catalog, and redownloads if stale.
 * Reads saved info about app updates, and redownloads if stale.
 */
public class CheckerInitThread extends Thread {

	private static final Logger log = LogManager.getLogger(CheckerInitThread.class);

	private final CheckerFrame frame;
	private final CheckerConfig config;

	public CheckerInitThread(CheckerFrame frame, CheckerConfig config) {
		this.frame = frame;
		this.config = config;

		this.setDaemon(true);
	}

	@Override
	public void run() {
		try {
			init();
		}
		catch (Exception e) {
			log.error("Error during CheckerFrame init.", e);
		}
	}

	private void init() throws InterruptedException {
		Lock checkerLock = frame.getLock();
		checkerLock.lock();
		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.rescanData();
				}
			});

			// Wait until notified that "mods/" has been scanned.
			while (frame.isWorking()) {
				frame.getScanEndedCondition().await();
			}
		}
		finally {
			checkerLock.unlock();
		}

		/*
		 * int dbUpdateInterval = config.getPropertyAsInt(CheckerConfig.UPDATE_DATABASE, 0);
		 * boolean needNewDatabase = false;
		 * if (databaseFile.exists()) {
		 * // Load the catalog first, before updating.
		 * reloadCatalog();
		 * if (catalogUpdateInterval > 0) {
		 * // Check if the downloaded catalog is stale.
		 * if (isFileStale(catalogFile, catalogUpdateInterval)) {
		 * log.debug(String.format("Catalog is older than %d days.", catalogUpdateInterval));
		 * needNewCatalog = true;
		 * } else {
		 * log.debug("Catalog isn't stale yet.");
		 * }
		 * }
		 * }
		 * else {
		 * // Catalog file doesn't exist.
		 * needNewDatabase = true;
		 * }
		 * // Don't update if the user doesn't want to.
		 * if (dbUpdateInterval <= 0)
		 * needNewDatabase = false;
		 * if (needNewDatabase) {
		 * boolean fetched = URLFetcher.refetchURL(CheckerFrame.CATALOG_URL, catalogFile, catalogETagFile);
		 * if (fetched && catalogFile.exists()) {
		 * reloadCatalog();
		 * }
		 * }
		 * int appUpdateInterval = config.getPropertyAsInt(CheckerConfig.UPDATE_APPLICATION, 0);
		 * boolean needAppUpdate = false;
		 * if (appUpdateFile.exists()) {
		 * // Load the info first, before downloading.
		 * reloadAppUpdateInfo();
		 * if (appUpdateInterval > 0) {
		 * // Check if the app update info is stale.
		 * if (isFileStale(appUpdateFile, appUpdateInterval)) {
		 * log.debug(String.format("App update info is older than %d days.", appUpdateInterval));
		 * needAppUpdate = true;
		 * } else {
		 * log.debug("App update info isn't stale yet.");
		 * }
		 * }
		 * }
		 * else {
		 * // App update file doesn't exist.
		 * needAppUpdate = true;
		 * }
		 * // Don't update if the user doesn't want to.
		 * if (appUpdateInterval <= 0)
		 * needAppUpdate = false;
		 * if (needAppUpdate) {
		 * boolean fetched = URLFetcher.refetchURL(ManagerFrame.APP_UPDATE_URL, appUpdateFile, appUpdateETagFile);
		 * if (fetched && appUpdateFile.exists()) {
		 * reloadAppUpdateInfo();
		 * }
		 * }
		 */
	}

	/**
	 * Returns true if a file is older than N days.
	 */
	private boolean isFileStale(File f, int maxDays) {
		Date modifiedDate = new Date(f.lastModified());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, maxDays * -1);
		return modifiedDate.before(cal.getTime());
	}
}
