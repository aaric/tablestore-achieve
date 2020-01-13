package com.github.aaric.ts;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * HelloTest
 *
 * @author Aaric, created on 2020-01-13T15:43.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class HelloTest {

    @Value("${spring.profiles.active}")
    private String activeProfileName;

    @Test
    public void testActiveProfileName() {
        Assertions.assertNotNull(activeProfileName);
    }
}
