package StudentManagerSystem;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 数据文件保存和读取：确定数据文件生成位置，保存按照指定字符分割数据，读取同样逻辑;
 *
 * <p>仅管理文件的生成和读取，数据修改依赖对应的实体管理类
 *
 * @author ArosyZhang
 * @since 1.0
 */
public class DataStore {
    private static final String DATA_DIR = System.getProperty("user.dir") + "/data";
    private static final String STUDENT_FILE = DATA_DIR + "/students.txt";
    private static final String SUBJECT_FILE = DATA_DIR + "/subjects.txt";
    private static final String SCORE_FILE = DATA_DIR + "/scores.txt";

    private final StudentManager studentManager;
    private final SubjectManager subjectManager;
    private final ScoreManager scoreManager;


    /**
     * 脏数据暂存区，加载时收集，不做删除，保存时原样写入。留给用户手动修复的机会
     */
    private final List<String> skippedStudentLines = new ArrayList<>();
    private final List<String> skippedSubjectLines = new ArrayList<>();
    private final List<String> skippedScoreLines = new ArrayList<>();

    /**
     * 构造生成三个类的数据页
     *
     * @param studentManager    学生数据
     * @param subjectManager    科目数据
     * @param scoreManager      成绩数据
     */
    public DataStore(StudentManager studentManager, SubjectManager subjectManager, ScoreManager scoreManager) {
        this.studentManager = studentManager;
        this.subjectManager = subjectManager;
        this.scoreManager = scoreManager;
    }


    //确保data目录存在
    private void ensureDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    //=====保存=====
    /**
     * 调用时把当前内存里这三个集合的所有数据写回对应的三个文件
     * 只在用户改完数据后调用
     *
     * <p><b>全量覆盖</b>：整个文件被重写，不是增量追加。因此内存里没有的数据，
     * 在文件里也会消失 —— 这也是 {@link #loadAll()} 收集无效行、再原样写回的原因。
     * <p><b>绝不能</b>在 {@link #loadAll()} 里调用本方法，否则加载完立刻把文件重写一遍。
     *
     */
    public void saveAll() {
        ensureDir();
        saveStudents();
        saveSubjects();
        saveScores();
    }

    private void saveStudents() {
        saveFile(STUDENT_FILE, "学生", studentManager.snapshotStudents(),
            s -> s.getId() + "|" + s.getName() + "|" + s.getStuAge(), skippedStudentLines);
    }

    private void saveSubjects() {
        saveFile(SUBJECT_FILE, "科目", subjectManager.snapshotSubjects(),
            s -> s.getId() + "|" + s.getName(), skippedSubjectLines);
    }

    private void saveScores() {
        saveFile(SCORE_FILE, "成绩", flattenScores(), line -> line, skippedScoreLines);
    }

    //把"学号 -> (科目号 -> 分数)"的嵌套 Map 摊平成 "学号|科目号|分数" 的字符串列表
    private List<String> flattenScores() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> outer : scoreManager.snapshotScores().entrySet()) {
            for (Map.Entry<String, Double> inner : outer.getValue().entrySet()) {
                lines.add(outer.getKey() + "|" + inner.getKey() + "|" + inner.getValue());
            }
        }
        return lines;
    }

    //把一个列表逐行写入文件；toLine 把一条数据变成一行文本，extraLines 是无法解析、需要原样保留的行
    private <T> void saveFile(String filePath, String label, List<T> items, Function<T, String> toLine, List<String> extraLines) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {
            for (T item : items) {
                bw.write(toLine.apply(item));
                bw.newLine();
            }
            for (String line : extraLines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("保存" + label + "数据失败：" + e.getMessage());
        }
    }

    //=====读取=====

    /**
     * 依次读取三个文件，填入对应的三个Manager
     * 每次读取前先清理数据防止二次加载
     *
     * <p>只读不写：本方法自身不落盘。但加载过程中跳过的无效行会被暂存，
     * 等 {@link #saveAll()} 时原样写回，不会因为"加载时跳过"就被删掉。
     */
    public void loadAll() {
        ensureDir();
        loadStudents();
        loadSubjects();
        loadScores();
    }

    private void loadStudents() {
        studentManager.clearStudents();
        skippedStudentLines.clear();
        skippedStudentLines.addAll(loadFile(STUDENT_FILE, "学生", 3,
            parts -> studentManager.loadStudent(parts[0].trim(), parts[1].trim(), parts[2].trim())));
    }

    private void loadSubjects() {
        subjectManager.clearSubjects();
        skippedSubjectLines.clear();
        skippedSubjectLines.addAll(loadFile(SUBJECT_FILE, "科目", 2,
            parts -> subjectManager.loadSubject(parts[0].trim(), parts[1].trim())));
    }

    private void loadScores() {
        scoreManager.clearScores();
        skippedScoreLines.clear();
        skippedScoreLines.addAll(loadFile(SCORE_FILE, "成绩", 3,
            parts -> scoreManager.loadScore(parts[0].trim(), parts[1].trim(), parts[2].trim())));
    }

    //读取一个数据文件：字段不足、内容非法都跳过并提示，最后汇总跳过行数
    private List<String> loadFile(String filePath, String label, int minFields, Function<String[], String> rowLoader) {
        List<String> skippedLines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return skippedLines;
        int lineNo = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < minFields) {
                    System.out.println(file.getName() + " 第 " + lineNo + " 行字段不足，已跳过：" + line);
                    skippedLines.add(line);
                    continue;
                }
                String reason = rowLoader.apply(parts);
                if (reason != null) {
                    System.out.println(file.getName() + " 第 " + lineNo + " 行" + reason + "，已跳过：" + line);
                    skippedLines.add(line);
                }
            }
            if (!skippedLines.isEmpty()) {
                System.out.println(file.getName() + " 共跳过 " + skippedLines.size() + " 行无效数据，这些行会在保存时原样写回，请手工修复");
            }
        } catch (IOException e) {
            System.out.println("读取" + label + "数据失败：" + e.getMessage());
        }
        return skippedLines;
    }
}
