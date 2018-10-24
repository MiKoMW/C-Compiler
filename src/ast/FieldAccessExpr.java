package ast;

public class FieldAccessExpr extends Expr{

    public final Expr struct;
    public final String field;
    //public StructTypeDecl structTypeDecl;

    public FieldAccessExpr(Expr str, String fld){
        this.struct = str;
        this.field = fld;
    }

    public <T> T accept(ASTVisitor<T> v){
        return v.visitFieldAccessExpr(this);
    }

}