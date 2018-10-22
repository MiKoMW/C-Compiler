package ast;

public class ChrLiteral extends Expr{

    public final char value;

    public ChrLiteral(char val){
        this.value = val;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }

}
