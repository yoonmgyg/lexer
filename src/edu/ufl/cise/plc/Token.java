package edu.ufl.cise.plc;
public class Token implements IToken {
	final Kind kind;
	final String input;
	final SourceLocation loc;
	final int length;

	public Token(Kind kind, String input, SourceLocation loc, int length) {
	    this.kind = kind;
	    this.input = input;
	    this.loc = loc;
	    this.length = length;
	  }

	
	public Kind getKind(){return kind;}
    @Override 
    public String getText() {
    	if (kind == Kind.PLUS) {
    		return "+";
    	}
    	else if (kind == Kind.MINUS) {
    		return "-";
    	}
    	else if (kind == Kind.TIMES) {
    		return "*";
    	}
    	else if (kind == Kind.DIV) {
    		return "/";
    	}
    	return input;
      }
 
    @Override  
    public int getIntValue(){
		if (kind == Kind.INT_LIT) {
			return Integer.parseInt(input);
		}
		return 0;
    }
	@Override
	public SourceLocation getSourceLocation() {
		return loc;
	}
	@Override
	public float getFloatValue() {
		if (kind == Kind.FLOAT_LIT) {
			return Float.parseFloat(input);
		}
		return 0;
	}
	@Override
	public boolean getBooleanValue() {
		if (kind == Kind.BOOLEAN_LIT) {
			return Boolean.parseBoolean(input);
		}
		return false;
	}
	@Override

	public String getStringValue() {
        String stringVal = "";

            for(int i = 1; i < input.length()- 1; i++) {
                char ch = input.charAt(i);
                if (ch == '\\') {
                    i++;
                    ch = input.charAt(i);
                    switch (ch) {
                        case 'n' -> {
                            stringVal += '\n';
                        }

                        case 'b' -> {
                            stringVal += '\b';
                        }

                        case 't' -> {
                            stringVal += '\t';
                        }

                        case 'f' -> {
                            stringVal += '\f';
                        }

                        case 'r' -> {
                            stringVal += '\r';
                        }

                        case '\"' -> {
                            stringVal += '\"';
                        }

                        case '\'' -> {
                            stringVal += "\'";

                        }
                        case '\\' -> {
                            stringVal += '\\';
                        }
                    }
                }
                else {
                    stringVal += input.charAt(i);
                }
            }

        return stringVal;
    }
}
