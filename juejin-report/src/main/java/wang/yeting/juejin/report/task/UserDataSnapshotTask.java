package wang.yeting.juejin.report.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.yeting.juejin.report.api.JueJInApi;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : weipeng
 * @since : 2021-07-03 4:37 下午
 */
@Slf4j
@Component
public class UserDataSnapshotTask {

    @Autowired
    UserDetailTask userDetailTask;

    public void run() {
        log.info("发现用户开始");
        List<JueJInApi.Author> allTopAuthor = JueJInApi.getAllTopAuthor();

        List<String> list = allTopAuthor.stream().map(JueJInApi.Author::getUser_id).collect(Collectors.toList());
        userDetailTask.addUserId(list);

        log.info("发现用户结束");
    }

    public void runBig() {
        log.info("发现用户开始");

        List<JueJInApi.Tag> allTag = JueJInApi.getAllTag();
        for (JueJInApi.Tag tag : allTag) {
            List<String> list = JueJInApi.getTagArticle(tag.getTag_id());
            userDetailTask.addUserId(list);
        }

        log.info("发现用户结束");
    }


}
