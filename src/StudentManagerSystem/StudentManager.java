package StudentManagerSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StudentManager {

    //常量，消除魔法值
    private static final String ID_PREFIX = "STU";
    private static final int ID_DIGITS = 3;

    static ArrayList<StudentManager> studentList = new ArrayList<>();

    String stuId;
    String stuName;
    int age;

    public StudentManager(String id, String name, int age){
        this.stuId = id;
        this.stuName = name;
        this.age = age;
    }

    @Override
    public String toString(){
        return "学号: " + stuId + " 姓名: " + stuName + " 年龄: " + age;    
    }

    public static void studentManager(Scanner scanner){

        while (true) {
            System.out.println("\n===== 学生管理菜单 =====");
            System.out.println("1. 添加学生");
            System.out.println("2. 删除学生");
            System.out.println("3. 全部学生");
            System.out.println("0. 返回主菜单");
            System.out.println("------------------------"); 
            int studentManagerChoice = ToolUtil.readInt(scanner, "请选择对应的数字：", 0 , 3);
            
            switch (studentManagerChoice) {
                case 1 -> addStudent(scanner);   
                case 2 -> deleteStudent(scanner);
                case 3 -> listStudent();
                case 0 -> { return; }
                default -> System.out.println("输入错误，请重新输入");
            }
        }
    }

    //添加学生 重复名校验 自动生成学号
    public static void addStudent(Scanner scanner) {
        System.out.println("------------------------");
        System.out.println("输入学生姓名: ");
        String stuName = scanner.nextLine().trim();
        //空名检测
        if (stuName.isEmpty()) {
            System.out.println("学生姓名不能为空，已取消添加");
            return;
        }
        //重复名校验
        boolean exist = studentList.stream().anyMatch(s -> s.getStuName().equalsIgnoreCase(stuName));

        if (exist) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("已有同名学生，后续请注意学号区别");
        }
                    
        System.out.println("------------------------");
        int age = ToolUtil.readInt(scanner, "输入学生年龄: ", 12, 40);

        String stuId = setStudentId(studentList);
        studentList.add(new StudentManager(stuId, stuName, age));
        DataStore.saveAll();//更新数据
        System.out.println("------------------------"); 
        System.out.println("成功添加 " + stuName + " 同学 年龄：" + age);
        System.out.println("唯一学号为：" + stuId);
    }

    //删除学生，包括同名删除，确认删除，支持通过姓名，学号，选择序号删除
    public static void deleteStudent(Scanner scanner) {
        System.out.println("请选择要删除学生的序号、姓名或学号之一: ");
        StudentManager student = StudentManager.chooseStudent(scanner);
        if (student == null) {
            System.out.println("已取消删除");
            return;
        }

        System.out.println("已选择学生：" + student.getStuName());

        System.out.print("确认删除该学生以及其所有成绩?(Y/N): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("已取消删除");
            return;
        }

        studentList.remove(student);
        ScoreManager.removeScoreByStudent(student.getStuId());//绑定删除成绩
        DataStore.saveAll();//更新数据
        System.out.println("已删除学生： " + student.getStuName());
     
    }

    //自动生成学生ID
    public static String setStudentId(ArrayList<StudentManager> studentList){

        if (studentList.isEmpty()){
            return ID_PREFIX + "001";
        }
        int maxNum = 0;
        for (StudentManager stu : studentList) {
            String numStr = stu.getStuId().substring(ID_PREFIX.length());
            int num = Integer.parseInt(numStr);
            if(num > maxNum){
                maxNum = num;
            }
        }
        int newNum = maxNum + 1;
        if (newNum > Math.pow(10, ID_DIGITS) - 1) {
            throw new IllegalStateException("学生ID已达到最大值,无法生成新的ID");
        }
        return ID_PREFIX + String.format("%0" + ID_DIGITS + "d", newNum);
    }   

    //打印全部学生
    public static void listStudent() {
        if (studentList.isEmpty()) {
            System.out.println("学生列表为空"); 
            return;        
        }
        //拼表头
        StringBuilder header = new StringBuilder();
        header.append(ToolUtil.padRight("学号", 10));
        header.append(ToolUtil.padRight("姓名", 12));
        header.append(ToolUtil.padRight("年龄", 6));

        String headerStr = header.toString();

        //打印横幅 + 表头 + 分隔线
        System.out.println();
        ToolUtil.printBanner("学生列表", headerStr);
        System.out.println(headerStr);
        ToolUtil.printDivider('-', headerStr);

        //逐行输出
        for (StudentManager s : studentList) {
            StringBuilder line = new StringBuilder();
            line.append(ToolUtil.padRight(s.getStuId(), 10));
            line.append(ToolUtil.padRight(ToolUtil.truncate(s.getStuName(), 12), 14));
            line.append(ToolUtil.padRight(String.valueOf(s.getStuAge()), 6));
            System.out.println(line);
        }  
    }

    //判断输入是否为学生学号
    public static boolean isStudentId(String enterStr){

        if (enterStr == null) return false;
        String upper = enterStr.toUpperCase();

        return
        upper.startsWith(ID_PREFIX)
        && enterStr.length() == ID_PREFIX.length() + ID_DIGITS
        && upper.substring(ID_PREFIX.length()).matches("\\d+");

    }

    public String getStuId() { 
        return stuId; 
    }

    public String getStuName() {
        return stuName;
    }

    public int getStuAge() {
        return age;
    }

    //返回学生列表
    public static ArrayList<StudentManager> getStudentList() {

        return studentList;

    }

    //静态方法，通过id找name
    public static String getStuName(String studentId) {
        StudentManager stu = findStudentById(studentId);
        return (stu != null) ? stu.getStuName() : "未知学生";
    }

    //检查学生是否存在 返回学生对象，包括姓名

    public static StudentManager findStudentById(String studentId ){
        for (StudentManager s : studentList) {
            if (s.getStuId().equalsIgnoreCase(studentId)) {
                return s;
            }
        }
        return null;
    }
    /**
     * 让用户选择学生
     * 用户可以输入序号（1.2.3...）选择，也可以输入精确编号来选择
     * 输入0表示放弃选择
     * @return 选中的对象；如果用户取消或选择无效 返回null
     */

    public static StudentManager chooseStudent(Scanner scanner) {
        ArrayList<StudentManager> students = getStudentList();
        if (students.isEmpty()) {
            System.out.println("暂无学生，请先在学生管理中添加");
            return null;
        }

        //拼表头：序号 + 学号 + 姓名
        StringBuilder header = new StringBuilder();
        header.append(ToolUtil.padRight("序号", 6));
        header.append(ToolUtil.padRight("学号", 10));
        header.append(ToolUtil.padRight("姓名", 12));

        String headerStr = header.toString();

        System.out.println();
        ToolUtil.printBanner("请选择学生", headerStr);
        System.out.println(headerStr);
        ToolUtil.printDivider('-', headerStr);

        //打印行
        for (int i = 0; i < students.size(); i++) {
            StudentManager stu = students.get(i);
            StringBuilder line = new StringBuilder();
            line.append(ToolUtil.padRight(String.valueOf(i + 1), 6));
            line.append(ToolUtil.padRight(stu.getStuId(), 10));
            line.append(ToolUtil.padRight(ToolUtil.truncate(stu.getStuName(), 12), 12));
            System.out.println(line);
        }
        System.out.println("0.返回");
        System.out.print("请输入序号或者学号：");

        String input = scanner.nextLine().trim();
        if (input.equals("0")) {
            return null;
        }
        //先尝试解析序号解析
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= students.size()) {
                return students.get(idx - 1);
            } else {
                System.out.println("序号超出范围");
                return null;
            }
        } catch (NumberFormatException e) {
            // 不是数字，先当学号查
            StudentManager stu = findStudentById(input);

            if (stu == null) {
                //学号没找到，再尝试按姓名查
                List<StudentManager> matched = new ArrayList<>();
                for (StudentManager s : studentList) {
                    if (s.getStuName().equalsIgnoreCase(input)) {
                        matched.add(s);
                    }
                }
                if (matched.isEmpty()) {
                    System.out.println("未找到学号或姓名为 " + input + " 的学生");
                    return null;
                }
                if (matched.size() >1) {
                    System.out.println("存在多个同名学生，请改用学号选择：");
                    for (StudentManager s : matched) {
                        System.out.println("  " + s.getStuId() + " - " + s.getStuName());
                    }
                        return null;
                }
                stu = matched.get(0);
            }
            return stu;
        } 
        
    }       
}
