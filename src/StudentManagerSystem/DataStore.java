package StudentManagerSystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
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
            for (StudentManager s : StudentManager.getStudentList()) {
                bw.write(s.getStuId() + "|" + s.getStuName() + "|" + s.getStuAge());
                bw.newLine();
            }            
        } catch (IOException e) {
            System.out.println("保存学生数据失败：" + e.getMessage());
        }
    }

    private static void saveSubjects() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SUBJECT_FILE),"UTF-8"))){
            for (SubjectManager s : SubjectManager.getSubjectList()) {
                bw.write(s.getSubId() + "|" + s.getSubName());
                bw.newLine();
            }            
        } catch (IOException e) {
            System.out.println("保存科目数据失败：" + e.getMessage());
        }
    }

    private static void saveScores() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SCORE_FILE),"UTF-8"))){
            for (Map.Entry<String, Map<String, Double>> outer : ScoreManager.getScoreMap().entrySet()) {
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
        StudentManager.getStudentList().clear();
        File file= new File(STUDENT_FILE);
        if (!file.exists()) return ;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;    
                }
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    StudentManager.getStudentList().add(new StudentManager(parts[0], parts[1], Integer.parseInt(parts[2])));
                }
            }   
        } catch (IOException e) {
            System.out.println("读取学生数据失败：" + e.getMessage());
        }
    }

    private static void loadSubjects() {
        SubjectManager.getSubjectList().clear();
        File file= new File(SUBJECT_FILE);
        if (!file.exists()) return ;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;                    
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    SubjectManager.getSubjectList().add(new SubjectManager(parts[0], parts[1]));
                }
            }   
        } catch (IOException e) {
            System.out.println("读取科目数据失败：" + e.getMessage());
        }
    }

    private static void loadScores() {
        ScoreManager.getScoreMap().clear();
        File file = new File(SCORE_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; 
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    String studentId = parts[0];
                    String subjectId = parts[1];
                    double score = Double.parseDouble(parts[2]);
                    ScoreManager.getScoreMap().computeIfAbsent(studentId, k -> new HashMap<>()).put(subjectId, score);
                }
            }
        
        } catch (IOException e) {
            System.out.println("读取成绩数据失败：" + e.getMessage());
        }

    }
}
