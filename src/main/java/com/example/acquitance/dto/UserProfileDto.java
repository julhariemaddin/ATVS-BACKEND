package com.example.acquitance.dto;

public class UserProfileDto {
    private String name;
    private String studentId;
    private String program;
    private String year;
    private boolean isAdmin;

    public UserProfileDto(String name, String studentId, String program, String year, boolean isAdmin) {
        this.name = name;
        this.studentId = studentId;
        this.program = program;
        this.year = year;
        this.isAdmin = isAdmin;
    }

    // Getters
    public String getName() { return name; }
    public String getStudentId() { return studentId; }
    public String getProgram() { return program; }
    public String getYear() { return year; }
    public boolean isAdmin() { return isAdmin; }
}
