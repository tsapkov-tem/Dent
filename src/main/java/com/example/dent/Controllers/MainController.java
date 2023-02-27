package com.example.dent.Controllers;

import com.example.dent.Services.JythonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @Autowired
    private JythonService jythonService;

    @GetMapping("/platform")
    @PreAuthorize("hasAnyAuthority('dev', 'user')")
    public String platform(){
        return "platform";
    }

    @GetMapping("/developer")
    @PreAuthorize("hasAuthority('dev')")
    public String developer(){
        return "developer";
    }


}
