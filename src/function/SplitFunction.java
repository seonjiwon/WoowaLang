package function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import woowa.Interpreter;
import woowa.RuntimeError;
import woowa.Token;

public class SplitFunction implements NativeFunction {


    @Override
    public String getName() {
        return "분리";
    }

    @Override
    public int arity() {
        return 2; // 분리(문자열, 구분자)
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object arg0 = arguments.get(0);
        Object arg1 = arguments.get(1);

        if (!(arg0 instanceof String) || !(arg1 instanceof String)) {
            throw new RuntimeError(new Token(null, "", null, -1),
                "분리()의 두 인자는 모두 문자열이어야 합니다.");
        }

        String target = (String) arg0;
        String delimiter = (String) arg1;

        List<String> parts = Arrays.asList(target.split(delimiter));

        return new ArrayList<>(parts);
    }

    @Override
    public String toString() {
        return "<native fn split>";
    }
}
