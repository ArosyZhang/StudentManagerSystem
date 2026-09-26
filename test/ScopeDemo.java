import java.util.*;

/** 正例：局部变量声明在 try 之前，catch 块里能不能用？ */
public class ScopeDemo {
    public static void main(String[] args) {
        List<String> students = new ArrayList<>(Arrays.asList("A", "B"));

        try {
            int idx = Integer.parseInt("abc");   // 故意抛 NumberFormatException
            System.out.println(idx);
        } catch (NumberFormatException e) {
            // students 声明在 try 之前，catch 里看得到吗？
            for (String s : students) {
                System.out.println("catch 里读到了: " + s);
            }
        }
    }
}
