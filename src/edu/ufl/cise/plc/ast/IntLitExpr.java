package edu.ufl.cise.plc.ast;

import edu.ufl.cise.plc.IToken;

public class IntLitExpr extends Expr
{	  
   public IntLitExpr(IToken firstToken){
         super(firstToken);
   }
   public int getValue() {
         return firstToken.getIntValue();
  }
}

