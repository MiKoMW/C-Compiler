package sem;

import ast.*;

import java.util.LinkedList;
import java.util.List;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	Scope scope;

	@Override
	public Void visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitStructTypeDecl(StructTypeDecl sts) {


		if(scope.lookupCurrent(sts.struct_type.struct_Name) != null){
			error("Var " + sts.struct_type.struct_Name +" was declared.");
			return null;
		}

		StructSymbol structSymbol = new StructSymbol(scope,sts.struct_type);
		scope.put(structSymbol);

		Scope oldScope = scope;
		scope = structSymbol.scope;

		for(VarDecl varDecl : sts.varDecls) {

			if (scope.lookupCurrent(varDecl.varName) != null) {
				error("Var " + varDecl.varName + " was declared.");
				return null;
			}else {
				varDecl.accept(this);
			}
		}

		scope = oldScope;
		// To be completed...
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		Scope oldScope = scope;
		scope = new Scope(oldScope);

		for(VarDecl varDecl : b.vardelcs) {

			if (scope.lookupCurrent(varDecl.varName) != null) {
				error("Var " + varDecl.varName + " was declared.");
				return null;
			}else {
				varDecl.accept(this);
			}
		}

		for(Stmt stmt : b.stmts){
			stmt.accept(this);
		}

		scope = oldScope;

		// To be completed...
		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {



		if (scope.lookupCurrent(p.name) != null) {
			error("Var " + p.name + " was declared.");
			return null;
		}

		FunDeclSymbol funDeclSymbol = new FunDeclSymbol(scope,p);

		scope.put(funDeclSymbol);
		Scope oldScope = scope;
		scope = new Scope(oldScope);

		for(VarDecl varDecl : p.params) {

			if (scope.lookupCurrent(varDecl.varName) != null) {
				error("Var " + varDecl.varName + " was declared.");
				return null;
			}else {
				varDecl.accept(this);
			}
		}

		for(VarDecl varDecl : p.block.vardelcs) {

			if (scope.lookupCurrent(varDecl.varName) != null) {
				error("Var " + varDecl.varName + " was declared.");
				return null;
			}else {
				varDecl.accept(this);
			}
		}

		for(Stmt stmt : p.block.stmts) {
			stmt.accept(this);
		}

		scope = oldScope;
		// To be completed...
		return null;
	}


	@Override
	public Void visitProgram(Program p) {

		scope = new Scope();

		List<VarDecl> params_print_s = new LinkedList<>();
		params_print_s.add(new VarDecl(new PointerType(BaseType.CHAR),"s"));
		FunDecl print_s = new FunDecl(BaseType.VOID,"print_s",params_print_s,null);
		FunDeclSymbol symbol_print_s = new FunDeclSymbol(scope,print_s);
		scope.put(symbol_print_s);

		List<VarDecl> params_print_i = new LinkedList<>();
		params_print_i.add(new VarDecl(BaseType.INT,"i"));
		FunDecl print_i = new FunDecl(BaseType.VOID,"print_i",params_print_i,null);
		FunDeclSymbol symbol_print_i = new FunDeclSymbol(scope,print_i);
		scope.put(symbol_print_i);

		List<VarDecl> params_print_c = new LinkedList<>();
		params_print_c.add(new VarDecl(BaseType.CHAR,"c"));
		FunDecl print_c = new FunDecl(BaseType.VOID,"print_c",params_print_c,null);
		FunDeclSymbol symbol_print_c = new FunDeclSymbol(scope,print_c);
		scope.put(symbol_print_c);

		List<VarDecl> params_read_c = new LinkedList<>();
		FunDecl read_c = new FunDecl(BaseType.CHAR,"read_c",params_read_c,null);
		FunDeclSymbol symbol_read_c = new FunDeclSymbol(scope,read_c);
		scope.put(symbol_read_c);

		List<VarDecl> params_read_i = new LinkedList<>();
		FunDecl read_i = new FunDecl(BaseType.INT,"read_i",params_read_i,null);
		FunDeclSymbol symbol_read_i = new FunDeclSymbol(scope,read_i);
		scope.put(symbol_read_i);

		List<VarDecl> params_mcmalloc = new LinkedList<>();
		params_mcmalloc.add(new VarDecl(BaseType.INT,"size"));
		FunDecl mcmalloc = new FunDecl(new PointerType(BaseType.VOID),"mcmalloc",params_mcmalloc,null);
		FunDeclSymbol symbol_mcmalloc = new FunDeclSymbol(scope,mcmalloc);
		scope.put(symbol_mcmalloc);

		for(StructTypeDecl structTypeDecl : p.structTypeDecls){
			structTypeDecl.accept(this);
		}

		for(VarDecl varDecl : p.varDecls){
			varDecl.accept(this);
		}

		for(FunDecl funDecl : p.funDecls){
			funDecl.accept(this);
		}

		// To be completed...
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		// To be completed...
		vd.type.accept(this);

		if(scope.lookupCurrent(vd.varName) != null){
			error("Var " + vd.varName + " was declared twice.");
			return null;
		}

		VarSymbol varSymbol = new VarSymbol(vd);
		scope.put(varSymbol);

		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		// To be completed...

		Symbol symbol = scope.lookup(v.name);

		if(symbol == null){
			error("Name Analysis Var " + v.name + " was not declared.");
			return null;
		}

		v.vd = ((VarSymbol) symbol).varDecl;

		return null;
	}

	// Noting to do?
	public Void visitPointerType(PointerType pointerType){
		return null;
	}

	// Struct app app;
	public Void visitStructType(StructType structType){
		return null;
	}

	public Void visitArrayType(ArrayType arrayType){
		return null;
	}

	public Void visitIntLiteral(IntLiteral intLiteral){
		return null;
	}

	public Void visitStrLiteral(StrLiteral strLiteral){
		return null;
	}
	public Void visitChrLiteral(ChrLiteral chrLiteral){
		return null;
	}

	public Void visitFunCallExpr(FunCallExpr funCallExpr){

		Symbol symbol = scope.lookup(funCallExpr.fun_name);

		if(symbol == null){
			error("Function " + funCallExpr.fun_name + " was not declared.");
			return null;
		}

		if(!(symbol instanceof  FunDeclSymbol)){
			error("Invalid function call " + funCallExpr.fun_name);
			return null;
		}

		for(Expr varExpr : funCallExpr.params){
			varExpr.accept(this);
		}

		funCallExpr.funDecl = ((FunDeclSymbol) symbol).funDecl;

		return null;
	}

	// Op doesn't need to be visited.
	public Void visitBinOp(BinOp binOp){
		//if(binOp.first != null){
			binOp.first.accept(this);
		//}
		//if(binOp.second!= null) {
			binOp.second.accept(this);
		//}
		return null;
	}

	// Op doesn't matter?
	public Void visitOp(Op v){
		return null;
	}

	// 用链接会var declear 吗？
	public Void visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr){

		arrayAccessExpr.array.accept(this);

		arrayAccessExpr.index.accept(this);
		return null;

	}


	//需要检查。
	public Void visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr){

		fieldAccessExpr.struct.accept(this);

		return null;
	}

	public Void visitValueAtExpr(ValueAtExpr valueAtExpr){
		valueAtExpr.accept(this);
		return null;
	}

	public Void visitSizeOfExpr(SizeOfExpr sizeOfExpr){
		return null;
	}

	public Void visitTypecastExpr(TypecastExpr typecastExpr) {
		typecastExpr.expr.accept(this);
		return null;
	}

	public Void visitExprStmt(ExprStmt exprStmt) {
		exprStmt.expr.accept(this);
		return null;
	}

	public Void visitWhile(While w){
		w.stmt.accept(this);
		w.expr.accept(this);
		return null;
	}

	public Void visitIf(If i) {
		i.expr.accept(this);
		i.stmt1.accept(this);
		if(i.stmt2!= null){
			i.stmt2.accept(this);
		}
		return null;
	}

	public Void visitAssign(Assign assign){
		assign.lhs.accept(this);
		assign.rhs.accept(this);
		return null;
	}

	public Void visitReturn(Return r){
		if(r.expr!= null) {
			r.expr.accept(this);
		}
		return null;
	}

	// To be completed...


}
