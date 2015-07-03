package com.kartoflane.ftl.errorchecker.processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.located.LocatedElement;

import com.kartoflane.common.utils.TimeUtils;
import com.kartoflane.ftl.errorchecker.core.CheckerConfig;
import com.kartoflane.ftl.errorchecker.core.CheckerContext;
import com.kartoflane.ftl.errorchecker.core.Database;
import com.kartoflane.ftl.errorchecker.core.FilePointer;
import com.kartoflane.ftl.layout.LayoutObject;
import com.kartoflane.ftl.layout.ShipLayout;


public class ProcessingThread extends Thread {

	private static final Logger log = LogManager.getLogger(ProcessingThread.class);

	private final CheckerConfig config;
	private final CheckerContext context;
	private final List<FilePointer> fileList;
	private final TaskObserver observer;

	public ProcessingThread(CheckerConfig config, CheckerContext context, TaskObserver observer) {
		this.config = config;
		this.context = context;
		this.fileList = context.getDatabase().listDataFiles();
		this.observer = observer;

		this.setDaemon(true);
	}

	@Override
	public void run() {
		Database db = context.getDatabase();
		ValidationManager vm = context.getValidationManager();

		String parseMsg = "Parsing pending";
		String validateMsg = "Validation pending";
		observer.taskStatus(parseMsg + "\n" + validateMsg);
		observer.taskProgress(-1, -1);

		boolean success = false;
		int parserMaxThreads = config.getPropertyAsInt(CheckerConfig.PARSER_MAX_THREADS);
		int validateMaxThreads = config.getPropertyAsInt(CheckerConfig.VALIDATE_MAX_THREADS);
		PriorityThreadFactory threadFactory = new PriorityThreadFactory(true, Thread.NORM_PRIORITY);

		try {
			log.info("Parsing XML files...");

			if (!db.verify()) {
				log.error( "Game's archives have been modified. Parsing aborted." );
				observer.taskStatus( "Game archives have been modified. Please reload the database and try again." );
				observer.taskFinished( false, null );
				observer.taskProgress( 0, 100 );
				return;
			}
			
			parseMsg = "Parsing in progress...";
			observer.taskStatus(parseMsg + "\n" + validateMsg);

			long time = System.currentTimeMillis();
			ExecutorService executorService = Executors.newFixedThreadPool(parserMaxThreads, threadFactory);

			for (FilePointer fi : fileList) {
				if (!db.isIgnoredName(fi.getPack().getName(), fi.getName())) {
					if (fi.isXML()) {
						executorService.execute(new XMLDataLoadTask(fi, vm));
					}
					else if (fi.isTXT()) {
						executorService.execute(new LayoutDataLoadTask(fi, vm));
					}
				}
			}

			executorService.shutdown();
			try {
				int parserTimeVal = config.getPropertyAsInt(CheckerConfig.PARSER_TIMEOUT_VAL);
				TimeUnit parserTimeUnit = TimeUnit.valueOf(config.getProperty(CheckerConfig.PARSER_TIMEOUT_UNIT));

				if (executorService.awaitTermination(parserTimeVal, parserTimeUnit)) {
					time = System.currentTimeMillis() - time;
					parseMsg = String.format("Parsing finished successfully! Time: %s", TimeUtils.formatMilisToTimeSpan(time));
					log.info(parseMsg);
					observer.taskStatus(parseMsg + "\n" + validateMsg);

					success = true;
				}
				else {
					parseMsg = String.format("Parsing timed out (%s %s)", parserTimeVal, parserTimeUnit.toString().toLowerCase());
					log.error(parseMsg);
					executorService.shutdownNow();
					observer.taskStatus(parseMsg + "\n" + validateMsg);
				}
			}
			catch (InterruptedException e) {
				log.info("Parsing aborted by user.");
				parseMsg = "Parsing aborted.";
				observer.taskStatus(parseMsg);
				return;
			}
			catch (Exception e) {
				log.error("An unknown exception has occured while parsing.", e);
			}

			/*
			 * ==============================================================
			 */

			if (success) {
				success = false;

				log.info("Executing cache scripts...");
				validateMsg = "Executing cache scripts...";
				observer.taskStatus(parseMsg + "\n" + validateMsg);

				vm.runDataScripts(context);

				log.info("Validating files...");
				validateMsg = "Validation in progress...";
				observer.taskStatus(parseMsg + "\n" + validateMsg);

				time = System.currentTimeMillis();
				executorService = Executors.newFixedThreadPool(validateMaxThreads, threadFactory);

				for (FilePointer fi : fileList) {
					if (!db.isIgnoredName(fi.getPack().getName(), fi.getName())) {
						if (fi.isXML()) {
							Map<NamedElement, Element> pending = new HashMap<NamedElement, Element>();
							LocatedElement wrapper = vm.getRootXMLElement(fi.getInnerPath());
							for (Element element : wrapper.getChildren()) {
								pending.put(new NamedElement(element.getAttributeValue("name"), element), element);
							}

							for (Element element : pending.values()) {
								executorService.execute(new ValidateTask<Element>(fi, element, context));
							}
							pending.clear();
						}
						else if (fi.isTXT()) {
							ShipLayout layout = vm.getLayout(fi.getInnerPath());
							for (LayoutObject lo : layout.listLayoutObjects()) {
								executorService.execute(new ValidateTask<LayoutObject>(fi, lo, context));
							}
						}
					}
				}

				executorService.shutdown();
				try {
					int validateTimeVal = config.getPropertyAsInt(CheckerConfig.VALIDATE_TIMEOUT_VAL);
					TimeUnit validateTimeUnit = TimeUnit.valueOf(config.getProperty(CheckerConfig.VALIDATE_TIMEOUT_UNIT));

					if (executorService.awaitTermination(validateTimeVal, validateTimeUnit)) {
						time = System.currentTimeMillis() - time;
						validateMsg = String.format("Validation finished successfully! Time: %s", TimeUtils.formatMilisToTimeSpan(time));
						log.info(validateMsg);
						observer.taskStatus(parseMsg + "\n" + validateMsg);

						success = true;
					}
					else {
						validateMsg = String.format("Validation timed out (%s %s)", validateTimeVal, validateTimeUnit.toString().toLowerCase());
						log.error(validateMsg);
						executorService.shutdownNow();
						observer.taskStatus(parseMsg + "\n" + validateMsg);
					}
				}
				catch (InterruptedException e) {
					log.info("Validation aborted by user.");
					validateMsg = "Validation aborted.";
					observer.taskStatus(parseMsg + "\n" + validateMsg);
					return;
				}
				catch (Exception e) {
					log.error("An unknown exception has occured while validating.", e);
				}

				if (!success) {
					validateMsg = "Validation phase failed.";
					observer.taskStatus(parseMsg + "\n" + validateMsg);
				}
			}
			else {
				observer.taskStatus("Parsing phase failed.");
			}
		}
		catch (Exception ex) {
			log.error("An exception occured during processing.", ex);
			observer.taskStatus("An error has occured: " + ex.getMessage() + "\nCheck log for details.");
		}

		observer.taskFinished(success, null);
		observer.taskProgress(success ? 100 : 0, 100);
		
		System.gc();
		log.trace("Finished.");
	}

	private static class NamedElement {
		public final String name;
		public final Element element;

		NamedElement(String name, Element element) {
			this.name = name;
			this.element = element;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof NamedElement) {
				NamedElement e = (NamedElement)o;
				if (element.getName().equals(e.element.getName()) &&
						name != null && e.name != null) {
					return name.equals(e.name);
				} else {
					return element.equals(e.element);
				}
			} else {
				return false;
			}
		}
		
		public int hashCode() {
			return name == null ? element.hashCode() : name.hashCode();
		}
	}

	/**
	 * Copy of {@link Executors.DefaultThreadFactory}, but with adjustable thread priority and daemon status.
	 */
	private static class PriorityThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		private final boolean threadDaemon;
		private final int threadPriority;

		PriorityThreadFactory(boolean daemon, int priority) {
			if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY)
				throw new IllegalArgumentException("Priority not within allowed range: " + priority);
			threadDaemon = daemon;
			threadPriority = priority;
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() :
					Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" +
					poolNumber.getAndIncrement() +
					"-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,
					namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon() != threadDaemon)
				t.setDaemon(threadDaemon);
			if (t.getPriority() != threadPriority)
				t.setPriority(threadPriority);
			return t;
		}
	}
}
