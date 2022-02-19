package edu.ufl.cise.plc.ast;

import edu.ufl.cise.plc.IToken;

abstract class Expr { 
	public final IToken firstToken;
	public Expr(IToken firstToken) {
		this.firstToken = firstToken;
	}
	  
}

