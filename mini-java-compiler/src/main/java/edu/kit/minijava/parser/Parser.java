package edu.kit.minijava.parser;

import edu.kit.minijava.lexer.*;

import java.io.*;
import java.util.*;

public final class Parser {

    // MARK: - Initialization

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private final Lexer lexer;

    // MARK: - Parsing

    /// A buffer of tokens returned by the lexer that have not yet been consumed.
    /// Do not access directly.
    private final List<Token> tokens = new ArrayList<>();

    /// Do not access directly.
    private boolean hasReceivedEndOfInputFromLexer = false;

    private Token getCurrentToken() throws ParserException {
        return this.getTokenAtOffset(0);
    }

    private Token getTokenAtOffset(int offset) throws ParserException {
        try {
            while (this.tokens.size() <= offset && !this.hasReceivedEndOfInputFromLexer) {
                Token token = this.lexer.nextToken();

                if (token != null) {
                    this.tokens.add(token);
                } else {
                    this.hasReceivedEndOfInputFromLexer = true;
                    break;
                }
            }
        }
        catch (LexerException | IOException exception) {
            throw new PropagatedException(exception);
        }

        if (offset < this.tokens.size()) {
            return this.tokens.get(offset);
        } else {
            return null;
        }
    }

    private boolean hasReachedEndOfInput() throws ParserException {
        return this.getCurrentToken() == null;
    }

    private boolean lookahead(TokenType first, TokenType... others) throws ParserException {
        if (!first.matches(this.getCurrentToken())) {
            return false;
        }

        for (int index = 0; index < others.length; index += 1) {
            if (!others[index].matches(this.getTokenAtOffset(index + 1))) {
                return false;
            }
        }

        return true;
    }

    private Token consume(TokenType type, String context) throws ParserException {
        Token token = this.getCurrentToken();

        if (token == null || token.type != type) {
            throw new UnexpectedTokenException(token, context, type);
        }

        this.tokens.remove(0);

        return token;
    }

    // MARK: - Parsing MiniJava Files

    public Program parseProgram() throws ParserException {
        List<ClassDeclaration> classes = new ArrayList<>();

        while (!this.hasReachedEndOfInput()) {
            classes.add(this.parseClassDeclaration());
        }

        return new Program(classes);
    }

    private ClassDeclaration parseClassDeclaration() throws ParserException {
        this.consume(TokenType.CLASS, "ClassDeclaration");

        String name = this.consume(TokenType.IDENTIFIER, "ClassDeclaration").text;
        List<ClassMember> members = new ArrayList<>();

        this.consume(TokenType.OPENING_BRACE, "ClassDeclaration");

        while (!this.lookahead(TokenType.CLOSING_BRACE)) {
            members.add(this.parseClassMember());
        }

        this.consume(TokenType.CLOSING_BRACE, "ClassDeclaration");

        return new ClassDeclaration(name, members);
    }

    private ClassMember parseClassMember() throws ParserException {
        this.consume(TokenType.PUBLIC, "Class Member");

        // ClassMember -> MainMethod
        if (this.lookahead(TokenType.STATIC)) {
            this.consume(TokenType.STATIC, "MainMethod");
            this.consume(TokenType.VOID, "MainMethod");

            String methodName = this.consume(TokenType.IDENTIFIER, "MainMethod").text;

            this.consume(TokenType.OPENING_PARENTHESIS, "MainMethod");

            Token parameterTypeToken = this.consume(TokenType.IDENTIFIER, "MainMethod");
            if (!parameterTypeToken.text.equals("String")) {
                throw new UnexpectedTokenException(parameterTypeToken, "MainMethod", TokenType.IDENTIFIER);
            }

            this.consume(TokenType.OPENING_BRACKET, "MainMethod");
            this.consume(TokenType.CLOSING_BRACKET, "MainMethod");

            String parameterName = this.consume(TokenType.IDENTIFIER, "MainMethod").text;

            this.consume(TokenType.CLOSING_PARENTHESIS, "MainMethod");

            if (this.lookahead(TokenType.THROWS)) {
                this.consume(TokenType.THROWS, "MainMethod");
                this.consume(TokenType.IDENTIFIER, "MainMethod");
            }

            Block body = this.parseBlock();

            return new MainMethod(methodName, parameterName, body);
        }

        // ClassMember -> Method | Field
        else {
            Type type = this.parseType();
            String name = this.consume(TokenType.IDENTIFIER, "ClassMember").text;

            // ClassMember -> Field
            if (this.lookahead(TokenType.SEMICOLON)) {
                this.consume(TokenType.SEMICOLON, "Field");

                return new Field(type, name);
            }

            // ClassMember -> Method
            else {
                this.consume(TokenType.OPENING_PARENTHESIS, "Method");

                List<Parameter> parameters = this.parseParameters();

                this.consume(TokenType.CLOSING_PARENTHESIS, "Method");

                if (this.lookahead(TokenType.THROWS)) {
                    this.consume(TokenType.THROWS, "Method");
                    this.consume(TokenType.IDENTIFIER, "Method");
                }

                Block body = this.parseBlock();

                return new Method(type, name, parameters, body);
            }
        }
    }

    private List<Parameter> parseParameters() throws ParserException {
        if (this.lookahead(TokenType.CLOSING_PARENTHESIS)) {
            return Collections.emptyList();
        }

        // Parameters -> Parameter { "," Parameter }
        else {
            List<Parameter> parameters = new ArrayList<>();
            parameters.add(this.parseParameter());

            while (this.lookahead(TokenType.COMMA)) {
                this.consume(TokenType.COMMA, null);
                parameters.add(this.parseParameter());
            }

            return parameters;
        }
    }

    private Parameter parseParameter() throws ParserException {
        Type type = this.parseType();
        String name = this.consume(TokenType.IDENTIFIER, "Parameter").text;

        return new Parameter(type, name);
    }

    // MARK: - Parsing Statements

    private Statement parseStatement() throws ParserException {
        if (this.lookahead(TokenType.IF)) {
            return this.parseIfStatement();
        }
        else if (this.lookahead(TokenType.WHILE)) {
            return this.parseWhileStatement();
        }
        else if (this.lookahead(TokenType.RETURN)) {
            return this.parseReturnStatement();
        }
        else if (this.lookahead(TokenType.OPENING_BRACE)) {
            return this.parseBlock();
        }
        else if (this.lookahead(TokenType.SEMICOLON)) {
            return this.parseEmptyStatement();
        }
        else {
            return this.parseExpressionStatement();
        }
    }

    private Block parseBlock() throws ParserException {
        this.consume(TokenType.OPENING_BRACE, "Block");

        List<BlockStatement> statements = new ArrayList<>();

        while (!this.lookahead(TokenType.CLOSING_BRACE)) {
            statements.add(this.parseBlockStatement());
        }

        this.consume(TokenType.CLOSING_BRACE, "Block");

        return new Block(statements);
    }

    private BlockStatement parseBlockStatement() throws ParserException {

        // BlockStatement -> LocalVariableDeclarationStatement
        if (this.lookahead(TokenType.IDENTIFIER, TokenType.OPENING_BRACKET, TokenType.CLOSING_BRACKET)) {
            return this.parseLocalVariableDeclarationStatement();
        }

        // BlockStatement -> LocalVariableDeclarationStatement
        else if (this.lookahead(TokenType.IDENTIFIER, TokenType.IDENTIFIER)) {
            return this.parseLocalVariableDeclarationStatement();
        }

        // BlockStatement -> LocalVariableDeclarationStatement
        else if (this.lookahead(TokenType.INT)) {
            return this.parseLocalVariableDeclarationStatement();
        }

        // BlockStatement -> LocalVariableDeclarationStatement
        else if (this.lookahead(TokenType.BOOLEAN)) {
            return this.parseLocalVariableDeclarationStatement();
        }

        // BlockStatement -> LocalVariableDeclarationStatement
        else if (this.lookahead(TokenType.VOID)) {
            return this.parseLocalVariableDeclarationStatement();
        }

        // BlockStatement -> Statement
        else {
            return this.parseStatement();
        }
    }

    private BlockStatement parseLocalVariableDeclarationStatement() throws ParserException {
        Type type = this.parseType();
        String name = this.consume(TokenType.IDENTIFIER, "LocalVariableDeclarationStatement").text;

        // LocalVariableDeclarationStatement -> Type "IDENTIFIER" "=" Expression ";"
        if (this.lookahead(TokenType.ASSIGN)) {
            this.consume(TokenType.ASSIGN, "LocalVariableDeclarationStatement");
            Expression value = this.parseExpression();
            this.consume(TokenType.SEMICOLON, "LocalVariableDeclarationStatement");

            return new LocalVariableInitializationStatement(type, name, value);
        }

        // LocalVariableDeclarationStatement -> Type "IDENTIFIER"  ";"
        else {
            this.consume(TokenType.SEMICOLON, "LocalVariableDeclarationStatement");

            return new LocalVariableDeclarationStatement(type, name);
        }
    }

    private Statement parseEmptyStatement() throws ParserException {
        this.consume(TokenType.SEMICOLON, "EmptyStatement");

        return new EmptyStatement();
    }

    private Statement parseWhileStatement() throws ParserException {
        this.consume(TokenType.WHILE, "WhileStatement");
        this.consume(TokenType.OPENING_PARENTHESIS, "WhileStatement");
        Expression condition = this.parseExpression();
        this.consume(TokenType.CLOSING_PARENTHESIS, "WhileStatement");
        Statement statementWhileTrue = this.parseStatement();

        return new WhileStatement(condition, statementWhileTrue);
    }

    private Statement parseIfStatement() throws ParserException {
        this.consume(TokenType.IF, "IfStatement");
        this.consume(TokenType.OPENING_PARENTHESIS, "IfStatement");
        Expression condition = this.parseExpression();
        this.consume(TokenType.CLOSING_PARENTHESIS, "IfStatement");
        Statement statementIfTrue = this.parseStatement();

        // IfStatement -> "if" "(" Expression ")" Statement "else" Statement
        if (this.lookahead(TokenType.ELSE)) {
            this.consume(TokenType.ELSE, "IfStatement");

            Statement statementIfFalse = this.parseStatement();

            return new IfElseStatement(condition, statementIfTrue, statementIfFalse);
        }

        // IfStatement -> "if" "(" Expression ")" Statement
        else {
            return new IfStatement(condition, statementIfTrue);
        }
    }

    private Statement parseExpressionStatement() throws ParserException {
        Expression expression = this.parseExpression();

        this.consume(TokenType.SEMICOLON, "ExpressionStatement");

        return new ExpressionStatement(expression);
    }

    private Statement parseReturnStatement() throws ParserException {
        this.consume(TokenType.RETURN, "ReturnStatement");

        // ReturnStatement -> "return" ";"
        if (this.lookahead(TokenType.SEMICOLON)) {
            this.consume(TokenType.SEMICOLON, "ReturnStatement");

            return new ReturnNoValueStatement();
        }

        // ReturnStatement -> "return" Expression ";"
        else {
            Expression expression = this.parseExpression();
            this.consume(TokenType.SEMICOLON, "ReturnStatement");

            return new ReturnValueStatement(expression);
        }
    }

    // MARK: - Parsing Expressions

    private Expression parseExpression() throws ParserException {
        return this.parseAssignmentExpression();
    }

    private Expression parseAssignmentExpression() throws ParserException {
        Expression expression = this.parseLogicalOrExpression();

        // AssignmentExpression -> LogicalOrExpression "=" AssignmentExpression
        if (this.lookahead(TokenType.ASSIGN)) {
            Stack<Expression> expressions = new Stack<>();
            expressions.push(expression);

            while (this.lookahead(TokenType.ASSIGN)) {
                this.consume(TokenType.ASSIGN, null);
                expressions.push(this.parseAssignmentExpression());
            }

            while (expressions.size() >= 2) {
                Expression last = expressions.pop();
                Expression secondToLast = expressions.pop();

                expressions.push(new AssignmentExpression(secondToLast, last));
            }

            return expressions.pop();
        }

        // AssignmentExpression -> LogicalOrExpression
        else {
            return expression;
        }
    }

    private Expression parseLogicalOrExpression() throws ParserException {
        Expression expression = this.parseLogicalAndExpression();

        outer:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case LOGICAL_OR:
                    this.consume(TokenType.LOGICAL_OR, null);
                    expression = new LogicalOrExpression(expression, this.parseLogicalAndExpression());
                    break;
                default:
                    break outer;
            }
        }

        return expression;
    }

    private Expression parseLogicalAndExpression() throws ParserException {
        Expression expression = this.parseEqualityExpression();

        outer:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case LOGICAL_AND:
                    this.consume(TokenType.LOGICAL_AND, null);
                    expression = new LogicalAndExpression(expression, this.parseEqualityExpression());
                    break;
                default:
                    break outer;
            }
        }

        return expression;
    }

    private Expression parseEqualityExpression() throws ParserException {
        Expression expression = this.parseRelationalExpression();

        outer:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case EQUAL_TO:
                    this.consume(TokenType.EQUAL_TO, null);
                    expression = new EqualToExpression(expression, this.parseRelationalExpression());
                    break;
                case NOT_EQUAL_TO:
                    this.consume(TokenType.NOT_EQUAL_TO, null);
                    expression = new NotEqualToExpression(expression, this.parseRelationalExpression());
                    break;
                default:
                    break outer;
            }
        }

        return expression;
    }

    private Expression parseRelationalExpression() throws ParserException {
        Expression expression = this.parseAdditiveExpression();

        outer:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case LESS_THAN:
                    this.consume(TokenType.LESS_THAN, null);
                    expression = new LessThanExpression(expression, this.parseAdditiveExpression());
                    break;
                case LESS_THAN_OR_EQUAL_TO:
                    this.consume(TokenType.LESS_THAN_OR_EQUAL_TO, null);
                    expression = new LessThanOrEqualToExpression(expression, this.parseAdditiveExpression());
                    break;
                case GREATER_THAN:
                    this.consume(TokenType.GREATER_THAN, null);
                    expression = new GreaterThanExpression(expression, this.parseAdditiveExpression());
                    break;
                case GREATER_THAN_OR_EQUAL_TO:
                    this.consume(TokenType.GREATER_THAN_OR_EQUAL_TO, null);
                    expression = new GreaterThanOrEqualToExpression(expression, this.parseAdditiveExpression());
                    break;
                default:
                    break outer;
            }
        }

        return expression;
    }

    private Expression parseAdditiveExpression() throws ParserException {
        Expression expression = this.parseMultiplicativeExpression();

        outer:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case PLUS:
                    this.consume(TokenType.PLUS, null);
                    expression = new AddExpression(expression, this.parseMultiplicativeExpression());
                    break;
                case MINUS:
                    this.consume(TokenType.MINUS, null);
                    expression = new SubtractExpression(expression, this.parseMultiplicativeExpression());
                    break;
                default:
                    break outer;
            }
        }

        return expression;
    }

    private Expression parseMultiplicativeExpression() throws ParserException {
        Expression expression = this.parseUnaryExpression();

        outer:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case MULTIPLY:
                    this.consume(TokenType.MULTIPLY, null);
                    expression = new MultiplyExpression(expression, this.parseUnaryExpression());
                    break;
                case DIVIDE:
                    this.consume(TokenType.DIVIDE, null);
                    expression = new DivideExpression(expression, this.parseUnaryExpression());
                    break;
                case MODULO:
                    this.consume(TokenType.MODULO, null);
                    expression = new ModuloExpression(expression, this.parseUnaryExpression());
                    break;
                default:
                    break outer;
            }
        }

        return expression;
    }

    private Expression parseUnaryExpression() throws ParserException {
        Stack<TokenType> consumedPrefixOperators = new Stack<>();

        outer:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case LOGICAL_NEGATION:
                    this.consume(TokenType.LOGICAL_NEGATION, null);
                    consumedPrefixOperators.push(TokenType.LOGICAL_NEGATION);
                    break;
                case MINUS:
                    this.consume(TokenType.MINUS, null);
                    consumedPrefixOperators.push(TokenType.MINUS);
                    break;
                default:
                    break outer;
            }
        }

        Expression expression = this.parsePostfixExpression();

        while (!consumedPrefixOperators.isEmpty()) {
            switch (consumedPrefixOperators.pop()) {
                case LOGICAL_NEGATION:
                    expression = new LogicalNotExpression(expression);
                    break;
                case MINUS:
                    expression = new NegateExpression(expression);
                    break;
                default:
                    throw new Error();
            }
        }

        return expression;
    }

    private Expression parsePostfixExpression() throws ParserException {
        Expression expression = this.parsePrimaryExpression();

        outer:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case PERIOD:
                case OPENING_BRACKET:
                    expression = new PostfixExpression(expression, this.parsePostfixOperation());
                    break;
                default:
                    break outer;
            }
        }

        return expression;
    }

    private PostfixOperation parsePostfixOperation() throws ParserException {
        // PostfixOperation -> MethodInvocation | FieldAccess
        if (this.lookahead(TokenType.PERIOD)) {
            this.consume(TokenType.PERIOD, "PostfixOperation");
            String identifier = this.consume(TokenType.IDENTIFIER, "PostfixOperation").text;

            if (this.lookahead(TokenType.OPENING_PARENTHESIS)) {
                this.consume(TokenType.OPENING_PARENTHESIS, "MethodInvocation");
                List<Expression> arguments = this.parseArguments();
                this.consume(TokenType.CLOSING_PARENTHESIS, "MethodInvocation");

                return new MethodInvocation(identifier, arguments);
            } else {
                return new FieldAccess(identifier);
            }
        }

        // PostfixOperation -> ArrayAccess
        else if (this.lookahead(TokenType.OPENING_BRACKET)) {
            this.consume(TokenType.OPENING_BRACKET, "ArrayAccess");
            Expression expression = this.parseExpression();
            this.consume(TokenType.CLOSING_BRACKET, "ArrayAccess");

            return new ArrayAccess(expression);
        }

        else {
            throw new UnexpectedTokenException(this.getCurrentToken(), "PostfixOperation", TokenType.PERIOD,
                    TokenType.OPENING_BRACKET);
        }
    }

    private Expression parsePrimaryExpression() throws ParserException {
        final String context = "PrimaryExpression";

        // PrimaryExpression -> "(" Expression ")"
        if (this.lookahead(TokenType.OPENING_PARENTHESIS)) {
            this.consume(TokenType.OPENING_PARENTHESIS, "PrimaryExpression");
            Expression expression = this.parseExpression();
            this.consume(TokenType.CLOSING_PARENTHESIS, "PrimaryExpression");

            return expression;
        }

        // PrimaryExpression -> "IDENTIFIER" | "IDENTIFIER" "(" Arguments ")"
        else if (this.lookahead(TokenType.IDENTIFIER)) {
            String identifier = this.consume(TokenType.IDENTIFIER, "PrimaryExpression").text;

            if (this.lookahead(TokenType.OPENING_PARENTHESIS)) {
                this.consume(TokenType.OPENING_PARENTHESIS, "PrimaryExpression");
                List<Expression> arguments = this.parseArguments();
                this.consume(TokenType.CLOSING_PARENTHESIS, "PrimaryExpression");

                return new IdentifierAndArgumentsExpression(identifier, arguments);
            } else {
                return new IdentifierExpression(identifier);
            }
        }

        // PrimaryExpression -> Literal -> "INTEGER_LITERAL"
        else if (this.lookahead(TokenType.INTEGER_LITERAL)) {
            String value = this.consume(TokenType.INTEGER_LITERAL, null).text;

            return new IntegerLiteral(value);
        }

        // PrimaryExpression -> Literal -> "null"
        else if (this.lookahead(TokenType.NULL)) {
            this.consume(TokenType.NULL, null);

            return new NullLiteral();
        }

        // PrimaryExpression -> Literal -> "true"
        else if (this.lookahead(TokenType.TRUE)) {
            this.consume(TokenType.TRUE, null);

            return new BooleanLiteral(true);
        }

        // PrimaryExpression -> Literal -> "false"
        else if (this.lookahead(TokenType.FALSE)) {
            this.consume(TokenType.FALSE, null);

            return new BooleanLiteral(false);
        }

        // PrimaryExpression -> "this"
        else if (this.lookahead(TokenType.THIS)) {
            this.consume(TokenType.THIS, null);

            return new ThisExpression();
        }

        // PrimaryExpression -> NewObjectExpression | NewArrayExpression
        else if (this.lookahead(TokenType.NEW)) {
            this.consume(TokenType.NEW, null);

            // PrimaryExpression -> NewObjectExpression -> "new" "IDENTIFIER" "(" ")"
            if (this.lookahead(TokenType.IDENTIFIER, TokenType.OPENING_PARENTHESIS)) {
                String className = this.consume(TokenType.IDENTIFIER, "NewObjectExpression").text;
                this.consume(TokenType.OPENING_PARENTHESIS, "NewObjectExpression");
                this.consume(TokenType.CLOSING_PARENTHESIS, "NewObjectExpression");

                return new NewObjectExpression(className);
            }

            // PrimaryExpression -> NewArrayExpression -> "new" BasicType "[" Expression "]" { "[" "]" }
            else {
                BasicType basicType = this.parseBasicType();
                this.consume(TokenType.OPENING_BRACKET, "NewArrayExpression");
                Expression expression = this.parseExpression();
                this.consume(TokenType.CLOSING_BRACKET, "NewArrayExpression");
                int numberOfDimensions = 1 + this.parseOpeningAndClosingBrackets();

                return new NewArrayExpression(basicType, expression, numberOfDimensions);
            }
        }

        else {
            throw new UnexpectedTokenException(this.getCurrentToken(), "PrimaryExpression",
                    TokenType.OPENING_PARENTHESIS, TokenType.IDENTIFIER, TokenType.INTEGER_LITERAL, TokenType.NULL,
                    TokenType.TRUE, TokenType.FALSE, TokenType.THIS, TokenType.NEW);
        }
    }

    private List<Expression> parseArguments() throws ParserException {
        if (this.lookahead(TokenType.CLOSING_PARENTHESIS)) {
            return Collections.emptyList();
        }

        // Arguments -> Expression { "," Expression }
        else {
            List<Expression> expressions = new ArrayList<>();
            expressions.add(this.parseExpression());

            while (this.lookahead(TokenType.COMMA)) {
                this.consume(TokenType.COMMA, null);
                expressions.add(this.parseExpression());
            }

            return expressions;
        }
    }

    // MARK: - Parsing Types

    private Type parseType() throws ParserException {
        BasicType basicType = this.parseBasicType();
        int numberOfDimensions = this.parseOpeningAndClosingBrackets();

        return new Type(basicType, numberOfDimensions);
    }

    private BasicType parseBasicType() throws ParserException {

        // BasicType -> "IDENTIFIER"
        if (this.lookahead(TokenType.IDENTIFIER)) {
            String className = this.consume(TokenType.IDENTIFIER, null).text;
            return new UserDefinedType(className);
        }

        // BasicType -> "int"
        else if (this.lookahead(TokenType.INT)) {
            this.consume(TokenType.INT, null);
            return new IntegerType();
        }

        // BasicType -> "boolean"
        else if (this.lookahead(TokenType.BOOLEAN)) {
            this.consume(TokenType.BOOLEAN, null);
            return new BooleanType();
        }

        // BasicType -> "void"
        else if (this.lookahead(TokenType.VOID)) {
            this.consume(TokenType.VOID, null);
            return new VoidType();
        }

        else {
            throw new UnexpectedTokenException(this.getCurrentToken(), "BasicType", TokenType.IDENTIFIER, TokenType.INT,
                    TokenType.BOOLEAN, TokenType.VOID);
        }
    }

    // Parses pairs of opening and closing brackets leniently.
    private int parseOpeningAndClosingBrackets() throws ParserException {
        int count = 0;

        while (this.lookahead(TokenType.OPENING_BRACKET, TokenType.CLOSING_BRACKET)) {
            this.consume(TokenType.OPENING_BRACKET, null);
            this.consume(TokenType.CLOSING_BRACKET, null);

            count += 1;
        }

        return count;
    }
}
