package com.kartoflane.ftl.errorchecker.core;

public class ErrorInstance implements Comparable<ErrorInstance> {

	private final IssueSeverity severity;
	private final String name;
	private final String comment;
	private final int line;
	private final int column;

	public ErrorInstance(IssueSeverity sev, String name, int line, int column) {
		if (sev == null || name == null)
			throw new IllegalArgumentException("Arguments must not be null!");
		severity = sev;
		this.name = name;
		this.comment = null;
		this.line = line;
		this.column = column;
	}

	public ErrorInstance(IssueSeverity sev, String name, String comment, int line, int column) {
		if (sev == null || name == null || comment == null)
			throw new IllegalArgumentException("Arguments must not be null!");
		severity = sev;
		this.name = name;
		this.comment = comment;
		this.line = line;
		this.column = column;
	}

	public IssueSeverity getSeverity() {
		return severity;
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("IssueInfo { ");
		sb.append("severity: ");
		sb.append(severity);
		sb.append(", name: ");
		sb.append(name);
		sb.append(", comment: ");
		sb.append(comment);
		sb.append(", location: ");
		sb.append(line);
		sb.append(";");
		sb.append(column);
		sb.append(" }");

		return sb.toString();
	}

	@Override
	public int compareTo(ErrorInstance o) {
		int result = line - o.line;
		if (result == 0)
			result = severity.compareTo(o.severity);
		if (result == 0)
			result = name.compareTo(o.name);

		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ErrorInstance) {
			ErrorInstance ii = (ErrorInstance) o;
			return severity == ii.severity && name.equals(ii.name) && line == ii.line;
		}
		else
			return false;
	}

	@Override
	public int hashCode() {
		int hash = line * column;
		hash = hash * 31 + severity.hashCode();
		hash = hash * 31 + name.hashCode();
		return hash;
	}
}
