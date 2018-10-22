package ast;

public interface ASTVisitor<T> {
    public T visitBaseType(BaseType bt);
    public T visitStructTypeDecl(StructTypeDecl st);
    public T visitBlock(Block b);
    public T visitFunDecl(FunDecl p);
    public T visitProgram(Program p);
    public T visitVarDecl(VarDecl vd);
    public T visitVarExpr(VarExpr v);

    public T visitPointerType(PointerType v);
    public T visitStructType(StructType v);
    public T visitArrayType(ArrayType v);
    public T visitIntLiteral(IntLiteral v);
    public T visitStrLiteral(StrLiteral v);
    public T visitChrLiteral(ChrLiteral v);
    public T visitFunCallExpr(FunCallExpr v);
    public T visitBinOp(BinOp v);
    public T visitOp(Op v);
    public T visitArrayAccessExpr(ArrayAccessExpr v);
    public T visitFieldAccessExpr(FieldAccessExpr v);
    public T visitValueAtExpr(ValueAtExpr v);
    public T visitSizeOfExpr(SizeOfExpr v);
    public T visitTypecastExpr(TypecastExpr v);
    public T visitExprStmt(ExprStmt v);
    public T visitWhile(While v);
    public T visitIf(If v);
    public T visitAssign(Assign v);
    public T visitReturn(Return v);

    // to complete ... (should have one visit method for each concrete AST node class)
}
