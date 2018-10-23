import ast.BaseType;
import lexer.Token;

public class Test
{
    public static void main(String[] args){


        System.out.println(BaseType.VOID );



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
