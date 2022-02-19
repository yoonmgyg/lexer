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
		if (kind == Kind.STRING_LIT) {
			return input;
		}
		return "";
	}
}
