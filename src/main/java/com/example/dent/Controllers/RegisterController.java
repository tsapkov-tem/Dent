package com.example.dent.Controllers;

import com.example.dent.Models.Role;
import com.example.dent.Models.Users;
import com.example.dent.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

@Controller
public class RegisterController {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegisterController(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model){
        model.addAttribute ("user", new Users ());
        return "register";
    }

    @PostMapping ("/register")
    public String getRegisterSubmit(@RequestParam String username, @RequestParam String password){
        Users user = new Users ();
        user.setUsername (username);
        user.setPassword(passwordEncoder.encode (password));
        user.setRole (Role.USER);
        try {
            usersRepository.save(user);
        }catch (DuplicateKeyException e){
            return "/register";
        }
        return "redirect:/login";
    }

}
