package StudentManagerSystem;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 成绩管理者：全项目唯一持有分数数据的地方，提供成绩的录入、查询、统计与删除。
 *
 * 由主菜单 {@link StudentManagerSystem} 调用 {@link #showAllScoresTable}，来展示学生成绩总表（排名）、 {@link #scoreMenu}，进入成绩管理子菜单；
 * 由主菜单 {@link StudentManagerSystem} 调用 {@link #removeScoreByStudent} 和 {@link #removeScoreBySubject}，
 * 把这两个方法注册为删除回调：删除学生/科目时连带清除其全部成绩。
 *
 * <p>边界：不负责成绩文件的写入和读取（由 {@link DataStore} 负责），
 * 不负责学生和科目两个实体的相关操作，由 {@link StudentManager} 和 {@link SubjectManager} 负责。
 *
 * @author ArosyZhang
 * @since 1.0
 */
public class ScoreManager {
    /**
     * 分数容差：两个分数（或两个总分）相差不超过它时，视为相等/并列。
     * 用它代替浮点数的 {@code ==} 比较，避免本该并列的两条记录因浮点误差拿到不同的名次。
     */
    private static final double SCORE_EPSILON = 1e-6;
    /** 分数范围最小值常量 */
    private static final double SCORE_MIN = 0.0;
    /** 分数范围最大值常量 */
    private static final double SCORE_MAX = 100.0;

    /**
     * 嵌套映射（其索引格式为：外层：学号；内层：科目编号 + 成绩）。
     * 本类以学生为主视角（使用频率高）：操作某个学生的全部成绩复杂度为 O(1)；操作某个科目的全部成绩复杂度为 O(n)。
     * 数据结构不是"装数据的容器"，是"查询方式的索引"。选哪层做索引，等于决定哪个方向的查询免费、哪个方向付代价。
     */
    private final Map<String, Map<String, Double>> scoreMap = new HashMap<>();

    private final StudentManager studentManager;
    private final SubjectManager subjectManager;

    /**
     * 构造注入：让本类拿到与主菜单、{@link DataStore} 完全相同的那两个 Manager 实例，
     * 而不是自己 new 一份（自己 new 的话拿到的是另一套空数据，什么都查不到）。
     *
     * @param studentManager    学生管理器，用于把学号翻译成姓名、并列出学生供选择
     * @param subjectManager    科目管理器，用于把科目编号翻译成名称、并列出科目供选择
     */
    public ScoreManager(StudentManager studentManager, SubjectManager subjectManager) {
        this.studentManager = studentManager;
        this.subjectManager = subjectManager;
    }

    /**
     * 负责成绩录入和成绩查询
     *
     * @param scanner   读取用户输入
     */
    public void scoreMenu(Scanner scanner) {
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
    /**
     * 两种录入方式1.科目录入：科目列表选择科目->学生列表选择学生->输入合法成绩->录入/更新成功。
     * 2.学生录入：学生列表选择学生->科目列表选择科目->输入合法成绩->录入/更新成功。
     *
     * @param scanner   读取用户输入
     */
    public void enterScoreMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== 成绩录入 ===");
            System.out.println("1.按科目录入");
            System.out.println("2.按学生录入");
            System.out.println("0.返回上级菜单");
            System.out.println("-------------------");
            int choiceNumber = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0, 2);

            switch (choiceNumber) {
                case 1 -> enterBySubject(scanner);
                case 2 -> enterByStudent(scanner);
                case 0 -> { return; }
                default -> System.out.println("输入有误，请重新输入");
            }
        }
    }

    //按科目录入：先选择一个科目，然后为该科目下的多个学生录入成绩
    private void enterBySubject(Scanner scanner) {

        //1.显示所有科目，让用户选择科目
        Subject subject = subjectManager.chooseSubject(scanner);
        if (subject == null) {
            return;
        }
        System.out.println("已选择科目：" + subject.getName());

        //循环选择学生录入成绩
        while (true) {
            Student student = studentManager.chooseStudent(scanner);
            if (student == null) {
                System.out.println("结束录入，返回上级菜单");
                break;
            }
            double score = ToolUtil.readDouble(scanner, "请输入分数(0-100): ", SCORE_MIN, SCORE_MAX);
            boolean saved = addOrUpdateScore(scanner, student.getId(), subject.getId(), score);
            if (!saved) {
                System.out.println("本次录入已放弃");
            }
        }
    }

    //按学生录入：先选择一个学生，然后为该学生的多个科目录入成绩
    private void enterByStudent(Scanner scanner) {
        //1.选择学生
        Student student = studentManager.chooseStudent(scanner);
        if (student == null) {
            return;
        }
        System.out.println("已选择学生：" + student.getName());

        //循环选择学生录入成绩
        while (true) {
            Subject subject = subjectManager.chooseSubject(scanner);
            if (subject == null) {
                System.out.println("结束录入，返回上级菜单");
                break;
            }
            double score = ToolUtil.readDouble(scanner, "请输入分数(0-100): ", SCORE_MIN, SCORE_MAX);
            boolean saved = addOrUpdateScore(scanner, student.getId(), subject.getId(), score);
            if (!saved) {
                System.out.println("本次录入已放弃");
            }
        }
    }

    //添加和更新成绩（已存在则覆盖）
    /**
     * 获取该学生的成绩Map，不存在则创建；录入时有成绩覆盖提示。
     *
     * @param scanner   读取用户输入
     * @param studentId 学号
     * @param subjectId 科目编号
     * @param score     分数，取值在 [{@value #SCORE_MIN}, {@value #SCORE_MAX}] 之间（含边界）
     * @return          true 表示成绩已写入（新增或覆盖成功）；false 表示用户在覆盖确认时放弃，成绩未变
     */
    public boolean addOrUpdateScore(Scanner scanner, String studentId, String subjectId, double score) {
        //入口归一化处理
        String normStudentId = ToolUtil.normalizeId(studentId);
        String normSubjectId = ToolUtil.normalizeId(subjectId);

        //获取该学生的成绩Map，不存在则创建
        Map<String, Double> studentScores = scoreMap.get(normStudentId);
        if (studentScores == null) {
            //  该学生从未录入过成绩，新建一个内层 Map
            studentScores = new HashMap<>();
            scoreMap.put(normStudentId, studentScores);
        }
        //判断是否已有该科目的成绩
        if (studentScores.containsKey(normSubjectId)) {
            double oldScore = studentScores.get(normSubjectId);

            String stuName = studentManager.getNameById(normStudentId);
            String subName = subjectManager.getNameById(normSubjectId);

            System.out.printf("该学生已有成绩：%s 的 %s 为 %.1f 分。 %n", stuName, subName, oldScore);
            System.out.print("是否覆盖旧成绩(Y/N): ");

            String confirm = ToolUtil.readLine(scanner);
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("已取消，保留原成绩");
                return false;
            }
        }
        //"科目编号"、"覆盖旧分数"
        studentScores.put(normSubjectId, score);

        //提示添加成功
        String stuName = studentManager.getNameById(normStudentId);
        String subName = subjectManager.getNameById(normSubjectId);
        System.out.println("已保存：学生：" + stuName + " 科目：" + subName + " 分数：" + score);
        return true;
    }

    //--- 成绩查询与统计 ---
    public void queryMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== 成绩查询 ===");
            System.out.println("1. 查询学生各科成绩");
            System.out.println("2. 单科目成绩排名");
            System.out.println("0. 返回上级菜单");
            System.out.println("-------------------");
            int choiceNumber = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0, 2);
            switch (choiceNumber) {
                case 1 -> queryStudentScores(scanner);
                case 2 -> subjectRanking(scanner);
                case 0 -> { return; }
                default -> System.out.println("输入有误，请重新输入");
            }
        }
    }

    //查询某个学生的所有科目成绩
    private void queryStudentScores(Scanner scanner) {

        //选择学生
        Student student = studentManager.chooseStudent(scanner);
        if (student == null) {
            System.out.println("已取消查询");
            return;
        }
        String studentId = student.getId();

        //从scoreMap中获取该学生成绩Map并判断是否有成绩记录
        Map<String, Double> studentScores = scoreMap.get(studentId);
        if (studentScores == null || studentScores.isEmpty()) {
            System.out.println("学生 " + student.getName() + " 暂无任何科目成绩");
            return;
        }
        List<Subject> subjects = subjectManager.snapshotSubjects();
        if (subjects.isEmpty()) {
            System.out.println("暂无科目数据");
            return;
        }
        //输出该学生的成绩表
        System.out.println("\n===== " + student.getName() + " 的成绩单 =====");

        double total = 0;
        int validCount = 0;

        for (Subject sub : subjects) {
            String subId = sub.getId();
            String subName = sub.getName();

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
            System.out.printf("平均分：%.2f (按 %d 门有成绩的科目计算) %n", total / validCount, validCount);
        }
    }

    //单科目成绩排名：输入科目编号或科目名，输出该科目所有学生的成绩降序排列
    private void subjectRanking(Scanner scanner) {
        //选择科目
        Subject subject = subjectManager.chooseSubject(scanner);
        if (subject == null) {
            System.out.println("已取消查询");
            return;
        }
        String subjectId = subject.getId();
        //收集该科目所有成绩
        List<Map.Entry<String, Double>> ranking = new ArrayList<>();

        String normSubjectId = ToolUtil.normalizeId(subjectId);
        for (Map.Entry<String, Map<String, Double>> outer : scoreMap.entrySet()) {
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
        System.out.println("\n===== " + subject.getName() + " 成绩排名 =====");
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
            String name = ToolUtil.truncate(studentManager.getNameById(studentId), 12);
            System.out.printf("第 %d 名：%s (%s)  %.1f 分%n\n", rank, name, studentId, score);
        }
    }

    //--- 数据清理（删除学生/科目时调用） ---
    //删除某个学生的所有成绩
    public void removeScoreByStudent(String studentId) {
        scoreMap.remove(ToolUtil.normalizeId(studentId));
    }
    //删除某个科目的所有成绩（遍历所有学生，移除该科目）
    public void removeScoreBySubject(String subjectId) {
        String normalizedSubjectId = ToolUtil.normalizeId(subjectId);
        for (Map<String, Double> scores : scoreMap.values()) {
            scores.remove(normalizedSubjectId);
        }
    }

    //临时数据类
    private static class StudentRow {
        String studentId;
        String name;
        double[] scores; //按科目顺序存放分数，没有用 -1 表示
        double total;
        double average;
        int rank; //名次
    }

    public void showAllScoresTable() {
        //获取所有科目
        List<Subject> subjects = subjectManager.snapshotSubjects();
        List<Student> students = studentManager.snapshotStudents();

        if (students.isEmpty()) {
            System.out.println("暂无学生数据");
            return;
        }
        if (subjects.isEmpty()) {
            System.out.println("暂无科目数据");
            return;
        }

        List<StudentRow> rows = buildRows(students, subjects);
        assignRanks(rows);

        ToolUtil.printTable("全体学生成绩表", buildHeaders(subjects), buildWidths(subjects), rows, (row, idx) -> renderRow(row, subjects.size()));
    }

    // 表头：名次/学号/姓名 + 每个科目一列 + 总分/平均分
    private String[] buildHeaders(List<Subject> subjects) {
        int subCount = subjects.size();
        String[] headers = new String[3 + subCount + 2];
        headers[0] = "名次";
        headers[1] = "学号";
        headers[2] = "姓名";
        for (int i = 0; i < subCount; i++) {
            headers[3 + i] = subjects.get(i).getName();
        }
        headers[3 + subCount] = "总分";
        headers[4 + subCount] = "平均分";
        return headers;
    }

    // 列宽，与 buildHeaders 一一对应 —— 想调宽度只改这里
    private int[] buildWidths(List<Subject> subjects) {
        int subCount = subjects.size();
        int[] widths = new int[3 + subCount + 2];
        widths[0] = 6;
        widths[1] = 10;
        widths[2] = 14;
        for (int i = 0; i < subCount; i++) {
            widths[3 + i] = 12;
        }
        widths[3 + subCount] = 10;
        widths[4 + subCount] = 10;
        return widths;
    }

    private String[] renderRow(StudentRow row, int subCount) {
        String[] cells = new String[3 + subCount + 2];

        cells[0] = String.valueOf(row.rank);
        cells[1] = row.studentId;
        cells[2] = row.name;

        for (int i = 0; i < subCount; i++) {
            // scores[i] == -1 表示这门课没成绩（约定见 buildRows）
            cells[3 + i] = (row.scores[i] < 0) ? "-" : String.format("%.1f", row.scores[i]);
        }
        cells[3 + subCount] = String.format("%.1f", row.total);
        cells[4 + subCount] = String.format("%.2f", row.average);

        return cells;
    }

    //按总分降序排名；总分差值小于 SCORE_EPSILON 的算并列
    private void assignRanks(List<StudentRow> rows) {
        rows.sort((a, b) -> Double.compare(b.total, a.total));

        for (int i = 0; i < rows.size(); i++) {
            StudentRow row = rows.get(i);
            if (i == 0 || Math.abs(row.total - rows.get(i - 1).total) > SCORE_EPSILON) {
                row.rank = i + 1;
            } else {
                row.rank = rows.get(i - 1).rank;
            }
        }
    }

    private List<StudentRow> buildRows(List<Student> students, List<Subject> subjects) {
        int subCount = subjects.size();
        List<StudentRow> rows = new ArrayList<>();

        for (Student stu : students) {
            StudentRow row = new StudentRow();
            row.studentId = stu.getId();
            row.name = stu.getName();
            row.scores = new double[subCount];

            Map<String, Double> stuScores = scoreMap.get(row.studentId);
            double total = 0;
            int validCount = 0;

            for (int i = 0; i < subCount; i++) {
                String subId = subjects.get(i).getId();
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
        return rows;
    }

    //从文件加载一条成绩数据。成功返回 null，失败返回原因
    public String loadScore(String studentId, String subjectId, String scoreText) {
        studentId = ToolUtil.normalizeId(studentId);
        subjectId = ToolUtil.normalizeId(subjectId);
        if (studentId.isEmpty()) return "学号为空";
        if (subjectId.isEmpty()) return "科目编号为空";
        double score;
        try {
            score = Double.parseDouble(scoreText);
        } catch (NumberFormatException e) {
            return "分数不是数字";
        }
        if (score < SCORE_MIN || score > SCORE_MAX) return "分数超出 " + SCORE_MIN + "~" + SCORE_MAX + " 范围";
        scoreMap.computeIfAbsent(studentId, k -> new HashMap<>()).put(subjectId, score);
        return null;
    }

    //只读快照
    public Map<String, Map<String, Double>> snapshotScores() {
        return Map.copyOf(scoreMap); //浅拷贝，外层不可变，内层仍可变
    }
    //清空
    public void clearScores() {
        scoreMap.clear();
    }
}
