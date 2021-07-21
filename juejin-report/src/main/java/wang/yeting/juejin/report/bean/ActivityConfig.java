package wang.yeting.juejin.report.bean;

import lombok.Data;

import java.util.List;

/**
 * @author : weipeng
 * @since : 2021-07-07 8:43 上午
 */
@Data
public class ActivityConfig {

    private Long lastRunTime;

    private String postCookie;

    private String postCategoryId;

    private String columnPostId;

    private List<String> postTagIds;

    private List<String> postColumnIds;

}
