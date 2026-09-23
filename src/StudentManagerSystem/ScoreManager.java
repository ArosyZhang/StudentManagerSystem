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
        SubjectManager subject = SubjectManager.chooseSubject(scanner);
        if (subject == null) {
            return;
        }
        System.out.println("已选择科目：" + subject.getSubName());

        //循环选择学生录入成绩
        while (true) {
            StudentManager student = StudentManager.chooseStudent(scanner);
            if (student == null) {
                System.out.println("结束录入，返回上级菜单");
                break;
            }

            double score = inputScore(scanner);
            boolean saved = addOrUpdateScore(scanner,student.getStuId(), subject.getSubId(), score);
            if (!saved) {
                System.out.println("本次录入已放弃");
            }
        }
    }

    //按学生录入：先选择一个学生，然后为该学生的多个科目录入成绩
    private static void enterByStudent(Scanner scanner) {
        
        //1.选择学生
        StudentManager student = StudentManager.chooseStudent(scanner);
        if (student == null) {
            return;
        }
        System.out.println("已选择学生：" + student.getStuName());

        //循环选择学生录入成绩
        while (true) {
            SubjectManager subject = SubjectManager.chooseSubject(scanner);
            if (subject == null) {
                System.out.println("结束录入，返回上级菜单");
                break;
            }

            double score = inputScore(scanner);
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

        //将subjectId和Score放入内层Map（如果已存在相同的科目边号，会覆盖就分数）
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
            int choiceNumber = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0, 3);
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
        StudentManager student = StudentManager.chooseStudent(scanner);
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
        ArrayList<SubjectManager> subjects = SubjectManager.getSubjectList();
        if (subjects.isEmpty()) {
            System.out.println("暂无科目数据");
            return ;
        }
        //输出该学生的成绩表
        System.out.println("\n===== " + student.getStuName() + " 的成绩单 =====");
        
        double total = 0;
        int validCount = 0;

        for (SubjectManager sub : subjects) {
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
        SubjectManager subject = SubjectManager.chooseSubject(scanner);
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

            if (score != lastScore) {
                rank = count;
                lastScore = score;
            }

            String name = ToolUtil.truncate(StudentManager.getStuName(studentId), 12);
            System.out.printf("第 %d 名：%s (%s)  %.1f 分%n\n", rank, name, studentId ,score);    
        }

        //缺考处理
        // 
        /* 
        System.out.println("\n缺考名单");
        for (StudentManager stu : students) {
            Map<String,Double> studentScores = scoreMap.get(stu.getStuId());
            if (studentScores == null || !studentScores.containsKey(subjectId)) {
                System.out.println(" " + stu.getStuName());
            }
        }
        */
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

    //显示所有学生与成绩表格
    //临时数据类
    static class StudentRow {
        String studentId;
        String name;
        double[] scores; //按科目顺序存放分数，没有用 -1 表示
        double total;
        double average;   
    }
    public static void showAllScoresTable() {
        //1.获取所有科目
        ArrayList<SubjectManager> subjects = SubjectManager.getSubjectList();
        ArrayList<StudentManager> students = StudentManager.getStudentList();

        if (students.isEmpty()) {
            System.out.println("暂无学生数据");
            return;
        }

        if (subjects.isEmpty()) {
            System.out.println("暂无科目数据");
            return;
        }

        int subCount = subjects.size();

        //2.为每个学生构建数据行
        List<StudentRow> rows = new ArrayList<>();

        for (StudentManager stu : students) {
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

        //3.按总分降序排列
        rows.sort((a, b) -> Double.compare(b.total, a.total));

        //4.表头：名次+学号+姓名+科目...+总分+平均分
        StringBuilder header = new StringBuilder();
        header.append(ToolUtil.padRight("名次", 6));
        header.append(ToolUtil.padRight("学号", 10));
        header.append(ToolUtil.padRight("姓名", 14));
        
        for (SubjectManager sub : subjects) {
            header.append(ToolUtil.padRight(sub.getSubName(), 12));
        }
        header.append(ToolUtil.padRight("总分", 10));
        header.append(ToolUtil.padRight("平均分", 10));

        String headerStr = header.toString();

        //打印横幅 + 表头 + 分隔线
        System.out.println();
        ToolUtil.printBanner("全体学生成绩表", headerStr);
        System.out.println(headerStr);
        ToolUtil.printDivider('-', headerStr);        

        //5.打印每一行，处理并列名次
        int rank = 0;
        int count = 0;
        double lastTotal = -1;

        for (StudentRow row : rows) {
            count++;
            if (row.total != lastTotal) {
                rank = count;
                lastTotal = row.total;
            }

            StringBuilder line = new StringBuilder();
            line.append(ToolUtil.padRight(String.valueOf(rank), 6));
            line.append(ToolUtil.padRight(row.studentId, 10));
            line.append(ToolUtil.padRight(ToolUtil.truncate(row.name, 12), 14));
            for (double s : row.scores) {     
                line.append(ToolUtil.padRight(s < 0 ? "-" : String.format("%.1f", s), 12));       
            }
            line.append(ToolUtil.padRight(String.format("%.1f", row.total), 10));
            line.append(ToolUtil.padRight(String.format("%.2f", row.average), 10));
            System.out.println(line);
        }
    }

    //分数录入 提取方法 (带校验)
    public static double inputScore(Scanner scanner) {
        while (true) {
            System.out.print("请输入分数(0-100): ");
            if (scanner.hasNextDouble()) {
                double score = scanner.nextDouble();
                scanner.nextLine();
                if (score >= 0 && score <=100) {
                    return score;                   
                } else {
                    System.out.println("分数必须在0到100之间, 请重新输入");
                }
                
            }else {
                System.out.println("输入无效，请输入正确的数字");
                scanner.nextLine();//清除输入错误
            }
        }
    }


    //初始化成绩（测试）
    public static void initRandomScores() {
        if (!scoreMap.isEmpty()) return;  // 避免重复初始化
        Random random = new Random();

        // 获取学生和科目列表
        ArrayList<StudentManager> students = StudentManager.getStudentList();
        ArrayList<SubjectManager> subjects = SubjectManager.getSubjectList();

        if (students.isEmpty() || subjects.isEmpty()) {

            System.out.println("学生或科目为空，无法初始化成绩。");
            return;
        }
        for (StudentManager stu : students) {

            // 为每个学生创建一个内层 Map
            Map<String, Double> stuScores = new HashMap<>();
            for (SubjectManager sub : subjects) {
                // 随机分数 40~100 的整数
                double score = 40 + random.nextInt(61);  // nextInt(61) 返回 0~60
                stuScores.put(sub.getSubId(), score);
            }
            scoreMap.put(stu.getStuId(), stuScores);
        }
        System.out.println("随机成绩初始化完成。");
    }

     public static void clearScores() {
        scoreMap.clear();
    }

    public static void loadScores(String studentId, String subjectId, double score) {
        studentId = ToolUtil.normalizeId(studentId);
        subjectId = ToolUtil.normalizeId(subjectId);
        if (studentId.isEmpty() || subjectId.isEmpty()) return; 
        scoreMap.computeIfAbsent(studentId, k -> new HashMap<>()).put(subjectId, score);
    }

    public static Map<String, Map<String, Double>> snapshotScores() {
        return Map.copyOf(scoreMap); //浅拷贝，外层不可变，内层仍可变
        
    }      
}


