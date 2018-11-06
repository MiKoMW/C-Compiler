public class Test
{
    public static void main(String[] args){


        int temp = 11;
        if((temp % 4) != 0){
            temp -= (temp % 4);
            temp += 4;
        }
        System.out.print(temp);


    }



}
