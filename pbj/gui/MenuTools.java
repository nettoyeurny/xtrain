package pbj.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MenuTools {

	private static final boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");
	private static final int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

	public static JMenuItem createQuitItem() {  // slightly unpleasant hack to address special case of Mac OS app menu...
		if (isMac) {
			return null;
		}
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		setShortCut(quitItem, KeyEvent.VK_Q);
		return quitItem;
	}

	public static void setShortCut(JMenuItem item, int key) {
		setShortCut(item, key, 0);
	}

	public static void setShortCut(JMenuItem item, int key, int mask) {
		item.setAccelerator(KeyStroke.getKeyStroke(key, mask | shortcutKeyMask));
	}
}
