package woowa;

import java.util.ArrayList;
import java.util.List;

public class WoowaArray {

    private List<Object> elements;

    public WoowaArray() {
        this.elements = new ArrayList<>();
    }

    public WoowaArray(List<Object> elements) {
        this.elements = new ArrayList<>(elements);
    }

    public Object get(int index) {
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeError(new Token(TokenType.EOF, "", null, -1)
                , "배열 인덱스가 범위를 벗어났습니다. (인덱스: " + index + ", 배열 크기: " + elements.size() + ")");
        }

        return elements.get(index);
    }

    public void set(int index, Object value) {
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeError(
                new Token(TokenType.EOF, "", null, -1),
                "배열 인덱스가 범위를 벗어났습니다. (인덱스: " + index + ", 배열 크기: " + elements.size() + ")"
            );
        }
        elements.set(index, value);
    }

    public int size() {
        return elements.size();
    }

    public void push(Object element) {
        elements.add(element);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(stringify(elements.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
