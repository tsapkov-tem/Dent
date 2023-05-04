package com.example.dent.Controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MainController {

    @GetMapping("/login")
    public String getLoginPage(){
        return "login";
    }

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

    @GetMapping("result/{file_name}")
    @PreAuthorize("hasAnyAuthority('dev', 'user')")
    public String result(Model model, @PathVariable("file_name") String fileName){
        String path = "/fissureResult/" + fileName +
                ".jpg";
        model.addAttribute("file", path);
        return "result";
    }

}
