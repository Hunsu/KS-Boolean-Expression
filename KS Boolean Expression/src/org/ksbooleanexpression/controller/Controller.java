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

package org.ksbooleanexpression.controller;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.ksbooleanexpression.preferences.UserPreferences;
import org.ksbooleanexpression.simplification.Simplification;
import org.ksbooleanexpression.swing.KarnaughMapPanel;
import org.ksbooleanexpression.swing.NumericFunctionPanel;
import org.ksbooleanexpression.swing.PreferencePanel;
import org.ksbooleanexpression.swing.Program;
import org.ksbooleanexpression.swing.TruthTablePanel;
import org.ksbooleanexpression.tools.Tools;
import org.ksbooleanexpression.tools.View;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.XHTMLPrintable;

import com.lowagie.text.DocumentException;

/**
 * Un controller pour l'application<br>
 *
 * @author Meradi, Hamoudi
 *
 */

public class Controller implements View {
	public Program program;
	public UserPreferences preferences;
	private File fileName;
	private boolean isChanged;

	public Controller(Program program, UserPreferences prefs) {
		this.program = program;
		preferences = prefs;
		preferences.addPropertyChangeListener(
				new LanguageChangeListener(this));
	}

	/**
	 * Ouvre la boite de dialogue A propos (About)
	 *
	 * @throws FileNotFoundException
	 */
	public void about() throws FileNotFoundException {
		program.showAboutDialog();

	}

	/**
	 * Permet d'ajouter une nouvelle fonction de</br> de forme alg�brique.
	 */
	public void algebricForm() {
		program.getMainPanel().getTextField().setText("");
		program.addTab(false);
		program.setEnabled(View.ActionType.PRINT, false);
		program.setEnabled(View.ActionType.SAVE, false);
		program.setEnabled(View.ActionType.SAVE_AS, false);
		program.setEnabled(View.ActionType.EXPORT, false);
		program.setEnabled(View.ActionType.MINIMIZED_FUNCTION, false);
		program.setEnabled(View.ActionType.DETAILED_SOLUTION, false);

	}

	public void copy() {
		program.getMainPanel().getTextField().copy();
	}

	public void cut() {
		program.getMainPanel().getTextField().cut();
	}

	/**
	 * Fonction supprimer. Pour supprimer le</br> le text selectionn�.
	 */
	public void delete() {
		StringBuffer s = new StringBuffer(program.getMainPanel().getTextField()
				.getText());
		int begin = program
				.getMainPanel()
				.getTextField()
				.getText()
				.indexOf(
						program.getMainPanel().getTextField().getSelectedText());
		int end = begin
				+ program.getMainPanel().getTextField().getSelectedText()
						.length();
		s.delete(begin, end);
		program.getMainPanel().getTextField().setText(s.toString());
	}

	/**
	 *
	 * Active les fonctionnalit�s de l'application � l'initialisation.
	 */
	public void enableDefaultActions(Program program) {

		program.setEnabled(View.ActionType.NEW, true);
		program.setEnabled(View.ActionType.OPEN, true);
		program.setEnabled(View.ActionType.PREFERENCES, true);
		program.setEnabled(View.ActionType.EXIT, true);
		program.setEnabled(ActionType.CUT, true);
		program.setEnabled(ActionType.COPY, true);
		program.setEnabled(ActionType.PASTE, true);
		program.setEnabled(ActionType.SELECT_ALL, true);
		program.setEnabled(ActionType.DELETE, true);
		program.setEnabled(View.ActionType.KARNAUGH_MAP, true);
		program.setEnabled(View.ActionType.TRUTH_TABLE, true);
		program.setEnabled(View.ActionType.ALGEBRIC_FORM, true);
		program.setEnabled(View.ActionType.NUMERIC_FORM, true);
		program.setEnabled(View.ActionType.HELP, true);
		program.setEnabled(View.ActionType.ABOUT, true);
		program.setEnabled(View.ActionType.AND, true);
		program.setEnabled(View.ActionType.OR, true);
		program.setEnabled(View.ActionType.NOT, true);
		program.setEnabled(View.ActionType.NAND, true);
		program.setEnabled(View.ActionType.XOR, true);
		program.setEnabled(View.ActionType.NOR, true);
		program.setEnabled(View.ActionType.CLOSE, true);
		program.setEnabled(View.ActionType.DELETE, true);
		program.setEnabled(View.ActionType.IMPORT, true);
		program.getMainPanel().getValidationButton().setEnabled(false);
		program.setEnabled(View.ActionType.UNDO, false);
		program.setEnabled(View.ActionType.REDO, false);
	}

	/**
	 * Quitte l'application. En cas de modification</br> du projet il sera
	 * demand� � l'utilisateur de </br> sauvegarder son travail avant de
	 * quitter.
	 */
	public void exit() {

		if (isChanged) {
			int s = Tools.showExitDialog();
			if (s == JOptionPane.YES_OPTION) {
				save();
				System.exit(0);
			}
			if (s == JOptionPane.NO_OPTION)
				System.exit(0);
		} else
			System.exit(0);

	}

	/**
	 * Permet d'exporter le projet en cours vers un fichier PDF
	 */
	public void export() {
		File f = Tools.showSaveDialog(Tools
				.getLocalizedString(ActionType.EXPORT.name() + ".Name"));
		if (f != null) {
			try {
				String url = Tools.getApplicationFolder() + "temp/temp.html";
				File file = new File(url);
				if (file.exists()) {
					url = file.toURI().toURL().toString();
				}
				String chemin = f.getAbsolutePath();
				if (!chemin.endsWith(".pdf"))
					chemin += ".pdf";
				Tools.createPDF(url, chemin);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			} catch (DocumentException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * Donne le r�sultat de la simplification sous</br> forme d'une fonction +
	 * sa table de karnaugh</br> en cas d'une seul fonction � simplifi�e on
	 * affichera</br> aussi sa table de v�rit�.
	 */
	public void getDetailedSolution() {
		simplify(SolutionType.DETAILLED_SOLUTION);
	}

	/**
	 * Donne le r�sultat de la simplification sous</br> forme d'une fonction
	 * r�duite uniquement (sans</br> la table de v�rit� ni la table de
	 * Karnaugh).
	 */
	public void getSimpleSolution() {
		simplify(SolutionType.MINIMIZED_FUNCTION);
	}

	/**
	 * Retourne la version de l'application
	 *
	 * @return
	 */
	public String getVersion() {
		return "1.0.2";
	}

	/**
	 * Affiche l'index (Aide)
	 */
	public void help() {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle(Tools.getLocalizedString("HELP_DIALOG_TITLE"));
		frame.setIconImage(Program.programIcon);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) ((0.65) * d.width), (int) ((0.65) * d.height));
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		frame.setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		String url = "help/" + preferences.getLanguage() + "/index.htm";
		XHTMLPanel xHTMLPanel = new XHTMLPanel();
		FSScrollPane scroll = new FSScrollPane(xHTMLPanel);
		panel.add(scroll, BorderLayout.CENTER);
		try {
			xHTMLPanel.setDocument(new File(url));
			frame.setVisible(true);
		} catch (Exception e1) {
			unavailableHelp();
		}
	}

	/**
	 * Permet d'importer des fonctions � partir</br> d'un fichier text. Des
	 * message d'erreur sont</br> pr�vus en cas d'extension diff�rente .txt ou
	 * bien</br> le fichier selectionn� est vide.
	 */
	public void importFunctions() {
		File f = Tools.showOpenDialog("Importer");
		if (f != null) {
			String ext = f.getName().substring(f.getName().length() - 3,
					f.getName().length());
			if (ext.compareTo("txt") != 0)
				JOptionPane.showMessageDialog(
						null,
						Tools.getLocalizedString("ERROR_IMPORT") + " "
								+ f.getName() + " "
								+ Tools.getLocalizedString("ERROR_IMPORT2"),
						Tools.getLocalizedString("ERROR"),
						JOptionPane.ERROR_MESSAGE);

			else {
				String functions = "";
				try {
					BufferedReader br = new BufferedReader(new FileReader(f));
					String line = br.readLine();
					while (line != null) {
						functions += line + ";";
						line = br.readLine();
					}
					isChanged = false;
					br.close();
					functions = functions.substring(0, functions.length() - 1);

				} catch (FileNotFoundException e) {
					System.err.println(e.getMessage());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}

				if (functions.length() != 0) {
					program.getMainPanel().getTextField().setText(functions);
					simplify(SolutionType.DETAILLED_SOLUTION);
				} else {
					JOptionPane
							.showMessageDialog(
									null,
									Tools.getLocalizedString("ERROR_IMPORT")
											+ " "
											+ f.getName()
											+ " "
											+ Tools.getLocalizedString("ERROR_IMPORT3"),
									Tools.getLocalizedString("ERROR"),
									JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 * Ouvre la boite de dialogue qui permet d'ajouter</br> une nouvelle table
	 * de Karnaugh.
	 */
	public void newKarnaughMap() {
		@SuppressWarnings("unused")
		KarnaughMapPanel kmap = new KarnaughMapPanel(this);

	}

	/**
	 * Permet de cr�er un nouveau projet, en cas</br> de modification du projet
	 * en cours un message sera</br> affich� � l'utilisateur lui demandant
	 * d'enregistrer </br> son travail.
	 *
	 * @throws IOException
	 */
	public void newProject() throws IOException {
		boolean canceled = false;
		if (isChanged) {
			Object[] options = { Tools.getLocalizedString("YES"),
					Tools.getLocalizedString("NO") };
			JFrame frame = new JFrame();
			int n = JOptionPane.showOptionDialog(frame,
					Tools.getLocalizedString("EXIT_DIALOGUE"),
					"KS Boolean Expression", JOptionPane.YES_NO_OPTION,
					JOptionPane.YES_NO_OPTION, null, options, options[0]);
			if (n == JOptionPane.YES_OPTION) {
				save();
				if (isChanged)
					canceled = true;
			}
		}
		if (!canceled) {
			algebricForm();
			fileName = null;
			isChanged = false;

		}

	}

	/**
	 * Ouvre la boite de dialogue qui permet d'ajouter</br> une nouvelle table
	 * de v�rit�.
	 */
	public void newTruthTable() {
		@SuppressWarnings("unused")
		TruthTablePanel table = new TruthTablePanel(this);

	}

	/**
	 * Permet d'ajouter une nouvelle fonction de</br> de forme num�rique.
	 */
	public void numericForm() {
		@SuppressWarnings("unused")
		NumericFunctionPanel digitalForm = new NumericFunctionPanel(this);

	}

	/**
	 * Permet d'ouvrir un projet déjà enregistré</br> Un message d'erreur est
	 * prévue en cas d'une</br> extension non prise en charge par l'application.
	 */
	public void open() {

		File f = Tools.showOpenDialog(Tools.getLocalizedString(ActionType.OPEN
				.name() + ".Name"));
		if(f != null)
			open(f.getAbsolutePath());

	}

	/**
	 * Ouvre le fichier dont le chemin est path.
	 *
	 * @param path
	 *            chemin
	 */
	public void open(String path) {
		File f = new File(path);
		String functions = "";
		if (f != null) {
			String ext = f.getName().substring(f.getName().length() - 2,
					f.getName().length());
			if (ext.compareTo("ks") != 0)
				JOptionPane.showMessageDialog(
						null,
						Tools.getLocalizedString("ERROR_OPENING") + " "
								+ f.getName() + " "
								+ Tools.getLocalizedString("ERROR_OPENING2"),
						Tools.getLocalizedString("ERROR"),
						JOptionPane.ERROR_MESSAGE);
			else {
				try {
					BufferedReader br = new BufferedReader(new FileReader(f));
					functions = br.readLine();
					isChanged = false;
					br.close();
				} catch (FileNotFoundException e1) {
				} catch (IOException e1) {
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
		program.getMainPanel().getTextField().setText(functions);
		simplify(SolutionType.DETAILLED_SOLUTION);
		isChanged = false;

	}

	public void paste() {
		program.getMainPanel().getTextField().paste();
	}

	/**
	 * Ouvre la boite de dialogue Preferences</br>
	 */

	public void preferences() {
		PreferencePanel dialog = new PreferencePanel(
				this.program.getUserPreferences(), this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		// program.setEtatInitial();

	}

	/**
	 * Permet d'imprimer le projet en cours.
	 */

	public void print() {
		final PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(new XHTMLPrintable(program.getMainPanel()
				.getxHTMLPanel()));

		if (printJob.printDialog()) {
			new Thread(new Runnable() {
				public void run() {
					try {
						printJob.print();
					} catch (PrinterException ex) {
						System.err.println(ex.getMessage());
					}
				}
			}).start();
		}
	}

	/**
	 * Permet d'�crire le symbole voulu en pressant sur les</br> diff�rents
	 * bouttons de la bar des symbole.
	 *
	 * @param type
	 */
	public void printSymbol(Integer type) {
		String string = program.getMainPanel().getTextField().getText();
		string = string.substring(0, program.getMainPanel().getTextField()
				.getCaretPosition());
		switch (type) {
		case 0:
			string = string + "*";
			break;
		case 1:
			string = string + "+";
			break;
		case 2:
			string = string + "!";
			;
			break;
		case 3:
			string = string + "|";
			;
			break;
		case 4:
			string = string + "/";
			;
			break;
		case 5:
			string = string + "^";
			;
			break;
		}
		string = string
				+ program
						.getMainPanel()
						.getTextField()
						.getText()
						.substring(
								string.length() - 1,
								program.getMainPanel().getTextField().getText()
										.length());
		program.getMainPanel().getTextField().setText(string);
		program.getMainPanel().getTextField().requestFocus();
	}

	/**
	 * Fonction R�tablir
	 */
	public void redo() {
		if (program.getMainPanel().getUndo().canRedo())
			program.getMainPanel().getUndo().redo();
		updateRedoState();
		updateUndoState();

	}

	/**
	 * Permet de sauvegarder le projet en cours.
	 */
	public void save() {
		String title = Tools.getLocalizedString(ActionType.SAVE.name()
				+ ".Name");
		String fonctions = program.getMainPanel().getTextField().getText();
		try {
			if (fileName == null) {
				String file = Tools.showSaveDialog(title).getAbsolutePath();
				if (!file.endsWith(".ks"))
					file += ".ks";
				fileName = new File(file);
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.append(fonctions);
			bw.newLine();
			bw.close();
			isChanged = false;
			program.setEnabled(ActionType.SAVE, false);
			program.setEnabled(ActionType.SAVE_AS, true);
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	/**
	 * Permet de sauvegarder le projet en cours.</br>
	 *
	 */
	public void saveAs() {
		try {
			String chemin = Tools.showSaveDialog(
					Tools.getLocalizedString(ActionType.SAVE_AS.name()
							+ ".Name")).getAbsolutePath();
			if (!chemin.endsWith(".ks"))
				chemin += ".ks";
			fileName = new File(chemin);
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.append(program.getMainPanel().getTextField().getText());
			bw.close();
			isChanged = false;
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	public void selectAll() {
		program.getMainPanel().getTextField().selectAll();
	}

	/**
	 * Permet d'afficher le r�sultat de la simplifiication</br>
	 *
	 */
	private void showSolution() {
		program.getTabbedPane().remove(0);
		program.addTab(false);
		try {
			program.getMainPanel().getxHTMLPanel()
					.setDocument(new File("temp/temp.html"));

			program.setEnabled(View.ActionType.SAVE, true);
			program.setEnabled(View.ActionType.SAVE_AS, true);
			program.setEnabled(View.ActionType.PRINT, true);
			program.setEnabled(View.ActionType.EXPORT, true);
			isChanged = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	/**
	 * Lance la simplification � partir d'une expression</br> introduite dans le
	 * JTextField.
	 *
	 * @param solutionType
	 *            type de la solution (simple ou d�taill�e)
	 */
	public void simplify(SolutionType solutionType) {
		String[] functions = program.getMainPanel().getTextField().getText()
				.split(";");
		if (functions.length == 1
				&& functions[0].startsWith(Tools
						.getLocalizedLabelText("EXAMPLE")))
			return;
		Simplification s = new Simplification(functions, solutionType, this);
		s.launchSimplification(Type.FUNCTION);
		program.createTree(functions.length);
		showSolution();
	}

	/**
	 * Simplifie une fonction donn�e sous frome num�rique
	 *
	 * @param sets0
	 * @param sets1
	 */
	public void simplifyNumericFunction(String[] sets0, String[] sets1,
			int nbrVar, String varOrder) {

		int table[] = new int[(int) Math.pow(2, nbrVar)];
		if (sets1 == null && sets0 == null)
			JOptionPane.showMessageDialog(null,
					Tools.getLocalizedString("ERROR_NUMFORM"),
					Tools.getLocalizedString("ERROR"),
					JOptionPane.ERROR_MESSAGE);
		else {
			if (sets1 != null) {
				for (int j = 0; j < table.length; j++)
					table[j] = 0;

				for (int i = 0; i < sets1.length; i++) {
					table[Integer.valueOf(sets1[i])] = 1;
				}
			} else if (sets0 != null) {
				for (int j = 0; j < table.length; j++)
					table[j] = 0;

				for (int i = 0; i < sets0.length; i++) {
					table[Integer.valueOf(sets0[i])] = 1;
				}
			}
			solveFromTruthTable(table, nbrVar, varOrder);
		}

	}

	/**
	 * Lance la simplification � partir d'une table</br> de Karnaugh.
	 *
	 * @param kmap
	 *            la table de Karnaugh
	 * @param nbrVar
	 *            le nombre de variables
	 * @param varOrder
	 *            l'ordre des variables
	 */
	public void solveFromKMap(int kmap[][], int nbrVar, String varOrder) {
		Simplification simplify = new Simplification(kmap, nbrVar, varOrder,
				this);
		simplify.launchSimplification(Type.KARNAUGH_MAP);
		program.createTree(1);
		showSolution();

	}

	/**
	 * Lance la simplification � partir d'une table</br> de v�rit�.
	 *
	 * @param ttable
	 *            la table de v�rit�
	 * @param nbrVar
	 *            le nombre de variables
	 * @param varOrder
	 *            l'ordre des variables
	 */
	public void solveFromTruthTable(int ttable[], int nbrVar, String varOrder) {
		Simplification simplify = new Simplification(ttable, nbrVar, varOrder,
				this);
		simplify.launchSimplification(Type.TRUTH_TABLE);
		program.createTree(1);
		showSolution();

	}

	/**
	 * Pr�vient l'utilisateur par 'interm�diaire d'une boite<br>
	 * de dialogue lorsque l'aide n'est pas disponible pour une<br>
	 * une langue donn�e.
	 */
	public void unavailableHelp() {
		JEditorPane messagePane = new JEditorPane("text/html",
				Tools.getLocalizedString("UNAVAILABLE_HELP"));
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
		JOptionPane.showMessageDialog(program.getFrame(), messagePane,
				Tools.getLocalizedString("HELP_DIALOG_TITLE"),
				JOptionPane.OK_OPTION, null);
	}

	/**
	 * Fonction annuler
	 */
	public void undo() {
		if (program.getMainPanel().getUndo().canUndo())
			program.getMainPanel().getUndo().undo();
		updateUndoState();
		updateRedoState();

	}

	/**
	 * Function updateRedoState
	 */
	public void updateRedoState() {
		if (program.getMainPanel().getUndo().canRedo()) {
			program.setEnabled(ActionType.REDO, true);
		} else {
			program.setEnabled(ActionType.REDO, false);
		}

	}

	/**
	 * Function updateUndoState
	 */
	public void updateUndoState() {
		if (program.getMainPanel().getUndo().canUndo()) {
			program.setEnabled(ActionType.UNDO, true);
		} else {
			program.setEnabled(ActionType.UNDO, false);

		}
	}



	/**
	 * Un �couteur pour d�tecter si la langue de l'application est chang�e
	 */
	private static class LanguageChangeListener implements
			PropertyChangeListener {
		private final WeakReference<Controller> controller;

		public LanguageChangeListener(Controller controller) {
			this.controller = new WeakReference<Controller>(controller);
		}

		public void propertyChange(PropertyChangeEvent ev) {

			Program program = this.controller.get().program;
			if (program == null) {
				((UserPreferences) ev.getSource())
						.removePropertyChangeListener("language", this);
			} else {
				program.getTabbedPane().setTitleAt(0,
						Tools.getLocalizedString("PROJECT"));
				if(program.getMainPanel().getxHTMLPanel().getDocumentTitle().equals("Welcome Page")){
					try {
						program.getMainPanel().getxHTMLPanel().setDocument(new File(Tools
								.getLocalizedString("WELCOME_PAGE")));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else{
					if(program.getMainPanel().getTextField().getText()
						.compareTo("") != 0)
					if (program.getUserPreferences().getSolutionType()
							.compareTo(SolutionType.DETAILLED_SOLUTION) == 0)
						program.getController().simplify(
								SolutionType.DETAILLED_SOLUTION);
					else
						program.getController().simplify(
								SolutionType.MINIMIZED_FUNCTION);
				program.getMainPanel().lblF.setText(Tools
						.getLocalizedString("FORMULA"));
				}
			}
		}
	}



}