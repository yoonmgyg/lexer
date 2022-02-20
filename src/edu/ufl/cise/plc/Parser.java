package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Token;
import edu.ufl.cise.plc.IToken.Kind;
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
		throw new SyntaxException("match error");
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
			Expr condition Token = expr();
			match(Kind.RPAREN);
			Expr trueCase = expr();
			match(Kind.KW_ELSE);
			Expr falseCase = expr();
			match(Kind.KW_FI);
			e = new ConditionalExpr(condition, trueCase, falseCase);
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
		Expr e = null;
		if (isKind(Kind.BANG) || isKind(Kind.MINUS) || isKind(Kind.COLOR_OP) ||  isKind(Kind.IMAGE_OP)) {
			match(Kind.BANG, Kind.MINUS, Kind.COLOR_OP, Kind.IMAGE_OP);
			Token operator = previous();
			e =  unary();
			e = new UnaryExpr(op, e);
		}
		else {
			e = postFix();
		}
		return e;
		
	}
	
	private Expr postFix() {
		Expr e = PrimaryExpr();
		return e;
		
	}
	
	private Expr PrimaryExpr(){
		Expr e;
        if(isKind(Kind.FLOAT_LIT)){
            e = FloatLitExpr();
            match(Kind.FLOAT_LIT);
        }
        else if(isKind(BOOLEAN_LIT)){
            e = BoolLitExpr();
            match(Kind.BOOLEAN_LIT);
        }
        else if(isKind(Kind.STRING_LIT)){
            e = StringLitExpr();
            match(Kind.STRING_LIT);
        }
        else if(isKind(Kind.IDENT)){
            e = IdentExpr();
            match(Kind.STRING_LIT);
            
        }
        else {
			match(Kind.LPAREN);
			e = expr();
			match(Kind.RPAREN);
			Expr selector = PrimaryExpr();
			e = UnaryExprPostfix(e, selector);
        }
        return e;
    }
	
	private Expr pixelSelector() {
		match(Kind.LSQUARE);
		Expr x = expr();
		match(Kind.COMMA);
		Expr y = expr();
		match(Kind.RSQUARE);
		Expr e = new PixelSelectorExpr(x, y);
		return e;
		
	}
	
	@Override
	public ASTNode parse() throws PLCException {
		return null;
	}
}
