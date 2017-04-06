package com.smp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class CustomController {

    @Autowired
    private CustomService customService;

    @RequestMapping("/page")
    public void page(){
        Page page = new Page();
        //字段名称转换成小驼峰式true，不转换为false
        page.setCamelName(true);
        //设置当前是第几页
        page.setCurrentPage(2);
        //每页有多条数据
        page.setShowCount(5);
//        page.getPd().put("lastName", "1");
        List<PageData> byListPage = customService.findByListPage(page);
        System.out.println("一页条数："+byListPage.size());
        System.out.println("本页内容："+byListPage);
        System.out.println("总记录数："+page.getTotalResult());

    }

}
