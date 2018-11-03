package ast;

public class ArrayType implements Type{

    public final Type elem_type;
    public final int size;

    public ArrayType(Type elem_type, int size){
        this.elem_type = elem_type;
        this.size = size;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayType(this);
    }

}