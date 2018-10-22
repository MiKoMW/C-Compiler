package ast;

public class StructType implements Type{

    public final String struct_Name;

    public StructType(String name){
        this.struct_Name = name;
    }

    public <T> T accept(ASTVisitor<T> v){
        return v.visitStructType(this);
    }

}

