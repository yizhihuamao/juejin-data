package wang.yeting.juejin.report.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, RestController}
import wang.yeting.juejin.report.task.{ActivityReportTask, PostTask, TopReportTask}

/**
 * @author : weipeng
 * @since : 2021-07-03 3:49 下午 
 *
 */

@RestController
class TestController @Autowired()(reportTask: TopReportTask
                                  , activityReportTask: ActivityReportTask
                                  , postTask: PostTask
                                 ) {

    @GetMapping(Array("/topTask"))
    def topTask(): String = {
        reportTask.task()
        "6666"
    }

    @GetMapping(Array("/activityTask"))
    def activityTask(): String = {
        activityReportTask.task()
        "6666"
    }

    @GetMapping(Array("/postTask"))
    def postTask(): String = {
        postTask.task()
        "6666"
    }

}
