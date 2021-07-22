package wang.yeting.juejin.report.task;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import wang.yeting.juejin.report.api.JueJInApi;
import wang.yeting.juejin.report.constant.FilePathConstant;
import wang.yeting.juejin.report.util.FileUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author : weipeng
 * @since : 2021-07-03 4:16 下午
 */
@Slf4j
@Component
public class UserExploreTask {

    static DateTimeFormatter yyyyMMddHH = DateTimeFormatter.ofPattern("yyyyMMddHH");
    static DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

    public ConcurrentHashSet<String> userIdSet = init();

    private ConcurrentHashSet<String> init() {
        ConcurrentHashSet<String> concurrentHashSet = new ConcurrentHashSet<>();
        List<String> userIdList = FileUtil.readJsonList(FilePathConstant.EXPLORE_USER_PATH, String.class);
        concurrentHashSet.addAll(userIdList);
        return concurrentHashSet;
    }

    public void run() {
        log.info("拉取用户快照");
        String now = LocalDateTime.now().format(yyyyMMddHH);
        try {
            String path = String.format(FilePathConstant.EXPLORE_DARA_PATH, LocalDate.now().format(yyyyMMdd));
            FileUtil.initFile(path);
            FileWriter fw = new FileWriter(path, true);
            PrintWriter pw = new PrintWriter(fw);

            int i = 0;
            for (String userId : userIdSet) {
                JueJInApi.UserData userData = JueJInApi.getUser(userId);
                if (userData == null) {
                    userData = JueJInApi.getUser(userId);
                }
                if (userData != null) {
                    userData.setTime(now);
                    pw.println(JSONUtil.toJsonStr(userData));
                }
                log.info((++i) + " 用户快照：" + userId);
            }
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("拉取用户快照结束：" + now);
    }

    public void addUserId(List<String> user_ids) {
        userIdSet.addAll(user_ids);
        FileUtil.writeJson(FilePathConstant.EXPLORE_USER_PATH, userIdSet);
    }
}
