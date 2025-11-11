package woowa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import woowa.Expr.Binary;
import woowa.Expr.Grouping;
import woowa.Expr.Literal;
import woowa.Expr.Unary;
import woowa.Stmt.Function;
import woowa.Stmt.Return;

/**
 * 인터프리터 (Interpreter) AST 를 순회하며 실제로 표현식을 평가
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    // 시간 측정
    Interpreter() {
        globals.define("clock", new WoowaCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    /**
     * 1. 표현식을 평가(evaluate)
     * 2. 결과를 문자열로 변환(stringify)
     * 3. 콘솔에 출력
     * 4. 런타임 에러 발생 시 에러 처리
     */
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement); // 각 문장 실행
            }
        } catch (RuntimeError error) {
            Woowa.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof WoowaClass)) {
                throw new RuntimeError(stmt.superclass.name, "슈퍼클래스는 클래스여야 한다.");
            }
        }

        environment.define(stmt.name.lexeme, null);

        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, WoowaFunction> methods = new HashMap<>();
        for (Function method : stmt.methods) {
            WoowaFunction function = new WoowaFunction(method, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }

        WoowaClass klass = new WoowaClass(stmt.name.lexeme, (WoowaClass) superclass, methods);

        if (superclass != null) {
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, klass);
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        // 블록 실행 후 원래 로 돌아가기 위해 현재 환경 백업
        Environment previous = this.environment;
        try {
            // 새로운 환경으로 전환
            this.environment = environment;

            // 블록 내부 문장 순차적으로 실행
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            // 환경 복원
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        WoowaFunction function = new WoowaFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        // return 값이 있으면 평가하고 없으면 nil 을 반환한다.
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new woowa.Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                // 숫자일 경우 연산
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                // 문자일 경우 문자열 합치기
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                throw new RuntimeError(expr.operator, "피연산자는 두개의 숫자거나 두개의 문장여야 합니다.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
        }

        // 실행되지 않는 코드
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof WoowaCallable)) {
            throw new RuntimeError(expr.paren, "함수와 객체만 호출할 수 있습니다.");
        }

        WoowaCallable function = (WoowaCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren,
                function.arity() + "개의 인자가 기대됬으나 " + arguments.size() + "개가 넘어왔습니다.");
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof WoowaInstance) {
            return ((WoowaInstance) object).get(expr.name);
        }

        throw new RuntimeError(expr.name, "오직 인스턴스만이 속성을 가집니다.");
    }

    // 괄호는 단순히 내부 표현식을 먼저 평가하라는 의미
    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    // 리터럴은 이미 값 자체이므로 그대로 반환
    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        // 좌측 피 연산자 먼저 평가
        Object left = evaluate(expr.left);

        // 쇼트 서킷이 아닌 경우만 우측 피연산자 평가
        // 참 or ? 인경우 우측 피 연산자 평가 X
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            }
        } else {
            if (!isTruthy(left)) {
                return left;
            }
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof WoowaInstance)) {
            throw new RuntimeError(expr.name, "오직 인스턴스만이 필드를 가집니다.");
        }

        Object value = evaluate(expr.value);
        ((WoowaInstance) object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        WoowaClass superclass = (WoowaClass) environment.getAt(distance, "super");

        WoowaInstance object = (WoowaInstance) environment.getAt(distance - 1, "this");

        WoowaFunction method = superclass.findMethod(expr.method.lexeme);

        if (method == null) {
            throw new RuntimeError(expr.method, "정의되지 않은 속성 '" + expr.method.lexeme + "'.");
        }

        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        // 실행되지 않는 코드
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    // 런타임 에러 감지용 메서드
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "피연산자는 숫자여야 합니다.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "피연산자들은 숫자여야 합니다.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }

        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }

        return a.equals(b);
    }

    /**
     * 평가 결과를 사용자 친화적인 문자열로 변환
     * - null -> "nil"
     * - 정수형 double -> 소수점 제거 (3.0 -> "3")
     * - 그 외 -> toString() 사용
     */
    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();
            // 정수일 경우 ".0" 제거 3.0 -> 3
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
