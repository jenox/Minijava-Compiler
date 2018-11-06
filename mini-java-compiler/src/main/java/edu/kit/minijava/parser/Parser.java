package edu.kit.minijava.parser;

import java.io.*;
import java.util.*;

import edu.kit.minijava.lexer.*;
import edu.kit.minijava.ast2.nodes.*;
import edu.kit.minijava.ast2.references.*;

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
        List<MethodDeclaration> staticMethodDeclarations = new ArrayList<>();
        List<MethodDeclaration> instanceMethodDeclarations = new ArrayList<>();
        List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

        this.consume(TokenType.OPENING_BRACE, "ClassDeclaration");

        // TODO: This is super ugly.
        while (!this.lookahead(TokenType.CLOSING_BRACE)) {
            boolean isStaticMethod = this.lookahead(TokenType.PUBLIC, TokenType.STATIC);
            MemberDeclaration declaration = this.parseClassMember();

            if (declaration instanceof MethodDeclaration) {
                if (isStaticMethod) {
                    staticMethodDeclarations.add((MethodDeclaration)declaration);
                }
                else {
                    instanceMethodDeclarations.add((MethodDeclaration)declaration);
                }
            }
            else if (declaration instanceof FieldDeclaration) {
                fieldDeclarations.add((FieldDeclaration)declaration);
            }
            else {
                throw new AssertionError();
            }
        }

        this.consume(TokenType.CLOSING_BRACE, "ClassDeclaration");

        return new ClassDeclaration(name, staticMethodDeclarations, instanceMethodDeclarations, fieldDeclarations);
    }

    private MemberDeclaration parseClassMember() throws ParserException {
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

            Statement.Block body = this.parseBlock();

            TypeReference returnType = new TypeReference(new BasicTypeReference("void", PrimitiveTypeDeclaration.VOID), 0);
            TypeReference parameterType = new TypeReference(new BasicTypeReference("String"), 1);
            ParameterDeclaration parameter = new ParameterDeclaration(parameterType, parameterName);
            List<ParameterDeclaration> parameters = Collections.singletonList(parameter);

            return new MethodDeclaration(returnType, methodName, parameters, body);
        }

        // ClassMember -> Method | Field
        else {
            TypeReference type = this.parseType();
            String name = this.consume(TokenType.IDENTIFIER, "ClassMember").text;

            // ClassMember -> Field
            if (this.lookahead(TokenType.SEMICOLON)) {
                this.consume(TokenType.SEMICOLON, "Field");

                return new FieldDeclaration(type, name);
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

                return new MethodDeclaration(type, name, parameters, body);
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
        TypeReference type = this.parseType();
        String name = this.consume(TokenType.IDENTIFIER, "Parameter").text;

        return new ParameterDeclaration(type, name);
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
        this.consume(TokenType.OPENING_BRACE, "Block");

        List<Statement> statements = new ArrayList<>();

        while (!this.lookahead(TokenType.CLOSING_BRACE)) {
            statements.add(this.parseBlockStatement());
        }

        this.consume(TokenType.CLOSING_BRACE, "Block");

        return new Statement.Block(statements);
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
        TypeReference type = this.parseType();
        String name = this.consume(TokenType.IDENTIFIER, "LocalVariableDeclarationStatement").text;

        // LocalVariableDeclarationStatement -> Type "IDENTIFIER" "=" Expression ";"
        if (this.lookahead(TokenType.ASSIGN)) {
            this.consume(TokenType.ASSIGN, "LocalVariableDeclarationStatement");
            Expression value = this.parseExpression(0);
            this.consume(TokenType.SEMICOLON, "LocalVariableDeclarationStatement");

            return new Statement.LocalVariableDeclarationStatement(type, name, value);
        }

        // LocalVariableDeclarationStatement -> Type "IDENTIFIER"  ";"
        else {
            this.consume(TokenType.SEMICOLON, "LocalVariableDeclarationStatement");

            return new Statement.LocalVariableDeclarationStatement(type, name);
        }
    }

    private Statement parseEmptyStatement() throws ParserException {
        this.consume(TokenType.SEMICOLON, "EmptyStatement");

        return new Statement.EmptyStatement();
    }

    private Statement parseWhileStatement() throws ParserException {
        this.consume(TokenType.WHILE, "WhileStatement");
        this.consume(TokenType.OPENING_PARENTHESIS, "WhileStatement");
        Expression condition = this.parseExpression(0);
        this.consume(TokenType.CLOSING_PARENTHESIS, "WhileStatement");
        Statement statementWhileTrue = this.parseStatement();

        return new Statement.WhileStatement(condition, statementWhileTrue);
    }

    private Statement parseIfStatement() throws ParserException {
        this.consume(TokenType.IF, "IfStatement");
        this.consume(TokenType.OPENING_PARENTHESIS, "IfStatement");
        Expression condition = this.parseExpression(0);
        this.consume(TokenType.CLOSING_PARENTHESIS, "IfStatement");
        Statement statementIfTrue = this.parseStatement();

        // IfStatement -> "if" "(" Expression ")" Statement "else" Statement
        if (this.lookahead(TokenType.ELSE)) {
            this.consume(TokenType.ELSE, "IfStatement");

            Statement statementIfFalse = this.parseStatement();

            return new Statement.IfStatement(condition, statementIfTrue, statementIfFalse);
        }

        // IfStatement -> "if" "(" Expression ")" Statement
        else {
            return new Statement.IfStatement(condition, statementIfTrue);
        }
    }

    private Statement parseExpressionStatement() throws ParserException {
        Expression expression = this.parseExpression(0);

        this.consume(TokenType.SEMICOLON, "ExpressionStatement");

        return new Statement.ExpressionStatement(expression);
    }

    private Statement parseReturnStatement() throws ParserException {
        this.consume(TokenType.RETURN, "ReturnStatement");

        // ReturnStatement -> "return" ";"
        if (this.lookahead(TokenType.SEMICOLON)) {
            this.consume(TokenType.SEMICOLON, "ReturnStatement");

            return new Statement.ReturnStatement();
        }

        // ReturnStatement -> "return" Expression ";"
        else {
            Expression value = this.parseExpression(0);
            this.consume(TokenType.SEMICOLON, "ReturnStatement");

            return new Statement.ReturnStatement(value);
        }
    }

    // MARK: - Parsing Expressions

    private Expression parseExpression(int minimumPrecedence) throws ParserException {
        Stack<TokenType> consumedPrefixOperators = new Stack<>();

        // 1. Consume prefix operators
        consumePrefixOperators:
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
                    break consumePrefixOperators;
            }
        }

        // 2. Parse primary expression
        Expression expression = this.parsePrimaryExpression();

        // 3. Consume postfix operations (they have higher precedence than prefix operators)
        consumePostfixOperations:
        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case PERIOD:
                case OPENING_BRACKET:
                    expression = this.parsePostfixOperationWithContext(expression);
                    break;
                default:
                    break consumePostfixOperations;
            }
        }

        // 4. Apply consumed prefix operators now.
        while (!consumedPrefixOperators.isEmpty()) {
            switch (consumedPrefixOperators.pop()) {
                case LOGICAL_NEGATION:
                    expression = new Expression.UnaryOperation(UnaryOperationType.LOGICAL_NEGATION, expression);
                    break;
                case MINUS:
                    expression = new Expression.UnaryOperation(UnaryOperationType.NUMERIC_NEGATION, expression);
                    break;
                default:
                    throw new Error();
            }
        }

        // 5. Precedence climbing with 'atom' including prefix operators and postfix operations.
        while (!this.hasReachedEndOfInput()) {
            BinaryOperation operation = BinaryOperation.forTokenType(this.getCurrentToken().type);

            if (operation == null || operation.precedence < minimumPrecedence) {
                break;
            } else {
                this.consume(operation.tokenType, null);
            }

            int precedence = operation.precedence;

            if (operation.associativity == Associativity.LEFT_ASSOCIATIVE) {
                precedence = precedence + 1;
            }

            Expression rhs = this.parseExpression(precedence);

            expression = operation.instantiate(expression, rhs);
        }

        return expression;
    }

    private Expression parsePostfixOperationWithContext(Expression context) throws ParserException {

        // PostfixOperation -> MethodInvocation | FieldAccess
        if (this.lookahead(TokenType.PERIOD)) {
            this.consume(TokenType.PERIOD, "PostfixOperation");
            String identifier = this.consume(TokenType.IDENTIFIER, "PostfixOperation").text;

            if (this.lookahead(TokenType.OPENING_PARENTHESIS)) {
                this.consume(TokenType.OPENING_PARENTHESIS, "MethodInvocation");
                List<Expression> arguments = this.parseArguments();
                this.consume(TokenType.CLOSING_PARENTHESIS, "MethodInvocation");

                return new Expression.MethodInvocation(context, identifier, arguments);
            } else {
                return new Expression.ExplicitFieldAccess(context, identifier);
            }
        }

        // PostfixOperation -> ArrayAccess
        else if (this.lookahead(TokenType.OPENING_BRACKET)) {
            this.consume(TokenType.OPENING_BRACKET, "ArrayAccess");
            Expression index = this.parseExpression(0);
            this.consume(TokenType.CLOSING_BRACKET, "ArrayAccess");

            return new Expression.ArrayElementAccess(context, index);
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

            return expression;
        }

        // PrimaryExpression -> "IDENTIFIER" | "IDENTIFIER" "(" Arguments ")"
        else if (this.lookahead(TokenType.IDENTIFIER)) {
            String identifier = this.consume(TokenType.IDENTIFIER, "PrimaryExpression").text;

            if (this.lookahead(TokenType.OPENING_PARENTHESIS)) {
                this.consume(TokenType.OPENING_PARENTHESIS, "PrimaryExpression");
                List<Expression> arguments = this.parseArguments();
                this.consume(TokenType.CLOSING_PARENTHESIS, "PrimaryExpression");

                return new Expression.MethodInvocation(null, identifier, arguments);
            } else {
                return new Expression.VariableAccess(identifier);
            }
        }

        // PrimaryExpression -> Literal -> "INTEGER_LITERAL"
        else if (this.lookahead(TokenType.INTEGER_LITERAL)) {
            String value = this.consume(TokenType.INTEGER_LITERAL, null).text;

            return new Expression.IntegerLiteral(value);
        }

        // PrimaryExpression -> Literal -> "null"
        else if (this.lookahead(TokenType.NULL)) {
            this.consume(TokenType.NULL, null);

            return new Expression.NullLiteral();
        }

        // PrimaryExpression -> Literal -> "true"
        else if (this.lookahead(TokenType.TRUE)) {
            this.consume(TokenType.TRUE, null);

            return new Expression.BooleanLiteral(true);
        }

        // PrimaryExpression -> Literal -> "false"
        else if (this.lookahead(TokenType.FALSE)) {
            this.consume(TokenType.FALSE, null);

            return new Expression.BooleanLiteral(false);
        }

        // PrimaryExpression -> "this"
        else if (this.lookahead(TokenType.THIS)) {
            this.consume(TokenType.THIS, null);

            return new Expression.CurrentContextAccess();
        }

        // PrimaryExpression -> NewObjectExpression | NewArrayExpression
        else if (this.lookahead(TokenType.NEW)) {
            this.consume(TokenType.NEW, null);

            // PrimaryExpression -> NewObjectExpression -> "new" "IDENTIFIER" "(" ")"
            if (this.lookahead(TokenType.IDENTIFIER, TokenType.OPENING_PARENTHESIS)) {
                String className = this.consume(TokenType.IDENTIFIER, "NewObjectExpression").text;
                this.consume(TokenType.OPENING_PARENTHESIS, "NewObjectExpression");
                this.consume(TokenType.CLOSING_PARENTHESIS, "NewObjectExpression");

                return new Expression.NewObjectCreation(className);
            }

            // PrimaryExpression -> NewArrayExpression -> "new" BasicType "[" Expression "]" { "[" "]" }
            else {
                BasicTypeReference basicType = this.parseBasicType();
                this.consume(TokenType.OPENING_BRACKET, "NewArrayExpression");
                Expression expression = this.parseExpression(0);
                this.consume(TokenType.CLOSING_BRACKET, "NewArrayExpression");
                int numberOfDimensions = 1 + this.parseOpeningAndClosingBrackets();

                return new Expression.NewArrayCreation(basicType, expression, numberOfDimensions);
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

    private TypeReference parseType() throws ParserException {
        BasicTypeReference basicType = this.parseBasicType();
        int numberOfDimensions = this.parseOpeningAndClosingBrackets();

        return new TypeReference(basicType, numberOfDimensions);
    }

    private BasicTypeReference parseBasicType() throws ParserException {

        // BasicType -> "IDENTIFIER"
        if (this.lookahead(TokenType.IDENTIFIER)) {
            String className = this.consume(TokenType.IDENTIFIER, null).text;

            return new BasicTypeReference(className);
        }

        // BasicType -> "int"
        else if (this.lookahead(TokenType.INT)) {
            String name = this.consume(TokenType.INT, null).text;

            return new BasicTypeReference(name, PrimitiveTypeDeclaration.INTEGER);
        }

        // BasicType -> "boolean"
        else if (this.lookahead(TokenType.BOOLEAN)) {
            String name = this.consume(TokenType.BOOLEAN, null).text;

            return new BasicTypeReference(name, PrimitiveTypeDeclaration.BOOLEAN);
        }

        // BasicType -> "void"
        else if (this.lookahead(TokenType.VOID)) {
            String name = this.consume(TokenType.VOID, null).text;

            return new BasicTypeReference(name, PrimitiveTypeDeclaration.VOID);
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
