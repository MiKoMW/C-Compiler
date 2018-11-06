package gen;

import ast.*;

import java.util.HashMap;

public class OffSetVisitor implements ASTVisitor<Integer> {


    // This visitor is awesome! I like this visitor!

    private HashMap<String,StructInfo> strcutInfos = new HashMap<>();

    private boolean isMain;

    private int current_Stack_offset;

    boolean globalLevel;


    public OffSetVisitor(){
        isMain = false;
        current_Stack_offset = 0;
        globalLevel = true;
    }

    public HashMap<String,StructInfo> getStrcutInfos(){
        return this.strcutInfos;
    }

    @Override
    public Integer visitBaseType(BaseType bt) {
        if(bt == BaseType.CHAR){
            return 1;
        }
        if(bt == BaseType.INT){
            return 4;
        }
        return 0;
    }

    // 目测没啥大毛病。
    @Override
    public Integer visitStructTypeDecl(StructTypeDecl st) {

        String type = st.struct_type.struct_Name;
        HashMap<String,Integer> innerVars = new HashMap<>();
        HashMap<String,Type> typeHashMap = new HashMap<>();

        int con = 0;

        for(VarDecl varDecl : st.varDecls){

            innerVars.put(varDecl.varName,con);
            typeHashMap.put(varDecl.varName,varDecl.var_type);
            if(varDecl.var_type instanceof StructType){
                StructInfo structInfo = strcutInfos.get(((StructType) varDecl.var_type).struct_Name);
                if (structInfo == null){
                    System.err.println("Something must be wrong!!");
                    return null;
                }
                con += structInfo.size;
            } else if(varDecl.var_type == BaseType.CHAR){
                con+=4;
            } else if(varDecl.var_type instanceof ArrayType){
                if(((ArrayType)varDecl.var_type).elem_type == BaseType.CHAR){
                    // if char, we make it word aligned! 目测是对的。
                    int temp = ((ArrayType)varDecl.var_type).size;
                    if((temp % 4) != 0){
                        temp -= (temp % 4);
                        temp += 8;
                    }
                    con += temp;
                }else if(((ArrayType)varDecl.var_type).elem_type instanceof StructType){

                    StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) varDecl.var_type).elem_type).struct_Name);

                    if (structInfo == null){
                        System.err.println("Something must be wrong!!");
                        return null;
                    }

                    if((structInfo.size % 4) != 0){
                        System.err.println("Struct type size not correct. Should be word aligned!");
                    }

                    con += structInfo.size * ((ArrayType)varDecl.var_type).size;
                }else {
                    con += ((ArrayType)varDecl.var_type).size * 4;
                }
            }else{
                con += 4;
            }
        }
        strcutInfos.put(type,new StructInfo(type,con,innerVars,typeHashMap));
        return con;
    }

    @Override
    public Integer visitBlock(Block b) {

        int temp = current_Stack_offset;

        for(VarDecl varDecl : b.vardelcs){
            varDecl.stack_offset = current_Stack_offset;
            varDecl.memo_size = varDecl.accept(this);
            current_Stack_offset += varDecl.memo_size;
        }

        for(Stmt stmt : b.stmts){
            //System.err.println(":)");
            //System.err.println(stmt);
            stmt.accept(this);
        }

        return current_Stack_offset - temp;

    }

    @Override
    public Integer visitFunDecl(FunDecl p) {

        current_Stack_offset = 0;

        int param_offset = 4;
        for(VarDecl varDecl : p.params){
            if(varDecl.var_type instanceof StructType){
                varDecl.isStatic = false;
                varDecl.memo_size = strcutInfos.get(((StructType) varDecl.var_type).struct_Name).size;
                varDecl.stack_offset = param_offset;
                param_offset += varDecl.memo_size;
            }else{
                varDecl.isStatic = false;
                varDecl.stack_offset = param_offset;
                //这不太对。到时候改。反正这个class不做offset的工作。
                varDecl.memo_size = 4;
                param_offset += varDecl.memo_size;
            }
        }


        current_Stack_offset += p.block.accept(this);

        return current_Stack_offset;
    }

    @Override
    public Integer visitProgram(Program p) {

        globalLevel = true;
        isMain = false;

        for(StructTypeDecl structTypeDecl : p.structTypeDecls){
            structTypeDecl.accept(this);
        }

        for(VarDecl varDecl : p.varDecls){
            varDecl.isStatic = true;
            varDecl.stack_offset = -1;
            varDecl.accept(this);
        }

        globalLevel = false;

        for(FunDecl funDecl : p.funDecls){
            if(funDecl.name.equals("main")){
                isMain = true;
            }
            funDecl.accept(this);
            isMain = false;
        }

        return 0;
    }

    @Override
    public Integer visitVarDecl(VarDecl vd) {

        if (vd.var_type instanceof StructType) {

            String structName = ((StructType) vd.var_type).struct_Name;
            vd.memo_size = strcutInfos.get(structName).size;

        } else if (vd.var_type instanceof ArrayType) {

            int size = ((ArrayType) vd.var_type).size;

            if (((ArrayType) vd.var_type).elem_type == BaseType.CHAR) {
                int temp = size;
                if((temp % 4) != 0){
                    temp -= (temp % 4);
                    temp += 8;
                }
                vd.memo_size = temp;
            } else if (((ArrayType) vd.var_type).elem_type instanceof StructType) {

                StructInfo structInfo = strcutInfos.get(((StructType) ((ArrayType) vd.var_type).elem_type).struct_Name);
                if (structInfo == null) {
                    System.err.println("Something must be wrong!!");
                    return null;
                }
                vd.memo_size = structInfo.size * size;

            } else {
                vd.memo_size = size * 4;
            }
        } else {
            vd.memo_size = 4;
        }

        vd.isStatic = globalLevel;

        //vd.stack_offset = current_Stack_offset;
        //current_Stack_offset +=  vd.memo_size;

        return vd.memo_size;

    }

    @Override
    public Integer visitVarExpr(VarExpr v) {
        return null;
    }

    @Override
    public Integer visitPointerType(PointerType v) {
        return 4;
    }

    @Override
    public Integer visitStructType(StructType v) {
        return null;
    }

    @Override
    public Integer visitArrayType(ArrayType v) {

        int size = v.size;

        if (v.elem_type == BaseType.CHAR) {
            int temp = size;
            if((temp % 4) != 0){
                temp -= (temp % 4);
                temp += 8;
            }
            return temp;
        } else if (v.elem_type instanceof StructType) {

            StructInfo structInfo = strcutInfos.get(((StructType) v.elem_type).struct_Name);
            if (structInfo == null) {
                System.err.println("Something must be wrong!!");
                return null;
            }
            return structInfo.size * size;

        } else {
            return 4 * size;
        }
    }

    @Override
    public Integer visitIntLiteral(IntLiteral v) {
        return 4;
    }

    @Override
    public Integer visitStrLiteral(StrLiteral v) {
        int temp = v.value.length();
        if((temp % 4) != 0){
            temp -= (temp % 4);
            temp += 8;
        }
        return temp;
    }

    @Override
    public Integer visitChrLiteral(ChrLiteral v) {
        return 4;
    }

    @Override
    public Integer visitFunCallExpr(FunCallExpr v) {
        for(Expr expr : v.params){
            expr.accept(this);
        }
        return 0;
    }

    @Override
    public Integer visitBinOp(BinOp v) {
        v.first.accept(this);
        v.second.accept(this);
        return 0;
    }

    @Override
    public Integer visitOp(Op v) {
        v.accept(this);
        return 0;
    }

    @Override
    public Integer visitArrayAccessExpr(ArrayAccessExpr v) {
        v.type.accept(this);
        v.array.accept(this);
        v.index.accept(this);
        return 0;
    }

    @Override
    public Integer visitFieldAccessExpr(FieldAccessExpr v) {
        v.struct.accept(this);
        return 0;
    }

    @Override
    public Integer visitValueAtExpr(ValueAtExpr v) {
        v.expr.accept(this);
        return 0;
    }

    @Override
    public Integer visitSizeOfExpr(SizeOfExpr v) {
        v.size_of_type.accept(this);
        return 0;
    }

    @Override
    public Integer visitTypecastExpr(TypecastExpr v) {
        v.cast_type.accept(this);
        v.expr.accept(this);
        return 0;
    }

    @Override
    public Integer visitExprStmt(ExprStmt v) {
        v.expr.accept(this);
        return 0;
    }

    @Override
    public Integer visitWhile(While v) {
        v.expr.accept(this);
        v.stmt.accept(this);
        return 0;
    }

    @Override
    public Integer visitIf(If v) {
        v.expr.accept(this);
        v.stmt1.accept(this);
        if(v.stmt2!= null){
            v.stmt2.accept(this);
        }
        return 0;
    }

    @Override
    public Integer visitAssign(Assign v) {
        v.lhs.accept(this);
        v.rhs.accept(this);
        return 0;
    }

    @Override
    public Integer visitReturn(Return v) {
        if (v.expr != null)
        v.expr.accept(this);
        return 0;
    }
}
