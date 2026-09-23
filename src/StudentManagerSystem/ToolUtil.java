package StudentManagerSystem;

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
    //归一化处理方法
    public static String normalizeId(String id) {
        return id == null ? "" : id.trim().toUpperCase();
    }


    //统一退出逻辑优化
    /*
    boolean running = true;
    while (running) {
        int choice = ToolUtil.readInt(scanner, "请选择：", 1, 5);

        switch (choice) {
            case 1 -> 
            case 5 -> {
            System.out.println("再见！");
            running = false;   // 结束循环
            }
        }
    }
    // 循环外统一清理
    scanner.close();
    */
}
