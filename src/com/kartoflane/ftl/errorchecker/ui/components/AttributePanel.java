package com.kartoflane.ftl.errorchecker.ui.components;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.kartoflane.common.swing.SwingUIUtils;


@SuppressWarnings("serial")
public class AttributePanel extends JPanel
		implements ActionListener, DocumentListener {

	private final JTextField txtKey;
	private final JTextField txtValue;
	private final JButton btnDispose;

	private final List<DocumentListener> documentListeners;

	public AttributePanel() {
		documentListeners = new ArrayList<DocumentListener>();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		txtKey = new JTextField();
		txtKey.getDocument().addDocumentListener(this);
		txtKey.setToolTipText(SwingUIUtils.toHTML("The attribute's name.\nCan be left empty to match any attribute that has the specified value."));

		GridBagConstraints gbc_keyText = new GridBagConstraints();
		gbc_keyText.fill = GridBagConstraints.HORIZONTAL;
		gbc_keyText.insets = new Insets(0, 0, 0, 5);
		gbc_keyText.gridx = 0;
		gbc_keyText.gridy = 0;
		add(txtKey, gbc_keyText);
		txtKey.setColumns(10);

		JLabel lblEqual = new JLabel("=");

		GridBagConstraints gbc_lblEqual = new GridBagConstraints();
		gbc_lblEqual.anchor = GridBagConstraints.CENTER;
		gbc_lblEqual.insets = new Insets(0, 0, 0, 5);
		gbc_lblEqual.gridx = 1;
		gbc_lblEqual.gridy = 0;
		add(lblEqual, gbc_lblEqual);

		txtValue = new JTextField();
		txtValue.getDocument().addDocumentListener(this);
		txtValue.setToolTipText(SwingUIUtils.toHTML("The attribute's value.\nCan be left empty to match any value for this attribute."));

		GridBagConstraints gbc_valueText = new GridBagConstraints();
		gbc_valueText.insets = new Insets(0, 0, 0, 5);
		gbc_valueText.fill = GridBagConstraints.HORIZONTAL;
		gbc_valueText.gridx = 2;
		gbc_valueText.gridy = 0;
		add(txtValue, gbc_valueText);
		txtValue.setColumns(10);

		btnDispose = new JButton("x");
		btnDispose.setToolTipText("Removes the attribute from search parameters.");
		btnDispose.addActionListener(this);

		GridBagConstraints gbc_btnDispose = new GridBagConstraints();
		gbc_btnDispose.anchor = GridBagConstraints.EAST;
		gbc_btnDispose.gridx = 3;
		gbc_btnDispose.gridy = 0;
		add(btnDispose, gbc_btnDispose);
	}

	public void setKey(String key) {
		txtKey.setText(key);
	}

	public String getKey() {
		return txtKey.getText();
	}

	public void setValue(String key) {
		txtValue.setText(key);
	}

	public String getValue() {
		return txtValue.getText();
	}

	public void addDocumentListener(DocumentListener listener) {
		if (listener == null) {
			return;
		}
		documentListeners.add(listener);
	}

	public void removeDocumentListener(DocumentListener listener) {
		if (listener == null) {
			return;
		}
		documentListeners.remove(listener);
	}

	/*
	 * =====================
	 * NOTE: Listener methods
	 * =====================
	 */

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == btnDispose) {
			documentListeners.clear();
			Container parent = getParent();
			parent.remove(this);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		Object source = e.getDocument();

		if (source == txtKey.getDocument() || source == txtValue.getDocument()) {
			for (DocumentListener listener : documentListeners) {
				listener.insertUpdate(e);
			}
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		Object source = e.getDocument();

		if (source == txtKey.getDocument() || source == txtValue.getDocument()) {
			for (DocumentListener listener : documentListeners) {
				listener.removeUpdate(e);
			}
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		Object source = e.getDocument();

		if (source == txtKey.getDocument() || source == txtValue.getDocument()) {
			for (DocumentListener listener : documentListeners) {
				listener.changedUpdate(e);
			}
		}
	}
}
