package edu.ufl.cise.plc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.IToken.SourceLocation;

class Lexer implements ILexer {
  private final String chars;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int pos = 0;
  private int columns = 0;
  private int lines = 1;
  private Kind last_kind;
  
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
	  IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS, HAVE_HASH, HAVE_LROW, HAVE_RROW, NEW_LINE}
  


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
    SourceLocation loc = new SourceLocation(lines, columns);
    tokens.add(new Token(kind, text, loc, pos - start));
  }

  @Override
  public IToken next() throws LexicalException {
	  if (!tokens.isEmpty()) {
		  return tokens.remove(0);
	  }

	  scanTokens();
	  return tokens.remove(0);
  }

  @Override
  public IToken peek() throws LexicalException {
	  if (!tokens.isEmpty()) {
		  return tokens.get(0);
	  }
	  else {
		  scanTokens();
		  return tokens.get(0);
	  }
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

  private void scanTokens() {
	  State state = State.START;
	  while (true) {
		 char ch = chars.charAt(++pos);
		 columns++;
		 switch (state) {
		 	case START -> {
		 		start = pos;
		 		switch(ch) {
		 			case ' ', '\t', '\r', '\n'-> {
		 			}
		 			case '#' -> {
		 				state = State.HAVE_HASH;
		 			}
		 			case '+' -> {
		 				addToken(Kind.PLUS);
		 				return;
		 			}
		 			case '(' -> {
		 				addToken(Kind.LPAREN);
		 				return;
		 			}

		 			case ')' -> {
		 				addToken(Kind.RPAREN);
		 				return;
		 			}
		 			case '[' -> {
		 				addToken(Kind.LSQUARE);
		 				return;
		 			}
			    	case ']' -> {
		 				addToken(Kind.RSQUARE);
		 				return;
		 			}
			    	case '*' -> {
		 				addToken(Kind.TIMES);
		 				return;
		 			}
			    	case '/' -> {
		 				addToken(Kind.DIV);
		 				return;
		 			}
			    	case '%' -> {
		 				addToken(Kind.MOD);
		 				return;
		 			}
			    	case '&' -> {
		 				addToken(Kind.AND);
		 				return;
		 			}
			    	case '|' -> {
		 				addToken(Kind.OR);
		 				return;
		 			}
			    	case ';' -> {
		 				addToken(Kind.SEMI);
		 				return;
		 			}
			    	case ',' -> {
		 				addToken(Kind.COMMA);
		 				return;
		 			}

			    	case '=' -> {
			    		state= State.HAVE_EQ;
			    		return;
			    	}
			    	case '0' -> {
			    		state = State.HAVE_ZERO;
			    		return;
			    	}
			    	case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
			    		state = State.IN_NUM;
			    		return;
			    	}
			    	case '-' -> {
			    		state = State.HAVE_MINUS;
			    		return;
			    	}
			    	case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			    		 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			    		 '$', '_' -> {
			    			 state = State.IN_IDENT;
			    			 return;
			    		 }
			    		 
		 			case 0 -> {
		 				addToken(Kind.EOF);
		 				return;
		 			}
		 		}
		 	}
		 	
		 	case HAVE_EQ -> {
		 		switch(ch) {
		 			case '=' -> {
		 				addToken(Kind.EQUALS);
		 				return;
		 			}
		 			case ' ' -> {
		 				addToken(Kind.ASSIGN);
		 				return;
		 			}

		 			case 0 -> {
		 				addToken(Kind.EOF);
		 				return;
		 			}
		 		}
		 	}
		 	
		 	case IN_NUM -> {
		 		switch (ch) { //int_lit can only start with 1-9 so check for that
                case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                }
                case '.' -> {
                    state = State.HAVE_DOT;
                }

	 			case 0 -> {
	 				addToken(Kind.EOF);
	 				return;
	 			}
                default -> {
                    addToken(Kind.INT_LIT);
                    return;
		 		}
            }
		 
		 		
		 	}
		 	case IN_FLOAT -> {
		 		switch(ch) {
				 	case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
		            }

		 			case 0 -> {
		 				addToken(Kind.EOF);
		 				return;
		 			}
		 			
			 		default -> {
			 			addToken(Kind.INT_LIT);
			 			return;
			 			}
		 		}
		 	}
		 	case IN_IDENT -> {
		 		switch(ch) {
			 		case '\n' -> {
			 			
			 		}

		 			case 0 -> {
		 				addToken(Kind.EOF);
		 				return;
		 			}
		 			
			 		default -> {
				 		if (isAlphaNumeric(ch)) {
				 			
				 		}
			 		}
		 		}
		 		
		 		// Referenced from Crafting Interpreterss 4.4
		 		String text = chars.substring(start, pos);
		 	    Kind kind = keywords.get(text);
		 	    if (kind == null) kind = Kind.IDENT;
		 	    addToken(kind);
		 	    return;
		 	}
		 	case HAVE_ZERO -> {
		 		
		 	}
		 	case HAVE_DOT -> {
		 		 if(ch > 0){
                     addToken(Kind.INT_LIT);
                     state = State.IN_FLOAT;
                 }
		 	}
		 	case HAVE_MINUS -> {
			 	switch(ch) {
			 		case '>' -> {
			 			addToken(Kind.RARROW);
			 			return;
			 			
			 		}

		 			case 0 -> {
		 				addToken(Kind.EOF);
		 				return;
		 			}
		 			
			 		default -> {
			 			addToken(Kind.MINUS);
			 			return;
			 		}
		 		}
		 	}
		 	case HAVE_LROW -> {
		 		switch(ch) {
			 		case('<') -> {
			 			addToken(Kind.LANGLE);
			 			return;
			 		}
			 		case('-') -> {
			 			addToken(Kind.LARROW);
			 			return;
			 		}
			 		case('=') -> {
			 			addToken(Kind.LE);
			 			return;
			 		}

		 			case 0 -> {
		 				addToken(Kind.EOF);
		 				return;
		 			}
		 			default -> {
		 				addToken(Kind.LT);
		 			}
		 		}
		 	}
		 	case HAVE_RROW -> {
		 		switch(ch) {
			 		case('>') -> {
			 			addToken(Kind.RANGLE);
			 			return;
			 		}
			 		case('=') -> {
			 			addToken(Kind.GE);
			 			return;
			 		}

		 			case 0 -> {
		 				addToken(Kind.EOF);
		 				return;
		 			}
		 			
			 		default -> {
			 			addToken(Kind.GT);
			 		}
			 		
		 		}
		 	}
		 	case HAVE_HASH -> {
		 		switch(ch) {
		 			case '\n', 0 -> {
		 				return;
		 			}
		 			default -> {
		 				return;
		 			}
		 		}
		 	}
		 	/*
		 	case NEW_LINE -> {
		 		lines += 1;
		 		columns = 0;
		 		return;
		 	}
		 	*/
		 	default -> throw new IllegalStateException("lexer bug");
		 }
	  }
  }
}

;