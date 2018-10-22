package sem;

import ast.StructType;

public class StructSymbol extends Symbol{

    public Scope scope;

    public StructType structType;

    public StructSymbol(Scope scope,StructType structType){
        super(structType.struct_Name);
        this.structType = structType;
        this.scope = new Scope(scope);
    }

}
