package com.gpt.component.idm.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.app.repository.IDMApplicationRepository;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menu.repository.IDMMenuRepository;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.idm.role.repository.IDMRoleRepository;
import com.gpt.component.idm.rolemenu.repository.IDMRoleMenuRepository;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Component
public class IDMRepository {
	@Autowired
	private IDMApplicationRepository applicationRepo;
	
	@Autowired
	private IDMRoleRepository roleRepo;
	
	@Autowired
	private IDMMenuRepository menuRepo;
	
	@Autowired
	private IDMUserRepository userRepo;
	
	@Autowired
	private IDMRoleMenuRepository roleMenuRepo;

	public IDMApplicationModel isIDMAppValid(String code) throws Exception {
		IDMApplicationModel model = applicationRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100005");
			}
		} else {
			throw new BusinessException("GPT-0100005");
		}
		
		return model;
	}
	
	public IDMRoleModel isIDMRoleValid(String code) throws Exception {
		IDMRoleModel model = roleRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100006");
			}
		} else {
			throw new BusinessException("GPT-0100006");
		}
		
		return model;
	}
	
	public IDMMenuModel isIDMMenuValid(String code) throws Exception {
		IDMMenuModel model = menuRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100007");
			}
		} else {
			throw new BusinessException("GPT-0100007");
		}
		
		return model;
	}
	
	public IDMUserModel isIDMUserValid(String code) throws Exception {
		IDMUserModel model = userRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100009");
			}
		} else {
			throw new BusinessException("GPT-0100009");
		}
		
		return model;
	}

	public IDMApplicationRepository getApplicationRepo() {
		return applicationRepo;
	}

	public IDMRoleRepository getRoleRepo() {
		return roleRepo;
	}

	public IDMMenuRepository getMenuRepo() {
		return menuRepo;
	}

	public IDMUserRepository getUserRepo() {
		return userRepo;
	}

	public IDMRoleMenuRepository getRoleMenuRepo() {
		return roleMenuRepo;
	}	
}
