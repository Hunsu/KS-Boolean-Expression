/*
# KS Boolean Expression, Copyright (c) 2012 The Authors. / ks.contrubutors@gmail.com
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

package org.ksbooleanexpression.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.ksbooleanexpression.simplification.KarnaughTable;
import org.ksbooleanexpression.simplification.TruthTable;
import org.ksbooleanexpression.swing.Program;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.XMLResource;
import org.xml.sax.InputSource;

import com.lowagie.text.DocumentException;

/**
 * Filtre pour les extentions de fichier<br>
 *
 * @author Mounir Hamoudi
 *
 */
class ExtensionFileFilter extends FileFilter {

	private String description = "";
	private ArrayList<String> extensions = new ArrayList<String>();

	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		String name = f.getName().toLowerCase();

		for (int i = 0; i < extensions.size(); i++)
			if (name.endsWith((String) extensions.get(i)))
				return true;
		return false;
	}

	public void addExtension(String extension) {
		if (!extension.startsWith("."))
			extension = "." + extension;
		extensions.add(extension.toLowerCase());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String aDescription) {
		description = aDescription;
	}

}

/**
 * Rassemble toutes les fonctions utiles qui sont<br>
 * utilis�es par plusieurs autres classes.
 *
 * @author Mounir Hamoudi, Meradi.
 *
 */
public class Tools implements View {

	private static class ResourceLoaderUserAgent extends ITextUserAgent {
		public ResourceLoaderUserAgent(ITextOutputDevice outputDevice) {
			super(outputDevice);
		}

		protected InputStream resolveAndOpenStream(String uri) {
			InputStream is = super.resolveAndOpenStream(uri);
			return is;
		}
	}

	/**
	 * Converti l'�criture d'une fonction vers son �criture en LaTeX.
	 *
	 * @param function
	 *            expression � convertir
	 * @return expression �crite en LaTeX.
	 */

	private static String convertToLatex(String function) {
		int i = 0;
		String convertedForm = "";
		while (i < function.length()) {
			if (function.charAt(i) != '*' && function.charAt(i) != '/'
					&& function.charAt(i) != '|' && function.charAt(i) != '^'
					&& function.charAt(i) != '!') {
				convertedForm = convertedForm + function.charAt(i);
				i++;
			} else {

				if (function.charAt(i) == '*') {
					convertedForm = convertedForm + ". ";
					i++;
				}

				if (function.charAt(i) == '/') {
					convertedForm = convertedForm + "\\downarrow ";
					i++;
				}

				if (function.charAt(i) == '|') {
					convertedForm = convertedForm + "\\uparrow  ";
					i++;
				}

				if (function.charAt(i) == '^') {
					convertedForm = convertedForm + "\\oplus  ";
					i++;
				}

				if (function.charAt(i) == '!') {
					if (i == function.length() - 1) {
						convertedForm += "!";
						i++;
					} else {
						if (function.charAt(i + 1) != '(') {
							convertedForm = convertedForm + "\\overline  ";
							i++;
						} else if (function.charAt(i + 1) == '(') {
							convertedForm = convertedForm
									+ "\\overline  {("
									+ convertToLatex(function.substring(i + 2,
											getEndOfBrackets(function, i + 1)))
									+ ")} ";
							i = getEndOfBrackets(function, i + 1) + 1;
						}
					}
				}

			}

		}
		return convertedForm;
	}

	/**
	 * Cr�e une image de la fonction �crite avec LaTeX.
	 *
	 * @param function
	 *            la fonction donn�e
	 * @param i
	 *            num�ro de la fonction
	 * @param name
	 *            nom de l'image � cr�er
	 */
	private static void createImageForFunction(String function, int i,
			String name) {
		TeXFormula formula = new TeXFormula(getLatexForm(function, i));
		TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
		icon.setInsets(new Insets(5, 5, 5, 5));

		BufferedImage image = new BufferedImage(icon.getIconWidth(),
				icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
		JLabel jl = new JLabel();
		jl.setForeground(new Color(0, 0, 0));
		icon.paintIcon(jl, g2, 0, 0);
		File file = new File("temp/" + name);
		file.deleteOnExit();
		try {
			ImageIO.write(image, "png", file.getAbsoluteFile());
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	/**
	 * Cr�e une fichier pdf � partir d'un fichier HTML.
	 *
	 * @param url
	 *            chemin du fichier HTML
	 * @param pdf
	 *            extension pdf
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static void createPDF(String url, String pdf) throws IOException,
			DocumentException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(pdf);
			ITextRenderer renderer = new ITextRenderer();
			ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent(
					renderer.getOutputDevice());
			callback.setSharedContext(renderer.getSharedContext());
			renderer.getSharedContext().setUserAgentCallback(callback);

			Document doc = XMLResource.load(new InputSource(url)).getDocument();

			renderer.setDocument(doc, url);
			renderer.layout();
			renderer.createPDF(os);

			os.close();
			os = null;
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}

	/**
	 * Remplacent les <code>%20</code> dans <code>url</code> par des espaces.
	 *
	 * @param chemin
	 */
	public static String formatURL(String url) {
		String s = "";
		int index = 0;
		while ((index = url.indexOf("%20")) != -1) {
			s += url.substring(0, index) + " ";
			url = url.substring(index + 3, url.length());
		}
		s += url;
		return s;
	}

	/**
	 * Retourne le repertoire o� se trouve l'application.
	 */

	public static String getApplicationFolder() {
		String chemin = Tools.class.getResource("").toString();
		if (chemin.equals("null"))
			return null;
		else {
			chemin = chemin.substring(6, chemin.length() - 14);
			chemin = Tools.formatURL(chemin);
			return chemin;
		}

	}

	/**
	 * Donne les nom des colonne d'une table de Karnaugh<br>
	 * pour une fonctions donn�e.
	 *
	 * @param nbrVar
	 *            nombre de variables
	 * @return tableau qui contient les noms des colonnes.
	 */
	public static String[] getColonneName(int nbVar) {
		int bits;
		if (nbVar % 2 == 0)
			bits = nbVar /2;
		else
			bits = nbVar/2 +1;

		return getGrayCode((int) Math.pow(2, Math.ceil((double) nbVar / 2)),bits);
	}

	private static String[] getGrayCode(int n, int bits) {
		String[] names = new String[n];
		for (int i = 0; i < n; i++) {
			names[i] = addLeadingZeros(Integer.toBinaryString((i >> 1) ^ i),bits);

		}
		return names;
	}

	private static String addLeadingZeros(String binaryNumber,int nbBits) {
		int len = binaryNumber.length();
		for (int i = 0; i < nbBits-len; i++) {
			binaryNumber = "0" + binaryNumber;
		}
		return binaryNumber;
	}

	/**
	 * Retourne la position ou se ferme une parenth�se dans une fonctions.
	 *
	 * @param function
	 *            l'expression de la fonction
	 * @param posActuelle
	 *            la position actuelle dans la fonction.
	 * @return position ou se ferme la prenth�se ouverte.
	 */
	public static int getEndOfBrackets(String function, int posActuelle) {
		int nbrBracketsOpened = 0;
		int nbrBracketsClosed = 0;
		int endPosition = 0;
		for (int j = posActuelle; j < function.length(); j++) {

			if (function.charAt(j) == '(')
				nbrBracketsOpened++;
			else if (function.charAt(j) == ')')
				nbrBracketsClosed++;

			if (nbrBracketsOpened == nbrBracketsClosed) {
				endPosition = j;
				break;
			}

		}
		return endPosition;
	}

	/**
	 * Donne la forme finale en LaTeX d'une expression.
	 *
	 * @param function
	 *            fonction � convertir
	 * @param numFunction
	 *            num�ro de la fonction.
	 * @return expression �crite en LaTeX
	 */
	public static String getLatexForm(String function, int numFunction) {
		return "$\\textbf{F}_{" + numFunction + "} = "
				+ convertToLatex(function) + "$";
	}

	/**
	 * Donne les noms des lignes d'une table de Karnaugh<br>
	 * pour une fonction donnée.
	 *
	 * @param nbrVar
	 *            nombre de variables
	 * @return tableau qui contient les noms des lignes.
	 */
	public static String[] getLigneName(int nbVar) {
		return getGrayCode((int) Math.pow(2, Math.floor(((double) nbVar / 2))),nbVar/2);

	}

	/**
	 * Retourne le text localis� pour les noms de menus ainsi<br>
	 * que les raccourcis qui eux dependent du system.
	 */
	public static String getLocalizedLabelText(String resourceKey) {
		String localizedString = Tools.getLocalizedString(resourceKey);
		return localizedString;
	}

	/**
	 * Returns the string matching <code>resourceKey</code> for the given
	 * resource bundle.
	 */
	public static String getLocalizedString(String resourceKey) {
		try {
			File file = new File("translations");
			java.net.URL[] url = {file.toURI().toURL()};
			ClassLoader loader = new URLClassLoader(url);
			ResourceBundle resource = ResourceBundle
					.getBundle("lpackage",Locale.getDefault(),loader);
			String localizedString = resource.getString(resourceKey);
			return localizedString;
		} catch (MissingResourceException | MalformedURLException ex) {
			throw new IllegalArgumentException("Unknown key " + resourceKey);
		}
	}

	/**
	 * Initialise le contenu du fichier HTML<br>
	 * Ecrit l'entete du fichier.
	 */
	public static String intialize() {
		String s = "";
		s += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		s += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + "<head>";
		s += "<link rel=\"stylesheet\" href=\"Style.css\" />";

		s += "</head>";
		s += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />";
		s += "<body>";
		return s;
	}

	/**
	 * Reourne <code>true</code> si le syst�me courant est un Mac OS
	 */
	public static boolean isMacOSX() {
		return System.getProperty("os.name").startsWith("Mac OS X");
	}

	/**
	 * Affiche une boite de dialogue lors de la fermuture de l'application Elle
	 * est affich�e dans le seul cas ou des modifications n'ont pas �t�
	 * enregitr�es
	 *
	 * @return int
	 */
	public static int showExitDialog() {
		Object[] options = { Tools.getLocalizedString("YES"),
				Tools.getLocalizedString("NO"),
				Tools.getLocalizedString("ButtonCancel") };
		JFrame frame = new JFrame();
		frame.setIconImage(Program.programIcon);
		int n = JOptionPane.showOptionDialog(frame,
				Tools.getLocalizedString("EXIT_DIALOGUE"),
				"KS Boolean Expression", JOptionPane.YES_NO_OPTION,
				JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);

		return n;
	}

	/**
	 * Permet d'afficher la boite de dialogue pour ouvrir un fichier
	 *
	 * @return File le fichier s�l�ctionn�
	 */
	public static File showOpenDialog(String title) {
		final ExtensionFileFilter filter = new ExtensionFileFilter();
		JFileChooser Open = new JFileChooser();
		if (title.compareTo("Importer") == 0) {
			filter.setDescription(Tools.getLocalizedString("FILE_TYPE_TXT"));
			filter.addExtension(".txt");
			Open.setFileFilter(filter);
			Open.setDialogTitle(Tools.getLocalizedString(ActionType.IMPORT
					.name() + ".Name"));
		}

		else {
			filter.setDescription(Tools.getLocalizedString("FILE_TYPE_KS"));
			filter.addExtension(".ks");
			Open.setFileFilter(filter);
			Open.setDialogTitle(title);
		}
		Open.setFileSelectionMode(JFileChooser.FILES_ONLY);
		JFrame frame = new JFrame();
		frame.setIconImage(Program.programIcon);
		File file = null;
		int result = Open.showOpenDialog(frame);

		if (result == JFileChooser.APPROVE_OPTION) {

			try {
				file = Open.getSelectedFile();

			} catch (Exception err) {
				System.err.println(err.getMessage());
			}

		}
		return file;
	}

	/**
	 *
	 * Permet d'afficher la boite de dialogue pour enregistrer un fichier.
	 *
	 * @param title
	 *            pour le nom de la boite de dialogue
	 * @return File le fichier sauvegrd�
	 */
	public static File showSaveDialog(String title) {
		final ExtensionFileFilter filter = new ExtensionFileFilter();
		JFileChooser Save = new JFileChooser();
		JFrame frame = new JFrame();
		frame.setIconImage(Program.programIcon);
		if (title.compareTo(Tools.getLocalizedString(ActionType.EXPORT.name()
				+ ".Name")) == 0) {
			filter.setDescription(Tools.getLocalizedString("FILE_TYPE_PDF"));
			filter.addExtension(".pdf");
			Save.setDialogTitle(Tools.getLocalizedString(ActionType.EXPORT
					.name() + ".Name"));
			Save.setSelectedFile(new File(Tools.getLocalizedString("UNTITLED")
					+ ".pdf"));
		} else {
			filter.setDescription(Tools.getLocalizedString("FILE_TYPE_KS"));
			filter.addExtension(".ks");
			Save.setDialogTitle(title);
			Save.setSelectedFile(new File(Tools.getLocalizedString("UNTITLED")
					+ ".ks"));
		}

		Save.setFileFilter(filter);
		Save.setFileSelectionMode(JFileChooser.FILES_ONLY);
		File file = null;
		int result = Save.showSaveDialog(frame);

		if (result == JFileChooser.APPROVE_OPTION) {
			file = Save.getSelectedFile();
			if (file.exists()) {
				Object[] options = { Tools.getLocalizedString("YES"),
						Tools.getLocalizedString("NO") };
				int n = JOptionPane
						.showOptionDialog(
								Save,
								Tools.getLocalizedString("FILE_ALREADY_EXIST")
										+ " "
										+ file.getName()
										+ " "
										+ Tools.getLocalizedString("FILE_ALREADY_EXIST2"),
								Tools.getLocalizedString("CONFIRM_SAVE_REPLACE"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.YES_NO_OPTION, null, options,
								options[0]);
				if (n == JOptionPane.NO_OPTION) {
					file = null;
					Tools.showSaveDialog(title);
				}
			}
		}
		return file;
	}

	/**
	 * Met � jour les resources bundles de Swing quand<br>
	 * on chnage la langue de l'application.
	 */
	private static void updateSwingResourceBundle(String swingResource) {
		ResourceBundle resource;
		try {
			resource = ResourceBundle.getBundle(swingResource);
		} catch (MissingResourceException ex) {
			resource = ResourceBundle.getBundle(swingResource, Locale.ENGLISH);
		}
		for (Enumeration<?> it = resource.getKeys(); it.hasMoreElements();) {
			String property = (String) it.nextElement();
			UIManager.put(property, resource.getString(property));
		}
	}

	/**
	 * Met � jour les resources bundles de Swing quand<br>
	 * on chnage la langue de l'application.
	 */
	public static void updateSwingResourceLanguage() {
		updateSwingResourceBundle("com.sun.swing.internal.plaf.metal.resources.metal");
		updateSwingResourceBundle("com.sun.swing.internal.plaf.basic.resources.basic");
		if (UIManager.getLookAndFeel().getClass().getName()
				.equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
			updateSwingResourceBundle("com.sun.java.swing.plaf.gtk.resources.gtk");
		} else if (UIManager.getLookAndFeel().getClass().getName()
				.equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel")) {
			updateSwingResourceBundle("com.sun.java.swing.plaf.motif.resources.motif");
		}
	}

	/**
	 * En cas d'erreur dans une fonctions introduite, alors cette<br>
	 * fonction �crit l'erreur dans le fichier HTML contenant la solution.
	 *
	 * @param i
	 *            num�ro de la fonction
	 * @param string
	 *            type d'erreur comise.
	 */
	public static String writeError(int i, String message) {
		StringBuffer s = new StringBuffer("");
		s.append("<h3 class =\"subTitle\" id='function" + i + "'>"
				+ Tools.getLocalizedString("FUNCTION") + " F" + i + " :</h3>");
		s.append("<h3>" + Tools.getLocalizedString("SYNTAX_ERROR") + " </h3>");
		s.append(message);
		return s.toString();
	}

	/**
	 * Ecrit les fonction introduite en HTML.
	 *
	 * @param functions
	 *            les fonctions itroduites
	 */
	public static String writeIntroducedFunctions(String[] functions) {
		StringBuffer s = new StringBuffer("");

		s.append("<h2 class=\"Title\">"
				+ Tools.getLocalizedString("INTRODUCED_FUNCTION") + "</h2>");
		for (int i = 0; i < functions.length; i++) {
			try {
				String name = "Function" + i + ".png";
				createImageForFunction(functions[i], i + 1, name);
				s.append("<img src=\"Function" + i + ".png" + "\" /><br />");
				s.append("\n", 0, 1);

			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		}
		s.append("<h2 class=\"Title\">"
				+ Tools.getLocalizedString("MINIMISED_FUNCTION") + "</h2>");

		return s.toString();
	}

	/**
	 * Ecrit la table de Karnaugh en HTML d'une fonction donn�e.
	 *
	 * @param kmap
	 *            la table de Karnaugh de la fonction
	 * @param nbrVars
	 *            nombre de variables de la fonction.
	 */
	public static String writeKarnaughMap(KarnaughTable kmap, int nbrVars) {
		int width = nbrVars * 50;
		String s = "";

		s += "<div id=\"cent\" align=\"center\">";

		s += "<h3 class =\"subsubTitle2\">"
				+ Tools.getLocalizedString("KARNAUGH_MAP.Name") + ":" + "</h3>";

		s += "<table width=\"" + width + "\"><tr>";
		s += "<th></th>";

		String[] colomNames = getColonneName(nbrVars);
		String[] linesNames = getLigneName(nbrVars);
		for (int i = 0; i < colomNames.length; i++) {
			s += "<th>" + colomNames[i] + "</th>";
		}
		s += "</tr><tr>";
		for (int i = 0; i < kmap.getRow(); i++) {
			for (int j = 0; j < kmap.getColumn() + 1; j++) {
				if (j == 0)
					s += "<th>" + linesNames[i] + "</th>";
				else {
					if (kmap.getCellValue(i, j - 1))
						s += "<td class=\"ones\">1</td>";
					else
						s += "<td>0</td>";
				}
			}
			s += "</tr><tr>";
		}
		s = s.substring(0, s.length() - 4);
		s += "</table>";
		s += "</div>";
		return s;

	}

	/**
	 * Ecrit la solution en HTML apr�s la simplification.
	 *
	 * @param minimisedExpression
	 *            l'expression r�duite.
	 * @param i
	 *            num�ro de la fonction
	 */
	public static String writeSolution(String minimisedExpression, int i) {
		StringBuffer s = new StringBuffer("");
		String name = "MinimisedForm" + i + ".png";
		createImageForFunction(minimisedExpression, i, name);
		s.append("<a id=\"function" + i + "\"></a>");
		s.append("<h3 class =\"subTitle\" >"
				+ Tools.getLocalizedString("FUNCTION") + " F" + i + " :</h3>");
		s.append("<img src=\"MinimisedForm" + i + ".png" + "\" /><br />");

		return s.toString();
	}

	/**
	 * Ecrit la table de v�rit� en HTML d'une fonction donn�e.
	 *
	 * @param tb
	 *            la table de v�rit� de la fonction
	 * @param nombreVars
	 *            nombre de variables de la fonction
	 * @param ordrVars
	 *            l'ordre des variables dans la tables de v�rit�
	 */
	public static String writeTruthTable(TruthTable tb, int nombreVars,
			String ordrVars) {
		StringBuffer s = new StringBuffer("");
		int width = nombreVars * 50;
		s.append("<div id=\"cent\" align=\"center\">");
		s.append("<h3 class =\"subsubTitle2\">"
				+ Tools.getLocalizedString("TRUTH_TABLE.Name") + " :" + "</h3>");
		s.append("<table width=\"" + width + "\" id=\"table1\" rules=\"all\">");
		s.append("<tr>");
		for (int i = 0; i < nombreVars; i++) {
			s.append("<th>" + ordrVars.charAt(i) + "</th>");
		}
		s.append("<th>S</th>");
		s.append("</tr>");
		for (int i = 0; i < tb.TruthTable.length; i++) {
			s.append("<tr>");
			for (int j = 0; j < nombreVars + 1; j++) {
				if (j == nombreVars) {
					if (tb.TruthTable[i])
						s.append("<td class=\"ones\">1</td>");
					else
						s.append("<td>0</td>");
				} else {
					if ((i % ((int) Math.pow(2, nombreVars - j))) < ((int) Math
							.pow(2, nombreVars - j - 1)))
						s.append("<td>0</td>");
					else
						s.append("<td>1</td>");
				}
			}
			s.append("</tr>");
		}
		s.append("</table>");
		s.append("</div>");
		return s.toString();
	}
}