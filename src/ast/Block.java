package ast;

import java.util.List;

public class Block extends Stmt {

    public final List<VarDecl> vardelcs;
    public final List<Stmt> stmts;

    public Block(List<VarDecl> vardelcs, List<Stmt> stmts){
        this.vardelcs = vardelcs;
        this.stmts = stmts;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitBlock(this);
    }
}
