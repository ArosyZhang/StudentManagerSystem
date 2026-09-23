package StudentManagerSystem;

public class Subject {
    private final String subId;
    private final String subName;

    public Subject(String subId, String subName){
        this.subId = subId;
        this.subName = subName;
    }

    @Override
    public String toString(){
        return "科目编号: " + subId + " 科目名称: " + subName;    
    }

    public String getSubId() { 
        return subId; 
    }

    public String getSubName(){
        return subName;
    }

}
