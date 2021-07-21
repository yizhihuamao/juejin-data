package wang.yeting.juejin.report.constant;

/**
 * @author : weipeng
 * @since : 2021-07-21 10:10 上午
 */

public interface FilePathConstant {

    String ALL_USER_PATH = "./data/all-user.json";

    String EXPLORE_USER_PATH = "./data/explore-user.json";

    /**
     * 掘金数据快照路径  %s为快照拉取时间
     */
    String EXPLORE_DARA_PATH = "./data/j-%s.json";

    /**
     * 掘金报表备份文件  %s为报表生成时间
     */
    String BAK_REPORT_DARA_PATH = "./data/r-%s.json";

    /**
     * 最新的报表文件
     */
    String REPORT_DARA_PATH = "./data/report.json";

    /**
     * 掘金活动排行报表备份文件  %s为报表生成时间
     */
    String BAK_ACTIVITY_REPORT_DARA_PATH = "./data/activity-%s.json";

    /**
     * 最新的活动排行报表文件
     */
    String ACTIVITY_REPORT_DARA_PATH = "./data/activity-report.json";
    /**
     * 活动 的配置文件
     */
    String ACTIVITY_CONFIG_PATH = "./data/activity-config.json";

    /**
     * 活动 详情+规则 配置文件
     */
    String ACTIVITY_RULE_PATH = "./data/activity-rule.json";

    /**
     * 活动 详情+规则 配置文件
     */
    String USER_VIEW_PATH = "./data/fangwen.json";
}
