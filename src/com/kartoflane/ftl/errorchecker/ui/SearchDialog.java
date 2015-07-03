package com.kartoflane.ftl.errorchecker.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.kartoflane.common.search.ISearchDialog;
import com.kartoflane.common.search.RegexFilter;
import com.kartoflane.common.search.SearchFilter;
import com.kartoflane.common.search.SearchListener;
import com.kartoflane.common.search.SearchPerformer;
import com.kartoflane.common.search.TextFilter;
import com.kartoflane.common.swing.SwingUIUtils;
import com.kartoflane.ftl.errorchecker.core.CheckerContext;
import com.kartoflane.ftl.errorchecker.ui.components.AttributePanel;


@SuppressWarnings("serial")
public class SearchDialog extends JDialog
		implements ActionListener, DocumentListener, ISearchDialog {

	// Search parameter keys
	public static final String SPK_BROAD_SEARCH = "Broad";
	public static final String SPK_FILTER_FOR = "Filters";

	private final List<SearchListener> searchListeners;

	private final JTabbedPane tabbedPane;
	private final JPanel tabText;
	private final JPanel tabXml;

	private final Dimension textMinDim = new Dimension(280, 255);
	private final Dimension xmlMinDim = new Dimension(280, 405);

	private final CheckerContext context;

	// Control area
	private final JButton btnFind;
	private final JButton btnClose;
	private final JLabel lblStatus;

	// Search direction
	private final JRadioButton btnScanBackward;
	private final JRadioButton btnScanForward;

	// Scope filter
	private final JRadioButton btnScopeCurrent;
	private final JRadioButton btnScopeAll;

	// Text search
	private final JTextField txtTextFind;

	// Text search options
	private final JCheckBox btnTextRegex;
	private final JCheckBox btnTextCase;

	// XML search
	private final JTextField txtXmlName;
	private final JTextField txtXmlValue;

	// XML attribute filter
	private final JScrollPane scrollXmlAttr;
	private final JPanel panelXmlAttr;
	private final JButton btnXmlAddAttr;

	// XML search options
	private final JCheckBox btnXmlNoChild;
	private final JCheckBox btnXmlCase;
	private final JCheckBox btnXmlYesChild;
	private final JCheckBox btnXmlStrict;

	private boolean paramsChanged = true;
	private SearchPerformer searchPerformer;

	private String statusText = " ";
	private String statusXML = " ";

	public SearchDialog(JFrame owner, CheckerContext context) {
		super(owner, "Search Dialog", Dialog.ModalityType.MODELESS);

		this.context = context;

		searchListeners = new ArrayList<SearchListener>();

		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(xmlMinDim);
		setLocationRelativeTo(null);

		SwingUIUtils.installEscapeHideOperation(this);
		SwingUIUtils.installOperation(this, JComponent.WHEN_IN_FOCUSED_WINDOW,
				SwingUIUtils.KS_ENTER, getClass().getCanonicalName() + ":DO_FIND",
				new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						if (!btnFind.isEnabled())
							return;
						executeFind();
					}
				});

		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				toggleFindButton();
			}
		});

		/*
		 * The rest of the code below is generated, with but some measly
		 * attempts at structuring.
		 */

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, 0.0 };
		setLayout(gridBagLayout);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!tabbedPane.isEnabled())
					return;

				if (tabbedPane.getSelectedComponent() == tabXml)
					setMinimumSize(xmlMinDim);
				else
					setMinimumSize(textMinDim);

				paramsChanged = true;
				if (btnFind != null)
					toggleFindButton();
			}
		});

		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.weighty = 0.5;
		gbc_tabbedPane.gridwidth = 2;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		add(tabbedPane, gbc_tabbedPane);

		/*
		 * =========================================================
		 * NOTE: Text search tab
		 */

		tabText = new JPanel();
		tabbedPane.addTab(" Text ", null, tabText, null);

		GridBagLayout gbl_textPanel = new GridBagLayout();
		gbl_textPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_textPanel.columnWeights = new double[] { 0.0, 1.0 };
		gbl_textPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		tabText.setLayout(gbl_textPanel);

		JLabel lblFind = new JLabel("Find:");
		GridBagConstraints gbc_lblFind = new GridBagConstraints();
		gbc_lblFind.anchor = GridBagConstraints.EAST;
		gbc_lblFind.insets = new Insets(0, 5, 0, 5);
		gbc_lblFind.gridx = 0;
		gbc_lblFind.gridy = 0;
		tabText.add(lblFind, gbc_lblFind);

		txtTextFind = new JTextField();
		txtTextFind.setColumns(10);
		txtTextFind.getDocument().addDocumentListener(this);

		GridBagConstraints gbc_textFind = new GridBagConstraints();
		gbc_textFind.insets = new Insets(5, 0, 5, 5);
		gbc_textFind.anchor = GridBagConstraints.SOUTH;
		gbc_textFind.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFind.gridx = 1;
		gbc_textFind.gridy = 0;
		tabText.add(txtTextFind, gbc_textFind);

		JPanel textOptionsPanel = new JPanel();

		GridBagConstraints gbc_textOptionsPanel = new GridBagConstraints();
		gbc_textOptionsPanel.gridwidth = 2;
		gbc_textOptionsPanel.fill = GridBagConstraints.BOTH;
		gbc_textOptionsPanel.gridx = 0;
		gbc_textOptionsPanel.gridy = 1;
		tabText.add(textOptionsPanel, gbc_textOptionsPanel);

		GridBagLayout gbl_textOptionsPanel = new GridBagLayout();
		gbl_textOptionsPanel.columnWeights = new double[] { 0.0, 1.0 };
		gbl_textOptionsPanel.rowWeights = new double[] { 1.0 };
		textOptionsPanel.setLayout(gbl_textOptionsPanel);

		btnTextRegex = new JCheckBox("Regular Expression");
		btnTextRegex.addActionListener(this);

		GridBagConstraints gbc_btnTextRegex = new GridBagConstraints();
		gbc_btnTextRegex.insets = new Insets(0, 5, 0, 0);
		gbc_btnTextRegex.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnTextRegex.gridx = 0;
		gbc_btnTextRegex.gridy = 0;
		textOptionsPanel.add(btnTextRegex, gbc_btnTextRegex);

		btnTextCase = new JCheckBox("Case-Sensitive");
		btnTextCase.addActionListener(this);

		GridBagConstraints gbc_btnTextCase = new GridBagConstraints();
		gbc_btnTextCase.insets = new Insets(0, 5, 0, 5);
		gbc_btnTextCase.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnTextCase.gridx = 1;
		gbc_btnTextCase.gridy = 0;
		textOptionsPanel.add(btnTextCase, gbc_btnTextCase);

		/*
		 * =========================================================
		 * NOTE: XML search tab
		 */

		tabXml = new JPanel();
		tabbedPane.addTab("  XML  ", null, tabXml, null);

		GridBagLayout gbl_xmlPanel = new GridBagLayout();
		gbl_xmlPanel.columnWeights = new double[] { 0.0, 1.0 };
		gbl_xmlPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 0, 0 };
		tabXml.setLayout(gbl_xmlPanel);

		JLabel lblTagName = new JLabel("Tag Name:");

		GridBagConstraints gbc_lblTagName = new GridBagConstraints();
		gbc_lblTagName.insets = new Insets(0, 5, 0, 5);
		gbc_lblTagName.anchor = GridBagConstraints.WEST;
		gbc_lblTagName.gridx = 0;
		gbc_lblTagName.gridy = 0;
		tabXml.add(lblTagName, gbc_lblTagName);

		txtXmlName = new JTextField();
		txtXmlName.setColumns(10);
		txtXmlName.getDocument().addDocumentListener(this);
		txtXmlName.setToolTipText(SwingUIUtils.toHTML("The name of the tag: <name>value</name>.\n" +
				"Can be left empty to match any name."));

		GridBagConstraints gbc_xmlNameText = new GridBagConstraints();
		gbc_xmlNameText.anchor = GridBagConstraints.WEST;
		gbc_xmlNameText.insets = new Insets(5, 0, 5, 5);
		gbc_xmlNameText.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmlNameText.gridx = 1;
		gbc_xmlNameText.gridy = 0;
		tabXml.add(txtXmlName, gbc_xmlNameText);

		JLabel lblValue = new JLabel("Value:");

		GridBagConstraints gbc_lblValue = new GridBagConstraints();
		gbc_lblValue.anchor = GridBagConstraints.WEST;
		gbc_lblValue.insets = new Insets(0, 5, 5, 5);
		gbc_lblValue.gridx = 0;
		gbc_lblValue.gridy = 1;
		tabXml.add(lblValue, gbc_lblValue);

		txtXmlValue = new JTextField();
		txtXmlValue.setColumns(10);
		txtXmlValue.getDocument().addDocumentListener(this);
		txtXmlValue.setToolTipText(SwingUIUtils.toHTML("The value of the tag: <name>value</name>.\n" +
				"Can be left empty to match any value.\n" +
				"Tags with children have no value."));

		GridBagConstraints gbc_xmlValueText = new GridBagConstraints();
		gbc_xmlValueText.anchor = GridBagConstraints.WEST;
		gbc_xmlValueText.insets = new Insets(0, 0, 5, 5);
		gbc_xmlValueText.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmlValueText.gridx = 1;
		gbc_xmlValueText.gridy = 1;
		tabXml.add(txtXmlValue, gbc_xmlValueText);

		panelXmlAttr = new JPanel();
		panelXmlAttr.addContainerListener(new ContainerAdapter() {
			public void componentRemoved(ContainerEvent e) {
				int count = panelXmlAttr.getComponentCount();
				if (count > 0) {
					GridBagLayout layout = (GridBagLayout) panelXmlAttr.getLayout();
					layout.rowWeights = new double[count];
					layout.rowWeights[count - 1] = 1.0;
				}

				paramsChanged = true;

				scrollXmlAttr.revalidate();
				scrollXmlAttr.repaint();
			}
		});

		GridBagLayout gbl_attrPanel = new GridBagLayout();
		// NOTE: WindowBuilder resets this to empty array, since the panel has no children;
		// Should be: new double[] { 1.0 };
		gbl_attrPanel.columnWeights = new double[] { 1.0 };
		panelXmlAttr.setLayout(gbl_attrPanel);

		scrollXmlAttr = new JScrollPane();
		scrollXmlAttr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollXmlAttr.setViewportView(panelXmlAttr);

		GridBagConstraints gbc_attributesPanel = new GridBagConstraints();
		gbc_attributesPanel.anchor = GridBagConstraints.NORTH;
		gbc_attributesPanel.insets = new Insets(0, 0, 5, 0);
		gbc_attributesPanel.gridwidth = 2;
		gbc_attributesPanel.fill = GridBagConstraints.BOTH;
		gbc_attributesPanel.gridx = 0;
		gbc_attributesPanel.gridy = 2;
		tabXml.add(scrollXmlAttr, gbc_attributesPanel);

		btnXmlAddAttr = new JButton("Add Attribute");
		btnXmlAddAttr.setToolTipText("Adds a new attribute to the search.");
		btnXmlAddAttr.addActionListener(this);

		GridBagConstraints gbc_btnAddAttr = new GridBagConstraints();
		gbc_btnAddAttr.gridwidth = 2;
		gbc_btnAddAttr.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnAddAttr.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddAttr.gridx = 0;
		gbc_btnAddAttr.gridy = 3;
		tabXml.add(btnXmlAddAttr, gbc_btnAddAttr);

		/*
		 * =========================================================
		 * NOTE: XML search options
		 */

		JPanel xmlOptionsPanel = new JPanel();

		GridBagConstraints gbc_xmlOptionsPanel = new GridBagConstraints();
		gbc_xmlOptionsPanel.anchor = GridBagConstraints.SOUTH;
		gbc_xmlOptionsPanel.gridwidth = 2;
		gbc_xmlOptionsPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_xmlOptionsPanel.gridx = 0;
		gbc_xmlOptionsPanel.gridy = 4;
		tabXml.add(xmlOptionsPanel, gbc_xmlOptionsPanel);

		GridBagLayout gbl_xmlOptionsPanel = new GridBagLayout();
		gbl_xmlOptionsPanel.columnWeights = new double[] { 0.0, 1.0 };
		xmlOptionsPanel.setLayout(gbl_xmlOptionsPanel);

		btnXmlNoChild = new JCheckBox("No Children Tags");
		btnXmlNoChild.setToolTipText(SwingUIUtils.toHTML("If checked, only tags that have no children will be matched."));
		btnXmlNoChild.addActionListener(this);

		GridBagConstraints gbc_btnSelfClose = new GridBagConstraints();
		gbc_btnSelfClose.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnSelfClose.insets = new Insets(0, 5, 0, 0);
		gbc_btnSelfClose.gridx = 0;
		gbc_btnSelfClose.gridy = 0;
		xmlOptionsPanel.add(btnXmlNoChild, gbc_btnSelfClose);

		btnXmlCase = new JCheckBox("Case-Sensitive");
		btnXmlCase.addActionListener(this);

		GridBagConstraints gbc_btnXmlCase = new GridBagConstraints();
		gbc_btnXmlCase.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnXmlCase.insets = new Insets(0, 5, 0, 5);
		gbc_btnXmlCase.gridx = 1;
		gbc_btnXmlCase.gridy = 0;
		xmlOptionsPanel.add(btnXmlCase, gbc_btnXmlCase);

		btnXmlYesChild = new JCheckBox("Has Children Tags");
		btnXmlYesChild.setToolTipText(SwingUIUtils.toHTML("If checked, only tags that have children will be matched."));
		btnXmlYesChild.addActionListener(this);

		GridBagConstraints gbc_btnNested = new GridBagConstraints();
		gbc_btnNested.insets = new Insets(0, 5, 0, 0);
		gbc_btnNested.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnNested.gridx = 0;
		gbc_btnNested.gridy = 1;
		xmlOptionsPanel.add(btnXmlYesChild, gbc_btnNested);

		btnXmlStrict = new JCheckBox("Strict Search");
		btnXmlStrict.setToolTipText(SwingUIUtils.toHTML("If checked, tags need to match the specified texts exactly.\n" +
				"If unchecked, tags only need to contain the specified texts to be matched."));
		btnXmlStrict.addActionListener(this);
		btnXmlStrict.setSelected(true);

		GridBagConstraints gbc_btnStrict = new GridBagConstraints();
		gbc_btnStrict.insets = new Insets(0, 5, 0, 5);
		gbc_btnStrict.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnStrict.gridx = 1;
		gbc_btnStrict.gridy = 1;
		xmlOptionsPanel.add(btnXmlStrict, gbc_btnStrict);

		/*
		 * =========================================================
		 * NOTE: General search options
		 */

		JPanel optionsPanel = new JPanel();

		GridBagConstraints gbc_optionsPanel = new GridBagConstraints();
		gbc_optionsPanel.anchor = GridBagConstraints.SOUTH;
		gbc_optionsPanel.gridwidth = 2;
		gbc_optionsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_optionsPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_optionsPanel.gridx = 0;
		gbc_optionsPanel.gridy = 1;
		add(optionsPanel, gbc_optionsPanel);

		GridBagLayout gbl_optionsPanel = new GridBagLayout();
		gbl_optionsPanel.columnWeights = new double[] { 1.0, 1.0 };
		gbl_optionsPanel.rowWeights = new double[] { 1.0 };
		optionsPanel.setLayout(gbl_optionsPanel);

		JPanel directionPanel = new JPanel();
		directionPanel.setBorder(BorderFactory.createTitledBorder("Direction"));

		GridBagConstraints gbc_directionPanel = new GridBagConstraints();
		gbc_directionPanel.anchor = GridBagConstraints.SOUTH;
		gbc_directionPanel.fill = GridBagConstraints.BOTH;
		gbc_directionPanel.insets = new Insets(0, 0, 0, 5);
		gbc_directionPanel.gridx = 0;
		gbc_directionPanel.gridy = 0;
		optionsPanel.add(directionPanel, gbc_directionPanel);

		btnScanBackward = new JRadioButton("Backward");
		btnScanBackward.setHorizontalAlignment(SwingConstants.TRAILING);

		btnScanForward = new JRadioButton("Forward");
		btnScanForward.setSelected(true);
		btnScanForward.setHorizontalAlignment(SwingConstants.TRAILING);

		directionPanel.setLayout(new BoxLayout(directionPanel, BoxLayout.Y_AXIS));
		directionPanel.add(btnScanBackward);
		directionPanel.add(btnScanForward);

		ButtonGroup scanGroup = new ButtonGroup();
		scanGroup.add(btnScanForward);
		scanGroup.add(btnScanBackward);

		JPanel scopePanel = new JPanel();
		scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
		scopePanel.setBorder(BorderFactory.createTitledBorder("Scope"));

		GridBagConstraints gbc_scopePanel = new GridBagConstraints();
		gbc_scopePanel.anchor = GridBagConstraints.SOUTH;
		gbc_scopePanel.fill = GridBagConstraints.BOTH;
		gbc_scopePanel.gridx = 1;
		gbc_scopePanel.gridy = 0;
		optionsPanel.add(scopePanel, gbc_scopePanel);

		btnScopeCurrent = new JRadioButton("Current File");
		btnScopeCurrent.setSelected(true);
		btnScopeCurrent.addActionListener(this);
		scopePanel.add(btnScopeCurrent);

		btnScopeAll = new JRadioButton("All Files");
		btnScopeAll.addActionListener(this);
		scopePanel.add(btnScopeAll);

		ButtonGroup scopeGroup = new ButtonGroup();
		scopeGroup.add(btnScopeCurrent);
		scopeGroup.add(btnScopeAll);

		/*
		 * =========================================================
		 * NOTE: Control buttons
		 */

		JPanel controlPanel = new JPanel();

		GridBagConstraints gbc_controlPanel = new GridBagConstraints();
		gbc_controlPanel.anchor = GridBagConstraints.SOUTH;
		gbc_controlPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_controlPanel.gridwidth = 2;
		gbc_controlPanel.gridx = 0;
		gbc_controlPanel.gridy = 2;
		add(controlPanel, gbc_controlPanel);

		GridBagLayout gbl_controlPanel = new GridBagLayout();
		gbl_controlPanel.columnWeights = new double[] { 0.0, 1.0 };
		controlPanel.setLayout(gbl_controlPanel);

		lblStatus = new JLabel(" ");

		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.insets = new Insets(0, 10, 5, 10);
		gbc_lblStatus.anchor = GridBagConstraints.WEST;
		gbc_lblStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblStatus.gridwidth = 2;
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 0;
		controlPanel.add(lblStatus, gbc_lblStatus);

		btnClose = new JButton("Close");
		btnClose.setPreferredSize(new Dimension(80, btnClose.getPreferredSize().height));
		btnClose.addActionListener(this);

		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.insets = new Insets(0, 5, 5, 0);
		gbc_btnClose.anchor = GridBagConstraints.SOUTH;
		gbc_btnClose.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClose.gridx = 0;
		gbc_btnClose.gridy = 1;
		controlPanel.add(btnClose, gbc_btnClose);

		btnFind = new JButton("Find");
		btnFind.setEnabled(false);
		btnFind.setPreferredSize(new Dimension(80, btnFind.getPreferredSize().height));
		btnFind.addActionListener(this);

		GridBagConstraints gbc_btnFind = new GridBagConstraints();
		gbc_btnFind.insets = new Insets(0, 0, 5, 5);
		gbc_btnFind.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnFind.gridx = 1;
		gbc_btnFind.gridy = 1;
		controlPanel.add(btnFind, gbc_btnFind);
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (tabbedPane.getSelectedComponent() == tabText) {
			txtTextFind.requestFocus();
		} else {
			txtXmlName.requestFocus();
		}
	}

	public void addSearchListener(SearchListener l) {
		if (l == null) {
			return;
		}
		searchListeners.add(l);
	}

	public void removeSearchListener(SearchListener l) {
		if (l == null) {
			return;
		}
		searchListeners.remove(l);
	}

	public void setSearchPerformer(SearchPerformer sp) {
		if (sp == null)
			return;
		searchPerformer = sp;
	}

	public void paramsChanged() {
		paramsChanged = true;
	}

	public void setStatus(String msg) {
		lblStatus.setText(msg);
		if (tabbedPane.getSelectedComponent() == tabText)
			statusText = msg;
		else
			statusXML = msg;
	}

	/*
	 * ======================
	 * NOTE: Listener methods
	 * ======================
	 */

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == btnFind) {
			if (!btnFind.isEnabled())
				return;

			executeFind();
		}
		else if (source == btnClose) {
			if (!btnClose.isEnabled())
				return;

			setVisible(false);
		}
		else if (source == btnScopeAll) {
			if (!btnScopeAll.isEnabled())
				return;

			paramsChanged = true;
		}
		else if (source == btnScopeCurrent) {
			if (!btnScopeCurrent.isEnabled())
				return;

			paramsChanged = true;
		}
		else if (source == btnXmlNoChild) {
			if (!btnXmlNoChild.isEnabled())
				return;

			btnXmlYesChild.setEnabled(!btnXmlNoChild.isSelected());
			paramsChanged = true;
		}
		else if (source == btnXmlYesChild) {
			if (!btnXmlYesChild.isEnabled())
				return;

			btnXmlNoChild.setEnabled(!btnXmlYesChild.isSelected());
			paramsChanged = true;
		}
		else if (source == btnXmlCase) {
			if (!btnXmlCase.isEnabled())
				return;

			paramsChanged = true;
		}
		else if (source == btnXmlStrict) {
			if (!btnXmlStrict.isEnabled())
				return;

			paramsChanged = true;
		}
		else if (source == btnTextCase) {
			if (!btnTextCase.isEnabled())
				return;

			paramsChanged = true;
		}
		else if (source == btnTextRegex) {
			if (!btnTextRegex.isEnabled())
				return;

			paramsChanged = true;
		}
		else if (source == btnXmlAddAttr) {
			if (!btnXmlAddAttr.isEnabled())
				return;

			AttributePanel attr = new AttributePanel();
			attr.addDocumentListener(this);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(4, 5, 0, 5);
			gbc.gridx = 0;
			panelXmlAttr.add(attr, gbc);

			int count = panelXmlAttr.getComponentCount();
			GridBagLayout layout = (GridBagLayout) panelXmlAttr.getLayout();
			layout.rowWeights = new double[count];
			layout.rowWeights[count - 1] = 1.0;

			scrollXmlAttr.revalidate();
			scrollXmlAttr.repaint();
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		paramsChanged = true;
		toggleFindButton();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		paramsChanged = true;
		toggleFindButton();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		paramsChanged = true;
		toggleFindButton();
	}

	/*
	 * =====================
	 * NOTE: Private methods
	 * =====================
	 */

	private SearchFilter<?> constructFilter() {
		final boolean textFind = tabbedPane.getSelectedComponent() == tabText;
		final boolean caseSense = textFind ? btnTextCase.isSelected() : btnXmlCase.isSelected();
		final boolean regex = btnTextRegex.isSelected();
		final boolean noChild = btnXmlNoChild.isSelected();
		final boolean yesChild = btnXmlYesChild.isSelected();
		final boolean broad = btnScopeAll.isSelected();
		final boolean strict = btnXmlStrict.isSelected();

		SearchFilter<?> result = null;

		if (textFind) {
			final String find = txtTextFind.getText();

			if (regex) {
				try {
					RegexFilter filter = new RegexFilter(find, Pattern.UNICODE_CASE | (caseSense ? 0 : Pattern.CASE_INSENSITIVE));
					filter.setSearchParameter(SPK_FILTER_FOR, String.class);
					filter.setSearchParameter(SPK_BROAD_SEARCH, broad);
					result = filter;
				}
				catch (PatternSyntaxException e) {
					String msg = "The regex syntax is wrong:\n\n";
					msg += e.getMessage();
					SwingUIUtils.showWarningDialog(msg);
				}
			}
			else {
				TextFilter filter = new TextFilter(find, caseSense);
				filter.setSearchParameter(SPK_FILTER_FOR, String.class);
				filter.setSearchParameter(SPK_BROAD_SEARCH, broad);
				result = filter;
			}
		}
		else {
			final Map<String, String> attributeMap = new HashMap<String, String>();
			for (Component c : panelXmlAttr.getComponents()) {
				AttributePanel attr = (AttributePanel) c;
				attributeMap.put(attr.getKey(), attr.getValue());
			}

			result = new SearchFilter<Element>() {
				private final String name = txtXmlName.getText();
				private final String value = txtXmlValue.getText();

				public boolean accept(Element o) {
					if (o == null)
						return false;
					int size = o.getChildren().size();
					if (noChild && size > 0)
						return false;
					if (yesChild && size == 0)
						return false;

					boolean matches = true;

					if (caseSense) {
						if (strict) {
							matches &= name.equals("") || o.getName().equals(name);
							matches &= value.equals("") || o.getValue().equals(value);
						}
						else {
							matches &= name.equals("") || o.getName().contains(name);
							matches &= value.equals("") || o.getValue().contains(value);
						}
					}
					else {
						if (strict) {
							matches &= name.equals("") || o.getName().equalsIgnoreCase(name);
							matches &= value.equals("") || o.getValue().equalsIgnoreCase(value);
						}
						else {
							matches &= name.equals("") || containsIgnoreCase(o.getName(), name);
							matches &= value.equals("") || containsIgnoreCase(o.getValue(), value);
						}
					}

					if (!matches)
						return false;

					for (Map.Entry<String, String> searchAttr : attributeMap.entrySet()) {
						if (!matches)
							break;

						if (searchAttr.getKey().equals("")) {
							List<Attribute> attributes = o.getAttributes();
							boolean found = searchAttr.getValue().equals("") && attributes.size() > 0;

							for (int i = 0; i < attributes.size() && !found; ++i) {
								if (caseSense) {
									if (strict)
										found = attributes.get(i).getValue().equals(searchAttr.getValue());
									else
										found = attributes.get(i).getValue().contains(searchAttr.getValue());
								}
								else {
									if (strict)
										found = attributes.get(i).getValue().equalsIgnoreCase(searchAttr.getValue());
									else
										found = containsIgnoreCase(attributes.get(i).getValue(), searchAttr.getValue());
								}
							}

							matches &= found;
						}
						else {
							String tagValue = o.getAttributeValue(searchAttr.getKey());
							if (tagValue != null) {
								if (caseSense) {
									if (strict)
										matches &= searchAttr.getValue().equals("") || searchAttr.getValue().equals(tagValue);
									else
										matches &= searchAttr.getValue().equals("") || tagValue.contains(searchAttr.getValue());
								}
								else {
									if (strict)
										matches &= searchAttr.getValue().equals("") || searchAttr.getValue().equalsIgnoreCase(tagValue);
									else
										matches &= searchAttr.getValue().equals("") || containsIgnoreCase(tagValue, searchAttr.getValue());
								}
							}
							else {
								matches = false;
							}
						}
					}

					return matches;
				}

				@Override
				public Object getSearchParameter(String key) {
					if (SPK_FILTER_FOR.equals(key)) {
						return Element.class;
					}
					else if (SPK_BROAD_SEARCH.equals(key)) {
						return broad;
					}
					else {
						return null;
					}
				}
			};
		}

		return result;
	}

	private boolean containsIgnoreCase(String thiz, String str) {
		if (str == null)
			return true;
		return Pattern.compile(Pattern.quote(str), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(thiz).find();
	}

	private void executeFind() {
		if (paramsChanged) {
			SearchFilter<?> result = constructFilter();

			if (result == null)
				return;

			for (SearchListener l : searchListeners) {
				l.filterConstructed(result);
			}
		}

		if (!searchPerformer.hasAnyResults()) {
			Toolkit.getDefaultToolkit().beep();
			setStatus("No matches found.");
			return;
		}

		for (SearchListener l : searchListeners) {
			setStatus(" ");
			if (paramsChanged || btnScanForward.isSelected())
				l.nextResultRequested();
			else
				l.prevResultRequested();
		}

		if (paramsChanged)
			paramsChanged = false;
	}

	private void toggleFindButton() {
		if (tabbedPane.getSelectedComponent() == tabText) {
			lblStatus.setText(statusText);
			btnFind.setEnabled(!txtTextFind.getText().equals(""));
		}
		else if (tabbedPane.getSelectedComponent() == tabXml) {
			boolean vmInitd = context.getValidationManager().isInitialized();
			if (vmInitd)
				lblStatus.setText(statusXML);
			else
				lblStatus.setText("XML search disabled: parse archives first.");

			btnFind.setEnabled(vmInitd);
		}
	}
}
