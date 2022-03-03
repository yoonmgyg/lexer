package edu.ufl.cise.plc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigInteger;
import java.math.BigDecimal;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.IToken.SourceLocation;

class Lexer implements ILexer {
  private final String chars;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int pos = -1;
  private int columns = 0;
  private int lines = 0;
  private boolean end = false;
  
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
	  IN_FLOAT, IN_NUM, IN_STR, HAVE_EQ, HAVE_MINUS, HAVE_HASH, HAVE_LROW, HAVE_RROW, }
  


  Lexer(String chars) {
    this.chars = chars;
    
  }


	public List<Token> getTokens() throws LexicalException {
		  while (!end) {
			  scanTokens();
		  }
		  return tokens;
	}

  // Referenced from Crafting Interpreters 4.4
  private boolean isAtEnd() {
	  return pos >= chars.length() - 1;
  }

  
  private void addToken(Kind kind) {
    String text = chars.substring(start, pos);
    int length = pos - start;
    tokens.add(new Token(kind, text, new SourceLocation(lines, columns - (length + 1)), length));
  }

  @Override
  public IToken next() throws LexicalException {
	  while (tokens.isEmpty()) {
		  scanTokens();
	  }
	  return tokens.remove(0);
  }

  @Override
  public IToken peek() throws LexicalException {
	  if (tokens.isEmpty()) {
		  scanTokens();
	  }
	  return tokens.get(0);
  }
  
  private void scanTokens() throws LexicalException {
	  State state = State.START;
	  while (true) {
		 if (isAtEnd()) {
			  pos++;
			  addToken(Kind.EOF);
			  end = true;
			  return;
		 }
		 
		 char ch = chars.charAt(++pos);
		 ++columns;
		 switch (state) {
		 	case START -> {
		 		start = pos;
		 		switch(ch) {
		 			case ' ', '\t', '\r' -> {
		 			}
		 			case '\n' -> {
		 				lines++;
		 				columns = 0;
		 				
		 			}
		 			case'!' -> {
		 				addToken(Kind.BANG);
		 				return;
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

			    	case '^' -> {
			    		addToken(Kind.RETURN);
			    		return;
			    	}
		 			case '#' -> {
		 				state = State.HAVE_HASH;
		 			}
		 			case '"' -> {
		 				state = State.IN_STR;
		 			}
			    	case '=' -> {
			    		state= State.HAVE_EQ;
			    	}
			    	case '0' -> {
			    		state = State.HAVE_ZERO;
			    	}
			    	case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
			    		state = State.IN_NUM;
			    	}
			    	case '-' -> {
			    		state = State.HAVE_MINUS;
			    	}
			    	case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			    		 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			    		 '$', '_' -> {
			    			 state = State.IN_IDENT;
			    	}	 
			    	default -> throw new LexicalException("invalid character");
		 		}
		 	}
		 	
		 	case HAVE_EQ -> {
		 		switch(ch) {
		 			case '=' -> {
		 				addToken(Kind.EQUALS);
		 				return;
		 			}
		 			default -> {
		 				addToken(Kind.ASSIGN);
		 				--pos;
		 				--columns;
		 				return;
		 			}

		 		}
		 	}
		 	
		 	case IN_NUM -> {
		 		switch (ch) {
	                case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> {
	                }
	                case '.' -> {
	                    state = State.HAVE_DOT;
	                }
	                default -> {
	                	BigInteger val = new BigInteger(chars.substring(start, pos));
	                	BigInteger maxVal = BigInteger.valueOf(Integer.MAX_VALUE);
	                	if (val.compareTo(maxVal) > 0) {
	                		throw new LexicalException("Int is too large");
	                	}
	                    addToken(Kind.INT_LIT);
	                    --pos;
	                    --columns;
	                    return;
			 		}
		 		}
		 	}
		 	
		 	case IN_STR -> {
		 		switch(ch) {
		 			case '"' -> {
		 				addToken(Kind.STRING_LIT);
		 				return;
		 			}
		 			case 0 -> {
                		throw new LexicalException("String is not complete");
		 			}
		 		}
		 	}
		 	case IN_FLOAT -> {
		 		switch(ch) {
				 	case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
		            }
		 			
			 		default -> {
	                	BigDecimal val = new BigDecimal(chars.substring(start, pos));
	                	BigDecimal maxVal = BigDecimal.valueOf(Float.MAX_VALUE);
	                	if (val.compareTo(maxVal) > 0) {
	                		throw new LexicalException("Float is too large");
	                	}
	                    addToken(Kind.FLOAT_LIT);
	                    --pos;
	                    --columns;
			 			return;
			 			}
		 		}
		 	}
		 	case IN_IDENT -> {
		 		switch(ch) {
			 		case '\n' -> {
			 			String text = chars.substring(start, pos);
				 	    Kind kind = keywords.get(text);
				 	    if (kind == null) kind = Kind.IDENT;
				 	    addToken(kind);
			 			lines++;
			 			columns = 0;
				 	    return;
			 		}
			 		
			 		case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		    		 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		    		 '$', '_', '0', '1', '2', '3', '4', '5', '6', '7', '9' -> {
		    			 state = State.IN_IDENT;
		    		 }	
		 			
			 		default -> {
			 			String text = chars.substring(start, pos);
				 	    Kind kind = keywords.get(text);
				 	    if (kind == null) kind = Kind.IDENT;
				 	    addToken(kind);
				 	    --pos;
				 	    --columns;
				 	    return;
			 		}
		 		}
		 		
		 	}
		 	case HAVE_ZERO -> {
			 	switch(ch) {
			 		case '.' -> {
			 			state = State.HAVE_DOT;
			 		}
			 		default -> {
			 			addToken(Kind.INT_LIT);
				 	    --pos;
				 	    --columns;
				 	    return;
			 		}
		 		}
		 		
		 	}
		 	case HAVE_DOT -> {
		 		switch (ch) {
		 			case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
		 				state = State.IN_FLOAT;
		 			}
		 			default -> {
		 				throw new LexicalException("Float is not complete");
		 			}
		 		}
		 	}
		 	case HAVE_MINUS -> {
			 	switch(ch) {
			 		case '>' -> {
			 			addToken(Kind.RARROW);
			 			return;
			 			
			 		}
		 			
		 			
			 		default -> {
			 			addToken(Kind.MINUS);
				 	    --pos;
				 	    --columns;
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
			 		
		 			default -> {
		 				addToken(Kind.LT);
				 	    --pos;
				 	    --columns;
		 				return;
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
		 			
			 		default -> {
			 			addToken(Kind.GT);
				 	    --pos;
				 	    --columns;
			 			return;
			 		}
			 		
		 		}
		 	}
		 	case HAVE_HASH -> {
		 		switch(ch) {
		 			case '\n', 0 -> {
		 				lines++;
		 				columns = 0;
		 				return;
		 			}
		 			default -> {
		 			}
		 		}
		 	}
		 }
	  }
  }
};