package com.gd.self.tool.config.mybatis;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * H2 数据库自动建表配置类
 * 启动时自动扫描指定包下的所有实体类，根据注解创建表
 */
@Configuration
public class H2AutoTableConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 配置要扫描的实体类包路径
    // 修改为数组，支持多个包路径
    private static final String[] ENTITY_PACKAGES = {
            "com.gd.self.tool.tx.domain"
            // 可以继续添加其他包
    };
    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            System.out.println("========== 开始自动检测并创建数据库表 ==========");

            // 1. 获取所有实体类
            Set<Class<?>> entityClasses = new HashSet<>();
            for (String packageName : ENTITY_PACKAGES) {
                entityClasses.addAll(scanEntityClasses(packageName));
            }
            // 2. 为每个实体类创建表
            for (Class<?> entityClass : entityClasses) {
                createTableIfNotExists(entityClass);
            }

            System.out.println("========== 数据库表初始化完成 ==========");
        };
    }

    /**
     * 扫描指定包下的所有实体类
     */
    private Set<Class<?>> scanEntityClasses(String packageName) {
        try {
            String packagePath = packageName.replace('.', '/');
            URL resource = Thread.currentThread().getContextClassLoader().getResource(packagePath);

            if (resource == null) {
                throw new RuntimeException("找不到包路径: " + packageName);
            }

            File directory = new File(resource.getFile());
            List<Class<?>> classes = new ArrayList<>();

            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    Class<?> clazz = Class.forName(className);

                    // 只处理带有 @TableName 注解的类（MyBatis-Plus实体类）
                    if (clazz.isAnnotationPresent(TableName.class)) {
                        classes.add(clazz);
                    }
                }
            }

            return classes.stream().collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RuntimeException("扫描实体类失败", e);
        }
    }

    /**
     * 根据实体类创建表（如果表不存在）
     */
    private void createTableIfNotExists(Class<?> entityClass) {
        TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        String tableName = tableNameAnnotation.value();

        // 检查表是否已存在
        String checkTableSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        Integer count = jdbcTemplate.queryForObject(checkTableSql, Integer.class, tableName.toUpperCase());

        if (count != null && count > 0) {
            System.out.println("表已存在，跳过创建: " + tableName);
            return;
        }

        // 生成建表SQL
        String createTableSql = generateCreateTableSql(entityClass, tableName);

        // 执行建表
        try {
            jdbcTemplate.execute(createTableSql);
            System.out.println("成功创建表: " + tableName);
        } catch (Exception e) {
            System.err.println("创建表失败: " + tableName + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 根据实体类生成建表SQL
     */
    private String generateCreateTableSql(Class<?> entityClass, String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");

        Field[] fields = entityClass.getDeclaredFields();
        List<String> columnDefinitions = new ArrayList<>();
        String primaryKey = null;

        for (Field field : fields) {
            String columnName = getColumnName(field);
            String columnType = getColumnType(field);
            String columnDefinition = columnName + " " + columnType;

            // 处理主键
            if (field.isAnnotationPresent(TableId.class)) {
                TableId tableId = field.getAnnotation(TableId.class);
                primaryKey = columnName;

                // 处理自增
                if (tableId.type() == IdType.AUTO) {
                    columnDefinition += " AUTO_INCREMENT";
                }
            }else {
                // 处理是否可为空（这里简单处理，实际可根据@TableField的注解判断）
                columnDefinition += " NULL";
            }



            columnDefinitions.add(columnDefinition);
        }

        sql.append(String.join(", ", columnDefinitions));

        // 添加主键约束
        if (primaryKey != null) {
            sql.append(", PRIMARY KEY (").append(primaryKey).append(")");
        }

        sql.append(")");

        return sql.toString();
    }

    /**
     * 获取字段对应的数据库列名
     */
    private String getColumnName(Field field) {
        // 优先使用 @TableField 注解中的 value
        if (field.isAnnotationPresent(TableField.class)) {
            TableField tableField = field.getAnnotation(TableField.class);
            if (StringUtils.isNotBlank(tableField.value())) {
                return tableField.value();
            }
        }

        // 使用字段名（这里可以添加驼峰转下划线的逻辑）
        return StrUtil.toUnderlineCase(field.getName());
    }

    /**
     * 根据Java类型获取H2数据库类型
     */
    private String getColumnType(Field field) {
        Class<?> type = field.getType();

        if (type == String.class) {
            return "VARCHAR(255)";
        } else if (type == Long.class || type == long.class) {
            return "BIGINT";
        } else if (type == Integer.class || type == int.class) {
            return "INT";
        } else if (type == Double.class || type == double.class) {
            return "DOUBLE";
        } else if (type == Boolean.class || type == boolean.class) {
            return "BOOLEAN";
        } else if (type == java.math.BigDecimal.class) {
            return "DECIMAL(19,2)";
        } else if (type == java.time.LocalDateTime.class) {
            return "TIMESTAMP";
        } else if (type == java.time.LocalDate.class) {
            return "DATE";
        } else if (type == java.time.LocalTime.class) {
            return "TIME";
        } else {
            return "VARCHAR(255)"; // 默认
        }
    }
}