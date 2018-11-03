package ast;

import gen.Register;

public class VarDecl implements ASTNode {
    public final Type var_type;
    public final String varName;
    public int stack_offset;
    public boolean isStatic;
    public int memo_size;
    public Register atRegister = null;

    public VarDecl(Type var_type, String varName) {
	    this.var_type = var_type;
	    this.varName = varName;
	    this.atRegister = null;
    }

     public <T> T accept(ASTVisitor<T> v) {
	return v.visitVarDecl(this);
    }
}
