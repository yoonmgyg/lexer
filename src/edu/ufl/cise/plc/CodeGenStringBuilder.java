package edu.ufl.cise.plc;

public class CodeGenStringBuilder {
    StringBuilder delegate = new StringBuilder();
    //methods reimplemented—just call the delegates method
       public CodeGenStringBuilder append(String s){
           delegate.append(s);
           return this;
       }
       public String toString() {
    	   return delegate.toString();
       }
       public CodeGenStringBuilder append(int s){
           delegate.append(s);
           return this;
       }

       public CodeGenStringBuilder insert(int pos, String s){
           delegate.insert(pos, s);
           return this;
       }
       public CodeGenStringBuilder append(float s){
           delegate.append(s);
           return this;
       }

       public CodeGenStringBuilder append(CodeGenStringBuilder s){
           delegate.append(s);
           return this;
       }
       
       public CodeGenStringBuilder append(boolean s){
           delegate.append(s);
           return this;
       }
       
       //new methods
       public CodeGenStringBuilder comma(){
            delegate.append(",");
            return this;
       }
       
       public CodeGenStringBuilder semi() {
    	   delegate.append(";");
    	   return this;
       }
       public CodeGenStringBuilder newline() {
    	   delegate.append("\n");
    	   return this;
       }

       public CodeGenStringBuilder lparen() {
    	   delegate.append("(");
    	   return this;
       }

       public CodeGenStringBuilder rparen() {
    	   delegate.append(")");
    	   return this;
       }

       public CodeGenStringBuilder quote() {
    	   delegate.append("\"");
    	   return this;
       }
}