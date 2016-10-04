package pbj.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import pbj.math.graph.GraphMap;
import pbj.math.graph.train.TrainTrack;
import pbj.math.numerical.IntMatrix;

public class MatrixDialog {
	private static final long serialVersionUID = 1L;

	private JDialog dialog;
	private JRadioButton transitionButton;
	private JRadioButton homologyButton;
	private ButtonGroup typeGroup;
	private JRadioButton plainButton;
	private JRadioButton matlabButton;
	private JRadioButton mathematicaButton;
	private JRadioButton mapleButton;
	private ButtonGroup targetGroup;
	private JButton cancelButton;
	private JButton okButton;
	private JFrame owner;

	private boolean valid;
	
	public MatrixDialog(JFrame owner) {
		this.owner = owner;
		dialog = new JDialog(owner, "Export matrix", true);
		dialog.setLayout(new GridLayout(0, 4));
		dialog.add(plainButton = new JRadioButton("Plain text"));;
		dialog.add(matlabButton = new JRadioButton("Matlab"));
		dialog.add(mathematicaButton = new JRadioButton("Mathematica"));
		dialog.add(mapleButton = new JRadioButton("Maple"));
		dialog.add(transitionButton = new JRadioButton("Transition"));
		dialog.add(homologyButton = new JRadioButton("Homology"));
		dialog.add(cancelButton = new JButton("Cancel"));
		dialog.add(okButton = new JButton("Okay"));

		typeGroup = new ButtonGroup();
		typeGroup.add(transitionButton);
		typeGroup.add(homologyButton);
		transitionButton.setSelected(true);
		
		targetGroup = new ButtonGroup();
		targetGroup.add(plainButton);
		targetGroup.add(matlabButton);
		targetGroup.add(mathematicaButton);
		targetGroup.add(mapleButton);
		matlabButton.setSelected(true);
		
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
	}

	public String getMatrix(GraphMap g) {
		TrainTrack tt = new TrainTrack(g);
		String m = null;
		valid = false;
		dialog.pack();
		dialog.setVisible(true);
		if (valid) {
			try {
				IntMatrix im;
				if (transitionButton.isSelected()) {
					im = tt.transitionMatrix();
					m = "T = ";
				} else {
					tt.rose();
					im = tt.abelianized();
					m = "M = ";
				}
				if (plainButton.isSelected()) {
					m += im.toString();
				} else if (matlabButton.isSelected()) {
					m += im.toMATLAB();
				} else if (mathematicaButton.isSelected()) {
					m += im.toMathematica();
				} else if (mapleButton.isSelected()) {
					m += im.toMaple();
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(owner, e.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		return m;
	}
}
