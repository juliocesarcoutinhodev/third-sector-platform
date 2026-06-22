package br.com.toponesystem.thirdsector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ThirdSectorApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThirdSectorApiApplication.class, args);
    }

}
