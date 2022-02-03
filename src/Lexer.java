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
  private int lines = 1;
  
  private static final Map<String, Kind> keywords;
  static {
	    keywords = new HashMap<>();
	    keywords.put("string", Kind.TYPE);
	    keywords.put("int", Kind.TYPE);
	    keywords.put("float", Kind.TYPE);
	    keywords.put("boolean", Kind.TYPE);
	    keywords.put("color", Kind.TYPE);
	    keywords.put("image", Kind.TYPE);
	    keywords.put("void", Kind.TYPE);
	    
	    keywords.put("getWidth", Kind.IMAGE_OP);
	    keywords.put("getHeight", Kind.IMAGE_OP);
	    
	    keywords.put("getRed", Kind.COLOR_OP);
	    keywords.put("getGreen", Kind.COLOR_OP);
	    keywords.put("getBlue", Kind.COLOR_OP);
	    
	    keywords.put("BLACK", Kind.COLOR_CONST);
	    keywords.put("BLUE", Kind.COLOR_CONST);
	    keywords.put("CYAN", Kind.COLOR_CONST);
	    keywords.put("DARK_GRAY", Kind.COLOR_CONST);
	    keywords.put("GRAY", Kind.COLOR_CONST);
	    keywords.put("GREEN", Kind.COLOR_CONST);
	    keywords.put("LIGHT_GRAY", Kind.COLOR_CONST);
	    keywords.put("MAGENTA", Kind.COLOR_CONST);
	    keywords.put("ORANGE", Kind.COLOR_CONST);
	    keywords.put("PINK", Kind.COLOR_CONST);
	    keywords.put("RED", Kind.COLOR_CONST);
	    keywords.put("WHITE", Kind.COLOR_CONST);
	    keywords.put("YELLOW", Kind.COLOR_CONST);
	    
	    keywords.put("true", Kind.BOOLEAN_LIT);
	    keywords.put("false", Kind.BOOLEAN_LIT);
	    
	    keywords.put("if", Kind.KW_IF);
	    keywords.put("else", Kind.KW_ELSE);
	    keywords.put("fi", Kind.KW_FI);
	    keywords.put("write", Kind.KW_WRITE);
	    keywords.put("console", Kind.KW_CONSOLE);
	    
	    
	  }
  
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
  
  // isDigit, isAlpha, and isAlphaNumeric are referenced from Crafting Interpreters 4.4
  private boolean isAlpha(char c) {
	    return (c >= 'a' && c <= 'z') ||
	           (c >= 'A' && c <= 'Z') ||
	            c == '_';
	  }
  private boolean isDigit(char c) {
	    return c >= '0' && c <= '9';
  } 
  
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

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
			    		state = State.HAVE_ZERO;
			    		pos++;
			    	}
			    	case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
			    		state = State.IN_NUM;
			    		pos++;
			    	}
			    	case '-' -> {
			    		state = State.HAVE_MINUS;
			    		pos++;
			    	}
			    	case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			    		 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			    		 '$', '_' -> {
			    			 state = State.IN_IDENT;
			    		 }
			    		 
		 			case 0 -> {
		 				addToken(Kind.EOF);
		 				lines += 1;
		 				return tokens;
		 			}
		 		}
		 	}
		 	
		 	case HAVE_EQ -> {
		 		switch(ch) {
		 			case('=') -> {
		 				addToken(Kind.EQUALS);
		 				pos++;
		 			}
		 			case ' ' -> {
		 				addToken(Kind.ASSIGN);
		 			}
		 		}
		 	}
		 	
		 	case IN_NUM -> {
		 		
		 	}
		 	case IN_FLOAT -> {
		 		
		 	}
		 	case IN_IDENT -> {
		 		if (isAlphaNumeric(ch)) {
		 			pos++;
		 		}
		 		
		 		// Referenced from Crafting Interpreterss 4.4
		 		String text = chars.substring(start, pos);
		 	    Kind kind = keywords.get(text);
		 	    if (kind == null) kind = Kind.IDENT;
		 	    addToken(kind);
		 	}
		 	case HAVE_ZERO -> {
		 		
		 	}
		 	case HAVE_DOT -> {
		 		
		 	}
		 	case HAVE_MINUS -> {
		 		
		 	}
		 	
		 	default -> throw new IllegalStateException("lexer bug");
		 }
	  }
  }
}

