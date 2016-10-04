package pbj.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import pbj.math.graph.Word;
import pbj.math.graph.train.TrainTrack;

public class FoldDialog {
	private static final long serialVersionUID = 1L;

	private JDialog dialog;
	private JComboBox foldField;
	private JButton cancelButton;
	private JButton okButton;
	private JFrame owner;

	private boolean valid;

	public FoldDialog(JFrame owner) {
		this.owner = owner;
		dialog = new JDialog(owner, "Fixed word Dehn twists", true);
		dialog.setLayout(new GridLayout(2, 2));
		dialog.add(new JLabel("Fold"));
		dialog.add(foldField = new JComboBox());
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

		dialog.setTitle("Fold edges");
	}

	public TrainTrack getFold(TrainTrack g) {
		g = new TrainTrack(g);
		HashMap<String, String> folds = getFolds(g);
		dialog.remove(foldField);
		dialog.add(foldField = new JComboBox(folds.keySet().toArray()), 0, 1);
		valid = false;
		dialog.pack();
		dialog.setVisible(true);
		if (valid) {
			try {
				String turn = folds.get(foldField.getSelectedItem());
				if (turn==null) return null;
				char c1 = turn.charAt(0);
				char c2 = turn.charAt(1);
				int e1 = Word.charToIndex(c1);
				int e2 = Word.charToIndex(c2);
				if (Word.isInverse(c1)) {
					g.reverseEdge(e1);
				}
				if (Word.isInverse(c2)) {
					g.reverseEdge(e2);
				}
				String w1 = g.getIm(e1);
				String w2 = g.getIm(e2);
				int i = 0;
				for(; i<w1.length() && i<w2.length() && w1.charAt(i)==w2.charAt(i); i++);
				if (i<w1.length()) {
					g.splitEdge(e1, i);
				}
				if (i<w2.length()) {
					g.splitEdge(e2, i);
				}
				g.elementaryFold(e1, e2);
				g.cleanItUp();
				return g;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(owner, e.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}

	private HashMap<String, String> getFolds(TrainTrack g) {
		HashMap<String, String> folds = new HashMap<String, String>();
		String fix = g.getFix();
		if (fix!=null && fix.length()>0) {
			for(int i = 0; i<fix.length(); i++) {
				int j = (i+1) % fix.length();
				char c1 = Word.inverse(fix.charAt(i));
				char c2 = fix.charAt(j);
				String w1 = g.mapWord(c1+"");
				String w2 = g.mapWord(c2+"");
				if (c1!=c2 && (w1.startsWith(w2) || w2.startsWith(w1))) {
					folds.put(Word.charToLabel(c1)+", "+Word.charToLabel(c2), c1+""+c2);
				}
			}
		}
		return folds;
	}
}
