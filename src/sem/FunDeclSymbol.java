package sem;

import ast.FunDecl;

public class FunDeclSymbol extends Symbol{

    public Scope scope;

    public FunDecl funDecl;

    public FunDeclSymbol(Scope outer, FunDecl funDecl){
        super(funDecl.name);
        this.scope = new Scope(outer);
        this.funDecl = funDecl;
    }

}
