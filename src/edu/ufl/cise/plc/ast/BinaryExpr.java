package edu.ufl.cise.plc.ast;

import edu.ufl.cise.plc.IToken;

public class BinaryExpr extends Expr {   
	public final Expr left;
	public final IToken op;
	public final Expr right;
    public BinaryExpr(IToken firstToken, Expr left, IToken op, Expr right){
    	super(firstToken);
        this.left = left;
        this.op = op;
        this.right = right;
    }
}
