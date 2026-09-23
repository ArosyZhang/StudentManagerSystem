package StudentManagerSystem;
import java.util.List;
import java.util.Scanner;

public class ToolUtil {
    //计算字符串的显示宽度（中文算2，英文算1）
    public static int displayWidth(String s) {
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
    public static String padRight(String s, int width) {
        if (s == null) s = " ";
        StringBuilder sb = new StringBuilder(s);
        int w = displayWidth(s);
        while (w < width) {
            sb.append(' ');
            w++;
        }
        return sb.toString();    
    }

    //打印标题横幅，宽度与给定参考字符串一致，两侧则用=填充
    public static void printBanner(String title, String reference) {
        int totalWidth = displayWidth(reference);
        String t = " " + title + " ";
        int side = Math.max((totalWidth - displayWidth(t)) / 2, 0);
        int rightSide = Math.max(totalWidth - displayWidth(t) - side, 0);
        System.out.println("=".repeat(side) + t + "=".repeat(rightSide));
    }

    //打印指定宽度的分隔线
    public static void printDivider(char ch, String reference) {
        System.out.println(String.valueOf(ch).repeat(displayWidth(reference)));
    }

    /**
     * 按显示宽度截断字符，超出部分用“...”代替
     * 中文2 英文1
     */
    public static String truncate(String s, int maxWidth) {
        if (s == null) return " ";
        if (displayWidth(s) <= maxWidth) return s;
        
        StringBuilder sb = new StringBuilder();
        int w = 0;
        for (char c : s.toCharArray()) {
            int cw = isFullWidth(c) ? 2 : 1;
            if (w + cw > maxWidth - 2) break;
            sb.append(c);
            w += cw;            
        }
        sb.append("...");
        return sb.toString();
    }



    //全局输入校验方法
    /*  
    * 从控制台读取一个整数，并限制在指定范围内。
    * 如果输入不是整数或超出范围，会提示并让用户重新输入。
    * @param scanner 全局共享的 Scanner 对象
    * @param prompt  提示信息
    * @param min     允许的最小值
    * @param max     允许的最大值
    * @return 用户输入的合法整数
    */
    public static int readInt(Scanner scanner, String prompt, int min ,int max) {
        int result;
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();//读取一行去掉首尾空格

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
    public static double readDouble(Scanner scanner, String prompt, double min ,double max) {
        double result;
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();//读取一行去掉首尾空格

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

    //归一化处理方法
    public static String normalizeId(String id) {
        return id == null ? "" : id.trim().toUpperCase();
    }

    //===========通用表格渲染===========

    /**
     * 行映射器接口，用于将对象映射为表格行
     * @param <T> 数据行的类型（studentManager、SubjectManager等）
     */
    @FunctionalInterface 
    public interface RowMapperWithIndex<T> {
        String[] map(T item, int index);
    }

    /**
     * 渲染一张等宽表格：表头、分隔线、数据行公用一份列宽定义
     * 
     * @param title     表格标题
     * @param headers   列名    
     * @param widths    每列的显示宽度,中文按2算，长度必须与headers一致
     * @param list      数据行
     * @param mapper    如何从一行数据取出每列的文本
     */
    public static <T> void printTable(String title, String[] headers, int[] widths, List<T> list, RowMapperWithIndex<T> mapper) {

        //1.接口自保：预防非法输入
        if (list ==  null || list.isEmpty()) return;
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
                line.append(padRight(cells[j], widths[j]));
            }
            System.out.println(line);
        }
    }

    
}
