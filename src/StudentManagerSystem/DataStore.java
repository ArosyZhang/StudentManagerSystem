package StudentManagerSystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

public class DataStore {
    private static final String DATA_DIR = System.getProperty("user.dir") + "/data";
    private static final String STUDENT_FILE = DATA_DIR + "/students.txt";
    private static final String SUBJECT_FILE = DATA_DIR + "/subjects.txt";
    private static final String SCORE_FILE = DATA_DIR + "/scores.txt";

    //确保data目录存在
    private static void ensureDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    //=====保存=====
    public static void saveAll() {
        ensureDir();
        saveStudents();
        saveSubjects();
        saveScores();
    }

    private static void saveStudents() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(STUDENT_FILE),"UTF-8"))){
            for (Student s : StudentManager.snapshotStudents()) {
                bw.write(s.getStuId() + "|" + s.getStuName() + "|" + s.getStuAge());
                bw.newLine();
            }            
        } catch (IOException e) {
            System.out.println("保存学生数据失败：" + e.getMessage());
        }
    }

    private static void saveSubjects() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SUBJECT_FILE),"UTF-8"))){
            for (Subject s : SubjectManager.snapshotSubjects()) {
                bw.write(s.getSubId() + "|" + s.getSubName());
                bw.newLine();
            }            
        } catch (IOException e) {
            System.out.println("保存科目数据失败：" + e.getMessage());
        }
    }

    private static void saveScores() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SCORE_FILE),"UTF-8"))){
            for (Map.Entry<String, Map<String, Double>> outer : ScoreManager.snapshotScores().entrySet()) {
                String studentId = outer.getKey();
                for (Map.Entry<String, Double> inner : outer.getValue().entrySet()) {
                    bw.write(studentId + "|" + inner.getKey() + "|" + inner.getValue());
                    bw.newLine();    
                }    
            }
        } catch (IOException e) {
            System.out.println("保存成绩数据失败：" + e.getMessage());
        }
    }

    //=====读取=====
    public static void loadAll() {
        ensureDir();
        loadStudents();
        loadSubjects();
        loadScores();
    }

    private static void loadStudents() {
        StudentManager.clearStudents();
        File file= new File(STUDENT_FILE);
        if (!file.exists()) return ;
        int lineNo = 0;
        int skipped = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;    
                String[] parts = line.split("\\|", -1);

                if (parts.length < 3) {
                    System.out.println(file.getName() + " 第 " + lineNo + " 行字段不足，已跳过：" + line);
                    skipped++;
                    continue;
                }
                String reason = StudentManager.loadStudent(parts[0].trim(), parts[1].trim(), parts[2].trim());
                if (reason != null) {
                    System.out.println(file.getName() + " 第 " + lineNo + " 行" + reason + "，已跳过：" + line);
                    skipped++;
                }
            }
            if (skipped > 0) {
                System.out.println(file.getName() + " 共跳过 " + skipped + " 行无效数据");
            }   
        } catch (IOException e) {
            System.out.println("读取学生数据失败：" + e.getMessage());
        }
    }

    private static void loadSubjects() {
        SubjectManager.clearSubjects();
        File file= new File(SUBJECT_FILE);
        if (!file.exists()) return ;
        int lineNo = 0;
        int skipped = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;                    
                String[] parts = line.split("\\|", -1);
                
                if (parts.length < 2) {
                    System.out.println(file.getName() + " 第 " + lineNo + " 行字段不足，已跳过：" + line);
                    skipped++;
                    continue;
                }
                String reason = SubjectManager.loadSubject(parts[0].trim(), parts[1].trim());
                if (reason != null) {
                    System.out.println(file.getName() + " 第 " + lineNo + " 行" + reason + "，已跳过：" + line);
                    skipped++;
                }
            }
            if (skipped > 0) {
                System.out.println(file.getName() + " 共跳过 " + skipped + " 行无效数据");
            }   
        } catch (IOException e) {
            System.out.println("读取科目数据失败：" + e.getMessage());
        }
    }

    private static void loadScores() {
        ScoreManager.clearScores();
        File file = new File(SCORE_FILE);
        if (!file.exists()) return;
        int lineNo = 0;
        int skipped = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue; 
                String[] parts = line.split("\\|", -1);

                if (parts.length < 3) {
                    System.out.println(file.getName() + " 第 " + lineNo + " 行字段不足，已跳过：" + line);
                    skipped++;
                    continue;
                }
                String reason = ScoreManager.loadScore(parts[0].trim(), parts[1].trim(), parts[2].trim());
                if (reason != null) {
                    System.out.println(file.getName() + " 第 " + lineNo + " 行" + reason + "，已跳过：" + line);
                    skipped++;
                }                
            }
            if (skipped > 0) {
                System.out.println(file.getName() + " 共跳过 " + skipped + " 行无效数据");
            }        
        } catch (IOException e) {
            System.out.println("读取成绩数据失败：" + e.getMessage());
        }
    }
}
