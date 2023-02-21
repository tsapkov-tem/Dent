package com.example.dent.Repository;

import com.example.dent.Models.Users;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends CrudRepository<Users, String> {
    Users findByUsername(String username);
}
