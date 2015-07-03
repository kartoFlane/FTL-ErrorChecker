package com.kartoflane.ftl.errorchecker.processing;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.input.JDOMParseException;

import com.kartoflane.ftl.errorchecker.core.FilePointer;


public class XMLDataLoadTask implements Runnable {

	private static final Logger log = LogManager.getLogger(XMLDataLoadTask.class);

	private final FilePointer fi;
	private final ValidationManager vm;

	public XMLDataLoadTask(FilePointer fileInfo, ValidationManager vm) {
		fi = fileInfo;
		this.vm = vm;
	}

	@Override
	public void run() {
		try {
			vm.loadXML(fi);
			log.trace(String.format("Parsed %s", fi.getInnerPath()));
		}
		catch (FileNotFoundException e) {
			log.error(String.format("File '%s' could not be found in the archive '%s'", fi.getInnerPath(), fi.getPack().getName()));
		}
		catch (IOException e) {
			log.error(String.format("An IOException occured while parsing '%s'", fi.getInnerPath()), e);
		}
		catch (JDOMParseException e) {
			log.error(String.format("Error occured while parsing '%s', at line %s:", fi.getInnerPath(), e.getLineNumber()), e);
		}
	}
}
