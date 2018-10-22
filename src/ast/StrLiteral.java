package ast;

public class StrLiteral extends Expr{

    public final String value;

    public StrLiteral(String str){
        this.value = str;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStrLiteral( this);
    }

}
