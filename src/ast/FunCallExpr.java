package ast;

import java.util.List;

public class FunCallExpr extends Expr{

    public final String fun_name;

    public final List<Expr> params;

    public FunDecl funDecl;

    public int stack_offset;

    public FunCallExpr(String name, List<Expr> params){
        this.fun_name = name;
        this.params = params;
        this.funDecl = null;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFunCallExpr(this);
    }
}