package ast;

import java.util.List;

public class StructTypeDecl implements ASTNode {

    public final StructType struct_type;
    public final List<VarDecl> varDecls;

    public StructTypeDecl(StructType str_type, List<VarDecl> var_Dec){
        this.struct_type = str_type;
        this.varDecls = var_Dec;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructTypeDecl(this);
    }

}
