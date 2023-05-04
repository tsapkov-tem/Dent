package com.example.dent.Controllers;

import com.example.dent.Models.Role;
import com.example.dent.Models.Users;
import com.example.dent.Services.UsersService;
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

    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegisterController(UsersService usersService, PasswordEncoder passwordEncoder) {
        this.usersService = usersService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/about")
    public String getRegisterPage(Model model){
        model.addAttribute ("user", new Users ());
        return "about";
    }

    @PostMapping ("/about")
    public String getRegisterSubmit(@RequestParam String username, @RequestParam String password){
        Users user = new Users ();
        user.setUsername (username);
        user.setPassword(passwordEncoder.encode (password));
        user.setRole (Role.USER);
        try {
            usersService.save(user);
        }catch (DuplicateKeyException e){
            return "/about";
        }
        return "redirect:/logout";
    }

}
