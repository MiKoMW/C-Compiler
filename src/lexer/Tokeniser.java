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
        reserve('0','\0');


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

        // Comment
        if(c == '/') {
            try{
                c = scanner.peek();
            } catch (EOFException e){
                return new Token(TokenClass.DIV, line, column);
            }

            // a line comment
            if (c == '/') {
                c = scanner.next();
                c = scanner.peek();
                while (c != '\n' /*&& c != '\t'*/) {    // \t doesn't stop comment
                    c = scanner.next();
                    c = scanner.peek();
                }
                scanner.next();
                return next();
            }else if (c == '*'){

                boolean looping = true; // conditional for comment

                try {
                    c = scanner.next();
                    while (looping) {
                        c = scanner.next();
                        if (c == '*') {
                            c = scanner.next();
                            if (c == '/') {
                                looping = false;
                            }
                        }
                    }
                } catch (EOFException e){
                    error(c, line, column);
                    return new Token(TokenClass.INVALID,line,column);
                }
                return next();
            }else {
                return new Token(TokenClass.DIV, line, column);  // seems useless.
            }
        }

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

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
            //case '/': return new Token(TokenClass.DIV,line,column);
            case '%': return new Token(TokenClass.REM,line,column);
            case '.': return new Token(TokenClass.DOT,line,column);
        }

        // comparisons and logical operators
        switch (c){
            case '&':
                try{
                if(scanner.peek() == '&') {
                    c = scanner.next();
                    return new Token(TokenClass.AND,line,column);
                }
                else{
                    error(c, line, column);
                    return new Token(TokenClass.INVALID,line,column);
                }
                }catch (EOFException e){
                    error(c, line, column);
                    return new Token(TokenClass.INVALID,line,column);
                }
            case '|':
                try {
                    if (scanner.peek() == '|') {
                        c = scanner.next();
                        return new Token(TokenClass.OR, line, column);
                    } else {
                        error(c, line, column);
                        return new Token(TokenClass.INVALID, line, column);
                    }
                } catch (EOFException e){
                    error(c, line, column);
                    return new Token(TokenClass.INVALID,line,column);
                }
            case '=':
                try{
                    if(scanner.peek() == '=') {
                        c = scanner.next();
                        return new Token(TokenClass.EQ,line,column);
                    }else {
                        return new Token(TokenClass.ASSIGN,line,column);
                    }
                } catch (EOFException e){
                    return new Token(TokenClass.ASSIGN,line,column);
                }
            case '!':
                try{
                    if(scanner.peek() == '=') {
                        c = scanner.next();
                        return new Token(TokenClass.NE,line,column);
                    }else {
                        error(c, line, column);
                        return new Token(TokenClass.INVALID,line,column);
                    }
                } catch(EOFException e){
                    error(c, line, column);
                    return new Token(TokenClass.INVALID,line,column);
                }

            case '<':
                try{
                    if(scanner.peek() == '='){
                        c = scanner.next();
                        return new Token(TokenClass.LE,line,column);
                    }else {
                        return new Token(TokenClass.LT,line,column);
                    }
                }catch (EOFException e){
                    return new Token(TokenClass.LT,line,column);
                }
            case '>':
                try {
                    if(scanner.peek() == '=') {
                        c = scanner.next();
                        return new Token(TokenClass.GE,line,column);
                    }else {
                        return new Token(TokenClass.GT, line, column);
                    }
                } catch (EOFException e){
                    return new Token(TokenClass.GT,line,column);
                }
        }

        // import statement
        if(c == '#'){
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            try{
                c = scanner.peek();
                while (Character.isLetterOrDigit(c)) {
                    sb.append(c);
                    scanner.next();
                    c = scanner.peek();
                }
                String temp = sb.toString();
                if(temp.equals("#include")){
                    return new Token(TokenClass.INCLUDE,line,column);
                }else{
                    error(c,line,column);
                    return new Token(TokenClass.INVALID,line,column);
                }
            } catch (EOFException e){
                String temp = sb.toString();
                if(temp.equals("#include")){
                    return new Token(TokenClass.INCLUDE,line,column);
                }else{
                    error(c,line,column);
                    return new Token(TokenClass.INVALID,line,column);
                }
            }
        }


        // char
        if(c == '\''){
            try{
                c = scanner.peek();
                Character ans = ' ';

                if(c == '\''){
                    error(c, line, column);
                    scanner.next();
                    return new Token(TokenClass.INVALID,line,column);
                }

            if(c=='\\') {
                c = scanner.next();
                c = scanner.peek();
                ans = chars.get(c);

                //gcc 不error
                if (ans == null) {
                    ans = c;
                    error(c, line, column);
                }
                c = scanner.next();
            }else {
                c = scanner.next();
                ans = c;
            }

            c = scanner.peek();

            if( c!= '\''){
                error(c,line,column);
            }

            while(c != '\'') {
                ans = c;
                c = scanner.next();
                c = scanner.peek();
            }

            c = scanner.next();

            return new Token(TokenClass.CHAR_LITERAL,ans.toString(),line,column);
            } catch (EOFException e){
                error(c,line,column);
                return new Token(TokenClass.INVALID,line,column);
            }
        }

        // string
        if(c == '\"'){
            Character ans = ' ';
            StringBuilder sb = new StringBuilder();
            try{
            c = scanner.peek();
            while (c != '\"') {

                c = scanner.next();

                if(c == '\n' /*|| c == '\t'*/){
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }

                if(c == '\\'){
                    c = scanner.next();
                    ans = chars.get(c);
                    if(ans == null){
                        ans = c;
                        //c = scanner.next();
                        //gcc don't raise error
                        error(c, line, column);
                        //return new Token(TokenClass.INVALID, line, column);
                    }
                    sb.append(ans);
                }else{
                    sb.append(c);
                }
                c = scanner.peek();
            }

            c = scanner.next();
            String st = sb.toString();
            return new Token(TokenClass.STRING_LITERAL,st,line,column);
            } catch (EOFException e){
                error(c,line,column);
                return new Token(TokenClass.INVALID,line,column);
            }
        }

        if (Character.isDigit(c)) {

            StringBuilder sb = new StringBuilder ();
            sb.append(c);
            try{
            c = scanner.peek();
            while (Character.isDigit (c)) {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }
            return new Token(TokenClass.INT_LITERAL, sb.toString(),line,column);
            }catch(EOFException e){
                return new Token(TokenClass.INT_LITERAL, sb.toString(),line,column);
            }

        }

        if (Character.isLetter(c) || c == '_') {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            try{
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
            }catch(EOFException e){
                String identifier = sb.toString();
                if(words.containsKey(identifier)){
                    return new Token(words.get(identifier),sb.toString(),line,column);
                }else {
                    return new Token(TokenClass.IDENTIFIER, sb.toString(), line, column);

                }
            }

        }


        // ... to be completed








        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }


}
