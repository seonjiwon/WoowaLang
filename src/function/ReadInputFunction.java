package function;

import java.util.List;
import java.util.Scanner;
import woowa.Interpreter;

public class ReadInputFunction implements NativeFunction{

    private static final Scanner SCANNER = new Scanner(System.in);

    @Override
    public String getName() {
        return "읽기";
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        System.out.print(">> ");
        return SCANNER.nextLine();
    }

    @Override
    public String toString() {
        return "<native fn>";
    }
}
