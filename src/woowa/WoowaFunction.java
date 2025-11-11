package woowa;

import java.util.List;
import woowa.Stmt.Function;

public class WoowaFunction implements WoowaCallable{
    private final Stmt.Function declaration;
    private final Environment closure;

    private final boolean isInitializer;

    public WoowaFunction(Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    WoowaFunction bind(WoowaInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new WoowaFunction(declaration, environment, isInitializer);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // 함수 호출을 할 때 마다 고유한 환경을 가져온다.
        Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) {
                return closure.getAt(0, "this");
            }
        }

        if (isInitializer) {
            return closure.getAt(0, "this");
        }
        return null;
    }
}
