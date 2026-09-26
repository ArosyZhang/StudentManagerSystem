package StudentManagerSystem;
import java.util.Scanner;

public class StudentManagerSystem {

    private final Scanner scanner = new Scanner(System.in);
    private final StudentManager studentManager= new StudentManager();
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
                int choiceNumber = ToolUtil.readInt(scanner,"请选择对应的数字：", 0 , 4);
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

