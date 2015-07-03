package com.kartoflane.ftl.errorchecker.ui.components;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.kartoflane.ftl.errorchecker.core.ErrorInstance;


@SuppressWarnings("serial")
public class ErrorTableModel extends DefaultTableModel {

	public static final int COLUMN_LINE = 0;
	public static final int COLUMN_MESSAGE = 1;

	private static String[] columnNames = new String[] { "Line #", "Error Message" };
	private static Class<?>[] columnTypes = new Class<?>[] { Integer.class, ErrorInstance.class };

	private final List<ErrorInstance> rowsList = new ArrayList<ErrorInstance>();

	public ErrorTableModel() {
		super(columnNames, 1);
	}

	public void addItem(ErrorInstance ii) {
		insertItem(rowsList.size(), ii);
	}

	public void insertItem(int row, ErrorInstance ii) {
		if (row == rowsList.size()) {
			rowsList.add(ii);
		}
		else {
			rowsList.add(row, ii);
		}
		fireTableRowsInserted(row, row);
	}

	public void removeItem(int row) {
		rowsList.remove(row);
		fireTableRowsDeleted(row, row);
	}

	public void clear() {
		rowsList.clear();
		fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return rowsList == null ? 0 : rowsList.size();
	}

	public ErrorInstance getItem(int row) {
		return rowsList.get(row);
	}

	@Override
	public Object getValueAt(int row, int column) {
		return rowsList.get(row);
	}

	@Override
	public void setValueAt(Object o, int row, int column) {
		if (o instanceof ErrorInstance) {
			rowsList.set(row, (ErrorInstance) o);
			fireTableRowsUpdated(row, row);
		}
		else {
			throw new IllegalArgumentException("Not an IssueInfo: " + o);
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public Class<?> getColumnClass(int column) {
		return columnTypes[column];
	}
}
