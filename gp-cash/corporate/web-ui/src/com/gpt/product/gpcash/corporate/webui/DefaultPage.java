package com.gpt.product.gpcash.corporate.webui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @deprecated
 * This class or rather this sub project is no longer needed as of common 1.0.2 and platform-webserver 0.0.2
 */

@Controller
public class DefaultPage {

    @GetMapping(path = "/corporate")
	public RedirectView defaultPage() {
    		return new RedirectView("/corporate/index.html");
	}
    
    @GetMapping(path = "/dki")
	public RedirectView defaultPageDKI() {
    		return new RedirectView("/dki/index.html");
	}
	
}
