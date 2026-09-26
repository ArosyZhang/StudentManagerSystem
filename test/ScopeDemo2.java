import java.util.*;

/** 反例：局部变量声明在 try 里面，catch 块里看不到 */
public class ScopeDemo2 {
    public static void main(String[] args) {
        try {
            List<String> inside = Arrays.asList("A");
            int idx = Integer.parseInt("abc");
            System.out.println(idx);
        } catch (NumberFormatException e) {
            System.out.println(inside);   // ← 这行应该编译报错
        }
    }
}
