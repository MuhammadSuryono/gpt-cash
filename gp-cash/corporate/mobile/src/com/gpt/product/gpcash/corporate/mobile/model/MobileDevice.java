package com.gpt.product.gpcash.corporate.mobile.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.mobile.Status;

@Entity
@Table(name = "CORP_MBL_DEVICE", uniqueConstraints = @UniqueConstraint(columnNames={"DEVICE_ID", "CORP_USER_CD"}))
public class MobileDevice implements Serializable {
	
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;

	@Column(name = "DEVICE_ID")
	protected String deviceId;
	
	@Column(name = "DEVICE_NAME")
	protected String deviceName;
	
	@Column(name = "DEVICE_MODEL")
	protected String model;
	
	@Column(name = "DEVICE_PLATFORM")
	protected String platform;
	
	@Column(name = "DEVICE_OS_VER")
	protected String osVersion;
	
	@Column(name = "DEVICE_APP_VER")
	protected String appVersion;
	
	@Column(name = "DEVICE_STATUS")
	protected Status status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_USER_CD", nullable = false)
	protected CorporateUserModel user;
	
	@Column(name = "LAST_ACTIVITY")
	protected Timestamp lastActivity;
	
	@Column(name = "FP_KEY", length = 1000)
	protected String fingerPrintPublicKey;
	
	@Column(name = "PUSH_NOTIFICATION_ID")
	protected String pushNotificationId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public CorporateUserModel getUser() {
		return user;
	}

	public void setUser(CorporateUserModel user) {
		this.user = user;
	}
	
	public Timestamp getLastActivity() {
		return lastActivity;
	}
	
	public void setLastActivity(Timestamp lastActivity) {
		this.lastActivity = lastActivity;
	}
	
	public String getFingerPrintPublicKey() {
		return fingerPrintPublicKey;
	}

	public void setFingerPrintPublicKey(String fingerPrintPublicKey) {
		this.fingerPrintPublicKey = fingerPrintPublicKey;
	}

	public String getPushNotificationId() {
		return pushNotificationId;
	}

	public void setPushNotificationId(String pushNotificationId) {
		this.pushNotificationId = pushNotificationId;
	}
	
}
