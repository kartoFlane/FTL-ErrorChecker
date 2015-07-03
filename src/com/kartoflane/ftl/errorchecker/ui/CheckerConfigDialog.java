package com.kartoflane.ftl.errorchecker.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import net.vhati.modmanager.ui.FieldEditorPanel;
import net.vhati.modmanager.ui.FieldEditorPanel.ContentType;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.kartoflane.ftl.errorchecker.core.CheckerConfig;


@SuppressWarnings("serial")
public class CheckerConfigDialog extends JDialog implements ActionListener {

	private final CheckerConfig config;

	private final JButton btnCancel;
	private final JButton btnConfirm;
	private final FieldEditorPanel fieldPanel;
	private final JScrollPane scrollPane;

	public CheckerConfigDialog(JFrame parent, CheckerConfig config) {
		super(parent, "Preferences", Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(400, 250));
		setSize(400, 400);

		this.config = config;

		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		fieldPanel = new FieldEditorPanel(false);
		scrollPane.setViewportView(fieldPanel);

		// =============
		// Set up fields

		fieldPanel.addSeparatorRow();
		fieldPanel.addTextRow("Appearance Settings");
		fieldPanel.addBlankRow();

		String key = CheckerConfig.USE_NATIVE_GUI;
		fieldPanel.addRow(key, "Use Native GUI", ContentType.BOOLEAN);
		fieldPanel.addTextRow("When checked, the program will try to resemble platform's native GUI. Requires restart.");
		JCheckBox box = fieldPanel.getBoolean(key);
		box.setSelected(config.getPropertyAsBoolean(key));

		fieldPanel.addBlankRow();

		key = CheckerConfig.REM_GEOMETRY;
		fieldPanel.addRow(key, "Remember Geometry", ContentType.BOOLEAN);
		fieldPanel.addTextRow("When checked, the program will remember size and location of the main window.");
		box = fieldPanel.getBoolean(key);
		box.setSelected(config.getPropertyAsBoolean(key));

		fieldPanel.addBlankRow();

		fieldPanel.addSeparatorRow();
		fieldPanel.addTextRow("Checker Settings");
		fieldPanel.addBlankRow();

		key = CheckerConfig.HIDE_EMPTY_FILES;
		fieldPanel.addRow(key, "Only Show Files with Errors", ContentType.BOOLEAN);
		fieldPanel.addTextRow("When checked, files that don't have any problems will not be shown in the tree.");
		box = fieldPanel.getBoolean(key);
		box.setSelected(config.getPropertyAsBoolean(key));

		fieldPanel.addBlankRow();

		key = CheckerConfig.LOGGING_LEVEL;
		fieldPanel.addRow(key, "Logging Level", ContentType.COMBO);
		fieldPanel.addTextRow("Specifies the threshold of logging messages. Messages below the selected level will not be displayed.");
		JComboBox combo = fieldPanel.getCombo(key);
		combo.addItem(Level.OFF.toString());
		combo.addItem(Level.FATAL.toString());
		combo.addItem(Level.ERROR.toString());
		combo.addItem(Level.WARN.toString());
		combo.addItem(Level.INFO.toString());
		combo.addItem(Level.DEBUG.toString());
		combo.addItem(Level.TRACE.toString());
		combo.setSelectedItem(config.getProperty(key));

		fieldPanel.addBlankRow();

		fieldPanel.addSeparatorRow();
		fieldPanel.addTextRow("Viewer Settings");
		fieldPanel.addBlankRow();

		key = CheckerConfig.VIEWER_WRAP_TEXT;
		fieldPanel.addRow(key, "Wrap Text in Viewer", ContentType.BOOLEAN);
		fieldPanel.addTextRow("When checked, the text in the viewer will be wrapped.");
		box = fieldPanel.getBoolean(key);
		box.setSelected(config.getPropertyAsBoolean(key));

		fieldPanel.addBlankRow();

		key = CheckerConfig.VIEWER_TAB_SIZE;
		fieldPanel.addRow(key, "Tab Size", ContentType.SLIDER);
		fieldPanel.addTextRow("Indentation level of a single tab character.");
		JSlider tabSlider = fieldPanel.getSlider(key);
		tabSlider.setMinimum(0);
		tabSlider.setMaximum(8);
		tabSlider.setValue(config.getPropertyAsInt(key));

		fieldPanel.addBlankRow();

		key = CheckerConfig.VIEWER_FONT_STYLE;
		fieldPanel.addRow(key, "Font Style", ContentType.COMBO);
		fieldPanel.addTextRow("The font used in viewer.");
		combo = (JComboBox) fieldPanel.getCombo(key);
		combo.addItem(CheckerConfig.FONT_STYLE_DEFAULT);
		combo.addItem(Font.MONOSPACED);
		combo.addItem(Font.SANS_SERIF);
		combo.addItem(Font.SERIF);
		combo.addItem(Font.DIALOG);
		combo.addItem(Font.DIALOG_INPUT);
		combo.setSelectedItem(config.getProperty(key));

		fieldPanel.addBlankRow();

		key = CheckerConfig.VIEWER_FONT_SIZE;
		fieldPanel.addRow(key, "Font Size", ContentType.SLIDER);
		fieldPanel.addTextRow("Size of font used in the viewer.");
		JSlider fontSlider = fieldPanel.getSlider(key);
		fontSlider.setMinimum(2);
		fontSlider.setMaximum(30);
		fontSlider.setValue(config.getPropertyAsInt(key));

		fieldPanel.addBlankRow();

		fieldPanel.addSeparatorRow();
		fieldPanel.addTextRow("Parser Settings");
		fieldPanel.addBlankRow();

		key = CheckerConfig.PARSER_MAX_THREADS;
		fieldPanel.addRow(key, "Parser Threads", ContentType.SLIDER);
		fieldPanel.addTextRow("Number of threads the program will use during parsing.\nDoesn't necessarily make it faster.");
		JSlider parserSlider = fieldPanel.getSlider(key);
		parserSlider.setMinimum(1);
		parserSlider.setMaximum(10);
		parserSlider.setValue(config.getPropertyAsInt(key));

		fieldPanel.addBlankRow();

		key = CheckerConfig.PARSER_TIMEOUT_VAL;
		fieldPanel.addRow(key, "Parser Timeout Value", ContentType.INTEGER);
		fieldPanel.addTextRow("Amount of time the parser has to finish parsing all files.");
		JTextField parserTimeInt = fieldPanel.getInt(key);
		parserTimeInt.setText("" + config.getPropertyAsInt(key));

		key = CheckerConfig.PARSER_TIMEOUT_UNIT;
		fieldPanel.addRow(key, "Time Unit", ContentType.COMBO);
		fieldPanel.addTextRow("Unit of time for the above value.");
		JComboBox parserTimeCombo = (JComboBox) fieldPanel.getCombo(key);
		parserTimeCombo.addItem(TimeUnit.DAYS.toString());
		parserTimeCombo.addItem(TimeUnit.HOURS.toString());
		parserTimeCombo.addItem(TimeUnit.MINUTES.toString());
		parserTimeCombo.addItem(TimeUnit.SECONDS.toString());
		parserTimeCombo.addItem(TimeUnit.MILLISECONDS.toString());
		parserTimeCombo.setSelectedItem(config.getProperty(key));

		fieldPanel.addSeparatorRow();
		fieldPanel.addTextRow("Validation Settings");
		fieldPanel.addBlankRow();

		key = CheckerConfig.VALIDATE_MAX_THREADS;
		fieldPanel.addRow(key, "Validation Threads", ContentType.SLIDER);
		fieldPanel.addTextRow("Number of threads the program will use during validation.\nDoesn't necessarily make it faster.");
		JSlider validateSlider = fieldPanel.getSlider(key);
		validateSlider.setMinimum(1);
		validateSlider.setMaximum(10);
		validateSlider.setValue(config.getPropertyAsInt(key));

		fieldPanel.addBlankRow();

		key = CheckerConfig.VALIDATE_TIMEOUT_VAL;
		fieldPanel.addRow(key, "Validation Timeout Value", ContentType.INTEGER);
		fieldPanel.addTextRow("Amount of time the program has to finish validating all files.");
		JTextField validateTimeInt = fieldPanel.getInt(key);
		validateTimeInt.setText("" + config.getPropertyAsInt(key));

		key = CheckerConfig.VALIDATE_TIMEOUT_UNIT;
		fieldPanel.addRow(key, "Time Unit", ContentType.COMBO);
		fieldPanel.addTextRow("Unit of time for the above value.");
		JComboBox validateTimeCombo = (JComboBox) fieldPanel.getCombo(key);
		validateTimeCombo.addItem(TimeUnit.DAYS.toString());
		validateTimeCombo.addItem(TimeUnit.HOURS.toString());
		validateTimeCombo.addItem(TimeUnit.MINUTES.toString());
		validateTimeCombo.addItem(TimeUnit.SECONDS.toString());
		validateTimeCombo.addItem(TimeUnit.MILLISECONDS.toString());
		validateTimeCombo.setSelectedItem(config.getProperty(key));

		// ===============
		// Control buttons

		JPanel controlPanel = new JPanel();
		getContentPane().add(controlPanel, BorderLayout.SOUTH);
		controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		btnConfirm = new JButton("Confirm");
		btnConfirm.addActionListener(this);
		controlPanel.add(btnConfirm);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);
		controlPanel.add(btnCancel);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				scrollPane.getVerticalScrollBar().setValue(0);
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == btnCancel) {
			dispose();
		}
		else if (source == btnConfirm) {
			// Remember settings
			String key = CheckerConfig.USE_NATIVE_GUI;
			config.setProperty(key, "" + fieldPanel.getBoolean(key).isSelected());
			key = CheckerConfig.REM_GEOMETRY;
			config.setProperty(key, "" + fieldPanel.getBoolean(key).isSelected());

			key = CheckerConfig.HIDE_EMPTY_FILES;
			config.setProperty(key, "" + fieldPanel.getBoolean(key).isSelected());
			key = CheckerConfig.LOGGING_LEVEL;
			config.setProperty(key, "" + fieldPanel.getCombo(key).getSelectedItem().toString());

			key = CheckerConfig.VIEWER_WRAP_TEXT;
			config.setProperty(key, "" + fieldPanel.getBoolean(key).isSelected());
			key = CheckerConfig.VIEWER_TAB_SIZE;
			config.setProperty(key, "" + fieldPanel.getSlider(key).getValue());
			key = CheckerConfig.VIEWER_FONT_STYLE;
			config.setProperty(key, fieldPanel.getCombo(key).getSelectedItem().toString());
			key = CheckerConfig.VIEWER_FONT_SIZE;
			config.setProperty(key, "" + fieldPanel.getSlider(key).getValue());

			key = CheckerConfig.PARSER_MAX_THREADS;
			config.setProperty(key, "" + fieldPanel.getSlider(key).getValue());
			key = CheckerConfig.PARSER_TIMEOUT_VAL;
			config.setProperty(key, "" + fieldPanel.getInt(key).getText());
			key = CheckerConfig.PARSER_TIMEOUT_UNIT;
			config.setProperty(key, fieldPanel.getCombo(key).getSelectedItem().toString());

			key = CheckerConfig.VALIDATE_MAX_THREADS;
			config.setProperty(key, "" + fieldPanel.getSlider(key).getValue());
			key = CheckerConfig.VALIDATE_TIMEOUT_VAL;
			config.setProperty(key, "" + fieldPanel.getInt(key).getText());
			key = CheckerConfig.VALIDATE_TIMEOUT_UNIT;
			config.setProperty(key, fieldPanel.getCombo(key).getSelectedItem().toString());

			// Apply their effects
			CheckerFrame frame = (CheckerFrame) getParent();
			frame.updateTreeNodes();
			frame.updateViewer();

			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			Configuration logCfg = ctx.getConfiguration();
			LoggerConfig loggerConfig = logCfg.getLoggerConfig("com.kartoflane");
			loggerConfig.setLevel(Level.valueOf(config.getProperty(CheckerConfig.LOGGING_LEVEL)));
			ctx.updateLoggers();

			dispose();
		}
	}
}
