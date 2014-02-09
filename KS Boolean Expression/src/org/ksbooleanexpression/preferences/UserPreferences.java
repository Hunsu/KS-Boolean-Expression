package org.ksbooleanexpression.preferences;

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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.ksbooleanexpression.tools.Tools;
import org.ksbooleanexpression.tools.View;

/**
 * Une classe pour sauvegarder les pr�f�rences de l'utilisateur
 *
 * @author Meradi
 *
 */
public class UserPreferences implements View {
	private static final String LANGUAGE = "language";
	private static final String LOOK_AND_FEEL = "lookAndFeel";
	private static final String SOLUTION_TYPE = "solutionType";
	private static final String HEIGHT = "height";
	private static final String WIDTH = "width";
	private static final String MAXIMAZED = "maximazed";
	private PropertyChangeSupport propertyChangeSupport;
	private String language;
	private String lookAndFeel;
	private SolutionType solutionType;
	private boolean maximazed;
	private int height;
	private int width;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private static Preferences preferences = Preferences
			.userNodeForPackage(UserPreferences.class);

	public UserPreferences() {
		readPreferences();
		this.propertyChangeSupport = new PropertyChangeSupport(this);

	}

	/**
	 * Ajouter un �couteur pour notifier les autres classe si la langue change
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String language2,
			PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener("language",
				listener);
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return Le langue en cours d'utilisation
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * @return le nom de langue � partir de son code ISO
	 */
	public String getLanguageName() {
		Locale locale = new Locale(language);
		return Character.toUpperCase(locale.getDisplayLanguage().charAt(0))
				+ locale.getDisplayLanguage().substring(1);
	}

	/**
	 *
	 * @return le look and feel utilis� par l'application
	 */
	public String getLookAndFeel() {
		UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		for (LookAndFeelInfo laf : lafs) {
			if (laf.getName() == lookAndFeel)
				return laf.getClassName();
		}
		return lookAndFeel;

	}

	/**
	 * Retourner le nom de look and feel
	 *
	 * @param lookAndFeel
	 * @param lookAndFeel
	 *            le look and feel
	 * @return le nom de look and feel
	 */
	public String getLookAndFeelClassName(String lookAndFeel) {
		UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		for (LookAndFeelInfo laf : lafs) {
			if (laf.getName().compareTo(lookAndFeel) == 0)
				return laf.getClassName();
		}
		return null;
	}

	public SolutionType getSolutionType() {
		return solutionType;
	}

	/**
	 *
	 * @return la liste des langues disponible dans cette application
	 * @throws MalformedURLException
	 */
	public String[] getSupportedLanguages() {
		try {
			File file = new File("translations");
			java.net.URL[] url = { file.toURI().toURL() };
			ClassLoader loader = new URLClassLoader(url);
			ResourceBundle resource = ResourceBundle.getBundle(
					"UserPreferences", Locale.getDefault(), loader);

			String[] displayLanguages = resource
					.getString("supportedLanguages").split("\\s");
			return displayLanguages;
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the maximazed
	 */
	public boolean isMaximazed() {
		return maximazed;
	}

	/**
	 * Lire les pr�f�rences de l'utilisateur
	 */
	public void readPreferences() {
		language = preferences.get("language", "en");
		setLookAndFeel(preferences.get("lookAndFeel",
				"javax.swing.plaf.nimbus.NimbusLookAndFeel"));
		if (preferences.get("solutionType",
				SolutionType.DETAILLED_SOLUTION.name()).compareTo(
				SolutionType.DETAILLED_SOLUTION.name()) == 0)
			setSolutionType(SolutionType.DETAILLED_SOLUTION);
		else
			setSolutionType(SolutionType.MINIMIZED_FUNCTION);
		maximazed = preferences.getBoolean("maximazed", false);
		height = preferences.getInt("height", 0);
		width = preferences.getInt("width", 0);
		Locale locale = new Locale(language);
		Locale.setDefault(locale);
		Tools.updateSwingResourceLanguage();
	}

	/**
	 * Supprimer un �couteur
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	/**
	 * Supprimer un �couteur
	 */
	public void removePropertyChangeListener(String language2,
			PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener("language",
				listener);
	}

	/**
	 * Sauvegarder les pr�f�rences de l'utilisateur
	 *
	 * @throws Exception
	 *             si les pr�f�rences ne peuvent pas �tre sauvegarder
	 *
	 */
	public void savePreferences() throws Exception {
		preferences.put(LANGUAGE, language);
		preferences.put(LOOK_AND_FEEL, lookAndFeel);
		preferences.put(SOLUTION_TYPE, solutionType.name());
		preferences.putBoolean(MAXIMAZED, maximazed);
		preferences.putInt(HEIGHT, height);
		preferences.putInt(WIDTH, width);
		try {
			// Write preferences
			preferences.flush();
		} catch (BackingStoreException ex) {
			throw new Exception("Couldn't write preferences", ex);
		}
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Affecter le langue et avertir les �couteurs s'il y a un changement de
	 * langue
	 *
	 * @param string
	 */
	public void setLanguage(String newLanguage) {
		if (this.language != null) {
			if (!newLanguage.equals(this.language)) {
				String oldLanguage = this.language;
				this.language = newLanguage;
				updateDefaultLocale();
				Tools.updateSwingResourceLanguage();
				this.pcs.firePropertyChange("language", oldLanguage,
						newLanguage);
			}
		} else
			this.language = newLanguage;

	}

	/**
	 * Affecter le look and feel
	 *
	 * @param string
	 */
	public void setLookAndFeel(String newLookAndFeel) {
		this.lookAndFeel = newLookAndFeel;

	}

	/**
	 * @param maximazed
	 *            the maximazed to set
	 */
	public void setMaximazed(boolean maximazed) {
		this.maximazed = maximazed;
	}

	public void setSolutionType(SolutionType solutionType) {
		this.solutionType = solutionType;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Mettre � jour Locale lors de changement de la langue
	 */
	private void updateDefaultLocale() {
		Locale.setDefault(new Locale(this.language));
	}
}
