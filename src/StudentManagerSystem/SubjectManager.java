package StudentManagerSystem;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class SubjectManager {

    private final Repository<Subject> subjectRepo = new Repository<>("SUB", 3, "科目");

    public void showSubjectMenu(Scanner scanner, Consumer<String> onSunjectDeleted) {
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

    //添加科目 重复名校验 自动生成编号
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

    //删除科目，包括同名删除，确认删除，选择序号删除
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

    //打印全部科目
    public void listSubject(){
        List<Subject> subjects = subjectRepo.snapshot();

        if(subjects.isEmpty()){
            System.out.println("科目列表为空\n");
            return ;
        }
        String[] headers = {"科目编号", "科目名称"};
        int[] widths = {10, 14};
        ToolUtil.printTable("科目列表", headers, widths, subjects, (s, i) -> new String[]{
            s.getId(),
            s.getName()
        });
    }

    //让用户选择一个科目
    public Subject chooseSubject(Scanner scanner) {
        return ToolUtil.chooseFromList(scanner, "科目", "科目编号", "科目名称",
            subjectRepo.snapshot(), subjectRepo::findById, this::findByName);
    }

    //按科目名查找
    private Subject findByName(String name) {
        for (Subject s : subjectRepo.snapshot()) {
            if (s.getName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    //从文件加载一条科目数据。成功返回 null，失败返回原因
    public String loadSubject(String subId, String subName) {
        subId = ToolUtil.normalizeId(subId);
        if (subId.isEmpty()) return "科目编号为空";
        if (!subjectRepo.isId(subId)) return "科目编号格式错误"; 
        if (subName.isEmpty()) return "科目名称为空";
        subjectRepo.add(new Subject(subId, subName));
        return null;
    }
    //只读快照
    public List<Subject> snapshotSubjects() {
        return subjectRepo.snapshot(); 
    }
    //清空
    public void clearSubjects() {
        subjectRepo.clear();
    }

    public String getNameById(String subjectId) {
        return subjectRepo.getNameById(subjectId);
    }
}