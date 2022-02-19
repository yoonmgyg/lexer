package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Token;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;
import java.util.ArrayList;
import java.util.List;


public class Parser implements IParser {
	private final List<Token> tokens;
	private int current = 0;
	private IToken t;
  
	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}
  
	// Referenced from Parsing 4
	protected boolean isKind(Kind kind) {
		return t.getKind() == kind;
	}
  
	protected boolean isKind(Kind... kinds) {
		for (Kind k: kinds){
			if (k == t.getKind()) {
				return true;
			}	
		}
		return false;
	}

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
		t = tokens.get(current);
		return previous();
	}
	
	//expression
	private Expr expr() {
		Expr e = cond();
		if (cond() != null) {
			return e;
		}
		else {
			e = or();
			return e;
		}
	}
	
	
	// conditional
	private Expr cond() throws SyntaxException {
		if (isKind(Kind.KW_IF)) {
			consume();
			if (isKind(Kind.LPAREN)) {
				consume();
				Expr e = null;
				e = expr();
				if (isKind(Kind.RPAREN)) {
					e = expr();
					if (isKind(Kind.KW_ELSE)) {
						e = expr();
						if (isKind(Kind.KW_FI)) {
							return e;
						}
					}
				}
			}
			throw new SyntaxException("Invalid conditional");
		}
		return null;
	}

	private Expr or() throws SyntaxException {
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