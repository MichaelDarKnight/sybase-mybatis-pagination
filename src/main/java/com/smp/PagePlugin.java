package com.smp;


import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class PagePlugin  {

    static void pageCount(Connection connection, MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) throws NoSuchFieldException, IllegalAccessException, SQLException {
        String countSql = "select count(0) from (" + sqlProcess(boundSql.getSql()) + ")  tmp_count"; //记录统计 == oracle 加 as 报错(SQL command not properly ended)
        PreparedStatement countStmt = connection.prepareStatement(countSql);
        BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(), countSql, boundSql.getParameterMappings(), parameterObject);
        setParameters(countStmt, mappedStatement, countBS, parameterObject);
        ResultSet rs = countStmt.executeQuery();
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        countStmt.close();
        Page page;
        if (parameterObject instanceof Page) {    //参数就是Page实体
            page = (Page) parameterObject;
            page.setEntityOrField(true);
            page.setTotalResult(count);
        } else {    //参数为某个实体，该实体拥有Page属性
            Field pageField = ReflectHelper.getFieldByFieldName(parameterObject, "page");
            if (pageField != null) {
                page = (Page) ReflectHelper.getValueByFieldName(parameterObject, "page");
                if (page == null)
                    page = new Page();
                page.setEntityOrField(false);
                page.setTotalResult(count);
                ReflectHelper.setValueByFieldName(parameterObject, "page", page); //通过反射，对实体对象设置分页对象
            } else {
                throw new NoSuchFieldException(parameterObject.getClass().getName() + "不存在 page 属性！");
            }
        }
    }

    /**
     * sql处理
     *
     * @param sql 被处理的sql
     * @return 处理后的sql语句
     */
    private static String sqlProcess(String sql) {
        //去掉sql语句 order by 语句
        int orderIndex;
        if ((orderIndex = sql.toLowerCase().lastIndexOf("order")) != -1) {
            return sql.substring(0, orderIndex);
        }
        return sql;
    }


    /**
     * 对SQL参数(?)设值,参考org.apache.ibatis.executor.parameter.DefaultParameterHandler
     */
    @SuppressWarnings("unchecked")
    static void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) throws SQLException {
        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    PropertyTokenizer prop = new PropertyTokenizer(propertyName);
                    if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
                        value = boundSql.getAdditionalParameter(prop.getName());
                        if (value != null) {
                            value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
                        }
                    } else {
                        value = metaObject == null ? null : metaObject.getValue(propertyName);
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    if (typeHandler == null) {
                        throw new ExecutorException("There was no TypeHandler found for parameter " + propertyName + " of statement " + mappedStatement.getId());
                    }
                    typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
                }
            }
        }
    }


}
