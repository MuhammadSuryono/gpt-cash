package com.gpt.component.idm.menu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMMenuRepository extends JpaRepository<IDMMenuModel, String>, CashRepository<IDMMenuModel> {
	@Query("select menu from IDMMenuTreeModel tree "
			+ "join tree.menu menu "
			+ "join menu.menuType typ "
			+ "where tree.application.code = ?1 "
			+ "and typ.code <> 'E' "
			+ "and menu.inactiveFlag = 'N' "
			+ "and menu.deleteFlag = 'N' "
			+ "order by menu.name")
	List<IDMMenuModel> findByApplicationCodeForActivityLog(String applicationCode);
	
	@Query("select distinct roleMenu.menu.code from IDMRoleMenuModel roleMenu "
			+ "where roleMenu.role.code in "
			+ "(select userRole.role.code "
			    + "from IDMUserRoleModel userRole "
			    + "where userRole.user.code = ?1 "
			    + "and userRole.role.roleType.code = 'AP')")
	List<String> findMenuByUserCodeForAP(String userCode);
	
	@Query("select distinct roleMenu.menu.code from IDMRoleMenuModel roleMenu "
			+ "where roleMenu.role.code in "
			+ "(select userRole.role.code "
			    + "from IDMUserRoleModel userRole "
			    + "where userRole.user.code = ?1 "
			    + "and userRole.role.roleType.code = 'WF') and roleMenu.menu.code in (?2)")
	List<String> findMenuByUserCodeJoinWithWF(String userCode, List<String> menuAPList);
	
	@Query("select distinct roleMenu.menu.code from IDMRoleMenuModel roleMenu "
			+ "where roleMenu.role.code in "
			+ "(select userRole.role.code "
			    + "from IDMUserRoleModel userRole "
			    + "where userRole.user.code = ?1 "
			    + "and userRole.role.roleType.code = 'AP')")
	List<String> findMenuByUserCodeForFilterPendingTask(String userCode);
	
	@Query("select distinct roleMenu.menu from IDMRoleMenuModel roleMenu "
			+ "where roleMenu.role.code = ?1 and roleMenu.menu.menuType in ('T', 'M')"
			+ "order by roleMenu.menu.name")
	List<IDMMenuModel> findTransactionAndMaintenanceMenuByRoleCode(String roleCode);
	
	@Query("select distinct roleMenu.menu from IDMRoleMenuModel roleMenu "
			+ "where roleMenu.role.code in (?1) and roleMenu.menu.menuType in ('I')"
			+ "order by roleMenu.menu.name")
	List<IDMMenuModel> findNonFinancialMenuByRoleCode(List<String> roleList);
	
	@Query("select roleMenu.menu.code from IDMRoleMenuModel roleMenu "
			+ "where roleMenu.role.code in (?1) and roleMenu.menu.menuType in ('I')"
			+ "order by roleMenu.menu.name")
	List<String> findListOfStringForNonFinancialMenuByRoleCode(List<String> roleList);
	
	@Modifying
	@Query("delete from IDMRoleMenuModel roleMenu "
			+ "where roleMenu.role.code in (?1) and roleMenu.menu.code in (?2)")
	void deleteByRoleCodeListAndMenuList(List<String> roleList, List<String> menuList);
}
