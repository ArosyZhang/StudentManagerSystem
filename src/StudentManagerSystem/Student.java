package StudentManagerSystem;

/**
 * 学生实体：一条学生记录，由学号、姓名、年龄三个不可变字段组成。
 *
 * <p>它只是数据的容器：不负责增删改查（那是 {@link StudentManager} 和 {@link Repository} 的事），也不负责读写文件。
 *
 * @author ArosyZhang
 * @since 1.0
 */
public class Student implements Entity {
    private final String stuId;
    private final String stuName;
    private final int age;

    /**
     *构造一个学生。
     *
     * <p><b>这里不做任何校验</b>：传 null 或负数年龄都会照单全收。
     * 参数的合法性由调用方 {@link StudentManager} 负责（它检查空值并限制年龄区间）。
     */
    public Student(String id, String name, int age) {
        this.stuId = id;
        this.stuName = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "学号: " + stuId + " 姓名: " + stuName + " 年龄: " + age;
    }

    public String getId() {
        return stuId;
    }

    public String getName() {
        return stuName;
    }

    /**
     * 返回学生年龄
     *
     * <p>年龄不是 {@link Entity} 约定的一部分（科目就没有年龄），所以只能在这里说明。
     */
    public int getStuAge() {
        return age;
    }
}
