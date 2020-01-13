package com.github.aaric.ts.curd;

import com.alicloud.openservices.tablestore.ClientConfiguration;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.*;
import com.github.aaric.ts.settings.TableStoreProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * ApiTest
 *
 * @author Aaric, created on 2020-01-13T16:18.
 * @version 0.0.2-SNAPSHOT
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ApiTest {

    @Autowired
    private TableStoreProperties tableStoreProperties;

    private SyncClient client;

    @BeforeEach
    public void setup() {
        // ClientConfiguration提供了很多配置项，以下只列举部分。
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        // 设置建立连接的超时时间。
        clientConfiguration.setConnectionTimeoutInMillisecond(5000);
        // 设置socket超时时间。
        clientConfiguration.setSocketTimeoutInMillisecond(5000);
        // 设置重试策略，若不设置，采用默认的重试策略。
        clientConfiguration.setRetryStrategy(new AlwaysRetryStrategy());
        // 初始化客户端
        client = new SyncClient(tableStoreProperties.getEndPoint(), tableStoreProperties.getAccessKeyId(), tableStoreProperties.getAccessKeySecret(),
                tableStoreProperties.getInstanceName(), clientConfiguration);
    }

    @Test
    public void testCreateTable() {
        // 表名
        TableMeta tableMeta = new TableMeta("telemetry");
        // 主键列
        tableMeta.addPrimaryKeyColumn(new PrimaryKeySchema("rowkey", PrimaryKeyType.STRING));
        // 数据的过期时间+保存的最大版本数
        TableOptions tableOptions = new TableOptions(TableStoreProperties.DEFAULT_TIME_TO_LIVE, TableStoreProperties.DEFAULT_MAX_VERSIONS);
        CreateTableRequest request = new CreateTableRequest(tableMeta, tableOptions);
        // 设置读写预留值（容量型实例只能设置为0，高性能实例可以设置为非零值）
        request.setReservedThroughput(new ReservedThroughput(new CapacityUnit(0, 0)));
        // 创建表
        client.createTable(request);
    }
}
