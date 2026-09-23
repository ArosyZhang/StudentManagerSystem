package StudentManagerSystem;

public class Student {
    private final String stuId;
    private final String stuName;
    private final int age;

    public Student(String id, String name, int age){
        this.stuId = id;
        this.stuName = name;
        this.age = age;
    }

    @Override
    public String toString(){
        return "学号: " + stuId + " 姓名: " + stuName + " 年龄: " + age;    
    }

    public String getStuId() { 
        return stuId; 
    }
    public String getStuName() {
        return stuName;
    }
    public int getStuAge() {
        return age;
    }
}
