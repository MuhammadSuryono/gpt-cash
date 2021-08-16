package com.gpt.component.maintenance.branch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface BranchRepository extends JpaRepository<BranchModel, String>, CashRepository<BranchModel> {

}
