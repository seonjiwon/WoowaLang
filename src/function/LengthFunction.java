package function;

import java.util.List;
import woowa.Interpreter;
import woowa.RuntimeError;
import woowa.Token;

public class LengthFunction implements NativeFunction{

    @Override
    public String getName() {
        return "길이";
    }

    @Override
    public int arity() {
        return 1; // 길이(문자열)
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object arg0 = arguments.get(0);

        if (!(arg0 instanceof String)) {
            throw new RuntimeError(new Token(null, "", null, -1),
                "길이()의 인자는 문자열이어야 합니다.");
        }

        String target = (String) arg0;
        return (double) target.length();
    }

    @Override
    public String toString() {
        return "<native fn length>";
    }
}
