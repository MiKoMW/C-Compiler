package ast;

import java.util.List;

public class FunDecl implements ASTNode {
    public final Type fun_type;
    public final String name;
    public final List<VarDecl> params;
    public final Block block;
    public int param_size;
    public int return_Size;

    public FunDecl(Type fun_type, String name, List<VarDecl> params, Block block) {
	    this.fun_type = fun_type;
	    this.name = name;
	    this.params = params;
	    this.block = block;
    }

    public <T> T accept(ASTVisitor<T> v) {
	return v.visitFunDecl(this);
    }
}
