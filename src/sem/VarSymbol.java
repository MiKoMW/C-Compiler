package sem;

import ast.VarDecl;

public class VarSymbol extends Symbol{

    public VarDecl varDecl;

    public VarSymbol(VarDecl varDecl){
        super(varDecl.varName);
        this.varDecl = varDecl;
    }

}

