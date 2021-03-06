package sem;

import ast.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	private HashMap<String, StructTypeDecl> structMap = new HashMap<>();

	private Stack<Type> funDeclReturnType = new Stack<>();
	private Stack<FunDecl> FunDecl_Return_Mapping = new Stack<>();

	@Override
	public Type visitBaseType(BaseType bt) {
		// To be completed...
		return bt;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		// To be completed...

		if(structMap.keySet().contains(st.struct_type.struct_Name)){
			error("Duplicate struct declaration.");
			return null;
		}

		structMap.put(st.struct_type.struct_Name,st);

		return null;
	}

	@Override
	public Type visitBlock(Block b) {
		// To be completed...

		for(VarDecl varDecl : b.vardelcs){
			varDecl.accept(this);
		}
		//Type returnType = null;
		for(Stmt stmt : b.stmts){

			if(stmt instanceof Return) {
				((Return) stmt).accept(this);
			}else {
				stmt.accept(this);
			}

		}
			return null;
	}



	@Override
	public Type visitFunDecl(FunDecl p) {
		// To be completed...\\


		for(VarDecl varDecl : p.params){
			varDecl.accept(this);
		}
		p.fun_type.accept(this);

		funDeclReturnType.push(p.fun_type);
		FunDecl_Return_Mapping.push(p);
		p.block.accept(this);

		funDeclReturnType.pop();
		FunDecl_Return_Mapping.pop();

		//思考一波？ emm 没啥毛病！
		// Statement does not have type. But I'd like to keep it to see it is useful or not later.
		return p.fun_type;
	}

	@Override
	public Type visitProgram(Program p) {
		// To be completed...
		for(StructTypeDecl structTypeDecl : p.structTypeDecls){
			structTypeDecl.accept(this);
		}

		for(VarDecl varDecl : p.varDecls){

			varDecl.accept(this);
		}

		//意义不明？
		// Seems useless!! But I wanna waste some memory!
		List<VarDecl> params_print_s = new LinkedList<>();
		params_print_s.add(new VarDecl(new PointerType(BaseType.CHAR),"s"));
		FunDecl print_s = new FunDecl(BaseType.VOID,"print_s",params_print_s,new Block(new LinkedList<>(),new LinkedList<>()));

		List<VarDecl> params_print_i = new LinkedList<>();
		params_print_i.add(new VarDecl(BaseType.INT,"i"));
		FunDecl print_i = new FunDecl(BaseType.VOID,"print_i",params_print_i,new Block(new LinkedList<>(),new LinkedList<>()));

		List<VarDecl> params_print_c = new LinkedList<>();
		params_print_c.add(new VarDecl(BaseType.CHAR,"c"));
		FunDecl print_c = new FunDecl(BaseType.VOID,"print_c",params_print_c,new Block(new LinkedList<>(),new LinkedList<>()));

		List<VarDecl> params_read_c = new LinkedList<>();
		FunDecl read_c = new FunDecl(BaseType.CHAR,"read_c",params_read_c,new Block(new LinkedList<>(),new LinkedList<>()));

		List<VarDecl> params_read_i = new LinkedList<>();
		FunDecl read_i = new FunDecl(BaseType.INT,"read_i",params_read_i,new Block(new LinkedList<>(),new LinkedList<>()));

		List<VarDecl> params_mcmalloc = new LinkedList<>();
		params_mcmalloc.add(new VarDecl(BaseType.INT,"size"));
		FunDecl mcmalloc = new FunDecl(new PointerType(BaseType.VOID),"mcmalloc",params_mcmalloc,new Block(new LinkedList<>(),new LinkedList<>()));

		p.funDecls.add(print_s);
		p.funDecls.add(print_i);
		p.funDecls.add(print_c);
		p.funDecls.add(read_c);
		p.funDecls.add(read_i);
		p.funDecls.add(mcmalloc);

		for (FunDecl funDecl : p.funDecls){
			funDecl.accept(this);
		}

		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
		// To be completed...
		if(vd.var_type == BaseType.VOID){
			error("Void var " + vd.varName + " can't be declared.");
			return null;
		}else if(vd.var_type instanceof PointerType){
			// To check if the typt pointer points to is valid.
			Type pointerType = ((PointerType) vd.var_type).point_to_type.accept(this);

			//if(pointerType == BaseType.VOID){
			//	error("Void pointer " + vd.varName + " can't be declared.");
			//	return null;
			//}
//			if(pointerType instanceof StructType){
//				((StructType) pointerType).accept(this);
//			}

		} else if(vd.var_type instanceof StructType){
			String struct_name = ((StructType) vd.var_type).struct_Name;
			if(!structMap.keySet().contains(struct_name)){
				error("Undeclared struct name " + struct_name + "!");
				return null;
			}
		} else if (vd.var_type instanceof ArrayType){
			Type arr_type = ((ArrayType) vd.var_type).elem_type.accept(this);
		}

		return vd.var_type;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		// To be completed...

		if(v.vd == null){
			error("Undeclared var " + v.name + "!");
			return null;
		}

		v.vd.var_type.accept(this);

		if(v.vd == null){
			error("Undeclared var " + v.name + "!");
			return null;
		}

		v.type = v.vd.accept(this);

		return v.type;
	}

	public Type visitPointerType(PointerType pointerType){
		pointerType.point_to_type.accept(this);
		return pointerType;
	}

	public Type visitStructType(StructType v){
		if(!structMap.keySet().contains(v.struct_Name)){
			error("Undeclared Struct Type " + v.struct_Name + "!");
			return null;
		}
		return v;
	}

	public Type visitArrayType(ArrayType v){
		v.elem_type.accept(this);
		return v;
	}

	public Type visitIntLiteral(IntLiteral v){
		return BaseType.INT;
	}

	public Type visitStrLiteral(StrLiteral v){
		return new ArrayType(BaseType.CHAR,v.value.length() + 1);
	}

	public Type visitChrLiteral(ChrLiteral v){
		return BaseType.CHAR;
	}

	public Type visitFunCallExpr(FunCallExpr v){

		FunDecl funDecl = v.funDecl;

		if(funDecl == null){
			error("Name Analysis Undeclared Function " + v.fun_name + "!");
			return null;
		}

		if(v.params.size() != funDecl.params.size()) {
			error("Argument size does not match!" + v.fun_name + "!");
			return null;
		}

		for(int con = 0; con < v.params.size(); con++) {
			Type arg_call  = v.params.get(con).accept(this);

			Type arg_decl = funDecl.params.get(con).accept(this);

			if(arg_call instanceof StructType){
				//System.out.println(":)");
				arg_call.accept(this);
			}

			//有潜在问题。
			if(!checkType(arg_call,arg_decl)) {
				//System.out.println(arg_call);
				//System.out.println(arg_decl);

				error("Function call arg elem_type does not match " + v.fun_name + "!");
				return null;
			}

		}

		v.type = funDecl.fun_type;
		//System.out.println(" : ) " + v.elem_type);
		return v.type;

	}

	private boolean checkType(Type type1, Type type2){

		if (type1 instanceof BaseType && type2 instanceof BaseType){
			return type1 == type2;
		}

		if (type1 instanceof ArrayType && type2 instanceof ArrayType){
			if (((ArrayType) type1).size != ((ArrayType) type2).size){
				// 有问题吗？
				return false;
			}
			Type t1 = ((ArrayType) type1).elem_type.accept(this);
			Type t2 = ((ArrayType) type2).elem_type.accept(this);
			return checkType(t1,t2);
		}

		if(type1 instanceof PointerType && type2 instanceof  PointerType){
			Type t1 = ((PointerType) type1).point_to_type.accept(this);
			Type t2 = ((PointerType) type2).point_to_type.accept(this);
			return checkType(t1,t2);
		}

		if(type1 instanceof StructType && type2 instanceof StructType){
			Type t1 = ((StructType) type1).accept(this);
			Type t2 = ((StructType) type2).accept(this);
			return ((StructType) type1).struct_Name.equals(((StructType) type2).struct_Name);
		}
		return false;
	}

	public Type visitBinOp(BinOp v){
		Type lhs = v.first.accept(this);
		Type rhs = v.second.accept(this);

		if(v.operator == Op.NE || v.operator == Op.EQ){
			if(lhs instanceof ArrayType || lhs instanceof StructType || lhs == BaseType.VOID){
				error("Type invalid for BinOp" );
				return null;
			}
			if(rhs instanceof ArrayType || rhs instanceof StructType || rhs == BaseType.VOID){
				error("Type invalid for BinOp" );
				return null;
			}

			if(checkType(lhs,rhs)){
				v.type = BaseType.INT;
				return BaseType.INT;
			}else{
				error("Type doesn't match!");
				return null;
			}
		}else {

			if (lhs == BaseType.INT && rhs == BaseType.INT) {
				v.type = BaseType.INT;
				return v.type;
			} else {
				error("Invalid types for arithmetic operation!");
				return null;
			}

		}
	}

	public Type visitOp(Op v){
		return null;
	}

	public Type visitArrayAccessExpr(ArrayAccessExpr v){
		Type type = v.array.accept(this);
		if(!(type instanceof ArrayType || type instanceof PointerType)){
			error("Invalid array access!");
			return null;
		}

		Type resultType;
		if(type instanceof ArrayType){
			resultType = ((ArrayType) type).elem_type.accept(this);
		} else {
			resultType = ((PointerType) type).point_to_type.accept(this);
		}

		Type idx_type = v.index.accept(this);
		if(idx_type != BaseType.INT){
			error("Invalid Type Indexing!");
			return null;
		}
		v.type = resultType;
		return resultType;

	}


	public Type visitFieldAccessExpr(FieldAccessExpr v){
		Type structType = v.struct.accept(this);
		if(!(structType instanceof StructType)){
			error("Invalid Struct Type Access!");
			return null;
		}

		//v.structType = (StructType) structType;

		String structName = ((StructType) structType).struct_Name;
		v.struct_name = structName;
		if(!structMap.keySet().contains(structName)){
			error("Undeclared Struct Type!");
		}
		StructTypeDecl thisStructDecl = structMap.get(structName);
		//v.structTypeDecl = thisStructDecl;
		boolean isFiled = false;
		Type resultType = null;

		for(VarDecl varDecl : thisStructDecl.varDecls){
			if (varDecl.varName.equals(v.field)){
				isFiled = true;
				resultType = varDecl.var_type;
			}
		}

		if(!isFiled){
			error("Field does not exist!");
			return null;
		}

		v.type = resultType;
		return resultType;



	}

	public Type visitValueAtExpr(ValueAtExpr v){
		Type type = v.expr.accept(this);
		if(!(type instanceof PointerType)){
			error("Invalid Pointer Access!");
			return null;
		}

		Type pointTo = ((PointerType) type).point_to_type.accept(this);

		v.type = pointTo;

		return v.type;
	}


	public Type visitSizeOfExpr(SizeOfExpr v){
		v.size_of_type.accept(this);
		v.type = BaseType.INT;
		return v.type;
	}


	public Type visitTypecastExpr(TypecastExpr v){
		Type castTo = v.cast_type;
		Type expr_Type = v.expr.accept(this);
		Type resultType = null;
		if(castTo == BaseType.INT && expr_Type == BaseType.CHAR){
			resultType = BaseType.INT;
			v.type = resultType;
			return resultType;
		}

		if(castTo instanceof PointerType && expr_Type instanceof ArrayType){
			Type array_type = ((ArrayType) expr_Type).elem_type;
			Type pointerTo = ((PointerType) castTo).point_to_type;

			if(!checkType(array_type,pointerTo)){
				error("Invalid casting!");
				return null;
			}

			resultType = new PointerType(pointerTo);
			v.type = resultType;
			return resultType;
		}

		if(castTo instanceof PointerType && expr_Type instanceof PointerType){
			Type pointerTo = ((PointerType) castTo).point_to_type;
			resultType = new PointerType(pointerTo);
			v.type = resultType;
			return resultType;
		}
		error("Invalid Casting!");
		return null;
	}

	public Type visitExprStmt(ExprStmt v){
		Type resultType = v.expr.accept(this);
		return null;
	}

	public Type visitWhile(While v){
		Type cond = v.expr.accept(this);
		if(cond != BaseType.INT){
			error("Invalid While Condition!");
			return null;
		}

		v.stmt.accept(this);
		return null;

	}

	public Type visitIf(If v){
		Type cond = v.expr.accept(this);
		if(cond != BaseType.INT){
			error("Invalid If Condition!");
			return null;
		}
		v.stmt1.accept(this);
		if(v.stmt2 != null) {
			v.stmt2.accept(this);
		}
		return null;
	}

	public Type visitAssign(Assign v){
		//VarExpr, FieldAccessExpr, ArrayAccessExpr or ValuteAtExpr.

		Expr lhs = v.lhs;
		if(!(lhs instanceof  VarExpr || lhs instanceof FieldAccessExpr || lhs instanceof  ArrayAccessExpr || lhs instanceof  ValueAtExpr)){
			error("Assign left hand side is not assignable!");
			return null;
		}
		Type lhs_type = v.lhs.accept(this);

		if(lhs_type == BaseType.VOID || lhs_type instanceof ArrayType){
			error("Assign left hand side is not assignable!");
			return null;
		}

		Type rhs_type = v.rhs.accept(this);

		if(rhs_type == BaseType.VOID || rhs_type instanceof ArrayType){
			error("Assign right hand side is not assignable!");
			return null;
		}

		if (!checkType(lhs_type,rhs_type)){
			//System.out.println(v.lhs.elem_type);
			//System.out.println(v.rhs);

			error("Assign left hand side and right hand side types do not match!");
			return null;
		}
		v.assignType = lhs_type;
		return null;
	}

	public Type visitReturn(Return v){

		Type funcType = null;
		FunDecl funDecl = null;
		if (!funDeclReturnType.empty()){
			funcType = funDeclReturnType.peek();
		}

		if (!FunDecl_Return_Mapping.empty()){
			funDecl = FunDecl_Return_Mapping.peek();
		}

		if(v.expr == null){
			if(funcType == BaseType.VOID){
				v.funDecl = funDecl;
				return null;
			}else {
				error("Return does not match!");
				return null;
			}
		}

		Type return_type = v.expr.accept(this);


		if(checkType(return_type,funcType)){
			v.returnType = return_type;
			v.funDecl = funDecl;
			return return_type;
		}

		error("Return Type does not match!");
		v.funDecl = funDecl;
		return null;

	}


	// To be completed...


}



