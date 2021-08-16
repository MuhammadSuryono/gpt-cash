package com.gpt.platform.cash.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.common.Helper;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.spi.IResultHandler;
import com.gpt.component.common.spring.invoker.spi.ISpringBeanInvoker;
import com.gpt.component.license.LicenseReader;
import com.gpt.component.license.valueobject.Component;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.exception.HystrixTimeoutException;

import rx.Observable;
import rx.Observer;

public class BaseController {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String LICENSED_PRODUCT = "gp-cash";
	private static final String LICENSED_COMPONENT = "core";
	
	private static Component licensedComponent;
	
	@Autowired
	private ISpringBeanInvoker invoker;
	
	@Value("${gpcash.circuit-breaker.enable}")
	private boolean enableCircuitBreaker;
	
	
	static {
		// listener for license update
		LicenseReader.addListener(l -> {
			try {
				licensedComponent = l.getLicense(LICENSED_PRODUCT, LICENSED_COMPONENT);
			}catch(Exception e) {
				// do not use logger as it can reveal this code through stack trace
				System.out.println(e.getMessage());
			}
		});
	}
	
	protected <K> DeferredResult<K> invoke(String serviceName, String methodName, Object... params) {
		return invoke(serviceName, methodName, ApplicationConstants.TIMEOUT, params);
	}

	protected <K> DeferredResult<K> invoke(String serviceName, String methodName, long timeout, Object... params) {
		return invoke(serviceName, methodName, timeout, this::defaultOnSuccess, this::defaultOnException, params);
	}
	
	/**
	 * Use this method if you need a custom handler for success and error, always remember to call {@link #removeAsynchResult()} to get the DeferredResult object on the handler
	 * @param serviceName
	 * @param methodName
	 * @param param
	 * @param successHandler
	 * @param exceptionHandler
	 * @return
	 */
	protected <K, T> DeferredResult<K> invoke(String serviceName, String methodName, IResultHandler<DeferredResult<K>, T> successHandler, IResultHandler<DeferredResult<K>, Exception> exceptionHandler, Object... params) {
		return invoke(serviceName, methodName, ApplicationConstants.TIMEOUT, successHandler, exceptionHandler, params);
	}

	protected void preInvoke(HttpServletRequest request, String serviceName, String methodName, Object... params) {
	}
	
	protected void checkUsage() throws Exception {
		Helper.checkUsage();
		if(licensedComponent == null) {
			LicenseReader lr = LicenseReader.getInstance();
			licensedComponent = lr.getLicense(LICENSED_PRODUCT, LICENSED_COMPONENT);
		}
		licensedComponent.check();
	}
	
	/**
	 * Use this method if you need a custom handler for success and error, always remember to call {@link #removeAsynchResult()} to get the DeferredResult object on the handler
	 * @param serviceName
	 * @param methodName
	 * @param param
	 * @param timeout
	 * @param successHandler
	 * @param exceptionHandler
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <K, T> DeferredResult<K> invoke(String serviceName, String methodName, long timeout, IResultHandler<DeferredResult<K>, T> successHandler, IResultHandler<DeferredResult<K>, Exception> exceptionHandler, Object... params) {
		
		try {
			checkUsage();
		}catch(Exception e) {
			final DeferredResult deferredResult = new DeferredResult<>();
			
			Map<String, String> result = new HashMap<>(1,1);
			result.put("message", e.getMessage());
			deferredResult.setResult(result);

			HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			
			return deferredResult;
		}
		
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		preInvoke(request, serviceName, methodName, params);
		
		final DeferredResult<K> deferredResult = new DeferredResult<>();
		
		if(enableCircuitBreaker) {
			CommandInvoker<K, T> cmd = new CommandInvoker<>(deferredResult, serviceName, methodName, timeout, successHandler, exceptionHandler, params);
			cmd.toObservable().subscribe(
				new Observer<T>() {
					@Override
					public void onCompleted() {
					}
					
					@Override
					public void onError(Throwable e) {
						if(!deferredResult.hasResult()) {
							deferredResult.setErrorResult(e);
						}
					}
					
					@Override
					public void onNext(Object t) {
					}
				}
			);
		} else {
			invoker.invoke(deferredResult, serviceName, methodName, params, timeout, successHandler, exceptionHandler);
		}
		
		return deferredResult;
	}
	
	@SuppressWarnings("unchecked")
	protected <K, T> void defaultOnSuccess(DeferredResult<K> deferredResult, T result) {
		if (logger.isDebugEnabled())
			logger.debug("onSuccess : " + result);
		
		deferredResult.setResult((K)result);
	}
	
	protected <K> void defaultOnException(DeferredResult<K> deferredResult, Exception ex) {
		if (logger.isDebugEnabled())
			logger.debug("onException : " + ex);
		
		deferredResult.setErrorResult(ex);
	}
	
	class CommandInvoker<K, T> extends HystrixObservableCommand<T> {
		String serviceName;
		String methodName;
		long timeout;
		DeferredResult<K> deferredResult;
		IResultHandler<DeferredResult<K>, T> successHandler;
		IResultHandler<DeferredResult<K>, Exception> exceptionHandler;
		Object[] params;
		
		public CommandInvoker(DeferredResult<K> deferredResult, String serviceName, String methodName, long timeout, IResultHandler<DeferredResult<K>, T> successHandler, IResultHandler<DeferredResult<K>, Exception> exceptionHandler, Object... params ) {
			super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(serviceName))
					    .andCommandKey(HystrixCommandKey.Factory.asKey(
					    		new StringBuilder(serviceName.length() + methodName.length() + 1)
					    			.append(serviceName).append("#").append(methodName).toString())));
			this.deferredResult = deferredResult;
			this.serviceName = serviceName;
			this.methodName = methodName;
			this.timeout = timeout;
			this.successHandler = successHandler;
			this.exceptionHandler = exceptionHandler;
			this.params = params;
		}

		@Override
		protected Observable<T> construct() {
			return Observable.create( subscriber -> {
				try {
					invoker.invoke(deferredResult, serviceName, methodName, params, timeout, (DeferredResult<K> deferredResult, T result) -> {
						if(successHandler!=null)
							successHandler.onResult(deferredResult, result);
						subscriber.onCompleted();
					}, (deferredResult, ex) -> {
						if(exceptionHandler!=null)
							exceptionHandler.onResult(deferredResult, ex);
						if(ex instanceof BusinessException) {
							// don't want to trip the circuit breaker
							subscriber.onCompleted();
						} else if(ex instanceof TimeoutException || ex.getCause() instanceof TimeoutException) {
							// let Timeout be handled properly so the report can distinguish regular ex with timeout ex 
							subscriber.onError(new HystrixTimeoutException());
						} else {
							subscriber.onError(ex);
						}
					});
				}catch(Exception e) {
					if(exceptionHandler!=null)
						exceptionHandler.onResult(deferredResult, e);
					subscriber.onError(e);
				}
			});
		}
	}
}
