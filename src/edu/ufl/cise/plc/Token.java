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
        return kind + " " + input;
      }
    @Override  
    public int getIntValue(){
    	return 0;
    }
	@Override
	public SourceLocation getSourceLocation() {
		return loc;
	}
	@Override
	public float getFloatValue() {
		return 0;
	}
	@Override
	public boolean getBooleanValue() {
		return false;
	}
	@Override
	public String getStringValue() {
		return input;
	}
}
