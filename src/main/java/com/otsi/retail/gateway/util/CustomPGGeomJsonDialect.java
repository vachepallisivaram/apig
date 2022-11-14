package com.otsi.retail.gateway.util;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StringType;

public class CustomPGGeomJsonDialect extends PostgreSQL95Dialect {

    public CustomPGGeomJsonDialect() {
        super();
        this.registerColumnType(2000, "jsonb");
        this.registerColumnType(Types.OTHER, "jsonb");
        this.registerFunction("json_text", new SQLFunctionTemplate(StringType.INSTANCE, "?1 ->> ?2"));
    }
}
