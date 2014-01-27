package com.ksbooleanexpression;

import java.util.ArrayList;

public class Evaluator {
	
	private String expression;
	private int currentPosition;
	private char[] operator = {'!', '*', '+', '^', '|', '/'};
	
	public Evaluator(String expression){
		this.expression = expression;
		
	}
	
   public int evaluate() throws SyntaxErrorException{
	   ArrayList<String> al = new ArrayList<String>();
	   for (currentPosition=0;currentPosition<expression.length();currentPosition++){
		   char c = expression.charAt(currentPosition);
		   if (c==' ') continue;
		   if (isFalseCharacter(c)) 
			   throw new SyntaxErrorException(Tools.getLocalizedString("False_Character"),currentPosition);
		   if(c == '(') {
			   String str = getExpression(currentPosition);
			   Evaluator e = new Evaluator(str);
			   al.add(String.valueOf(e.evaluate()));
		   }
		   else
		   al.add(String.valueOf(c));
	   }
	   
	   
	return evaluate(al);
	   
   }

private boolean isFalseCharacter(char c) {
	if (c == '(' || c== ')' || c =='1' || c == '0' || c == ' ')
	return false;
	for(int i=0;i<operator.length;i++){
		if (c==operator[i]) return false;
	}
	return true;
}

private int evaluate(ArrayList<String> al) throws SyntaxErrorException {
	for(char c : operator){
		if(al.size() ==1) return Integer.valueOf(al.get(0));
		for (int i=0;i<al.size();i++){
			int arg1=0, arg2 =0;
			if (al.get(i).equalsIgnoreCase(String.valueOf(c))){
				try{
				switch(c){
				case '!' : 
					if(al.get(i+1).equalsIgnoreCase("1"))
					al.add(i, "0") ;
					else al.add(i, "1");
					al.remove(i+1);al.remove(i+1);
					i--;
				    break;
				case '*' : 
					arg1 = Integer.valueOf(al.get(i-1));
					arg2 = Integer.valueOf(al.get(i+1));
					al.remove(i-1); 
					al.remove(i-1); 
					al.remove(i-1);
					al.add(i-1,String.valueOf(arg1*arg2));
					i--;
					break;
				case '+' :
					arg1 = Integer.valueOf(al.get(i-1));
					arg2 = Integer.valueOf(al.get(i+1));
					al.remove(i-1); al.remove(i-1); al.remove(i-1);
					if (arg1+arg2 <1) al.add(i-1,String.valueOf(arg1+arg2));
					else al.add(i-1,"1");
					i--;
					break;
				case '^' :
					arg1 = Integer.valueOf(al.get(i-1));
					arg2 = Integer.valueOf(al.get(i+1));
					al.remove(i-1); al.remove(i-1); al.remove(i-1);
					if (arg1 == arg2) al.add("0");
					else al.add("1");
					i--;
					break;
				case '|' :
					arg1 = Integer.valueOf(al.get(i-1));
					arg2 = Integer.valueOf(al.get(i+1));
					al.remove(i-1); al.remove(i-1); al.remove(i-1);
					if (arg1 == 1 && arg2 ==1) al.add("0");
					else al.add("1");
					i--;
					break;
				case '/' : 
					arg1 = Integer.valueOf(al.get(i-1));
					arg2 = Integer.valueOf(al.get(i+1));
					al.remove(i-1); al.remove(i-1); al.remove(i-1);
					if (arg1 == 0 && arg2 ==0) al.add("1");
					else al.add("0");
					i--;
					break;
				}
				}
				catch(Exception e){
					throw new SyntaxErrorException();
				}
				}
			}
		
		}
	if(al.size() !=1) throw new SyntaxErrorException();
	return Integer.valueOf(al.get(0));
}


private String getExpression(int index) throws SyntaxErrorException {
	int c=1;
	for(int j=index+1;j<expression.length();j++){
		char s = expression.charAt(j);
		if(s == '(') c++;
		if (s == ')') c--;
		currentPosition = j;
		if(c==0) return expression.substring(index+1, j);
	}
	 throw new SyntaxErrorException(Tools.getLocalizedString("MISSING_BRACKETS"),index);
}

}
