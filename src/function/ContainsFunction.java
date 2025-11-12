package function;

import java.util.List;
import woowa.Interpreter;
import woowa.RuntimeError;
import woowa.Token;

public class ContainsFunction implements NativeFunction {


    @Override
    public String getName() {
        return "포함";
    }

    @Override
    public int arity() {
        return 2; // 포함(전체문자열, 부분문자열)
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object arg0 = arguments.get(0);
        Object arg1 = arguments.get(1);

        if (!(arg0 instanceof String) || !(arg1 instanceof String)) {
            throw new RuntimeError(new Token(null, "", null, -1),
                "포함()의 두 인자는 모두 문자열이어야 합니다.");
        }

        String target = (String) arg0;
        String part = (String) arg1;

        return target.contains(part);
    }

    @Override
    public String toString() {
        return "<native fn contains>";
    }
}
