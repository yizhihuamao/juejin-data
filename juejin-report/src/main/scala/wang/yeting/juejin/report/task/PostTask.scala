package wang.yeting.juejin.report.task

import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.{EnableScheduling, Scheduled}
import org.springframework.stereotype.Component
import wang.yeting.juejin.report.api.JueJInApi
import wang.yeting.juejin.report.bean.{ActivityConfig, ActivityReportResult, ActivityRule}
import wang.yeting.juejin.report.constant.FilePathConstant
import wang.yeting.juejin.report.service.ReportService
import wang.yeting.juejin.report.util.FileUtil

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util
import scala.collection.JavaConverters._

/**
 * @author : weipeng
 * @since : 2021-07-07 2:49 下午 
 *
 */
@Component
class PostTask @Autowired()(reportService: ReportService) {

    private val log: Logger = LoggerFactory.getLogger(classOf[ActivityReportTask])

    private[task] val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @Scheduled(cron = "0 30 7,13,19 * * ?")
    def task(): Unit = {
        column()
        activity()
    }

    def column(): Unit = {
        val activityConfig = FileUtil.readJson(FilePathConstant.ACTIVITY_CONFIG_PATH, classOf[ActivityConfig])
        val report = reportService.top()
        val list = report.getColumnList.asScala.toList

        val builder = new StringBuilder
        for (i <- list.indices) {
            val value = list.apply(i)
            builder.append("|").append(i + 1)
                .append("|").append(s"[${value.getColumn_version.getTitle.replaceAll("\\|", "\\\\|")}](https://juejin.cn/column/${value.getColumn_id})")
                .append("|").append(value.getColumn.getFollow_cnt)
                .append("|").append(s"[${value.getAuthor.getUser_name.replaceAll("\\|", "\\\\|")}](https://juejin.cn/user/${value.getAuthor.getUser_id}/columns)")
                .append("|").append("\n")
        }

        val mark_content =
            s"""---
               |highlight: arduino-light
               |theme: channing-cyan
               |---
               |
               |> 本文已参与好文召集令活动，点击查看：[后端、大前端双赛道投稿，2万元奖池等你挑战！](https://juejin.cn/post/6978685539985653767)
               |
               |> 文章会在 每天7点30、13点30、19点30 定时更新，但是有审核，数据更新有延迟 [无延迟点这里](http://juejin.yeting.wang/)
               |>
               |> 更新时间: ${LocalDateTime.now().format(dateFormatter)}
               |
               |### 前言
               |xdm，大家好，我是摸鱼专家呀，我又带来最新的排行榜啦。
               |
               |初衷：通过排行榜激励每位创作者，创作出许许多多的优质文章，让大家卷起来。
               |
               |### 排名
               |
               ||排名|专栏  |关注数  |作者  |
               || --- | --- | --- | --- |
               |${builder.toString()}
               |
               |""".stripMargin

        val headers = new util.HashMap[String, String]
        headers.put("cookie", activityConfig.getPostCookie)
        val paramMap = new util.HashMap[String, Object]
        paramMap.put("brief_content", "xdm，大家好，我是摸鱼专家呀，我又带来最新的排行榜啦。 初衷：通过排行榜激励每位创作者，创作出许许多多的优质文章，让大家卷起来")
        paramMap.put("category_id", activityConfig.getPostCategoryId)
        paramMap.put("cover_image", "")
        paramMap.put("edit_type", new Integer(10))
        paramMap.put("html_content", "deprecated")
        paramMap.put("id", activityConfig.getColumnPostId)
        paramMap.put("is_english", new Integer(0))
        paramMap.put("is_gfw", new Integer(0))
        paramMap.put("is_original", new Integer(0))
        paramMap.put("link_url", "")
        paramMap.put("mark_content", mark_content)
        paramMap.put("tag_ids", activityConfig.getPostTagIds)
        paramMap.put("title", "掘金专栏排行榜，来看看你上榜了吗？")
        val update = JueJInApi.updateArticle(headers, paramMap)
        if (update)
            log.info("专栏文章更新成功")
        else
            log.info("专栏文章更新失败")

        val paramMap2 = new util.HashMap[String, Object]
        paramMap2.put("draft_id", activityConfig.getColumnPostId)
        paramMap2.put("sync_to_org", new java.lang.Boolean(false))
        paramMap2.put("column_ids", activityConfig.getPostColumnIds)
        val publish = JueJInApi.publishArticle(headers, paramMap2)
        if (publish)
            log.info("专栏文章发布成功")
        else
            log.info("专栏文章发布失败")
    }

    def activity(): Unit = {
        val activityConfig = FileUtil.readJson(FilePathConstant.ACTIVITY_CONFIG_PATH, classOf[ActivityConfig])
        val report: util.Iterator[util.List[ActivityReportResult]] = reportService.activity()
        val activityReportResultMap = report.asScala.flatMap(l => l.asScala).map(activityReportResult => {
            (activityReportResult.getActivityRule.getId, activityReportResult)
        }).toMap

        val activityRuleList = FileUtil.readJsonList(FilePathConstant.ACTIVITY_RULE_PATH, classOf[ActivityRule]).asScala
        activityRuleList.foreach(rule => {
            val activityReportResult = activityReportResultMap.get(rule.getId)
            if (activityReportResult.nonEmpty) {
                val activityReport = activityReportResult.get
                val userActivityReportList = activityReport.getUserActivityReportList.asScala

                val builder = new StringBuilder
                for (i <- userActivityReportList.indices) {
                    val userActivityReport = userActivityReportList.apply(i)
                    builder
                        .append("|").append(i + 1)
                        .append("|").append(s"[${userActivityReport.getUser_name.replaceAll("\\|", "\\\\|")}](https://juejin.cn/user/${userActivityReport.getUser_id})")
                        .append("|").append(userActivityReport.getCount)
                        .append("|").append(userActivityReport.getSum_digg_count)
                        .append("|").append(userActivityReport.getSum_view_count)
                        .append("|").append(userActivityReport.getSum_comment_count)
                        .append("|").append(userActivityReport.getSum_collect_count)
                        .append("|").append("\n")
                }

                val mark_content =
                    s"""---
                       |highlight: arduino-light
                       |theme: channing-cyan
                       |---
                       |
                       |> 本文已参与好文召集令活动，点击查看：[后端、大前端双赛道投稿，2万元奖池等你挑战！](https://juejin.cn/post/6978685539985653767)
                       |
                       |> 文章会在 每天7点30、13点30、19点30 定时更新，但是有审核，数据更新有延迟 [无延迟点这里](http://juejin.yeting.wang/)
                       |>
                       |> 更新时间: ${LocalDateTime.now().format(dateFormatter)}
                       |
                       |### 前言
                       |xdm，大家好，我是摸鱼专家呀，我又带来最新的排行榜啦。
                       |
                       |初衷：通过排行榜激励每位创作者，创作出许许多多的优质文章，让大家卷起来。
                       |
                       |### 排名
                       |
                       ||排名  |作者 |文章数 |获赞数|浏览数|评论数|收藏数|
                       || --- | --- | --- | --- | --- | --- | --- |
                       |${builder.toString()}
                       |
                       |""".stripMargin

                val headers = new util.HashMap[String, String]
                headers.put("cookie", activityConfig.getPostCookie)
                val paramMap = new util.HashMap[String, Object]
                paramMap.put("brief_content", "xdm，大家好，我是摸鱼专家呀，我又带来最新的排行榜啦。 初衷：通过排行榜激励每位创作者，创作出许许多多的优质文章，让大家卷起来")
                paramMap.put("category_id", activityConfig.getPostCategoryId)
                paramMap.put("cover_image", "")
                paramMap.put("edit_type", new Integer(10))
                paramMap.put("html_content", "deprecated")
                paramMap.put("id", rule.getPostId)
                paramMap.put("is_english", new Integer(0))
                paramMap.put("is_gfw", new Integer(0))
                paramMap.put("is_original", new Integer(0))
                paramMap.put("link_url", "")
                paramMap.put("mark_content", mark_content)
                paramMap.put("tag_ids", activityConfig.getPostTagIds)
                paramMap.put("title", s"掘金「 ${rule.getTitle} 」活动排行榜，来看看你排第几名")
                val update = JueJInApi.updateArticle(headers, paramMap)
                if (update)
                    log.info("专栏文章更新成功")
                else
                    log.info("专栏文章更新失败")

                val paramMap2 = new util.HashMap[String, Object]
                paramMap2.put("draft_id", rule.getPostId)
                paramMap2.put("sync_to_org", new java.lang.Boolean(false))
                paramMap2.put("column_ids", activityConfig.getPostColumnIds)
                val publish = JueJInApi.publishArticle(headers, paramMap2)
                if (publish)
                    log.info("专栏文章发布成功")
                else
                    log.info("专栏文章发布失败")
            }
        })
    }


    /**
     * brief_content: "xdm，大家好，我是摸鱼专家呀，我又带来最新的排行榜啦。 初衷：通过排行榜激励每位创作者，创作出许许多多的优质文章，让大家卷起来"
     * category_id: "6809637769959178254"
     * cover_image: ""
     * edit_type: 10
     * html_content: "deprecated"
     * id: "6982070873196855332"
     * is_english: 0
     * is_gfw: 0
     * is_original: 1
     * link_url: ""
     * mark_content: "---\nhighlight: arduino-light\ntheme: channing-cyan\n---\n\n> 本文已参与好文召集令活动，点击查看：[后端、大前端双赛道投稿，2万元奖池等你挑战！](https://juejin.cn/post/6978685539985653767)\n\n> 文章会定时更新，但是有审核，数据更新有延迟 [无延迟点这里](http://juejin.yeting.wang/)\n>\n> 更新时间: xxx\n\n### 前言\nxdm，大家好，我是摸鱼专家呀，我又带来最新的排行榜啦。\n\n初衷：通过排行榜激励每位创作者，创作出许许多多的优质文章，让大家卷起来。\n\n### 排名\n\n\n|  |  |\n| --- | --- |\n|  |  |\n\n"
     * tag_ids: ["6809640408797167623", "6809640407484334093"]
     * title: ""
     */

    /**
     * val paramMap = new util.HashMap[String, Object]
     * paramMap.put("brief_content", "")
     * paramMap.put("category_id", "0")
     * paramMap.put("cover_image", "0")
     * paramMap.put("edit_type", 2)
     * paramMap.put("html_content", "deprecated")
     * paramMap.put("link_url", "")
     * paramMap.put("mark_content", mark_content)
     * paramMap.put("tag_ids", CollUtil.newArrayList("6809640408797167623", "6809640407484334093"))
     * paramMap.put("title", "")
     * JueJInApi.createArticle(headers, paramMap)
     */
}
