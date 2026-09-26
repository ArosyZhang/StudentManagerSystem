package StudentManagerSystem;

/**
 * 所有实体（学生、科目）的公共约定：能报出自己的编号和名称。
 *
 * <p>这个接口不存放任何数据，它的唯一作用是让 {@link Repository} 能用同一个类型参数 {@code T} 同时管理学生和科目。
 * 仓库声明里那句 {@code <T extends Entity>} 就是要求 T 必须具备 {@link #getId()} 和 {@link #getName()}，
 * 否则仓库内部「按编号查找」「按名称查找」根本没法写
 *
 * <p>实现类：{@link Student}、{@link Subject}。
 *
 * @author ArosyZhang
 * @since 1.0
 */
public interface Entity {

    /** 返回实体的唯一编号（学号/科目编号）。约定不会返回 null，也不带首尾空格。 */
    String getId();

    /** 返回实体的显示名称（姓名 / 科目名称）。约定不会返回 null。 */
    String getName();
}
