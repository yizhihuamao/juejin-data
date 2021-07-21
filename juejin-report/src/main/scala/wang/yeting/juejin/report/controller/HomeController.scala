package wang.yeting.juejin.report.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.{GetMapping, PostMapping, ResponseBody}
import wang.yeting.juejin.report.Interceptor.AuthenticationInterceptor
import wang.yeting.juejin.report.service.ReportService

import scala.collection.JavaConverters._

/**
 * @author : weipeng
 * @since : 2021-07-03 3:49 下午 
 *
 */

@Controller
class HomeController @Autowired()(reportService: ReportService) {

    @GetMapping(Array("/index.html", "/index", "/", ""))
    def index(model: Model): String = {
        model.addAttribute("report", reportService.top())
        model.addAttribute("fangwen", AuthenticationInterceptor.getFangWen)
        "index"
    }

    @GetMapping(Array("/activity.html", "/activity"))
    def activity(model: Model): String = {
        model.addAttribute("report", reportService.activity())
        model.addAttribute("fangwen", AuthenticationInterceptor.getFangWen)
        "activity"
    }

    @PostMapping(Array("/json/article"))
    @ResponseBody
    def activity(): java.lang.Iterable[String] = {
        val report = reportService.top()
        report.getArticleList.asScala.map(_.getArticleData.getArticle_id).asJava
    }

}
