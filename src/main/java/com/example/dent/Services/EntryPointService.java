package com.example.dent.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import py4j.GatewayServer;

@Service
public class EntryPointService {

    @Autowired
    public EntryPointService(){
        GatewayServer gatewayServer = new GatewayServer(new PythonResponse());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }
}