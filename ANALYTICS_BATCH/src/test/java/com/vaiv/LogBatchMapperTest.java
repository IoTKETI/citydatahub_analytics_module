package com.vaiv;

import lombok.extern.slf4j.Slf4j;

import com.vaiv.restFull.domain.LogBatch;
import com.vaiv.restFull.mapper.BatchMapper;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class LogBatchMapperTest {

    @Autowired
    BatchMapper batchMapper;

    @Test
    public void logBatchMapperTest() {

        LogBatch logBatch = new LogBatch();
        logBatch.setCode("ABC");
        logBatch.setUserId("ABC");

        System.out.println(logBatch.toString());

        try {
            System.out.println("test code is on comment status.");
            //batchMapper.insertLogBatch(logBatch);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
