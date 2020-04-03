package com.neu.community;

import com.neu.community.dao.DiscussPostMapper;
import com.neu.community.dao.UserMapper;
import com.neu.community.dao.elasticsearch.DiscussPostRepository;
import com.neu.community.entity.DiscussPost;
import com.neu.community.entity.User;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {


    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    @Test
    public void testInsert(){
        discussRepository.save(discussMapper.selectDiscussPostById(228));
        discussRepository.save(discussMapper.selectDiscussPostById(2));
        discussRepository.save(discussMapper.selectDiscussPostById(283));
    }

    @Test
    public void testInsertList(){
        discussRepository.saveAll(discussMapper.selectDiscussPosts(150,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(153,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(1,0,100,0));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussMapper.selectDiscussPostById(228);
        post.setContent("分身强袭，跳刀冰眼");
        discussRepository.save(post);
    }

    @Test
    public void testDelete(){
        discussRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("狗哥","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("狗哥").preTags("<em>").postTags("</em>")
                ).build();
        Page<DiscussPost> page = discussRepository.search(searchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getSize());
        System.out.println(page.getNumber());
        for(DiscussPost post:page){
            System.out.println(post);
        }
    }

}
