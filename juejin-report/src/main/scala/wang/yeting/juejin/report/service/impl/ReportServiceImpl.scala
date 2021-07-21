package wang.yeting.juejin.report.service.impl

import lombok.extern.slf4j.Slf4j
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service
import wang.yeting.juejin.report.bean.{ActivityReport, ActivityReportResult, ActivityRule, TopReport}
import wang.yeting.juejin.report.constant.FilePathConstant
import wang.yeting.juejin.report.service.ReportService
import wang.yeting.juejin.report.task.ActivityReportTask
import wang.yeting.juejin.report.util.FileUtil

import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * @author : weipeng
 * @since : 2021-07-06 9:25 上午 
 *
 */
@Service
class ReportServiceImpl extends ReportService {

    private val log: Logger = LoggerFactory.getLogger(classOf[ActivityReportTask])

    var topReportAll: TopReport = _

    var topReport: TopReport = new TopReport()

    var activityReportAll: util.List[ActivityReport] = _
    var activityReport: util.List[ActivityReportResult] = new util.ArrayList[ActivityReportResult]()

    init()

    def init(): Unit = {
        topReportAll = FileUtil.readJson(FilePathConstant.REPORT_DARA_PATH, classOf[TopReport])
        initTop()
        activityReportAll = FileUtil.readJsonList(FilePathConstant.ACTIVITY_REPORT_DARA_PATH, classOf[ActivityReport])
        val activityRuleList = FileUtil.readJsonList(FilePathConstant.ACTIVITY_RULE_PATH, classOf[ActivityRule]).asScala
        initActivityReport(activityRuleList)
    }

    override def updateTopReport(r: TopReport): Unit = {
        topReportAll = r
        initTop()
    }

    def initTop(): Unit = {
        if (topReportAll != null) {
            topReport.setDiggList(topReportAll.getDiggList.asScala.take(50).asJavaCollection)
            topReport.setViewList(topReportAll.getViewList.asScala.take(50).asJavaCollection)
            topReport.setLevelList(topReportAll.getLevelList.asScala.take(300).asJavaCollection)
            topReport.setColumnList(topReportAll.getColumnList.asScala.take(500).map(c => {
                val column_version = c.getColumn_version
                if (column_version.getTitle.length > 15) {
                    column_version.setTitle(column_version.getTitle.substring(0, 14) + "...")
                }
                c
            }).asJavaCollection)
            topReport.setArticleList(topReportAll.getArticleList.asScala.take(500).map(c => {
                val article = c.getArticleData.getArticle_info
                if (article.getTitle.length > 25) {
                    article.setTitle(article.getTitle.substring(0, 24) + "...")
                }
                c
            }).asJavaCollection)
        }
    }

    override def top(): TopReport = {
        topReport
    }

    override def updateActivityReport(activityReportList: util.List[ActivityReport], activityRuleList: mutable.Buffer[ActivityRule]): Unit = {
        activityReportAll = activityReportList
        initActivityReport(activityRuleList)
    }

    def initActivityReport(activityRuleList: mutable.Buffer[ActivityRule]): Unit = {
        val ruleMap = activityRuleList.map(rule => (rule.getId, rule)).toMap
        if (activityReportAll != null) {
            activityReport = activityReportAll.asScala.map(a => {
                a.getUserActivityReportMap.asScala
                val result = new ActivityReportResult
                val rule = ruleMap(a.getId)
                result.setActivityRule(rule)
                result.setUserActivityReportList(
                    a.getUserActivityReportMap
                        .values()
                        .asScala
                        .toList
                        .sortBy(uar => {
                            val sortBy = rule.getSortBy
                            if (sortBy == null) {
                                uar.getCount.toDouble
                            } else {
                                try {
                                    val s1 = sortBy.split("#")
                                    //1个 或者 2个计算规则没写
                                    if (s1.size == 3) {
                                        val v1 = s1(0).split("@").map(f => {
                                            val field = uar.getClass.getDeclaredField(f)
                                            field.setAccessible(true)
                                            field.get(uar).asInstanceOf[Integer].toDouble
                                        })
                                        val s2 = s1(1).split("@")
                                        val v2 = mutable.Buffer[Double]()
                                        for (i <- v1.indices) {
                                            val v = v1(i)
                                            val s = s2(i).split("&")
                                            val d = s(0) match {
                                                case "+" => {
                                                    v + s(1).toDouble
                                                }
                                                case "-" => {
                                                    v - s(1).toDouble
                                                }
                                                case "*" => {
                                                    v * s(1).toDouble
                                                }
                                                case "/" => {
                                                    v / s(1).toDouble
                                                }
                                            }
                                            v2.append(d)
                                        }
                                        val s3 = s1(2).split("@")
                                        var v3 = v2.head
                                        for (i <- 1 until v2.size) {
                                            val v = v2(i)
                                            s3(i - 1) match {
                                                case "+" => {
                                                    v3 += v
                                                }
                                                case "-" => {
                                                    v3 -= v
                                                }
                                                case "*" => {
                                                    v3 *= v
                                                }
                                                case "/" => {
                                                    v3 /= v
                                                }
                                            }
                                        }
                                        v3
                                    } else {
                                        uar.getCount.toDouble
                                    }
                                } catch {
                                    case e: Exception => {
                                        e.printStackTrace()
                                        log.info(s"排序规则错误：${rule.getTitle}")
                                        uar.getCount.toDouble
                                    }
                                }
                            }
                        })(Ordering.Double.reverse)
                        .asJava
                )
                result
            }).asJava
        }
    }

    override def activity(): util.Iterator[util.List[ActivityReportResult]] = {
        activityReport.asScala.sliding(2, 2).map(_.asJava).asJava
    }
}
