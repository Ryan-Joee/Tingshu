package com.ryan.repository;

import com.ryan.entity.SuggestIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SuggestRepository extends ElasticsearchRepository<SuggestIndex, Long> {
}
