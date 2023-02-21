package com.example.dent.Services;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;

@Service
public class JythonService {
    public void runPython() {
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.execfile("GAN.py");
        }
    }
}
