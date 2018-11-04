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
        init(program);
        visitProgram(program);
        finale();

        for(String st : output){
            writer.println(st);
        }

        writer.close();
    }

    // =============================================================================================
    // Utility Function below.


    boolean globalLevel;

    // Buffer the output.
    ArrayList<String> output = new ArrayList<>();

    ArrayList<String> mainFun = new ArrayList<>();
    ArrayList<String> funOut = new ArrayList<>();
    ArrayList<String> stastic_data = new ArrayList<>();
    ArrayList<String> currentList;

    int lab_con;

    private HashMap<String,StructInfo> strcutInfos = new HashMap<>();
    private HashMap<String,String> str_label = new HashMap<>();
    private HashMap<String,String> lib_fun = new HashMap<>();

    private boolean isMain;

    private int current_Stack_offset;


    private void init(Program program){

        stastic_data.add(".data");
        stastic_data.add(".align 2");

        funOut.add(".text");
        funOut.add("j main");

        mainFun.add(".globl main");
        //mainFun.add("main:");

        current_Stack_offset = 0;
        OffSetVisitor offSetVisitor = new OffSetVisitor();
        offSetVisitor.visitProgram(program);
        this.strcutInfos = offSetVisitor.getStrcutInfos();
        lab_con = 0;

        lib_fun.put("print_s","li $v0, 4\nsyscall");
        lib_fun.put("print_i","li $v0, 1\nsyscall");
        lib_fun.put("print_c","li $v0, 11\nsyscall");
        lib_fun.put("read_c","li $v0, 12\nsyscall");
        lib_fun.put("read_i","li $v0, 5\nsyscall");

        //这我也不知道对不对了!!!!!!!!
        lib_fun.put("mcmalloc","li $v0, 9\nsyscall\n");

    }

    private void finale(){

        output.addAll(stastic_data);
        output.addAll(funOut);
        //System.out.println(mainFun.size());indfsa
        if (mainFun.size() < 2){
            mainFun.add("main:");
        }
        output.addAll(mainFun);


        // 这个有问题！！！！！
        output.add("li " + Register.v0.toString() + ", 10");
        output.add("syscall");
    }

    private String newLable(){
        return "Lable"+(lab_con++)+":";
    }


    //==================================================================================================================== 这个思考下。
    //private int str_lab;

    private Stack<Stack<Register>> registerHistory = new Stack<>();

    private void saveAllRegister(){

        Stack<Register> freeRegs_temp = new Stack<Register>();
        freeRegs_temp.addAll(freeRegs);
        registerHistory.push(freeRegs_temp);

        // saving all temp register/
        for (Register register : Register.tmpRegs) {
            currentList.add("sw " + register.toString() + ", " + "0($sp)");
            currentList.add("addi " + "$sp, $sp, -4");
        }

        for(Register register : Register.paramRegs){
            currentList.add("sw " + register.toString() + ", " + "0($sp)");
            currentList.add("addi " + "$sp, $sp, -4");
        }

        Register register = Register.v0;
        currentList.add("sw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

        register = Register.gp;
        currentList.add("sw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

        register = Register.ra;
        currentList.add("sw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

        register = Register.sp;
        currentList.add("sw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

        register = Register.fp;
        currentList.add("sw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

        currentList.add("move $fp, $sp");

    }

    private void loadAllRegisters(){
        freeRegs = registerHistory.pop();

        currentList.add("move $sp, $fp");
        int tempIdx = -27 * 4;

        // saving all temp register/
        for (Register register : Register.tmpRegs) {
            currentList.add("lw " + register.toString() + ", " + tempIdx + "($sp)");
            tempIdx += 4;
        }

        for(Register register : Register.paramRegs){
            currentList.add("lw " + register.toString() + ", " + tempIdx + "($sp)");
            tempIdx += 4;
        }

        Register register = Register.v0;
        currentList.add("lw " + register.toString() + ", " + tempIdx + "0($sp)");
        tempIdx += 4;

        register = Register.gp;
        currentList.add("lw " + register.toString() + ", " +tempIdx+ "0($sp)");
        tempIdx += 4;


        register = Register.ra;
        currentList.add("lw " + register.toString() + ", " +tempIdx+ "0($sp)");
        tempIdx += 4;

        register = Register.sp;
        currentList.add("sw " + register.toString() + ", " +tempIdx+ "0($sp)");
        tempIdx += 4;

        register = Register.fp;
        currentList.add("lw " + register.toString() + ", " +tempIdx+ "0($sp)");
        tempIdx += 4;
        if(tempIdx!=0){
            System.err.println("好像有什么不对？");
        }

    }


    // Utility Function above.
    // =============================================================================================


    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {

        //strcutInfo.put(st.struct_type.struct_Name,new StructInfo(st,strcutInfo));
/*
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
*/
        return null;
    }

    @Override
    public Register visitBlock(Block b) {

        for(VarDecl varDecl : b.vardelcs){
            varDecl.accept(this);
        }

        for(Stmt stmt : b.stmts){
            stmt.accept(this);
        }

        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl p) {

        ArrayList<String> cur_List;
        current_Stack_offset = 0;
        if(p.name.equals("main")){
            cur_List = mainFun;
        }else{
            cur_List = funOut;
        }

        cur_List.add(p.name + ":");

        if(lib_fun.containsKey(p.name)){
            cur_List.add(lib_fun.get(p.name));
            currentList.add("jr " + Register.ra.toString());
            return null;
        }


        //这个得思考下？
        //没啥用的啊？
        // 这个问题贼鸡儿大！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        for(VarDecl varDecl : p.params){
            varDecl.accept(this);
            System.out.println(varDecl.varName);
            System.out.println(varDecl.var_type);
            System.out.println(varDecl.isStatic);
            System.out.println(varDecl.atRegister);

        }

        p.block.accept(this);

        if(!isMain) {
            currentList.add("jr " + Register.ra.toString());
        }

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
            if(funDecl.name.equals("main")){
                isMain = true;
            }
            if(isMain){
                currentList = mainFun;
            }else{
                currentList = funOut;
            }
            funDecl.accept(this);
            isMain = false;
        }


        //currentList.add("li  " + Register.v0.toString() + ", 10");
        //currentList.add("syscall");

        return null;

    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        if(vd.isStatic) {
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

        }else if(vd.atRegister != null){
          // 貌似什么都不需要做啊？
        } else{

            // addi $sp , $sp, -4

            if(isMain){
                currentList = mainFun;
            }else{
                currentList = funOut;
            }

            vd.stack_offset = current_Stack_offset;

            if (vd.var_type instanceof StructType) {
                String structName = ((StructType) vd.var_type).struct_Name;
                currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" + strcutInfos.get(structName).size);
                current_Stack_offset -= strcutInfos.get(structName).size;

            } else if (vd.var_type instanceof ArrayType) {

                int size = ((ArrayType) vd.var_type).size;

                if (((ArrayType) vd.var_type).elem_type == BaseType.CHAR) {
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" + size);
                    current_Stack_offset -= size;
                } else if (((ArrayType) vd.var_type).elem_type instanceof StructType) {

                    StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) vd.var_type).elem_type).struct_Name);

                    if (structInfo == null) {
                        System.err.println("Something must be wrong!!");
                        return null;
                    }
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  structInfo.size * size);
                    current_Stack_offset -= structInfo.size * size;

                } else {
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  size * 4);
                    current_Stack_offset -= size * 4;

                }
            } else if (vd.var_type == BaseType.CHAR) {
                currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  1);
                current_Stack_offset -= 1;
            } else {
                currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  4);
                current_Stack_offset -= 4;
            }


        }

        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {

        VarDecl varDecl = v.vd;

        if(v.vd.atRegister != null){
            return v.vd.atRegister;
        }

        if(varDecl.isStatic){
            Register addr = getRegister();
            currentList.add("la " + addr.toString() + ", " + varDecl.varName);
            Register result  = getRegister();
            currentList.add("lw " + addr.toString() + ", " + result.toString());
            freeRegister(addr);
            return result;
        } else{
            int offset = varDecl.stack_offset;
            Register result  = getRegister();
            currentList.add("lw " + result.toString() + ", " +(-offset) + "($fp)");
            freeRegister(result);
            return result;
        }

        //return null;
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
        Register result = getRegister();
        currentList.add("li " + result.toString() + ", " + v.value);
        return result;
    }

    @Override
    public Register visitStrLiteral(StrLiteral v) {
        Register result = getRegister();
        String label = newLable();
        stastic_data.add(label + ": " + ".asciiz " + "\"" + v + "\"");
        str_label.put(v.value, label);
        currentList.add("la " + result.toString() + ", " + label);
        return result;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral v) {
        Register result = getRegister();
        currentList.add("li " + result.toString() + ", "+ ((int) v.value));
        return result;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr v) {

        // save temp register.

        saveAllRegister();
        FunDecl funDecl = v.funDecl;

        int temp = 0;
        for(VarDecl varDecl : funDecl.params){
            if (varDecl.atRegister != null){
                currentList.add("move " + varDecl.atRegister.toString() + " , " + v.params.get(temp).accept(this).toString());
                freeRegister(v.params.get(temp).accept(this));
            } else if(varDecl.isStatic){
                currentList.add("sw " + v.params.get(temp).accept(this).toString() + " , " + varDecl.varName);
                freeRegister(v.params.get(temp).accept(this));
            } else {
                currentList.add("sw " + v.params.get(temp).accept(this).toString() + " , " + -varDecl.stack_offset + "(" + Register.fp + ")");
                freeRegister(v.params.get(temp).accept(this));
            }
        }


        Register result;

        currentList.add("jal " + v.fun_name);

        loadAllRegisters();

        // 我们需要研究library function！！ =========================================================================================================================================
        return Register.v0;
    }

    @Override
    public Register visitBinOp(BinOp v) {
        Register lhs = v.first.accept(this);
        Register rhs = v.second.accept(this);
        Register result = getRegister();

        switch (v.operator) {
            case ADD:
                currentList.add("add " + result.toString() + ", " + lhs.toString() + ", " + rhs.toString());
                break;
            case SUB:
                currentList.add("sub " + result.toString() + ", " + lhs.toString() + ", " + rhs.toString());
                break;
            case DIV:
                currentList.add("div " + lhs.toString() + ", " + rhs.toString());
                currentList.add("mflo " + result.toString());
                break;
            case MUL:
                currentList.add("mult " + lhs.toString() + ", " + rhs.toString());
                currentList.add("mflo " + result.toString());

                break;
            case MOD:
                currentList.add("div " + lhs.toString() + ", " + rhs.toString());
                currentList.add("mfhi " + result.toString());
                break;
            case GE:
                currentList.add("sge " + ", " + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case LE:
                currentList.add("sle " + ", " + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case GT:
                currentList.add("sgt " + ", " + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case LT:
                currentList.add("slt " + ", " + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case NE:
                currentList.add("sne " + ", " + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case EQ:
                currentList.add("seq " + ", " + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            default:
                result = null;
        }
        freeRegister(lhs);
        freeRegister(rhs);
        return result;
    }

    @Override
    public Register visitOp(Op v) {
        return null;
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr v) {
        return null;
    }

    // 这些都留着之后些吧。 ============================================================================================================================================================
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

        return v.expr.accept(this);
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

        Register lhsReg = v.lhs.accept(this);
        Register rhsReg = v.rhs.accept(this);


        // 未完待续。 ================================================================================================================

        //VarExpr, FieldAccessExpr, ArrayAccessExpr or ValuteAtExpr.

        if(v.lhs instanceof  VarExpr) {
            if (((VarExpr) v.lhs).vd.isStatic) {
                currentList.add("sw  " + rhsReg.toString() + ", " + ((VarExpr) v.lhs).name);
            } else if (((VarExpr) v.lhs).vd.atRegister != null) {
                currentList.add("move  " + lhsReg.toString() + ", " + rhsReg.toString());
            } else {
                currentList.add("sw " + rhsReg.toString() + " " + -((VarExpr) v.lhs).vd.stack_offset + "(" + Register.fp + ")");
            }
        }


        return null;
    }

    @Override
    public Register visitReturn(Return v) {

        Register register = v.expr.accept(this);

        currentList.add("move  " + Register.v0.toString() + ", " + register.toString());

        freeRegister(register);

        return  Register.v0;

    }
}
