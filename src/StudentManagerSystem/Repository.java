package StudentManagerSystem;

import java.util.ArrayList;
import java.util.List;

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

    public List<T> snapshot() {
        return List.copyOf(items);
    }

    public void clear() {
        items.clear();
    }

    public T findById(String id) {
        for (T t : items) {
            if (t.getId().equalsIgnoreCase(id)) {
                return t;
            }
        }
        return null;
    }

    public String getNameById(String id) {
        T item = findById(id);
        return item != null ? item.getName() : "未知" + label;
    }

    public boolean isId(String id) {
        if (id == null) return false;

        String upper = id.toUpperCase();
        return
        upper.startsWith(idPrefix)
        && upper.length() == idPrefix.length() + idDigits
        && upper.substring(idPrefix.length()).matches("\\d+");
    }

    public String generateId() {
        int maxNum = 0;
        for (T t : items) {
            if (!isId(t.getId())) continue;   // 格式不对的直接跳过
            String numStr = t.getId().substring(idPrefix.length());
            int num = Integer.parseInt(numStr);
            if(num > maxNum) maxNum = num;
        }
        int newNum = maxNum + 1;
        if (newNum > (int) Math.pow(10, idDigits) - 1) {
            throw new IllegalStateException(label + "ID已达到最大值,无法生成新的ID");
        }
        return idPrefix + String.format("%0" + idDigits + "d", newNum);
    }
}