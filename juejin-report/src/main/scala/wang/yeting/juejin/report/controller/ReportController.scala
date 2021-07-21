package wang.yeting.juejin.report.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, RestController}
import wang.yeting.juejin.report.bean.TopReport
import wang.yeting.juejin.report.service.ReportService

/**
 * @author : weipeng
 * @since : 2021-07-06 12:22 下午 
 *
 */

@RestController
class ReportController @Autowired()(reportService: ReportService) {

    @GetMapping(Array("report"))
    def test(): TopReport = {
        reportService.top()
    }
}
