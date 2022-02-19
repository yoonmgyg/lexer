package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Token;
import java.util.ArrayList;
import java.util.List;


class Parser implements IParser {
  private final List<Token> tokens = new ArrayList<>();
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  
  private Expr equality() {
	    Expr expr = comparison();

	    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
	      Token operator = previous();
	      Expr right = comparison();
	      expr = new Expr.Binary(expr, operator, right);
	    }

	    return expr;
	  }
  
  private Expr expression() {
	    return equality();
  }
}