import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.IToken.SourceLocation;
import edu.ufl.cise.plc.*;

class Lexer implements ILexer {
  private final String chars;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int pos = 0;
  private int lines = 0;
  
  // Referenced from Lexer Implementation in Java Slides
  private enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, 
	  IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS}
  
  Lexer(String chars) {
    this.chars = chars;
    
  }

  // Referenced from Crafting Interpreters 4.4
  private boolean isAtEnd() {
	  return pos >= chars.length();
  }

  // Referenced from Crafting Interpreters 4.4
  private void addToken(Kind kind) {
    String text = chars.substring(start, pos);
    SourceLocation loc = new SourceLocation(lines, start);
    tokens.add(new Token(kind, text, loc, pos - start));
  }

  @Override
  public IToken next() throws LexicalException {
	  if (!tokens.isEmpty()) {
		  return tokens.remove(0);
	  }
	  return null;
  }

  @Override
  public IToken peek() throws LexicalException {
	  if (!tokens.isEmpty()) {
		  return tokens.get(0);
	  }
	  return null;
  }
  // Referenced from Crafting Interpreters 4.4
  List<Token> scanTokens() {
	  State state = State.START;
	  while (true) {
		 char ch = chars.charAt(pos++);
		 switch (state) {
		 	case START -> {
		 		start = pos;
		 		switch(ch) {
		 			case ' ', '\t', '\n', '\r' -> {pos++;}
		 			case '+' -> {
		 				addToken(Kind.PLUS);
		 				pos++;
		 			}
		 			case '(' -> {
		 				addToken(Kind.LPAREN);
		 				pos++;
		 			}

		 			case ')' -> {
		 				addToken(Kind.RPAREN);
		 				pos++;
		 			}
		 			case '[' -> {
		 				addToken(Kind.LSQUARE);
		 				pos++;
		 			}
			    	case ']' -> {
		 				addToken(Kind.RSQUARE);
		 				pos++;
		 			}
			    	case '*' -> {
		 				addToken(Kind.TIMES);
		 				pos++;
		 			}
			    	case '/' -> {
		 				addToken(Kind.DIV);
		 				pos++;
		 			}
			    	case '%' -> {
		 				addToken(Kind.MOD);
		 				pos++;
		 			}
			    	case '&' -> {
		 				addToken(Kind.AND);
		 				pos++;
		 			}
			    	case '|' -> {
		 				addToken(Kind.OR);
		 				pos++;
		 			}
			    	case ';' -> {
		 				addToken(Kind.SEMI);
		 				pos++;
		 			}
			    	case ',' -> {
		 				addToken(Kind.COMMA);
		 				pos++;
		 			}

			    	case '=' -> {
			    		state= State.HAVE_EQ;
			    		pos++;
			    	}
		 			case '0' -> {
		 				addToken(Kind.EOF);
		 				pos++;
		 			}
		 		}
		 	}
		 	
		 	case HAVE_EQ -> {
		 		switch(ch) {
		 			case('=') -> {
		 				addToken(Kind.EQUALS);
		 				pos++;
		 			}
		 		}
		 	}
		 	
		 	case IN_IDENT -> {
		 	
		 	}
		 	case HAVE_ZERO -> {
		 		
		 	}
		 	case HAVE_DOT -> {
		 		
		 	}
		 }
		 
		 /*
	    tokens.add(new Token(EOF, "", null, line));
	    return tokens;
	    */
	  }
  }
}

