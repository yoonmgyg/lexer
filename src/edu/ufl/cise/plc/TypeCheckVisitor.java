package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.Statement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable symbolTable = new SymbolTable();  
	Program root;
	
	record Pair<T0,T1>(T0 t0, T1 t1){};  //may be useful for constructing lookup tables.
	
	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}
	
	private boolean assignmentCompatible(Type targetType, Type rhsType) {
		return (targetType == rhsType 
		|| targetType==Type.STRING && rhsType==Type.INT
		|| targetType==Type.STRING && rhsType==Type.BOOLEAN
		|| targetType==Type.INT && rhsType==Type.FLOAT
		|| targetType==Type.FLOAT && rhsType==Type.INT
		|| targetType==Type.INT && rhsType==Type.COLOR
		|| targetType==Type.COLOR && rhsType==Type.INT
		|| targetType==Type.IMAGE && rhsType==Type.INT
		|| targetType==Type.IMAGE && rhsType==Type.FLOAT
		|| targetType==Type.IMAGE && rhsType==Type.COLOR
		|| targetType==Type.IMAGE && rhsType==Type.COLORFLOAT
		|| targetType==Type.STRING && rhsType==Type.CONSOLE
		|| targetType==Type.IMAGE && rhsType==Type.STRING
		|| targetType==Type.IMAGE && rhsType==Type.COLOR
		
		);
	}
	
	//The type of a BooleanLitExpr is always BOOLEAN.  
	//Set the type in AST Node for later passes (code generation)
	//Return the type for convenience in this visitor.  
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(Type.BOOLEAN);
		return Type.BOOLEAN;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		stringLitExpr.setType(Type.STRING);
	    return Type.STRING;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		 intLitExpr.setType(Type.INT);
		 return Type.INT;
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(Type.FLOAT);
		return Type.FLOAT;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		colorConstExpr.setType(Type.COLOR);
		return Type.COLOR;
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(Type.CONSOLE);
		return Type.CONSOLE;
	}
	
	//Visits the child expressions to get their type (and ensure they are correctly typed)
	//then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == Type.INT || redType == Type.FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == Type.INT) ? Type.COLOR : Type.COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}	

	
	
	//Maps forms a lookup table that maps an operator expression pair into result type.  
	//This more convenient than a long chain of if-else statements. 
	//Given combinations are legal; if the operator expression pair is not in the map, it is an error. 
	Map<Pair<Kind,Type>, Type> unaryExprs = Map.of(
			new Pair<Kind,Type>(Kind.BANG,BOOLEAN), BOOLEAN,
			new Pair<Kind,Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind,Type>(Kind.MINUS, INT),INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,INT), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,COLOR), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,IMAGE), IMAGE,
			new Pair<Kind,Type>(Kind.IMAGE_OP,IMAGE), INT
			);
	
	//Visits the child expression to get the type, then uses the above table to determine the result type
	//and check that this node represents a legal combination of operator and expression type. 
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		//Use the lookup table above to both check for a legal combination of operator and expression, and to get result type.
		Type resultType = unaryExprs.get(new Pair<Kind,Type>(op,exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		//Save the type of the unary expression in the AST node for use in code generation later. 
		unaryExpr.setType(resultType);
		//return the type for convenience in this visitor.
		return resultType;
	}


	//This method has several cases. Work incrementally and test as you go. 
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		Kind op = binaryExpr.getOp().getKind();
		Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
		Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
		Type resultType = null;
		switch(op) {//AND, OR, PLUS, MINUS, TIMES, DIV, MOD, EQUALS, NOT_EQUALS, LT, LE, GT,GE
		case AND, OR -> {
			check(leftType == Type.BOOLEAN && rightType == Type.BOOLEAN, binaryExpr, "incompatible types for comparison");
			resultType = Type.BOOLEAN;
		}
		case EQUALS,NOT_EQUALS -> {
			check(leftType == rightType, binaryExpr, "incompatible types for comparison");
			resultType = Type.BOOLEAN;
		}
		case PLUS, MINUS -> {
			if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
			else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
			else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
			else if (leftType == Type.FLOAT && rightType == Type.FLOAT) resultType = Type.FLOAT;
			else if (leftType == Type.INT && rightType == Type.FLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.FLOAT);
				resultType = Type.FLOAT;
			}
			else if (leftType == Type.FLOAT && rightType == Type.INT) {
				binaryExpr.getRight().setCoerceTo(Type.FLOAT);
				resultType = Type.FLOAT;resultType = Type.FLOAT;
			}
			else if (leftType == Type.COLOR && rightType == Type.COLOR) resultType = Type.COLOR;
			else if (leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT) resultType = Type.COLORFLOAT;
			else if (leftType == Type.COLORFLOAT && rightType == Type.COLOR) {
				binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				resultType = Type.COLORFLOAT;
			}
			else if (leftType == Type.COLOR && rightType == Type.COLORFLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				resultType = Type.COLORFLOAT;
			}

			else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;

			else check(false, binaryExpr, "incompatible types for operator");
		}
		case TIMES, DIV, MOD -> {
			if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
			else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
			else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
			else if (leftType == Type.FLOAT && rightType == Type.FLOAT) resultType = Type.FLOAT;
			else if (leftType == Type.INT && rightType == Type.FLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.FLOAT);
				resultType = Type.FLOAT;
	}
			else if (leftType == Type.FLOAT && rightType == Type.INT) {
				binaryExpr.getRight().setCoerceTo(Type.FLOAT);
				resultType = Type.FLOAT;resultType = Type.FLOAT;
			}
			else if (leftType == Type.COLOR && rightType == Type.COLOR) resultType = Type.COLOR;
			else if (leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT) resultType = Type.COLORFLOAT;
			else if (leftType == Type.COLORFLOAT && rightType == Type.COLOR) {
				binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				resultType = Type.COLORFLOAT;
			}
			else if (leftType == Type.COLOR && rightType == Type.COLORFLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				resultType = Type.COLORFLOAT;
			}

			else if (leftType == Type.IMAGE && rightType == Type.INT) resultType = Type.IMAGE;
			else if (leftType == Type.IMAGE && rightType == Type.FLOAT) resultType = Type.IMAGE;
			else if (leftType == Type.INT && rightType == Type.COLOR) {
				binaryExpr.getLeft().setCoerceTo(Type.COLOR);
				resultType = Type.COLOR;
			}
			else if (leftType == Type.COLOR && rightType == Type.INT) {
				binaryExpr.getRight().setCoerceTo(Type.COLOR);
				resultType = Type.COLOR;
			}
			else if (leftType == Type.FLOAT && rightType == Type.COLOR) {
				binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				resultType = Type.COLORFLOAT;
			}
			else if (leftType == Type.COLOR && rightType == Type.FLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				resultType = Type.COLORFLOAT;
			}
			else check(false, binaryExpr, "incompatible types for operator");
		}
		case LT, LE, GT, GE -> {
			if (leftType == rightType) resultType = Type.BOOLEAN;
			else if (leftType == Type.INT && rightType == Type.FLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.FLOAT);
				resultType = Type.BOOLEAN;
			}
			else if (leftType == Type.FLOAT && rightType == Type.INT) {
				binaryExpr.getRight().setCoerceTo(Type.FLOAT);
				resultType = Type.BOOLEAN;
			}
			else check(false, binaryExpr, "incompatible types for operator");
		}
		default -> {
		throw new Exception("compiler error");
		}
		}
		binaryExpr.setType(resultType);
		return resultType;
		}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		String name = identExpr.getText();
		Declaration dec = symbolTable.lookup(name);
		check(dec !=null , identExpr, "using undeclared variable");
		check(dec.isInitialized(), identExpr, "using uninitialized variable");
		identExpr.setDec(dec);  //save declaration--will be useful later. 
		Type type = dec.getType();
		identExpr.setType(type);
		return type;
		}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		//TODO  implement this method
		
		Type condType = (Type) conditionalExpr.getCondition().visit(this,  arg);
		Type trueType = (Type) conditionalExpr.getTrueCase().visit(this, arg);
		Type falseType = (Type) conditionalExpr.getFalseCase().visit(this, arg);
		check(condType == Type.BOOLEAN, conditionalExpr, "Type of condition must be boolean");
		check(trueType != null, conditionalExpr, "trueType must be Type");
		check(trueType == falseType, conditionalExpr, "Type of true and false cases must be equal");
		conditionalExpr.setType(trueType);
		return trueType;
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		//TODO  implement this method
		Type widthType = (Type) dimension.getWidth().visit(this, arg);
		Type heightType = (Type) dimension.getHeight().visit(this, arg);
		check(widthType == Type.INT && heightType == Type.INT, dimension, "Width and height must be int");
		return null;
	}

	@Override
	//This method can only be used to check PixelSelector objects on the right hand side of an assignment. 
	//Either modify to pass in context info and add code to handle both cases, or when on left side
	//of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == Type.INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == Type.INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}
	

	@Override
	//This method several cases--you don't have to implement them all at once.
	//Work incrementally and systematically, testing as you go.  
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		String name = assignmentStatement.getName();

		Declaration dec = symbolTable.lookup(name);
		Type returnType = null;
		
		check(dec != null, assignmentStatement, "undefined identifier " + name);
		dec.setInitialized(true);
		Type targetType = dec.getType();

		if (targetType != Type.IMAGE) {
			Type exprType = (Type) assignmentStatement.getExpr().visit(this,  arg);
			check(assignmentStatement.getSelector() == null, assignmentStatement, "Non-image cannot have pixel selector");
			check(assignmentCompatible(targetType, exprType), assignmentStatement, "Pairs are assignment incompatible");
			if (targetType == exprType) {
				returnType = targetType;
			}
			else if (targetType == Type.INT && exprType == Type.FLOAT) {
				assignmentStatement.getExpr().setCoerceTo(Type.INT);
				returnType = Type.INT;
			}
			else if (targetType == Type.FLOAT && exprType == Type.INT) {
				assignmentStatement.getExpr().setCoerceTo(Type.FLOAT);
				returnType = Type.FLOAT;
			}
			else if (targetType == Type.INT && exprType == Type.COLOR) {
				assignmentStatement.getExpr().setCoerceTo(Type.INT);
				returnType = Type.INT;
			}
			else if (targetType == Type.COLOR && exprType == Type.INT) {
				assignmentStatement.getExpr().setCoerceTo(Type.COLOR);
				returnType = Type.COLOR;
			}
			else {
				throw new Exception("incompatible assignment");
			}
		}
		else if (targetType == Type.IMAGE && assignmentStatement.getSelector() == null) {
			Type exprType = (Type) assignmentStatement.getExpr().visit(this,  arg);
			check(assignmentCompatible(targetType, exprType), assignmentStatement, "Pairs are assignment incompatible");
			switch(exprType) {
				case INT-> {
					assignmentStatement.getExpr().setCoerceTo(Type.COLOR);
					returnType = Type.COLOR;
				}
				case FLOAT->  {
					assignmentStatement.getExpr().setCoerceTo(Type.COLORFLOAT);
					returnType = Type.COLORFLOAT;
				}
				
				case COLOR -> {
					returnType = Type.COLOR;
				}
				
				case COLORFLOAT-> {
					returnType = Type.COLORFLOAT;
				}
				default -> {
					throw new Exception("incompatible assigment");
				}
			}
		}
		else if (targetType == Type.IMAGE && assignmentStatement.getSelector() != null){
			NameDef localDecX = new NameDef(null, "int", assignmentStatement.getSelector().getX().getText());
			NameDef localDecY = new NameDef(null, "int", assignmentStatement.getSelector().getY().getText());

			localDecX.setInitialized(true);
			localDecY.setInitialized(true);
			symbolTable.insert(assignmentStatement.getSelector().getX().getText(), localDecX);
			symbolTable.insert(assignmentStatement.getSelector().getY().getText(), localDecY);
			Type exprType = (Type) assignmentStatement.getExpr().visit(this,  arg);
			check(exprType == COLOR || exprType == COLORFLOAT || exprType == INT || exprType == FLOAT, assignmentStatement, "Pair are assignment incompatible");
			assignmentStatement.getSelector().visit(this, arg);
			check(symbolTable.lookup(name)!=null , assignmentStatement.getExpr(), "using undeclared variable");
			assignmentStatement.getExpr().setCoerceTo(Type.COLOR);
			returnType = Type.COLOR;
			symbolTable.remove(assignmentStatement.getSelector().getX().getText());
			symbolTable.remove(assignmentStatement.getSelector().getY().getText());
		}
		else {
			throw new Exception("incompatible assignment");
		}
		assignmentStatement.setTargetDec(dec);
		return returnType;

	}


	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, arg);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		check(destType == Type.STRING || destType == Type.CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != Type.CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		
		String name = readStatement.getName();
		Declaration targetDec = symbolTable.lookup(name);
		check(targetDec != null, readStatement, "undefined identifier " + name);
		Type rhsType = (Type) readStatement.getSource().visit(this, arg);
		check(readStatement.getSelector() == null, readStatement, "Read statment cannot have pixel selector");
		check(targetDec != null, readStatement, "undefined identifier " + name);
		
		
		check(rhsType == Type.CONSOLE || rhsType == Type.STRING, readStatement, "illegal destination type for read");
		if (rhsType == Type.CONSOLE) {
			readStatement.getSource().setCoerceTo(targetDec.getType());
		}
		targetDec.setInitialized(true);
		readStatement.setTargetDec(targetDec);  
		Type type = targetDec.getType();
		return type;
		
	}

	@Override
		public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		String name = declaration.getName();
		boolean inserted = symbolTable.insert(name,declaration);
		check(inserted, declaration, "variable " + name + "already declared");
		Expr initializer = declaration.getExpr();
		if (initializer != null) {
			//infer type of initializer
			Type initializerType = (Type) initializer.visit(this,arg);
			/*
			System.out.println(declaration.getType().toString());
			System.out.println(initializerType.toString());
			*/
			check(assignmentCompatible(declaration.getType(), initializerType),declaration, 
			"type of expression and declared type do not match");
			declaration.getExpr().setCoerceTo(declaration.getType());
			declaration.setInitialized(true);
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {		
		//TODO:  this method is incomplete, finish it.  
		
		//Save root of AST so return type can be accessed in return statements
		root = program;
		
		//Check declarations and statements

		List<NameDef> params = program.getParams();
		for (ASTNode node : params) {
			node.visit(this,  arg);
		}
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		return program;
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		nameDef.setInitialized(true);
		check(symbolTable.insert(nameDef.getName(), nameDef), nameDef, "Variable already declared");
		return nameDef.getType();
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		Type dimType = (Type) nameDefWithDim.getDim().visit(this, arg);
		check(dimType == Type.INT, nameDefWithDim, "Dimensions are not type int");
		check(symbolTable.insert(nameDefWithDim.getName(), nameDefWithDim), nameDefWithDim, "Variable already declared");
		return nameDefWithDim.getType();
	}
 
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType();  //This is why we save program in visitProgram.
		Type expressionType = (Type) returnStatement.getExpr().visit(this, arg);
		check(returnType == expressionType, returnStatement, "return statement with invalid type");
		return null;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, arg);
		check(expType == Type.IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(Type.INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return Type.IMAGE;
	}

}
