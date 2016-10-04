package pbj.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import pbj.math.graph.DehnTwist;
import pbj.math.graph.GraphMap;
import pbj.math.graph.Word;

public class RandomDialog {
	private static final long serialVersionUID = 1L;

	private Random generator = new Random();
	private JDialog dialog;
	private JTextField rankField;
	private JTextField lengthField;
	private JButton cancelButton;
	private JButton okButton;
	private JFrame owner;

	private boolean valid;
	
	public RandomDialog(JFrame owner) {
		this.owner = owner;
		dialog = new JDialog(owner, "Random automorphisms", true);
		dialog.setLayout(new GridLayout(3, 2));
		dialog.add(new JLabel("Rank"));
		dialog.add(rankField = new JTextField("3"));
		dialog.add(new JLabel("Length"));
		dialog.add(lengthField = new JTextField("10"));
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

		lengthField.setColumns(5);
		dialog.setTitle("Random Automorphisms");
	}

	public GraphMap getMap() {
		GraphMap g = null;
		valid = false;
		dialog.pack();
		dialog.setVisible(true);
		if (valid) {
			try {
				int rank = Integer.parseInt(rankField.getText());
				int len = Integer.parseInt(lengthField.getText());
				g = randomMap(rank, len);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(owner, e.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		return g;
	}
	
	private GraphMap randomMap(int rank, int len) {
		GraphMap g = GraphMap.identity(rank);
		for(int i=0; i<len; i++) {
			switch(generator.nextInt(4)) {
			case 0:
				String s = g.getIm(0);
				g.setImage(0, g.getIm(1));
				g.setImage(1, s);
				break;
			case 1:
				g.setImage(0, g.getIm(0)+g.getIm(1));
				g.tighten();
				break;
			case 2:
				g.setImage(0, Word.reverseWord(g.getIm(0)));
				break;
			case 3:
				s = g.getIm(0);
				int j=0;
				for(; j<rank-1; j++) {
					g.setImage(j, g.getIm(j+1));
				}
				g.setImage(j, s);
				break;
			}
		}
		return g;
	}
}
