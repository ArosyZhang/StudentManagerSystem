package StudentManagerSystem;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * 工具箱：集合需要用到的工具：字符串显示宽度计算（分中英文）；全角判断；右补空格；打印标题横幅；打印自适应分隔线；
 * 超显示宽度截断；全局输入校验（int型和double型）带提示信息+范围；归一化处理；输入流相关；通用表格渲染；用户选择列表；
 *
 * <p>计算字符长度相关为私有类，其余全局生效
 *
 * @author ArosyZhang
 * @since 1.0
 */
public class ToolUtil {

    // 计算字符串的显示宽度（中文算2，英文算1）
    private static int displayWidth(String s) {
        if (s == null) return 0;
        int w = 0;
        for (char c : s.toCharArray()) {
            w += isFullWidth(c) ? 2 : 1;
        }
        return w;
    }

    /**
     * 判断字符是否为全角（显示宽度 2）。
     * 只把 CJK、谚文、全角符号等区块算作 2，其他非 ASCII 算 1。
     */
    private static boolean isFullWidth(char c) {
        return (c >= 0x1100 && c <= 0x115F) ||   // 谚文字母
               (c >= 0x2E80 && c <= 0xA4CF) ||   // CJK 部首、符号、注音、汉字
               (c >= 0xAC00 && c <= 0xD7A3) ||   // 谚文音节
               (c >= 0xF900 && c <= 0xFAFF) ||   // CJK 兼容汉字
               (c >= 0xFE30 && c <= 0xFE6F) ||   // CJK 兼容形式
               (c >= 0xFF00 && c <= 0xFF60) ||   // 全角 ASCII、全角标点
               (c >= 0xFFE0 && c <= 0xFFE6);     // 全角货币符号
    }

    //右补空格到指定显示宽度 中文算2
    private static String padRight(String s, int width) {
        if (s == null) s = " ";
        StringBuilder sb = new StringBuilder(s);
        int w = displayWidth(s);
        while (w < width) {
            sb.append(' ');
            w++;
        }
        return sb.toString();
    }

    /**
     * 打印标题横幅，宽度与给定参考字符串一致，两侧则用=填充
     *
     * @param title         标题名
     * @param reference
     */
    public static void printBanner(String title, String reference) {
        int totalWidth = displayWidth(reference);
        String t = " " + title + " ";
        int side = Math.max((totalWidth - displayWidth(t)) / 2, 0);
        int rightSide = Math.max(totalWidth - displayWidth(t) - side, 0);
        System.out.println("=".repeat(side) + t + "=".repeat(rightSide));
    }

    /**
     * 打印指定宽度的分隔线
     * @param ch            组成分割线的字符
     * @param reference
     */
    public static void printDivider(char ch, String reference) {
        System.out.println(String.valueOf(ch).repeat(displayWidth(reference)));
    }

    /**
     * 按显示宽度截断字符，超出部分用“...”代替
     * 中文2 英文1
     * "..." 占 3 格，再多留 1 格余量，所以先按 maxWidth - 4 收口
     *
     * @param s         传入的字符串
     * @param maxWidth  最大显示宽度
     * @return          裁剪后的字符串 + "..."的拼接字符串
     */
    public static String truncate(String s, int maxWidth) {
        if (s == null) return " ";
        if (displayWidth(s) <= maxWidth) return s;

        StringBuilder sb = new StringBuilder();
        int w = 0;
        for (char c : s.toCharArray()) {
            int cw = isFullWidth(c) ? 2 : 1;
            if (w + cw > maxWidth - 4) break;
            sb.append(c);
            w += cw;
        }
        sb.append("...");
        return sb.toString();
    }

    //全局输入校验方法
    /**
     * 从控制台读取一个整数，并限制在指定范围内。
     * 如果输入不是整数或超出范围，会提示并让用户重新输入。
     *
     * @param scanner 本次会话共用的 Scanner
     * @param prompt  提示信息
     * @param min     允许的最小值
     * @param max     允许的最大值
     * @return        用户输入的合法整数
     */
    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        int result;
        while (true) {
            System.out.print(prompt);
            String input = readLine(scanner);//读取一行去掉首尾空格

            try {
                result = Integer.parseInt(input);
                if (result < min || result > max) {
                    System.out.println("输入超出范围，请输入 " + min + "~" + max + "之间的整数");
                    continue;
                }
                return result;
            } catch (NumberFormatException e) {
                System.out.println("输入无效，请输入一个整数");
            }
        }
    }

    /**
     * 从控制台读取一个浮点数，并限制在指定范围内。
     * 如果输入不是浮点数或超出范围，会提示并让用户重新输入。
     *
     * @param scanner 全局共享的 Scanner 对象
     * @param prompt  提示信息
     * @param min     允许的最小值
     * @param max     允许的最大值
     * @return        用户输入的合法浮点数
     */
    public static double readDouble(Scanner scanner, String prompt, double min ,double max) {
        double result;
        while (true) {
            System.out.print(prompt);
            String input = readLine(scanner);//读取一行去掉首尾空格

            try {
                result = Double.parseDouble(input);
                if (!Double.isFinite(result)) {
                    System.out.println("输入无效，请输入一个有限的数字");
                    continue;
                }
                if (result < min || result > max) {
                    System.out.println("输入超出范围，请输入 " + min + "~" + max + "之间的数字");
                    continue;
                }
                return result;
            } catch (NumberFormatException e) {
                System.out.println("输入无效，请输入一个数字");
            }
        }
    }

    /**
     * 全局归一化处理方法
     *
     * @param id        需要归一化的字符串
     * @return          去空格+转大写
     */
    public static String normalizeId(String id) {
        return id == null ? "" : id.trim().toUpperCase();
    }

    /**
     * 输入流已结束（Ctrl+Z 或管道输入耗尽）时抛出
     */
    public static class InputCloseException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public InputCloseException(String message) {
            super(message);
        }
    }

    /**
     * {@code #nextLine()} 在流结束时抛的是 NoSuchElementException，而"空集合的迭代器"抛的也是它,
     * 两者撞车会把真正的 bug 伪装成一次正常退出，所以要自己包一层并 {@link InputCloseException}
     *
     * @param scanner   输入内容
     * @return          去掉首尾空格的的字符串
     */
    public static String readLine(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            throw new InputCloseException("输入流已结束");
        }
        return scanner.nextLine().trim();
    }

    //===========通用表格渲染===========

    /**
     * 行映射器接口，用于将对象映射为表格行
     * @param <T> 表格每一行代表的那个对象的类型（本项目是 {@link Student}、{@link Subject}，以及 {@link ScoreManager} 内部给成绩表用的 StudentRow）
     */
    @FunctionalInterface
    public interface RowMapperWithIndex<T> {
        String[] map(T item, int index);
    }

    /**
     * 渲染一张等宽表格：表头、分隔线、数据行公用一份列宽定义
     *
     * @param <T>       表格每一行代表的那个对象的类型(本项目是 {@link Student}、{@link Subject}，以及 {@link ScoreManager}内部给成绩表用的 StudentRow)
     * @param title     表格标题
     * @param headers   列名
     * @param widths    每列的显示宽度,中文按2算，长度必须与headers一致
     * @param list      数据行
     * @param mapper    如何从一行数据取出每列的文本
     */
    public static <T> void printTable(String title, String[] headers, int[] widths, List<T> list, RowMapperWithIndex<T> mapper) {

        //1.接口自保：预防非法输入
        if (list == null || list.isEmpty()) return;
        if (headers.length != widths.length) {
            throw new IllegalArgumentException("表头列数与列宽数不一致:headers=" + headers.length + ", widths=" + widths.length);
        }

        //2.拼表头：按widths 逐列填充
        StringBuilder headerSb = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            headerSb.append(padRight(headers[i], widths[i]));
        }
        String headerStr = headerSb.toString();

        //3.横幅+表头+分隔线
        System.out.println();
        printBanner(title, headerStr);
        System.out.println(headerStr);
        printDivider('-', headerStr);

        //4.打印行
        for (int i = 0; i < list.size(); i++) {
            String[] cells = mapper.map(list.get(i), i);
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < cells.length; j++) {
                line.append(padRight(truncate(cells[j], widths[j]), widths[j]));
            }
            System.out.println(line);
        }
    }

    /**
     * 让用户从一个列表里选择一个对象
     * 可以输入序号选择，也可以输入唯一编号或者名称选择；输入0表示放弃选择
     *
     * @param <T>           要从列表里挑出来的那种实体的类型（{@link Student} 或 {@link Subject}）
     * @param scanner       输入内容可能是选择序号、编号、名称需要逐步解析
     * @param label         传入数据页标签（学生/科目）
     * @param idLabel       传入id标签（学号/科目编号）
     * @param nameLabel     传入的名称标签
     * @param items         候选对象列表（调用方给的快照，本方法不修改它）
     * @param idLookup      由调用方提供的"按编号查找"方法；找不到返回 null
     * @param nameLookup    由调用方提供的"按名称查找"方法；找不到返回 null。<b>允许传 null</b>，表示该调用方不支持按名称选择
     * @return              选中的对象；取消选择或没找到返回null
     */
    public static <T extends Entity> T chooseFromList(Scanner scanner, String label, String idLabel, String nameLabel, List<T> items, Function<String, T> idLookup, Function<String, T> nameLookup) {
        if (items.isEmpty()) {
            System.out.println("暂无" + label + "，请先在" + label + "管理中添加");
            return null;
        }

        String[] headers = {"序号", idLabel, nameLabel};
        int[] widths = {6, 10, 14};
        printTable("请选择" + label, headers, widths, items, (item, i) -> new String[]{String.valueOf(i + 1), item.getId(), item.getName()});

        System.out.println("0.返回");
        System.out.print("请输入序号或者" + idLabel + ": ");

        String input = readLine(scanner);
        if (input.equals("0")) {
            return null;
        }

        //先按序号解析
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= items.size()) {
                return items.get(idx - 1);
            }
            System.out.println("序号超出范围");
            return null;
        } catch (NumberFormatException e) {
            //不是数字，先按编号查找，再按名称查
            T item = idLookup.apply(input);
            if (item == null && nameLookup != null) {
                item = nameLookup.apply(input);
            }
            if (item == null) {
                System.out.println("未找到" + idLabel + "或" + nameLabel + "为 " + input + " 的" + label);
            }
            return item;
        }
    }
}
