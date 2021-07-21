package wang.yeting.juejin.report.bean;

import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * @author : weipeng
 * @since : 2021-07-07 8:43 上午
 */
@Data
public class ActivityReport {

    private Integer id;
    private Map<String, UserActivityReport> userActivityReportMap;

    @Data
    public static class UserActivityReport {

        private Set<String> articleIdSet;

        private String user_name;

        private String user_id;

        private Integer count;

        private Integer sum_digg_count;

        private Integer sum_view_count;

        private Integer sum_comment_count;

        private Integer sum_collect_count;
    }
}
