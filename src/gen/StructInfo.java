package gen;

import ast.Type;

import java.util.HashMap;

public class StructInfo extends MemoryInfo {

    //public String type;
    //public int size;

    public HashMap<String,Integer> innerDecl;
    public HashMap<String, Type> typeMapping;

    public StructInfo (String type, int size, HashMap<String,Integer> innerDecl, HashMap<String, Type> typeMapping){
        this.type = type;
        this.size = size;
        this.innerDecl = innerDecl;
        this.typeMapping = typeMapping;
    }


}
