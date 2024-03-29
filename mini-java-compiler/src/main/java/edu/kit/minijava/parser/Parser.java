package edu.kit.minijava.parser;

import edu.kit.minijava.lexer.*;
import edu.kit.minijava.ast.nodes.*;
import edu.kit.minijava.ast.references.*;

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
                }
                else {
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
        }
        else {
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

        if (token == null || token.getType() != type) {
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

        Token token = this.consume(TokenType.IDENTIFIER, "ClassDeclaration");
        List<FieldDeclaration> fields = new ArrayList<>();
        List<MethodDeclaration> methods = new ArrayList<>();
        List<MainMethodDeclaration> mainMethods = new ArrayList<>();

        this.consume(TokenType.OPENING_BRACE, "ClassDeclaration");

        while (!this.lookahead(TokenType.CLOSING_BRACE)) {
            MemberDeclaration declaration = this.parseClassMember();

            if (declaration instanceof FieldDeclaration) {
                fields.add((FieldDeclaration)declaration);
            }
            else if (declaration instanceof MethodDeclaration) {
                methods.add((MethodDeclaration)declaration);
            }
            else if (declaration instanceof MainMethodDeclaration) {
                mainMethods.add((MainMethodDeclaration)declaration);
            }
            else {
                throw new AssertionError();
            }
        }

        this.consume(TokenType.CLOSING_BRACE, "ClassDeclaration");

        return new ClassDeclaration(token.getText(), mainMethods, methods, fields, token.getLocation());
    }

    private MemberDeclaration parseClassMember() throws ParserException {
        this.consume(TokenType.PUBLIC, "Class Member");

        // ClassMember -> MainMethod
        if (this.lookahead(TokenType.STATIC)) {
            this.consume(TokenType.STATIC, "MainMethod");

            this.consume(TokenType.VOID, "MainMethod");
            Token methodNameToken = this.consume(TokenType.IDENTIFIER, "MainMethod");

            this.consume(TokenType.OPENING_PARENTHESIS, "MainMethod");

            Token parameterTypeToken = this.consume(TokenType.IDENTIFIER, "MainMethod");

            if (!parameterTypeToken.getText().equals("String")) {
                throw new UnexpectedTokenException(parameterTypeToken, "MainMethod", TokenType.IDENTIFIER);
            }

            this.consume(TokenType.OPENING_BRACKET, "MainMethod");
            this.consume(TokenType.CLOSING_BRACKET, "MainMethod");

            Token parameterNameToken = this.consume(TokenType.IDENTIFIER, "MainMethod");

            this.consume(TokenType.CLOSING_PARENTHESIS, "MainMethod");

            if (this.lookahead(TokenType.THROWS)) {
                this.consume(TokenType.THROWS, "MainMethod");
                this.consume(TokenType.IDENTIFIER, "MainMethod");
            }

            Statement.Block body = this.parseBlock();

            String methodName = methodNameToken.getText();
            TokenLocation methodLocation = methodNameToken.getLocation();

            return new MainMethodDeclaration(methodName, parameterNameToken, body, methodLocation);
        }

        // ClassMember -> Method | Field
        else {
            ExplicitTypeReference type = this.parseType();
            Token name = this.consume(TokenType.IDENTIFIER, "ClassMember");

            // ClassMember -> Field
            if (this.lookahead(TokenType.SEMICOLON)) {
                this.consume(TokenType.SEMICOLON, "Field");

                return new FieldDeclaration(type, false, name.getText(), name.getLocation());
            }

            // ClassMember -> Method
            else {
                this.consume(TokenType.OPENING_PARENTHESIS, "Method");

                List<ParameterDeclaration> parameters = this.parseParameters();

                this.consume(TokenType.CLOSING_PARENTHESIS, "Method");

                if (this.lookahead(TokenType.THROWS)) {
                    this.consume(TokenType.THROWS, "Method");
                    this.consume(TokenType.IDENTIFIER, "Method");
                }

                Statement.Block body = this.parseBlock();

                return new MethodDeclaration(type, name.getText(), parameters, body, name.getLocation());
            }
        }
    }

    private List<ParameterDeclaration> parseParameters() throws ParserException {
        if (this.lookahead(TokenType.CLOSING_PARENTHESIS)) {
            return Collections.emptyList();
        }

        // Parameters -> Parameter { "," Parameter }
        else {
            List<ParameterDeclaration> parameters = new ArrayList<>();
            parameters.add(this.parseParameter());

            while (this.lookahead(TokenType.COMMA)) {
                this.consume(TokenType.COMMA, null);
                parameters.add(this.parseParameter());
            }

            return parameters;
        }
    }

    private ParameterDeclaration parseParameter() throws ParserException {
        ExplicitTypeReference type = this.parseType();
        Token token = this.consume(TokenType.IDENTIFIER, "Parameter");

        return new ParameterDeclaration(type, token.getText(), token.getLocation());
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

    private Statement.Block parseBlock() throws ParserException {
        Token token = this.consume(TokenType.OPENING_BRACE, "Block");

        List<Statement> statements = new ArrayList<>();

        while (!this.lookahead(TokenType.CLOSING_BRACE)) {
            statements.add(this.parseBlockStatement());
        }

        this.consume(TokenType.CLOSING_BRACE, "Block");

        return new Statement.Block(statements, token.getLocation());
    }

    private Statement parseBlockStatement() throws ParserException {

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

    private Statement parseLocalVariableDeclarationStatement() throws ParserException {
        ExplicitTypeReference type = this.parseType();
        Token name = this.consume(TokenType.IDENTIFIER, "LocalVariableDeclarationStatement");

        // LocalVariableDeclarationStatement -> Type "IDENTIFIER" "=" Expression ";"
        if (this.lookahead(TokenType.ASSIGN)) {
            this.consume(TokenType.ASSIGN, "LocalVariableDeclarationStatement");
            Expression value = this.parseExpression(0);
            this.consume(TokenType.SEMICOLON, "LocalVariableDeclarationStatement");

            return new Statement.LocalVariableDeclarationStatement(type, name.getText(), value, name.getLocation());
        }

        // LocalVariableDeclarationStatement -> Type "IDENTIFIER"  ";"
        else {
            this.consume(TokenType.SEMICOLON, "LocalVariableDeclarationStatement");

            return new Statement.LocalVariableDeclarationStatement(type, name.getText(), name.getLocation());
        }
    }

    private Statement parseEmptyStatement() throws ParserException {
        Token token = this.consume(TokenType.SEMICOLON, "EmptyStatement");

        return new Statement.EmptyStatement(token.getLocation());
    }

    private Statement parseWhileStatement() throws ParserException {
        Token token = this.consume(TokenType.WHILE, "WhileStatement");
        this.consume(TokenType.OPENING_PARENTHESIS, "WhileStatement");
        Expression condition = this.parseExpression(0);
        this.consume(TokenType.CLOSING_PARENTHESIS, "WhileStatement");
        Statement statementWhileTrue = this.parseStatement();

        return new Statement.WhileStatement(condition, statementWhileTrue, token.getLocation());
    }

    private Statement parseIfStatement() throws ParserException {
        Token token = this.consume(TokenType.IF, "IfStatement");
        this.consume(TokenType.OPENING_PARENTHESIS, "IfStatement");
        Expression condition = this.parseExpression(0);
        this.consume(TokenType.CLOSING_PARENTHESIS, "IfStatement");
        Statement statementIfTrue = this.parseStatement();

        // IfStatement -> "if" "(" Expression ")" Statement "else" Statement
        if (this.lookahead(TokenType.ELSE)) {
            this.consume(TokenType.ELSE, "IfStatement");

            Statement statementIfFalse = this.parseStatement();

            return new Statement.IfStatement(condition, statementIfTrue, statementIfFalse, token.getLocation());
        }

        // IfStatement -> "if" "(" Expression ")" Statement
        else {
            return new Statement.IfStatement(condition, statementIfTrue, token.getLocation());
        }
    }

    private Statement parseExpressionStatement() throws ParserException {
        if (this.hasReachedEndOfInput()) {
            Expression expression = this.parseExpression(0);

            // Parsing expression at EOF will throw.
            throw new AssertionError();
        }
        else {
            TokenLocation location = this.getCurrentToken().getLocation();

            Expression expression = this.parseExpression(0);

            this.consume(TokenType.SEMICOLON, "ExpressionStatement");

            return new Statement.ExpressionStatement(expression, location);
        }
    }

    private Statement parseReturnStatement() throws ParserException {
        Token token = this.consume(TokenType.RETURN, "ReturnStatement");

        // ReturnStatement -> "return" ";"
        if (this.lookahead(TokenType.SEMICOLON)) {
            this.consume(TokenType.SEMICOLON, "ReturnStatement");

            return new Statement.ReturnStatement(token.getLocation());
        }

        // ReturnStatement -> "return" Expression ";"
        else {
            Expression value = this.parseExpression(0);
            this.consume(TokenType.SEMICOLON, "ReturnStatement");

            return new Statement.ReturnStatement(value, token.getLocation());
        }
    }

    // MARK: - Parsing Expressions

    private Expression parseExpression(int minimumPrecedence) throws ParserException {
        Stack<Token> consumedPrefixOperationTokens = new Stack<>();

        // 1. Consume prefix operators
        consumePrefixOperators:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().getType()) {
                case LOGICAL_NEGATION:
                    consumedPrefixOperationTokens.push(this.consume(TokenType.LOGICAL_NEGATION, null));
                    break;
                case MINUS:
                    consumedPrefixOperationTokens.push(this.consume(TokenType.MINUS, null));
                    break;
                default:
                    break consumePrefixOperators;
            }
        }

        // 2. Parse primary expression
        Expression expression = this.parsePrimaryExpression();

        // 3. Consume postfix operations (they have higher precedence than prefix operators)
        consumePostfixOperations:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().getType()) {
                case PERIOD:
                case OPENING_BRACKET:
                    expression = this.parsePostfixOperationWithContext(expression);
                    break;
                default:
                    break consumePostfixOperations;
            }
        }

        // 4. Apply consumed prefix operators now.
        while (!consumedPrefixOperationTokens.isEmpty()) {
            Token token = consumedPrefixOperationTokens.pop();
            UnaryOperationType operation;

            switch (token.getType()) {
                case LOGICAL_NEGATION:
                    operation = UnaryOperationType.LOGICAL_NEGATION;
                    break;
                case MINUS:
                    operation = UnaryOperationType.NUMERIC_NEGATION;
                    break;
                default:
                    throw new Error();
            }

            expression = new Expression.UnaryOperation(operation, expression, token.getLocation());
        }

        // 5. Precedence climbing with 'atom' including prefix operators and postfix operations.
        while (!this.hasReachedEndOfInput()) {
            BinaryOperation operation = BinaryOperation.forTokenType(this.getCurrentToken().getType());

            if (operation == null || operation.getPrecedence() < minimumPrecedence) {
                break;
            }

            Token token = this.consume(operation.getTokenType(), null);
            int precedence = operation.getPrecedence();

            if (operation.getAssociativity() == Associativity.LEFT_ASSOCIATIVE) {
                precedence = precedence + 1;
            }

            Expression rhs = this.parseExpression(precedence);

            expression = operation.instantiate(expression, rhs, token.getLocation());
        }

        return expression;
    }

    private Expression parsePostfixOperationWithContext(Expression context) throws ParserException {

        // PostfixOperation -> MethodInvocation | FieldAccess
        if (this.lookahead(TokenType.PERIOD)) {
            this.consume(TokenType.PERIOD, "PostfixOperation");

            Token token = this.consume(TokenType.IDENTIFIER, "PostfixOperation");

            if (this.lookahead(TokenType.OPENING_PARENTHESIS)) {
                this.consume(TokenType.OPENING_PARENTHESIS, "MethodInvocation");
                List<Expression> arguments = this.parseArguments();
                this.consume(TokenType.CLOSING_PARENTHESIS, "MethodInvocation");

                return new Expression.MethodInvocation(context, token.getText(), arguments, token.getLocation());
            }
            else {
                return new Expression.ExplicitFieldAccess(context, token.getText(), token.getLocation());
            }
        }

        // PostfixOperation -> ArrayAccess
        else if (this.lookahead(TokenType.OPENING_BRACKET)) {
            Token token = this.consume(TokenType.OPENING_BRACKET, "ArrayAccess");
            Expression index = this.parseExpression(0);
            this.consume(TokenType.CLOSING_BRACKET, "ArrayAccess");

            return new Expression.ArrayElementAccess(context, index, token.getLocation());
        }

        else {
            throw new UnexpectedTokenException(this.getCurrentToken(), "PostfixOperation", TokenType.PERIOD,
                    TokenType.OPENING_BRACKET);
        }
    }

    private Expression parsePrimaryExpression() throws ParserException {

        // PrimaryExpression -> "(" Expression ")"
        if (this.lookahead(TokenType.OPENING_PARENTHESIS)) {
            this.consume(TokenType.OPENING_PARENTHESIS, "PrimaryExpression");
            Expression expression = this.parseExpression(0);
            this.consume(TokenType.CLOSING_PARENTHESIS, "PrimaryExpression");

            // Mutable state not nice, but looping through number of explicit parentheses ain't prettier.
            expression.setNumberOfExplicitParentheses(expression.getNumberOfExplicitParentheses() + 1);

            return expression;
        }

        // PrimaryExpression -> "IDENTIFIER" | "IDENTIFIER" "(" Arguments ")"
        else if (this.lookahead(TokenType.IDENTIFIER)) {
            Token token = this.consume(TokenType.IDENTIFIER, "PrimaryExpression");

            if (this.lookahead(TokenType.OPENING_PARENTHESIS)) {
                this.consume(TokenType.OPENING_PARENTHESIS, "PrimaryExpression");
                List<Expression> arguments = this.parseArguments();
                this.consume(TokenType.CLOSING_PARENTHESIS, "PrimaryExpression");

                return new Expression.MethodInvocation(token.getText(), arguments, token.getLocation());
            }
            else {
                return new Expression.VariableAccess(token.getText(), token.getLocation());
            }
        }

        // PrimaryExpression -> Literal -> "INTEGER_LITERAL"
        else if (this.lookahead(TokenType.INTEGER_LITERAL)) {
            Token token = this.consume(TokenType.INTEGER_LITERAL, null);

            return new Expression.IntegerLiteral(token.getText(), token.getLocation());
        }

        // PrimaryExpression -> Literal -> "null"
        else if (this.lookahead(TokenType.NULL)) {
            Token token = this.consume(TokenType.NULL, null);

            return new Expression.NullLiteral(token.getLocation());
        }

        // PrimaryExpression -> Literal -> "true"
        else if (this.lookahead(TokenType.TRUE)) {
            Token token = this.consume(TokenType.TRUE, null);

            return new Expression.BooleanLiteral(true, token.getLocation());
        }

        // PrimaryExpression -> Literal -> "false"
        else if (this.lookahead(TokenType.FALSE)) {
            Token token = this.consume(TokenType.FALSE, null);

            return new Expression.BooleanLiteral(false, token.getLocation());
        }

        // PrimaryExpression -> "this"
        else if (this.lookahead(TokenType.THIS)) {
            Token token = this.consume(TokenType.THIS, null);

            return new Expression.CurrentContextAccess(token.getLocation());
        }

        // PrimaryExpression -> NewObjectExpression | NewArrayExpression
        else if (this.lookahead(TokenType.NEW)) {
            this.consume(TokenType.NEW, null);

            // PrimaryExpression -> NewObjectExpression -> "new" "IDENTIFIER" "(" ")"
            if (this.lookahead(TokenType.IDENTIFIER, TokenType.OPENING_PARENTHESIS)) {
                Token token = this.consume(TokenType.IDENTIFIER, "NewObjectExpression");
                this.consume(TokenType.OPENING_PARENTHESIS, "NewObjectExpression");
                this.consume(TokenType.CLOSING_PARENTHESIS, "NewObjectExpression");

                return new Expression.NewObjectCreation(token.getText(), token.getLocation());
            }

            // PrimaryExpression -> NewArrayExpression -> "new" BasicType "[" Expression "]" { "[" "]" }
            else {
                ExplicitReference<BasicTypeDeclaration> basicType = this.parseBasicType();
                Token token = this.consume(TokenType.OPENING_BRACKET, "NewArrayExpression");
                Expression expression = this.parseExpression(0);
                this.consume(TokenType.CLOSING_BRACKET, "NewArrayExpression");
                int numberOfDimensions = 1 + this.parseOpeningAndClosingBrackets();

                return new Expression.NewArrayCreation(basicType, expression, numberOfDimensions, token.getLocation());
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
            expressions.add(this.parseExpression(0));

            while (this.lookahead(TokenType.COMMA)) {
                this.consume(TokenType.COMMA, null);
                expressions.add(this.parseExpression(0));
            }

            return expressions;
        }
    }

    // MARK: - Parsing Types

    private ExplicitTypeReference parseType() throws ParserException {
        ExplicitReference<BasicTypeDeclaration> basicTypeReference = this.parseBasicType();
        int numberOfDimensions = this.parseOpeningAndClosingBrackets();

        return new ExplicitTypeReference(basicTypeReference, numberOfDimensions);
    }

    private ExplicitReference<BasicTypeDeclaration> parseBasicType() throws ParserException {

        // BasicType -> "IDENTIFIER"
        if (this.lookahead(TokenType.IDENTIFIER)) {
            Token token = this.consume(TokenType.IDENTIFIER, null);

            return new ExplicitReference<>(token.getText(), token.getLocation());
        }

        // BasicType -> "int"
        else if (this.lookahead(TokenType.INT)) {
            Token token = this.consume(TokenType.INT, null);

            return new ExplicitReference<>(token.getText(), token.getLocation());
        }

        // BasicType -> "boolean"
        else if (this.lookahead(TokenType.BOOLEAN)) {
            Token token = this.consume(TokenType.BOOLEAN, null);

            return new ExplicitReference<>(token.getText(), token.getLocation());
        }

        // BasicType -> "void"
        else if (this.lookahead(TokenType.VOID)) {
            Token token = this.consume(TokenType.VOID, null);

            return new ExplicitReference<>(token.getText(), token.getLocation());
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
