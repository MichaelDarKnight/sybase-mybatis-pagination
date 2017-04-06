package com.smp;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * sybase 分页
 * Created by 孙少平 on 2017/3/17.
 */
@Component
public class SybasePagination {

    @Resource(name = "sqlSessionFactory")
    private SqlSessionFactory sqlSessionFactory;

    private BoundSql getBoundSql(String mapperId, Object parameter) {
        return getMappedStatement(mapperId).getBoundSql(parameter);
    }

    private MappedStatement getMappedStatement(String mapperId) {
        return sqlSessionFactory.getConfiguration().getMappedStatement(mapperId);
    }

    public List<PageData> listPage(String mapperId, Object params) throws SQLException, NoSuchFieldException, IllegalAccessException {
        return queryForListPage(sqlSessionFactory.openSession().getConnection(), getBoundSql(mapperId, params), getMappedStatement(mapperId), (Page) params);
    }

    @SuppressWarnings("unchecked")
    private List<PageData> queryForListPage(final Connection connection, final BoundSql boundSql, final MappedStatement mappedStatement, final Page page) throws SQLException, NoSuchFieldException, IllegalAccessException {
        //分页count
        PagePlugin.pageCount(connection, mappedStatement, boundSql, page);

        return (List) new JdbcTemplate(new JdbcTemplateDataSource(connection))
                .query(new MyPreparedStatementCreator(boundSql, mappedStatement, page)
                , new RowMapperResultSetExtractor(new ColumnPageDataRowMapper(page)) {
                    @Override
                    public List extractData(ResultSet rs) throws SQLException {
                        if (page.getCurrentResult() != 0) {
                            //将游标移动到第一条记录
                            rs.first();
                            //   游标移动到要输出的第一条记录
                            rs.relative(page.getCurrentResult() - 1);
                        }
                        return super.extractData(rs);
                    }
                });

    }

    private class JdbcTemplateDataSource extends AbstractDataSource {

        private Connection connection;

        JdbcTemplateDataSource(Connection connection) {
            this.connection = connection;
        }

        public Connection getConnection() throws SQLException {
            return connection;
        }

        public Connection getConnection(String username, String password) throws SQLException {
            return connection;
        }
    }

    private class MyPreparedStatementCreator implements PreparedStatementCreator {

        private BoundSql boundSql;

        private MappedStatement mappedStatement;

        private Page page;

        MyPreparedStatementCreator(BoundSql boundSql, MappedStatement mappedStatement, Page page) {
            this.boundSql = boundSql;
            this.mappedStatement = mappedStatement;
            this.page = page;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement pstat = con.prepareStatement(boundSql.getSql(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            PagePlugin.setParameters(pstat, mappedStatement, boundSql, boundSql.getParameterObject());
            pstat.setMaxRows(page.getCurrentResult() + page.getShowCount());
            return pstat;
        }
    }

    private class ColumnPageDataRowMapper implements RowMapper<PageData> {

        private Page page;

        ColumnMapRowMapper columnMapRowMapper = new ColumnMapRowMapper() {
            @Override
            protected Map<String, Object> createColumnMap(int columnCount) {
                return new PageData();
            }

            @Override
            protected String getColumnKey(String columnName) {
                if (page.getCamelName())
                    return StringUtil.camelName(columnName);
                else
                    return super.getColumnKey(columnName);
            }
        };

        private ColumnPageDataRowMapper(Page page) {
            this.page = page;
        }


        public PageData mapRow(ResultSet rs, int rowNum) throws SQLException {
            return (PageData) columnMapRowMapper.mapRow(rs, rowNum);
        }
    }
}
