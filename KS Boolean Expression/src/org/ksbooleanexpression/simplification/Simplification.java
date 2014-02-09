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


package org.ksbooleanexpression.simplification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.ksbooleanexpression.controller.Controller;
import org.ksbooleanexpression.tools.Tools;
import org.ksbooleanexpression.tools.View;


/**
 * Permet la simplification d'une fonction logique<br>
 * La fontion peut �tre donn�e sous diverses formes: <br>
 * expression, tables de v�rit�, table de Karnaugh, et<br>
 * forme num�rique. <br>
 * @author Rabah Meradi, Mounir Hamoudi
 *
 */

public class Simplification implements View {

	/**
	 * KarnaughNode Structure<br>
	 * Structure d'un regroupement de 1<br>
	 * Elle contient:<br>
	 * le nombre de term participant au regroupement
	 * l'adresse du regoupement
	 * un bool�en qui indique si le groupement est utilis� dans
	 * un autre regroupement pls important
	 * l'adresse des cellule participants � ce regroupement
	 * @author Hamoudi, Meradi
	 *
	 */
	private class KarnaughNode{
		public ArrayList<String> cellesAdress;
		public int numberOfItems;
		public String nodes;
		public boolean flag;
		public KarnaughNode() {

		}

		public KarnaughNode(String nodes,boolean flag,int numberOfItems)
		{
			this.nodes=nodes;
			this.flag=flag;
			this.numberOfItems=numberOfItems;
		}

		public void addCellAdress(String adress){
			if(cellesAdress ==null) cellesAdress=new ArrayList<String>();
			cellesAdress.add(adress);
		}
	}
	private String[] functions;
	private int nombreVars ;
	private TruthTable truthTable;
	private KarnaughTable kmap;
	private ArrayList<KarnaughNode> arrayList;
	private String minimisedExpression;
	private String ordrVar;
	private String s;
	private boolean detailledSolution;

	private Controller controller;
	/**
	 * Constructeur
	 * @param ttable table de v�rit� de la fonction
	 * @param nbrVar le nombre de variables
	 * @param ordrVar l'ordre des variables
	 * @param controller controller de l'application
	 */

	public Simplification(int ttable[], int nbrVar, String ordrVar, Controller controller){

		this.controller = controller;
		truthTable = new TruthTable();
		truthTable.TruthTable = ttable;
		nombreVars=nbrVar;
		s="";
		s +=Tools.intialize();
		kmap = new KarnaughTable(ttable, nbrVar);
		this.ordrVar = ordrVar;
		}

		/**
		 * Constructeurs
		 * @param kmap la table de Karnaugh
		 * @param nbrVar le nombre de variables
		 * @param ordrVar l'ordre des variables
		 */
	public Simplification(int kmap[][], int nbrVar, String ordrVar, Controller controller){

		this.controller = controller;
		nombreVars=nbrVar;
		this.ordrVar = ordrVar;
		s="";
		s +=Tools.intialize();
		this.kmap = new KarnaughTable(kmap,nombreVars);

		}


	/**
	 * Constructeur
	 * @param strings les fonctions � simplifi�es
	 * @param solutionType le type de solution
	 * @param controller controller de l'application
	 */
	public Simplification(String[] strings,SolutionType solutionType, Controller controller){
		s="";
		if (solutionType.name().compareTo("DETAILLED_SOLUTION")==0)
		this.detailledSolution = true;
		else this.detailledSolution=false;
		functions = strings;
		s +=Tools.intialize();
		s +=Tools.writeIntroducedFunctions(strings);
		this.controller = controller;



		}


	/**
	    * Verifie si un regroupement existe d�j� dans la liste des regroupements<br>
	    * @param n le regoupement dont on veut v�rifier l'existence<br>
	    * @return true s'il existe d�j�.
	    */
	   private boolean alreadyExist(KarnaughNode n) {
		boolean exist = false;
		   for (int i=0;i<arrayList.size();i++){
			if(arrayList.get(i).nodes.compareTo(n.nodes)==0)
			{
				exist = true; break;
			}
		}
		return exist;
	}




	/**
	 * Retourne l'adresse en binaire d'une celulle
	 * @param cellAdress l'adresse de la celulle en d�cimal
	 * @param nbrVar le nombre de variable
	 * @return binaryAdress l'adresse en binaire sous forme de chaine
	 */
	private String binaryCode(int cellAdress, int nbrVar)
	{
	  	byte bt[] = new byte[nbrVar];
	  	String binaryAdress = "";
	  	for(int i=0; i<nbrVar; i++) bt[i]=0;
		if(cellAdress!=0){
		while(cellAdress!=0)
		{
			bt[nbrVar-1]=(byte) (cellAdress % 2);
			cellAdress= cellAdress/2;
			nbrVar--;
		}
	  	}
		for(int i=0; i<bt.length; i++) binaryAdress = binaryAdress +bt[i];
		return binaryAdress;

	}

	private void deleteUnneededNodes(){
		int [][] temp = new int[kmap.getRow()][kmap.getColumn()];
		boolean del=false;
		for (int i=0;i<arrayList.size();i++){
			for (int j=0;j<kmap.getRow();j++){
				for(int k=0;k<kmap.getColumn();k++){
					temp[j][k] =0;
				}
			}
			for (int m=0;m<arrayList.size();m++){
				if(arrayList.get(i).nodes.compareTo(arrayList.get(m).nodes)!=0){
					for(int j=0; j<kmap.getRow(); j++)
					{
						for(int k=0; k<kmap.getColumn(); k++)
						{
							if(IsAtCell(j, k, arrayList.get(m).nodes))
								temp[j][k] = 1;
						}
					}
				}

				del=true;
				for(int j=0; j<kmap.getRow(); j++)
				{
					for(int k=0; k<kmap.getColumn(); k++)
					{
						if(temp[j][k]!=kmap.getCellValue(j, k))
						{
							del=false;
							break;
						}
					}
					if(!del) break;
				}
			}
			if(del)
			{
				arrayList.remove(i);
				i--;
			}


		}
	}

	/**
	 * Supprime les regoupement de 1 qui sont utilis�s pour former<br>
	 * d'autre regroupement plus grand.
	 */

	private void deleteUsedBlocs()
	{
		for(int i=0; i<arrayList.size(); i++)
		{
			if(arrayList.get(i).flag)

				{arrayList.remove(i); i--;}
		}
	}


	/**
	 * Verifie si on est � la fin des terms d'un regroupement
	 * @param ch adresse du regroupement
	 * @param i la position actuelle dans l'adresse du groupement
	 * @return true si oui, false sinon
	 */

	private boolean endOfTerms(String ch, int i)
	{
		boolean end=true;
		char tabChar[] = ch.toCharArray();
		for(int j=i+1; j<ch.length(); j++)
		{
			if(tabChar[j]=='1' || tabChar[j]=='0')
			{
				end = false;
				break;
			}
		}
		return end;
	}


	/**
	 * Permet de r�cup�rer l'expression de la fonction<br>
	 * � partir de la table de Karnaugh de celle-ci.
	 * @param nbrVar le nombre de variables
	 * @return l'expression de la fonction
	 */

	public String functionFromKmap(int nbrVar)
	{
		int truthTable[] = new int [kmap.getRow()*kmap.getColumn()];
		for(int i=0; i<kmap.getRow(); i++)
		{
			for(int j=0; j<kmap.getColumn(); j++)
				truthTable[kmap.getCellAdress(i, j)]=kmap.getCellValue(i, j);
		}
		String func = getFunctionFromTruthTable(truthTable, nbrVar);
		return func;

	}


	/**
	 * Combine entre les cellule participants � une regroupement.
	 * @param karnaughNode le premier regroupement.
	 * @param karnaughNode2 le second regroupement.
	 */
   private ArrayList<String> getCellesAdress(KarnaughNode karnaughNode,
		KarnaughNode karnaughNode2) {
	ArrayList<String> arrayList = new ArrayList<String>();
	for(int i=0;i<karnaughNode.cellesAdress.size();i++){
		arrayList.add(karnaughNode.cellesAdress.get(i));
	}
	for(int i=0;i<karnaughNode2.cellesAdress.size();i++){
		arrayList.add(karnaughNode2.cellesAdress.get(i));
	}
	return arrayList;
}



	/**
	 * Permet de r�cup�rer l'expression de la fonction<br>
	 * � partir de la table de v�rit� de celle-ci.
	 * @return l'expression de la fonction.
	 */
	private String getFunctionFromTruthTable(int truthT[], int nbrVar) {
		String function ="";
		int k = 65;
		for (int i=0;i<truthT.length;i++){
			k =65;
			if (truthT[i] ==1){
			for (int j=0;j<nbrVar;j++){
                if ((i%((int)Math.pow(2, nbrVar-j)))<((int)Math.pow(2, nbrVar-j-1)))
					   function += "!" +String.valueOf((char) k)+"*";
				else   function += String.valueOf((char) k)+"*";
                k++;
			}
			function = function.substring(0,function.length()-1);
            function += "+";
			}
		}
		function = function.substring(0,function.length()-1);
		return function;
	}

	/**
	 * Retourne la fonction (l'expression) simplifi�e
	 * @return minimisedExpression l'expression simplifi�e (String)
	 */

	public String getMinimisedExpression()
	{
		return minimisedExpression;
	}


		/**
		 * Si on trouve deux regroupement qui peuvent �tre regroup�s<br>
		 * alors on utlise cette fonction pour obtenir la nouvelle adresse<br>
		 * le bit qui change sera remplac� par le chiffre 2.
		 * @param binAdress1 l'adresse de l'un des deux regroupement
		 * @param pos la position du bit qui diff�re.
		 * @return la nouvelle adresse sous forme de chaine
		 */
		private String getNewBinaryAdress(String binAdress1, int pos)
		{
			String newAdress = "";
			for(int i=0; i<binAdress1.length(); i++)
			{
				if(i==pos) {newAdress= newAdress+"2"; continue;}
				newAdress = newAdress+binAdress1.charAt(i);
			}

			return newAdress;
		}

	   /**
	 * Verifie si toutes les celulle de la table de Karnaugh sont � un
	 * @return retourne true si oui, false sinon
	 */

	private boolean isAllOnes()
	{
		boolean isAllOnes=true;
		for(int i=0; i<this.kmap.getRow(); i++)
		{
			for(int j=0; j<kmap.getColumn(); j++)
			{
				if(kmap.getCellValue(i, j)==0) {isAllOnes=false; break;}
			}
		}
		return isAllOnes;
	}

	   /**
	 * Verifie si toutes les celulle de la table de Karnaugh sont � z�ro
	 * @return retourne true si oui, false sinon
	 */
	private boolean isAllZero()
	{
		boolean isAllZero= true;
		for(int i=0; i<this.kmap.getRow(); i++)
		{
			for(int j=0; j<kmap.getColumn(); j++)
			{
				if(kmap.getCellValue(i, j)==1) {isAllZero=false; break;}
			}
		}
		return isAllZero;
	}



		private boolean IsAtCell(int j, int k, String nodes) {
			String s = binaryCode(kmap.getCellAdress(j, k),nombreVars);
			char[] a = nodes.toCharArray();
			char[] b = s.toCharArray();
			for (int i=0;i<a.length;i++){
				if( (a[i]!=b[i]) && (a[i]!='2') ) return false;
			}
			return true;
		}



		/**
		 * Verifie si deux regoupement peuvent �tre regoup�s pour former<br>
		 * un noueau groupement
		 * @param karnaughNode le regoupement 1
		 * @param karnaughNode2 le regoupement 2
		 * @return reourne true si oui
		 */
		private int IsJoinable(KarnaughNode karnaughNode,
				KarnaughNode karnaughNode2) {

		return isOneBitDeferent(karnaughNode.nodes, karnaughNode2.nodes);
		}



		/**
		 * Verifie si deux adresse ne contient qu'un seul bit qui est diff�rent<br>
		 * @param binAdress1  l'adresse de la celulle 1
		 * @param binAdress2  l'adresse de la celulle 2
		 * @return retourne -1 si non, et la position ou il diff�rent si oui
		 */
		private int isOneBitDeferent(String binAdress1, String binAdress2 )
		{

			int pos=-1; int bitDefer=0;

			for(int i=0; i<binAdress1.length(); i++)
			{
				if(binAdress1.charAt(i)!= binAdress2.charAt(i))
				{
					pos = i;
					bitDefer++;
				}
			}
			if(bitDefer!=1) pos=-1;
			return pos;

		}

		/**
		 * Permet de lancer la simplification
		 * @param type forme de la fonction � simplifier.
		 */
		public void launchSimplification(Type type) {
			int i=1;
			switch (type){
			case FUNCTION :
			    for(String function : functions){
					if(function.length()==1){
						this.s +=Tools.writeSolution(function, i);
						i++;
						continue;
					}
				TruthTable truthTable = new TruthTable(function);
				if (truthTable.getError() !=null){
					this.s+=Tools.writeError(i, truthTable.getError().getMessage());
					i++;
					continue;
					}
				nombreVars=truthTable.getVars().length();
				ordrVar = truthTable.getVars();
				kmap = new KarnaughTable(truthTable.TruthTable,nombreVars);
				solve();
				truthTable = new TruthTable(function);
				nombreVars=truthTable.getVars().length();
				ordrVar = truthTable.getVars();
				kmap = new KarnaughTable(truthTable.TruthTable,nombreVars);

				this.s +=Tools.writeSolution(minimisedExpression, i);
				if(detailledSolution && nombreVars!=1) this.s +=Tools.writeKarnaughMap(kmap, nombreVars);
				if(functions.length==1 && detailledSolution && nombreVars!=1 ) this.s +=Tools.writeTruthTable(truthTable, nombreVars,ordrVar);
				i++;
				}
			    break;
			case TRUTH_TABLE :
				String function[] = new String[1];
				function[0] = getFunctionFromTruthTable(truthTable.TruthTable, nombreVars);
				controller.program.getMainPanel().getTextField().setText(function[0]);
				s += Tools.writeIntroducedFunctions(function);
				kmap = new KarnaughTable(truthTable.TruthTable,nombreVars);
				solve();
				String ordreVars = ordrVar;
				ordrVar = ordreVars;
				kmap = new KarnaughTable(truthTable.TruthTable, nombreVars);
				s +=Tools.writeSolution(minimisedExpression, 1);
				s +=Tools.writeKarnaughMap(kmap, nombreVars);
				s +=Tools.writeTruthTable(truthTable, nombreVars,ordrVar);
				break;

			case KARNAUGH_MAP :
				String func[] = new String[1];
				func[0] = functionFromKmap(nombreVars);
				controller.program.getMainPanel().getTextField().setText(func[0]);
				s += Tools.writeIntroducedFunctions(func);
				solve();
				int nbVar = nombreVars;
				String varOrder = ordrVar;
				KarnaughTable kt =kmap;
				kmap = kt;
				nombreVars = nbVar;
				ordrVar = varOrder;
				s +=Tools.writeSolution(minimisedExpression, 1);
				s +=Tools.writeKarnaughMap(kmap, nombreVars);
				break;
			}
			writeSolution();
		}

		/**
		    * Calcul le log2 d'un nombre donn� x<br>
		    * @param x de type double
		    * @return log1(x)
		    */
		    private double log2(double x){
				return Math.log(x)/Math.log(2);
			}


		/**
		 * Parcourt la liste des regoupement pour �crire l'expression simplifi�<br>
		 * sous forme de chaine (String).
		 */

		private void minimisedExpression()
		{
			String ch = "";
			for(int i=0; i<arrayList.size(); i++)
			{
				char tabChar[] = arrayList.get(i).nodes.toCharArray();
				for(int j=0; j<tabChar.length; j++)
				{
					if(tabChar[j]=='0') {ch=ch+"!"+ordrVar.charAt(j);
					if(!endOfTerms(arrayList.get(i).nodes, j)) ch=ch+"*";
					}
					else{
						if(tabChar[j]=='1'){ ch=ch+ordrVar.charAt(j);
						if(!endOfTerms(arrayList.get(i).nodes, j)) ch=ch+"*";}
					}

				}
				if(i!=arrayList.size()-1) ch=ch+"+";
			}
			minimisedExpression = ch;
		}

		/**
		 * Parcourt la liste des groupement de 1 et cr�ee tout le groupement<br>
		 * possible<br>
		 * un groupement peut contenir 2, 4, 8, 16,... �l�ments (celulles)<br>
		 */

		@SuppressWarnings("unchecked")
		private void regroupe()
		{
				int x = (int)log2(kmap.getColumn()*kmap.getRow());
				ArrayList<KarnaughNode> array = new ArrayList<KarnaughNode>();
				for (int i=1; i<x+1;i++)
				{
					for(int j=0;j<arrayList.size();j++)
					{
						for(int k=0;k<arrayList.size();k++)
						{
							if (arrayList.get(j).numberOfItems== (int) Math.pow(2, i-1) &&
									arrayList.get(k).numberOfItems== (int) Math.pow(2, i-1))
							{
								int y =IsJoinable(arrayList.get(j),arrayList.get(k));
								if(y!=-1)
								{
									String s = getNewBinaryAdress(arrayList.get(j).nodes,
												IsJoinable(arrayList.get(j),arrayList.get(k)));
									KarnaughNode n = new KarnaughNode(s,false,arrayList.get(j).numberOfItems*2);
									n.cellesAdress = getCellesAdress(arrayList.get(j),arrayList.get(k));

									arrayList.get(j).flag=true;
									arrayList.get(k).flag=true;
									if(!alreadyExist(n))
									arrayList.add(n);

								}
							}
						}
					}

					for (int s=0;s<arrayList.size();s++){
					if (arrayList.get(s).numberOfItems== (int) Math.pow(2, i-1)){
						array.add(arrayList.get(s));
						arrayList.remove(s);
						s--;
					}
					}
				}
				arrayList =  (ArrayList<KarnaughNode>) array.clone();
				deleteUsedBlocs();
		}

		/**
		 * Supprime les espaces dans la fonction introduite.
		 * @param s l'expression de la fonction
		 */
		public String removeSpaces(String s){
			char [] data = s.toCharArray();
			s = "";
			for (char c : data){
				if (c != ' ') s = s + String.valueOf(c);
			}
			return s;
		}

		/**
		 * Fonction qui simplifie l'expression bool�enne introduite.
		 */

		private void solve()
		{
				if(isAllOnes()){
					minimisedExpression = "1";
				}
				else{
					if(isAllZero()) minimisedExpression = "0";

					else{
				arrayList = new ArrayList<KarnaughNode>();
				for (int i=0;i<kmap.getRow();i++)
				{
					for(int j=0;j<kmap.getColumn();j++)
					{
						if(kmap.getCellValue(i, j)==1)
						{
							KarnaughNode n = new KarnaughNode();
							n.flag=false;
							n.numberOfItems=1;
							n.nodes=binaryCode(kmap.getCellAdress(i, j),nombreVars);
							n.addCellAdress(n.nodes);
							arrayList.add(n);
						}
					}

				}
				regroupe();
				deleteUnneededNodes();
				minimisedExpression();
			}
				}
		}

		/**
		 * Ecrit la solution en HTML dans un fichier temporaire.
		 */

		private void writeSolution() {
			File file = new File("temp/temp.html");
			file.deleteOnExit();
			s +="</body></html>";
			try {
				Writer out = new BufferedWriter(new OutputStreamWriter(
					    new FileOutputStream(file), "UTF-8"));
					try {
					    out.write(s);
					} finally {
					    out.close();
					}
			} catch (IOException e) {
				System.err.println( e.getMessage() );}

		}

	}


