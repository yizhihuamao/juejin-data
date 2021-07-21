package wang.yeting.juejin.report.task

import cn.hutool.json.JSONUtil
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.{EnableScheduling, Scheduled}
import org.springframework.stereotype.Component
import wang.yeting.juejin.report.api.JueJInApi
import wang.yeting.juejin.report.bean.TopReport
import wang.yeting.juejin.report.bean.TopReport.{Article, Digg, Level, View}
import wang.yeting.juejin.report.constant.FilePathConstant
import wang.yeting.juejin.report.service.ReportService
import wang.yeting.juejin.report.util.FileUtil

import java.io.IOException
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import java.util.concurrent.{SynchronousQueue, ThreadPoolExecutor, TimeUnit}
import scala.collection.JavaConverters._

/**
 * @author : weipeng
 * @since : 2021-07-06 12:27 下午 
 *
 */
@Component
class TopReportTask @Autowired()(reportService: ReportService) {

    private val log: Logger = LoggerFactory.getLogger(classOf[ActivityReportTask])

    private[task] val yyyyMMddHH = DateTimeFormatter.ofPattern("yyyyMMddHH")
    private[task] val yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd")

    private[task] val executor = new ThreadPoolExecutor(1, 5, 60, TimeUnit.SECONDS, new SynchronousQueue[Runnable])


    /**
     * 每天4次 计算数据
     */
    @Scheduled(cron = "0 50 0,6,12,18 * * ?")
    def task(): Unit = {
        executor.execute(() => {
            run()
        })
    }

    def run(): Unit = {
        log.info("计算Top榜单")
        val now = LocalDateTime.now.format(yyyyMMddHH)
        try {
            val path = FilePathConstant.EXPLORE_DARA_PATH.format(LocalDate.now.format(yyyyMMdd));
            val outPath = FilePathConstant.BAK_REPORT_DARA_PATH.format(now)
            val outNowPath = FilePathConstant.REPORT_DARA_PATH

            val userDataList = FileUtil.readLineJsonList(path, classOf[JueJInApi.UserData]).asScala

            log.info(s"数据读取完成，长度：${userDataList.size}")

            log.info("开始计算 专栏")
            /**
             * 专栏
             * 1.过滤，找到最新的数据
             * 2.拿到所以专栏
             * 3.关注数排序
             */
            val columnList: List[JueJInApi.SelfData] = userDataList
                .groupBy(_.getUser_id)
                .map(_._2.maxBy(_.getTime.toInt))
                .flatMap(_.getSelf_center_list.asScala)
                .toList
                .sortBy(_.getColumn.getFollow_cnt.toInt)(Ordering.Int.reverse)

            log.info("开始计算 赞 浏览 升级")
            /**
             * 赞 浏览 升级
             */
            val diggAndViewAndLevelList = userDataList
                .groupBy(_.getUser_id)
                .values
                .map(userExploreList => {
                    try {
                        //赞 浏览
                        val authorList = userExploreList.map(userExplore => (userExplore.getTime, userExplore.getAuthor_user_info))
                            .sortBy(_._1.toInt).map(_._2)
                        val day_got_digg_count = authorList.last.getGot_digg_count.toInt - authorList.head.getGot_digg_count.toInt
                        val day_got_view_count = authorList.last.getGot_view_count.toInt - authorList.head.getGot_view_count.toInt

                        //升级
                        val authors = authorList.sortBy(_.getLevel)
                        var level = (false, (0, 0))
                        val headLevel = authors.head.getLevel.toInt
                        val lastLevel = authors.last.getLevel.toInt
                        if ((lastLevel - headLevel) != 0) {
                            level = (true, (headLevel, lastLevel))
                        }

                        (authorList.last, day_got_digg_count, day_got_view_count, level)
                    } catch {
                        case e: Exception => {
                            log.info("计算报错：" + JSONUtil.toJsonStr(userExploreList))
                            null
                        }
                    }
                }).filter(_ != null)

            val diggList = diggAndViewAndLevelList.map(t => {
                val digg = new Digg
                digg.setDay_got_digg_count(t._2)
                digg.setUser_id(t._1.getUser_id)
                digg.setUser_name(t._1.getUser_name)
                digg
            }).toList.sortBy(_.getDay_got_digg_count.toInt)(Ordering.Int.reverse)

            val viewList = diggAndViewAndLevelList.map(t => {
                val view = new View
                view.setDay_got_view_count(t._3)
                view.setUser_id(t._1.getUser_id)
                view.setUser_name(t._1.getUser_name)
                view
            }).toList.sortBy(_.getDay_got_view_count.toInt)(Ordering.Int.reverse)

            val levelList = diggAndViewAndLevelList.filter(_._4._1).map(t => {
                val level = new Level
                val value = t._4._2
                level.setHeadLevel(value._1)
                level.setLastLevel(value._2)
                level.setUser_id(t._1.getUser_id)
                level.setUser_name(t._1.getUser_name)
                level
            })

            /**
             * 文章排行榜
             */
            val articleList = userDataList
                .groupBy(_.getUser_id)
                .values
                .flatMap(userExploreList => {
                    try {
                        val userData = userExploreList.maxBy(_.getTime.toInt)
                        val user_name = userData.getAuthor_user_info.getUser_name
                        val user_id = userData.getAuthor_user_info.getUser_id
                        val datas = userData.getArticle_list.asScala.filter(a => {
                            val ctime = a.getArticle_info.getCtime
                            val now = System.currentTimeMillis / 1000
                            ctime.toLong > (now - (60 * 60 * 24 * 3))
                        }).map(ad => {
                            val article = new Article
                            article.setUser_id(user_id)
                            article.setUser_name(user_name)
                            article.setUser_id(user_id)
                            article.setArticleData(ad)
                            article
                        })
                        datas
                    } catch {
                        case e: Exception => {
                            log.error("计算报错：" + JSONUtil.toJsonStr(userExploreList))
                            List()
                        }
                    }
                })
                .toList
                .sortBy(article => {
                    val article_info = article.getArticleData.getArticle_info
                    val view_count = article_info.getView_count
                    val digg_count = article_info.getDigg_count
                    val comment_count = article_info.getComment_count
                    val collect_count = article_info.getCollect_count
                    (view_count / 100) + digg_count + (comment_count * 1.5).toInt + collect_count
                })(Ordering.Int.reverse)

            log.info("Top榜单 计算完成")

            val report = new TopReport
            report.setColumnList(columnList.asJavaCollection)
            report.setDiggList(diggList.asJavaCollection)
            report.setViewList(viewList.asJavaCollection)
            report.setLevelList(levelList.asJavaCollection)
            report.setArticleList(articleList.asJavaCollection)

            FileUtil.writeJson(outPath, report)
            FileUtil.writeJson(outNowPath, report)
            reportService.updateTopReport(report)

        } catch {
            case e: IOException =>
                e.printStackTrace()
        }
        log.info("计算Top榜单结束：" + now)
    }

}
