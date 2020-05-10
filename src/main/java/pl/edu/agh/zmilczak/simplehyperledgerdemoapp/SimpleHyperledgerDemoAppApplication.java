package pl.edu.agh.zmilczak.simplehyperledgerdemoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimpleHyperledgerDemoAppApplication {

    public static void main(String[] args) {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        SpringApplication.run(SimpleHyperledgerDemoAppApplication.class, args);
    }

}
