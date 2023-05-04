package com.example.dent.Controllers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Controller
public class FileUploadController {

    private final String[] names = {"neighbors", "fissure"};

    @GetMapping("/upload")
    public String provideUploadInfo() {
        return "Вы можете загружать файл с использованием того же URL.";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("files") MultipartFile[] files){
        int count = 0;
        String id = String.valueOf(UUID.randomUUID());
        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    byte[] bytes = file.getBytes();
                    BufferedOutputStream stream =
                            new BufferedOutputStream(new FileOutputStream(new File("files/" + id + names[count] + ".jpg")));
                    stream.write(bytes);
                    stream.close();
                    count++;
                } else {
                    return "Вам не удалось загрузить " + names[count] + " потому что файл пустой.";
                }
            }
            return "redirect:/result/" + id;
        } catch (Exception e) {
            return "Вам не удалось загрузить " + names[count] + " => " + e.getMessage();
        }
    }

    @GetMapping(value="/uploadDev")
    public @ResponseBody String provideUploadDevInfo() {
        return "Вы можете загружать файл с использованием того же URL.";
    }

    @PostMapping(value="/uploadDev")
    public @ResponseBody String handleFileUploadDev(@RequestParam("files") MultipartFile[] files){
        String id = String.valueOf(UUID.randomUUID());
        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    byte[] bytes = file.getBytes();
                    BufferedOutputStream stream =
                            new BufferedOutputStream(new FileOutputStream(new File("gan_models/" + id + names[0] + ".pkl")));
                    stream.write(bytes);
                    stream.close();
                } else {
                    return "Вам не удалось загрузить " + names[0] + " потому что файл пустой.";
                }
            }
            return ("redirect: /files/{file_name:mesh" + id + ".obj}");
        } catch (Exception e) {
            return "Вам не удалось загрузить " + names[0] + " => " + e.getMessage();
        }
    }



    @PreAuthorize("hasAnyAuthority('dev', 'user')")
    @GetMapping(value = "/files/{file_name:.+}")
    public void getFile(@PathVariable("file_name") String fileName, HttpServletResponse response) {
        //Авторизованные пользователи смогут скачать файл
        Path file = Paths.get("src/main/java/com/example/dent/MeshData", fileName);
        if (Files.exists(file)){
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            response.setContentType("application/vnd.mesh");
            try {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
                System.out.println("Down");
            } catch (IOException e) {
                throw new RuntimeException("IOError writing file to output stream");
            }
        }
    }
}
