package com.kartoflane.ftl.errorchecker.ui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import com.kartoflane.common.awt.graphics.AWTCache;
import com.kartoflane.ftl.errorchecker.core.CheckerContext;
import com.kartoflane.ftl.errorchecker.core.Database;
import com.kartoflane.ftl.errorchecker.core.ErrorInstance;
import com.kartoflane.ftl.errorchecker.core.IssueSeverity;


@SuppressWarnings("serial")
public class ErrorTableRenderer extends DefaultTableCellRenderer {

	private static final Color colorError = new Color(255, 110, 100);
	private static final Color colorWarn = new Color(255, 232, 127);

	private final Database db;
	private final AWTCache cache;

	private Icon iconError;
	private Icon iconWarn;
	private Icon iconInfo;

	public ErrorTableRenderer(CheckerContext context) {
		db = context.getDatabase();
		cache = context.getCache();

		iconError = new ImageIcon(cache.checkOutImage(this, "cpath://assets/error.png"));
		iconWarn = new ImageIcon(cache.checkOutImage(this, "cpath://assets/warning.png"));
		iconInfo = new ImageIcon(cache.checkOutImage(this, "cpath://assets/info.png"));

		setHorizontalAlignment(SwingConstants.LEFT);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {

		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		ErrorInstance ii = (ErrorInstance) value;
		IssueSeverity severity = ii.getSeverity();

		Icon icon = null;
		Color color = null;

		if (severity == IssueSeverity.ERROR) {
			icon = iconError;
			color = colorError;
		}
		else if (severity == IssueSeverity.WARN) {
			icon = iconWarn;
			color = colorWarn;
		}
		else if (severity == IssueSeverity.INFO) {
			icon = iconInfo;
			color = null;
		}

		if (column == 0) {
			setText("" + ii.getLine());
			setIcon(icon);
		}
		else if (column == 1) {
			String text = db.getErrorDescription(ii.getName());
			if (ii.getComment() != null)
				text += " (" + ii.getComment() + ")";
			setText(text);
			setIcon(null);
		}

		if (isSelected) {
			setBackground(UIManager.getDefaults().getColor("Table.selectionBackground"));
		}
		else {
			setBackground(color);
		}

		return this;
	}

	public void dispose() {
		iconError = null;
		iconWarn = null;
		iconInfo = null;

		cache.checkInImage(this, "cpath://assets/error.png");
		cache.checkInImage(this, "cpath://assets/warning.png");
		cache.checkInImage(this, "cpath://assets/info.png");
	}
}
