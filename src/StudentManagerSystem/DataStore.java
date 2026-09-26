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

public class DataStore {
    private static final String DATA_DIR = System.getProperty("user.dir") + "/data";
    private static final String STUDENT_FILE = DATA_DIR + "/students.txt";
    private static final String SUBJECT_FILE = DATA_DIR + "/subjects.txt";
    private static final String SCORE_FILE = DATA_DIR + "/scores.txt";

    private final StudentManager studentManager;
    private final SubjectManager subjectManager;
    private final ScoreManager scoreManager;

    private final List<String> skippedStudentLines = new ArrayList<>();
    private final List<String> skippedSubjectLines = new ArrayList<>();
    private final List<String> skippedScoreLines = new ArrayList<>();

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
