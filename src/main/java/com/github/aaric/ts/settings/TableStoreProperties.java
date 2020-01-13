package com.github.aaric.ts.settings;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * TableStore配置
 *
 * @author Aaric, created on 2020-01-13T16:26.
 * @version 0.0.2-SNAPSHOT
 */
@Getter
@Setter
@Component
@ConfigurationProperties("tablestore")
public class TableStoreProperties {

    /**
     * 数据的过期时间，单位秒, -1代表永不过期，例如设置过期时间为一年, 即为 365 * 24 * 3600。
     */
    public static final int DEFAULT_TIME_TO_LIVE = -1;

    /**
     * 保存的最大版本数，设置为3即代表每列上最多保存3个最新的版本。
     */
    public static final int DEFAULT_MAX_VERSIONS = 3;

    private String endPoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String instanceName;
}
