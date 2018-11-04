import gen.Register;

public class Test
{
    public static void main(String[] args){


        int tempIdx = -27 * 4;

        // saving all temp register/
        for (Register register : Register.tmpRegs) {
            tempIdx += 4;
        }

        for(Register register : Register.paramRegs){
            tempIdx += 4;
        }

        Register register = Register.v0;
        tempIdx += 4;

        register = Register.gp;
        tempIdx += 4;


        register = Register.ra;
        tempIdx += 4;

        register = Register.sp;
        tempIdx += 4;

        register = Register.fp;
        tempIdx += 4;
        System.out.print(tempIdx);


    }


}
