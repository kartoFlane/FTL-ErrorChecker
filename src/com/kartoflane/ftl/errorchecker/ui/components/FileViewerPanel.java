package com.kartoflane.ftl.errorchecker.ui.components;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.Element;

import com.kartoflane.common.utils.IOUtils;
import com.kartoflane.ftl.errorchecker.core.CheckerConfig;
import com.kartoflane.ftl.errorchecker.core.FilePointer;


@SuppressWarnings("serial")
public class FileViewerPanel extends JPanel {

	private final JLabel viewerHeader;
	private final JTextArea textArea;
	private final JScrollPane textScrollPane;
	private final TextLineNumber textLineNumberer;

	public FileViewerPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		viewerHeader = new JLabel(" ");
		viewerHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
		viewerHeader.setHorizontalAlignment(SwingConstants.LEFT);
		add(viewerHeader);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		textArea.setBackground(UIManager.getDefaults().getColor("TextArea.disabledBackground"));

		textScrollPane = new JScrollPane(textArea);
		// NOTE: Comment out these two lines when using WindowBuilder to prevent it from crashing
		textLineNumberer = new TextLineNumber(textArea);
		textScrollPane.setRowHeaderView(textLineNumberer);
		add(textScrollPane);

		textArea.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				textArea.getCaret().setVisible(true);
			}

			public void focusLost(FocusEvent e) {
				textArea.getCaret().setVisible(false);
			}
		});
	}

	public void setWrapText(boolean wrap) {
		textArea.setLineWrap(wrap);
		textArea.setWrapStyleWord(wrap);
	}

	public void setFontStyle(String fontStyle, int fontSize) {
		if (fontStyle == null)
			throw new IllegalArgumentException("Argument must not be null!");

		if (fontStyle.equals(CheckerConfig.FONT_STYLE_DEFAULT)) {
			textArea.setFont(UIManager.getFont("Label.font"));
		}
		else if (fontStyle.equals(Font.MONOSPACED) || fontStyle.equals(Font.SANS_SERIF) ||
				fontStyle.equals(Font.SERIF) || fontStyle.equals(Font.DIALOG_INPUT) ||
				fontStyle.equals(Font.DIALOG)) {
			textArea.setFont(new Font(fontStyle, Font.PLAIN, fontSize));
		}
		else {
			throw new IllegalArgumentException("Unknown font style: " + fontStyle);
		}
	}

	public void setTabSize(int size) {
		textArea.setTabSize(size);
	}

	public void setHeaderText(String message) {
		// When the string is empty, the JLabel is hidden, don't want that
		if (message == null || message.length() == 0)
			message = " ";
		viewerHeader.setText(message);
	}

	public void showLine(int line) {
		Element root = textArea.getDocument().getDefaultRootElement();
		line = Math.max(line, 1);
		line = Math.min(line, root.getElementCount() + 1);
		int start = root.getElement(line - 1).getStartOffset();
		int end = root.getElement(line - 1).getEndOffset();
		showPos(start, end - 1);
	}

	public void showPos(int start, int end) {
		textArea.select(start, end);
		textArea.getCaret().setSelectionVisible(true);
	}

	public void viewFile(FilePointer fi) throws FileNotFoundException, IOException {
		InputStream is = null;
		try {
			is = fi.getInputStream();
			setHeaderText(String.format("%s (%s)", fi.getName(), IOUtils.readableFileSize(fi.getSize())));
			textArea.setText(IOUtils.readStream(is, ""));
			textArea.setCaretPosition(0);
		}
		finally {
			if (is != null)
				is.close();
		}
	}
}
