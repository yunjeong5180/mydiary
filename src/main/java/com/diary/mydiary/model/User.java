package com.diary.mydiary.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    // 👇 추가된 필드들
    private String name;

    private LocalDate birth;

    private String gender;

    private String email;

    private String phone;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
