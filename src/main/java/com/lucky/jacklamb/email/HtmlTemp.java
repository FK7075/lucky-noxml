package com.lucky.jacklamb.email;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/8 10:44
 */
public class HtmlTemp {

    private StringBuilder html;

    private String prefix;

    private String suffix;

    public HtmlTemp(){
        html=new StringBuilder();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setHtml(String html){
        this.html.append(html);
    }

    public HtmlTemp addHtmlFragment(String htmlFragment, Object...params){
        html.append(String.format(htmlFragment,params));
        return this;
    }

    public String getHtml(){
        if(prefix!=null){
            html=new StringBuilder(prefix).append(html);
        }
        if(suffix!=null){
            html.append(suffix);
        }
        return html.toString();
    }


}
