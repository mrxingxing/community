package com.neu.community;

import com.neu.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ServiceTest {
    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void testLabelSet(){
        Set<String> set = discussPostService.findDiscussLabels();
        System.out.println(set);
    }
}
