package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author cdubach
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;
    public int getErrorCount() {
	return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
        reserve("int",TokenClass.INT);
        reserve("void",TokenClass.VOID);
        reserve("char",TokenClass.CHAR);
        reserve("if",TokenClass.IF);
        reserve("else",TokenClass.ELSE);
        reserve("while",TokenClass.WHILE);
        reserve("return",TokenClass.RETURN);
        reserve("struct",TokenClass.STRUCT);
        reserve("sizeof",TokenClass.SIZEOF);
        reserve('t','\t');
        reserve('b','\b');
        reserve('n','\n');
        reserve('r','\r');
        reserve('f','\f');
        reserve('\'','\'');
        reserve('\\','\\');
        reserve('\"','\"');
        reserve('\0','\0');


    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
	error++;
    }


    public Token nextToken() {
        Token result;
        try {
             result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        }
        return result;
    }

    /*
     * To be completed
     */

    private HashMap<String,Token.TokenClass> words = new HashMap<String,Token.TokenClass>();
    private HashMap<Character,Character> chars = new HashMap<>();

    private void reserve(String w,Token.TokenClass tokclass){
        words.put(w,tokclass);
    }

    private void reserve(Character ch_in, Character ch_out){
        this.chars.put(ch_in,ch_out);
    }


    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();


        // get the next character
        char c = scanner.next();

        // comment return Token？
        if(c == '/') {

            c = scanner.peek();


            // a line comment
            if (c == '/') {
                c = scanner.next();

                c = scanner.next();

                while (c != '\n' && c != '\t') {
                    c = scanner.next();
                }
            }else if (c == '*'){
                boolean looping = true; // conditional for comment
                c = scanner.next();

                while(looping){
                    c = scanner.next();
                    if(c == '*'){
                        c = scanner.peek();
                        if(c == '/'){
                            c = scanner.next();
                            looping = false;
                        }
                    }
                }
            }else {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
        }



        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        // /t and /n未处理


        // recognises the plus operator





        // delimiters
        switch (c){
            case '{': return new Token(TokenClass.LBRA,line,column);
            case '}': return new Token(TokenClass.RBRA,line,column);
            case '(': return new Token(TokenClass.LPAR,line,column);
            case ')': return new Token(TokenClass.RPAR,line,column);
            case '[': return new Token(TokenClass.LSBR,line,column);
            case ']': return new Token(TokenClass.RSBR,line,column);
            case ';': return new Token(TokenClass.SC,line,column);
            case ',': return new Token(TokenClass.COMMA,line,column);
        }

        // operators and dot
        switch (c){
            case '+': return new Token(TokenClass.PLUS,line,column);
            case '-': return new Token(TokenClass.MINUS,line,column);
            case '*': return new Token(TokenClass.ASTERIX,line,column);
            case '/': return new Token(TokenClass.DIV,line,column);
            case '%': return new Token(TokenClass.REM,line,column);
            case '.': return new Token(TokenClass.DOT,line,column);
        }

        switch (c){
            case '&':
                if(scanner.peek() == '&') {
                    c = scanner.next();
                    return new Token(TokenClass.AND,line,column);
                }
                else{
                    error(c, line, column);
                    return new Token(TokenClass.INVALID,line,column);
                }
            case '|':
                if(scanner.peek() == '|') {
                    c = scanner.next();
                    return new Token(TokenClass.OR,line,column);
                }
                else {
                    error(c, line, column);
                    return new Token(TokenClass.INVALID,line,column);
                }
            case '=':
                if(scanner.peek() == '=') {
                    c = scanner.next();
                    return new Token(TokenClass.EQ,line,column);
                }else {
                    return new Token(TokenClass.ASSIGN,line,column);
                }
            case '!':
                if(scanner.peek() == '=') {
                    c = scanner.next();
                    return new Token(TokenClass.NE,line,column);
                }else {
                    error(c, line, column);
                    return new Token(TokenClass.INVALID,line,column);
                }

            case '<':
                if(scanner.peek() == '='){
                    c = scanner.next();
                    new Token(TokenClass.LE,line,column);
                }else {
                    return new Token(TokenClass.LT,line,column);
                }
            case '>':
                if(scanner.peek() == '=') {
                    c = scanner.next();
                    new Token(TokenClass.GE,line,column);
                }else {
                    return new Token(TokenClass.GT,line,column);
                }
        }

        if(c == '#'){
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            c = scanner.peek();
            while (Character.isLetterOrDigit(c)) {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }
            String temp = sb.toString();
            if(temp.equals("#include")){
                return new Token(TokenClass.INCLUDE,line,column);
            }
        }

        if(c == '\''){
            c = scanner.next();

            Character ans = ' ';

            if(c=='\\'){
                c = scanner.next();
                ans = chars.get(c);
                c = scanner.peek();
                if(c!= '\''){
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }

                c = scanner.next();

                if(ans == null){
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }else {
                    return new Token(TokenClass.CHAR_LITERAL,ans.toString(),line,column);
                }
            }else{
                ans = c;

                c = scanner.peek();
                if(c!= '\''){
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }
                c = scanner.next();
                return new Token(TokenClass.CHAR_LITERAL,ans.toString(),line,column);
            }

        }

        if(c == '\"'){
            Character ans = ' ';
            StringBuilder sb = new StringBuilder();
            c = scanner.peek();
            while (c != '\"') {
                c = scanner.next();

                if(c == '\n' || c == '\t'){
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }

                if(c == '\\'){
                    c = scanner.peek();
                    ans = chars.get(c);
                    if(ans == null){
                        c = scanner.next();
                        error(c, line, column);
                        //return new Token(TokenClass.INVALID, line, column);
                    }else{
                        c = scanner.next();
                        sb.append(ans);
                        continue;
                    }
                }

                sb.append(c);
                c = scanner.peek();

            }

            c = scanner.next();
            String st = sb.toString();
            return new Token(TokenClass.STRING_LITERAL,st,line,column);
        }

        if (Character.isDigit(c)) {

            StringBuilder sb = new StringBuilder ();
            sb.append(c);
            c = scanner.peek();
            while (Character.isDigit (c)) {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }
            return new Token(TokenClass.INT_LITERAL, sb.toString(),line,column);
        }

        if (Character.isLetter(c)) {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            c = scanner.peek();
            while (Character.isLetterOrDigit(c) || c == '_') {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }
            String identifier = sb.toString();
            if(words.containsKey(identifier)){
                return new Token(words.get(identifier),sb.toString(),line,column);
            }else{
                return new Token(TokenClass.IDENTIFIER,sb.toString(),line,column);

            }

        }


        // ... to be completed








        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }


}
