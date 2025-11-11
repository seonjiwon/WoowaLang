package woowa;

import java.util.List;
import java.util.Map;

public class WoowaClass implements WoowaCallable{
    final String name;
    final WoowaClass superclass;
    private final Map<String, WoowaFunction> methods;

    public WoowaClass(String name, WoowaClass superclass, Map<String, WoowaFunction> methods) {
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    WoowaFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        WoowaInstance instance = new WoowaInstance(this);
        WoowaFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity() {
        WoowaFunction initializer = findMethod("init");
        if (initializer == null) {
            return 0;
        }
        return initializer.arity();
    }
}
