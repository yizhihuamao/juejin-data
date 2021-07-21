package wang.yeting.juejin.report.Interceptor;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import wang.yeting.juejin.report.constant.FilePathConstant;
import wang.yeting.juejin.report.util.FileUtil;
import wang.yeting.juejin.report.util.RandomCodeUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : weipeng
 * @date : 2020-08-20 18:45
 */

@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    static ConcurrentMap<String, AtomicInteger> map = new ConcurrentHashMap<>();

    static AtomicInteger day = new AtomicInteger(0);

    static {
        JSONObject jsonObject = FileUtil.readJson(FilePathConstant.USER_VIEW_PATH, JSONObject.class);
        if (jsonObject != null) {
            day.getAndSet(jsonObject.getInt("day"));
            JSONObject map1 = jsonObject.getJSONObject("map");
            for (Map.Entry<String, Object> entry : map1.entrySet()) {
                map.put(entry.getKey(), new AtomicInteger(Integer.parseInt(entry.getValue().toString())));
            }
        }
        int nowDay = LocalDate.now().getDayOfMonth();
        if (day.get() != nowDay) {
            day.getAndSet(nowDay);
            map.clear();
        }
    }

    public static Map<String, Object> getFangWen() {
        int sum = map.values().stream().mapToInt(AtomicInteger::get).sum();
        Map<String, Object> res = new HashMap<>();
        res.put("sumView", sum);
        res.put("count", map.size());
        return res;
    }

    public static String getIPAddress(HttpServletRequest request) {
        String ip = null;

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        int nowDay = LocalDate.now().getDayOfMonth();
        if (day.get() != nowDay) {
            day.getAndSet(nowDay);
            map.clear();
        }
        if (RandomCodeUtil.genNumIncludeMinAndMax(1, 10) > 6) {
            HashMap<String, Object> map2 = new HashMap<>();
            map2.put("map", map);
            map2.put("day", day);
            FileUtil.writeJson(FilePathConstant.USER_VIEW_PATH, map2);
        }
        String ipAddress = getIPAddress(request);
        log.info("访问IP：{}",ipAddress);
        AtomicInteger atomicInteger = map.get(ipAddress);
        if (atomicInteger == null) {
            atomicInteger = new AtomicInteger();
            map.put(ipAddress, atomicInteger);
        }
        atomicInteger.incrementAndGet();
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

}
