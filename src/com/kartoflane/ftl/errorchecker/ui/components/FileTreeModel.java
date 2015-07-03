package com.kartoflane.ftl.errorchecker.ui.components;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import com.kartoflane.ftl.errorchecker.core.FilePointer;


@SuppressWarnings("serial")
public class FileTreeModel extends DefaultTreeModel {

	public FileTreeModel() {
		super(new DefaultMutableTreeNode("data.dat"));
	}

	/**
	 * Inserts the child node at the last index in the parent node.
	 * 
	 * @param child
	 *            the new node to be added to parent
	 * @param parent
	 *            the parent node to which the new node will be added
	 */
	public void insertNodeInto(MutableTreeNode child, MutableTreeNode parent) {
		insertNodeInto(child, parent, parent.getChildCount());
	}

	/**
	 * Creates a new node from the argument, inserts it into the root, and returns it.
	 */
	public FileTreeNode addNode(FilePointer fi) {
		FileTreeNode node = new FileTreeNode(fi.getName());
		insertNodeInto(node, (MutableTreeNode) root);
		return node;
	}

	public FileTreeNode getNode(FilePointer fi) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			FileTreeNode child = (FileTreeNode) root.getChildAt(i);
			if (child.getName().equals(fi.getName())) {
				return child;
			}
		}
		return null;
	}

	public int getRowOf(FilePointer fi) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			FileTreeNode child = (FileTreeNode) root.getChildAt(i);
			if (child.getName().equals(fi.getName())) {
				return i + 1;
			}
		}
		return -1;
	}

	public void clear() {
		((DefaultMutableTreeNode) root).removeAllChildren();
		reload();
	}

	public class FileTreeNode extends DefaultMutableTreeNode {
		private IconStates issueState = IconStates.EMPTY;

		public FileTreeNode(String name) {
			super(name);
		}

		public String getName() {
			return (String) getUserObject();
		}

		public void setIssueState(IconStates state) {
			issueState = state;
		}

		public IconStates getIssueState() {
			return issueState;
		}
	}
}
