package com.kartoflane.ftl.errorchecker.processing;

/**
 * A simple wrapper class to associate two strings together.
 */
public class Error {

	private final String errorId;
	private final String comment;

	public Error(String errorId) {
		this.errorId = errorId;
		this.comment = null;
	}

	public Error(String errorId, String comment) {
		this.errorId = errorId;
		this.comment = comment;
	}

	public String getErrorId() {
		return errorId;
	}

	public String getComment() {
		return comment;
	}

	public boolean hasComment() {
		return comment != null && !"".equals(comment.trim());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((errorId == null) ? 0 : errorId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Error))
			return false;
		Error other = (Error) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		}
		else if (!comment.equals(other.comment))
			return false;
		if (errorId == null) {
			if (other.errorId != null)
				return false;
		}
		else if (!errorId.equals(other.errorId))
			return false;
		return true;
	}
}
