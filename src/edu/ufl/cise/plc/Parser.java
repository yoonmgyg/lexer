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
	protected boolean isKind(Kind kind) {
	    return t.getKind() == kind;
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
	private Expr expr() throws SyntaxException{
		Expr e = cond();
		if (e == null) {
			e = or();
		}
		return e;
	}
	
	
	// conditional
	private Expr cond() throws SyntaxException {
		Token firstToken = tokens.get(current);
		Expr e = null;
		if(match(Kind.KW_IF)) {
			match(Kind.LPAREN);
			Expr condition = expr();
			match(Kind.RPAREN);
			Expr trueCase = expr();
			match(Kind.KW_ELSE);
			Expr falseCase = expr();
			match(Kind.KW_FI);
			e = new ConditionalExpr(firstToken, condition, trueCase, falseCase);
		}
		return e;
	}

	private Expr or() throws SyntaxException {
		Token firstToken = t;
		Expr e = and();
		while (match(Kind.OR)) {
			Token operator = previous();
			Expr right =  and();
			e = new BinaryExpr(firstToken, e, operator, right);
		}
		return e;
	}
	
	private Expr and() throws SyntaxException {
		Token firstToken = t;
		Expr e = comp();
		while (match(Kind.AND)) {
			Token operator = previous();
			Expr right =  comp();
			e = new BinaryExpr(firstToken, e, operator, right);
		}
		return e;
	}
	
	private Expr comp() throws SyntaxException{
		Token firstToken = t;
		Expr e = add();
		while (match(Kind.GT, Kind.LT, Kind.LE, Kind.GE, Kind.EQUALS, Kind.NOT_EQUALS)) {
			Token operator = previous();
			Expr right =  add();
			e = new BinaryExpr(firstToken, e, operator, right);
		}
		return e;
	}
	
	private Expr add() throws SyntaxException{
		Token firstToken = t;
		Expr e = mult();
		while (match(Kind.PLUS, Kind.MINUS)) {
			Token operator = previous();
			Expr right =  mult();
			e = new BinaryExpr(firstToken, e, operator, right);
		}
		return e;
	}


	private Expr mult() throws SyntaxException{
		Token firstToken = t;
		Expr e = unary();
		while (match(Kind.TIMES, Kind.DIV, Kind.MOD)) {
			Token operator = previous();
			Expr right =  unary();
			e = new BinaryExpr(firstToken, e, operator, right);
		}
		return e;
	}
	
	private Expr unary() throws SyntaxException{
		Token firstToken = t;
		Expr e = null;
		if (isKind(Kind.BANG) || isKind(Kind.MINUS) || isKind(Kind.COLOR_OP) ||  isKind(Kind.IMAGE_OP)) {
			match(Kind.BANG, Kind.MINUS, Kind.COLOR_OP, Kind.IMAGE_OP);
			Token operator = previous();
			e = unary();
			e = new UnaryExpr(firstToken, operator, e);
		}
		else {
			e = postFix();
		}
		return e;
		
	}
	
	private Expr postFix() throws SyntaxException{
		Expr e = PrimaryExpr();
		return e;
	}
	
	private Expr PrimaryExpr() throws SyntaxException {
		Expr e;
		Token firstToken = t;
        if(isKind(Kind.FLOAT_LIT)){
            e = new FloatLitExpr(firstToken);
            match(Kind.FLOAT_LIT);
        }
        else if(isKind(Kind.BOOLEAN_LIT)){
            e = new BooleanLitExpr(firstToken);
            match(Kind.BOOLEAN_LIT);
        }
        else if(isKind(Kind.STRING_LIT)){
            e = new StringLitExpr(firstToken);
            match(Kind.STRING_LIT);
        }
        else if(isKind(Kind.IDENT)){
            e = new IdentExpr(firstToken);
            match(Kind.STRING_LIT);
            
        }
        else {
			match(Kind.LPAREN);
			e = expr();
			match(Kind.RPAREN);
			PixelSelector selector = pixelSelector();
			e = new UnaryExprPostfix(firstToken, e, selector);
        }
        return e;
    }
	
	private PixelSelector pixelSelector() throws SyntaxException {
		Token firstToken = t;
		match(Kind.LSQUARE);
		Expr x = expr();
		match(Kind.COMMA);
		Expr y = expr();
		match(Kind.RSQUARE);
		PixelSelector e = new PixelSelector(firstToken, x, y);
		return e;
		
	}
	
	@Override
	public ASTNode parse() throws PLCException {
		t = peek();
	    ASTNode e = expr(); 
	    return e; 
	}
}
