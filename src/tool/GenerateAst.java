package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


/**
 * Expr(Expression), Stmt(Statement) 생성
 * 보일러플레이트 코드
 *
 * [실행]
 * javac -d out/production/WoowaLang src/tool/GenerateAst.java
 * java -cp out/production/WoowaLang tool.GenerateAst src/woowa
 */
public class GenerateAst {

    public static void main(String[] args) throws IOException{
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
            "Assign   : Token name, Expr value",
            "Binary   : Expr left, Token operator, Expr right", // 1 + 2
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right",
            "Variable : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block      : List<Stmt> statements",
            "Expression : Expr expression",
            "Print      : Expr expression",
            "Var        : Token name, Expr initializer"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types)
        throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package woowa;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        writer.println();
        writer.println("  // Nested " + baseName + " classes here...");
        // AST class
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim(); // [robust]
            defineType(writer, baseName, className, fields);
        }

        // accept() 메서드
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.println("//< Appendix II " + baseName.toLowerCase());
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("  }");
    }

    private static void defineType(
        PrintWriter writer, String baseName,
        String className, String fieldList) {
        writer.println("//> " +
            baseName.toLowerCase() + "-" + className.toLowerCase());
        writer.println("  static class " + className + " extends " +
            baseName + " {");

        if (fieldList.length() > 64) {
            fieldList = fieldList.replace(", ", ",\n          ");
        }

        // 생성자.
        writer.println("    " + className + "(" + fieldList + ") {");

        fieldList = fieldList.replace(",\n          ", ", ");
        // 파라미터 저장
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
            className + baseName + "(this);");
        writer.println("    }");

        // 필드.
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
        writer.println("//< " +
            baseName.toLowerCase() + "-" + className.toLowerCase());
    }
}
