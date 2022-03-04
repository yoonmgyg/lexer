package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Token;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Types.Type;
import java.util.ArrayList;
import java.util.List;


public class Parser implements IParser {
	private final List<Token> tokens;
	private int current = 0;
	private Token t;
	private List<NameDef> ndList = new ArrayList<NameDef>(); 
	private List<ASTNode> stdec = new ArrayList<ASTNode>();
  
	Parser(List<Token> tokens) {
		this.tokens = tokens;

	}
  

	//Referenced from Crafting Interpreters 6.2
	private Token peek() {
		return tokens.get(current);
	}

	private Token next() {
		if (!isAtEnd()) {
			return tokens.get(current+1);
		}
		else {
			return peek();
		}
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
		if (!isAtEnd()) {
			current++;
			t = peek();
		}
		return previous();
	}
	

	// Referenced from Parsing 4
	
	protected boolean isKind(Kind...kinds)  {
		for (Kind k: kinds) {
			if (k == t.getKind()) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean match(Kind... kinds) throws SyntaxException {
		for (Kind k: kinds){
			/*
			System.out.println("current: " + t.getKind());
			System.out.println("wanted: " +  k);
			*/
			if (k == t.getKind()) {
				/*System.out.println(t.getKind());
				 */
				consume();
				return true;
			}	
		}
		throw new SyntaxException("Unmatching kinds");
	}
	
	private Program prog() throws SyntaxException {
		Token firstToken = t;
	    Type type = Type.toType(t.getText());
		match(Kind.TYPE, Kind.KW_VOID);
		String ident = t.getText();
		match(Kind.IDENT);
		match(Kind.LPAREN);
		if (isKind(Kind.TYPE)) {
			ndList.add(ndef());
			while (isKind(Kind.COMMA)) {
				match(Kind.COMMA);
				ndList.add(ndef());
			}
		}
		match(Kind.RPAREN);

		while (isKind(Kind.IDENT, Kind.TYPE, Kind.KW_WRITE, Kind.RETURN)) {
			System.out.println(t.getKind() + " " + t.getText());
			if (isKind(Kind.TYPE)) {
				Declaration decl = decl();
				match(Kind.SEMI);
				stdec.add(decl);
			}
			else {
				Statement state = statement();
				match(Kind.SEMI);
				stdec.add(state);
			}
		}
		return new Program(firstToken, type, ident, ndList, stdec);
	}
	
	private NameDef ndef() throws SyntaxException {
		Token firstToken = t;
		String type = t.getText();
		NameDef nd = null;
		String ident = null;
		if (isKind(Kind.TYPE)) {
			match(Kind.TYPE);
			Dimension d = dimension();
			ident = t.getText();
			if (d!= null){
				match(Kind.IDENT);
				nd = new NameDefWithDim(firstToken, type, ident, d);
				
			}
			else if (isKind(Kind.IDENT)) {
				match(Kind.IDENT);
				nd = new NameDef(firstToken, type, ident);
			}
		}
		return nd;
	}
	
	private Declaration decl() throws SyntaxException {
		Token firstToken = t;
		NameDef n = ndef();
		Declaration d = null;
		Token op = null;
		Expr e = null;
		if (isKind(Kind.ASSIGN, Kind.LARROW)) {
			op = t;
			match(Kind.ASSIGN, Kind.LARROW);
			e = expr();
		}
		d = new VarDeclaration(firstToken, n, op, e);
		return d;
	}
	
	private Statement statement() throws SyntaxException {
		Token firstToken = t;
		Statement state = null;
		if (isKind(Kind.IDENT)) {
			String ident = t.getText();
			match(Kind.IDENT);
			PixelSelector p = pixelSelector();
			
			Expr e = null;
			if (isKind(Kind.ASSIGN)) {
				match(Kind.ASSIGN);
				e = expr();
				state = new AssignmentStatement(firstToken, ident, p, e);
			}
			else if (isKind(Kind.LARROW)) {
				match(Kind.LARROW);
				e = expr(); 
				state = new ReadStatement(firstToken, ident, p, e);
			}
		}
		
		else if (isKind(Kind.KW_WRITE)) {
			match(Kind.KW_WRITE);
			Expr source = expr();
			match(Kind.RARROW);
			Expr dest = expr();
			state = new WriteStatement(firstToken, source, dest);
		}
		else if (isKind(Kind.RETURN)) {
			match(Kind.RETURN);
			Expr e = expr();
			state = new ReturnStatement(firstToken, e);
		}
			
		return state;
		
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
		Token firstToken = t;
		Expr e = null;
		if(isKind(Kind.KW_IF)) {
			match(Kind.KW_IF);
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
		while (isKind(Kind.OR)) {
			match(Kind.OR);
			Token operator = previous();
			Expr right =  and();
			e = new BinaryExpr(firstToken, e, operator, right);
		}
		return e;
	}
	
	private Expr and() throws SyntaxException {
		Token firstToken = t;
		Expr e = comp();
		while (isKind(Kind.AND)) {
			match(Kind.AND);
			Token operator = previous();
			Expr right =  comp();
			e = new BinaryExpr(firstToken, e, operator, right);
		}
		return e;
	}
	
	private Expr comp() throws SyntaxException{
		Token firstToken = t;
		Expr e = add();
		while (isKind(Kind.GT, Kind.LT, Kind.LE, Kind.GE, Kind.EQUALS, Kind.NOT_EQUALS)) {
			match(Kind.GT, Kind.LT, Kind.LE, Kind.GE, Kind.EQUALS, Kind.NOT_EQUALS);
			Token operator = previous();
			Expr right =  add();
			e = new BinaryExpr(firstToken, e, operator, right);
		}
		return e;
	}
	
	private Expr add() throws SyntaxException{
		Token firstToken = t;
		Expr e = mult();
		while (isKind(Kind.PLUS, Kind.MINUS)) {
			match(Kind.PLUS, Kind.MINUS);
			Token operator = previous();
			Expr right =  mult();
			e = new BinaryExpr(firstToken, e, operator, right);
		}
		return e;
	}


	private Expr mult() throws SyntaxException{
		Token firstToken = t;
		Expr e = unary();
		while (isKind(Kind.TIMES, Kind.DIV, Kind.MOD)) {
			match(Kind.TIMES, Kind.DIV, Kind.MOD);
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
		Token firstToken = t;
		Expr e = PrimaryExpr();
		PixelSelector selector = pixelSelector();
		if (selector != null) {
			e = new UnaryExprPostfix(firstToken, e, selector);
		}
		return e;
	}
	
	private Expr PrimaryExpr() throws SyntaxException {
		Expr e;
		Token firstToken = t;
		if (isKind(Kind.INT_LIT)) {
			e = new IntLitExpr(firstToken);
			match(Kind.INT_LIT);
		}
		else if(isKind(Kind.FLOAT_LIT)){
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
            match(Kind.IDENT);
            
        }
        else if (isKind(Kind.COLOR_CONST)) {
        	e = new ColorConstExpr(firstToken);
        	match(Kind.COLOR_CONST);
        }
        else if (isKind(Kind.LANGLE)) {
        	Expr red = expr();
        	match(Kind.COMMA);
        	Expr green = expr();
        	match(Kind.COMMA);
        	Expr blue = expr();
        	match(Kind.RANGLE);
        	e = new ColorExpr(firstToken, red, green, blue);
        }
        else if (isKind(Kind.KW_CONSOLE)) {
        	e = new ConsoleExpr(firstToken);
        	match(Kind.KW_CONSOLE);
        }
        else {
			match(Kind.LPAREN);
			e = expr();
			match(Kind.RPAREN);
        }
        return e;
    }
	
	private PixelSelector pixelSelector() throws SyntaxException {
		Token firstToken = t;
		PixelSelector e = null;
		if (isKind(Kind.LSQUARE)) {
			match(Kind.LSQUARE);
			Expr x = expr();
			match(Kind.COMMA);
			Expr y = expr();
			match(Kind.RSQUARE);
			e = new PixelSelector(firstToken, x, y);
		}
		return e;
		
	}
	private Dimension dimension() throws SyntaxException {
		Token firstToken = t;
		Dimension d = null;
		if (isKind(Kind.LSQUARE)) {
			match(Kind.LSQUARE);
			Expr x = expr();
			match(Kind.COMMA);
			Expr y = expr();
			match(Kind.RSQUARE);
			d = new Dimension(firstToken, x, y);
		}
		return d;
	
	}

	
	@Override
	public ASTNode parse() throws PLCException {
		t = peek();
	    ASTNode e = prog(); 
	    return e; 
	}
}
