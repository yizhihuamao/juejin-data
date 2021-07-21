package wang.yeting.juejin.report.task;

import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.yeting.juejin.report.api.JueJInApi;
import wang.yeting.juejin.report.constant.FilePathConstant;
import wang.yeting.juejin.report.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : weipeng
 * @since : 2021-07-03 4:15 下午
 */
@Slf4j
@Component
public class UserDetailTask {

    public ConcurrentHashSet<String> userIdSet = init();
    @Autowired
    UserExploreTask userExploreTask;

    private ConcurrentHashSet<String> init() {
        ConcurrentHashSet<String> concurrentHashSet = new ConcurrentHashSet<>();
        List<String> userIdList = FileUtil.readJsonList(FilePathConstant.ALL_USER_PATH, String.class);
        concurrentHashSet.addAll(userIdList);
        return concurrentHashSet;
    }

    public void addUserId(List<String> user_ids) {
        List<String> temp = new ArrayList<>();
        for (String user_id : user_ids) {
            log.info("发现用户：{}", user_id);
            //没在所以用户里的 或者 在所有用户里的没在快照用户里的，都去看看满足进快照用户吗
            if (!userIdSet.contains(user_id) || (userIdSet.contains(user_id) && !userExploreTask.userIdSet.contains(user_id))) {
                log.info("添加用户：{}", user_id);
                userIdSet.add(user_id);
                JueJInApi.UserData userData = JueJInApi.getInitUser(user_id);
                if (userData != null && userData.getArticle_list().size() > 0) {
                    String ctime = userData.getArticle_list().get(0).getArticle_info().getCtime();
                    long now = System.currentTimeMillis() / 1000;
//                    发布时间小于45天，说明活跃
                    if (Long.parseLong(ctime) > (now - (60 * 60 * 24 * 45))) {
                        temp.add(user_id);
                    }
                }
            }
        }
        if (!temp.isEmpty()) {
            userExploreTask.addUserId(temp);
            FileUtil.writeJson(FilePathConstant.ALL_USER_PATH, userIdSet);
        }
    }
}
