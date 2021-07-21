package wang.yeting.juejin.report.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : weipeng
 * @since : 2021-07-03 4:10 下午
 */
@Component
public class MainTask {

    static ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 5, 60, TimeUnit.SECONDS, new SynchronousQueue<>());

    @Autowired
    UserDataSnapshotTask userDataSnapshotTask;

    @Autowired
    UserExploreTask userExploreTask;

    /**
     * 每天4次 快照数据
     */
    @Scheduled(cron = "0 0 0,6,12,18 * * ?")
    public void userExplore() {
        executor.execute(() -> {
            userExploreTask.run();
        });
    }

    /**
     * 每天4点 作者榜单
     */
    @Scheduled(cron = "0 0 4 1/1 * ?")
    public void userDataSnapshot() {
        executor.execute(() -> {
            userDataSnapshotTask.run();
        });
    }

    /**
     * 每天3点跑一次所以标签
     */
    @Scheduled(cron = "0 0 3 1/1 * ?")
    public void userDataSnapshotBig() {
        executor.execute(() -> {
            userDataSnapshotTask.runBig();
        });
    }

}
