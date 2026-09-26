package StudentManagerSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * 学生业务的管理者：负责学生的新增、删除、查询，以及从文件加载一条学生记录。
 *
 * <p>边界：不负责读写文件（那是 {@link DataStore} 的事），不负责成绩（那是 {@link ScoreManager} 的事），
 * 也不决定"什么时候保存"（那是组合根 {@link StudentManagerSystem} 的事）。
 *
 * <p><b>本类为什么收一个 {@link Consumer} 回调？</b>删掉一个学生时，他的成绩也必须一起删除，
 * 但本类不能直接持有 {@link ScoreManager} —— 一旦两个 Manager 互相持有就成了循环依赖，
 * 谁的构造函数都写不出来。所以改成"删完把学号喊出去，谁关心谁处理"：
 * 由组合根在构造时把 {@code scoreManager::removeScoreByStudent} 接上来。
 *
 * @author ArosyZhang
 * @since 1.0
 */
public class StudentManager {
    //常量，消除魔法值
    private static final int AGE_MIN = 12;
    private static final int AGE_MAX = 40;

    /** 本类的全部数据都在这个仓库里，本类自己不维护任何集合。 */
    private final Repository<Student> studentRepo = new Repository<>("STU", 3, "学生");

    /**
     * 显示学生管理菜单，循环处理用户选择，直到输入 0 返回。
     *
     * @param scanner           用于读取用户输入
     * @param onStudentDeleted  学生被成功删除后的回调，参数是被删学生的学号。本类不认识 {@link ScoreManager}，靠这个回调把"该生没了"这件事传出去，
     *                          由组合根 {@link StudentManagerSystem} 接上"同时删除其全部成绩"
     */
    public void showStudentMenu(Scanner scanner, Consumer<String> onStudentDeleted){
        while (true) {
            System.out.println("\n===== 学生管理菜单 =====");
            System.out.println("1. 添加学生");
            System.out.println("2. 删除学生");
            System.out.println("3. 全部学生");
            System.out.println("0. 返回主菜单");
            System.out.println("------------------------");
            int studentManagerChoice = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0, 3);

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

    /**
     * 交互式添加一名学生：依次询问姓名和年龄，学号由 {@link Repository#generateId()} 自动生成。
     *
     * <p>姓名重复只提醒"注意学号区别"，<b>不阻止添加</b> —— 同名是合法的。
     * <p>年龄经 {@link ToolUtil#readInt} 约束在 {@link #AGE_MIN}~{@link #AGE_MAX} 之间，
     * 输入非法会一直重问，所以走到下面时拿到的年龄一定合法。
     *
     * @param scanner   用于读取用户输入
     */
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

    /**
     * 交互式删除一名学生：先让学生选择，再要求二次确认。
     *
     * @param scanner   用于读取用户输入
     * @return          被删除的学生；用户输入 0、没选到人、或确认时没输入 Y 时返回 <b>null</b>。
     *                  返回 null 表示"什么都没删"，调用方据此决定要不要去通知删除该生成绩
     */
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

    /** 打印全部学生的表格（学号 / 姓名 / 年龄）。列表为空时只提示一句，不打印空表头。 */
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

    /**
     * 让用户从全部学生中选一个：可以输入序号、学号或姓名（大小写不敏感）。
     *
     *
     * @param scanner   用于读取用户输入
     * @return          选中的学生；输入 0、找不到、或姓名撞上多人时返回 null
     */
    public Student chooseStudent(Scanner scanner) {
        return ToolUtil.chooseFromList(scanner, "学生", "学号", "姓名",
            studentRepo.snapshot(), studentRepo::findById, this::findByName);
    }

    /**
     * 按姓名精确查找（忽略大小写）。
     *
     * <p><b>同名多人时返回 null，而不是随便挑一个</b> —— 挑错人的代价比让用户重选大得多。
     * 这种情况下会把候选列表打印出来，引导用户改用学号。
     */
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

    /**
     * 从文件加载一条学生记录。本方法<b>不检查文件是否存在</b>，那是 {@link DataStore} 的职责。
     *
     * <p>返回类型为什么是 {@link String} 而不是 {@code boolean}？因为失败原因不止一种，
     * 调用方需要把具体原因拼进"第 N 行 xxx,已跳过：..."的提示里；只给 true/false 的话，用户看到提示也不知道到底该改哪。
     *
     * @param stuId         学号，先经 {@link ToolUtil#normalizeId} 归一化（null 变 ""、去首尾空格、转大写）
     * @param stuName       姓名，不做归一化，只判空
     * @param stuAgeText    年龄的文本形式，可能不是数字
     * @return              <b>成功返回 null</b>；失败返回具体原因，取值只有这五种：
     *                      {@code "学号为空"}、{@code "学号格式错误"}、{@code "姓名为空"}、{@code "年龄不是数字"}、{@code "年龄超出范围"}
     */
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

    /**
     * 返回全部学生的快照。
     *
     * @return  <b>不可变副本</b>：改动它既不会影响仓库，也不会影响后续调用
     */
    public List<Student> snapshotStudents() {
        return studentRepo.snapshot();
    }

    /** 清空全部学生。<b>只在加载数据前调用</b>：{@link DataStore#loadAll()} 每次都先清空再读，否则文件不存在时会残留上一次运行的数据 */
    public void clearStudents() {
        studentRepo.clear();
    }

    /**
     * 按学号取学生姓名，用于打印需要显示姓名的地方。
     *
     * @param studentId     studentId 学号
     * @return              学生的姓名；找不到时返回 {@code "未知学生"}（<b>不是 null</b>，调用方不必判空）
     */
    public String getNameById(String studentId) {
        return studentRepo.getNameById(studentId);
    }
}
