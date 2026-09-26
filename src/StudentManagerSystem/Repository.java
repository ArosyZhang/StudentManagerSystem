package StudentManagerSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个通用仓库，负责在内存里管理一批 {@link Entity}（增、删、清空、按编号查找、生成新编号）。
 *
 * <p>它只在内存里维护这批实体，不负责读写文件（那是 {@link DataStore} 的事），也不负责与用户交互（那是各 Manager 的事）。
 * <p>类型参数 {@code T} 被约束为 {@link Entity} 的子类型，所以仓库内部可以放心调用
 * {@link Entity#getId()} 和 {@link Entity#getName()} —— 编译器保证它们一定存在。
 *
 * @param <T> 被管理的实体类型，必须是 {@link Entity} 的实现类（{@link Student} 或 {@link Subject}）
 *
 * @author ArosyZhang
 * @since 1.0
 */
public class Repository<T extends Entity> {

    private final String idPrefix;
    private final int idDigits;
    private final String label;
    private final List<T> items = new ArrayList<>();

    public Repository(String idPrefix, int idDigits, String label) {
        this.idPrefix = idPrefix;
        this.idDigits = idDigits;
        this.label = label;
    }

    public void add(T item) {
        items.add(item);
    }

    public void remove(T item) {
        items.remove(item);
    }

    /** 返回所有实体的快照。@return 不可变列表，试图修改会抛 UnsupportedOperationException */
    public List<T> snapshot() {
        return List.copyOf(items);
    }

    /**  防止数据残留*/
    public void clear() {
        items.clear();
    }

    /**
     * 按编号查找实体，忽略大小写。
     *
     * @param id 要查找的编号，大小写不敏感
     * @return 找到的实体；找不到返回 {@code null}，调用方必须判空
     */
    public T findById(String id) {
        for (T t : items) {
            if (t.getId().equalsIgnoreCase(id)) {
                return t;
            }
        }
        return null;
    }

    /**
     * 按编号取实体名称。
     *
     * @param id 要查找的编号
     * @return 找到时返回实体名称；找不到返回 {@code "未知" + label}（如「未知科目」）。
     *         注意<b>不会返回 {@code null}</b>，调用方可直接使用
     */
    public String getNameById(String id) {
        T item = findById(id);
        return item != null ? item.getName() : "未知" + label;
    }

    /**
     * 判断字符串是否符合本仓库的编号格式（前缀 + 固定位数数字，如 {@code STU001}）。
     *
     * @param id 待判断的字符串，允许为 {@code null}
     * @return 格式合法返回 {@code true}；{@code null} 或格式不符返回 {@code false}
     */
    public boolean isId(String id) {
        if (id == null) return false;

        String upper = id.toUpperCase();
        return upper.startsWith(idPrefix)
            && upper.length() == idPrefix.length() + idDigits
            && upper.substring(idPrefix.length()).matches("\\d+");
    }

    /**
     * 生成下一个可用编号。
     *
     * @return 新编号，形如 {@code STU004}
     * @throws IllegalStateException 编号数字部分已达上限（{@code 10^idDigits - 1}）时抛出
     */
    public String generateId() {
        int maxNum = 0;
        for (T t : items) {
            if (!isId(t.getId())) continue;   // 格式不对的直接跳过
            String numStr = t.getId().substring(idPrefix.length());
            int num = Integer.parseInt(numStr);
            if (num > maxNum) maxNum = num;
        }
        int newNum = maxNum + 1;
        if (newNum > (int) Math.pow(10, idDigits) - 1) {
            throw new IllegalStateException(label + "ID已达到最大值，无法生成新的ID");
        }
        return idPrefix + String.format("%0" + idDigits + "d", newNum);
    }
}
