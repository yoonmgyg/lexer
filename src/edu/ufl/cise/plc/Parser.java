package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Token;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;
import java.util.ArrayList;
import java.util.List;


public class Parser implements IParser {
	private final List<Token> tokens;
	private int current = 0;
	private Token t;
  
	Parser(List<Token> tokens) {
		this.tokens = tokens;
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
		t = peek();
		return previous();
	}
	

	// Referenced from Parsing 4
	protected boolean isKind(Kind... kinds) {
	    for (Kind k : kinds) {
	       if (k == t.getKind())
	       return true;
	    }
	    return false;
	}
	
	protected boolean match(Kind... kinds) throws SyntaxException {
		for (Kind k: kinds){
			if (k == t.getKind()) {
				consume();
				return true;
			}	
		}
		return false;
	}
	
	//expression
	private Expr expr() {
		Expr e = condition();
		if (e == null) {
			e = or();
		}
		return e;
	}
	
	
	// conditional
	private Expr cond() {
		Expr e = null;
		if (match(Kind.KW_IF)) {
			match(Kind.LPAREN);
			e = expr();
			match(Kind.RPAREN);
			e = expr();
			match(Kind.KW_ELSE);
			e = expr();
			match(Kind.KW_FI);
			e = new ConditionalExpr(t);
		}
		return e;
	}

	private Expr or() {
		Expr e = and();
		while (match(Kind.OR)) {
			Token operator = previous();
			Expr right =  and();
			e = new BinaryExpr(e, operator, right);
		}
		return e;
	}
	
	private Expr and() {
		Expr e = comp();
		while (match(Kind.AND)) {
			Token operator = previous();
			Expr right =  comp();
			e = new BinaryExpr(e, operator, right);
		}
		return e;
	}
	
	private Expr comp() {
		Expr e = add();
		while (match(Kind.GT, Kind.LT, Kind.LE, Kind.GE, Kind.EQUALS, Kind.NOT_EQUALS)) {
			Token operator = previous();
			Expr right =  add();
			e = new BinaryExpr(e, operator, right);
		}
		return e;
	}
	
	private Expr add() {
		Expr e = mult();
		while (match(Kind.PLUS, Kind.MINUS)) {
			Token operator = previous();
			Expr right =  mult();
			e = new BinaryExpr(e, operator, right);
		}
		return e;
	}
	

	private Expr mult() {
		Expr e = unary();
		while (match(Kind.TIMES, Kind.DIV, Kind.MOD)) {
			Token operator = previous();
			Expr right =  unary();
			e = new BinaryExpr(e, operator, right);
		}
		return e;
	}
	
	private Expr unary() {
		while (match(Kind.BANG, Kind.MINUS, Kind.COLOR_OP, Kind.IMAGE_OP)) {
			Token operator = previous();
			Expr right =  unary();
			e = new BinaryExpr(e, operator, right);
		}
		return e;
	}
	
	private Expr PrimaryExpr(){
        if(isKind(FLOAT_LIT)){
            return FloatLitExpr();
        }
        else if(isKind(BOOLEAN_LIT)){
            return BoolLitExpr();
        }
        else if(isKind(STRING_LIT)){
            return StringLitExpr();
        }
        else if(isKind(IDENT)){
            return IdentExpr();
        }
        }
	   /*
		
	  public Expr factor() {   
		  IToken firstToken = t;
		  Expr e = null;
		  if (match(INT_LIT)){
			  e = new IntLitExpr(firstToken);
		      consume();
		  } 
		  else if (match(LPAREN))){
			consume(); 
		    e = expr(); 
		    match(RPAREN); 
		  }
		  else error();
		  return e;
	  }
	  
	  private void term() {
		  factor();
		  while (match(Kind.TIMES, Kind.DIV)) {consume(); term();};
	  }
	  public void expr() {
		  term();
		  while(match(Kind.PLUS, Kind.MINUS)) {consume(); term();}
	  }
	    */
	
	@Override
	public ASTNode parse() throws PLCException {\
		return null;
	}
}
