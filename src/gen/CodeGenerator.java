package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.EmptyStackException;
import java.util.Stack;

public class CodeGenerator implements ASTVisitor<Register> {

    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public CodeGenerator() {
        freeRegs.addAll(Register.tmpRegs);
    }

    private class RegisterAllocationError extends Error {}

    private Register getRegister() {
        try {
            return freeRegs.pop();
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    private void freeRegister(Register reg) {
        freeRegs.push(reg);
    }





    private PrintWriter writer; // use this writer to output the assembly instructions


    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);

        visitProgram(program);
        writer.close();
    }

    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {
        return null;
    }

    @Override
    public Register visitBlock(Block b) {
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl p) {
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitProgram(Program p) {
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitPointerType(PointerType v) {
        return null;
    }

    @Override
    public Register visitStructType(StructType v) {
        return null;
    }

    @Override
    public Register visitArrayType(ArrayType v) {
        return null;
    }

    @Override
    public Register visitIntLiteral(IntLiteral v) {
        return null;
    }

    @Override
    public Register visitStrLiteral(StrLiteral v) {
        return null;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral v) {
        return null;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr v) {
        return null;
    }

    @Override
    public Register visitBinOp(BinOp v) {
        return null;
    }

    @Override
    public Register visitOp(Op v) {
        return null;
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr v) {
        return null;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr v) {
        return null;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr v) {
        return null;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr v) {
        return null;
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr v) {
        return null;
    }

    @Override
    public Register visitExprStmt(ExprStmt v) {
        return null;
    }

    @Override
    public Register visitWhile(While v) {
        return null;
    }

    @Override
    public Register visitIf(If v) {
        return null;
    }

    @Override
    public Register visitAssign(Assign v) {
        return null;
    }

    @Override
    public Register visitReturn(Return v) {
        return null;
    }
}
