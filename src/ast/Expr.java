package ast;

public abstract class Expr implements ASTNode {

    public Type type; // to be filled in by the size_of_type analyser
    public abstract <T> T accept(ASTVisitor<T> v);
}
