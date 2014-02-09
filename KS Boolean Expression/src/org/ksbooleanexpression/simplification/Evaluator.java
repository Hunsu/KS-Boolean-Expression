package org.ksbooleanexpression.simplification;

import java.util.ArrayList;

import org.ksbooleanexpression.simplification.exception.SyntaxErrorException;
import org.ksbooleanexpression.tools.Tools;

public class Evaluator {

	private String expression;
	private int currentPosition;
	private char[] operator = { '!', '*', '+', '^', '|', '/' };

	public Evaluator(String expression) {
		this.expression = expression;

	}

	public boolean evaluate() throws SyntaxErrorException {
		ArrayList<Object> al = new ArrayList<Object>();
		for (currentPosition = 0; currentPosition < expression.length(); currentPosition++) {
			char c = expression.charAt(currentPosition);
			if (c == ' ')
				continue;
			if (isFalseCharacter(c))
				throw new SyntaxErrorException(
						Tools.getLocalizedString("False_Character"),
						currentPosition);
			if (c == '(') {
				String str = getExpression(currentPosition);
				Evaluator e = new Evaluator(str);
				al.add(e.evaluate());
			} else {
				if (c == '0')
					al.add(false);
				else {
					if (c == '1')
						al.add(true);
					else
						al.add(String.valueOf(c));
				}
			}
		}

		return evaluate(al);

	}

	private boolean isFalseCharacter(char c) {
		if (c == '(' || c == ')' || c == '1' || c == '0' || c == ' ')
			return false;
		for (int i = 0; i < operator.length; i++) {
			if (c == operator[i])
				return false;
		}
		return true;
	}

	private boolean evaluate(ArrayList<Object> al) throws SyntaxErrorException {
		for (char c : operator) {
			if (al.size() == 1)
				return (Boolean) (al.get(0));
			for (int i = 0; i < al.size(); i++) {
				Boolean arg1, arg2;
				Object object = al.get(i);
				String op;
				if (object instanceof String)
					op = (String) object;
				else
					continue;
				if (op.equalsIgnoreCase(String.valueOf(c))) {
					try {
						switch (c) {
						case '!':
							if (al.size() < i + 2)
								throw new SyntaxErrorException();
							arg1 = (Boolean) al.get(i + 1);
							if (arg1)
								al.add(i, false);
							else
								al.add(i, true);
							al.remove(i + 1);
							al.remove(i + 1);
							i--;
							break;
						case '*':
							arg1 = (Boolean) al.get(i - 1);
							arg2 = (Boolean) al.get(i + 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.add(i - 1, arg1 && arg2);
							i--;
							break;
						case '+':
							arg1 = (Boolean) al.get(i - 1);
							arg2 = (Boolean) al.get(i + 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.add(i - 1, arg1 || arg2);
							i--;
							break;
						case '^':
							arg1 = (Boolean) al.get(i - 1);
							arg2 = (Boolean) al.get(i + 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.add((arg1 && !arg2) || (!arg1 && arg2));
							i--;
							break;
						case '|':
							arg1 = (Boolean) al.get(i - 1);
							arg2 = (Boolean) al.get(i + 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.add(!(arg1 && arg2));
							i--;
							break;
						case '/':
							arg1 = (Boolean) al.get(i - 1);
							arg2 = (Boolean) al.get(i + 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.remove(i - 1);
							al.add(!(arg1 || arg2));
							i--;
							break;
						}
					} catch (Exception e) {
						throw new SyntaxErrorException();
					}
				}
			}

		}
		if (al.size() != 1 || !(al.get(0) instanceof Boolean))
			throw new SyntaxErrorException();
		return (boolean) (al.get(0));
	}

	private String getExpression(int index) throws SyntaxErrorException {
		int c = 1;
		for (int j = index + 1; j < expression.length(); j++) {
			char s = expression.charAt(j);
			if (s == '(')
				c++;
			if (s == ')')
				c--;
			currentPosition = j;
			if (c == 0)
				return expression.substring(index + 1, j);
		}
		throw new SyntaxErrorException(
				Tools.getLocalizedString("MISSING_BRACKETS"), index);
	}

}
