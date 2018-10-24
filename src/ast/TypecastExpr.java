package ast;

public class TypecastExpr extends Expr{

    public final Type cast_type;
    public final Expr expr;

    public TypecastExpr(Type cast_type, Expr expr){
        this.cast_type = cast_type;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> v){
        return v.visitTypecastExpr(this);
    }
}