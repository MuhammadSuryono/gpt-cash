package com.gpt.platform.springboot;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ResourceResolver extends PathMatchingResourcePatternResolver {

	@Override
	protected Set<Resource> doFindAllClassPathResources(String path) throws IOException {
		Set<Resource> result = super.doFindAllClassPathResources(path);
		
		if("".equals(path)) {
			Enumeration<URL> resourceUrls = ClassLoader.getSystemClassLoader().getResources(".");
			while (resourceUrls.hasMoreElements()) {
				URL url = resourceUrls.nextElement();
				String urlPath = url.getPath();
				if(urlPath.endsWith("."))
					urlPath = urlPath.substring(0, urlPath.length() - 1);
				url = new URL(url.getProtocol(), url.getHost(), url.getPort(), urlPath);
				result.add(convertClassLoaderURL(url));
			}
		}
		
		return result;
	}
	
}
