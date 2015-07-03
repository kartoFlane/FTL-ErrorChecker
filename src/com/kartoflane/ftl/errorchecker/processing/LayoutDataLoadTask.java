package com.kartoflane.ftl.errorchecker.processing;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kartoflane.ftl.errorchecker.core.FilePointer;
import com.kartoflane.ftl.layout.FTLLayoutParseException;


public class LayoutDataLoadTask implements Runnable {

	private static final Logger log = LogManager.getLogger(LayoutDataLoadTask.class);

	private final FilePointer fi;
	private final ValidationManager vm;

	public LayoutDataLoadTask(FilePointer fileInfo, ValidationManager vm) {
		fi = fileInfo;
		this.vm = vm;
	}

	@Override
	public void run() {
		try {
			vm.loadTXT(fi);
			log.trace(String.format("Parsed %s", fi.getInnerPath()));
		}
		catch (FileNotFoundException e) {
			log.error(String.format("File '%s' could not be found in the archive '%s'", fi.getInnerPath(), fi.getPack().getName()));
		}
		catch (IOException e) {
			log.error(String.format("An IOException occured while parsing '%s'", fi.getInnerPath()), e);
		}
		catch (FTLLayoutParseException e) {
			log.error(String.format("Error while parsing '%s' at line %s:%n", fi.getInnerPath(), e.getLine()), e);
		}
	}
}