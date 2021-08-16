package com.gpt.platform.springboot.hystrix;

import static com.netflix.config.ConfigurationManager.APPLICATION_PROPERTIES;
import static com.netflix.config.ConfigurationManager.DISABLE_DEFAULT_ENV_CONFIG;
import static com.netflix.config.ConfigurationManager.DISABLE_DEFAULT_SYS_CONFIG;
import static com.netflix.config.ConfigurationManager.ENV_CONFIG_NAME;
import static com.netflix.config.ConfigurationManager.SYS_CONFIG_NAME;
import static com.netflix.config.ConfigurationManager.URL_CONFIG_NAME;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ReflectionUtils;

import com.netflix.config.AggregatedConfiguration;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicURLConfiguration;

@Configuration
public class ArchaiusAutoConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(ArchaiusAutoConfiguration.class);
	
	private static final AtomicBoolean initialized = new AtomicBoolean(false);

	@Autowired
	private ConfigurableEnvironment env;

	@Autowired(required = false)
	private List<AbstractConfiguration> externalConfigurations = new ArrayList<>();

	@PreDestroy
	public void close() {
		setStatic(ConfigurationManager.class, "instance", null);
		setStatic(ConfigurationManager.class, "customConfigurationInstalled", false);
		setStatic(DynamicPropertyFactory.class, "config", null);
		setStatic(DynamicPropertyFactory.class, "initializedWithDefaultConfig", false);
		setStatic(DynamicProperty.class, "dynamicPropertySupportImpl", null);
		initialized.compareAndSet(true, false);
	}

	@Bean
	public ConfigurableEnvironmentConfiguration configurableEnvironmentConfiguration() {
		ConfigurableEnvironmentConfiguration envConfig = new ConfigurableEnvironmentConfiguration(
				this.env);
		configureArchaius(envConfig);
		return envConfig;
	}

	@Configuration
	@ConditionalOnClass(Endpoint.class)
	@ConditionalOnEnabledEndpoint("archaius")
	protected static class ArchaiusEndpointConfiguration {
		@Bean
		protected ArchaiusEndpoint archaiusEndpoint() {
			return new ArchaiusEndpoint();
		}
	}

	protected void configureArchaius(ConfigurableEnvironmentConfiguration envConfig) {
		if (initialized.compareAndSet(false, true)) {
			String appName = this.env.getProperty("spring.application.name");
			if (appName == null) {
				appName = "application";
				logger.warn("No spring.application.name found, defaulting to 'application'");
			}
			System.setProperty(DeploymentContext.ContextKey.appId.getKey(), appName);

			ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();

			// support to add other Configurations (Jdbc, DynamoDb, Zookeeper, jclouds,
			// etc...)
			if (this.externalConfigurations != null) {
				for (AbstractConfiguration externalConfig : this.externalConfigurations) {
					config.addConfiguration(externalConfig);
				}
			}
			config.addConfiguration(envConfig,
					ConfigurableEnvironmentConfiguration.class.getSimpleName());

			// below come from ConfigurationManager.createDefaultConfigInstance()
			DynamicURLConfiguration defaultURLConfig = new DynamicURLConfiguration();
			try {
				config.addConfiguration(defaultURLConfig, URL_CONFIG_NAME);
			}
			catch (Throwable ex) {
				logger.error("Cannot create config from " + defaultURLConfig, ex);
			}

			// TODO: sys/env above urls?
			if (!Boolean.getBoolean(DISABLE_DEFAULT_SYS_CONFIG)) {
				SystemConfiguration sysConfig = new SystemConfiguration();
				config.addConfiguration(sysConfig, SYS_CONFIG_NAME);
			}
			if (!Boolean.getBoolean(DISABLE_DEFAULT_ENV_CONFIG)) {
				EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration();
				config.addConfiguration(environmentConfiguration, ENV_CONFIG_NAME);
			}

			ConcurrentCompositeConfiguration appOverrideConfig = new ConcurrentCompositeConfiguration();
			config.addConfiguration(appOverrideConfig, APPLICATION_PROPERTIES);
			config.setContainerConfigurationIndex(
					config.getIndexOfConfiguration(appOverrideConfig));

			addArchaiusConfiguration(config);
		}
		else {
			// TODO: reinstall ConfigurationManager
			logger.warn(
					"Netflix ConfigurationManager has already been installed, unable to re-install");
		}
	}

	private void addArchaiusConfiguration(ConcurrentCompositeConfiguration config) {
		if (ConfigurationManager.isConfigurationInstalled()) {
			AbstractConfiguration installedConfiguration = ConfigurationManager
					.getConfigInstance();
			if (installedConfiguration instanceof ConcurrentCompositeConfiguration) {
				ConcurrentCompositeConfiguration configInstance = (ConcurrentCompositeConfiguration) installedConfiguration;
				configInstance.addConfiguration(config);
			}
			else {
				installedConfiguration.append(config);
				if (!(installedConfiguration instanceof AggregatedConfiguration)) {
					logger.warn(
							"Appending a configuration to an existing non-aggregated installed configuration will have no effect");
				}
			}
		}
		else {
			ConfigurationManager.install(config);
		}
	}

	private static void setStatic(Class<?> type, String name, Object value) {
		// Hack a private static field
		Field field = ReflectionUtils.findField(type, name);
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, null, value);
	}

}