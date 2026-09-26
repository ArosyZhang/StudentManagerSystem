package StudentManagerSystem;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * 科目业务管理：负责科目新增、删除、查询和从文件加载一条科目记录
 *
 * <p>边界：不负责读写文件（那是 {@link DataStore} 的事），不负责成绩（那是 {@link ScoreManager} 的事），
 * 也不决定"什么时候保存"（那是组合根 {@link StudentManagerSystem} 的事）。
 *
 * 本类不能直接持有 ScoreManager —— 一旦两个 Manager 互相持有就成了循环依赖，谁的构造函数都写不出来
 * 删除一个科目时由 {@link Consumer}回调返回科目编号，通知 {@link StudentManagerSystem} 删除该科目编号下对应的所有成绩
 *
 * @author ArosyZhang
 * @since 1.0
 */
public class SubjectManager {

    /** 本类的全部数据都在这个仓库里，本类自己不维护任何集合。 */
    private final Repository<Subject> subjectRepo = new Repository<>("SUB", 3, "科目");

    /**
     * 显示科目管理菜单：添加、删除和显示全部科目；0返回上级菜单
     *
     * @param scanner           读取用户输入
     * @param onSubjectDeleted  科目删除后的回调参数，被删科目的唯一编号，本类不能直接操作 {@link ScoreManager}，
     *                          由组合根 {@link StudentManagerSystem} 接上"同时删除该科目下全部成绩"
     */
    public void showSubjectMenu(Scanner scanner, Consumer<String> onSubjectDeleted) {
        while (true) {
            System.out.println("\n===== 科目管理菜单 =====");
            System.out.println("1.添加新科目");
            System.out.println("2.删除科目");
            System.out.println("3.全部科目");
            System.out.println("0.返回主菜单");
            System.out.println("----------------------------");
            int subjectManagerChoice = ToolUtil.readInt(scanner, "请选择对应的数字: ", 0, 3);

            switch (subjectManagerChoice) {
                case 1 -> addSubject(scanner);
                case 2 -> deleteSubject(scanner);
                case 3 -> listSubject();
                case 0 -> { return; }
                default -> System.out.println("输入错误，请重新输入");
            }
        }
    }

    /**
     * 添加一门科目：输入科目名称，由 {@link Repository#generateId()} 自动生成科目编号并添加
     * 空名和重复名不允许添加
     *
     * @param scanner   读取用户输入
     */
    public void addSubject(Scanner scanner) {
        System.out.println("------------------------");
        System.out.println("输入科目名称: ");
        String subName = ToolUtil.readLine(scanner);
        //空名检测
        if (subName.isEmpty()) {
            System.out.println("科目名称不能为空，已取消添加");
            return;
        }
        //重复名校验
        boolean exist = subjectRepo.snapshot().stream().anyMatch(s -> s.getName().equalsIgnoreCase(subName));

        if (exist) {
            System.out.println("该科目已存在，结束添加");
            return;
        }
        String subId = subjectRepo.generateId();
        subjectRepo.add(new Subject(subId, subName));
        System.out.println("------------------------");
        System.out.println("添加《" + subName + "》 成功");
    }

    /**
     * 删除一门科目：调用 {@link chooseSubject} 来选择一门科目，二次确认删除
     *
     * @param scanner   读取用户输入
     * @return          被删除的科目；用户输入 0、没选到科目、或确认时没输入 Y 时返回 <b>null</b>。
     *                  返回 null 表示"什么都没删"，调用方据此决定要不要去通知删除该科目成绩
     */
    public Subject deleteSubject(Scanner scanner) {
        Subject subject = chooseSubject(scanner);
        if (subject == null) {
            System.out.println("已取消删除");
            return null;
        }
        System.out.println("已选择科目：" + subject.getName());

        System.out.print("确认删除该科目以及其所有成绩?(Y/N): ");
        String confirm = ToolUtil.readLine(scanner);
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("已取消删除");
            return null;
        }
        subjectRepo.remove(subject);
        System.out.println("已删除科目和其所有成绩： " + subject.getName());
        return subject;
    }

    /**
     * 打印全部科目的表格（科目编号 + 科目名称）。列表为空时只提示一句，不打印空表头。
     */
    public void listSubject() {
        List<Subject> subjects = subjectRepo.snapshot();

        if (subjects.isEmpty()) {
            System.out.println("科目列表为空");
            return;
        }
        String[] headers = {"科目编号", "科目名称"};
        int[] widths = {10, 14};
        ToolUtil.printTable("科目列表", headers, widths, subjects, (s, i) -> new String[]{
            s.getId(),
            s.getName()
        });
    }

    /**
     * 让用户从全部科目中选一个：可以输入序号、科目编号或名称（大小写不敏感）。
     *
     * @param scanner   用于读取用户输入
     * @return          选中的科目；输入 0、找不到返回 null
     */
    public Subject chooseSubject(Scanner scanner) {
        return ToolUtil.chooseFromList(scanner, "科目", "科目编号", "科目名称",
            subjectRepo.snapshot(), subjectRepo::findById, this::findByName);
    }

    /**
     * 按名称精确查找不区分大小写
     *
     * @param name  要找的名称
     * @return      返回匹配的科目对象
     */
    private Subject findByName(String name) {
        for (Subject s : subjectRepo.snapshot()) {
            if (s.getName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    /**
     * 从文件加载一条科目记录。本方法<b>不检查文件是否存在</b>，那是 {@link DataStore} 的职责。
     * 失败原因有多个，所以返回 {@link String} 类型
     * 调用方需要把具体原因拼进"第 N 行 xxx，已跳过：..."的提示里；
     *
     * @param subId     科目编号，先经 {@link ToolUtil#normalizeId} 归一化（null 变 ""、去首尾空格、转大写）
     * @param subName   科目名称，不做归一化，只判空
     * @return          <b>成功返回 null</b>；失败返三种原因：{@code "科目编号为空"}、{@code "科目编号格式错误"}、{@code "科目名称为空"}
     */
    public String loadSubject(String subId, String subName) {
        subId = ToolUtil.normalizeId(subId);
        if (subId.isEmpty()) return "科目编号为空";
        if (!subjectRepo.isId(subId)) return "科目编号格式错误";
        if (subName.isEmpty()) return "科目名称为空";
        subjectRepo.add(new Subject(subId, subName));
        return null;
    }

    /**
     * 返回全部科目的快照。
     *
     * @return  <b>不可变副本</b>：改动它既不会影响仓库，也不会影响后续调用
     */
    public List<Subject> snapshotSubjects() {
        return subjectRepo.snapshot();
    }


    /**
     * 清空全部科目。<b>只在加载数据前调用</b>：{@link DataStore#loadAll()} 每次都先清空再读，否则文件不存在时会残留上一次运行的数据
     */
    public void clearSubjects() {
        subjectRepo.clear();
    }

    /**
     * 按科目编号取科目名称，用于打印需要显示科目名称的地方。
     *
     * @param subjectId 科目编号
     * @return          科目名称；找不到时返回 {@code "未知科目"}（<b>不是 null</b>，调用方不必判空）
     */
    public String getNameById(String subjectId) {
        return subjectRepo.getNameById(subjectId);
    }
}
