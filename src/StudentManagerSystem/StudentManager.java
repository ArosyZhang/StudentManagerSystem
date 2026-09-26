package StudentManagerSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class StudentManager {

    //常量，消除魔法值
    private static final int AGE_MIN = 12;
    private static final int AGE_MAX = 40;

    private final Repository<Student> studentRepo = new Repository<>("STU", 3, "学生");

    public void showStudentMenu(Scanner scanner, Consumer<String> onStudentDeleted){
        while (true) {
            System.out.println("\n===== 学生管理菜单 =====");
            System.out.println("1. 添加学生");
            System.out.println("2. 删除学生");
            System.out.println("3. 全部学生");
            System.out.println("0. 返回主菜单");
            System.out.println("------------------------");
            int studentManagerChoice = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0 , 3);

            switch (studentManagerChoice) {
                case 1 -> addStudent(scanner);
                case 2 -> {
                    Student deleted = deleteStudent(scanner);
                    if (deleted != null) {
                        onStudentDeleted.accept(deleted.getId());
                    }
                }
                case 3 -> listStudent();
                case 0 -> { return; }
                default -> System.out.println("输入错误，请重新输入");
            }
        }
    }

    //添加学生 重复名校验 自动生成学号
    public void addStudent(Scanner scanner) {
        System.out.println("------------------------");
        System.out.println("输入学生姓名: ");
        String stuName = ToolUtil.readLine(scanner);
        //空名检测
        if (stuName.isEmpty()) {
            System.out.println("学生姓名不能为空，已取消添加");
            return;
        }
        //重复名校验
        boolean exist = studentRepo.snapshot().stream().anyMatch(s -> s.getName().equalsIgnoreCase(stuName));

        if (exist) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("已有同名学生，后续请注意学号区别");
        }

        System.out.println("------------------------");
        int age = ToolUtil.readInt(scanner, "输入学生年龄: ", AGE_MIN, AGE_MAX);

        String stuId = studentRepo.generateId();
        studentRepo.add(new Student(stuId, stuName, age));
        System.out.println("------------------------");
        System.out.println("成功添加 " + stuName + " 同学 年龄：" + age);
        System.out.println("唯一学号为：" + stuId);
    }

    //删除学生，包括同名删除，确认删除，支持通过姓名，学号，选择序号删除
    public Student deleteStudent(Scanner scanner) {
        Student student = chooseStudent(scanner);
        if (student == null) {
            System.out.println("已取消删除");
            return null;
        }
        System.out.println("已选择学生：" + student.getName());

        System.out.print("确认删除该学生以及其所有成绩?(Y/N): ");
        String confirm = ToolUtil.readLine(scanner);
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("已取消删除");
            return null;
        }
        studentRepo.remove(student);
        System.out.println("已删除学生： " + student.getName());
        return student;
    }

    //打印全部学生
    public void listStudent() {
        List<Student> students = studentRepo.snapshot();
        if (students.isEmpty()) {
            System.out.println("学生列表为空");
            return;
        }
        String[] headers = {"学号", "姓名", "年龄"};
        int[] widths = {10, 14, 6};

        ToolUtil.printTable("学生列表", headers, widths, students, (s, i) -> new String[]{
            s.getId(),
            s.getName(),
            String.valueOf(s.getStuAge())
        });
    }

    //让学生选择一个学生
    public Student chooseStudent(Scanner scanner) {
        return ToolUtil.chooseFromList(scanner, "学生", "学号", "姓名",
            studentRepo.snapshot(), studentRepo::findById, this::findByName);
    }

    //按姓名查找学生；存在同名时打印列表并返回null
    private Student findByName(String name) {
        List<Student> matched = new ArrayList<>();
        for (Student s : studentRepo.snapshot()) {
            if (s.getName().equalsIgnoreCase(name)) {
                matched.add(s);
            }
        }
        if (matched.isEmpty()) {
            return null;
        }
        if (matched.size() > 1) {
            System.out.println("存在多个同名学生，请改用学号选择：");
            for (Student s : matched) {
                System.out.println("  " + s.getId() + " - " + s.getName());
            }
            return null;
        }
        return matched.get(0);
    }

    //从文件加载一条学生数据。成功返回 null，失败返回原因
    public String loadStudent(String stuId, String stuName, String stuAgeText) {
        stuId = ToolUtil.normalizeId(stuId);
        if (stuId.isEmpty()) return "学号为空";
        if (!studentRepo.isId(stuId)) return "学号格式错误";
        if (stuName.isEmpty()) return "姓名为空";
        int stuAge;
        try {
            stuAge = Integer.parseInt(stuAgeText);
        } catch (NumberFormatException e) {
            return "年龄不是数字";
        }
        if (stuAge < AGE_MIN || stuAge > AGE_MAX) return "年龄超出范围";
        studentRepo.add(new Student(stuId, stuName, stuAge));
        return null;
    }

    public List<Student> snapshotStudents() {
        return studentRepo.snapshot();
    }

    public void clearStudents() {
        studentRepo.clear();
    }

    public String getNameById(String studentId) {
        return studentRepo.getNameById(studentId);
    }
}
