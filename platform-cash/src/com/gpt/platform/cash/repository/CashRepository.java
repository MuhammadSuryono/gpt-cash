package com.gpt.platform.cash.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.component.common.repository.BasicRepository;

public interface CashRepository<T> extends BasicRepository<T> {

	Page<T> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;
}
