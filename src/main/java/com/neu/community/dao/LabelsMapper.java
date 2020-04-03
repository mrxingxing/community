package com.neu.community.dao;

import com.neu.community.entity.Favorite;
import com.neu.community.entity.Labels;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "LabelsMapper")
public interface LabelsMapper {


    List<Labels> selectLabels();



}