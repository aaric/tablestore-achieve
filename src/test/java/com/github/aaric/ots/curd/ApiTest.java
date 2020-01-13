package com.github.aaric.ots.curd;

import com.alicloud.openservices.tablestore.ClientConfiguration;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.*;
import com.github.aaric.ots.settings.TableStoreProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.MessageFormat;

/**
 * ApiTest
 *
 * @author Aaric, created on 2020-01-13T16:18.
 * @version 0.0.2-SNAPSHOT
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ApiTest {

    public static final String TABLE_NAME = "telemetry";
    public static final String PRIMARY_KEY_NAME = "rowkey";
    public static final String DATA_KEY_NAME = "data";

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
    @Disabled
    public void testCreateTable() {
        // 表名
        TableMeta tableMeta = new TableMeta(TABLE_NAME);
        // 主键列
        tableMeta.addPrimaryKeyColumn(new PrimaryKeySchema(PRIMARY_KEY_NAME, PrimaryKeyType.STRING));
        // 数据的过期时间+保存的最大版本数
        TableOptions tableOptions = new TableOptions(TableStoreProperties.DEFAULT_TIME_TO_LIVE, TableStoreProperties.DEFAULT_MAX_VERSIONS);
        CreateTableRequest request = new CreateTableRequest(tableMeta, tableOptions);
        // 设置读写预留值（容量型实例只能设置为0，高性能实例可以设置为非零值）
        request.setReservedThroughput(new ReservedThroughput(new CapacityUnit(0, 0)));
        // 创建表
        client.createTable(request);
    }

    @Test
    @Disabled
    public void testPutRow() {
        // 构造主键
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(PRIMARY_KEY_NAME, PrimaryKeyValue.fromString("pk001"));
        PrimaryKey primaryKey = primaryKeyBuilder.build();

        // 准备数据
        RowPutChange rowPutChange = new RowPutChange(TABLE_NAME, primaryKey);
        // 添加数据列
        rowPutChange.addColumn(DATA_KEY_NAME, ColumnValue.fromString("hello world"));

        // 保持数据
        client.putRow(new PutRowRequest(rowPutChange));
    }

    @Test
    @Disabled
    public void testGetRow() {
        // 构造主键
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(PRIMARY_KEY_NAME, PrimaryKeyValue.fromString("pk001"));
        PrimaryKey primaryKey = primaryKeyBuilder.build();

        // 查询数据
        SingleRowQueryCriteria criteria = new SingleRowQueryCriteria(TABLE_NAME, primaryKey);
        criteria.addColumnsToGet(DATA_KEY_NAME);
        // 设置读取最新版本
        criteria.setMaxVersions(1);
        GetRowResponse getRowResponse = client.getRow(new GetRowRequest(criteria));

        // 打印数据
        Row row = getRowResponse.getRow();
        System.out.println("key: " + row.getPrimaryKey().toString());
        System.out.println("value: " + row.getColumn(DATA_KEY_NAME).get(0).getValue().asString());
    }

    @Test
    @Disabled
    public void testDeleteRow() {
        for (int i = 0; i < 10; i++) {
            // 构造主键
            PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
            primaryKeyBuilder.addPrimaryKeyColumn(PRIMARY_KEY_NAME, PrimaryKeyValue.fromString(MessageFormat.format("pk{0,number,000}", i)));
            PrimaryKey primaryKey = primaryKeyBuilder.build();

            // 删除数据
            RowDeleteChange rowDeleteChange = new RowDeleteChange(TABLE_NAME, primaryKey);
            client.deleteRow(new DeleteRowRequest(rowDeleteChange));
        }
    }

    @Test
    @Disabled
    public void testBatchWriteRow() {
        // 初始化批处理请求
        BatchWriteRowRequest batchWriteRowRequest = new BatchWriteRowRequest();

        // 准备数据
        for (int i = 0; i < 10; i++) {
            // 构造rowPutChange
            PrimaryKeyBuilder pkBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
            pkBuilder.addPrimaryKeyColumn(PRIMARY_KEY_NAME, PrimaryKeyValue.fromString(MessageFormat.format("pk{0,number,000}", i)));
            RowPutChange rowPutChange = new RowPutChange(TABLE_NAME, pkBuilder.build());
            // 添加数据列
            rowPutChange.addColumn(new Column(DATA_KEY_NAME, ColumnValue.fromString("hello world")));
            // 添加到batch操作中
            batchWriteRowRequest.addRowChange(rowPutChange);
        }

        // 保持数据
        BatchWriteRowResponse response = client.batchWriteRow(batchWriteRowRequest);
        System.out.println("是否全部成功：" + response.isAllSucceed());
        if (!response.isAllSucceed()) {
            for (BatchWriteRowResponse.RowResult rowResult : response.getFailedRows()) {
                System.out.println("失败的行：" + batchWriteRowRequest.getRowChange(rowResult.getTableName(), rowResult.getIndex()).getPrimaryKey());
                System.out.println("失败原因：" + rowResult.getError());
            }
            // 重试
            BatchWriteRowRequest retryRequest = batchWriteRowRequest.createRequestForRetry(response.getFailedRows());
        }
    }

    @Test
    @Disabled
    public void testGetRange() {
        // 初始化批处理请求
        RangeRowQueryCriteria rangeRowQueryCriteria = new RangeRowQueryCriteria(TABLE_NAME);

        // 设置起始主键
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(PRIMARY_KEY_NAME, PrimaryKeyValue.fromString("pk004"));
        rangeRowQueryCriteria.setInclusiveStartPrimaryKey(primaryKeyBuilder.build());

        // 设置结束主键
        primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(PRIMARY_KEY_NAME, PrimaryKeyValue.fromString("pk006"));
        rangeRowQueryCriteria.setExclusiveEndPrimaryKey(primaryKeyBuilder.build());

        // 设置版本
        rangeRowQueryCriteria.setMaxVersions(1);

        // 查询数据
        System.out.println("GetRange的结果为：");
        while (true) {
            GetRangeResponse getRangeResponse = client.getRange(new GetRangeRequest(rangeRowQueryCriteria));
            for (Row row : getRangeResponse.getRows()) {
                System.out.println("--");
                System.out.println("key: " + row.getPrimaryKey().toString());
                System.out.println("value: " + row.getColumn(DATA_KEY_NAME).get(0).getValue().asString());
            }

            // 若nextStartPrimaryKey不为null，则继续读取。
            if (getRangeResponse.getNextStartPrimaryKey() != null) {
                rangeRowQueryCriteria.setInclusiveStartPrimaryKey(getRangeResponse.getNextStartPrimaryKey());
            } else {
                break;
            }
        }
    }
}
