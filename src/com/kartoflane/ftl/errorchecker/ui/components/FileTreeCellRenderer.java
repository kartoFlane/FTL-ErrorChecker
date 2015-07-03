package com.kartoflane.ftl.errorchecker.ui.components;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.kartoflane.common.awt.graphics.AWTCache;
import com.kartoflane.ftl.errorchecker.ui.components.FileTreeModel.FileTreeNode;


@SuppressWarnings("serial")
public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

	private AWTCache cache = null;

	private Icon leafIconError;
	private Icon leafIconWarning;
	private Icon leafIconInfo;
	private Icon leafIconFailed;
	private Icon leafIconEmpty;

	public FileTreeCellRenderer(AWTCache cache) {
		if (cache == null)
			throw new IllegalArgumentException("Cache is null.");

		this.cache = cache;

		leafIconError = new ImageIcon(cache.checkOutImage(this, "cpath://assets/error.png"));
		leafIconWarning = new ImageIcon(cache.checkOutImage(this, "cpath://assets/warning.png"));
		leafIconInfo = new ImageIcon(cache.checkOutImage(this, "cpath://assets/info.png"));
		leafIconFailed = new ImageIcon(cache.checkOutImage(this, "cpath://assets/notparsed.png"));
		leafIconEmpty = new ImageIcon(cache.checkOutImage(this, "cpath://assets/empty.png"));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
			boolean expanded, boolean isLeaf, int row, boolean focused) {

		super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);

		if (isLeaf && value instanceof FileTreeNode) {
			FileTreeNode node = (FileTreeNode) value;
			IconStates state = node.getIssueState();

			switch (state) {
				case NOTPARSED:
					setIcon(leafIconFailed);
					break;
				case ERROR:
					setIcon(leafIconError);
					break;
				case WARNING:
					setIcon(leafIconWarning);
					break;
				case INFO:
					setIcon(leafIconInfo);
					break;
				case EMPTY:
					setIcon(leafIconEmpty);
					break;
			}
		}

		return this;
	}

	public void dispose() {
		leafIconError = null;
		leafIconWarning = null;
		leafIconInfo = null;
		leafIconFailed = null;
		leafIconEmpty = null;

		cache.checkInImage(this, "cpath://assets/error.png");
		cache.checkInImage(this, "cpath://assets/warning.png");
		cache.checkInImage(this, "cpath://assets/info.png");
		cache.checkInImage(this, "cpath://assets/notparsed.png");
		cache.checkInImage(this, "cpath://assets/empty.png");
	}
}
