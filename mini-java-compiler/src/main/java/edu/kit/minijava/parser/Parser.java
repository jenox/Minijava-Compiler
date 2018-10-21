package edu.kit.minijava.parser;

import edu.kit.minijava.lexer.*;
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

    private Token getCurrentToken() {
        return this.getTokenAtOffset(0);
    }

    private Token getTokenAtOffset(int offset) {
        while (this.tokens.size() <= offset && !this.hasReceivedEndOfInputFromLexer) {
            Token token = this.lexer.nextToken();

            if (token != null) {
                this.tokens.add(token);
            } else {
                this.hasReceivedEndOfInputFromLexer = true;
                break;
            }
        }

        if (offset < this.tokens.size()) {
            return this.tokens.get(offset);
        } else {
            return null;
        }
    }

    private boolean hasReachedEndOfInput() {
        return this.getCurrentToken() == null;
    }

    private boolean check(TokenType type) {
        Token token = this.getCurrentToken();

        if (token == null) {
            return false;
        } else {
            return token.type == type;
        }
    }

    private boolean lookahead(TokenType first, TokenType... others) {
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

    private Token consume(TokenType type) {
        Token token = this.getCurrentToken();

        if (token == null) {
            throw new RuntimeException("Expected to read " + type +  ", but found end of file");
        } else if (token.type != type) {
            throw new RuntimeException("Expected to read " + type +  ", but found " + token.type);
        }

        this.tokens.remove(0);

        return token;
    }

    // MARK: - Parsing MiniJava Files

    public Program parseProgram() {
        List<ClassDeclaration> classes = new ArrayList<>();

        while (!this.hasReachedEndOfInput()) {
            classes.add(this.parseClassDeclaration());
        }

        return new Program(classes);
    }

    private ClassDeclaration parseClassDeclaration() {
        this.consume(TokenType.CLASS);

        String name = this.consume(TokenType.IDENTIFIER).text;
        List<ClassMember> members = new ArrayList<>();

        this.consume(TokenType.OPENING_BRACE);

        while (this.lookahead(TokenType.PUBLIC)) {
            members.add(this.parseClassMember());
        }

        this.consume(TokenType.CLOSING_BRACE);

        return new ClassDeclaration(name, members);
    }

    private ClassMember parseClassMember() {
        if (this.hasReachedEndOfInput()) {
            throw new RuntimeException();
        }

        this.consume(TokenType.PUBLIC);

        switch (this.getCurrentToken().type) {
            case STATIC: {
                this.consume(TokenType.STATIC);
                this.consume(TokenType.VOID);

                String methodName = this.consume(TokenType.IDENTIFIER).text;

                this.consume(TokenType.OPENING_PARENTHESIS);

                // TODO: String appears to be keyword on sheet?
                String parameterType = this.consume(TokenType.IDENTIFIER).text;
                if (!parameterType.equals("String")) {
                    throw new RuntimeException();
                }

                this.consume(TokenType.OPENING_BRACKET);
                this.consume(TokenType.CLOSING_BRACKET);

                String parameterName = this.consume(TokenType.IDENTIFIER).text;

                this.consume(TokenType.CLOSING_PARENTHESIS);

                if (this.lookahead(TokenType.THROWS)) {
                    this.consume(TokenType.THROWS);
                    this.consume(TokenType.IDENTIFIER);
                }

                Block body = this.parseBlock();

                return new MainMethod(methodName, parameterName, body);
            }
            case INT:
            case BOOLEAN:
            case VOID:
            case IDENTIFIER: {
                Type type = this.parseType();
                String name = this.consume(TokenType.IDENTIFIER).text;

                if (this.lookahead(TokenType.SEMICOLON)) {
                    this.consume(TokenType.SEMICOLON);

                    return new Field(type, name);
                } else {
                    this.consume(TokenType.OPENING_PARENTHESIS);

                    List<Parameter> parameters = this.parseParameters();

                    this.consume(TokenType.CLOSING_PARENTHESIS);

                    if (this.lookahead(TokenType.THROWS)) {
                        this.consume(TokenType.THROWS);
                        this.consume(TokenType.IDENTIFIER);
                    }

                    Block body = this.parseBlock();

                    return new Method(type, name, parameters, body);
                }
            }
            default:
                throw new RuntimeException();
        }
    }

    // MARK: - Parsing Parameters & Types

    private List<Parameter> parseParameters() {
        List<Parameter> parameters = new ArrayList<>();

        if (this.currentTokenIsInFirstOfParameter()) {
            parameters.add(this.parseParameter());
        }

        while (this.lookahead(TokenType.COMMA)) {
            this.consume(TokenType.COMMA);

            parameters.add(this.parseParameter());
        }

        return parameters;
    }

    private Parameter parseParameter() {
        Type type = this.parseType();
        String name = this.consume(TokenType.IDENTIFIER).text;

        return new Parameter(type, name);
    }

    private Type parseType() {
        BasicType basicType = this.parseBasicType();
        int numberOfDimensions = this.parseOpeningAndClosingBrackets();

        return new Type(basicType, numberOfDimensions);
    }

    private BasicType parseBasicType() {
        if (this.hasReachedEndOfInput()) {
            throw new RuntimeException();
        }

        switch (this.getCurrentToken().type) {
            case INT:
                this.consume(TokenType.INT);
                return new IntegerType();
            case BOOLEAN:
                this.consume(TokenType.BOOLEAN);
                return new BooleanType();
            case VOID:
                this.consume(TokenType.VOID);
                return new VoidType();
            case IDENTIFIER:
                Token token = this.consume(TokenType.IDENTIFIER);
                return new UserDefinedType(token.text);
            default:
                throw new RuntimeException("Bad BasicType");
        }
    }

    private boolean currentTokenIsInFirstOfParameter() {
        if (this.hasReachedEndOfInput()) {
            return false;
        }

        switch (this.getCurrentToken().type) {
            case INT:
            case BOOLEAN:
            case VOID:
            case IDENTIFIER:
                return true;
            default:
                return false;
        }
    }

    // MARK: - Parsing Statements

    public Statement parseStatement() {
        if (this.hasReachedEndOfInput()) {
            throw new RuntimeException();
        }

        switch (this.getCurrentToken().type) {
            case OPENING_BRACE:
                return this.parseBlock();
            case SEMICOLON:
                return this.parseEmptyStatement();
            case IF:
                return this.parseIfStatement();
            case WHILE:
                return this.parseWhileStatement();
            case RETURN:
                return this.parseReturnStatement();
            case LOGICAL_NEGATION:
            case MINUS:
            case FALSE:
            case TRUE:
            case INTEGER_LITERAL:
            case IDENTIFIER:
            case THIS:
            case OPENING_PARENTHESIS:
            case NEW:
                return this.parseExpressionStatement();
            default:
                throw new RuntimeException();
        }
    }

    private Block parseBlock() {
        this.consume(TokenType.OPENING_BRACE);

        List<BlockStatement> statements = new ArrayList<>();

        while (this.currentCharacterIsInFirstOfBlockStatement()) {
            statements.add(this.parseBlockStatement());
        }

        this.consume(TokenType.CLOSING_BRACE);

        return new Block(statements);
    }

    private BlockStatement parseBlockStatement() {
        if (this.hasReachedEndOfInput()) {
            throw new RuntimeException();
        }

        switch (this.getCurrentToken().type) {
            case INT:
            case BOOLEAN:
            case VOID:
                return this.parseLocalVariableDeclarationStatement();
            case OPENING_BRACE:
            case SEMICOLON:
            case IF:
            case WHILE:
            case RETURN:
            case LOGICAL_NEGATION:
            case MINUS:
            case NULL:
            case FALSE:
            case TRUE:
            case INTEGER_LITERAL:
            case THIS:
            case OPENING_PARENTHESIS:
            case NEW:
                return this.parseStatement();
            case IDENTIFIER:
                // IDENTIFIER is in both first(Statement) and first(LocalVariableDeclaration)
                if (this.lookahead(TokenType.IDENTIFIER, TokenType.IDENTIFIER)) {
                    return this.parseLocalVariableDeclarationStatement();
                } else if (this.lookahead(TokenType.IDENTIFIER, TokenType.OPENING_BRACKET, TokenType.CLOSING_BRACKET)) {
                    return this.parseLocalVariableDeclarationStatement();
                } else {
                    return this.parseStatement();
                }
            default:
                throw new RuntimeException();
        }
    }

    private BlockStatement parseLocalVariableDeclarationStatement() {
        Type type = this.parseType();
        String name = consume(TokenType.IDENTIFIER).text;

        if (this.check(TokenType.ASSIGN)) {
            this.consume(TokenType.ASSIGN);
            Expression value = this.parseExpression();
            this.consume(TokenType.SEMICOLON);

            return new LocalVariableInitializationStatement(type, name, value);
        } else {
            this.consume(TokenType.SEMICOLON);

            return new LocalVariableDeclarationStatement(type, name);
        }
    }

    private Statement parseEmptyStatement() {
        this.consume(TokenType.SEMICOLON);

        return new EmptyStatement();
    }

    private Statement parseWhileStatement() {
        this.consume(TokenType.WHILE);
        this.consume(TokenType.OPENING_PARENTHESIS);
        Expression condition = this.parseExpression();
        this.consume(TokenType.CLOSING_PARENTHESIS);
        Statement statementWhileTrue = this.parseStatement();

        return new WhileStatement(condition, statementWhileTrue);
    }

    private Statement parseIfStatement() {
        this.consume(TokenType.IF);
        this.consume(TokenType.OPENING_PARENTHESIS);
        Expression condition = this.parseExpression();
        this.consume(TokenType.CLOSING_PARENTHESIS);
        Statement statementIfTrue = this.parseStatement();

        if (this.check(TokenType.ELSE)) {
            this.consume(TokenType.ELSE);

            Statement statementIfFalse = this.parseStatement();

            return new IfElseStatement(condition, statementIfTrue, statementIfFalse);
        }  else {
            return new IfStatement(condition, statementIfTrue);
        }
    }

    private Statement parseExpressionStatement() {
        Expression expression = parseExpression();

        this.consume(TokenType.SEMICOLON);

        return new ExpressionStatement(expression);
    }

    private Statement parseReturnStatement() {
        this.consume(TokenType.RETURN);

        if (this.currentCharacterIsInFirstOfExpression()) {
            Expression expression = parseExpression();
            this.consume(TokenType.SEMICOLON);

            return new ReturnValueStatement(expression);
        } else {
            this.consume(TokenType.SEMICOLON);

            return new ReturnNoValueStatement();
        }
    }

    private boolean currentCharacterIsInFirstOfBlockStatement() {
        if (this.hasReachedEndOfInput()) {
            return false;
        }

        switch (this.getCurrentToken().type) {
            case OPENING_BRACE:
            case SEMICOLON:
            case IF:
            case WHILE:
            case RETURN:
            case INT:
            case BOOLEAN:
            case VOID:
            case IDENTIFIER:
            case LOGICAL_NEGATION:
            case MINUS:
            case NULL:
            case FALSE:
            case TRUE:
            case INTEGER_LITERAL:
            case THIS:
            case OPENING_PARENTHESIS:
            case NEW:
                return true;
            default:
                return false;
        }
    }

    // MARK: - Parsing Expressions

    public Expression parseExpression() {
        return this.parseAssignmentExpression();
    }

    private Expression parseAssignmentExpression() {
        Expression expression = this.parseLogicalOrExpression();

        if (this.check(TokenType.ASSIGN)) {
            return new AssignmentExpression(expression, this.parseAssignmentExpression());
        } else {
            return expression;
        }
    }

    private Expression parseLogicalOrExpression() {
        Expression expression = this.parseLogicalAndExpression();

        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case LOGICAL_OR:
                    this.consume(TokenType.LOGICAL_OR);
                    expression = new LogicalOrExpression(expression, this.parseLogicalAndExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseLogicalAndExpression() {
        Expression expression = this.parseEqualityExpression();

        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case LOGICAL_AND:
                    this.consume(TokenType.LOGICAL_AND);
                    expression = new LogicalAndExpression(expression, this.parseEqualityExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseEqualityExpression() {
        Expression expression = this.parseRelationalExpression();

        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case EQUAL_TO:
                    this.consume(TokenType.EQUAL_TO);
                    expression = new EqualToExpression(expression, this.parseRelationalExpression());
                    break;
                case NOT_EQUAL_TO:
                    this.consume(TokenType.NOT_EQUAL_TO);
                    expression = new NotEqualToExpression(expression, this.parseRelationalExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseRelationalExpression() {
        Expression expression = this.parseAdditiveExpression();

        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case LESS_THAN:
                    this.consume(TokenType.LESS_THAN);
                    expression = new LessThanExpression(expression, this.parseAdditiveExpression());
                    break;
                case LESS_THAN_OR_EQUAL_TO:
                    this.consume(TokenType.LESS_THAN_OR_EQUAL_TO);
                    expression = new LessThanOrEqualToExpression(expression, this.parseAdditiveExpression());
                    break;
                case GREATER_THAN:
                    this.consume(TokenType.GREATER_THAN);
                    expression = new GreaterThanExpression(expression, this.parseAdditiveExpression());
                    break;
                case GREATER_THAN_OR_EQUAL_TO:
                    this.consume(TokenType.GREATER_THAN_OR_EQUAL_TO);
                    expression = new GreaterThanOrEqualToExpression(expression, this.parseAdditiveExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseAdditiveExpression() {
        Expression expression = this.parseMultiplicativeExpression();

        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case PLUS:
                    this.consume(TokenType.PLUS);
                    expression = new AddExpression(expression, this.parseMultiplicativeExpression());
                    break;
                case MINUS:
                    this.consume(TokenType.MINUS);
                    expression = new SubtractExpression(expression, this.parseMultiplicativeExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseMultiplicativeExpression() {
        Expression expression = this.parseUnaryExpression();

        while (!this.hasReachedEndOfInput()) {
            switch (this.getCurrentToken().type) {
                case MULTIPLY:
                    this.consume(TokenType.MULTIPLY);
                    expression = new MultiplyExpression(expression, this.parseUnaryExpression());
                    break;
                case DIVIDE:
                    this.consume(TokenType.DIVIDE);
                    expression = new DivideExpression(expression, this.parseUnaryExpression());
                    break;
                case MODULO:
                    this.consume(TokenType.MODULO);
                    expression = new ModuloExpression(expression, this.parseUnaryExpression());
                    break;
                default:
                    return expression;
            }
        }

        return expression;
    }

    private Expression parseUnaryExpression() {
        if (this.hasReachedEndOfInput()) {
            throw new RuntimeException();
        }

        switch (this.getCurrentToken().type) {
            case NULL:
            case TRUE:
            case FALSE:
            case INTEGER_LITERAL:
            case IDENTIFIER:
            case THIS:
            case OPENING_PARENTHESIS:
            case NEW:
                return this.parsePostfixExpression();
            case LOGICAL_NEGATION:
                return new LogicalNotExpression(this.parseUnaryExpression());
            case MINUS:
                return new NegateExpression(this.parseUnaryExpression());
            default:
                throw new RuntimeException();
        }
    }

    private Expression parsePostfixExpression() {
        Expression expression = this.parsePrimaryExpression();

        if (this.hasReachedEndOfInput()) {
            return expression;
        }

        switch (this.getCurrentToken().type) {
            case PERIOD:
            case OPENING_BRACKET:
                return new PostfixExpression(expression, this.parsePostfixOperation());
            default:
                return expression;
        }
    }

    private PostfixOperation parsePostfixOperation() {
        if (this.check(TokenType.PERIOD)) {
            this.consume(TokenType.PERIOD);
            String identifier = this.consume(TokenType.IDENTIFIER).text;

            if (this.check(TokenType.OPENING_PARENTHESIS)) {
                this.consume(TokenType.OPENING_PARENTHESIS);
                List<Expression> arguments = this.parseArguments();
                this.consume(TokenType.CLOSING_PARENTHESIS);

                return new MethodInvocation(identifier, arguments);
            } else {
                return new FieldAccess(identifier);
            }
        } else if (this.check(TokenType.OPENING_BRACKET)) {
            this.consume(TokenType.OPENING_BRACKET);
            Expression expression = this.parseExpression();
            this.consume(TokenType.CLOSING_BRACKET);

            return new ArrayAccess(expression);
        } else {
            throw new RuntimeException();
        }
    }

    private List<Expression> parseArguments() {
        List<Expression> exp_list = new ArrayList<>();

        if (this.currentCharacterIsInFirstOfExpression()) {
            exp_list.add(this.parseExpression());
            while (this.check(TokenType.COMMA)) {
                this.consume(TokenType.COMMA);
                exp_list.add(this.parseExpression());
            }
        }

        return exp_list;
    }

    private Expression parsePrimaryExpression() {
        switch (this.getCurrentToken().type) {
            case NULL:
                this.consume(TokenType.NULL);
                return new NullLiteral();
            case FALSE:
                this.consume(TokenType.FALSE);
                return new BooleanLiteral(false);
            case TRUE:
                this.consume(TokenType.TRUE);
                return new BooleanLiteral(true);
            case INTEGER_LITERAL: {
                String value = this.consume(TokenType.INTEGER_LITERAL).text;
                try {
                    return new IntegerLiteral(Integer.parseInt(value));
                } catch (NumberFormatException exception) {
                    throw new RuntimeException();
                }
            }
            case IDENTIFIER: {
                String identifier = this.consume(TokenType.IDENTIFIER).text;
                if (this.check(TokenType.OPENING_PARENTHESIS)) {
                    this.consume(TokenType.OPENING_PARENTHESIS);
                    List<Expression> arguments = this.parseArguments();
                    this.consume(TokenType.CLOSING_PARENTHESIS);
                    return new IdentifierAndArgumentsExpression(identifier, arguments);
                } else {
                    return new IdentifierExpression(identifier);
                }
            }
            case THIS:
                this.consume(TokenType.THIS);
                return new ThisExpression();
            case OPENING_PARENTHESIS: {
                this.consume(TokenType.OPENING_PARENTHESIS);
                Expression expression = this.parseExpression();
                this.consume(TokenType.CLOSING_PARENTHESIS);
                return expression;
            }
            case NEW: {
                this.consume(TokenType.NEW);

                if (this.hasReachedEndOfInput()) {
                    throw new RuntimeException();
                }

                BasicType basicType = null;

                switch (this.getCurrentToken().type) {
                    case IDENTIFIER: {
                        String identifier = this.consume(TokenType.IDENTIFIER).text;
                        if (this.check(TokenType.OPENING_PARENTHESIS)) {
                            this.consume(TokenType.OPENING_PARENTHESIS);
                            this.consume(TokenType.CLOSING_PARENTHESIS);
                            return new NewObjectExpression(identifier);
                        } else if (this.check(TokenType.OPENING_BRACKET)) {
                            this.consume(TokenType.OPENING_BRACKET);
                            basicType = new UserDefinedType(identifier);
                        }
                    }
                    case INT:
                    case BOOLEAN:
                    case VOID: {
                        if (basicType == null) {
                            basicType = this.parseBasicType();
                            this.consume(TokenType.OPENING_BRACKET);
                        }
                        Expression expression = this.parseExpression();
                        this.consume(TokenType.CLOSING_BRACKET);
                        int numberOfDimensions = 1 + this.parseOpeningAndClosingBrackets();
                        return new NewArrayExpression(basicType, expression, numberOfDimensions);
                    }
                    default:
                        throw new RuntimeException("PrimaryExpression not valid");
                }
            }
            default:
                throw new RuntimeException("parsePrimary");
        }
    }

    private boolean currentCharacterIsInFirstOfExpression() {
        if (this.hasReachedEndOfInput()) {
            return false;
        }

        switch (this.getCurrentToken().type) {
            case LOGICAL_NEGATION:
            case MINUS:
            case NULL:
            case FALSE:
            case TRUE:
            case INTEGER_LITERAL:
            case IDENTIFIER:
            case THIS:
            case OPENING_PARENTHESIS:
            case NEW:
                return true;
            default:
                return false;
        }
    }

    // MARK: - Miscellaneous

    private int parseOpeningAndClosingBrackets() {
        int count = 0;

        while (this.check(TokenType.OPENING_BRACKET)) {
            this.consume(TokenType.OPENING_BRACKET);
            this.consume(TokenType.CLOSING_BRACKET);

            count += 1;
        }

        return count;
    }
}
