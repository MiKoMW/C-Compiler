package ast;

public class SizeOfExpr extends Expr{

    public final Type size_of_type;

    public SizeOfExpr(Type size_of_type){
        this.size_of_type = size_of_type;
    }

    public <T> T accept(ASTVisitor<T> v){
        return v.visitSizeOfExpr(this);
    }

}
