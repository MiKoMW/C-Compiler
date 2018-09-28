import lexer.Scanner;

import java.io.File;

public class Test
{
    public static void main(String[] args){

        File fl = new File("test.in");



        try {
            Scanner sc = new Scanner(fl);

                System.out.println(sc.peek());


        }catch (Exception e){
            e.printStackTrace();
        }







    }
}
