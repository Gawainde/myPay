package moonlit.chill.ownpay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class OwnPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwnPayApplication.class, args);
        log.info("启动成功");
    }

}
