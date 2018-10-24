package ast;

public class BinOp extends Expr {

    public final Expr first;
    public final Expr second;
    public final Op operator;

    public BinOp(Expr fst, Op op, Expr snd){
        this.first = fst;
        this.operator = op;
        this.second = snd;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBinOp(this);
    }


}