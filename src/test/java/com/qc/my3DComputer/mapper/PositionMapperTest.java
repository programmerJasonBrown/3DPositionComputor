package com.qc.my3DComputer.mapper;

import com.qc.my3DComputer.domain.Position;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

class PositionMapperTest {
    private PositionMapper positionMapper;

    private ApplicationContext applicationContext;

    @Test
    void selectByPrimaryKey() {
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml");
        positionMapper = (PositionMapper) applicationContext.getBean("positionMapper");
        System.out.println();
        Position position = positionMapper.selectByPrimaryKey(1);
        System.out.println();

    }
}