package br.com.toponesystem.thirdsectorapi;

import org.springframework.boot.SpringApplication;

public class TestThirdSectorApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(ThirdSectorApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
