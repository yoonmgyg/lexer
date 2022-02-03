import edu.ufl.cise.plc.*;

public class Token implements IToken {
	final Kind kind;
	final String input;
	final int pos;
	final int length;
	final Object literal;

	Token(Kind kind, String input, int pos, int length, Object literal) {
	    this.kind = kind;
	    this.input = input;
	    this.pos = pos;
	    this.length = length;
	    this.literal = literal;
	  }

	
	public Kind getKind(){return kind;}
    @Override 
    public String getText() {
        return kind + " " + input + " " + literal;
      }
    @Override  
    public int getIntValue(){
    	return 0;
    }
	@Override
	public SourceLocation getSourceLocation() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public float getFloatValue() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean getBooleanValue() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String getStringValue() {
		// TODO Auto-generated method stub
		return null;
	}
}
