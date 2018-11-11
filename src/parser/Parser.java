package parser;

import ast.*;
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
        List<StructTypeDecl> structTypeDecls = new LinkedList<>();
        while (accept(TokenClass.STRUCT) && lookAhead(2).tokenClass == TokenClass.LBRA){
            structTypeDecls.add(parseStructdecl());
        }
        return structTypeDecls;
    }

    private StructTypeDecl parseStructdecl() {


        StructType structType = parseStructType();

        List<VarDecl> varDecls = new LinkedList<>();

        expect(TokenClass.LBRA);

        varDecls.add(parseVardecl());

        while (accept(type_First)) {

            varDecls.add(parseVardecl());

        }

        expect(TokenClass.RBRA);
        expect(TokenClass.SC);
        return new StructTypeDecl(structType,varDecls);
    }

    private StructType parseStructType(){
        expect(TokenClass.STRUCT);
        Token cur = expect(TokenClass.IDENTIFIER);
        if(cur == null){
            return new StructType("Invalid!!!!");
        }
        return new StructType(cur.data);
    }


    private List<VarDecl> parseVarDecls() {
        // to be completed ...

        List<VarDecl> varDecls = new LinkedList<>();

        while(is_vardecl()){
            VarDecl varDecl = parseVardecl();
            varDecls.add(varDecl);
        }
        return varDecls;
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
        List<FunDecl> funDecls = new LinkedList<>();
        while(accept(type_First)){
            FunDecl funDecl = parseFundecl();
            funDecls.add(funDecl);

        }
        return funDecls;
    }


    private FunDecl parseFundecl(){

        Type type = parseType();
        Token cur = expect(TokenClass.IDENTIFIER);
        if (cur == null){
            return null;
        }
        String name  = cur.data;

        expect(TokenClass.LPAR);

        List<VarDecl> varDecls = parseParams();

        expect(TokenClass.RPAR);

        Block block = parseBlock();

        return new FunDecl(type,name,varDecls,block) ;
    }

    private List<VarDecl> parseParams(){

        List<VarDecl> varDecls = new LinkedList<>();

        if(accept(type_First)){
            Type type = parseType();
            Token cur = expect(TokenClass.IDENTIFIER);
            if(cur == null){
                error(TokenClass.IDENTIFIER);
                return null;
            }

            varDecls.add(new VarDecl(type,cur.data));
            while (accept(TokenClass.COMMA)) {
                nextToken();
                type = parseType();
                cur = expect(TokenClass.IDENTIFIER);
                if(cur == null){
                    error(TokenClass.IDENTIFIER);
                    return null;
                }
                varDecls.add(new VarDecl(type,cur.data));

            }
        }
        return varDecls;
    }

    private Type parseType(){
        boolean isStruct = false;
        Type type = null;

        isStruct = accept(TokenClass.STRUCT);
        Token cur = expect(type_First);
        if (isStruct){
            Token id = expect(TokenClass.IDENTIFIER);
            if(id == null){
                return null;
            }
            type =  new StructType(id.data);

            if(accept(TokenClass.ASTERIX)){
                nextToken();
                return new PointerType(type);
            }
        }
        if(cur == null){
            return null;
        }
        if(cur.tokenClass == TokenClass.INT){
            type = BaseType.INT;
        }
        if(cur.tokenClass == TokenClass.CHAR){
            type = BaseType.CHAR;
        }
        if(cur.tokenClass == TokenClass.VOID){
            type = BaseType.VOID;
        }
        if(accept(TokenClass.ASTERIX)){
            nextToken();
            return new PointerType(type);
        }
        if(accept(TokenClass.LSBR)){
            nextToken();
            Token size = expect(TokenClass.INT_LITERAL);
            if (size == null){
                return null;
            }
            expect(TokenClass.RSBR);
            return new ArrayType(type,Integer.parseInt(size.data));
        }
        return type;
    }


    //这个问题贼鸡儿大。
    private VarDecl parseVardecl(){


        Type type = parseType();
        Token cur = expect(TokenClass.IDENTIFIER);

        if(cur == null){
            return null;
        }

        if(accept(TokenClass.SC)){
            nextToken();
            return new VarDecl(type,cur.data);
        }else if(accept(TokenClass.LSBR)){
            nextToken();
            Token size = expect(TokenClass.INT_LITERAL);
            expect(TokenClass.RSBR);
            expect(TokenClass.SC);
            if (size == null){
                return null;
            }
            return new VarDecl(new ArrayType(type,Integer.parseInt(size.data)),cur.data);
        }else{
            error(TokenClass.SC, TokenClass.LSBR);
            return null;
        }

    }


    private SizeOfExpr parseSizeof(){
        expect(TokenClass.SIZEOF);
        expect(TokenClass.LPAR);
        Type type = parseType();
        expect(TokenClass.RPAR);
        return new SizeOfExpr(type);
    }


    // this is the primary element which can be in the exp
    // This can not be separated which has the highest precedence.
    private Expr parsePrimary(){
        if(accept(TokenClass.LPAR)){
            nextToken();
            Expr expr = parseExp();
            expect(TokenClass.RPAR);
            return expr;
        }else{
            Token cur = expect(TokenClass.IDENTIFIER, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);
            if(cur == null){
                return null;
            }
            if(cur.tokenClass == TokenClass.IDENTIFIER){
                return new VarExpr(cur.data);
            }
            if(cur.tokenClass == TokenClass.INT_LITERAL){
                return new IntLiteral(Integer.parseInt(cur.data));
            }
            if(cur.tokenClass == TokenClass.CHAR_LITERAL){
                return new ChrLiteral(cur.data.charAt(0));
            }
            if(cur.tokenClass == TokenClass.STRING_LITERAL){
                return new StrLiteral(cur.data);
            }
            return null;
        }
    }

    // Precedence level 1  Function call Array subscripting and structure member access.
    private Expr parsePostfix(){

        boolean isId = accept(TokenClass.IDENTIFIER);

        Token cur = token;

        Expr expr = parsePrimary();

        if((accept(TokenClass.LPAR) && isId)){

                nextToken();
                List<Expr> exprs = new LinkedList<>();
                if (accept(TokenClass.RPAR)) {
                    nextToken();
                    //return;
                } else {
                    Expr param = parseExp();
                    exprs.add(param);
                    while (accept(TokenClass.COMMA)) {
                        nextToken();
                        param = parseExp();
                        exprs.add(param);
                    }

                    expect(TokenClass.RPAR);
                    //return;
                }
                expr =  new FunCallExpr(cur.data,exprs);
            }

        while(accept(TokenClass.LSBR,TokenClass.DOT) ) {

            if (accept(TokenClass.LSBR)) {
                nextToken();
                Expr idx = parseExp();
                expect(TokenClass.RSBR);
                expr = new ArrayAccessExpr(expr,idx);
                //return;
            }else if (accept(TokenClass.DOT)) {
                nextToken();
                Token ident = expect(TokenClass.IDENTIFIER);
                expr = new FieldAccessExpr(expr,ident.data);
                //return;
            }
        }
        //new added.
        //error(TokenClass.INVALID);
        return expr;
    }

    // Unary operation which has precedence 2
    // Unary minus Type cast Pointer indirection Size of size_of_type
    private Expr parseUnary(){
        if(accept(TokenClass.MINUS)){
            nextToken();
            Expr expr = parseTerm();
            //if(!(expr instanceof IntLiteral)){
            //    return null;
            //}

            return new BinOp(new IntLiteral(0),Op.SUB,expr);
        }

        if(accept(TokenClass.ASTERIX)) {
            nextToken();
            Expr expr = parseTerm();
            return new ValueAtExpr(expr);
        }

        if(accept(TokenClass.SIZEOF)){

            Expr expr = parseSizeof();

            return expr;
        }

        Expr expr = parsePostfix();
        return expr;

    }

    // Level 2 recursion makes our grammar support -------1;
    private Expr parseTerm(){

        if(accept(TokenClass.LPAR) && inList(type_First,lookAhead(1).tokenClass)){
            expect(TokenClass.LPAR);
            Type type = parseType();
            expect(TokenClass.RPAR);
            Expr expr = parseTerm();
            return new TypecastExpr(type,expr);
        }else{
            Expr expr = parseUnary();
            return expr;
        }

    }


    // level 3
    private Expr parseExp1(){
        Expr expr1 = parseTerm();
        while(accept(TokenClass.ASTERIX,TokenClass.DIV,TokenClass.REM)){
        if(accept(TokenClass.ASTERIX)){
            nextToken();
            Expr expr2 = parseTerm();
            expr1 = new BinOp(expr1,Op.MUL,expr2);
        }else if(accept(TokenClass.DIV)){
            nextToken();
            Expr expr2 = parseTerm();
            expr1 =  new BinOp(expr1,Op.DIV,expr2);
        }else if(accept(TokenClass.REM)){
            nextToken();
            Expr expr2 = parseTerm();
            expr1 =  new BinOp(expr1,Op.MOD,expr2);
        }
        }
        return expr1;
    }

    // level 4
    private Expr parseExp2(){
        Expr expr1 = parseExp1();
        while (accept(TokenClass.PLUS,TokenClass.MINUS)){
            Expr expr2;
        if(accept(TokenClass.PLUS)) {
            nextToken();
            expr2 = parseExp1();
            expr1 = new BinOp(expr1,Op.ADD,expr2);
        }else if(accept(TokenClass.MINUS)) {
            nextToken();
            expr2 = parseExp1();
            expr1 = new BinOp(expr1,Op.SUB,expr2);
        }
        }
        return expr1;
    }

    // level 5
    private Expr parseExp3(){
        Expr expr1 = parseExp2();
        while(accept(TokenClass.GT,TokenClass.GE,TokenClass.LT,TokenClass.LE)){
            Expr expr2;
        if(accept(TokenClass.GT)){
            nextToken();
            expr2 = parseExp2();
            expr1 = new BinOp(expr1,Op.GT,expr2);
        }else if(accept(TokenClass.GE)){
            nextToken();
            expr2 = parseExp2();
            expr1 = new BinOp(expr1,Op.GE,expr2);
        }else if(accept(TokenClass.LT)){
            nextToken();
            expr2 = parseExp2();
            expr1 = new BinOp(expr1,Op.LT,expr2);
        }else if(accept(TokenClass.LE)){
            nextToken();
            expr2 = parseExp2();
            expr1 = new BinOp(expr1,Op.LE,expr2);
        }
        }
        return expr1;
    }

    // level 6
    private Expr parseExp4(){
        Expr expr1 = parseExp3();
        while(accept(TokenClass.EQ, TokenClass.NE)){
            Expr expr2;
             if (accept(TokenClass.EQ)){
                nextToken();
                expr2 = parseExp3();
                expr1 = new BinOp(expr1,Op.EQ,expr2);
            }else if (accept(TokenClass.NE)){
                nextToken();
                expr2 = parseExp3();
                expr1 = new BinOp(expr1,Op.NE,expr2);
            }
        }
        return expr1;
    }

    // level 7
    private Expr parseExp5(){
        Expr expr1 = parseExp4();
        while(accept(TokenClass.AND)){
            nextToken();
            Expr expr2 = parseExp4();
            expr1 = new BinOp(expr1,Op.AND,expr2);
        }
        return expr1;
    }

    // level 8
    private Expr parseExp(){
        Expr expr1 = parseExp5();
        while (accept(TokenClass.OR)){
            nextToken();
            Expr expr2 = parseExp5();
            expr1 = new BinOp(expr1,Op.OR,expr2);
        }
        return expr1;
    }

    private Block parseBlock(){
        expect(TokenClass.LBRA);

        List<VarDecl> varDecls = new LinkedList<>();
        List<Stmt> stmts = new LinkedList<>();

        while(is_vardecl()){
            VarDecl varDecl = parseVardecl();
            varDecls.add(varDecl);
        }

        while(is_stmt()){
            Stmt stmt = parseStmt();
            stmts.add(stmt);
        }

        expect(TokenClass.RBRA);
        return new Block(varDecls,stmts);
    }

    private boolean is_stmt(){

        boolean ans = false;
        ans = accept(TokenClass.LBRA,TokenClass.WHILE,TokenClass.IF,TokenClass.RETURN);
        ans |= accept(TokenClass.LPAR,TokenClass.ASTERIX,TokenClass.MINUS,TokenClass.SIZEOF);
        ans |= accept(TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.STRING_LITERAL,TokenClass.IDENTIFIER);
        return ans;

    }

    private Stmt parseStmt(){

        if(accept(TokenClass.LBRA)){
            Block block = parseBlock();
            return block;
        }
        if(accept(TokenClass.WHILE)){
            nextToken();
            expect(TokenClass.LPAR);
            Expr expr1 = parseExp();
            expect(TokenClass.RPAR);
            Stmt stmt = parseStmt();
            return new While(expr1,stmt);
        }
        if (accept(TokenClass.IF)){
            nextToken();
            expect(TokenClass.LPAR);
            Expr expr = parseExp();
            expect(TokenClass.RPAR);
            Stmt stmt = parseStmt();
            if(accept(TokenClass.ELSE)){
                nextToken();
                Stmt stmt2 = parseStmt();
                return new If(expr,stmt,stmt2);
            }
            return new If(expr,stmt);
        }

        if (accept(TokenClass.RETURN)){
            nextToken();
            if(!accept(TokenClass.SC)){
                Expr expr = parseExp();
                expect(TokenClass.SC);
                return new Return(expr);
            }else{
                expect(TokenClass.SC);
                return new Return();
            }
        }

        Expr expr1 = parseExp();

        if(accept(TokenClass.ASSIGN)){
            nextToken();
            Expr expr2 = parseExp();
            expect(TokenClass.SC);
            return new Assign(expr1,expr2);
        }else{
            expect(TokenClass.SC);
            return new ExprStmt(expr1);
        }
    }

    // to be completed ...
}