import java.util.ArrayList;
import java.util.List;

public class ErasureDemo {
    public static void main(String[] args) {
        List<String> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        System.out.println("List<String> 和 List<Integer> 是同一个类吗？ " + (a.getClass() == b.getClass()));
        System.out.println("a.getClass().getName() = " + a.getClass().getName());
        System.out.println("b.getClass().getName() = " + b.getClass().getName());
    }
}