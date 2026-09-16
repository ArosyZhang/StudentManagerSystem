package StudentManagerSystem;
import java.util.Scanner;

public class StudentManagerSystem {

    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {

        //测试：预置科目
        //intDefaultSubject();
        //测试：预置学生
        //intDefaultStudent();
        //测试：预置成绩
        //ScoreManager.initRandomScores();
        
        //启动 加载文件数据
        DataStore.loadAll();

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

                case 1 -> ScoreManager.showAllScoresTable();
                case 2 -> StudentManager.studentManager(scanner);
                case 3 -> ScoreManager.scoreMenu(scanner);
                case 4 -> SubjectManager.showSubjectMenu(scanner);
                case 0 -> { 
                    DataStore.saveAll();//退出前保存数据
                    System.out.println("感谢使用，再见！");
                    scanner.close();
                    return; 
                }    
            }
        }  
    }

    // 初始化科目（测试）
    public static void intDefaultSubject(){
        //只在列表为空时添加
        if (SubjectManager.subjectList.isEmpty()) {
            SubjectManager.subjectList.add(new SubjectManager("SUB001", "Java"));
            SubjectManager.subjectList.add(new SubjectManager("SUB002", "C++"));
            SubjectManager.subjectList.add(new SubjectManager("SUB003", "English"));
            
        }
    }
    //初始化学生（测试）
    public static void intDefaultStudent(){
        if (StudentManager.studentList.isEmpty()) {
            StudentManager.studentList.add(new StudentManager("STU001", "Arosy", 23));
            StudentManager.studentList.add(new StudentManager("STU002", "Jack", 18));
            StudentManager.studentList.add(new StudentManager("STU003", "Rose", 26));
            StudentManager.studentList.add(new StudentManager("STU004", "Davi", 19));
            StudentManager.studentList.add(new StudentManager("STU005", "Liu", 21));
        }
    }    
}

