package StudentManagerSystem;
import java.util.ArrayList;
import java.util.Scanner;

public class SubjectManager {

    //常量，消除魔法值
    private static final String ID_PREFIX = "SUB";
    private static final int ID_DIGITS = 3;
    private static final String ID_NUM = "001";

    static ArrayList<Subject> subjectList = new ArrayList<>();

    public static void showSubjectMenu(Scanner scanner ) {

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
    public static void addSubject(Scanner scanner) {
        System.out.println("------------------------");
        System.out.println("输入科目名称: ");
        String subName = scanner.nextLine().trim();
        //空名检测
        if (subName.isEmpty()) {
            System.out.println("科目名称不能为空，已取消添加");
            return;
        }
        //重复名校验
        boolean exist = subjectList.stream().anyMatch(s -> s.getSubName().equalsIgnoreCase(subName));

        if (exist) {
            System.out.println("该科目已存在，结束添加");
            return;
        }
    
        String subId = setSubjectId(subjectList);
        subjectList.add(new Subject(subId, subName));
        DataStore.saveAll();//更新数据
        System.out.println("------------------------"); 
        System.out.println("添加《" + subName + "》 成功");
    }

    //删除科目，包括同名删除，确认删除，选择序号删除
    public static void deleteSubject(Scanner scanner) {
        System.out.println("请选择要删除科目的序号、名称或编号之一: ");
        Subject subject = chooseSubject(scanner);
        if (subject == null) {
            System.out.println("已取消删除");
            return;
        }

        System.out.println("已选择科目：" + subject.getSubName());

        System.out.print("确认删除该科目以及其所有成绩?(Y/N): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("已取消删除");
            return;
        }

        subjectList.remove(subject);
        ScoreManager.removeScoreBySubject(subject.getSubId());//绑定删除成绩
        DataStore.saveAll();//更新数据
        System.out.println("已删除科目和其所有成绩： " + subject.getSubName());
     
    }

    //自动生成科目id
    public static String setSubjectId(ArrayList<Subject> subjectList){

        if (subjectList.isEmpty()){
            return ID_PREFIX + ID_NUM;
        }
        int maxNum = 0;
        for (Subject sub : subjectList) {
            String numStr = sub.getSubId().substring(ID_PREFIX.length());
            int num = Integer.parseInt(numStr);
            if(num > maxNum){
                maxNum = num;
            }
        }
        int newNum = maxNum + 1;
        if (newNum > (int) Math.pow(10, ID_DIGITS) - 1) {
            throw new IllegalStateException("科目ID已达到最大值,无法生成新的ID");
        }
        return ID_PREFIX + String.format("%0" + ID_DIGITS + "d", newNum);
    }

    //打印全部科目
    public static void listSubject(){

        if(subjectList.isEmpty()){
            System.out.println("科目列表为空\n");
            return ;

        }
        
        String[] headers = {"科目编号", "科目名称"};
        int[] widths = {10, 14};
        ToolUtil.printTable("科目列表", headers, widths, subjectList, (s, i) -> new String[]{
            s.getSubId(),
            s.getSubName()
        });
    }

    //判断输入是否为科目ID
    public static boolean isSubjectId(String enterStr){

        if (enterStr == null) return false;
        String upper = enterStr.toUpperCase();

        return 
        upper.startsWith(ID_PREFIX)
        && upper.length() == ID_PREFIX.length() + ID_DIGITS
        && upper.substring(ID_PREFIX.length()).matches("\\d+"); 
    }

    //返回科目列表
    public static ArrayList<Subject> getSubjectList() {
        return subjectList;
    }

    //静态方法，通过id找name
    public static String getSubName(String subjectId) {
        Subject sub = findSubjectById(subjectId);
        return (sub != null) ? sub.getSubName() : "未知科目";
    }

    //检查科目是否存在 返回科目对象，包括名称
    public static Subject findSubjectById(String subjectId ){
        for (Subject s : subjectList) {
            if (s.getSubId().equalsIgnoreCase(subjectId)) {
                return s;
            }
        }
        return null;
    }

    /**
     * 让用户选择科目
     * 用户可以输入序号（1.2.3...）选择，也可以输入精确编号来选择
     * 输入0表示放弃选择
     * @return 选中的对象；如果用户取消或选择无效 返回null
     */

    public static Subject chooseSubject(Scanner scanner) {
        ArrayList<Subject> subjects = getSubjectList();
        if (subjects.isEmpty()) {
            System.out.println("暂无科目，请先在科目管理中添加");
            return null;
        }

        String[] headers = {"序号", "科目编号", "科目名称"};
        int[] widths = {6, 10, 14};

        ToolUtil.printTable("请选择科目", headers, widths, subjects, (s, i) -> new String[]{
            String.valueOf(i + 1),
            s.getSubId(),
            s.getSubName()
        });

        System.out.println("0.返回");
        System.out.print("请输入序号或者科目编号：");

        String input = scanner.nextLine().trim();
        if (input.equals("0")) {
            return null;
        }

        //先尝试解析序号解析
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= subjects.size()) {
                return subjects.get(idx - 1);
            } else {
                System.out.println("序号超出范围");
                return null;
            }
        } catch (NumberFormatException e) {
            // 不是数字，当作编号处理
            Subject sub = findSubjectById(input);
            if (sub == null) {
                System.out.println("未找到编号为 " + input + " 的科目");
            }
            return sub;
        }
    }
  
}
