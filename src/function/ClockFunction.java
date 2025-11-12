package function;

import java.util.List;
import woowa.Interpreter;

public class ClockFunction implements NativeFunction {
    @Override
    public String getName() {
        return "시간";
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
    }

    @Override
    public String toString() {
        return "<native fn>";
    }
}
