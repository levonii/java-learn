package com.atguigu.redis.servlet;

import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Random;

//处理发送验证码的Servlet
@WebServlet("/SendCodeServlet")
public class SendCodeServlet extends HttpServlet {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//获取手机号
        String phoneNo = request.getParameter("phone_no");
        //验空
        if (phoneNo == null || phoneNo.equals("")) {
            return;
        }

        //计数器
        String countKey = phoneNo + ":count";
//创建Jedis对象
        Jedis jedis = new Jedis("192.168.6.100", 6379);
        //获取计数器的值
        String count = jedis.get(countKey);
        if(count ==null){
            //向redis中保存以一个计数器，设置值为1，有效期在当天
            long theLeftSeconds = getTheLeftSeconds();
            jedis.setex(countKey,(int)theLeftSeconds,"1");
        }else if("3".equals(count)){
            response.getWriter().write("limit");
            jedis.close();
            return;
        }else{
            jedis.incr(countKey);
        }
        //生成六位数验证码
        String code = getCode(6);
        System.out.println("code = " + code);
        String codeKey = phoneNo + ":code";

        //向Redis中保存验证码
        jedis.setex(codeKey, 120, code);
        response.getWriter().write("true");
        jedis.close();
        System.out.println("phoneNo = " + phoneNo);
        response.getWriter().write("limit");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    // 随机生成验证码的方法
    private String getCode(int len) {
        String code = "";
        for (int i = 0; i < len; i++) {
            int rand = new Random().nextInt(10);
            code += rand;
        }
        return code;
    }

    //获取当天剩余秒数的方法
    private long getTheLeftSeconds() {
        //获取现在的时间
        LocalTime now = LocalTime.now();
        //获取当日23点59分59秒的时间
        LocalTime end = LocalTime.of(23, 59, 59);
        //获取end与now相差的秒数
        long millis = Duration.between(now, end).toMillis() / 1000;
        return millis;
    }
}
