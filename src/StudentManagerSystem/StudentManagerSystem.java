package StudentManagerSystem;
import java.util.Scanner;

public class StudentManagerSystem {

    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) { 
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
}

