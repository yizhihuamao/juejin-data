package wang.yeting.juejin.report.service

import wang.yeting.juejin.report.bean.{ActivityReport, ActivityReportResult, ActivityRule, TopReport}

import java.util
import scala.collection.mutable

/**
 * @author : weipeng
 * @since : 2021-07-06 9:24 上午
 *
 */

trait ReportService {

    def updateActivityReport(activityReportList: util.List[ActivityReport], activityRuleList: mutable.Buffer[ActivityRule])

    def top(): TopReport

    def activity(): util.Iterator[util.List[ActivityReportResult]]

    def updateTopReport(report: TopReport)

}
