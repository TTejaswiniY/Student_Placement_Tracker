package model;

public class Student {

    private String name;
    private String roll;
    private String branch;
    private String company;
    private boolean placed = false;

    public Student(String name, String roll, String branch) {
        this.name = name;
        this.roll = roll;
        this.branch = branch;
    }

    public void place(String company){
        this.company = company;
        this.placed = true;
    }

    public String getName(){ return name; }
    public String getRoll(){ return roll; }
    public String getBranch(){ return branch; }
    public boolean isPlaced(){ return placed; }
    public String getCompany(){ return company; }
}