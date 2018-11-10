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


    //private boolean globalLevel;

    // Buffer the output.
    private ArrayList<String> output = new ArrayList<>();

    private ArrayList<String> mainFun = new ArrayList<>();
    private ArrayList<String> funOut = new ArrayList<>();
    private ArrayList<String> stastic_data = new ArrayList<>();
    private ArrayList<String> currentList;

    private int lab_con;

    private HashMap<String,StructInfo> strcutInfos = new HashMap<>();
    private HashMap<String,String> str_label = new HashMap<>();
    private HashMap<String,String> lib_fun = new HashMap<>();

    private boolean isMain;

    private int current_Stack_offset;

    private void init(Program program){

        stastic_data.add(".data");
        stastic_data.add(".align 2");

        funOut.add(".text");
        //funOut.add("j main");

        mainFun.add(".globl main");
        mainFun.add("main:");
       // mainFun.add("move $fp, $sp");

        current_Stack_offset = 0;
        OffSetVisitor offSetVisitor = new OffSetVisitor();
        offSetVisitor.visitProgram(program);
        this.strcutInfos = offSetVisitor.getStrcutInfos();
        lab_con = 0;

        globalLevel = true;

        // =========================================================================================改了parameter后可能会存在问题！！！！
        lib_fun.put("print_s","li $v0, 4\nsyscall");
        lib_fun.put("print_i","li $v0, 1\nsyscall");
        lib_fun.put("print_c","li $v0, 11\nsyscall");
        lib_fun.put("read_c","li $v0, 12\nsyscall");
        lib_fun.put("read_i","li $v0, 5\nsyscall");

        //这我也不知道对不对了!!!!!!!!
        lib_fun.put("mcmalloc","li $v0, 9\nsyscall");

    }

    private void finale(){

        output.addAll(stastic_data);
        output.addAll(funOut);
        //System.out.println(mainFun.size());indfsa
        /*
        if (mainFun.size() < 2){
            mainFun.add("main:");
        }*/
        output.addAll(mainFun);

        // 这个有问题！！！！！
        output.add("li " + Register.v0.toString() + ", 10");
        output.add("syscall");
    }

    private String newLable(){
        return "Lable"+(lab_con++);
    }

    private Stack<Stack<Register>> registerHistory = new Stack<>();

    private boolean globalLevel;


    // 从fp开始。
    private void saveAllRegister(){

        Stack<Register> freeRegs_temp = new Stack<Register>();
        freeRegs_temp.addAll(freeRegs);
        registerHistory.push(freeRegs_temp);
        freeRegs = new Stack<>();
        freeRegs.addAll(Register.tmpRegs);

        // saving all temp register/
        for (Register register : Register.tmpRegs) {
            currentList.add("addi " + "$sp, $sp, -4");
            currentList.add("sw " + register.toString() + ", " + "0($sp)");
            current_Stack_offset += 4;
        }

    }

    private void loadAllRegisters(){
        freeRegs = registerHistory.pop();

        currentList.add("move $sp, $fp");

        // saving all temp register/
        for (Register register : Register.tmpRegs) {
            currentList.add("addi " + "$sp, $sp, -4");
            currentList.add("lw " + register.toString() + ", " + "0($sp)");
        }

        currentList.add("move $sp, $fp");

    }


    public Register genLibFun(FunCallExpr v){

        for (Register register : Register.tmpRegs) {
            currentList.add("addi " + "$sp, $sp, -4");
            currentList.add("sw " + register.toString() + ", " + "0($sp)");
        }

        Register ans = getRegister();
        int temp_Reg_num = Register.tmpRegs.size();

        if(v.fun_name.equals("read_c")) {
            currentList.add("li $v0, 12\nsyscall");
        } else if(v.fun_name.equals("read_i")){
            currentList.add("li $v0, 5\nsyscall");
        } else if(v.fun_name.equals("mcmalloc")){
            Register temp = v.params.get(0).accept(this);
            currentList.add("move " + Register.paramRegs[0].toString() + ", " + temp);
            freeRegister(temp);
            currentList.add("li $v0, 9\nsyscall");
        } else if(v.fun_name.equals("print_s")){
            Register temp = v.params.get(0).accept(this);
            currentList.add("move " + Register.paramRegs[0].toString() + ", " + temp);
            freeRegister(temp);
            currentList.add("li $v0, 4\nsyscall");
            currentList.add("addi " + "$sp, $sp, " + temp_Reg_num * 4);
            for (Register register : Register.tmpRegs) {
                currentList.add("addi " + "$sp, $sp, -4");
                currentList.add("lw " + register.toString() + ", " + "0($sp)");
            }
            currentList.add("addi " + "$sp, $sp, " + temp_Reg_num * 4);
            freeRegister(ans);
            return null;
        } else if(v.fun_name.equals("print_i")){
            Register temp = v.params.get(0).accept(this);
            currentList.add("move " + Register.paramRegs[0].toString() + ", " + temp);
            freeRegister(temp);
            currentList.add("li $v0, 1\nsyscall");
            currentList.add("addi " + "$sp, $sp, " + temp_Reg_num * 4);

            for (Register register : Register.tmpRegs) {
                currentList.add("addi " + "$sp, $sp, -4");
                currentList.add("lw " + register.toString() + ", " + "0($sp)");
            }
            currentList.add("addi " + "$sp, $sp, " + temp_Reg_num * 4);
            freeRegister(ans);
            return null;
        } else if(v.fun_name.equals("print_c")){
            Register temp = v.params.get(0).accept(this);
            currentList.add("move " + Register.paramRegs[0].toString() + ", " + temp);
            freeRegister(temp);
            currentList.add("li $v0, 11\nsyscall");

            currentList.add("addi " + "$sp, $sp, " + temp_Reg_num * 4);

            for (Register register : Register.tmpRegs) {
                currentList.add("addi " + "$sp, $sp, -4");
                currentList.add("lw " + register.toString() + ", " + "0($sp)");
            }
            currentList.add("addi " + "$sp, $sp, " + temp_Reg_num * 4);
            freeRegister(ans);
            return null;
        }

        currentList.add("addi " + "$sp, $sp, " + temp_Reg_num * 4);
        for (Register register : Register.tmpRegs) {
            currentList.add("addi " + "$sp, $sp, -4");
            currentList.add("lw " + register.toString() + ", " + "0($sp)");
        }
        currentList.add("addi " + "$sp, $sp, " + temp_Reg_num * 4);
        currentList.add("move " + ans.toString() + ", " + Register.v0);
        return ans;
    }


    // This function emis codes for moving values from one position to another position with size.
    public void copyValue(Register from, Register to, int size){

        Register temp = getRegister();

        for(int con = 0; con < size; con++){
            currentList.add("lb  " + temp.toString() + ", " + con + "(" + from.toString() + ")");
            currentList.add("sb  " + temp.toString() + ", " + con + "(" + to.toString() + ")");
        }

        freeRegister(temp);

    }


    // Utility Function above.
    // =============================================================================================


    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {
        // done it in offset visitor!
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
        //Type returnType = p.fun_type;

        current_Stack_offset = 0;



        if(p.name.equals("main")){
            cur_List = mainFun;
        }else{
            cur_List = funOut;
            if(lib_fun.containsKey(p.name)) {
                //cur_List.add(lib_fun.get(p.name));
                //cur_List.add("jr " + Register.ra.toString());
                return null;
            }

            cur_List.add(p.name + ":");
        }



        //这个得思考下？
        //没啥用的啊？
        // 这个问题贼鸡儿大！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！


        int params_size = 0;

        currentList.add("move $fp, $sp");

        for(VarDecl varDecl : p.params){
            varDecl.isStatic = false;
            //varDecl.accept(this);
            varDecl.stack_offset = current_Stack_offset;
            current_Stack_offset += varDecl.memo_size;
            params_size += varDecl.memo_size;
        }

        p.param_size = params_size;

        for(VarDecl varDecl : p.params){
            params_size -= varDecl.memo_size;
            varDecl.stack_offset = -params_size;
        }


        current_Stack_offset = 0;

        if(!p.name.equals("main")) {
            saveAllRegister();
        }
        p.block.accept(this);

        //currentList.add("move $sp, $fp");

        if(!isMain) {
            currentList.add("EndFun_" + p.name+ ":");
            loadAllRegisters();
            currentList.add("jr " + Register.ra.toString());
        } else {
            currentList.add("EndFun_" + p.name+ ":");
        }
        return Register.v0;
    }

    @Override
    public Register visitProgram(Program p) {
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

        return null;

    }

    @Override
    public Register visitVarDecl(VarDecl vd) {

        if(vd.isStatic) {
            if (vd.var_type instanceof StructType) {
                String structName = ((StructType) vd.var_type).struct_Name;
                stastic_data.add(vd.varName + ": .space " + strcutInfos.get(structName).size);
                vd.memo_size = strcutInfos.get(structName).size;
                if((vd.memo_size % 4) != 0){
                    System.err.println("StructType size not right!");
                }
            } else if (vd.var_type instanceof ArrayType) {

                int size = ((ArrayType) vd.var_type).size;

                if (((ArrayType) vd.var_type).elem_type == BaseType.CHAR) {
                    int temp = size;
                    if((temp % 4) != 0){
                        temp -= (temp % 4);
                        temp += 4;
                    }
                    vd.memo_size = temp;
                    stastic_data.add(vd.varName + ": .space " + temp);

                } else if (((ArrayType) vd.var_type).elem_type instanceof StructType) {

                    StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) vd.var_type).elem_type).struct_Name);

                    if (structInfo == null) {
                        System.err.println("Something must be wrong!!");
                        return null;
                    }
                    stastic_data.add(vd.varName + ": .space " + structInfo.size * size);
                    vd.memo_size = structInfo.size * size;
                    if((structInfo.size % 4) != 0){
                        System.err.println("StructType size not right!");

                    }
                } else {
                    stastic_data.add(vd.varName + ": .space " + size * 4);
                    vd.memo_size = 4 * size;
                }
            } else {
                stastic_data.add(vd.varName + ": .space " + 4);
                vd.memo_size = 4;
            }
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
                vd.memo_size = strcutInfos.get(structName).size;
                current_Stack_offset += strcutInfos.get(structName).size;
                vd.stack_offset = current_Stack_offset;

            } else if (vd.var_type instanceof ArrayType) {

                int size = ((ArrayType) vd.var_type).size;

                if (((ArrayType) vd.var_type).elem_type == BaseType.CHAR) {
                    int temp = size;
                    if((temp % 4) != 0){
                        temp -= (temp % 4);
                        temp += 4;
                    }
                    vd.memo_size = temp;
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" + temp);
                    current_Stack_offset += temp;
                    vd.stack_offset = current_Stack_offset;

                } else if (((ArrayType) vd.var_type).elem_type instanceof StructType) {
                    StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) vd.var_type).elem_type).struct_Name);

                    if (structInfo == null) {
                        System.err.println("Something must be wrong!!");
                        return null;
                    }

                    if((structInfo.size) % 4 != 0){
                        System.err.println("Struct size is not right for array!");
                    }

                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  structInfo.size * size);
                    current_Stack_offset += structInfo.size * size;
                    vd.memo_size = structInfo.size * size;
                    vd.stack_offset = current_Stack_offset;
                } else {
                    currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  size * 4);
                    vd.memo_size = 4 * size;
                    current_Stack_offset += size * 4;
                    vd.stack_offset = current_Stack_offset;
                }
            } else {
                currentList.add("add  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" +  4);
                vd.memo_size = 4;
                current_Stack_offset += 4;
                vd.stack_offset = current_Stack_offset;
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
                currentList.add("la "+ans.toString()+","+v.name);
                freeRegister(addrReg);
                return ans;
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
                freeRegister(addrReg);
                return ans;
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
        stastic_data.add(label + ": " + ".asciiz " + "\"" + v.value + "\"");
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

        FunDecl funDecl = v.funDecl;
        // Lib_function 一会写！！


        if(lib_fun.containsKey(v.fun_name)){
            return genLibFun(v);
        }
            Type returnType = funDecl.fun_type;

        if(returnType == BaseType.VOID) {

        }
        else if (returnType instanceof StructType) {
                String structName = ((StructType) returnType).struct_Name;
                currentList.add("addi  " + Register.sp.toString() + ", " + Register.sp.toString() + ", -" + strcutInfos.get(structName).size);
                funDecl.return_Size = strcutInfos.get(structName).size;
                current_Stack_offset += strcutInfos.get(structName).size;
                v.stack_offset = current_Stack_offset;
        } else {
                currentList.add("addi  " + Register.sp.toString() +", "+ Register.sp.toString() + ", -" +  4);
                funDecl.return_Size = 4;
                current_Stack_offset += 4;
                v.stack_offset = current_Stack_offset;
        }
        int pre_stack_offset = current_Stack_offset;


        currentList.add("addi  " + Register.sp.toString() +", "+ Register.sp.toString() + ", -" +  4);
        currentList.add("sw $ra, (" + Register.sp.toString()+")");
        currentList.add("addi  "+ Register.sp.toString() +", " + Register.sp.toString() + ", -" +  4);
        currentList.add("sw "+ Register.fp + ", (" + Register.sp.toString()+")");
        //currentList.add("addi $sp, $sp, -4");

        int param_Szie = 0;
        int con = 0;
        for(Expr expr : v.params){

            Register register = expr.accept(this);
            Type type = v.params.get(con).type;
            con++;

            if(type instanceof StructType){
                StructInfo structInfo = strcutInfos.get(((StructType) type).struct_Name);
                int size = structInfo.size;
                currentList.add("addi $sp, $sp, -" + (size));

                copyValue(register,Register.sp,size);
                freeRegister(register);
                param_Szie += size;
            }else{
                currentList.add("addi $sp, $sp, -4");
                if(type == BaseType.CHAR){
                    currentList.add("sb " + register.toString() + ", " + "(" + Register.sp.toString()+ ")");
                }else {
                    currentList.add("sw " + register.toString() + ", " + "(" + Register.sp.toString() + ")");
                }
                freeRegister(register);
                param_Szie += 4;
            }
        }

        currentList.add("jal " + v.fun_name);
        currentList.add("move " + Register.sp.toString() + ", " + Register.fp.toString());
        currentList.add("addi  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " +  param_Szie);
        currentList.add("lw "+ Register.fp + ", (" + Register.sp.toString()+")");
        currentList.add("addi  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " +  4);
        currentList.add("lw $ra, (" + Register.sp.toString()+")");
        currentList.add("addi  " + Register.sp.toString() + ", " + Register.sp.toString() + ", " +  4);
        //param_Szie = v.funDecl.param_size;

        Register ans = getRegister();
        currentList.add("move " + ans.toString() + ", " + Register.sp.toString());
        if(returnType == BaseType.VOID) {
            freeRegister(ans);
            return null;
        }
        else if (returnType instanceof StructType) {

        } else {
            currentList.add("lw " + ans.toString() + ", (" + ans.toString() + ")");
        }

        return ans;
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
                currentList.add("sge "  + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case LE:
                currentList.add("sle " + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case GT:
                currentList.add("sgt "  + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case LT:
                currentList.add("slt " + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case NE:
                currentList.add("sne "+ result.toString() +"," + lhs.toString() + ", " + rhs.toString());
                break;
            case EQ:
                currentList.add("seq "  + result.toString() +"," + lhs.toString() + ", " + rhs.toString());
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



        Type type = ((VarExpr) v.array).vd.var_type;

        if (type instanceof StructType) {
                int size = ((VarExpr) v.array).vd.memo_size;
                currentList.add("move " + ans.toString() + ", " + arr.toString());
                currentList.add("li " + arr.toString() + ",  " + size);
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + arr.toString());
                currentList.add("add " + ans.toString() + ", " + ans.toString() + ", " + idx.toString());
        } else if(type == BaseType.CHAR){
                currentList.add("move " + ans.toString() + ", " + arr.toString());
                currentList.add("add " + ans.toString() + ", " + ans.toString() + ", " + idx.toString());
                currentList.add("lb " + ans.toString() + ", " + ans.toString());

        } else{
                currentList.add("move " + ans.toString() + ", " + arr.toString());
                currentList.add("li " + arr.toString() + ",  4");
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + arr.toString());
                currentList.add("add " + ans.toString() + ", " + ans.toString() + ", " + idx.toString());
                currentList.add("lw " + ans.toString() + ", (" + ans.toString() + ")");
        }
        freeRegister(arr);
        freeRegister(idx);
        return ans;
    }


    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr v) {

        Register ans = getRegister();

        //如果是structure 这货应该是传址的。
        Register struct = v.struct.accept(this);

        String structName = ((StructType) ((VarExpr)v.struct).vd.var_type).struct_Name;

        StructInfo structInfo = strcutInfos.get(structName);

        int offset = structInfo.innerDecl.get(structName);
        Type type = structInfo.typeMapping.get(structName);



        currentList.add("move " + ans.toString() + ", " + struct.toString());
        currentList.add("addi " +ans.toString() + ", "  + ans.toString() + ", " + offset);

        if(!(type instanceof StructType || type instanceof ArrayType)){
            if(type == BaseType.CHAR){
                currentList.add("lb " + ans.toString() + ", (" + ans.toString() +")");
            }else {
                currentList.add("lw " + ans.toString() + ", (" + ans.toString() +")");
            }
        }

        freeRegister(struct);
        return ans;
    }


    // 这东西怎么用呢？ ============================================================================================================================================================

    @Override
    public Register visitValueAtExpr(ValueAtExpr v) {

        Register ans = getRegister();
        Register expr = v.expr.accept(this);



        currentList.add("move " + ans.toString() + ", " + expr.toString());
        if(v.type == BaseType.CHAR){
            currentList.add("lb " + ans.toString() + ", (" + ans.toString() +")");
        }else if(v.type == BaseType.INT || v.type instanceof PointerType){
            currentList.add("lw " + ans.toString() + ", (" + ans.toString() +")");
        }
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
        return v.expr.accept(this);
    }

    @Override
    public Register visitExprStmt(ExprStmt v) {
        return v.expr.accept(this);
    }

    @Override
    public Register visitWhile(While v) {

        String start = newLable();
        String end = newLable();

        currentList.add(start + ":");
        Register con = v.expr.accept(this);
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

        currentList.add(elseLab + ":");

        if(v.stmt2!=null){
            v.stmt2.accept(this);
        }

        currentList.add(endLab + ":");

        freeRegister(con);

        return null;
    }

    // 左值是地址！！！！！！！！！！！！！！
    @Override
    public Register visitAssign(Assign v) {

        Register lhsReg;
        Register rhsReg = v.rhs.accept(this);

        //VarExpr, FieldAccessExpr, ArrayAccessExpr or ValueAtExpr.

        // deal with lhsReg and lhsReg is an address!!!!!!!!!!!!!!!!!!!



        if(v.lhs instanceof VarExpr) {
            if (((VarExpr) v.lhs).vd.isStatic) {
                lhsReg = getRegister();
                currentList.add("la  " + lhsReg.toString() + ", " + ((VarExpr) v.lhs).name);
            } else {
                lhsReg = getRegister();
                currentList.add("la  " + lhsReg.toString() + ", " + -((VarExpr) v.lhs).vd.stack_offset + "(" + Register.fp + ")");
            }
        } else if(v.lhs instanceof FieldAccessExpr){
            lhsReg = getRegister();

            //如果是structure 这货应该是传址的。
            Register struct = ((FieldAccessExpr) v.lhs).struct.accept(this);

            String structName = ((StructType) ((VarExpr)((FieldAccessExpr) v.lhs).struct).vd.var_type).struct_Name;

            StructInfo structInfo = strcutInfos.get(structName);

            int offset = structInfo.innerDecl.get(structName);

            currentList.add("move " + lhsReg.toString() + ", " + struct.toString());
            currentList.add("addi " + lhsReg.toString() + ", " + lhsReg.toString() + ", " + offset);
            freeRegister(struct);
            lhsReg = v.lhs.accept(this);
        } else if(v.lhs instanceof  ArrayAccessExpr){

            lhsReg = getRegister();
            //currentList.add("我的register？");
            Register arr = ((ArrayAccessExpr) v.lhs).array.accept(this);
            //System.out.println(((ArrayAccessExpr) v.lhs).array);
            //currentList.add("我的register？不见了？");
            Register idx = ((ArrayAccessExpr) v.lhs).index.accept(this);

            Type type = ((VarExpr) ((ArrayAccessExpr) v.lhs).array).vd.var_type;

            if (type instanceof StructType) {
                int size = ((VarExpr) ((ArrayAccessExpr) v.lhs).array).vd.memo_size;

                currentList.add("move " + lhsReg.toString() + ", " + arr.toString());
                currentList.add("li " + arr.toString() + ", " + size);
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + arr.toString());
                currentList.add("add " + lhsReg.toString() + ", " + lhsReg.toString() + ", " + idx.toString());
            } else if(type == BaseType.CHAR){
                currentList.add("add " + lhsReg.toString() + ", " + arr.toString() + ", " + idx.toString());
            } else{
                currentList.add("move " + lhsReg.toString() + ", " + arr.toString());
                currentList.add("li " + arr.toString() + ",  4");
                currentList.add("mul " + idx.toString() + ", " + idx.toString() + ", " + arr.toString());
                currentList.add("add " + lhsReg.toString() + ", " + lhsReg.toString() + ", " + idx.toString());
            }
            freeRegister(arr);
            freeRegister(idx);
        } else {
            lhsReg = ((ValueAtExpr) v.lhs).expr.accept(this);
        }

        //====================上面的都是地址！！

        if(v.assignType == BaseType.CHAR){
            currentList.add("sb  " + rhsReg.toString() + ", (" + lhsReg.toString() + ")");
        } else if(v.assignType instanceof StructType){
            StructInfo structInfo = strcutInfos.get(((StructType)v.assignType).struct_Name);
            int structSize = structInfo.size;
            copyValue(rhsReg,lhsReg,structSize);
        } else {
            currentList.add("sw  " + rhsReg.toString() + ", (" + lhsReg.toString()+")");
        }
        freeRegister(lhsReg);
        freeRegister(rhsReg);
        return null;
    }

    @Override
    public Register visitReturn(Return v) {

        if(isMain){
            if(v.expr == null) {
                currentList.add("li " + Register.v0.toString() + ", 10");
                currentList.add("syscall");
                return null;
            }else{
                Register register = v.expr.accept(this);
                currentList.add("move " + Register.v0.toString() + ", " + register.toString());
                freeRegister(register);
                currentList.add("li " + Register.v0.toString() + ", 17");
                currentList.add("syscall");
            }
        }

        Type type = v.returnType;
        if(v.expr == null) {
            currentList.add("jr " + Register.ra.toString());
            return Register.v0;
        }

        Register expr = v.expr.accept(this);
        //currentList.add("move " + Register.v0.toString() + ", " + expr.toString());

        int param_size = v.funDecl.param_size;
        currentList.add("move " + Register.v0 + ", "  + Register.fp.toString());
        currentList.add("addi " + Register.v0+", "+ Register.v0 + ", " + (8 + param_size));

        if (type instanceof StructType) {
            StructInfo structInfo = strcutInfos.get(((StructType)type).struct_Name);
            int structSize = structInfo.size;
            copyValue(expr,Register.v0,structSize);
        } else {
            if(type == BaseType.CHAR){
                currentList.add("sb " + expr.toString() + ", " + "(" + Register.v0 + ")");
            }else {
                currentList.add("sw " + expr.toString() + ", " + "(" + Register.v0+")");
            }
        }
        freeRegister(expr);
        currentList.add("move $sp, $fp");

        // saving all temp register/
        for (Register register : Register.tmpRegs) {
            currentList.add("addi " + "$sp, $sp, -4");
            currentList.add("lw " + register.toString() + ", " + "0($sp)");
        }

        currentList.add("move $sp, $fp");        currentList.add("jr " + Register.ra.toString());
        return Register.v0;

    }
}
