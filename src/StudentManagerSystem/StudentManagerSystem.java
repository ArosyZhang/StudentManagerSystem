package StudentManagerSystem;
import java.util.Scanner;

/**
 * 程序入口：组装所有组件，驱动主菜单循环。
 *
 * <p>它是全项目唯一知道「有哪些组件、谁先构造、谁依赖谁」的地方。
 * 构造顺序不能颠倒：{@link StudentManager} → {@link SubjectManager} → {@link ScoreManager} → {@link DataStore}，因为后者的构造函数要吃前者。
 *
 * 各 Manager 只改内存数据，它们不认识 {@link DataStore} —— 否则去 static 时会撞上循环依赖。所以"改完就落盘"的时机由本类掌握：子菜单返回后立即保存。
 *
 * <p>输入流被关闭时（Ctrl+Z、或管道读完），{@link ToolUtil#readLine} 会抛 {@link ToolUtil.InputCloseException}，本类在 {@link #run()} 外层捕获并优雅退出。
 *
 * 之所以不直接捕获 NoSuchElementException，是因为它同样会由空集合的迭代器抛出，会把真正的 bug 伪装成一次正常退出。

 * @author ArosyZhang
 * @since 1.0
 */
public class StudentManagerSystem {
    private final Scanner scanner = new Scanner(System.in);
    private final StudentManager studentManager = new StudentManager();
    private final SubjectManager subjectManager = new SubjectManager();
    private final ScoreManager scoreManager = new ScoreManager(studentManager, subjectManager);
    private final DataStore dataStore = new DataStore(studentManager, subjectManager, scoreManager);

    public static void main(String[] args) {
        new StudentManagerSystem().run();
    }

    private void run() {
        //启动 加载文件数据
        dataStore.loadAll();
        try {
            while (true) {
                System.out.println("\n===== 学生成绩管理系统 =====");
                System.out.println("\n1.打印所有学生和成绩");
                System.out.println("2.学生管理 ");
                System.out.println("3.成绩管理");
                System.out.println("4.科目管理");
                System.out.println("0.退出");
                System.out.println("----------------------------");
                int choiceNumber = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0 , 4);
                switch(choiceNumber){
                    case 1 -> scoreManager.showAllScoresTable();
                    case 2 -> {
                        studentMenu();
                        dataStore.saveAll();
                    }
                    case 3 -> {
                        scoreManager.scoreMenu(scanner);
                        dataStore.saveAll();
                    }
                    case 4 -> {
                        subjectMenu();
                        dataStore.saveAll();
                    }
                    case 0 -> {
                        dataStore.saveAll();//退出前保存数据
                        System.out.println("感谢使用，再见！");
                        scanner.close();
                        return;
                    }
                }
            }
        } catch (ToolUtil.InputCloseException e) {
            System.out.println();
            System.out.println(e.getMessage() + "，程序已退出");
        }
    }

    private void studentMenu() {
        studentManager.showStudentMenu(scanner, id -> scoreManager.removeScoreByStudent(id));
    }

    private void subjectMenu() {
        subjectManager.showSubjectMenu(scanner, id -> scoreManager.removeScoreBySubject(id));
    }
}
