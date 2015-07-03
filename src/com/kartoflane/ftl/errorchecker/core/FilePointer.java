package com.kartoflane.ftl.errorchecker.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.vhati.ftldat.FTLDat.FTLPack;


/**
 * This class is a pointer to files stored inside an FTLPack.
 * 
 * @author kartoFlane
 *
 */
public class FilePointer {

	private final FTLPack pack;
	private final String innerPath;

	public FilePointer(FTLPack pack, String innerPath) throws IOException {
		if (pack == null || innerPath == null)
			throw new IllegalArgumentException("Arguments must not be null!");
		if (!pack.contains(innerPath))
			throw new IllegalArgumentException("Archive does not contain the specified innerPath:" + innerPath);

		innerPath = innerPath.replace("\\", "/");

		this.pack = pack;
		this.innerPath = innerPath;

		if (!pack.contains(innerPath))
			throw new FileNotFoundException(String.format("The FTLPack did not contain the specified innerPath: %s", innerPath));
	}

	public String getInnerPath() {
		return innerPath;
	}

	public String getName() {
		int index = innerPath.lastIndexOf('/');
		return innerPath.substring(index + 1);
	}

	public FTLPack getPack() {
		return pack;
	}

	public long getSize() {
		try {
			return pack.getSize(innerPath);
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException("Archive does not contain the specified innerPath: " + innerPath + "\n" +
					"Archive was modified after the pointer was created?");
		}
	}

	/**
	 * Returns an InputStream to this file in the dat archive.
	 * 
	 * Remember to close the stream after you're done with it.
	 */
	public InputStream getInputStream() throws FileNotFoundException, IOException {
		return pack.getInputStream(innerPath);
	}

	public boolean isXML() {
		return innerPath.endsWith(".xml");
	}

	public boolean isTXT() {
		return innerPath.endsWith(".txt");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((innerPath == null) ? 0 : innerPath.hashCode());
		result = prime * result + ((pack == null) ? 0 : pack.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FilePointer))
			return false;
		FilePointer other = (FilePointer) obj;
		if (innerPath == null) {
			if (other.innerPath != null)
				return false;
		}
		else if (!innerPath.equals(other.innerPath))
			return false;
		if (pack == null) {
			if (other.pack != null)
				return false;
		}
		else if (!pack.equals(other.pack))
			return false;
		return true;
	}
}
