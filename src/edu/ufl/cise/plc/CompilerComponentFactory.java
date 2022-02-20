package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Lexer;


public class CompilerComponentFactory {
	//This method will be invoked to get an instance of your lexer.  
	public static ILexer getLexer(String input) {
		return new Lexer(input);
	}

	public static IParser getParser(String input) {
		return new Parser(new Lexer(input).getTokens());
	}
	
}
