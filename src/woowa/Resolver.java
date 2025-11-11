package woowa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Resolver
 *
 * 코드 실행 전에 변수 스코프를 미리 분석하여 각 변수가 몇 단계 위 스코프에 있는지 계산
 */
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Interpreter interpreter;
    // 활성 스코프를 저장 <변수이름 초기화 여부>
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    // 함수 컨텍스트 추적
    private FunctionType currentFunction = FunctionType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE, // 최상위 레벨
        FUNCTION, // 함수 레벨
        INITIALIZER,
        METHOD
    }

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }

    private ClassType currentClass = ClassType.NONE;

    // 블록문은 자신이 포함한 문장들에 해당하는 새로운 스코프를 시작한다.
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope(); // 새 스코프 시작
        resolve(stmt.statements); // 블록 내부 분석
        endScope(); // 스코프 종료
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);

        if (stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
            Woowa.error(stmt.superclass.name, "클래스는 스스로를 상복받을 수 없습니다.");
        }

        if (stmt.superclass != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass);
        }

        if (stmt.superclass != null) {
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;

            if (method.name.lexeme.equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }

        endScope();

        if (stmt.superclass != null) {
            endScope();
        }

        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    // 재귀 호출을 허용하기 위해 선언과 동시에 정의
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name); // 함수 이름 선언
        define(stmt.name); // 즉시 정의

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition); // 조건식
        resolve(stmt.thenBranch); // if 본문
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch); // else 본문
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression); // 출력할 부분만 분석
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Woowa.error(stmt.keyword, "최상위 코드에서 반환할 수 없습니다.");
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Woowa.error(stmt.keyword, "초기자에서 값을 리턴할 수 없습니다.");
            }
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name); // 선언
        if (stmt.initializer != null) { // 초기식 계산
            resolve(stmt.initializer);
        }
        define(stmt.name); // 정의
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value); // 우변 먼저
        resolveLocal(expr, expr.name); // 좌변 변수 위치 계산
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }


    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            Woowa.error(expr.keyword, "클래스 밖에 'super' 를 사용할 수 없습니다.");
        } else if (currentClass != ClassType.SUBCLASS) {
            Woowa.error(expr.keyword, "슈퍼 클래스 없이 'super' 를 사용 할 수 없습니다.");
        }

        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Woowa.error(expr.keyword, "클래스 외부에서 'this' 를 사용할 수 없습니다.");
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        // 변수가 선언은 됐지만 초기화는 안 된 상태인지 확인
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Woowa.error(expr.name, "초기자에서 지역 변수를 읽을 수 없습니다.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    // 새 스코프를 열고 블록 안 문장을 순회한 다음 스코프를 폐기한다.
    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction; // 백업
        currentFunction = type; // 현재 함수 타입 설정

        beginScope();
        // 파라미터들을 스코프에 추가
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction; // 복원
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    // 현재 스코프에 변수를 false 로 추가
    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme)) {
            Woowa.error(name, "같은 스코프에 이미 변수가 존재합니다.");
        }
        // false 로 바인딩해서 아직 준비 중 표시
        scope.put(name.lexeme, false);
    }


    // true 로 변경해서 초기화 완료 및 사용가능 변수 표기
    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }
}
