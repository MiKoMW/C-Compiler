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
        mainFun.add("main:");
        mainFun.add("move $fp, $sp");

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
        return "Lable"+(lab_con++);
    }


    //==================================================================================================================== 这个思考下。
    //private int str_lab;

    private Stack<Stack<Register>> registerHistory = new Stack<>();

    private void saveAllRegister(){

        Stack<Register> freeRegs_temp = new Stack<Register>();
        freeRegs_temp.addAll(freeRegs);
        registerHistory.push(freeRegs_temp);
        freeRegs = new Stack<>();
        freeRegs.addAll(Register.tmpRegs);

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

        register = Register.fp;
        currentList.add("sw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

        currentList.add("move $fp, $sp");

    }

    private void loadAllRegisters(){
        freeRegs = registerHistory.pop();

        currentList.add("move $sp, $fp");
        int tempIdx = 26 * 4;
        currentList.add("addi " + "$sp, $sp, " + tempIdx);

        // saving all temp register/
        for (Register register : Register.tmpRegs) {
            currentList.add("lw " + register.toString() + ", " + "0($sp)");
            currentList.add("addi " + "$sp, $sp, -4");
        }

        for(Register register : Register.paramRegs){
            currentList.add("lw " + register.toString() + ", " + "0($sp)");
            currentList.add("addi " + "$sp, $sp, -4");
        }

        Register register = Register.v0;
        currentList.add("lw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

        register = Register.gp;
        currentList.add("lw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

        register = Register.ra;
        currentList.add("lw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

        register = Register.fp;
        currentList.add("lw " + register.toString() + ", " + "0($sp)");
        currentList.add("addi " + "$sp, $sp, -4");

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
            cur_List.add(p.name + ":");
        }


        if(lib_fun.containsKey(p.name)){
            int temp = 0;
            for(VarDecl varDecl : p.params){
                varDecl.atRegister = Register.paramRegs[temp++];
            }
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
                vd.memo_size = strcutInfos.get(structName).size;

            } else if (vd.var_type instanceof ArrayType) {

                int size = ((ArrayType) vd.var_type).size;

                if (((ArrayType) vd.var_type).elem_type == BaseType.CHAR) {
                    stastic_data.add(vd.varName + ": .space " + size);
                    vd.memo_size = size;

                } else if (((ArrayType) vd.var_type).elem_type instanceof StructType) {

                    StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) vd.var_type).elem_type).struct_Name);

                    if (structInfo == null) {
                        System.err.println("Something must be wrong!!");
                        return null;
                    }
                    stastic_data.add(vd.varName + ": .space " + structInfo.size * size);
                    vd.memo_size = structInfo.size * size;


                } else {
                    stastic_data.add(vd.varName + ": .space " + size * 4);
                    vd.memo_size = 4 * size;

                }
            } else if (vd.var_type == BaseType.CHAR) {
                stastic_data.add(vd.varName + ": .space " + 1);
                vd.memo_size = 1;

            } else {
                stastic_data.add(vd.varName + ": .space " + 4);
                vd.memo_size = 4;
            }

        }else if(vd.atRegister != null){
          // 貌似什么都不需要做啊？
        } else{

            //Dynamic
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
                vd.memo_size = strcutInfos.get(structName).size;
                current_Stack_offset += strcutInfos.get(structName).size;

            } else if (vd.var_type instanceof ArrayType) {

                int size = ((ArrayType) vd.var_type).size;

                if (((ArrayType) vd.var_type).elem_type == BaseType.CHAR) {
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" + size);
                    vd.memo_size = size;

                    current_Stack_offset += size;
                } else if (((ArrayType) vd.var_type).elem_type instanceof StructType) {
                    StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) vd.var_type).elem_type).struct_Name);

                    if (structInfo == null) {
                        System.err.println("Something must be wrong!!");
                        return null;
                    }
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  structInfo.size * size);
                    vd.memo_size = structInfo.size * size;
                    current_Stack_offset += structInfo.size * size;

                } else {
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  size * 4);
                    vd.memo_size = 4 * size;
                    current_Stack_offset += size * 4;

                }
            } else if (vd.var_type == BaseType.CHAR) {
                currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  1);
                vd.memo_size = 1;
                current_Stack_offset += 1;
            } else {
                currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  4);
                vd.memo_size = 4;
                current_Stack_offset += 4;
            }


        }

        return null;
    }

    // 基本类型传值，其他类型传址。
    @Override
    public Register visitVarExpr(VarExpr v) {

        //基本类型传值，array和structure传址。
        Register ans = getRegister();
        VarDecl varDecl = v.vd;
        Register addrReg = getRegister();


        if(v.vd.atRegister != null){
            currentList.add("move " + ans.toString() + v.vd.atRegister.toString());
            freeRegister(addrReg);
            return ans;
        }


        if(varDecl.isStatic){
            if(varDecl.var_type == BaseType.INT || varDecl.var_type instanceof PointerType){
                currentList.add("la "+addrReg.toString()+","+v.name);
                currentList.add("lw "+ans.toString()+", ("+addrReg.toString()+")");
                freeRegister(addrReg);
            } else if (varDecl.var_type == BaseType.CHAR){
                currentList.add("la "+addrReg.toString()+","+v.name);
                currentList.add("lb "+ans.toString()+", ("+addrReg.toString()+")");
                freeRegister(addrReg);
            } else {
                writer.println("la "+addrReg.toString()+","+v.name);
                freeRegister(ans);
                return addrReg;
            }
            return ans;
        } else{
            if(varDecl.var_type == BaseType.INT || varDecl.var_type instanceof PointerType){
                currentList.add("lw "+ans.toString()+", " + -varDecl.stack_offset + "("+Register.fp.toString()+")");
                freeRegister(addrReg);
            } else if (varDecl.var_type == BaseType.CHAR){
                currentList.add("lb "+ans.toString()+", " + -varDecl.stack_offset + "("+Register.fp.toString()+")");
                freeRegister(addrReg);
            } else {
                currentList.add("la "+ans.toString()+", "+ -varDecl.stack_offset + "("+Register.fp.toString()+")");
                freeRegister(ans);
                return addrReg;
            }
            return ans;

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
        if(lib_fun.containsKey(v.fun_name)){
            currentList.add("move " + Register.paramRegs[0] + " , " + v.params.get(temp).accept(this).toString());
        }else{
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
            temp++;
        }
        }


        Register result;

        currentList.add("jal " + v.fun_name);

        loadAllRegisters();

        // 我们需要研究library function！！ =========================================================================================================================================
        return Register.v0;
    }

    // 所有的register都会被free。
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

        //我们假设所有array都是传址的。
        Register ans = getRegister();
        Register arr = v.array.accept(this);
        Register idx = v.index.accept(this);

        boolean isStatic = ((VarExpr) v.array).vd.isStatic;
        Type type = ((VarExpr) v.array).vd.var_type;

        if(isStatic) {
            if (type == BaseType.CHAR) {
                currentList.add("move " + ans.toString() + ", " + arr.toString());
                currentList.add("add " + ans.toString() + ", " + ans.toString() + ", " + idx.toString());
            }
            if (type instanceof StructType) {
                int size = ((VarExpr) v.array).vd.memo_size;
                Register temp = getRegister();
                currentList.add("move " + ans.toString() + ", " + arr.toString());
                currentList.add("li " + temp.toString() + ",  4");
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + temp.toString());
                currentList.add("li " + temp.toString() + ",  " + size);
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + temp.toString());
                freeRegister(temp);
                currentList.add("add " + ans.toString() + ", " + ans.toString() + ", " + idx.toString());
            } else {
                Register temp = getRegister();
                currentList.add("move " + ans.toString() + ", " + arr.toString());
                currentList.add("li " + temp.toString() + ",  4");
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + temp.toString());
                freeRegister(temp);
                currentList.add("add " + ans.toString() + ", " + ans.toString() + ", " + idx.toString());
            }
        }else {
            if (type == BaseType.CHAR) {
                currentList.add("move " + ans.toString() + ", " + arr.toString());
                currentList.add("sub " + ans.toString() + ", " + ans.toString() + ", " + idx.toString());
            }
            if (type instanceof StructType) {
                int size = ((VarExpr) v.array).vd.memo_size;
                Register temp = getRegister();
                currentList.add("move " + ans.toString() + ", " + arr.toString());
                currentList.add("li " + temp.toString() + ",  4");
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + temp.toString());
                currentList.add("li " + temp.toString() + ",  " + size);
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + temp.toString());
                freeRegister(temp);
                currentList.add("sub " + ans.toString() + ", " + ans.toString() + ", " + idx.toString());
            } else {
                Register temp = getRegister();
                currentList.add("move " + ans.toString() + ", " + arr.toString());
                currentList.add("li " + temp.toString() + ",  4");
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + temp.toString());
                freeRegister(temp);
                currentList.add("sub " + ans.toString() + ", " + ans.toString() + ", " + idx.toString());
            }
        }

        freeRegister(arr);
        freeRegister(idx);
        return ans;
    }

    // 这些都留着之后些吧。 ============================================================================================================================================================

    // 这货就应该返还pointer！ 这东西register里面是address。
    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr v) {

        Register ans = getRegister();

        //如果是structure 这货应该是传址的。
        Register struct = v.struct.accept(this);

        String structName = ((StructType) ((VarExpr)v.struct).vd.var_type).struct_Name;

        StructInfo structInfo = strcutInfos.get(structName);

        int offset = structInfo.innerDecl.get(structName);
        //Type type = structInfo.typeMapping.get(structName);

        if( ( ((VarExpr)v.struct).vd.isStatic)){
            currentList.add("move " + ans.toString() + ", " + struct.toString());
            currentList.add("addi " + ans.toString() + ", " + offset);
        }else{
            currentList.add("move " + ans.toString() + ", " + struct.toString());
            currentList.add("addi " + ans.toString() + ", " + -offset);
        }
        freeRegister(struct);
        return ans;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr v) {

        Register ans = getRegister();
        Register expr = v.expr.accept(this);

        currentList.add("move " + ans.toString() + ", " + expr.toString());
        currentList.add("lw " + ans.toString() + ", " + ans.toString());
        freeRegister(expr);

        return ans;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr v) {

        Register ans = getRegister();

        if(v.size_of_type == BaseType.INT){
            currentList.add("li " + ans.toString() + ", 4");
        }else if(v.size_of_type == BaseType.CHAR){
            currentList.add("li " + ans.toString() + ", 1");
        } else if(v.size_of_type instanceof PointerType){
            currentList.add("li " + ans.toString() + ", 4");
        } else if(v.size_of_type instanceof ArrayType) {
            int size = 0;
            if (((ArrayType) v.size_of_type).elem_type == BaseType.CHAR) {
                size = ((ArrayType) v.size_of_type).size;
            } else if (((ArrayType) v.size_of_type).elem_type instanceof StructType) {

                StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) v.size_of_type).elem_type).struct_Name);

                if (structInfo == null) {
                    System.err.println("Something must be wrong!!");
                    return null;
                }
                size = structInfo.size * ((ArrayType) v.size_of_type).size;
            } else {
                size = ((ArrayType) v.size_of_type).size * 4;
            }
            currentList.add("li " + ans.toString() + ", " + size);
        } else if(v.size_of_type instanceof StructType){
                StructInfo structInfo = strcutInfos.get(((StructType) v.size_of_type).struct_Name);
                if (structInfo == null){
                    System.err.println("Something must be wrong!!");
                    return null;
                }
                currentList.add("li " + ans.toString() + ", " + structInfo.size);
        } else{
            currentList.add("li " + ans.toString() + ", 4");
        }
        return ans;
    }

    // mips这没啥用吧？
    @Override
    public Register visitTypecastExpr(TypecastExpr v) {
        // 我就要这么写！：）
        Register ans = v.expr.accept(this);
        return ans;
    }

    @Override
    public Register visitExprStmt(ExprStmt v) {
        Register ans = v.expr.accept(this);
        return ans;
    }

    @Override
    public Register visitWhile(While v) {

        String start = newLable();
        String end = newLable();
        Register con = v.expr.accept(this);

        currentList.add(start + ":");
        currentList.add("beqz " + con.toString() + ", " + end);
        v.stmt.accept(this);
        currentList.add("j " + start);
        currentList.add(end + ":");
        freeRegister(con);

        return null;
    }

    @Override
    public Register visitIf(If v) {

        String elseLab = newLable();
        String endLab = newLable();

        Register con = v.expr.accept(this);

        currentList.add("beqz " + con.toString() + ", " + elseLab);

        v.stmt1.accept(this);

        currentList.add("j " + endLab);

        currentList.add(elseLab = ":");

        if(v.stmt2!=null){
            v.stmt2.accept(this);
        }

        currentList.add(endLab + ":");

        freeRegister(con);

        return null;
    }

    @Override
    public Register visitAssign(Assign v) {

        Register lhsReg = v.lhs.accept(this);
        Register rhsReg = v.rhs.accept(this);

        //VarExpr, FieldAccessExpr, ArrayAccessExpr or ValuteAtExpr.

        if(v.lhs instanceof  VarExpr) {
            currentList.add("move " + lhsReg.toString() + ", " + rhsReg.toString());

            if (((VarExpr) v.lhs).vd.isStatic) {
                if(((VarExpr) v.lhs).vd.var_type == BaseType.INT || ((VarExpr) v.lhs).vd.var_type instanceof PointerType) {
                    currentList.add("sw  " + rhsReg.toString() + ", " + ((VarExpr) v.lhs).name);
                }else {
                    currentList.add("sb  " + rhsReg.toString() + ", " + ((VarExpr) v.lhs).name);

                }
            }  else if (((VarExpr) v.lhs).vd.atRegister != null) {
                //currentList.add("move  " + lhsReg.toString() + ", " + rhsReg.toString());
            } else {
                if(((VarExpr) v.lhs).vd.var_type == BaseType.INT || ((VarExpr) v.lhs).vd.var_type instanceof PointerType) {
                    currentList.add("sw  " + rhsReg.toString() + ", " + -((VarExpr) v.lhs).vd.stack_offset + "(" + Register.fp + ")");
                }else {
                    currentList.add("sb  " + rhsReg.toString() + ", " + -((VarExpr) v.lhs).vd.stack_offset + "(" + Register.fp + ")");

                }
            }
        }else if(v.lhs instanceof  FieldAccessExpr){
            //这个不一定对。 希望传址。

            //Register field = ((FieldAccessExpr) v.lhs).accept(this);

            if (((VarExpr) ((FieldAccessExpr) v.lhs).struct).vd.isStatic){
                StructInfo structInfo = strcutInfos.get(((StructType)((VarExpr) ((FieldAccessExpr) v.lhs).struct).vd.var_type).struct_Name);
                int offset = structInfo.innerDecl.get(((FieldAccessExpr) v.lhs).field);
                Type type = structInfo.typeMapping.get(((FieldAccessExpr) v.lhs).field);

                if(((VarExpr) v.lhs).vd.var_type == BaseType.INT || ((VarExpr) v.lhs).vd.var_type instanceof PointerType) {
                    currentList.add("sw  " + rhsReg.toString() + ", " + -((VarExpr) v.lhs).vd.stack_offset + "(" + Register.fp + ")");
                }else {
                    currentList.add("sb  " + rhsReg.toString() + ", " + -((VarExpr) v.lhs).vd.stack_offset + "(" + Register.fp + ")");

                }



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
