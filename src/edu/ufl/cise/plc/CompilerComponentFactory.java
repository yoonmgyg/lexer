package edu.ufl.cise.plc;
import edu.ufl.cise.plc.Lexer.java;


public class CompilerComponentFactory {
	
	//This method will be invoked to get an instance of your lexer.  
	public static ILexer getLexer(String input) {
		//TODO:  modify this method so it returns an instance of your Lexer instead of throwing the exception.
		//for example:  
		      //return new Lexer(input); 
		return new Lexer("input");
		throw new UnsupportedOperationException(
				"CompilerComponentFactory must be modified to return an instance of your lexer");
	}
	
}
