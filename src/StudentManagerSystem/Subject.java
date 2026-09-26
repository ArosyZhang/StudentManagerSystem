package StudentManagerSystem;

/**
 * 科目实体：一条科目记录，由科目编号和科目名称两个不可变字段组成。
 *
 * <p>只是数据的容器，不负责增删改查，也不负责读写文件。
 *
 * @author ArosyZhang
 * @since 1.0
 */
public class Subject implements Entity {
    private final String subId;
    private final String subName;

    /**
     *构造一个科目。
     *
     * <p><b>这里不做任何校验</b>
     * 参数的合法性由调用方 {@link SubjectManager} 负责。
     */
    public Subject(String subId, String subName) {
        this.subId = subId;
        this.subName = subName;
    }

    @Override
    public String toString() {
        return "科目编号: " + subId + " 科目名称: " + subName;
    }

    public String getId() {
        return subId;
    }

    public String getName(){
        return subName;
    }
}
