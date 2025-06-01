package com.ryan.repository;

import com.ryan.entity.AlbumInfoIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AlbumRepository extends ElasticsearchRepository<AlbumInfoIndex, Long> {
}
