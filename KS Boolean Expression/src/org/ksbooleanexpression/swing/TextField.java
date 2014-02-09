package org.ksbooleanexpression.swing;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Un JTextField qui affiche le text introduit en majuscules.
 *
 */
public class TextField extends JTextField {

	static class TextDocument extends PlainDocument {

		private static final long serialVersionUID = 1L;

		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {

			if (str == null) {
				return;
			}
			char[] upper = new char[str.length()];
			int j = 0;
			for (int i = 0; i < upper.length; i++) {
				char c = Character.toUpperCase(str.charAt(i));
				if ((c < 'A' || c > 'Z')
						&& (c != '*' && c != '+' && c != '!' && c != '/'
								&& c != '^' && c != '|' && c != '(' && c != ' '
								&& c != ')' && c != ';')) {
					Toolkit.getDefaultToolkit().beep();
					continue;
				}

				upper[j] = c;
				j++;
			}
			if (j != str.length()) {
				char[] t = new char[j];
				for (int i = 0; i < j; i++) {
					t[i] = upper[i];
				}
				upper = t;
			}

			super.insertString(offs, new String(upper), a);
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public TextField() {
		super();
	}

	public TextField(int cols) {
		super(cols);
	}

	protected Document createDefaultModel() {
		return new TextDocument();
	}
}
