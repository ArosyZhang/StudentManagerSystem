public class StaticDemo {
    static String label;          // static：属于"类"，全局一份
    String instanceLabel;         // 实例：属于"对象"，一个对象一份

    StaticDemo(String label) {
        this.label = label;
        this.instanceLabel = label;
    }

    public static void main(String[] args) {
        StaticDemo a = new StaticDemo("学生");
        StaticDemo b = new StaticDemo("科目");
        System.out.println("a.label (static)         = " + a.label);
        System.out.println("b.label (static)         = " + b.label);
        System.out.println("a.instanceLabel (实例)   = " + a.instanceLabel);
        System.out.println("b.instanceLabel (实例)   = " + b.instanceLabel);
    }
}