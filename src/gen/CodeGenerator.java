package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
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
        init();
        visitProgram(program);
        finale();

        for(String st : output){
            writer.println(st);
        }

        writer.close();
    }

    // Code starts Here!!!!!

    boolean globalLevel;

    ArrayList<String> output = new ArrayList<>();
    ArrayList<String> mainFun = new ArrayList<>();
    ArrayList<String> funOut = new ArrayList<>();
    ArrayList<String> stastic_data = new ArrayList<>();
    ArrayList<String> currentList;

    //HashMap<String,Integer> typeSize = new HashMap<>();

    private void init(){

        stastic_data.add(".data");

        funOut.add(".text");
        funOut.add("j main");

        mainFun.add(".globl main");
        mainFun.add("main:");

        ///memoryInfo.put("INT",);
        //sizeof(char)==1, sizeof(int)==4, sizeof(int*)==4

        current_Stack_offset = 0;
    }

    private void finale(){

        output.addAll(stastic_data);
        output.addAll(funOut);
        output.addAll(mainFun);

        // 这个有问题！！！！！
        output.add("li " + Register.v0.toString() + ", 10");
        output.add("syscall");

    }


    private HashMap<String,StructInfo> strcutInfos = new HashMap<>();

    boolean isMain;

    int current_Stack_offset;


    // Utility Function above.
    // =============================================================================================


    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {

        //strcutInfo.put(st.struct_type.struct_Name,new StructInfo(st,strcutInfo));

        String type = st.struct_type.struct_Name;
        HashMap<String,Integer> innerVars = new HashMap<>();

        int con = 0;

        for(VarDecl varDecl : st.varDecls){

            innerVars.put(varDecl.varName,con);

            if(varDecl.var_type instanceof StructType){
                StructInfo structInfo = strcutInfos.get(((StructType) varDecl.var_type).struct_Name);
                if (structInfo == null){
                    System.err.println("Something must be wrong!!");
                    return null;
                }
                con += structInfo.size;
            } else if(varDecl.var_type == BaseType.CHAR){
                con+=1;
            } else if(varDecl.var_type instanceof ArrayType){
                if(((ArrayType)varDecl.var_type).elem_type == BaseType.CHAR){
                    con += ((ArrayType)varDecl.var_type).size;
                }else if(((ArrayType)varDecl.var_type).elem_type instanceof StructType){

                    StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) varDecl.var_type).elem_type).struct_Name);

                    if (structInfo == null){
                        System.err.println("Something must be wrong!!");
                        return null;
                    }
                    con += structInfo.size * ((ArrayType)varDecl.var_type).size;
                }else {
                    con += ((ArrayType)varDecl.var_type).size * 4;
                }
            }else{
                con += 4;
            }
        }

        strcutInfos.put(type,new StructInfo(type,con,innerVars));

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
        globalLevel = true;
        isMain = false;

        // ***************************************************************************************** We need string buffer to store the format output.
        // We need st
        for(StructTypeDecl structTypeDecl : p.structTypeDecls){
            structTypeDecl.accept(this);
        }

        for(VarDecl varDecl : p.varDecls){
            varDecl.accept(this);
        }

        globalLevel = false;

        for(FunDecl funDecl : p.funDecls){
            funDecl.accept(this);
        }

        return null;

    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        if(globalLevel) {
            if (vd.var_type instanceof StructType) {

                String structName = ((StructType) vd.var_type).struct_Name;
                stastic_data.add(vd.varName + ": .space " + strcutInfos.get(structName).size);

            } else if (vd.var_type instanceof ArrayType) {

                int size = ((ArrayType) vd.var_type).size;

                if (((ArrayType) vd.var_type).elem_type == BaseType.CHAR) {
                    stastic_data.add(vd.varName + ": .space " + size);
                } else if (((ArrayType) vd.var_type).elem_type instanceof StructType) {

                    StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) vd.var_type).elem_type).struct_Name);

                    if (structInfo == null) {
                        System.err.println("Something must be wrong!!");
                        return null;
                    }
                    stastic_data.add(vd.varName + ": .space " + structInfo.size * size);

                } else {
                    stastic_data.add(vd.varName + ": .space " + size * 4);
                }
            } else if (vd.var_type == BaseType.CHAR) {
                stastic_data.add(vd.varName + ": .space " + 1);
            } else {
                stastic_data.add(vd.varName + ": .space " + 4);

            }

        }else{

            // addi $sp , $sp, -4

            if(isMain){
                currentList = mainFun;
            }else{
                currentList = funOut;
            }

            if (vd.var_type instanceof StructType) {
                String structName = ((StructType) vd.var_type).struct_Name;
                currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " + strcutInfos.get(structName).size);
                current_Stack_offset -= strcutInfos.get(structName).size;

            } else if (vd.var_type instanceof ArrayType) {

                int size = ((ArrayType) vd.var_type).size;

                if (((ArrayType) vd.var_type).elem_type == BaseType.CHAR) {
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " + size);
                    current_Stack_offset -= size;
                } else if (((ArrayType) vd.var_type).elem_type instanceof StructType) {

                    StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) vd.var_type).elem_type).struct_Name);

                    if (structInfo == null) {
                        System.err.println("Something must be wrong!!");
                        return null;
                    }
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " +  structInfo.size * size);
                    current_Stack_offset -= structInfo.size * size;

                } else {
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " +  size * 4);
                    current_Stack_offset -= size * 4;

                }
            } else if (vd.var_type == BaseType.CHAR) {
                currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " +  1);
                current_Stack_offset -= 1;
            } else {
                currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " +  4);
                current_Stack_offset -= 4;
            }

        }

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
