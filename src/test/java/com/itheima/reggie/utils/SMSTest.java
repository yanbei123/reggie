/*
package com.itheima.reggie.utils;

import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

*/
/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/07/03/10:13
 * @Description: 短信服务测试
 *//*

public class SMSTest {
    @Test
    public static void main(String[] args) {
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "5c64c27e902f4ff5a3a308ede32a15a6";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        //随机生成四位验证码

        querys.put("mobile", "19155368471");
        querys.put("param", "code:1234");
        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            */
/**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             *//*

            HttpResponse response = (HttpResponse) HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/
