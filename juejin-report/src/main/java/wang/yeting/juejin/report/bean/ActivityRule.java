package wang.yeting.juejin.report.bean;

import lombok.Data;

/**
 * @author : weipeng
 * @since : 2021-07-07 8:43 上午
 */
@Data
public class ActivityRule {

    private Integer id;
    private String type;
    private String keyword;
    private String desc;
    private String title;
    private String url;
    private String postId;
    private String sortBy;
    private Integer endDate;

}
