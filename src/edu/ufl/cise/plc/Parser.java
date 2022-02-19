package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Token;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.Expr;
import java.util.ArrayList;
import java.util.List;


public class Parser implements IParser {
	private final List<Token> tokens;
	private int current = 0;
	private Token t;
  
	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}
  
	// Referenced from Parsing 4

	//Referenced from Crafting Interpreters 6.2
	private Token peek() {
		return tokens.get(current);
	}

	//Referenced from Crafting Interpreters 6.2
	private Token previous() {
		return tokens.get(current - 1);
	}

	//Referenced from Crafting Interpreters 6.2

	private boolean isAtEnd() {
		return peek().kind == Kind.EOF;
	}

	//Referenced from Crafting Interpreters 6.2
	private Token consume() {
		if (!isAtEnd()) current++;
		t = peek();
		return previous();
	}
	

	protected boolean isKind(Kind... kinds) {
		for (Kind k: kinds){
			if (k == t.getKind()) {
				consume();
				return true;
			}	
		}
		return false;
	}
	
	//expression
	private Expr expression() throws SyntaxException{
		Expr e = condition();
		if (e == null) {
			e = or();
		}
		if (e == null) {
			throw new SyntaxException("Invaid syntax");
		}
		return e;
	}
	
	
	// conditional
	private Expr condition() throws SyntaxException {
		Expr e = null;
		if (isKind(Kind.KW_IF)) {
			isKind(Kind.LPAREN);
			e = expression();
			isKind(Kind.RPAREN);
			e = expression();
			isKind(Kind.KW_ELSE);
			e = expression();
			isKind(Kind.KW_FI);
			e = new ConditionalExpr(t);
		}
		return e;
	}

	private Expr or() throws SyntaxException {
		Expr e = and();
		while (isKind(Kind.OR))) {
			Token operator = previous();
			Expr right =  and();
			e = new BinaryExpr(e, operator, right);
		}
		return e;
	}
	   /*
		
	  public Expr factor() {   
		  IToken firstToken = t;
		  Expr e = null;
		  if (isKind(INT_LIT)){
			  e = new IntLitExpr(firstToken);
		      consume();
		  } 
		  else if (isKind(LPAREN))){
			consume(); 
		    e = expr(); 
		    match(RPAREN); 
		  }
		  else error();
		  return e;
	  }
	  
	  private void term() {
		  factor();
		  while (isKind(Kind.TIMES, Kind.DIV)) {consume(); term();};
	  }
	  public void expr() {
		  term();
		  while(isKind(Kind.PLUS, Kind.MINUS)) {consume(); term();}
	  }
	    */
	
	@Override
	public ASTNode parse() throws PLCException {\
		return null;
	}
}