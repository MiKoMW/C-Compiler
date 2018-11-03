package gen;

public class VarInfo extends MemoryInfo{

    public int stack_offset;

    public VarInfo(String type, int size, int currentOffset){

        this.type = type;
        this.size = size;
        this.stack_offset = currentOffset;


    }


}
