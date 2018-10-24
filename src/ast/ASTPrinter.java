package ast;

import java.io.PrintWriter;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    @Override
    public Void visitBlock(Block b) {
        writer.print("Block(");

        String delimiter = "";

        for(VarDecl vd : b.vardelcs){
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for(Stmt s: b.stmts){
            writer.print(delimiter);
            delimiter = ",";
            s.accept(this);
        }

        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.fun_type.accept(this);
        writer.print(","+fd.name+",");
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(",");
        }
        fd.block.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");
        String delimiter = "";
        for (StructTypeDecl std : p.structTypeDecls) {
            writer.print(delimiter);
            delimiter = ",";
            std.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (FunDecl fd : p.funDecls) {
            writer.print(delimiter);
            delimiter = ",";
            fd.accept(this);
        }
        writer.print(")");
	    writer.flush();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd){
        writer.print("VarDecl(");
        vd.var_type.accept(this);
        writer.print(","+vd.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        writer.print(bt);
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {

        writer.print("StructTypeDecl(");
        st.struct_type.accept(this);
        for(VarDecl vd : st.varDecls){
            writer.print(",");
            vd.accept(this);
        }
        writer.print(")");

        return null;
    }

    public Void visitPointerType(PointerType pt){

        writer.print("PointerType(");
        pt.point_to_type.accept(this);
        writer.print(")");

        return null;
    }

    public Void visitStructType(StructType st){

        writer.print("StructType(");
        writer.print(st.struct_Name);
        writer.print(")");

        return null;
    }

    public Void visitArrayType(ArrayType at){

        writer.print("ArrayType(");
        at.elem_type.accept(this);
        writer.print(",");
        writer.print(at.size);
        writer.print(")");
        return null;

    }

    public Void visitIntLiteral(IntLiteral intLiteral){

        writer.print("IntLiteral(");
        writer.print(intLiteral.value);
        writer.print(")");
        return null;

    }

    public Void visitStrLiteral(StrLiteral strLiteral){

        writer.print("StrLiteral(");
        writer.print(strLiteral.value);
        writer.print(")");
        return null;

    }

    public Void visitChrLiteral(ChrLiteral chrLiteral){

        writer.print("ChrLiteral(");
        writer.print(chrLiteral.value);
        writer.print(")");
        return null;

    }

    public Void visitFunCallExpr(FunCallExpr funCallExpr){

        writer.print("FunCallExpr(");
        writer.print(funCallExpr.fun_name);
        for(Expr expr : funCallExpr.params){
            writer.print(",");
            expr.accept(this);
        }
        writer.print(")");
        return null;

    }

    //BinOp(IntLiteral(3), MUL, VarExpr(x))
    public Void visitBinOp(BinOp binOp){

        writer.print("BinOp(");
        binOp.first.accept(this);
        writer.print(",");
        binOp.operator.accept(this);
        writer.print(",");
        binOp.second.accept(this);
        writer.print(")");
        return null;
    }

    public Void visitOp(Op op){

        writer.print(op);
        return null;

    }

    public Void visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr){

        writer.print("ArrayAccessExpr(");
        arrayAccessExpr.array.accept(this);
        writer.print(",");
        arrayAccessExpr.index.accept(this);
        writer.print(")");
        return null;

    }

    public Void visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr){

        writer.print("FieldAccessExpr(");
        fieldAccessExpr.struct.accept(this);
        writer.print(",");
        writer.print(fieldAccessExpr.field);
        writer.print(")");
        return null;

    }

    public Void visitValueAtExpr(ValueAtExpr valueAtExpr){

        writer.print("ValueAtExpr(");
        valueAtExpr.expr.accept(this);
        writer.print(")");
        return null;

    }

    public Void visitSizeOfExpr(SizeOfExpr sizeOfExpr){

        writer.print("SizeOfExpr(");
        sizeOfExpr.size_of_type.accept(this);
        writer.print(")");
        return null;

    }

    public Void visitTypecastExpr(TypecastExpr typecastExpr){

        writer.print("TypecastExpr(");
        typecastExpr.cast_type.accept(this);
        writer.print(",");
        typecastExpr.expr.accept(this);
        writer.print(")");
        return null;
    }

    public Void visitExprStmt(ExprStmt exprStmt){

        writer.print("ExprStmt(");
        exprStmt.expr.accept(this);
        writer.print(")");
        return null;

    }

    public Void visitWhile(While w){

        writer.print("While(");
        w.expr.accept(this);
        writer.print(",");
        w.stmt.accept(this);
        writer.print(")");
        return null;

    }

    public Void visitIf(If i){

        writer.print("If(");
        i.expr.accept(this);
        writer.print(",");
        i.stmt1.accept(this);
        if(i.stmt2 != null){
            writer.print(",");
            i.stmt2.accept(this);
        }
        writer.print(")");
        return null;

    }

    public Void visitAssign(Assign assign){

        writer.print("Assign(");
        assign.lhs.accept(this);
        writer.print(",");
        assign.rhs.accept(this);
        writer.print(")");
        return null;

    }

    public Void visitReturn(Return r){

        writer.print("Return(");
        if (r.expr != null){
            r.expr.accept(this);
        }
        writer.print(")");
        return null;
    }


    // to complete ...
    
}
