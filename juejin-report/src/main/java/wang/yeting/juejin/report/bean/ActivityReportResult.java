package wang.yeting.juejin.report.bean;

import lombok.Data;

import java.util.List;

/**
 * @author : weipeng
 * @since : 2021-07-07 8:43 上午
 */
@Data
public class ActivityReportResult {

    private ActivityRule activityRule;
    private List<ActivityReport.UserActivityReport> userActivityReportList;
}
