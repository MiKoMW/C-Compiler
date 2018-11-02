package gen;

import java.util.HashMap;

public class StructInfo extends MemoryInfo {

    //public String type;
    //public int size;

    public HashMap<String,Integer> innerDecl;

    public StructInfo (String type, int size, HashMap<String,Integer> innerDecl){
        this.type = type;
        this.size = size;
        this.innerDecl = innerDecl;
    }


}
