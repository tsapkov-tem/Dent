package com.example.dent.Controllers;

import com.example.dent.Services.JythonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @Autowired
    private JythonService jythonService;

    @GetMapping("/main")
    public String main(){
        jythonService.runPython();
        return "main";
    }
}
