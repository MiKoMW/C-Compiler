package ast;

public class IntLiteral extends Expr{

    public final int value;

    public IntLiteral(int val){
        this.value = val;
    }

    public <T> T accept(ASTVisitor<T> v){
        return v.visitIntLiteral(this);
    }


}
