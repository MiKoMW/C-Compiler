package parser;

import lexer.Token;
import lexer.Token.TokenClass;
import lexer.Tokeniser;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author cdubach
 */
public class Parser {

    private Token token;

    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;

    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public void parse() {
        // get the first token
        nextToken();

        parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;

        int cnt=1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return null;
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return cur;
            }
        }

        error(expected);
        return null;
    }

    /*
    * Returns true if the current token is equals to any of the expected ones.
    */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }

    TokenClass[] type_First = new TokenClass[]{
            TokenClass.INT,TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT
    };

    private boolean inList(TokenClass[] expected, TokenClass tokClass){
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == tokClass);
        return result;
    }


    private void parseProgram() {
        parseIncludes();
        //System.out.println(token.tokenClass);
        //System.out.println(token.position);
        //System.out.println("includeOK");
        parseStructDecls();
        //System.out.println(token.tokenClass);
        //System.out.println(token.position);
        //System.out.println("StrOK");
        parseVarDecls();
        //System.out.println(token.tokenClass);
        //System.out.println(token.position);
        //System.out.println("VarOK");
        parseFunDecls();
        //System.out.println(token.tokenClass);
        //System.out.println(token.position);
        //System.out.println("FunOK");
        expect(TokenClass.EOF);
    }

    // includes are ignored, so does not need to return an AST node
    // *
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private void parseStructDecls() {
        // to be completed ...

        while (accept(TokenClass.STRUCT)){
            parseStructdecl();
        }
        return;
    }

    private void parseVarDecls() {
        // to be completed ...

        while(is_vardecl()){
            parseVardecl();

        }
        return;
    }

    private boolean is_vardecl(){

        boolean ans = false;
        boolean isStruct = false;

        ans |= (lookAhead(1).tokenClass == TokenClass.ASTERIX && lookAhead(2).tokenClass == TokenClass.IDENTIFIER &&
                (lookAhead(3).tokenClass == TokenClass.SC || lookAhead(3).tokenClass == TokenClass.LSBR));

        ans |= (lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                (lookAhead(2).tokenClass == TokenClass.SC || lookAhead(2).tokenClass == TokenClass.LSBR));



        ans &= accept(type_First);

        isStruct |= (lookAhead(2).tokenClass == TokenClass.ASTERIX && lookAhead(3).tokenClass == TokenClass.IDENTIFIER &&
                (lookAhead(4).tokenClass == TokenClass.SC || lookAhead(4).tokenClass == TokenClass.LSBR));

        isStruct |= (lookAhead(2).tokenClass == TokenClass.IDENTIFIER &&
                (lookAhead(3).tokenClass == TokenClass.SC || lookAhead(3).tokenClass == TokenClass.LSBR));


        isStruct &= accept(TokenClass.STRUCT) && lookAhead(1).tokenClass == TokenClass.IDENTIFIER;

        ans |= isStruct;

        return ans;

    }


    private void parseFunDecls() {
        while(accept(type_First)){
            //System.out.println(token.tokenClass);
            //System.out.println(token.position);
            //System.out.println("FarseFunDelc");

            parseFundecl();
        }
        return;
        // to be completed ...
    }

    private void parseType(){
        boolean isStruct = false;
        isStruct = accept(TokenClass.STRUCT);
        expect(type_First);
        if (isStruct){
            expect(TokenClass.IDENTIFIER);
        }
        if(accept(TokenClass.ASTERIX)){
            nextToken();
        }
        return;
    }

    private void parseStructType(){
        expect(TokenClass.STRUCT);
        expect(TokenClass.IDENTIFIER);
        return;
    }

    private void parseParams(){
        if(accept(type_First)){
            nextToken();
            expect(TokenClass.IDENTIFIER);
            while (accept(TokenClass.COMMA)) {
                nextToken();
                parseType();
                expect(TokenClass.IDENTIFIER);
            }
        }
        return;


    }

    private void parseVardecl(){

        parseType();


        expect(TokenClass.IDENTIFIER);

        if(accept(TokenClass.SC)){
            nextToken();
            return;
        }else if(accept(TokenClass.LSBR)){
            nextToken();
            expect(TokenClass.INT_LITERAL);
            expect(TokenClass.RSBR);

            expect(TokenClass.SC);
            return;
        }else{
            error(TokenClass.SC, TokenClass.LSBR);
            return;
        }

    }

    private void parseStructdecl() {
        parseStructType();
        expect(TokenClass.LBRA);
        parseVardecl();


        while (accept(type_First)) {
            //System.out.println("********");
            //System.out.println(token);
            //System.out.println(token.position);
            parseVardecl();
            //System.out.println("********");

        }

        expect(TokenClass.RBRA);
        expect(TokenClass.SC);
        return;
    }

    private void parseSizeof(){
        expect(TokenClass.SIZEOF);
        expect(TokenClass.LPAR);
        parseType();
        expect(TokenClass.RPAR);
        return;
    }


    private void parsePrimary(){
        if(accept(TokenClass.LPAR)){
            nextToken();
            parseExp();
            expect(TokenClass.RPAR);
            return;
        }else{
            expect(TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);
            return;
        }
    }

    private void parsePostfix(){
        boolean isId = accept(TokenClass.IDENTIFIER);



        parsePrimary();



        while(accept(TokenClass.LSBR,TokenClass.DOT) || (accept(TokenClass.LPAR) && isId)) {
            //System.out.println(token);
            //System.out.println(token.position);

            if (accept(TokenClass.LSBR)) {
                nextToken();
                parseExp();
                expect(TokenClass.RSBR);
                //return;
            }else if (accept(TokenClass.DOT)) {
                nextToken();
                expect(TokenClass.IDENTIFIER);
                //return;
            }else if (accept(TokenClass.LPAR) && isId) {
                nextToken();
                if (accept(TokenClass.RPAR)) {
                    nextToken();
                    //return;
                } else {
                    //System.out.println("****");

                    //System.out.println(token);

                    parseExp();
                    //System.out.println("****");
                    //System.out.println(token);


                    while (accept(TokenClass.COMMA)) {
                        nextToken();
                        parseExp();
                    }

                    expect(TokenClass.RPAR);
                    //return;
                }
            }
            //isId = accept(TokenClass.IDENTIFIER);
        }

        return ;
    }

    private void parseUnary(){
        if(accept(TokenClass.MINUS)){
            nextToken();
            parseTerm();
            return;
        }

        if(accept(TokenClass.ASTERIX)){
            nextToken();
            parseTerm();
            return;
        }

        if(accept(TokenClass.SIZEOF)){
            parseSizeof();
            return;
        }

        parsePostfix();
        return;

    }

    private void parseTerm(){


        if(accept(TokenClass.LPAR) && inList(type_First,lookAhead(1).tokenClass)){
            expect(TokenClass.LPAR);
            parseType();
            expect(TokenClass.RPAR);
            parseTerm();

            return;
        }else{
            parseUnary();
            return;
        }

    }

    private void parseExp1(){
        parseTerm();
        if(accept(TokenClass.ASTERIX)){
            nextToken();
            parseTerm();
            return;
        }

        if(accept(TokenClass.DIV)){
            nextToken();
            parseTerm();
            return;
        }

        if(accept(TokenClass.REM)){
            nextToken();
            parseTerm();
            return;
        }
        return;
    }

    private void parseExp2(){
        parseExp1();
        if(accept(TokenClass.PLUS)) {
            nextToken();
            parseExp1();
            return;
        }

        if(accept(TokenClass.MINUS)) {
            nextToken();
            parseExp1();
            return;
        }
        return;
    }

    private void parseExp(){
        parseExp2();
        if(accept(TokenClass.GT)){
            nextToken();
            parseExp2();
            return;
        }
        if(accept(TokenClass.GE)){
            nextToken();
            parseExp2();
            return;
        }
        if(accept(TokenClass.LT)){
            nextToken();
            parseExp2();
            return;
        }
        if(accept(TokenClass.LE)){
            nextToken();
            parseExp2();
            return;
        }
        if (accept(TokenClass.EQ)){
            nextToken();
            parseExp2();
            return;
        }
        if (accept(TokenClass.NE)){
            nextToken();
            parseExp2();
            return;
        }
        return;
    }

    private void parseBlock(){
        expect(TokenClass.LBRA);

        while(is_vardecl()){
            parseVardecl();
        }

        //System.out.println("Stmt");
        //System.out.println(token.tokenClass);
        //System.out.println(token.position);
        //System.out.println(token);

        while(is_stmt()){
            parseStmt();
        }
        expect(TokenClass.RBRA);
        return;
    }

    private boolean is_stmt(){

        boolean ans = false;
        ans = accept(TokenClass.LBRA,TokenClass.WHILE,TokenClass.IF,TokenClass.RETURN);
        ans |= accept(TokenClass.LPAR,TokenClass.ASTERIX,TokenClass.MINUS,TokenClass.SIZEOF);
        ans |= accept(TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.STRING_LITERAL,TokenClass.IDENTIFIER);
        return ans;

    }

    private void parseFundecl(){

        //System.out.println(":)");


        parseType();
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);


        parseParams();


        expect(TokenClass.RPAR);

        parseBlock();


        return;

    }


    private void parseStmt(){

        //System.out.println(token);


        if(accept(TokenClass.LBRA)){
            parseBlock();
            return;
        }
        if(accept(TokenClass.WHILE)){
            nextToken();
            expect(TokenClass.LPAR);
            parseExp();
            expect(TokenClass.RPAR);
            parseStmt();
            return;
        }
        if (accept(TokenClass.IF)){
            nextToken();
            expect(TokenClass.LPAR);
            parseExp();
            expect(TokenClass.RPAR);
            parseStmt();
            if(accept(TokenClass.ELSE)){
                nextToken();
                parseStmt();
            }
            return;
        }

        if (accept(TokenClass.RETURN)){
            nextToken();
            if(!accept(TokenClass.SC)){
                parseExp();
                expect(TokenClass.SC);
                return;
            }else{
                expect(TokenClass.SC);
                return;
            }
        }

        //System.out.println(token.tokenClass);
        //System.out.println(token.position);
        //System.out.println("1");

        parseExp();

        //System.out.println(token.tokenClass);
        //System.out.println(token.position);
        //System.out.println("2");

        if(accept(TokenClass.ASSIGN)){
            nextToken();

            parseExp();

            expect(TokenClass.SC);
            return;
        }else{
            expect(TokenClass.SC);
            return;
        }

    }










    // to be completed ...
}
