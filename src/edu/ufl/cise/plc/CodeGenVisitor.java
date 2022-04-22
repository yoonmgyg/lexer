
package edu.ufl.cise.plc;

import java.util.List;
import java.util.HashSet;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.Types.Type;


public class CodeGenVisitor implements ASTVisitor {
	String packageName;
	HashSet<String> imports = new HashSet<String>(); 
	
	public CodeGenVisitor(String name) {
		packageName = name;
	}
	
	private boolean genTypeConversion(Type type, Type coerceTo, CodeGenStringBuilder sb) {
		// TODO Auto-generated method stub
		if (type == coerceTo || coerceTo == null || coerceTo == Type.IMAGE) {
			return false;
		}
		sb.lparen();
		sb.append(getReturn(coerceTo));
		sb.rparen().append(" ");
		
		
		return true;
	}
	
	private String getReturn(Declaration declaration) {
		Type type = declaration.getType();
		if (type == Type.STRING) {
			return "String";
		}

		else if (type == Type.COLOR){
			imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
			return "ColorTuple";
		}
		else if (type == Type.IMAGE) {
			return "BufferedImage";
		}
		else {
			return type.toString().toLowerCase();
		}
	}

	private String getReturn(Type type) {
		if (type == Type.STRING) {
			return "String";
		}

		else if (type == Type.COLOR){
			imports.add("java.awt.Color");
			return "ColorTuple";
		}
		else if (type == Type.IMAGE) {
			return "BufferedImage";
		}
		else {
			return type.toString().toLowerCase();
		}
	}
	
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Declaration nameDef = assignmentStatement.getTargetDec();
		Expr expr = assignmentStatement.getExpr();
		if (nameDef.getType() == Type.IMAGE)  {
			String[] pixels = (String[]) assignmentStatement.getSelector().visit(this, sb);
			sb.append("for (int " + pixels[0] + " = 0; ");
			sb.append(pixels[0] + " < " + assignmentStatement.getName() + ".getWidth();");
			sb.append(pixels[0] + "++)\n\t");
			sb.append("for(int " + pixels[1] + " = 0; ");
			sb.append(pixels[1] + " < " + assignmentStatement.getName() + ".getHeight();");
			sb.append(pixels[1] + "++)\n\t\t");
			
			imports.add("edu.ufl.cise.plc.runtime.ImageOps");
			Dimension dim = nameDef.getDim();
			if (dim != null) {
				imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
				if (expr.getCoerceTo() == Type.COLOR) {
					sb.append("ImageOps.setColor(" + assignmentStatement.getName() + ",");
					sb.append(pixels[0] + "," + pixels[1] + ",");
					expr.visit(this, sb);
					sb.rparen();
				}
				else if (expr.getCoerceTo() == Type.INT) {
					sb.append("ImageOps.setColor(" + assignmentStatement.getName() + ",");
					sb.append(pixels[0] + "," + pixels[1] + ",");
					sb.append("ColorTuple.unpack(ColorTuple.truncate(");
					sb.lparen();
					expr.visit(this, sb);
					sb.rparen().rparen().rparen().rparen();
				}
				else {
					throw new Exception("Invalid dimensions in assignment statement");
				}
			}
			else {
				sb.append("ImageOps.clone(");
				expr.visit(this, sb);
				sb.rparen();
			}
			
		}
		else {
			sb.append(" = ");
			genTypeConversion(assignmentStatement.getExpr().getType() , assignmentStatement.getTargetDec().getType(), sb);
	    	sb.lparen();
	    	expr.visit(this,  sb);
	    	sb.rparen();
		}
    	sb.semi().newline();
		return sb;
	}
	
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		  CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		  Expr expr = returnStatement.getExpr();
		  sb.append("return ");
		  expr.visit(this, sb);
		  sb.semi().newline();
		  return sb;
		}
	
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		
		Expr leftExpr = binaryExpr.getLeft();
		Expr rightExpr = binaryExpr.getRight();
		Type leftType = leftExpr.getCoerceTo() != null ? leftExpr.getCoerceTo() : leftExpr.getType();
		Type rightType = rightExpr.getCoerceTo() != null ? rightExpr.getCoerceTo() : rightExpr.getType();
		Kind op = binaryExpr.getOp().getKind();
		
		/*
		if (binaryExpr.getCoerceTo() != type) {
			   genTypeConversion(type, binaryExpr.getCoerceTo(), sb);
		}
		*/
		
		sb.lparen();
		if (leftType == Type.COLOR && rightType == Type.COLOR) {
			imports.add("edu.ufl.cise.plc.runtime.ImageOps");
			if (op.toString() == "EQUALS" || op.toString() == "NOT_EQUALS") {
				sb.append("ImageOps.binaryTupleOp(ImageOps.BoolOP.");
			}
			else {
				sb.append("ImageOps.binaryTupleOp(ImageOps.OP.");
			}
			sb.append(op.toString()).comma();
			leftExpr.visit(this, sb);
			sb.comma();
			rightExpr.visit(this, sb);
			sb.rparen();
		}
		else if (leftType == Type.IMAGE && rightType == Type.IMAGE) {
			imports.add("edu.ufl.cise.plc.runtime.ImageOps");
			if (op.toString() == "EQUALS" || op.toString() == "NOT_EQUALS") {
				sb.append("ImageOps.binaryImageImageOp(ImageOps.BoolOP.");
			}
			else {
				sb.append("ImageOps.binaryImageImageOp(ImageOps.OP.");
			}
			sb.append(op.toString()).comma();
			leftExpr.visit(this, sb);
			sb.comma();
			rightExpr.visit(this, sb);
			sb.rparen();
		}
		
		else if (leftType == Type.IMAGE && rightType == Type.COLOR) {
			imports.add("edu.ufl.cise.plc.runtime.ImageOps");
			sb.append("ImageOps.binaryImageScalarOp(ImageOps.OP.");
			sb.append(op.toString()).comma();
			leftExpr.visit(this, sb);
			sb.comma();
			rightExpr.visit(this, sb);
			sb.rparen().semi().newline();
			
			sb.append("ColorTuple.makePackedColor(ColorTuple.getRed(");
			rightExpr.visit(this, sb);
			sb.comma();
			
			sb.append("ColorTuple.getGreen(");
			rightExpr.visit(this, sb);
			sb.comma();
			
			sb.append("ColorTuple.getBlue(");
			rightExpr.visit(this, sb);
			sb.rparen().rparen();
		}
		
		else if (leftType == Type.IMAGE && rightType == Type.INT) {
			imports.add("edu.ufl.cise.plc.runtime.ImageOps");
			sb.append("ImageOps.binaryImageScalarOp(ImageOps.OP.");
			sb.append(op.toString()).comma();
			leftExpr.visit(this, sb);
			sb.comma();
			rightExpr.visit(this, sb);
			sb.rparen();
		}
		else {

			leftExpr.visit(this, sb);
			sb.append(binaryExpr.getOp().getText());
			rightExpr.visit(this, sb);

		}
		sb.rparen();
		

		return sb;
	}


	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.append(booleanLitExpr.getValue());
		return sb;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.quote().quote().quote();
		sb.newline();
		sb.append(stringLitExpr.getValue());
		sb.quote().quote().quote();
		return sb;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type intType = intLitExpr.getCoerceTo() != null ? intLitExpr.getCoerceTo() : intLitExpr.getType();
		if (intType != Type.INT) {
			   genTypeConversion(Type.INT, intType, sb);
		}
		sb.append(intLitExpr.getValue());
		return sb;
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type floatType = floatLitExpr.getCoerceTo() != null ? floatLitExpr.getCoerceTo() : floatLitExpr.getType();
		if (floatType != Type.FLOAT) {
			   genTypeConversion(Type.FLOAT, floatType, sb);
		}
		sb.append(floatLitExpr.getValue());
		if (floatType == Type.FLOAT) {
			sb.append("f");
		}
		return sb;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		imports.add("java.awt.Color");
		imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
		sb.append("ColorTuple.unpack(Color." + colorConstExpr.getText() + ".getRGB())");
		
		return sb;

	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		imports.add("edu.ufl.cise.plc.runtime.ConsoleIO");
		Type boxedType = consoleExpr.getCoerceTo();
		String boxedString;
		if (boxedType == Type.INT) {
			boxedString = "Integer";
		}
		else if (boxedType == Type.BOOLEAN) {
			boxedString = "Boolean";
		}
		else if (boxedType == Type.FLOAT) {
			boxedString = "Float";
		}
		else if (boxedType == Type.STRING) {
			boxedString = "String";
		}
		else {
			boxedString = boxedType.toString();
		}
		sb.lparen().append(boxedString).rparen();
		sb.append(" ConsoleIO.readValueFromConsole");
		sb.lparen().quote();
		sb.append(boxedType.toString()).quote().comma().append(" ");
		if (boxedType == Type.COLOR) {
			sb.append("\"Enter RGB values: ");
		}
		else {
			sb.append("\"Enter " + boxedString.toLowerCase() +": \"");
		}
		sb.rparen();
		return sb;
	}

	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.append("new ColorTuple(" );
		colorExpr.getRed().visit(this, sb);
		sb.comma();
		colorExpr.getGreen().visit(this, sb);
		sb.comma(); 
		colorExpr.getBlue().visit(this, sb);
		sb.rparen();
		return sb;

	}

	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		
		IToken unOp = unaryExpression.getOp();
		sb.lparen();
		
		if (unOp.getKind() == Kind.COLOR_OP) {
			if (unaryExpression.getType() == Type.INT || unaryExpression.getType() == Type.COLOR) {
				sb.append("ColorTuple.");
				sb.append(unOp.toString());
				sb.lparen();
				unaryExpression.visit(this, sb);
				sb.rparen();
			}
			else if (unaryExpression.getType() == Type.IMAGE) {
				imports.add("edu.ufl.cise.plc.runtime.ImageOps");
				sb.append("ColorTuple.extract");
				switch (unOp.getText()) {
					case ("getRed") -> sb.append("Red");
					case ("getGreen") -> sb.append("Green");
					case ("getBlue") -> sb.append("Blue");
				}
				sb.lparen();
				unaryExpression.visit(this, sb);
				sb.rparen();
			}
			else {
				throw new Exception("Invalid unary expression type");
			}
		}
		else {
			sb.append(unOp.getText());
			unaryExpression.getExpr().visit(this, sb);
		}
		sb.rparen();
		
		return sb;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		// TODO Auto-generated method stub
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type identType = identExpr.getCoerceTo() != null ? identExpr.getCoerceTo() : identExpr.getType();
		if (identType != identExpr.getType() && identType != Type.IMAGE) {
			   genTypeConversion(identExpr.getType(), identType, sb);
		}
		sb.append(identExpr.getText());
		return sb;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.lparen();
		conditionalExpr.getCondition().visit(this, sb);
		sb.append(" ? ");
		conditionalExpr.getTrueCase().visit(this, sb);
		sb.append(" : ");
		conditionalExpr.getFalseCase().visit(this, sb);
		sb.rparen();
		return sb;
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		dimension.getWidth().visit(this, sb);
		sb.comma();
		dimension.getHeight().visit(this, sb);
		return sb;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		String[] sb=new String[2];
		sb[0] = pixelSelector.getX().getText();
		sb[1] = pixelSelector.getY().getText();
		return sb;
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		imports.add("edu.ufl.cise.plc.runtime.ConsoleIO");
		
		Expr source = writeStatement.getSource();
		Expr target = writeStatement.getDest();
		if (source.getType()== Type.IMAGE) {
			if (target.getType() == Type.CONSOLE) {
				sb.append("ConsoleIO.displayImageOnScreen(");
				sb.append(source.getText());
				sb.rparen();
			}
			else if (target.getType() == Type.STRING) {
				sb.append("ConsoleIO.writeImage(");
				sb.append(source.getText());
				sb.comma();
				sb.append(target.getText());
				sb.rparen();
			}
		}
		else if (target.getType() == Type.STRING)
		{				
			sb.append("ConsoleIO.writeValue(");
			source.visit(this, sb);
			sb.comma();
			sb.append(target.getText());
			sb.rparen();
		}
		else {
			sb.append("ConsoleIO.console.println");
			sb.lparen();
			writeStatement.getSource().visit(this, sb);
			sb.rparen();
		}
		sb.semi().newline();
		return sb;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
		String readName = readStatement.getName();
		sb.append(readName);
		sb.append(" = ");
		Declaration targetDec = readStatement.getTargetDec();
		Expr expr = readStatement.getSource();
		if (targetDec.getType() == Type.IMAGE) {
			if (targetDec.isInitialized() == true) {
				if (targetDec.getDim() != null) {
					sb.append("FileURLIO.readImage(");
					expr.visit(this, sb);
					sb.comma();
					targetDec.getDim().visit(this, sb);
					sb.rparen().semi().newline();
					sb.append("FileURLIO.closeFiles()");
				}
				else {
					sb.append("FileURLIO.readImage(");
					expr.visit(this, sb);
					sb.rparen();
				}
			}
		}
		else {
			expr.visit(this,  sb);
		}
		sb.semi().newline();
		return sb;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		CodeGenStringBuilder sb = new CodeGenStringBuilder();
		sb.append("package ").append(packageName).semi().newline();
		sb.append("public class ").append(program.getName()).append(" {").newline();
		sb.append("public static ");
		Type progType = program.getReturnType();
		sb.append(getReturn(progType));
		sb.append(" apply").lparen();
		List<NameDef> params = program.getParams();
		for (int i = 0; i < params.size();i++) {
			sb.append(getReturn(params.get(i)));
			sb.append(" ");
			sb.append(params.get(i).getName());
			if (i != params.size() - 1) {
				sb.append(",");
			}
		}
		sb.rparen().append("{").newline();
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode i : decsAndStatements) {
			i.visit(this, sb);
		}
		sb.append("}").newline().append("}");
		for (String s: imports) {
			sb.insert(10 + packageName.length(), "import " + s + ";\n");
		}
		return sb.toString();
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.append(getReturn(nameDef)).append(" ");
		sb.append(nameDef.getName());
		return sb;
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDsdim, Object arg) throws Exception {
		
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.append(getReturn(nameDefWithDsdim)).append(" ");
		sb.append(nameDefWithDsdim.getName());
		return sb;
		/*
		if (nameDefWithDsdim.isInitialized()) {
			sb.append(getReturn(nameDefWithDsdim)).append(" ");
			sb.append(nameDefWithDsdim.getName());
			return sb;
		}
		else {			
			sb.append(getReturn(nameDefWithDsdim)).append(" ");
			sb.append(nameDefWithDsdim.getName());
			sb.append("BufferedImage " + nameDefWithDsdim.getName() + " = new BufferedImage(");
			nameDefWithDsdim.getDim().visit(this, sb);
			sb.comma();
			sb.append("BufferedImage.TYPE_INT_RGB)").semi().newline();
		}
		*/
	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		declaration.getNameDef().visit(this, sb);
		
	 	IToken decOp = declaration.getOp();
		if (declaration.getType() == Type.IMAGE) {
			imports.add("java.awt.image.BufferedImage");
			sb.append(" = ");

			if (declaration.getExpr() == null) {
				sb.append(" new BufferedImage(");
				declaration.getDim().visit(this, sb);
				sb.comma();
				sb.append("BufferedImage.TYPE_INT_RGB)");
			}
			else if (declaration.getOp().getKind() == Kind.LARROW) {
				imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
				if (declaration.getDim() != null) {
					sb.append("FileURLIO.readImage(");
					declaration.getExpr().visit(this, sb);
					sb.comma();
					declaration.getDim().visit(this, sb);
					sb.rparen().semi().newline();
				}
				else {
					sb.append("FileURLIO.readImage(");
					declaration.getExpr().visit(this, sb);
					sb.rparen().semi().newline();
				}
				sb.append("FileURLIO.closeFiles()");
			}
			else {
				genTypeConversion(declaration.getExpr().getType(),declaration.getType(),  sb);
				declaration.getExpr().setType(declaration.getType());
				sb.lparen();
				declaration.getExpr().visit(this, sb);
				sb.rparen();
			}
		}
		else if (decOp != null) {
			sb.append(" = ");
			genTypeConversion(declaration.getExpr().getType(),declaration.getType(),  sb);
			declaration.getExpr().setType(declaration.getType());
			sb.lparen();
			declaration.getExpr().visit(this, sb);
			sb.rparen();
		}
		sb.semi().newline();
		return sb;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.append("ColorTuple.unpack(" + unaryExprPostfix.getText() + ".getRGB(");
		String[] selector = (String[]) unaryExprPostfix.getSelector().visit(this, sb);
		sb.append(selector[0]).comma().append(selector[1]);
		sb.rparen().rparen();
		return sb;

	}
	
}

