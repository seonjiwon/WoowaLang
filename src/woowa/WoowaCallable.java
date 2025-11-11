package woowa;

import java.util.List;

public interface WoowaCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
