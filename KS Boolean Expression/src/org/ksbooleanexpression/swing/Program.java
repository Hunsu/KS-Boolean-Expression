/*
# KS Boolean Expression, Copyright (c) 2012 The Authors. / ks.contrubutors@gmail.com
#
# This program is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free Software
# Foundation; either version 3 of the License, or (at your option) any later
# version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation,  Inc.,
# 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.ksbooleanexpression.swing;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.ksbooleanexpression.controller.Controller;
import org.ksbooleanexpression.controller.ControllerAction;
import org.ksbooleanexpression.controller.ResourceAction;
import org.ksbooleanexpression.preferences.UserPreferences;
import org.ksbooleanexpression.tools.Tools;
import org.ksbooleanexpression.tools.View;

/**
 *
 * @author Hamoudi, Meradi
 *
 */
public class Program implements View, TreeSelectionListener {

	private JFrame frame;
	private Controller controller;
	private UserPreferences userPreferences;
	private JPanel panel;
	private MainPanel mainPanel;
	private JTabbedPane tabbedPane;
	private JTree tree;
	private DefaultMutableTreeNode top;
	private ActionMap menuActionMap;
	private ActionMap actionMap;
	private JPopupMenu popupMenu;

	public static Image programIcon = Toolkit.getDefaultToolkit().getImage(
			Program.class.getClassLoader().getResource(
					"resources/Program_Icone.jpg"));

	/**
	 * Cr�er et l'interface de l'application
	 */

	public Program() {
		/*
		 * try { System.setErr(new PrintStream(new
		 * File(Tools.getApplicationFolder()+"debug.log"))); } catch
		 * (FileNotFoundException e) { }
		 */
		initialize();
	}

	/**
	 * Ajouter une action � un menu
	 *
	 * @param actionType
	 *            la cl� de l'action
	 * @param menu
	 *            la menu auquel on ajoute l'action
	 */

	private void addActionToMenu(ActionType actionType, JMenu menu) {
		Action action = getActionMap().get(actionType);
		if (action != null && action.getValue(Action.NAME) != null) {
			menu.add(new ResourceAction.MenuItemAction(action));
		}
	}

	/**
	 * Ajouter un boutton dont la cl� est <code>actionType</code> � la boite
	 * d'outils.
	 */
	private void addActionToToolBar(ActionType actionType, JToolBar toolBar) {
		Action action = getActionMap().get(actionType);
		if (action != null && action.getValue(Action.NAME) != null) {
			toolBar.add(new ResourceAction.ToolBarAction(action));
		}
	}

	/**
	 *
	 * @param b
	 */

	public void addTab(boolean b) {
		if (getMainPanel() == null)
			setMainPanel(new MainPanel(getUserPreferences()));
		getTabbedPane().addTab(Tools.getLocalizedString("PROJECT"), null,
				getMainPanel().getMainPane(tree, getController(), b), null);
		getFrame().repaint();
	}

	/**
	 * Cr�e un objet
	 * <code>ControllerAction<code> qui appelle dans <code>controller<code> </br>
	 * une m�thode donn�e avec ses param�tres
	 *
	 * @param actionType
	 *            le nom de l'action � cr�er
	 * @param preferences
	 *            les pr�f�rences de l'utilisateur
	 * @param controller
	 *            le controlleur dans lequel se trouve la m�thode � appeler
	 * @param method
	 *            le nom de la m�thode � appeler
	 * @param parameters
	 *            les param�tres de la m�thode
	 *
	 */
	private void createAction(ActionType actionType,
			UserPreferences preferences, Object controller, String method,
			Object... parameters) {
		try {
			getActionMap().put(
					actionType,
					new ControllerAction(preferences, this.getClass(),
							actionType.name(), controller, method, parameters));
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Cr�er des actions d'un �l�ment d'un menu
	 */
	private void createActions() {
		createAction(ActionType.NEW, getUserPreferences(), getController(),
				"newProject");
		createAction(ActionType.OPEN, getUserPreferences(), getController(),
				"open");
		createAction(ActionType.SAVE, getUserPreferences(), getController(),
				"save");
		createAction(ActionType.SAVE_AS, getUserPreferences(), getController(),
				"saveAs");
		createAction(ActionType.PREFERENCES, getUserPreferences(),
				getController(), "preferences");
		createAction(ActionType.PRINT, getUserPreferences(), getController(),
				"print");
		createAction(ActionType.EXIT, getUserPreferences(), getController(),
				"exit");
		createAction(ActionType.PRINT, getUserPreferences(), getController(),
				"print");
		createAction(ActionType.UNDO, getUserPreferences(), getController(),
				"undo");
		createAction(ActionType.REDO, getUserPreferences(), getController(),
				"redo");
		createAction(ActionType.CUT, getUserPreferences(), getController(),
				"cut");
		createAction(ActionType.COPY, getUserPreferences(), getController(),
				"copy");
		createAction(ActionType.PASTE, getUserPreferences(), getController(),
				"paste");
		createAction(ActionType.SELECT_ALL, getUserPreferences(),
				getController(), "selectAll");
		createAction(ActionType.DELETE, getUserPreferences(), getController(),
				"delete");
		createAction(ActionType.KARNAUGH_MAP, getUserPreferences(),
				getController(), "newKarnaughMap");
		createAction(ActionType.TRUTH_TABLE, getUserPreferences(),
				getController(), "newTruthTable");
		createAction(ActionType.ALGEBRIC_FORM, getUserPreferences(),
				getController(), "algebricForm");
		createAction(ActionType.NUMERIC_FORM, getUserPreferences(),
				getController(), "numericForm");
		createAction(ActionType.IMPORT, getUserPreferences(), getController(),
				"importFunctions");
		createAction(ActionType.EXPORT, getUserPreferences(), getController(),
				"export");
		createAction(ActionType.MINIMIZED_FUNCTION, getUserPreferences(),
				getController(), "getSimpleSolution");
		createAction(ActionType.DETAILED_SOLUTION, getUserPreferences(),
				getController(), "getDetailedSolution");
		createAction(ActionType.HELP, getUserPreferences(), getController(),
				"help");
		createAction(ActionType.ABOUT, getUserPreferences(), getController(),
				"about");
		createAction(ActionType.AND, getUserPreferences(), getController(),
				"printSymbol", 0);
		createAction(ActionType.OR, getUserPreferences(), getController(),
				"printSymbol", 1);
		createAction(ActionType.NOT, getUserPreferences(), getController(),
				"printSymbol", 2);
		createAction(ActionType.NAND, getUserPreferences(), getController(),
				"printSymbol", 3);
		createAction(ActionType.NOR, getUserPreferences(), getController(),
				"printSymbol", 4);
		createAction(ActionType.XOR, getUserPreferences(), getController(),
				"printSymbol", 5);

	}

	/**
	 * Cr�er une action d'un menu
	 *
	 * @param preferences
	 *            les pr�f�rences de l'utilisateur
	 * @param action
	 *            le nom de l'action
	 */
	private void createMenuAction(UserPreferences preferences,
			MenuActionType action) {
		menuActionMap.put(action, new ResourceAction(preferences,
				action.name(), true));
	}

	/**
	 * Cr�er les actions Create <code>menuActionMap</code> utilis� pour cr�er
	 * les menus.
	 */
	private void createMenuActions(UserPreferences preferences,
			Controller controller) {
		menuActionMap = new ActionMap();
		createMenuAction(preferences, MenuActionType.FILE_MENU);
		createMenuAction(preferences, MenuActionType.EDIT_MENU);
		createMenuAction(preferences, MenuActionType.ADD_MENU);
		createMenuAction(preferences, MenuActionType.NEW_FUNCTION_MENU);
		createMenuAction(preferences, MenuActionType.NEW_TABLE_MENU);
		createMenuAction(preferences, MenuActionType.SIMPLIFY_MENU);
		createMenuAction(preferences, MenuActionType.HELP_MENU);

	}

	/**
	 * Retourner la barre de menu afficher dans cette application
	 */
	private JMenuBar createMenuBar(UserPreferences preferences,
			Controller controller) {

		// Cr�ation du menu Fichier
		JMenu fileMenu = new JMenu(
				this.menuActionMap.get(MenuActionType.FILE_MENU));
		addActionToMenu(ActionType.NEW, fileMenu);
		addActionToMenu(ActionType.OPEN, fileMenu);

		fileMenu.addSeparator();
		addActionToMenu(ActionType.CLOSE, fileMenu);
		addActionToMenu(ActionType.SAVE, fileMenu);
		addActionToMenu(ActionType.SAVE_AS, fileMenu);
		fileMenu.addSeparator();
		addActionToMenu(ActionType.PREFERENCES, fileMenu);
		addActionToMenu(ActionType.EXPORT, fileMenu);
		addActionToMenu(ActionType.PRINT, fileMenu);
		addActionToMenu(ActionType.EXIT, fileMenu);

		// Cr�ation du menu Edition
		JMenu editMenu = new JMenu(
				this.menuActionMap.get(MenuActionType.EDIT_MENU));

		addActionToMenu(ActionType.UNDO, editMenu);
		addActionToMenu(ActionType.REDO, editMenu);
		editMenu.addSeparator();
		addActionToMenu(ActionType.CUT, editMenu);
		addActionToMenu(ActionType.COPY, editMenu);
		addActionToMenu(ActionType.PASTE, editMenu);
		editMenu.addSeparator();
		addActionToMenu(ActionType.DELETE, editMenu);
		addActionToMenu(ActionType.SELECT_ALL, editMenu);

		// Cr�ation du menu Ajouter
		JMenu addMenu = new JMenu(
				this.menuActionMap.get(MenuActionType.ADD_MENU));
		addMenu.add(createNewTableMenu());
		addMenu.add(createNewFunctionMenu());
		addActionToMenu(ActionType.IMPORT, addMenu);

		// Cr�ation du menu Simplifier
		JMenu simplifyMenu = new JMenu(
				this.menuActionMap.get(MenuActionType.SIMPLIFY_MENU));
		addActionToMenu(ActionType.MINIMIZED_FUNCTION, simplifyMenu);
		addActionToMenu(ActionType.DETAILED_SOLUTION, simplifyMenu);

		// Cr�ation du menu Aide
		JMenu helpMenu = new JMenu(
				this.menuActionMap.get(MenuActionType.HELP_MENU));
		addActionToMenu(ActionType.HELP, helpMenu);
		addActionToMenu(ActionType.ABOUT, helpMenu);

		// cr�ation de la bar de menus
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(addMenu);
		menuBar.add(simplifyMenu);
		menuBar.add(helpMenu);
		return menuBar;
	}

	/**
	 * Cr�er le sous menu Nouvelle Fonction
	 */
	private JMenu createNewFunctionMenu() {
		JMenu newFunctionMenu = new JMenu(
				this.menuActionMap.get(MenuActionType.NEW_FUNCTION_MENU));
		addActionToMenu(ActionType.ALGEBRIC_FORM, newFunctionMenu);
		addActionToMenu(ActionType.NUMERIC_FORM, newFunctionMenu);
		return newFunctionMenu;
	}

	/**
	 * Cr�er le sous menu Nouvelle Table
	 */
	private JMenu createNewTableMenu() {
		JMenu newTableMenu = new JMenu(
				this.menuActionMap.get(MenuActionType.NEW_TABLE_MENU));
		addActionToMenu(ActionType.TRUTH_TABLE, newTableMenu);
		addActionToMenu(ActionType.KARNAUGH_MAP, newTableMenu);
		return newTableMenu;
	}

	private void createPopupMenu() {
		popupMenu = new JPopupMenu();
		popupMenu.add(getActionMap().get(ActionType.COPY));
		popupMenu.add(getActionMap().get(ActionType.CUT));
		popupMenu.add(getActionMap().get(ActionType.PASTE));

	}

	/**
	 * Retourner la boite d'outils
	 */
	private JToolBar createToolBar() {
		final JToolBar toolBar = new JToolBar();
		addActionToToolBar(ActionType.NEW, toolBar);
		addActionToToolBar(ActionType.OPEN, toolBar);
		addActionToToolBar(ActionType.SAVE, toolBar);
		toolBar.addSeparator();

		addActionToToolBar(ActionType.UNDO, toolBar);
		addActionToToolBar(ActionType.REDO, toolBar);
		toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
		addActionToToolBar(ActionType.CUT, toolBar);
		addActionToToolBar(ActionType.COPY, toolBar);
		addActionToToolBar(ActionType.PASTE, toolBar);
		toolBar.addSeparator();
		addActionToToolBar(ActionType.AND, toolBar);
		addActionToToolBar(ActionType.OR, toolBar);
		addActionToToolBar(ActionType.NOT, toolBar);
		addActionToToolBar(ActionType.NAND, toolBar);
		addActionToToolBar(ActionType.NOR, toolBar);
		addActionToToolBar(ActionType.XOR, toolBar);
		toolBar.addSeparator();
		addActionToToolBar(ActionType.CLOSE, toolBar);

		return toolBar;
	}

	/**
	 * Cr�er le JTree afficher dans l'application
	 *
	 * @param nodes
	 *            le nombre des noeuds
	 */
	public void createTree(int nodes) {
		top = new DefaultMutableTreeNode(
				Tools.getLocalizedString("FUNCTION_EXPLORATOR"));
		for (int i = 0; i < nodes; i++) {
			DefaultMutableTreeNode function = new DefaultMutableTreeNode(
					Tools.getLocalizedString("FUNCTION")
							+ String.valueOf(i + 1));
			top.add(function);
		}
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
		tree.addTreeSelectionListener(this);

		tree.setSize(new Dimension(200, 200));
	}

	/**
	 * Retourner l'ensemble des actions
	 */
	private ActionMap getActionMap() {
		if (actionMap == null)
			actionMap = new ActionMap();
		return actionMap;
	}

	public Controller getController() {
		return controller;
	}

	public JFrame getFrame() {
		return frame;
	}

	public MainPanel getMainPanel() {
		return mainPanel;
	}

	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public UserPreferences getUserPreferences() {
		return userPreferences;
	}

	/**
	 * Initialise les composants de la fen�tre principale de l'application.
	 */
	private void initialize() {
		setUserPreferences(new UserPreferences());
		try {
			UIManager.setLookAndFeel(getUserPreferences().getLookAndFeel());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setController(new Controller(this, getUserPreferences()));

		setFrame(new JFrame());
		setMainPanel(new MainPanel(getUserPreferences()));
		if (getUserPreferences().getHeight() != 0
				&& getUserPreferences().getWidth() != 0) {
			getFrame().setSize(getUserPreferences().getHeight(),
					getUserPreferences().getWidth());
		} else {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			getFrame().setSize((int) ((0.75) * d.width),
					(int) ((0.75) * d.height));
		}
		if (getUserPreferences().isMaximazed())
			getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);

		getFrame().setLocationRelativeTo(null);
		getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getFrame().addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				getUserPreferences().setMaximazed(
						getFrame().getExtendedState() == Frame.MAXIMIZED_BOTH);
				try {
					getUserPreferences().savePreferences();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				getController().exit();
			}
		});
		;
		getFrame().setTitle("KS Boolean Expression");
		getFrame().setIconImage(programIcon);
		getFrame().getContentPane().setLayout(new BorderLayout(0, 0));

		createActions();
		createMenuActions(getUserPreferences(), getController());
		createPopupMenu();

		panel = new JPanel();
		getFrame().getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		setTabbedPane(new JTabbedPane(JTabbedPane.TOP));
		addTab(true);
		mainPanel.getTextField().setFont(
				new java.awt.Font("Calibri", java.awt.Font.ITALIC, 12));
		mainPanel.getTextField().setText(
				Tools.getLocalizedString("EXAMPLE")
						+ " (ABCD+ABC*!D)|(B*!C*D+A/E)");
		mainPanel.getTextField().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				mainPanel.getTextField().setText("");
				mainPanel.getTextField().setFont(
						new java.awt.Font("Calibri", java.awt.Font.PLAIN, 12));
				mainPanel.getTextField().removeMouseListener(this);

			}
		});
		mainPanel.getTextField().setComponentPopupMenu(popupMenu);
		mainPanel.getTextField().addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
			}

			public void mouseEntered(MouseEvent event) {
			}

			public void mouseExited(MouseEvent event) {
			}

			public void mousePressed(MouseEvent event) {
			}

			public void mouseReleased(MouseEvent event) {
				if (event.isPopupTrigger())
					popupMenu.show(event.getComponent(), event.getX(),
							event.getY());
			}

		});

		panel.add(getTabbedPane(), BorderLayout.CENTER);
		panel.add(createToolBar(), BorderLayout.NORTH);

		JMenuBar menuBar = createMenuBar(getUserPreferences(), getController());
		getFrame().getContentPane().add(menuBar, BorderLayout.NORTH);
		JMenuBar homeMenuBar = createMenuBar(getUserPreferences(),
				getController());
		getFrame().add(homeMenuBar, BorderLayout.NORTH);
		getFrame().setVisible(true);
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	/**
	 * Activer ou désactiver une action dont la clé est <code>actionType</code>.
	 */
	public void setEnabled(ActionType actionType, boolean enabled) {
		Action action = getActionMap().get(actionType);
		if (action != null) {
			action.setEnabled(enabled);
		}
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public void setMainPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public void setTabbedPane(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

	public void setUserPreferences(UserPreferences userPreferences) {
		this.userPreferences = userPreferences;
	}

	/**
	 * Cr�er et afficher la boite de dialogue A propos (About).
	 */
	public void showAboutDialog() {
		String messageFormat = Tools.getLocalizedString("about.message");
		String aboutVersion = this.getController().getVersion();
		String message = String.format(messageFormat, aboutVersion,
				System.getProperty("java.version"));
		JEditorPane messagePane = new JEditorPane("text/html", message);
		messagePane.setOpaque(false);
		messagePane.setEditable(false);
		messagePane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent ev) {
				if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

					Desktop desktop = Desktop.getDesktop();

					try {
						desktop.browse(ev.getURL().toURI());
					} catch (Exception e) {

						System.err.println(e.getMessage());
					}
				}
			}
		});

		String title = Tools.getLocalizedString("about.title");
		Icon icon = new ImageIcon(this.getClass().getClassLoader()
				.getResource("resources/Program_Icone.jpg"));
		JOptionPane.showMessageDialog(getFrame(), messagePane, title,
				JOptionPane.INFORMATION_MESSAGE, icon);
	}

	/**
	 *
	 */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();

		if (node == null)
			return;
		int nodeInfo = top.getIndex(node);
		String chemin = "function" + String.valueOf(nodeInfo + 1);
		getMainPanel().setDocumentRelative(chemin);
	}

}
