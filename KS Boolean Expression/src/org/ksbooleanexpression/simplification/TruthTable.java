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



package org.ksbooleanexpression.simplification;

import java.util.Arrays;

import org.ksbooleanexpression.simplification.exception.SyntaxErrorException;
import org.ksbooleanexpression.tools.Tools;

/**
 * Permet d'avoir la table de v�rit� d'une fonction<br>
 * � partir de son expression.
 * @author Mounir Hamoudi
 *
 */


public class TruthTable {

	private String exprBool;
	private String variables;
	public boolean TruthTable[];
	private SyntaxErrorException error;

	/**
	 * Constructeurs par d�faut
	 */
	public TruthTable() {
	}


	/**
	 * Constructeur
	 * @param expression l'expression de la fonction
	 */
	public TruthTable(String expression)
	    {
		this.setBooleanExpression(expression);
		setError(null);
		addAndOperator();
		getTruthTable();
	    }


	/**
	 * Ajoute l'op�rateur logique ET dans les emplacement<br>
	 * ou il en manque. exemple: AB--> A*B
	 */

	private void addAndOperator()
	{
		char ch[] = exprBool.toCharArray();
		String newExpreBool = "";

		for(int i=0; i<ch.length-1; i++)
		{

			if((ch[i]<='Z' && ch[i]>='A' && ch[i+1]<='Z' && ch[i+1]>='A') || (ch[i]<='Z' && ch[i]>='A' && ch[i+1]=='(')
					|| (ch[i]==')' && ch[i+1]<='Z' && ch[i+1]>='A') || (ch[i]==')' && ch[i+1]=='('))
				newExpreBool = newExpreBool+ ch[i]+'*';

			else newExpreBool = newExpreBool+ ch[i];

		}
		newExpreBool = newExpreBool+ch[ch.length-1];
		this.exprBool = newExpreBool;
	}


	/**
	 * Permet de convertir un nombre du d�cimal au binaire<br>
	 * Le r�sultat est donn� sous forme d'un tableau de byte<br>
	 * @param n le nombre � convertir vers la binaire<br>
	 * @param lesBit tableau qui contiendera les bit du nombre obtenu<br>
	 * @param nbrVar le nombre de bit sur lesquel doit �tre �crit le r�sultat<br>
	 */
	private void decimalToBinary(int n, int[] lesBit, int nbrVar)
	{
      	if(n!=0){
		while(n!=0)
		{
			lesBit[nbrVar-1]=(int) (n % 2);
			n= n/2;
			nbrVar--;
		}
      	}
	}


    /**
	 * Donne la table de v�rit� apr�s �valuation de l'�xpression bool�enne<br>
	 * La table de v�rit� est donn� sous forme d'un vecteur � 2 puissance le nombre<br>
	 * de variables.<br>
	 * La case 0 correspond � l'�tat ou toutes les variables sont � 0 (Faux), et<br>
	 * ainsi de suite... <br>
	 */
	private void getTruthTable()
	    {
	     variables= "";
	     //on extrait toutes les variables de la fonction
	     getVariables();
	     triVariables();
	     if(variables.length() >8) error = new SyntaxErrorException(Tools.getLocalizedString("TOO_MUCH_VARIABLES"));

	     //on initialise la table de v�rit�
	     TruthTable= new boolean[(int) Math.pow(2, variables.length())];
	     int lesBit[]= new int[variables.length()];
	     for(int i=0; i<lesBit.length; i++)
			{
	    	 lesBit[i]=0;
			}
	     //on remplit la table de v�rit�
	     for(int j=0; j<TruthTable.length; j++)
	     {
	    	 //on donne les valeurs aux variables et on les ajoute � l'evalueur
	    	 //par exemple pour j=0, on obtiendra pour 3 var: lesBit={0, 0, 0}
	    	 decimalToBinary(j, lesBit, variables.length());
	    	 int k=0;
	    	 char[] expression = exprBool.toCharArray();
	    	 for(int i=0; i<variables.length(); i++)
		     {
		    	for(int index=0; index<expression.length;index++){
		    		if(expression[index]==variables.charAt(i))
		    			expression[index] =  (char)(lesBit[k]+48);
		    	}
		    	k++;
		     }
	    	 Evaluator e = new Evaluator(new String(expression));
	    	 boolean r;
			try {
				r = e.evaluate();
		    	TruthTable[j]=r;
			} catch (SyntaxErrorException error) {
				this.setError(error);
				return;
			}
	     }
	    }




	/**
	 * Permet d'�xtraire les variables d'une expression bool�enne<br>
	 * donn�e.<br>
	 * Les variables sont ensuite plac�e dans la table des variables<br>
	 * On garde leur ordre dans une chaine de type String.<br>
	 */

	private void getVariables()
	{
		 for(int i=0 ; i<exprBool.length(); i++)
		 {
			 char c = exprBool.charAt(i);

			if(c >='A' && c <= 'Z')
			 {
				if(variables.indexOf(c)<0) variables += c;
			 }
			 }
	}


	/**
	 * Retourne l'ordre des variables avec lequel a �t� <br>
	 * �val�e l'�xpression bool�nne<br>
	 * @return ordrVar l'ordre des variables, de type String<br>
	 */
	public String getVars()
	{
		return variables;
	}


	/**
	 * Initialise l'�xpression boll�enne � �valuer<br>
	 * @param exprBool l'�xpression bool�enne, de type String<br>
	 */

	private void setBooleanExpression(String exprBool)
	{
		this.exprBool=exprBool;
	}

	/**
	 * Tri les variables extraites de l'expression bool�enne<br>
	 * comme �a l'expression sera evalu� en suivant l'ordre<br>
	 * alphabetique des variables.<br>
	 */
	public void triVariables()
	{
		char[] ch = variables.toCharArray();
		Arrays.sort(ch);
		variables = new String(ch);

	}


	/**
	 * @param error the error to set
	 */
	public void setError(SyntaxErrorException error) {
		this.error = error;
	}


	/**
	 * @return the error
	 */
	public SyntaxErrorException getError() {
		return error;
	}

}



