package ast;

public class ArrayAccessExpr extends Expr{
    public final Expr array;
    public final Expr index;
    //public VarSymbol varSymbol;

    public ArrayAccessExpr(Expr arr, Expr idx){
        this.array = arr;
        this.index = idx;
        //this.varSymbol = null;
    }

    public <T> T accept(ASTVisitor<T> v){
        return v.visitArrayAccessExpr(this);
    }

}
