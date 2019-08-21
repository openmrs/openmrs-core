package org.openmrs.web.filter.initialization;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class InstallationWizardError extends JFrame {
	
	private JButton resetJB;
	
	private JLabel errorJL;
	
	private static final int WIDTH = 275;
	
	private static final int HEIGHT = 110;
	
	public InstallationWizardError() {
		
		super("An Error Occured");
		
		resetJB = new JButton("Reset");
		resetJB.setSize(70, 30);
		resetJB.setLocation(90, 40);
		resetJB.setToolTipText("Reset or start-over the Installation process");
		resetJB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (e.getActionCommand().equals("Reset")) {
					setVisible(false);
					ErrorWarning ew = new ErrorWarning();
					ew.setSize(240, 170);
					ew.setResizable(false);
					ew.setVisible(true);
					ew.setLocationRelativeTo(null);
					ew.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
			}
		});
		errorJL = new JLabel("An Error Occured!!");
		errorJL.setLocation(50, 00);
		errorJL.setSize(180, 30);
		errorJL.setForeground(Color.RED);
		errorJL.setFont(new Font("Century Gothic", Font.BOLD, 18));
		
		Container pane = getContentPane();
		setResizable(false);
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pane.setLayout(null);
		pane.add(resetJB);
		pane.add(errorJL);
		
	}
	
	public class ErrorWindow extends JFrame implements ActionListener {
		
		private JButton existingConfigJB, newConfigJB;
		
		private static final int WIDTH = 275;
		
		private static final int HEIGHT = 110;
		
		public ErrorWindow() {
			
			super("Choose Reset Option");
			
			existingConfigJB = new JButton("Restart with Existing Config");
			existingConfigJB.setSize(180, 30);
			existingConfigJB.setLocation(50, 20);
			existingConfigJB.addActionListener(this);
			
			newConfigJB = new JButton("Enter new Config Settings");
			newConfigJB.setSize(180, 30);
			newConfigJB.setLocation(50, 70);
			newConfigJB.addActionListener(this);
			
			Container pane = getContentPane();
			
			pane.setLayout(null);
			pane.setSize(WIDTH, HEIGHT);
			pane.add(existingConfigJB);
			pane.add(newConfigJB);
			
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			/*
			 * Code for what happens when
			 * the two buttons are pressed
			 */
			
		}
		
	}
	
	public class ErrorWarning extends JFrame implements ActionListener {
		
		private JButton procedeJB, cancelJB;
		
		private JLabel error1JL, error2JL, error3JL;
		
		public ErrorWarning() {
			
			super("Warning!!");
			Container pane = getContentPane();
			pane.setLayout(null);
			
			error1JL = new JLabel("Do You Really Want To");
			error1JL.setSize(180, 30);
			error1JL.setLocation(50, 20);
			
			error2JL = new JLabel("Reset Or Start-Over");
			error2JL.setSize(180, 30);
			error2JL.setLocation(50, 40);
			
			error3JL = new JLabel("The Installation Process?!!!");
			error3JL.setSize(180, 30);
			error3JL.setLocation(50, 60);
			
			procedeJB = new JButton("Procede");
			procedeJB.setSize(80, 30);
			procedeJB.setLocation(40, 100);
			procedeJB.addActionListener(this);
			
			cancelJB = new JButton("Cancel");
			cancelJB.setSize(80, 30);
			cancelJB.setLocation(130, 100);
			cancelJB.addActionListener(this);
			
			pane.add(error1JL);
			pane.add(error2JL);
			pane.add(error3JL);
			pane.add(procedeJB);
			pane.add(cancelJB);
			
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (e.getActionCommand().equals("Procede")) {
				setVisible(false);
				ErrorWindow ew = new ErrorWindow();
				ew.setSize(300, 170);
				ew.setResizable(false);
				ew.setVisible(true);
				ew.setLocationRelativeTo(null);
				ew.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
			if (e.getActionCommand().equals("Cancel")) {
				System.exit(1);
			}
		}
	}
}
