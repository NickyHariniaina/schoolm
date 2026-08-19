package hei.student.schoolm.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "jwt.secret", () -> "6F6B4E33716B7A2E4D4C2B59334A563866587447624D2A7265324F793F38584E");
    registry.add("jwt.expiration", () -> "3600000");
  }
}
