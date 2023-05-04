package com.example.dent.Services;

import com.example.dent.Models.Users;
import com.example.dent.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class UsersService {
    private final UsersRepository usersRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }


    public Users findByUsername(String username) {
        return usersRepository.findByUsername(username);
    }

    public void save(Users user) {
        usersRepository.save(user);
    }
}
