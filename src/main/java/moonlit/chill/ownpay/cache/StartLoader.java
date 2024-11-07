package moonlit.chill.ownpay.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Gawaind
 * @date 2024/11/7 10:55
 */
@Component
public class StartLoader implements CommandLineRunner {

    @Autowired
    private LocalCachesSchedule localCachesSchedule;

    @Override
    public void run(String... args) {
        localCachesSchedule.initData();
    }
}
