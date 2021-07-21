package wang.yeting.juejin.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author weipeng
 */
@EnableScheduling
@SpringBootApplication
public class JueJinReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(JueJinReportApplication.class, args);
    }

}
