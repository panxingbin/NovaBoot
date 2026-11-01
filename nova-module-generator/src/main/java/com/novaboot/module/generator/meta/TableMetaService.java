package com.novaboot.module.generator.meta;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 表元數據採集：統一走 JDBC DatabaseMetaData（MySQL / H2 通用，無方言 SQL）。
 * 僅做隻讀查詢，不修改任何表結構。
 */
@Service
@RequiredArgsConstructor
public class TableMetaService {

    private static final List<String> AUDIT_COLUMNS =
            List.of("created_at", "updated_at", "deleted", "create_by", "create_time", "update_by", "update_time", "del_flag");

    private final JdbcTemplate jdbcTemplate;

    private volatile Boolean h2;

    public record TableInfo(String tableName, String tableComment) {
    }

    public boolean isH2() {
        if (h2 == null) {
            String url = jdbcTemplate.execute(
                    (org.springframework.jdbc.core.ConnectionCallback<String>) con -> con.getMetaData().getURL());
            h2 = url != null && url.toLowerCase(Locale.ROOT).contains(":h2:");
        }
        return h2;
    }

    /**
     * 全部業務表（排除 Flyway 歷史表）
     */
    public List<TableInfo> listTables() {
        return jdbcTemplate.execute((org.springframework.jdbc.core.ConnectionCallback<List<TableInfo>>) con -> {
            DatabaseMetaData md = con.getMetaData();
            List<TableInfo> result = new ArrayList<>();
            try (ResultSet rs = md.getTables(catalogOf(con), schemaOf(con), "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String name = rs.getString("TABLE_NAME");
                    if ("flyway_schema_history".equalsIgnoreCase(name)) {
                        continue;
                    }
                    String remark = safe(rs, "REMARKS");
                    result.add(new TableInfo(name, remark == null ? "" : remark));
                }
            }
            return result;
        });
    }

    public TableMeta metaOf(String tableName) {
        return jdbcTemplate.execute((org.springframework.jdbc.core.ConnectionCallback<TableMeta>) con -> {
            DatabaseMetaData md = con.getMetaData();
            Set<String> pkColumns = new HashSet<>();
            try (ResultSet rs = md.getPrimaryKeys(catalogOf(con), schemaOf(con), tableName)) {
                while (rs.next()) {
                    pkColumns.add(rs.getString("COLUMN_NAME"));
                }
            }
            TableMeta meta = new TableMeta();
            meta.setTableName(tableName);
            meta.setTableComment(tableCommentOf(con, tableName));
            List<ColumnMeta> columns = new ArrayList<>();
            try (ResultSet rs = md.getColumns(catalogOf(con), schemaOf(con), tableName, "%")) {
                while (rs.next()) {
                    ColumnMeta col = new ColumnMeta();
                    col.setColumnName(rs.getString("COLUMN_NAME"));
                    int sqlType = rs.getInt("DATA_TYPE");
                    String typeName = rs.getString("TYPE_NAME");
                    String size = safe(rs, "COLUMN_SIZE");
                    col.setColumnType(displayType(typeName, size, sqlType));
                    String remark = safe(rs, "REMARKS");
                    col.setComment(remark == null ? "" : remark);
                    col.setNullable("YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")));
                    col.setPk(pkColumns.contains(col.getColumnName()));
                    col.setJavaType(javaTypeOf(sqlType));
                    col.setJavaName(underscoreToCamel(col.getColumnName()));
                    String lower = col.getColumnName().toLowerCase(Locale.ROOT);
                    col.setFormField(!col.isPk() && !AUDIT_COLUMNS.contains(lower));
                    col.setSearchCandidate(!col.isPk() && "String".equals(col.getJavaType()) && !AUDIT_COLUMNS.contains(lower));
                    columns.add(col);
                }
            }
            meta.setColumns(columns);
            columns.stream().filter(ColumnMeta::isSearchCandidate).findFirst().ifPresent(meta::setSearchColumn);
            return meta;
        });
    }

    /** MySQL 用 catalog 指定當前庫，H2 用 schema（PUBLIC） */
    private String catalogOf(java.sql.Connection con) throws java.sql.SQLException {
        return isH2() ? null : con.getCatalog();
    }

    private String schemaOf(java.sql.Connection con) throws java.sql.SQLException {
        return isH2() ? con.getSchema() : null;
    }

    private String tableCommentOf(java.sql.Connection con, String tableName) {
        try (ResultSet rs = con.getMetaData().getTables(catalogOf(con), schemaOf(con), tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                String remark = safe(rs, "REMARKS");
                return remark == null ? "" : remark;
            }
        } catch (Exception ignored) {
            // 表註釋拿不到不影響生成
        }
        return "";
    }

    private String safe(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (Exception e) {
            return null;
        }
    }

    private String displayType(String typeName, String size, int sqlType) {
        String name = typeName == null ? "VARCHAR" : typeName.toUpperCase(Locale.ROOT);
        if ((sqlType == Types.VARCHAR || sqlType == Types.CHAR) && size != null) {
            return name + "(" + size + ")";
        }
        return name;
    }

    private String javaTypeOf(int sqlType) {
        return switch (sqlType) {
            case Types.BIGINT -> "Long";
            case Types.INTEGER, Types.SMALLINT, Types.TINYINT -> "Integer";
            case Types.DECIMAL, Types.NUMERIC -> "BigDecimal";
            case Types.FLOAT, Types.DOUBLE, Types.REAL -> "Double";
            case Types.BOOLEAN, Types.BIT -> "Boolean";
            case Types.TIMESTAMP -> "LocalDateTime";
            case Types.DATE -> "LocalDate";
            case Types.TIME -> "LocalTime";
            default -> "String";
        };
    }

    private String underscoreToCamel(String name) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upper = false;
            }
        }
        return sb.toString();
    }
}
