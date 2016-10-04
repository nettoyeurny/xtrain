package pbj.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import pbj.math.graph.DehnTwist;
import pbj.math.graph.GraphMap;

public class StdTwistDialog {
	private static final long serialVersionUID = 1L;

	private JDialog dialog;
	private JTextField genusField;
	private JTextField twistField;
	private JButton cancelButton;
	private JButton okButton;
	private JFrame owner;

	private boolean valid;
	
	public StdTwistDialog(JFrame owner) {
		this.owner = owner;
		dialog = new JDialog(owner, "Standard Dehn twists", true);
		dialog.setLayout(new GridLayout(3, 2));
		dialog.add(new JLabel("Genus"));
		dialog.add(genusField = new JTextField("3"));
		dialog.add(new JLabel("Twists"));
		dialog.add(twistField = new JTextField("d0c0d1c1d2C2"));
		dialog.add(cancelButton = new JButton("Cancel"));
		dialog.add(okButton = new JButton("Okay"));

		dialog.getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				valid = true;
				dialog.setVisible(false);
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		twistField.setColumns(20);
		dialog.setTitle("Standard generators");
	}

	public GraphMap getMap() {
		GraphMap g = null;
		valid = false;
		dialog.pack();
		dialog.setVisible(true);
		if (valid) {
			try {
				int genus = Integer.parseInt(genusField.getText());
				String twists = DehnTwist.normalize(twistField.getText());
				g = DehnTwist.stdGenerators(genus, twists, "genus."+genus+"."+twists);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(owner, e.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		return g;
	}
}
