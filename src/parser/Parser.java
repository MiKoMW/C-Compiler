package parser;

import ast.FunDecl;
import ast.Program;
import ast.StructTypeDecl;
import ast.VarDecl;
import lexer.Token;
import lexer.Token.TokenClass;
import lexer.Tokeniser;

import java.util.LinkedList;
import java.util.List;
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

    public Program parse() {
        // get the first token
        nextToken();

        return parseProgram();
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

    // test weather a token class is in the list of classes
    private boolean inList(TokenClass[] expected, TokenClass tokClass){
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == tokClass);
        return result;
    }

    private Program parseProgram() {
        parseIncludes();
        List<StructTypeDecl> stds = parseStructDecls();
        List<VarDecl> vds = parseVarDecls();
        List<FunDecl> fds = parseFunDecls();
        expect(TokenClass.EOF);
        return new Program(stds, vds, fds);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private List<StructTypeDecl> parseStructDecls() {
        // to be completed ...

        while (accept(TokenClass.STRUCT) && lookAhead(2).tokenClass == TokenClass.LBRA){
            parseStructdecl();
        }
        return null;
    }

    private void parseStructdecl() {
        parseStructType();
        expect(TokenClass.LBRA);
        parseVardecl();

        while (accept(type_First)) {
            parseVardecl();
        }

        expect(TokenClass.RBRA);
        expect(TokenClass.SC);
        return ;
    }

    private void parseStructType(){
        expect(TokenClass.STRUCT);
        expect(TokenClass.IDENTIFIER);
        return;
    }


    private List<VarDecl> parseVarDecls() {
        // to be completed ...

        while(is_vardecl()){
            parseVardecl();

        }
        return null;
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


    private List<FunDecl> parseFunDecls() {
        while(accept(type_First)){
            parseFundecl();
        }
        return null;
    }


    private void parseFundecl(){
        parseType();
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);

        parseParams();

        expect(TokenClass.RPAR);

        parseBlock();
        return;
    }

    private void parseParams(){
        if(accept(type_First)){
            parseType();
            expect(TokenClass.IDENTIFIER);
            while (accept(TokenClass.COMMA)) {
                nextToken();
                parseType();
                expect(TokenClass.IDENTIFIER);
            }
        }
        return;
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


    private void parseSizeof(){
        expect(TokenClass.SIZEOF);
        expect(TokenClass.LPAR);
        parseType();
        expect(TokenClass.RPAR);
        return;
    }


    // this is the primary element which can be in the exp
    // This can not be separated which has the highest precedence.
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

    // Precedence level 1  Function call Array subscripting and structure member access.
    private void parsePostfix(){

        boolean isId = accept(TokenClass.IDENTIFIER);

        parsePrimary();

        if((accept(TokenClass.LPAR) && isId)){

                nextToken();

                if (accept(TokenClass.RPAR)) {
                    nextToken();
                    //return;
                } else {
                    parseExp();
                    while (accept(TokenClass.COMMA)) {
                        nextToken();
                        parseExp();
                    }

                    expect(TokenClass.RPAR);
                    //return;
                }
                return;
            }

        while(accept(TokenClass.LSBR,TokenClass.DOT) ) {

            if (accept(TokenClass.LSBR)) {
                nextToken();
                parseExp();
                expect(TokenClass.RSBR);
                //return;
            }else if (accept(TokenClass.DOT)) {
                nextToken();
                expect(TokenClass.IDENTIFIER);
                //return;
            }
        }

        return ;
    }

    // Unary operation which has precedence 2
    // Unary minus Type cast Pointer indirection Size of type
    private void parseUnary(){
        if(accept(TokenClass.MINUS)){
            nextToken();
            parseTerm();
            return;
        }

        if(accept(TokenClass.ASTERIX)) {
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

    // Level 2 recursion makes our grammar support -------1;
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


    // level 3
    private void parseExp1(){
        parseTerm();
        while(accept(TokenClass.ASTERIX,TokenClass.DIV,TokenClass.REM)){
        if(accept(TokenClass.ASTERIX)){
            nextToken();
            parseTerm();
            //return;
        }else if(accept(TokenClass.DIV)){
            nextToken();
            parseTerm();
            //return;
        }else if(accept(TokenClass.REM)){
            nextToken();
            parseTerm();
            //return;
        }}
        return;
    }

    // level 4
    private void parseExp2(){
        parseExp1();
        while (accept(TokenClass.PLUS,TokenClass.MINUS)){
        if(accept(TokenClass.PLUS)) {
            nextToken();
            parseExp1();
            //return;
        }else if(accept(TokenClass.MINUS)) {
            nextToken();
            parseExp1();
            //return;
        }
        }
        return;
    }

    // level 5
    private void parseExp3(){
        parseExp2();
        while(accept(TokenClass.GT,TokenClass.GE,TokenClass.LT,TokenClass.LE)){
        if(accept(TokenClass.GT)){
            nextToken();
            parseExp2();
            //return;
        }else if(accept(TokenClass.GE)){
            nextToken();
            parseExp2();
            //return;
        }else if(accept(TokenClass.LT)){
            nextToken();
            parseExp2();
            //return;
        }else if(accept(TokenClass.LE)){
            nextToken();
            parseExp2();
            //return;
        }
        }
        return;
    }

    // level 6
    private void parseExp4(){
        parseExp3();
        while(accept(TokenClass.EQ, TokenClass.NE)){
             if (accept(TokenClass.EQ)){
                nextToken();
                parseExp3();
                //return;
            }else if (accept(TokenClass.NE)){
                nextToken();
                parseExp3();
                //return;
            }
        }
        return;
    }

    // level 7
    private void parseExp5(){
        parseExp4();
        while(accept(TokenClass.AND)){
            nextToken();
            parseExp4();
            //return;
        }
        return;
    }

    // level 8
    private void parseExp(){
        parseExp5();
        while (accept(TokenClass.OR)){
            nextToken();
            parseExp5();
            //return;
        }
        return;
    }

    private void parseBlock(){
        expect(TokenClass.LBRA);

        while(is_vardecl()){
            parseVardecl();
        }

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




    private void parseStmt(){

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

        parseExp();

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
