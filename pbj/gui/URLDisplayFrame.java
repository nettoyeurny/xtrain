package pbj.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class URLDisplayFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public URLDisplayFrame(String title, URL helpURL) throws IOException {
		JMenuBar mb = new JMenuBar();
		JMenu fm = new JMenu("File");
		JMenuItem cl = new JMenuItem("Close");
		JMenuItem qu = MenuTools.createQuitItem();

		setJMenuBar(mb);
		mb.add(fm);
		fm.add(cl);
		if (qu!=null) {
			fm.add(qu);
		}
		
		cl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		MenuTools.setShortCut(cl, KeyEvent.VK_W);
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setPage(helpURL);
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		add(scrollPane);
		setTitle(title);
		pack();
	}
}
