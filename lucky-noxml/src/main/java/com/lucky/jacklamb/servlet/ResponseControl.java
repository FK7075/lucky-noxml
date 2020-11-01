package com.lucky.jacklamb.servlet;

import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.servlet.core.Model;
import com.lucky.jacklamb.thymeleaf.utils.ThymeleafConfig;
import com.lucky.jacklamb.thymeleaf.utils.ThymeleafWrite;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 处理响应相关的类
 *
 * @author fk-7075
 */
public class ResponseControl {

    private static final boolean isEnabled= ThymeleafConfig.getConf().isEnabled();


    /**
     * 响应当前请求
     *
     * @param info    响应的目标
     * @param pre_suf 前后缀配置
     * @throws IOException
     * @throws ServletException
     */
    private void toPage(Model model, String info, List<String> pre_suf) {
        String topage = "";
        if (info.contains("page:")) {//重定向到页面
            info = info.replaceFirst("page:", "");
            topage = model.getRequest().getContextPath() + pre_suf.get(0) + info + pre_suf.get(1);
            topage = topage.replaceFirst(" ", "");
            model.redirect(topage);
        } else if (info.contains("forward:")) {//转发到本Controller的某个方法
            info = info.replaceFirst("forward:", "");
            model.forward(info);
        } else if (info.contains("redirect:")) {//重定向到本Controller的某个方法
            info = info.replaceFirst("redirect:", "");
            model.redirect(info);
        } else {//转发到页面
            if(isEnabled){
                ThymeleafWrite.write(model,info);
            }else{
                topage = pre_suf.get(0) + info + pre_suf.get(1);
                topage = topage.replaceAll(" ", "");
                model.forward(topage);
            }
        }
    }


    /**
     * 处理响应信息
     *
     * @param model  Model对象
     * @param rest   响应方式
     * @param method 响应请求的方法
     * @param obj    方法返回的结果
     * @param pre_suf 转发重定向操作的固定前后缀
     */
    public void jump(Model model, Rest rest, Method method, Object obj,List<String> pre_suf) {
        if (obj != null) {
            if (rest == Rest.JSON) {
                model.writerJson(obj);
                return;
            }
            if (rest == Rest.XML) {
                model.writerXml(obj);
                return;
            }
            if (rest == Rest.TXT) {
                model.writer(obj.toString());
                return;
            }
            if (rest == Rest.NO) {
                if (String.class.isAssignableFrom(obj.getClass())) {
                        toPage(model, obj.toString(), pre_suf);
                } else {
                    RuntimeException e = new RuntimeException("返回值类型错误，无法完成转发和重定向操作!合法的返回值类型为String，错误位置：" + method);
                    model.error(e,Code.ERROR);
                }
            }

        }
    }
}
