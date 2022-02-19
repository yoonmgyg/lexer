public class Parser {
}
    public void expr() {//expr::= term((+|-)term)*
        term();
        while(isKind(PLUS, MINUS)){
            consume();
            term();
        }
        return;
    }

    void term(){ //term::==factor((*|/)factor)*
        factor();
        while(isKind(TIMES,DIV)){
            consume();
            factor();
        }
        return;
    }

    void factor() { //factor::=int_lit|(expr)
        if(isKind(INT_LIT)){
            consume();
        }
        else if(isKind(LPAREN)){
            consume();
            expr();
            match(RPAREN);
        }
        else{
            error();
        }
        return;
    }

    public ConditionalExpr(){

    }
    public void FloatLitExpr(){

    }