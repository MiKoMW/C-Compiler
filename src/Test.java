import lexer.Scanner;
import lexer.Token;

import java.io.File;

public class Test
{
    public static void main(String[] args){

        File fl = new File("test.in");

        try {
            Scanner scanner = new Scanner(fl);


            System.out.println(scanner.peek());
            System.out.println(scanner.peek());


            System.out.print(scanner.next());
            System.out.print(scanner.next());

            System.out.print(scanner.next());

            System.out.println(Token.TokenClass.IDENTIFIER);

            Token to = new Token(null,"Miko11",10,11);


            Token too = new Token(null,1,1);
            System.out.print("hihi");

        }catch (Exception e){
            e.printStackTrace();
        }







    }
}
