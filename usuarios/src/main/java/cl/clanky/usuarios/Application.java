package cl.clanky.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cl.clanky")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
