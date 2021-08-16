package com.gpt.product.gpcash.menupackage.spi;

import java.util.List;

public interface MenuPackageListener {
	void deleteALLCorporateMenuByMenuList(String roleCode, List<String> menuList) throws Exception;
}
