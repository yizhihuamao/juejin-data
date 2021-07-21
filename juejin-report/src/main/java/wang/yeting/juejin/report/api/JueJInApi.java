package wang.yeting.juejin.report.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import wang.yeting.juejin.report.util.HttpUtil;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author : weipeng
 * @since : 2021-07-03 4:11 下午
 */

public class JueJInApi {

    public static List<Category> getAllTopCategory() {
        String res = HttpUtil.get("https://api.juejin.cn/tag_api/v1/query_category_briefs?show_type=1");
        JSONArray data = JSONUtil.parseObj(res).getJSONArray("data");
        return JSONUtil.toList(data, Category.class);
    }

    public static List<Author> getAllTopAuthor() {
        List<Author> list = new ArrayList<>();
        for (Category category : getAllTopCategory()) {
            try {
                String res = HttpUtil.get("https://api.juejin.cn/user_api/v1/author/recommend?category_id=" + category.getCategory_id() + "&cursor=0&limit=100");
                JSONArray data = JSONUtil.parseObj(res).getJSONArray("data");
                list.addAll(JSONUtil.toList(data, Author.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static UserData getUser(String user_id) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("cursor", "0");
        map1.put("sort_type", 2);
        map1.put("user_id", user_id);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("audit_status", null);
        map2.put("cursor", "0");
        map2.put("limit", 10);
        map2.put("user_id", user_id);
        try {
            List<ArticleData> articleDataList = new ArrayList<>();
            for (Integer cursor = 0; true; cursor += 10) {
                map1.put("cursor", cursor.toString());
                String res1 = HttpUtil.post("https://api.juejin.cn/content_api/v1/article/query_list", JSONUtil.toJsonStr(map1));
                JSONObject jsonObject = JSONUtil.parseObj(res1);
                Integer count = jsonObject.getInt("count");
                if (count == 0) {
                    break;
                }
                JSONArray data1 = jsonObject.getJSONArray("data");
                List<ArticleData> dataList = JSONUtil.toList(data1, ArticleData.class);
                List<ArticleData> data2 = dataList.stream().filter(data -> {
                    String ctime = data.getArticle_info().getCtime();
                    long now = System.currentTimeMillis() / 1000;
                    return Long.parseLong(ctime) > (now - (60 * 60 * 24 * 45));
                }).collect(Collectors.toList());

                articleDataList.addAll(data2);

                if (dataList.size() != data2.size()) {
                    break;
                }
                if (jsonObject.getInt("cursor") >= (count)) {
                    break;
                }
            }
            AtomicReference<AuthorUserInfo> author_user_info = new AtomicReference<>();
            articleDataList = articleDataList.stream().peek(articleData -> {
                author_user_info.set(articleData.getAuthor_user_info());
                articleData.setAuthor_user_info(null);
            }).collect(Collectors.toList());

            List<SelfData> selfDataList = new ArrayList<>();
            for (Integer cursor = 0; true; cursor += 10) {
                map2.put("cursor", cursor.toString());
                String res2 = HttpUtil.post("https://api.juejin.cn/content_api/v1/column/self_center_list", JSONUtil.toJsonStr(map2));
                JSONObject jsonObject = JSONUtil.parseObj(res2);
                Integer count = jsonObject.getInt("count");
                if (count == 0) {
                    break;
                }
                JSONArray data2 = jsonObject.getJSONArray("data");
                selfDataList.addAll(JSONUtil.toList(data2, SelfData.class));
                if (jsonObject.getInt("cursor") >= (count)) {
                    break;
                }
            }

            UserData userData = new UserData();
            userData.setUser_id(user_id);
            userData.setArticle_list(articleDataList);
            userData.setSelf_center_list(selfDataList);
            userData.setAuthor_user_info(author_user_info.get());
            return userData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UserData getInitUser(String user_id) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("cursor", "0");
        map1.put("sort_type", 2);
        map1.put("user_id", user_id);
        try {
            String res1 = HttpUtil.post("https://api.juejin.cn/content_api/v1/article/query_list", JSONUtil.toJsonStr(map1));
            JSONObject jsonObject = JSONUtil.parseObj(res1);
            JSONArray data1 = jsonObject.getJSONArray("data");
            List<ArticleData> articleDataList = JSONUtil.toList(data1, ArticleData.class);
            UserData userData = new UserData();
            userData.setUser_id(user_id);
            userData.setArticle_list(articleDataList);
            return userData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArticleData getArticleDetail(String article_id) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("article_id", article_id);
        try {
            String res1 = HttpUtil.post("https://api.juejin.cn/content_api/v1/article/detail", JSONUtil.toJsonStr(map1));
            JSONObject jsonObject = JSONUtil.parseObj(res1);
            return jsonObject.get("data", ArticleData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Tag> getAllTag() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("cursor", "0");
        map1.put("sort_type", 1);
        map1.put("limit", 1000);
        try {
            String res1 = HttpUtil.post("https://api.juejin.cn/tag_api/v1/query_tag_list", JSONUtil.toJsonStr(map1));
            JSONObject jsonObject = JSONUtil.parseObj(res1);
            JSONArray data1 = jsonObject.getJSONArray("data");
            return JSONUtil.toList(data1, Tag.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<String> getTagArticle(String tag_id) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("cursor", "0");
        map1.put("sort_type", 300);
        map1.put("id_type", 2);
        map1.put("tag_ids", CollUtil.newArrayList(tag_id));
        try {
            String res1 = HttpUtil.post("https://api.juejin.cn/recommend_api/v1/article/recommend_tag_feed", JSONUtil.toJsonStr(map1));
            JSONObject jsonObject = JSONUtil.parseObj(res1);
            JSONArray data1 = jsonObject.getJSONArray("data");
            List<ArticleData> articleDataList = JSONUtil.toList(data1, ArticleData.class);
            return articleDataList.stream().map(articleData -> articleData.getArticle_info().getUser_id()).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static Boolean createArticle(Map<String, String> headers, Map<String, Object> paramMap) {
        /**
         * brief_content: ""
         * category_id: "0"
         * cover_image: ""
         * edit_type: 10
         * html_content: "deprecated"
         * link_url: ""
         * mark_content: "带娃带娃大"
         * tag_ids: []
         * title: ""
         */
        try {
            String res = HttpUtil.post("https://api.juejin.cn/content_api/v1/article_draft/create", headers, JSONUtil.toJsonStr(paramMap));
            return "0".equals(JSONUtil.parseObj(res).getStr("err_no"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Boolean updateArticle(Map<String, String> headers, Map<String, Object> paramMap) {
        try {
            String res = HttpUtil.post("https://api.juejin.cn/content_api/v1/article_draft/update", headers, JSONUtil.toJsonStr(paramMap));
            return "0".equals(JSONUtil.parseObj(res).getStr("err_no"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Boolean publishArticle(Map<String, String> headers, Map<String, Object> paramMap) {
        try {
            String res = HttpUtil.post("https://api.juejin.cn/content_api/v1/article/publish", headers, JSONUtil.toJsonStr(paramMap));
            return "0".equals(JSONUtil.parseObj(res).getStr("err_no"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Data
    public static class Tag {
        private String tag_id;
    }

    @Data
    public static class Author implements Serializable {
        private String user_id;
        private String user_name;
        private String got_digg_count;
        private String got_view_count;
        private String company;
        private String job_title;
        private String level;
        private String description;
        private String author_desc;
        private String time;
    }

    @Data
    public static class Category implements Serializable {

        private String category_id;
        private String category_name;
        private String category_url;

    }

    @Data
    public static class JResult<T> {

        private String cursor;

        private T data;

        private String err_msg;

        private Integer count;

        private Boolean has_more;

        private Integer err_no;

    }

    @Data
    public static class UserData {
        private String user_id;
        private String time;
        private List<ArticleData> article_list;
        private List<SelfData> self_center_list;
        private AuthorUserInfo author_user_info;
    }

    @Data
    public static class ArticleData {

        private String article_id;

        private AuthorUserInfo author_user_info;

        private Category category;

        private ArticleInfo article_info;

        private List<Tags> tags;

    }

    @Data
    public static class University {

        private String university_id;

        private String name;

        private String logo;

    }


    @Data
    public static class UserInteract {

        private Boolean is_collect;

        private Long user_id;

        private String id;

        private Integer omitempty;

        private Boolean is_digg;

    }

    @Data
    public static class UserCategory {

        private Integer show_type;

        private String category_name;

        private String category_url;

        private String category_id;

        private Integer item_type;

        private Integer rank;

        private Integer ctime;

        private Integer mtime;

    }

    @Data
    public static class ArticleInfo {

        private Integer comment_count;

        private String rtime;

        private Double rank_index;

        private Integer audit_status;

        private String title;

        private String mtime;

        private Integer is_gfw;

        private Integer is_english;

        private String content;

        private String article_id;

        private String category_id;

        private Integer visible_level;

        private Integer is_hot;

        private String link_url;

        private String ctime;

        private Integer verify_status;

        private Double user_index;

        private List<String> tag_ids;

        private String brief_content;

        private Integer is_original;

        private String mark_content;

        private String draft_id;

        private Integer collect_count;

        private String user_id;

        private Integer digg_count;

        private Integer original_type;

        private Integer hot_index;

        private String original_author;

        private Integer view_count;

        private Integer status;

    }


    @Data
    public static class Tags {

        private Integer show_navi;

        private String color;

        private String tag_name;

        private String icon;

        private Integer mtime;

        private Integer post_article_count;

        private Integer concern_user_count;

        private String tag_alias;

        private String tag_id;

        private Integer ctime;

        private Integer id_type;

        private Integer id;

        private String back_ground;

    }

    @Data
    public static class Major {

        private String parent_id;

        private String name;

        private String major_id;

    }

    @Data
    public static class AuthorUserInfo {

        private Integer select_event_count;

        private Map<String, String> extra_map;

        private Integer post_shortmsg_count;

        private String user_name;

        private University university;

        private String description;

        private Integer annual_list_type;

        private Integer follower_count;

        private Integer select_online_course_count;

        private Integer digg_article_count;

        private Integer followee_count;

        private Major major;

        private Integer study_point;

        private Integer identity;

        private Integer student_status;

        private String company;

        private Integer power;

        private String job_title;

        private Boolean isfollowed;

        private Integer got_digg_count;

        private Integer level;

        private Integer digg_shortmsg_count;

        private Integer post_article_count;

        private Integer favorable_author;

        private String user_id;

        private Integer got_view_count;

        private Boolean is_select_annual;

        private Integer select_annual_rank;

        private Integer is_logout;

    }


    @Data
    public static class ColumnVersion {

        private String column_id;

        private Integer ctime;

        private Integer audit_status;

        private String version_id;

        private String title;

        private String content;

    }

    @Data
    public static class SelfData {

        private String column_id;

        private ColumnVersion column_version;

        private Author author;

        private Column column;

    }

    @Data
    public static class SelfAuthor {

        private Integer select_event_count;

        private Map<String, String> extra_map;

        private Integer post_shortmsg_count;

        private String user_name;

        private University university;

        private String description;

        private Integer annual_list_type;

        private Integer follower_count;

        private Integer select_online_course_count;

        private Integer digg_article_count;

        private Integer followee_count;

        private Major major;

        private Integer study_point;

        private Integer identity;

        private Integer student_status;

        private String company;

        private Integer power;

        private String job_title;

        private Boolean isfollowed;

        private Integer got_digg_count;

        private Integer level;

        private Integer digg_shortmsg_count;

        private Integer post_article_count;

        private Integer favorable_author;

        private String user_id;

        private Integer got_view_count;

        private Boolean is_select_annual;

        private Integer select_annual_rank;

        private Integer is_logout;

    }

    @Data
    public static class Column {

        private String column_id;

        private String user_id;

        private Integer top_status;

        private Integer follow_cnt;

        private Integer ctime;

        private Integer article_cnt;

        private Integer status;

    }


}
