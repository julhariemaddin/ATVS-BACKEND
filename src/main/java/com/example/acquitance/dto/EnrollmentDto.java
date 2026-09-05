package com.example.acquitance.dto;

public class EnrollmentDto {
    private String semester;
    private String schoolYear;
    private String programName;
    private String enrollmentId;

    public EnrollmentDto() {}

    public EnrollmentDto(String semester, String schoolYear, String programName, String enrollmentId) {
        this.semester = semester;
        this.schoolYear = schoolYear;
        this.programName = programName;
        this.enrollmentId = enrollmentId;
    }

    // Getters and Setters
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getSchoolYear() { return schoolYear; }
    public void setSchoolYear(String schoolYear) { this.schoolYear = schoolYear; }
    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }
    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }
}
