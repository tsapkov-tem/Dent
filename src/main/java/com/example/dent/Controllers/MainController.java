package com.example.dent.Controllers;

import com.example.dent.Services.JythonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @Autowired
    private JythonService jythonService;

    @GetMapping("/platform")
    public String main(){
        return "main";
    }


}
