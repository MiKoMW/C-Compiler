
import lexer.Scanner;
import lexer.Token;
import lexer.Tokeniser;
import parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;

public class Comp_Tester {


    private static final int FILE_NOT_FOUND = 2;
    private static final int MODE_FAIL      = 254;
    private static final int LEXER_FAIL     = 250;
    private static final int PARSER_FAIL    = 245;
    private static final int SEM_FAIL       = 240;
    private static final int PASS           = 0;

    private enum Mode {
        LEXER, PARSER, AST, SEMANTICANALYSIS, GEN
    }

    public static void main(String[] args){
        // for easy testing
            String pathname = args[0];
            String mode = args[1];
            File file = new File(pathname);
            String[] ans = file.list();
            for(String st : ans){
                System.out.println("Test case: " + st);

                if(!st.contains(".c")){
                   continue;
                }
                String[] temp = new String[]{mode,pathname + st,"test.out"};
                main_test(temp);

            }

    }

    public static void main_test(String[] args) {


        Mode mode = null;
        switch (args[0]) {
            case "-lexer": mode = Mode.LEXER; break;	case "-parser": mode = Mode.PARSER; break;
            case "-ast":   mode = Mode.AST; break;		case "-sem":    mode = Mode.SEMANTICANALYSIS; break;
            case "-gen":   mode = Mode.GEN; break;
            default:
                break;
        }

        File inputFile = new File(args[1]);
        File outputFile = new File(args[2]);

        Scanner scanner;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File "+inputFile.toString()+" does not exist.");
            System.exit(FILE_NOT_FOUND);
            return;
        }

        Tokeniser tokeniser = new Tokeniser(scanner);
        if (mode == Mode.LEXER) {
            for (Token t = tokeniser.nextToken(); t.tokenClass != Token.TokenClass.EOF; t = tokeniser.nextToken())
                System.out.println(t);
            if (tokeniser.getErrorCount() == 0)
                System.out.println("Lexing: pass");
            else
                System.out.println("Lexing: failed ("+tokeniser.getErrorCount()+" errors)");
            //System.exit(tokeniser.getErrorCount() == 0 ? PASS : LEXER_FAIL);
        } else if (mode == Mode.PARSER) {
            Parser parser = new Parser(tokeniser);
            parser.parse();
            if (parser.getErrorCount() == 0)
                System.out.println("Parsing: pass");
            else
                System.out.println("Parsing: failed ("+parser.getErrorCount()+" errors)");
           // System.exit(parser.getErrorCount() == 0 ? PASS : PARSER_FAIL);
        }  else if (mode == Mode.AST) {
            System.exit(MODE_FAIL);
        } else if (mode == Mode.SEMANTICANALYSIS) {
            System.exit(MODE_FAIL);
        } else if (mode == Mode.GEN) {
            System.exit(MODE_FAIL);
        } else {
            System.exit(MODE_FAIL);
        }
    }


}
