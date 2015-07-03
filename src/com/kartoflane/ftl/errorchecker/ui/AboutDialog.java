package com.kartoflane.ftl.errorchecker.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.kartoflane.ftl.errorchecker.ErrorChecker;


@SuppressWarnings("serial")
public class AboutDialog extends JDialog implements ActionListener {

	private final JButton btnOk;

	public AboutDialog(JFrame parent) {
		super(parent, "About", Dialog.ModalityType.APPLICATION_MODAL);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(300, 130));
		setSize(300, 130);
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		add(panel, BorderLayout.SOUTH);

		btnOk = new JButton("OK");
		btnOk.setPreferredSize(new Dimension(80, btnOk.getPreferredSize().height));
		btnOk.addActionListener(this);
		panel.add(btnOk);

		String msg = "<html>" + ErrorChecker.APP_NAME + " version " + ErrorChecker.APP_VERSION + "<br/>" +
				"Created by " + ErrorChecker.APP_AUTHOR + "</html>";

		JLabel lblInfo = new JLabel(msg);
		lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblInfo, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOk) {
			if (!btnOk.isEnabled())
				return;

			dispose();
		}
	}
}
