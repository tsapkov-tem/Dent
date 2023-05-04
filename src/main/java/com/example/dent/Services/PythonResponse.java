package com.example.dent.Services;

import org.springframework.stereotype.Service;

@Service
public class PythonResponse {
    public void fromPython(String idModel) {
        System.out.println("PYTHON!!!!!" + idModel);
    }
}
