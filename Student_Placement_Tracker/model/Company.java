package model;

public class Company {

    private String name;
    private String role;

    public Company(String name, String role){
        this.name = name;
        this.role = role;
    }

    public String getName(){ return name; }
    public String getRole(){ return role; }
}