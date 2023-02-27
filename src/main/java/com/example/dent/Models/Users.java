package com.example.dent.Models;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class Users {
    private String idUser;
    private String username;
    private String password;
    private Role role;
}
