package com.kuang.utils;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.kuang.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// 爬虫工具类编写测试
public class HtmlParseUtil {

//    public static void main(String[] args) throws Exception {
//        new HtmlParseUtil().parseJD("vue").forEach(System.out::println);
//    }

    /**
     * @author 狂神说Java  公众号：狂神说
     * @param keywords 要搜索的关键字！
     * @return 抓取的商品集合
     */
    public List<Content> parseJD(String keywords) throws Exception {
        String url = "https://search.jd.com/Search?keyword="+keywords;
        //所有的javascript中能使用的方法，这里面都可以用
        Document document = Jsoup.parse(new URL(url), 30000);
        //div=“J_goodsList” 里面的数据
        Element element = document.getElementById("J_goodsList");
        //打印html数据
        System.out.println(element.html());
        //获取div=“J_goodsList”里面的所有的li标签
        Elements elements = element.getElementsByTag("li");

        ArrayList<Content> goodsList = new ArrayList<>();

        // 获取京东的商品信息
        for (Element el : elements) {
            //img标签的第一个 中的source-data-lazy-img属性
            //关于图片信息，大部分网页都是懒加载的，
            String img = el.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            // 封装获取的数据
            Content content = new Content();
            content.setImg(img);
            content.setPrice(price);
            content.setTitle(title);
            goodsList.add(content);
        }
        return goodsList;
    }


}
