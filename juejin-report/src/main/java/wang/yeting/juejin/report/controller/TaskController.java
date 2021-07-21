package wang.yeting.juejin.report.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.yeting.juejin.report.task.MainTask;

/**
 * @author : weipeng
 * @since : 2021-07-05 4:05 下午
 */
@RestController
public class TaskController {

    @Autowired
    MainTask mainTask;

    @GetMapping("/task")
    public String task() {

        return "ok";
    }

    @GetMapping("/userDataSnapshot")
    public String userDataSnapshot() {
        mainTask.userDataSnapshot();
        return "ok";
    }

    @GetMapping("/userDataSnapshotBig")
    public String userDataSnapshotBig() {
        mainTask.userDataSnapshotBig();
        return "ok";
    }

    @GetMapping("/userExplore")
    public String userExplore() {
        mainTask.userExplore();
        return "ok";
    }

}
