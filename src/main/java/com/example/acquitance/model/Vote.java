package com.example.acquitance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "group_id"})
})
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    @JsonIgnore
    private Theme theme;

    public Vote() {}

    public Vote(String userId, String userName, Long groupId, Theme theme) {
        this.userId = userId;
        this.userName = userName;
        this.groupId = groupId;
        this.theme = theme;
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Long getGroupId() { return groupId; }
    public Theme getTheme() { return theme; }
}
