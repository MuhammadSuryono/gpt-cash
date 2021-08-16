package com.gpt.component.idm.menutree.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.menutree.model.IDMMenuTreeModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMMenuTreeRepository extends JpaRepository<IDMMenuTreeModel, String>, CashRepository<IDMMenuTreeModel> {
	
	@Query("from IDMMenuTreeModel menuTree "
			+ "where menuTree.menu.code in (?1) "
			+ "and menuTree.application.code in (?2) "
			+ "order by menuTree.lvl, menuTree.idx")
	List<IDMMenuTreeModel> findByMenuCodesAndApplicationCodes(List<String> menuCodeList, List<String> applicationCodeList);
}
