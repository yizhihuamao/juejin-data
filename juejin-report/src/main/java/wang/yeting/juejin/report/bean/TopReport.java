package wang.yeting.juejin.report.bean;

import lombok.Data;
import wang.yeting.juejin.report.api.JueJInApi;

import java.util.Collection;

/**
 * @author : weipeng
 * @since : 2021-07-06 1:04 下午
 */
@Data
public class TopReport {

    Collection<JueJInApi.SelfData> columnList;

    Collection<Digg> diggList;

    Collection<View> viewList;

    Collection<Level> levelList;

    Collection<Article> articleList;

    @Data
    public static class Article {

        private JueJInApi.ArticleData articleData;

        private String user_name;

        private String user_id;

    }

    @Data
    public static class Digg {

        private Integer day_got_digg_count;

        private String user_name;

        private String user_id;

    }

    @Data
    public static class View {

        private Integer day_got_view_count;

        private String user_name;

        private String user_id;

    }

    @Data
    public static class Level {

        private Integer headLevel;
        private Integer lastLevel;

        private String user_name;

        private String user_id;

    }

}
