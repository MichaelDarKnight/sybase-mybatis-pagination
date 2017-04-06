package com.smp;

import com.smp.mapper.CustomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;


@Service
public class CustomService {

    @Autowired
    private CustomMapper customMapper;

    @Autowired
    private SybasePagination sybasePagination;

    List<PageData> findByListPage(Page page){
        try {
            return sybasePagination.listPage(CustomMapper.class.getName() + ".findByListPage", page);
        } catch (SQLException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

}
