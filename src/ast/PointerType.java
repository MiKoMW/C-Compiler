package ast;

public class PointerType implements Type{

    public final Type point_to_type;

    public PointerType(Type type){
        this.point_to_type = type;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitPointerType(this);
    }

}
