package pbj.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileWriter;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import pbj.math.graph.GraphMap;
import pbj.math.graph.train.TrainPic;
import pbj.math.graph.train.TrainTrack;

public class XTrainGraphics extends JFrame {
	private static final long serialVersionUID = 1L;

	private TrainTrack trainTrack = null;

	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem psItem;
	private JMenuItem psfragItem;
	private JMenuItem closeItem;
	private JMenuItem quitItem;
	private JMenu optionsMenu;
	private JCheckBoxMenuItem modelItem;
	private JCheckBoxMenuItem borderItem;
	private JMenu helpMenu;
	private JMenuItem helpItem;

	private JPanel panel;
	private JFileChooser chooser;
	private static URLDisplayFrame helpFrame = null;

	public XTrainGraphics() {
		chooser = new JFileChooser();

		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		psItem = new JMenuItem("Export PostScript");
		psfragItem = new JMenuItem("Export psfrag");
		closeItem = new JMenuItem("Close");
		quitItem = MenuTools.createQuitItem();
		optionsMenu = new JMenu("Options");
		modelItem = new JCheckBoxMenuItem("Poincare model");
		borderItem = new JCheckBoxMenuItem("Show circle at infinity");
		helpMenu = new JMenu("Help");
		helpItem = new JMenuItem("Graphics");

		modelItem.setSelected(true);
		borderItem.setSelected(true);

		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		MenuTools.setShortCut(closeItem, KeyEvent.VK_W);
		psItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (trainTrack!=null) {
					int result = chooser.showSaveDialog(XTrainGraphics.this);
					if (result==JFileChooser.APPROVE_OPTION) {
						try {
							FileWriter fw = new FileWriter(chooser.getSelectedFile());
							new TrainPic(fw, modelItem.getState(), borderItem.getState(), trainTrack, trainTrack.getLabel(), 1.0);
							fw.close();
						} catch(Exception ex) {
							JOptionPane.showMessageDialog(XTrainGraphics.this, ex.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		MenuTools.setShortCut(psItem, KeyEvent.VK_S);
		psfragItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (trainTrack!=null) {
					int result = chooser.showSaveDialog(XTrainGraphics.this);
					if (result==JFileChooser.APPROVE_OPTION) {
						try {
							FileWriter fw = new FileWriter(chooser.getSelectedFile());
							TrainPic.psfragLabels(fw, trainTrack);
							fw.close();
						} catch(Exception ex) {
							JOptionPane.showMessageDialog(XTrainGraphics.this, ex.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		modelItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		borderItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		helpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (helpFrame==null) {
					try {
						helpFrame = new URLDisplayFrame("Xtrain Graphics Help", getClass().getResource("docs/draw.html"));
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(XTrainGraphics.this, "Help text unavailable", "Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
				if (helpFrame!=null) {
					helpFrame.setVisible(true);
				}
			}
		});
		menuBar.add(fileMenu);
		fileMenu.add(psItem);
		fileMenu.add(psfragItem);
		fileMenu.addSeparator();
		fileMenu.add(closeItem);
		if (quitItem!=null) {
			fileMenu.add(quitItem);
		}
		menuBar.add(optionsMenu);
		optionsMenu.add(modelItem);
		optionsMenu.add(borderItem);
		menuBar.add(helpMenu);
		helpMenu.add(helpItem);
		setJMenuBar(menuBar);

		panel = new JPanel() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				if (trainTrack!=null) {
					new TrainPic(g, modelItem.getState(), borderItem.getState(), trainTrack, panel.getSize());
				}
			}
		};
		panel.setBackground(new Color(75, 175, 205));
		add(panel, BorderLayout.CENTER);
		
		setSize(400, 400);
		setTitle("XTrain Graphics");
	}

	public void setTrainTrack(GraphMap g) {
		if (g.getRank()<4) {
			throw new RuntimeException("Rank too low!");
		} else if (g.getFix().equals("")) {
			throw new RuntimeException("Empty fixed word!");
		} else if (!g.isGoodMap()) {
			throw new RuntimeException("Discontinuous map!");
		}
		trainTrack = new TrainTrack(g);
		repaint();
	}
}
