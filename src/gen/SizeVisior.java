package gen;

import ast.*;

public class SizeVisior implements ASTVisitor<Integer> {
    @Override
    public Integer visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Integer visitStructTypeDecl(StructTypeDecl st) {
        return null;
    }

    @Override
    public Integer visitBlock(Block b) {
        return null;
    }

    @Override
    public Integer visitFunDecl(FunDecl p) {
        return null;
    }

    @Override
    public Integer visitProgram(Program p) {
        return null;
    }

    @Override
    public Integer visitVarDecl(VarDecl vd) {
        return null;
}

    @Override
    public Integer visitVarExpr(VarExpr v) {
        return null;
    }

    @Override
    public Integer visitPointerType(PointerType v) {
        return null;
    }

    @Override
    public Integer visitStructType(StructType v) {
        return null;
    }

    @Override
    public Integer visitArrayType(ArrayType v) {
        return null;
    }

    @Override
    public Integer visitIntLiteral(IntLiteral v) {
        return null;
    }

    @Override
    public Integer visitStrLiteral(StrLiteral v) {
        return null;
    }

    @Override
    public Integer visitChrLiteral(ChrLiteral v) {
        return null;
    }

    @Override
    public Integer visitFunCallExpr(FunCallExpr v) {
        return null;
    }

    @Override
    public Integer visitBinOp(BinOp v) {
        return null;
    }

    @Override
    public Integer visitOp(Op v) {
        return null;
    }

    @Override
    public Integer visitArrayAccessExpr(ArrayAccessExpr v) {
        return null;
    }

    @Override
    public Integer visitFieldAccessExpr(FieldAccessExpr v) {
        return null;
    }

    @Override
    public Integer visitValueAtExpr(ValueAtExpr v) {
        return null;
    }

    @Override
    public Integer visitSizeOfExpr(SizeOfExpr v) {
        return null;
    }

    @Override
    public Integer visitTypecastExpr(TypecastExpr v) {
        return null;
    }

    @Override
    public Integer visitExprStmt(ExprStmt v) {
        return null;
    }

    @Override
    public Integer visitWhile(While v) {
        return null;
    }

    @Override
    public Integer visitIf(If v) {
        return null;
    }

    @Override
    public Integer visitAssign(Assign v) {
        return null;
    }

    @Override
    public Integer visitReturn(Return v) {
        return null;
    }
}
