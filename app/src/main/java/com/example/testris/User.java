package com.example.testris;

public class User {
    private String name, address, score;
    public User(String name, String email, String score){
        this.name = name;
        this.address = email;
        this.score = score;
    }
    public String getName(){ return this.name; }
    public String getAddress() { return this.address; }
    public String getScore() {return this.score;}
}
