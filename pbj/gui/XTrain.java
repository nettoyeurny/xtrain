package pbj.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import pbj.math.graph.GraphMap;
import pbj.math.graph.train.Gates;
import pbj.math.graph.train.TrainTrack;
import pbj.math.manifold.snappea.SnapPeaBridge;
import pbj.math.numerical.IntMatrix;

public class XTrain extends JFrame {
	private static final long serialVersionUID = 1L;

	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem openItem;
	private JMenuItem saveItem;
	private JMenuItem latexItem;
	private JMenuItem snappeaItem;
	private JMenuItem matrixItem;
	private JMenuItem closeItem;
	private JMenuItem quitItem;
	private JMenu editMenu;
	private JMenuItem undoItem;
	private JMenuItem redoItem;
	private JMenuItem clearItem;
	private JMenuItem evalItem;
	private JMenuItem invertItem;
	private JMenuItem powerItem;
	private JMenu dehnMenu;
	private JMenuItem stdItem;
	private JMenuItem generalItem;
	private JMenuItem fixedItem;
	private JMenuItem lorenzItem;
	private JMenuItem randomItem;
	private JMenu trainMenu;
	private JMenuItem trainItem;
	private JMenuItem drawItem;
	private JMenuItem foldItem;
	private JMenuItem cleanItem;
	private JMenu snapMenu;
	private JMenuItem infoItem;
	private JMenuItem isomCensusItem;
	private JMenuItem isomFileItem;
	private JMenu helpMenu;
	private JMenuItem generalHelpItem;
	private JMenuItem twistHelpItem;
	private JMenuItem snappeaHelpItem;

	private JLabel statusBar;
	private JTextArea graphArea;
	private JButton updateButton;
	private JFileChooser chooser;
	private UndoManager undo;

	private MatrixDialog matrixDialog;
	private StdTwistDialog standardDialog;
	private GeneralTwistDialog generalDialog;
	private FixedTwistDialog fixedDialog;
	private LorenzKnotDialog lorenzDialog;
	private RandomDialog randomDialog;
	private FoldDialog foldDialog;

	private TrainTrack currentMap = null;
	private SnapPeaBridge snappeaBridge = null;
	private XTrainGraphics graphicsWindow = null;

	private static URLDisplayFrame generalHelpFrame;
	private static URLDisplayFrame twistHelpFrame;
	private static URLDisplayFrame snappeaHelpFrame;

	public XTrain() {
		undo = new UndoManager();
		chooser = new JFileChooser();

		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		openItem = new JMenuItem("Open");
		saveItem = new JMenuItem("Save");
		latexItem = new JMenuItem("Export to LaTeX");
		closeItem = new JMenuItem("Close");
		quitItem = MenuTools.createQuitItem();
		snappeaItem = new JMenuItem("Export to SnapPea");
		matrixItem = new JMenuItem("Export matrix");
		editMenu = new JMenu("Edit");
		undoItem = new JMenuItem("Undo");
		redoItem = new JMenuItem("Redo");
		clearItem = new JMenuItem("Clear");
		evalItem = new JMenuItem("Evaluate");
		invertItem = new JMenuItem("Invert");
		powerItem = new JMenuItem("Exponentiate");
		dehnMenu = new JMenu("Dehn twists");
		stdItem = new JMenuItem("Standard");
		generalItem = new JMenuItem("General (with bounday word)");
		fixedItem = new JMenuItem("General (with fixed word)");
		lorenzItem = new JMenuItem("Lorenz knot");
		randomItem = new JMenuItem("Random automorphism");
		trainMenu = new JMenu("Train tracks");
		trainItem = new JMenuItem("Compute train track");
		drawItem = new JMenuItem("Draw train track");
		foldItem = new JMenuItem("Fold edges");
		cleanItem = new JMenuItem("Tighten map");
		snapMenu = new JMenu("SnapPea");
		infoItem = new JMenuItem("Analyze mapping torus");
		isomCensusItem = new JMenuItem("Compare to census");
		isomFileItem = new JMenuItem("Compare to file");
		helpMenu = new JMenu("Help");
		generalHelpItem = new JMenuItem("Graphs");
		twistHelpItem = new JMenuItem("Dehn twists");
		snappeaHelpItem = new JMenuItem("SnapPea");

		statusBar = new JLabel();
		graphArea = new JTextArea(25, 80);
		updateButton = new JButton("Evaluate");

		graphArea.setFont(new Font("monospaced", 0, 12));
		
		menuBar.add(fileMenu);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(latexItem);
		fileMenu.add(snappeaItem);
		fileMenu.add(matrixItem);
		fileMenu.addSeparator();
		fileMenu.add(closeItem);
		if (quitItem!=null) {
			fileMenu.add(quitItem);
		}
		menuBar.add(editMenu);
		editMenu.add(undoItem);
		editMenu.add(redoItem);
		editMenu.add(clearItem);
		editMenu.add(evalItem);
		editMenu.addSeparator();
		editMenu.add(invertItem);
		editMenu.add(powerItem);
		menuBar.add(dehnMenu);
		dehnMenu.add(stdItem);
		dehnMenu.add(generalItem);
		dehnMenu.add(fixedItem);
		dehnMenu.add(lorenzItem);
		dehnMenu.addSeparator();
		dehnMenu.add(randomItem);
		menuBar.add(trainMenu);
		trainMenu.add(trainItem);
		trainMenu.add(drawItem);
		trainMenu.addSeparator();
		trainMenu.add(foldItem);
		trainMenu.add(cleanItem);
		menuBar.add(snapMenu);
		snapMenu.add(infoItem);
		snapMenu.add(isomCensusItem);
		snapMenu.add(isomFileItem);
		menuBar.add(helpMenu);
		helpMenu.add(generalHelpItem);
		helpMenu.add(twistHelpItem);
		helpMenu.add(snappeaHelpItem);

		setJMenuBar(menuBar);
		add(statusBar, BorderLayout.NORTH);
		add(new JScrollPane(graphArea), BorderLayout.CENTER);
		add(updateButton, BorderLayout.SOUTH);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("XTrain");
		pack();

		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		MenuTools.setShortCut(closeItem, KeyEvent.VK_W);
		graphArea.getDocument().addUndoableEditListener(undo);
		graphArea.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				clearMap();
			}
			public void insertUpdate(DocumentEvent e) {
				clearMap();
			}
			public void removeUpdate(DocumentEvent e) {
				clearMap();
			}
		});
		undoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					undo.undo();
				} catch(CannotUndoException ex) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		});
		MenuTools.setShortCut(undoItem, KeyEvent.VK_Z);
		redoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					undo.redo();
				} catch(CannotRedoException ex) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		});
		MenuTools.setShortCut(redoItem, KeyEvent.VK_Y);
		clearItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setMap(null);
			}
		});
		evalItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					setMap(getMap());
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		MenuTools.setShortCut(evalItem, KeyEvent.VK_E, KeyEvent.SHIFT_MASK);
		invertItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					TrainTrack g = new TrainTrack(getMap());
					if (g.invert()) {
						setMap(g);
					} else {
						JOptionPane.showMessageDialog(XTrain.this, "Map is not invertible!", "Not invertible!", JOptionPane.WARNING_MESSAGE);
					}
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		MenuTools.setShortCut(invertItem, KeyEvent.VK_I);
		powerItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = JOptionPane.showInputDialog(XTrain.this, "Exponent?", "1");
				if (s==null) return;
				try {
					int n = Integer.valueOf(s);
					TrainTrack g = new TrainTrack(getMap());
					if (n<0) {
						if (!g.invert()) {
							JOptionPane.showMessageDialog(XTrain.this, "Map is not invertible!", "Not invertible!", JOptionPane.WARNING_MESSAGE);
							return;
						}
						n = -n;
					}
					TrainTrack gg = new TrainTrack(g);
					gg.identity();
					for(; n>0; n--) {
						gg.compose(g);
					}
					setMap(gg);
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		MenuTools.setShortCut(powerItem, KeyEvent.VK_P, KeyEvent.SHIFT_MASK);
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = chooser.showOpenDialog(XTrain.this);
				if (result==JFileChooser.APPROVE_OPTION) {
					String name = chooser.getSelectedFile().getPath();
					try {
						GraphMap g = new GraphMap();
						g.readFromFile(name);
						setMap(new TrainTrack(g));
					} catch(Exception ex) {
						JOptionPane.showMessageDialog(XTrain.this, ex.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
						setMap(null);
					}
				}
			}
		});
		MenuTools.setShortCut(openItem, KeyEvent.VK_O);
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					writeToFile(getMap().toString());
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		MenuTools.setShortCut(saveItem, KeyEvent.VK_S);
		latexItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					writeToFile(getMap().toLaTeX());
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		snappeaItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					writeToFile(getSnapPea().getTriangulation());
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.getMessage(), "Exception!", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		matrixItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (matrixDialog==null) matrixDialog = new MatrixDialog(XTrain.this);
					String m = matrixDialog.getMatrix(getMap());
					if (m!=null) {
						writeToFile(m);
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		MenuTools.setShortCut(matrixItem, KeyEvent.VK_E);
		stdItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (standardDialog==null) standardDialog = new StdTwistDialog(XTrain.this);
				GraphMap g = standardDialog.getMap();
				if (g!=null) {
					setMap(new TrainTrack(g));
				}
			}
		});
		MenuTools.setShortCut(stdItem, KeyEvent.VK_1);
		generalItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (generalDialog==null) generalDialog = new GeneralTwistDialog(XTrain.this);
				GraphMap g = generalDialog.getMap();
				if (g!=null) {
					setMap(new TrainTrack(g));
				}
			}
		});
		MenuTools.setShortCut(generalItem, KeyEvent.VK_2);
		fixedItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fixedDialog==null) fixedDialog = new FixedTwistDialog(XTrain.this);
				GraphMap g = fixedDialog.getMap();
				if (g!=null) {
					setMap(new TrainTrack(g));
				}
			}
		});
		MenuTools.setShortCut(fixedItem, KeyEvent.VK_3);
		lorenzItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (lorenzDialog==null) lorenzDialog = new LorenzKnotDialog(XTrain.this);
				GraphMap g = lorenzDialog.getMap();
				if (g!=null) {
					setMap(new TrainTrack(g));
				}
			}
		});
		MenuTools.setShortCut(lorenzItem, KeyEvent.VK_4);
		randomItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (randomDialog==null) randomDialog = new RandomDialog(XTrain.this);
				GraphMap g = randomDialog.getMap();
				if (g!=null) {
					setMap(new TrainTrack(g));
				}
			}
		});
		MenuTools.setShortCut(randomItem, KeyEvent.VK_5);
		trainItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					getMap().trainTrackMap();
					setMap(currentMap);
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		MenuTools.setShortCut(trainItem, KeyEvent.VK_T);
		drawItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (graphicsWindow==null) {
						graphicsWindow = new XTrainGraphics();
					}
					updateGraphics(getMap());
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		MenuTools.setShortCut(drawItem, KeyEvent.VK_D);
		foldItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (foldDialog==null) foldDialog = new FoldDialog(XTrain.this);
					TrainTrack g = foldDialog.getFold(getMap());
					if (g!=null) {
						setMap(g);
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		MenuTools.setShortCut(foldItem, KeyEvent.VK_F, KeyEvent.SHIFT_MASK);
		cleanItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getMap().cleanItUp();
				setMap(currentMap);
			}
		});
		MenuTools.setShortCut(cleanItem, KeyEvent.VK_C, KeyEvent.SHIFT_MASK);
		infoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					String result = getSnapPea().getInfo();
					JOptionPane.showMessageDialog(XTrain.this, result, "SnapPea", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		MenuTools.setShortCut(infoItem, KeyEvent.VK_M);
		isomCensusItem.addActionListener(new ActionListener() {
			String name = "m016";
			public void actionPerformed(ActionEvent e) {
				try {
					SnapPeaBridge spb = getSnapPea();
					name = (String) JOptionPane.showInputDialog(XTrain.this, "Compare with (name from census)", name);
					if (name!=null) {
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						String result = spb.compareToCensus(name) ? "Isometric" : "Not isometric";
						JOptionPane.showMessageDialog(XTrain.this, result, "SnapPea", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		MenuTools.setShortCut(isomCensusItem, KeyEvent.VK_N);
		isomFileItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					SnapPeaBridge spb = getSnapPea();
					int val = chooser.showOpenDialog(XTrain.this);
					if (val==JFileChooser.APPROVE_OPTION) {
						String name = chooser.getSelectedFile().getPath();
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						String result = spb.compareToFile(name) ? "Isometric" : "Not isometric";
						JOptionPane.showMessageDialog(XTrain.this, result, "SnapPea", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					setMap(getMap());
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(XTrain.this, ex.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		generalHelpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (generalHelpFrame==null) {
					try {
						generalHelpFrame = new URLDisplayFrame("XTrain Help", getClass().getResource("docs/graph.html"));
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(XTrain.this, "Help text unavailable", "Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
				if (generalHelpFrame!=null) {
					generalHelpFrame.setVisible(true);
				}
			}
		});
		twistHelpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (twistHelpFrame==null) {
					try {
						twistHelpFrame = new URLDisplayFrame("XTrain Dehn Twist Help", getClass().getResource("docs/twists.html"));
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(XTrain.this, "Help text unavailable", "Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
				if (twistHelpFrame!=null) {
					twistHelpFrame.setVisible(true);
				}
			}
		});
		snappeaHelpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (snappeaHelpFrame==null) {
					try {
						snappeaHelpFrame = new URLDisplayFrame("XTrain-SnapPea Help", getClass().getResource("docs/snappea.html"));
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(XTrain.this, "Help text unavailable", "Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
				if (snappeaHelpFrame!=null) {
					snappeaHelpFrame.setVisible(true);
				}
			}
		});
	}

	private void clearMap() {
		currentMap = null;
		snappeaBridge = null;
		updateStatus(currentMap);
		if (graphicsWindow!=null && graphicsWindow.isVisible()) {
			graphicsWindow.setVisible(false);
		}
	}

	private void setMap(TrainTrack tt) {
		currentMap = tt;
		snappeaBridge = null;
		updateDisplays(tt);
	}

	private void updateDisplays(TrainTrack tt) {
		updateText(tt);
		if (graphicsWindow!=null && graphicsWindow.isVisible()) {
			updateGraphics(tt);
		}
		updateStatus(tt);
		Gates g = new Gates(tt);
		System.out.println(g);
	}

	private void updateText(TrainTrack tt) {
		if (tt==null) {
			graphArea.setText("");
		} else {
			graphArea.setText(tt.toString());
		}
	}

	private void updateStatus(TrainTrack tt) {
		String s = "";
		if (tt!=null) {
			s = "Train track: "+(tt.isTrainTrack() ? "yes" : "no");
			if (tt.isIrreducible()) {
				s += ", irreducible: yes, growth rate: "+IntMatrix.PFForm.format(tt.growthRate())+", char poly: "+IntMatrix.polyString(tt.transitionMatrix().reducedCharPoly());
			} else {
				s += ", irreducible: no";
			}
		}
		statusBar.setText(s);
	}

	private void updateGraphics(TrainTrack tt) {
		if (graphicsWindow==null) return;
		try {
			graphicsWindow.setTrainTrack(tt);
			graphicsWindow.setVisible(true);
		} catch(Exception ex) {
			graphicsWindow.setVisible(false);
			JOptionPane.showMessageDialog(this, "Unable to draw map! ("+ex.toString()+")", "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}

	private SnapPeaBridge getSnapPea() {
		return (snappeaBridge!=null) ? snappeaBridge : (snappeaBridge = new SnapPeaBridge(getMap()));
	}

	private TrainTrack getMap() {
		if (currentMap==null) {
			TrainTrack tt = new TrainTrack();
			tt.readGraph(new StringReader(graphArea.getText()));
			currentMap = tt;
		}
		return currentMap;
	}

	private void writeToFile(String s) {
		int result = chooser.showSaveDialog(this);
		if (result==JFileChooser.APPROVE_OPTION) {
			try {
				FileWriter fw = new FileWriter(chooser.getSelectedFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(s);
				bw.close();
			} catch(Exception ex) {
				JOptionPane.showMessageDialog(this, ex.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void dispose() {
		if (graphicsWindow!=null) {
			graphicsWindow.dispose();
		}
		super.dispose();
	}

	public static void main(String[] args) {
		System.setProperty("apple.laf.useScreenMenuBar", "true"); // hack for Mac OS...
		new XTrain().setVisible(true);
	}
}
