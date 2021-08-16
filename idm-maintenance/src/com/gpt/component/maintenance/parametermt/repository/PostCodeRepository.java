package com.gpt.component.maintenance.parametermt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.parametermt.model.PostCodeModel;

@Repository
public interface PostCodeRepository extends JpaRepository<PostCodeModel, String> {

}
