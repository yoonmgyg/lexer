package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Lexer;
import edu.ufl.cise.plc.ast.ASTVisitor;


public class CompilerComponentFactory {
	//This method will be invoked to get an instance of your lexer.  
	public static ILexer getLexer(String input) {
		return new Lexer(input);
	}

	public static IParser getParser(String input) throws LexicalException{
		Lexer newLexer = new Lexer(input);
		return new Parser(newLexer.getTokens());
	}
	
	public static TypeCheckVisitor getTypeChecker() throws TypeCheckException{
		return new TypeCheckVisitor();
	}
	
	public static ASTVisitor getCodeGenerator(String packageName) throws Exception {
		return new CodeGenVisitor(packageName);
	}
}
