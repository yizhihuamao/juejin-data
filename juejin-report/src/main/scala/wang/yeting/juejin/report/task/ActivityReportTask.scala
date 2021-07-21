package wang.yeting.juejin.report.task

import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.{EnableScheduling, Scheduled}
import org.springframework.stereotype.Component
import wang.yeting.juejin.report.api.JueJInApi
import wang.yeting.juejin.report.api.JueJInApi.ArticleData
import wang.yeting.juejin.report.bean.{ActivityConfig, ActivityReport, ActivityRule}
import wang.yeting.juejin.report.constant.FilePathConstant
import wang.yeting.juejin.report.service.ReportService
import wang.yeting.juejin.report.util.FileUtil

import java.io.IOException
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import java.util
import java.util.concurrent.{SynchronousQueue, ThreadPoolExecutor, TimeUnit}
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * @author : weipeng
 * @since : 2021-07-06 12:27 下午 
 *
 */
@Component
class ActivityReportTask @Autowired()(reportService: ReportService) {

    private val log: Logger = LoggerFactory.getLogger(classOf[ActivityReportTask])

    private[task] val yyyyMMddHH = DateTimeFormatter.ofPattern("yyyyMMddHH")
    private[task] val yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd")

    private[task] val executor = new ThreadPoolExecutor(1, 5, 60, TimeUnit.SECONDS, new SynchronousQueue[Runnable])


    /**
     * 每天4次 计算数据
     */
    @Scheduled(cron = "0 0 7,13,19 * * ?")
    def task(): Unit = {
        executor.execute(() => {
            run()
        })
    }

    def run(): Unit = {
        log.info("计算活动文章")
        val now = LocalDateTime.now.format(yyyyMMddHH)
        val runTime = System.currentTimeMillis() / 1000
        val yyyyMMddStr = LocalDate.now.format(yyyyMMdd)
        val yyyyMMddInt = Integer.parseInt(yyyyMMddStr)
        try {
            val path = FilePathConstant.EXPLORE_DARA_PATH.format(yyyyMMddStr)
            val outPath = FilePathConstant.BAK_ACTIVITY_REPORT_DARA_PATH.format(now.format(yyyyMMdd))
            val outNowPath = FilePathConstant.ACTIVITY_REPORT_DARA_PATH
            val rulePath = FilePathConstant.ACTIVITY_RULE_PATH
            val configPath = FilePathConstant.ACTIVITY_CONFIG_PATH

            val userDataList = FileUtil.readLineJsonList(path, classOf[JueJInApi.UserData]).asScala
            val activityRuleList = FileUtil.readJsonList(rulePath, classOf[ActivityRule]).asScala
            val activityConfig = FileUtil.readJson(configPath, classOf[ActivityConfig])

            val lastRunTime = activityConfig.getLastRunTime

            log.info(s"数据读取完成，长度：${userDataList.size}")

            log.info("开始计算 活动文章")
            /**
             * 专栏
             * 1.用户分组
             * 2.保留一天最新的一个快照
             * 3.关注数排序
             */
            val resMap = mutable.Map[Int, ListBuffer[(ActivityRule, ArticleData)]]()

            userDataList
                .groupBy(_.getUser_id)
                .values
                .map(userData => (userData.maxBy(_.getTime.toInt)))
                .foreach(userData => {
                    userData.getArticle_list.asScala
                        .foreach(articleData => {
                            val ctime = articleData.getArticle_info.getCtime.toLong
                            //首次 或者 最后一次运行-12小时
                            if (lastRunTime == 0 || ctime > (lastRunTime - 60 * 60 * 12)) {
                                log.info(s"拉取文章详情: ${articleData.getArticle_id}")
                                val detail = JueJInApi.getArticleDetail(articleData.getArticle_id)
                                if (detail != null) {
                                    activityRuleList.foreach(activityRule => {
                                        def isActivity(content: String) {
                                            if (content.contains(activityRule.getKeyword)) {
                                                //匹配到活动文章
                                                log.info(s"匹配到活动文章: ${activityRule.getKeyword}")
                                                val listBuffer = resMap.getOrElse(activityRule.getId.toInt, ListBuffer[(ActivityRule, ArticleData)]())
                                                articleData.setAuthor_user_info(detail.getAuthor_user_info)
                                                listBuffer.append((activityRule, articleData))
                                                resMap.put(activityRule.getId.toInt, listBuffer)
                                            }
                                        }

                                        if (activityRule.getEndDate <= yyyyMMddInt) {
                                            if (activityRule.getType == "post") {
                                                isActivity(detail.getArticle_info.getMark_content)
                                            } else if (activityRule.getType == "title") {
                                                isActivity(detail.getArticle_info.getTitle)
                                            }
                                        }
                                    })
                                }
                            }
                        })
                })

            val userDataMap: Map[String, JueJInApi.UserData] = userDataList
                .groupBy(_.getUser_id)
                .values
                .map(userData => (userData.maxBy(_.getTime.toInt)))
                .map(userData => (userData.getUser_id, userData))
                .toMap


            var fileActivityReportList: mutable.Buffer[ActivityReport] = FileUtil.readJsonList(outNowPath, classOf[ActivityReport]).asScala
            log.info("初始化 活动 列表")
            val ids = fileActivityReportList.map(_.getId)
            activityRuleList.foreach(rule => {
                val id = rule.getId
                if (!ids.contains(id)) {
                    val report = new ActivityReport
                    report.setId(id)
                    report.setUserActivityReportMap(new util.HashMap[String, ActivityReport.UserActivityReport]())
                    fileActivityReportList = fileActivityReportList.+:(report)
                }
                //结束7天的  直接删除
                val end7Date = LocalDate.parse(rule.getEndDate.toString, yyyyMMdd).plusDays(7).format(yyyyMMdd).toInt
                if (yyyyMMddInt > end7Date) {
                    fileActivityReportList = fileActivityReportList.filter(r => r.getId != id)
                }
            })

            val ruleMap: Map[Integer, ActivityRule] = activityRuleList.map(rule => (rule.getId, rule)).toMap

            log.info("转换数据")
            val activityReportList = fileActivityReportList
                .map(activityReport => {
                    val id = activityReport.getId
                    val ruleOption: Option[ActivityRule] = ruleMap.get(id)
                    val rule = ruleOption.get
                    //活动过期后不更新数据
                    if (rule == null || rule.getEndDate <= yyyyMMddInt) {
                        null
                    } else {
                        val activityReportMap = activityReport.getUserActivityReportMap
                        val articleIdSet = activityReportMap.asScala.flatMap(a => a._2.getArticleIdSet.asScala).toSet
                        val option = resMap.get(id)
                        if (option.nonEmpty) {
                            option.get.foreach(t => {
                                val value = t._2
                                val article_id = value.getArticle_id
                                if (!articleIdSet.contains(article_id)) {
                                    val user_id = value.getAuthor_user_info.getUser_id
                                    val user_name = value.getAuthor_user_info.getUser_name
                                    var report = activityReportMap.get(user_id)
                                    if (report == null) {
                                        report = new ActivityReport.UserActivityReport()
                                        report.setArticleIdSet(new util.HashSet[String]())
                                        report.setUser_id(user_id)
                                        report.setUser_name(user_name)
                                        report.setCount(0)
                                        report.setSum_digg_count(0)
                                        report.setSum_view_count(0)
                                        report.setSum_collect_count(0)
                                        report.setSum_comment_count(0)
                                    }
                                    val set = report.getArticleIdSet
                                    set.add(article_id)
                                    report.setArticleIdSet(set)
                                    activityReportMap.put(user_id, report)
                                }
                            })

                        }
                        activityReport.setUserActivityReportMap(activityReportMap)
                        activityReport
                    }
                })
                .filter(a => a != null)
                .map(activityReport => {
                    val activityReportMap = activityReport.getUserActivityReportMap.asScala.map(t => {
                        val user_id = t._1
                        val userActivityReport = t._2
                        val set = userActivityReport.getArticleIdSet
                        val maybeUserData = userDataMap.get(user_id)
                        if (maybeUserData.nonEmpty) {
                            val userData = maybeUserData.get
                            val datas = userData.getArticle_list.asScala.filter(article => set.contains(article.getArticle_id))
                            userActivityReport.setCount(datas.size)
                            userActivityReport.setSum_view_count(datas.map(_.getArticle_info.getView_count.toInt).sum)
                            userActivityReport.setSum_digg_count(datas.map(_.getArticle_info.getDigg_count.toInt).sum)
                            userActivityReport.setSum_collect_count(datas.map(_.getArticle_info.getCollect_count.toInt).sum)
                            userActivityReport.setSum_comment_count(datas.map(_.getArticle_info.getComment_count.toInt).sum)
                        }
                        (user_id, userActivityReport)
                    }).toMap.asJava
                    activityReport.setUserActivityReportMap(activityReportMap)
                    activityReport
                })
                .asJava

            log.info("保存数据")
            activityConfig.setLastRunTime(runTime)
            FileUtil.writeJson(outPath, activityReportList)
            FileUtil.writeJson(outNowPath, activityReportList)
            FileUtil.writeJson(configPath, activityConfig)
            reportService.updateActivityReport(activityReportList, activityRuleList)

        } catch {
            case e: IOException =>
                e.printStackTrace()
        }
        log.info("计算活动文章结束：" + now)
    }

}
