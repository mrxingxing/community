package com.neu.community;

import com.neu.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Test
    public void testSensitiveFilter(){
        String text = "❤你❤妈❤死❤了，这❤里❤可❤以❤吸❤毒，可❤以❤赌❤博，可❤以❤开❤票，f❤uck y❤ou mot❤her, s❤uck my d❤ick";
        System.out.println(sensitiveFilter.filter(text));
    }
}
