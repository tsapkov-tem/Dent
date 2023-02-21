package com.example.dent.Services;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;

@Service
public class JythonService {
    public String runPython() {
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec("x = \"HELLO\"");
            PyObject x = pyInterp.get("x");
            return x.asString();
        }
    }
}
