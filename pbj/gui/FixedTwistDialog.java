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

public class FixedTwistDialog {
	private static final long serialVersionUID = 1L;

	private JDialog dialog;
	private JTextField fixedWordField;
	private JTextField twistField;
	private JButton cancelButton;
	private JButton okButton;
	private JFrame owner;

	private boolean valid;
	
	public FixedTwistDialog(JFrame owner) {
		this.owner = owner;
		dialog = new JDialog(owner, "Fixed word Dehn twists", true);
		dialog.setLayout(new GridLayout(3, 2));
		dialog.add(new JLabel("Fixed word"));
		dialog.add(fixedWordField = new JTextField("aBAbDcdC"));
		dialog.add(new JLabel("Twists"));
		dialog.add(twistField = new JTextField("-c(bD)aa-b"));
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
		dialog.setTitle("Fixed Word Twists");
	}

	public GraphMap getMap() {
		GraphMap g = null;
		valid = false;
		dialog.pack();
		dialog.setVisible(true);
		if (valid) {
			try {
				String fixed = fixedWordField.getText();
				String twists = DehnTwist.normalize(twistField.getText());
				g = DehnTwist.twistWithFixedWord(fixed, twists, "fix."+fixed+"."+twists);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(owner, e.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		return g;
	}
}
