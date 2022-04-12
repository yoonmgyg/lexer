package edu.ufl.cise.plc;
import java.util.List;
import java.util.HashSet;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Types.Type;


public class CodeGenVisitor implements ASTVisitor {
	String packageName;
	HashSet<String> imports = new HashSet<String>(); 
	
	public CodeGenVisitor(String name) {
		packageName = name;
	}
	
	private boolean genTypeConversion(Type type, Type coerceTo, CodeGenStringBuilder sb) {
		// TODO Auto-generated method stub
		if (type == coerceTo) {
			return false;
		}
		sb.lparen();
		sb.append(coerceTo.toString().toLowerCase());
		sb.rparen().append(" ");
		return true;
	}
	
	private String getReturn(Declaration declaration) {
		Type type = declaration.getType();
		if (type == Type.STRING) {
			return "String";
		}
		else {
			return type.toString().toLowerCase();
		}
	}
	
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.append(assignmentStatement.getName());
		sb.append(" = ");
	    genTypeConversion(assignmentStatement.getExpr().getType() , assignmentStatement.getTargetDec().getType(), sb);
	    sb.lparen();
		Expr expr = assignmentStatement.getExpr();
		expr.visit(this,  sb);
		sb.rparen();
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
		/*
		Expr leftExpr = binaryExpr.getLeft();
		Expr rightExpr = binaryExpr.getRight();
		Type leftType = leftExpr.getCoerceTo() != null ? leftExpr.getCoerceTo() : leftExpr.getType();
		Type rightType = rightExpr.getCoerceTo() != null ? rightExpr.getCoerceTo() : rightExpr.getType();
		Kind op = binaryExpr.getOp().getKind();
		*/
		sb.lparen();
		binaryExpr.getLeft().visit(this, sb);
		sb.append(binaryExpr.getOp().getText());
		binaryExpr.getRight().visit(this, sb);
		sb.rparen();
		/*
		if (binaryExpr.getCoerceTo() != type) {
			   genTypeConversion(type, binaryExpr.getCoerceTo(), sb);
		}
		*/
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
		throw new Exception("Not yet implemented");

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
		else {
			boxedString = boxedType.toString();
		}
		sb.lparen().append(boxedString).rparen();
		sb.append(" ConsoleIO.readValueFromConsole");
		sb.lparen().quote();
		sb.append(boxedType.toString()).quote().comma().append(" ");
		sb.append("\"Enter " + boxedString.toLowerCase() +": \"").rparen().semi().newline();;
		return sb;
	}

	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		throw new Exception("Not yet implemented");

	}

	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.lparen();
		sb.append(unaryExpression.getOp().getText());
		unaryExpression.getExpr().visit(this, sb);
		sb.rparen();
		return sb;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		// TODO Auto-generated method stub
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		Type identType = identExpr.getCoerceTo() != null ? identExpr.getCoerceTo() : identExpr.getType();
		if (identType != identExpr.getType()) {
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
		throw new Exception("Not yet implemented");

	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		throw new Exception("Not yet implemented");

	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		imports.add("edu.ufl.cise.plc.runtime.ConsoleIO");
		sb.append("ConsoleIO.console.println");
		sb.lparen();
		writeStatement.getSource().visit(this, sb);
		sb.rparen();
		sb.semi().newline();
		return sb;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		sb.append(readStatement.getName());
		sb.append(" = ");
		Expr expr = readStatement.getSource();
		expr.visit(this,  sb);
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
		if (progType == Type.STRING) {
			sb.append("String ");
		}
		else {
			sb.append(progType.toString().toLowerCase());
		}
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
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		throw new Exception("Not yet implemented");
	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
		declaration.getNameDef().visit(this, sb);
		if (declaration.getOp() != null) {
			sb.append(" = ");
			genTypeConversion(declaration.getExpr().getType(),declaration.getType(),  sb);
			sb.lparen();
			declaration.getExpr().visit(this, sb);
			sb.rparen();
		}
		sb.semi().newline();
		return sb;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		throw new Exception("Not yet implemented");

	}
	
}
