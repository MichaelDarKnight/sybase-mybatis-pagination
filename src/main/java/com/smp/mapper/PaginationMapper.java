package com.smp.mapper;

import com.smp.Page;
import com.smp.PageData;

import java.util.List;

/**
 * 分页mapper
 * Created by Administrator on 2017/3/18.
 */
public interface PaginationMapper{

    List<PageData> findByListPage(Page page);
    
}
