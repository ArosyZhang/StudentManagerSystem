package StudentManagerSystem;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class ScoreManager {

    private static Map<String, Map<String,Double>> scoreMap = new HashMap<>();
    //分数容差
    private static final double SCORE_EPSILON = 1e-6;

    public static void scoreMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n===== 成绩管理菜单 =====");
            System.out.println("1.成绩录入");
            System.out.println("2.成绩查询与统计");
            System.out.println("0.返回主菜单");
            System.out.println("----------------------------"); 
            int choiceNumber = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0, 2);
            switch (choiceNumber) {
                 
                case 1 -> enterScoreMenu(scanner);
                case 2 -> queryMenu(scanner);
                case 0 -> { return; }               
                default -> System.out.println("输入错误，请重新输入");
            }          
        }       
    }

    //--- 成绩录入 ---
    public static void enterScoreMenu(Scanner scanner){
        while (true) {
            System.out.println("\n=== 成绩录入 ===");
            System.out.println("1.按科目录入");
            System.out.println("2.按学生录入");
            System.out.println("0.返回上级菜单");
            System.out.println("-------------------");
            int choiceNumber = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0, 2);

            switch (choiceNumber){
                
                case 1 -> enterBySubject(scanner);
                case 2 -> enterByStudent(scanner);
                case 0 -> { return; }
                default -> System.out.println("输入有误，请重新输入");

            }  
        }
    }

    //按科目录入：先选择一个科目，然后为该科目下的多个学生录入成绩
    private static void enterBySubject(Scanner scanner) {
        
        //1.显示所有科目，让用户选择科目
        Subject subject = SubjectManager.chooseSubject(scanner);
        if (subject == null) {
            return;
        }
        System.out.println("已选择科目：" + subject.getSubName());

        //循环选择学生录入成绩
        while (true) {
            Student student = StudentManager.chooseStudent(scanner);
            if (student == null) {
                System.out.println("结束录入，返回上级菜单");
                break;
            }

            double score = ToolUtil.readDouble(scanner, "请输入分数(0-100): ", 0, 100);
            boolean saved = addOrUpdateScore(scanner,student.getStuId(), subject.getSubId(), score);
            if (!saved) {
                System.out.println("本次录入已放弃");
            }
        }
    }

    //按学生录入：先选择一个学生，然后为该学生的多个科目录入成绩
    private static void enterByStudent(Scanner scanner) {
        
        //1.选择学生
        Student student = StudentManager.chooseStudent(scanner);
        if (student == null) {
            return;
        }
        System.out.println("已选择学生：" + student.getStuName());

        //循环选择学生录入成绩
        while (true) {
            Subject subject = SubjectManager.chooseSubject(scanner);
            if (subject == null) {
                System.out.println("结束录入，返回上级菜单");
                break;
            }

            double score = ToolUtil.readDouble(scanner, "请输入分数(0-100): ", 0, 100);
            boolean saved = addOrUpdateScore(scanner,student.getStuId(), subject.getSubId(), score);
            if (!saved) {
                System.out.println("本次录入已放弃");
            }
        }
    }

    //添加和更新成绩（已存在则覆盖）
    public static boolean addOrUpdateScore(Scanner scanner, String studentId, String subjectId,double score) {
        //入口归一化处理
        String normStudentId = ToolUtil.normalizeId(studentId);
        String normSubjectId = ToolUtil.normalizeId(subjectId);

        //获取该学生的成绩Map，不存在则创建
        Map<String,Double> studentScores = scoreMap.get(normStudentId);
        if (studentScores == null) {
            //  该学生从未录入过成绩，新建一个内层 Map
            studentScores = new HashMap<>();
            scoreMap.put(normStudentId,studentScores);
        }
        //判断是否已有该科目的成绩
        if (studentScores.containsKey(normSubjectId)) {
            double oldScore = studentScores.get(normSubjectId);

            String stuName = StudentManager.getStuName(normStudentId);
            String subName = SubjectManager.getSubName(normSubjectId);

            System.out.printf("该学生已有成绩：%s 的 %s 为 %.1f 分。 %n", stuName, subName, oldScore);
            System.out.print("是否覆盖旧成绩(Y/N): ");

            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("已取消，保留原成绩");
                return false;
            }         
        }

        //"科目编号"、"覆盖旧分数"
        studentScores.put(normSubjectId,score);
        DataStore.saveAll();//更新数据

        //提示添加成功
        String stuName = StudentManager.getStuName(normStudentId);
        String subName = SubjectManager.getSubName(normSubjectId);
        System.out.println("已保存：学生：" + stuName + " 科目：" + subName + " 分数: " + score);
        return true;
    }

    //--- 成绩查询与统计 ---
    public static void queryMenu(Scanner scanner) {
        while (true) {

            System.out.println("\n=== 成绩查询 ===");
            System.out.println("1. 查询学生各科成绩");
            System.out.println("2. 单科目成绩排名");
            System.out.println("0. 返回上级菜单");
            System.out.println("-------------------");
            int choiceNumber = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0, 2);
            switch (choiceNumber){
                case 1 -> queryStudentScores(scanner);
                case 2 -> subjectRanking(scanner);
                case 0 -> { return; }
                default -> System.out.println("输入有误，请重新输入");

            }
        }
        
    }

    //查询某个学生的所有科目成绩
    private static void queryStudentScores(Scanner scanner) {

        //选择学生
        Student student = StudentManager.chooseStudent(scanner);
        if (student == null) {
            System.out.println("已取消查询");
            return;
        }
        String studentId = student.getStuId();

        //从scoreMap中获取该学生成绩Map并判断是否有成绩记录
        Map<String, Double> studentScores = scoreMap.get(studentId);
        if (studentScores == null || studentScores.isEmpty()) {
            System.out.println("学生 " + student.getStuName() + " 暂无任何科目成绩");
            return;
        }
        List<Subject> subjects = SubjectManager.snapshotSubjects();
        if (subjects.isEmpty()) {
            System.out.println("暂无科目数据");
            return ;
        }
        //输出该学生的成绩表
        System.out.println("\n===== " + student.getStuName() + " 的成绩单 =====");
        
        double total = 0;
        int validCount = 0;

        for (Subject sub : subjects) {
            String subId = sub.getSubId();
            String subName = sub.getSubName();

            if (studentScores != null && studentScores.containsKey(subId)) {
                double score = studentScores.get(subId);
                System.out.printf("%s: %.1f%n", subName, score);
                total += score;//总分
                validCount++;
            } else {
                System.out.printf("%s: -%n", subName);
            }
        }
        System.out.println("--------------------------");
        if (validCount == 0) {
            System.out.println("该学生暂无任何成绩");
        } else {
            System.out.printf("总分： %.1f%n", total);
            System.out.printf("平均分：%.2f (按 %d 门 有成绩的科目计算) %n", total / validCount,validCount);
        }  
    }

    //单科目成绩排名：输入科目编号或科目名，输出该科目所有学生的成绩降序排列
    private static void subjectRanking(Scanner scanner) {
        //选择科目
        Subject subject = SubjectManager.chooseSubject(scanner);
        if (subject == null) {
            System.out.println("已取消查询");
            return;  
        }
        String subjectId = subject.getSubId();
        //收集该科目所有成绩
        List<Map.Entry<String, Double>> ranking = new ArrayList<>();

        String normSubjectId = ToolUtil.normalizeId(subjectId);
        for (Map.Entry<String , Map<String, Double>> outer : scoreMap.entrySet()) {
            String studentId = outer.getKey();
            Map<String, Double> studentScores = outer.getValue();
            if (studentScores.containsKey(normSubjectId)) {
                ranking.add(new AbstractMap.SimpleEntry<>(studentId, studentScores.get(normSubjectId)));
            }
        }

        //检查该科目是否有成绩
        if (ranking.isEmpty()) {
            System.out.println("该科目暂无成绩记录");
            return;
        }

        //降序排列成绩
        ranking.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        //输出排名
        System.out.println("\n===== " + subject.getSubName() + " 成绩排名 =====");
        int rank = 0;
        int count = 0;
        double lastScore = -1;

        for (Map.Entry<String, Double> entry : ranking) {
            String studentId = entry.getKey();
            double score = entry.getValue();
            count++;
            double diff = Math.abs(score - lastScore);
            if (diff > SCORE_EPSILON) { //使用容差判断是否相等
                rank = count;
                lastScore = score;
            }

            String name = ToolUtil.truncate(StudentManager.getStuName(studentId), 12);
            System.out.printf("第 %d 名：%s (%s)  %.1f 分%n\n", rank, name, studentId ,score);    
        }
    }

    //--- 数据清理（删除学生/科目时调用） ---
    //删除某个学生的所有成绩
    public static void removeScoreByStudent(String studentId) {   
        scoreMap.remove(ToolUtil.normalizeId(studentId));    
    }
    //删除某个科目的所有成绩（遍历所有学生，移除该科目）
    public static void removeScoreBySubject(String subjectId) { 
        String normalizedSubjectId = ToolUtil.normalizeId(subjectId);
        for (Map<String,Double> scores : scoreMap.values()) {
            scores.remove(normalizedSubjectId);
        }   
    }

    //临时数据类
    static class StudentRow {
        String studentId;
        String name;
        double[] scores; //按科目顺序存放分数，没有用 -1 表示
        double total;
        double average; 
        int rank; //名次  
    }
    public static void showAllScoresTable() {
        //1.获取所有科目
        List<Subject> subjects = SubjectManager.snapshotSubjects();
        List<Student> students = StudentManager.snapshotStudents();

        if (students.isEmpty()) {
            System.out.println("暂无学生数据");
            return;
        }
        if (subjects.isEmpty()) {
            System.out.println("暂无科目数据");
            return;
        }

        int subCount = subjects.size();
        int totalCols = 3 + subCount + 2; //名次+学号+姓名+科目...+总分+平均分

        String[] headers = new String[totalCols];
        int[] widths = new int[totalCols];

        //左边固定的三列
        headers[0] = "名次"; widths[0] = 6;
        headers[1] = "学号"; widths[1] = 10;
        headers[2] = "姓名"; widths[2] = 14;
        //科目列 动态
        for (int i = 0; i < subCount; i++) {
            headers[3 + i] = subjects.get(i).getSubName();
            widths[3 + i] = 12;
        }
        //最后两列 总分 平均分
        headers[3 + subCount] = "总分"; widths[3 + subCount] = 10;
        headers[4 + subCount] = "平均分"; widths[4 + subCount] = 10;

        List<StudentRow> rows = new ArrayList<>();

        for (Student stu : students) {
            StudentRow row = new StudentRow();
            row.studentId = stu.getStuId();
            row.name = stu.getStuName();
            row.scores = new double[subCount];

            Map<String, Double> stuScores = scoreMap.get(row.studentId);
            double total = 0;
            int validCount = 0;

            for (int i = 0; i < subCount; i++) {
                String subId = subjects.get(i).getSubId();
                if (stuScores != null && stuScores.containsKey(subId)) {
                    double s = stuScores.get(subId);
                    row.scores[i] = s;
                    total += s;
                    validCount++;
                } else {
                    row.scores[i] = -1; //-1表示没有成绩
                }
            }

            row.total = total;
            row.average = validCount > 0 ? total / validCount : 0;
            rows.add(row);
        }

        //按总分降序排列
        rows.sort((a, b) -> Double.compare(b.total, a.total));

        for (int i = 0; i < rows.size(); i++) {
            StudentRow row = rows.get(i);
            if (i == 0 || Math.abs(row.total - rows.get(i - 1).total) > SCORE_EPSILON) {
                row.rank = i + 1;
            } else {
                row.rank = rows.get( i - 1).rank;
            }
        }

        ToolUtil.printTable("全体学生成绩表", headers, widths, rows, (row, idx) -> {
   
            String[] cells = new String[totalCols];
             
            cells[0] = String.valueOf(row.rank);
            cells[1] = row.studentId;
            cells[2] = row.name;

            for (int i = 0; i < subCount; i++) {
                cells[3 + i] = (row.scores[i] < 0) ? "-" : String.format("%.1f", row.scores[i]);
            }
            cells[3 + subCount] = String.format("%.1f", row.total);
            cells[4 + subCount] = String.format("%.2f", row.average);

            return cells;
        }); 
    }

    //初始化成绩（测试）
    public static void initRandomScores() {
        if (!scoreMap.isEmpty()) return;  // 避免重复初始化
        Random random = new Random();

        // 获取学生和科目列表
        List<Student> students = StudentManager.snapshotStudents();
        List<Subject> subjects = SubjectManager.snapshotSubjects();

        if (students.isEmpty() || subjects.isEmpty()) {

            System.out.println("学生或科目为空，无法初始化成绩。");
            return;
        }
        for (Student stu : students) {

            // 为每个学生创建一个内层 Map
            Map<String, Double> stuScores = new HashMap<>();
            for (Subject sub : subjects) {
                // 随机分数 40~100 的整数
                double score = 40 + random.nextInt(61);  // nextInt(61) 返回 0~60
                stuScores.put(sub.getSubId(), score);
            }
            scoreMap.put(stu.getStuId(), stuScores);
        }
        System.out.println("随机成绩初始化完成。");
    }

    //从文件加载一条
    public static void loadScores(String studentId, String subjectId, double score) {
        studentId = ToolUtil.normalizeId(studentId);
        subjectId = ToolUtil.normalizeId(subjectId);
        if (studentId.isEmpty() || subjectId.isEmpty()) return; 
        scoreMap.computeIfAbsent(studentId, k -> new HashMap<>()).put(subjectId, score);
    }
    //只读快照
    public static Map<String, Map<String, Double>> snapshotScores() {
        return Map.copyOf(scoreMap); //浅拷贝，外层不可变，内层仍可变
        
    }
    //清空
    public static void clearScores() {
        scoreMap.clear();
    }
     
}


