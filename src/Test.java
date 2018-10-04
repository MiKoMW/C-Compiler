import lexer.Scanner;
import lexer.Token;
import lexer.Tokeniser;
import parser.Parser;

import java.io.File;

public class Test
{
    public static void main(String[] args){

        File fl = new File("test.in");


        try {

            Scanner scanner = new Scanner(fl);

            Tokeniser tok = new Tokeniser(scanner);
            Parser ps = new Parser(tok);



            error();

        } catch (Exception e){
            System.out.println(e.toString());
        }

        Token.TokenClass[] Type_First = new Token.TokenClass[]{
                Token.TokenClass.INT, Token.TokenClass.CHAR, Token.TokenClass.VOID, Token.TokenClass.STRUCT
        };





    }

    private static void error(Token.TokenClass... expected) {


        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Token.TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected ("+sb);

    }

    public static void test1(Integer... as){



        for(Integer a : as){
            System.out.println(a);
        }



    }

}
