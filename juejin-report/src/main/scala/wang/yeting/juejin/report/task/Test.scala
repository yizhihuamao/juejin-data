package wang.yeting.juejin.report.task

/**
 * @author : weipeng
 * @since : 2021-07-06 1:53 下午 
 *
 */

object Test {

    def main(args: Array[String]): Unit = {
        val ints = List(1, 2, 3, 4, 5)
        val iterator = ints.sliding(2, 2).toList
        println(iterator)
    }

}
