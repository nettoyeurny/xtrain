package pbj.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import pbj.math.graph.GraphMap;
import pbj.math.graph.LorenzKnot;


public class LorenzKnotDialog {
	private static final long serialVersionUID = 1L;

	private JDialog dialog;
	private JTextField rVectorField;
	private JTextField sVectorField;
	private JButton cancelButton;
	private JButton okButton;
	private JFrame owner;

	private boolean valid;
	
	public LorenzKnotDialog(JFrame owner) {
		this.owner = owner;
		dialog = new JDialog(owner, "Lorenz knot monodromies", true);
		dialog.setLayout(new GridLayout(3, 2));
		dialog.add(new JLabel("r_i"));
		dialog.add(rVectorField = new JTextField("2, 3"));
		dialog.add(new JLabel("s_i"));
		dialog.add(sVectorField = new JTextField("4, 4"));
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

		sVectorField.setColumns(20);
		dialog.setTitle("Lorenz Knot Monodromies");
	}
	
	private int[] toArray(String s) {
		StringTokenizer st = new StringTokenizer(s, ", ");
		int n = st.countTokens();
		int[] result = new int[n];
		int i = 0;
		while (st.hasMoreElements()) {
			result[i++] = Integer.valueOf(st.nextToken());
		}
		return result;
	}

	public GraphMap getMap() {
		GraphMap g = null;
		valid = false;
		dialog.pack();
		dialog.setVisible(true);
		if (valid) {
			try {
				int[] r = toArray(rVectorField.getText());
				int[] s = toArray(sVectorField.getText());
				LorenzKnot lk = new LorenzKnot(r, s);
				g = lk.getMonodromy();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(owner, e.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		return g;
	}
}
