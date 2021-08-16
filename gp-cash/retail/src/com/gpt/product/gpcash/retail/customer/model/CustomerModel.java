package com.gpt.product.gpcash.retail.customer.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.CityModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.parametermt.model.IdentityTypeModel;
import com.gpt.component.maintenance.parametermt.model.PostCodeModel;
import com.gpt.component.maintenance.parametermt.model.StateModel;
import com.gpt.component.maintenance.parametermt.model.SubstateModel;
import com.gpt.product.gpcash.servicepackage.model.ServicePackageModel;

@Entity
@Cacheable
@Table(name = "CUST")
public class CustomerModel implements Serializable{
	@Id
	@Column(name = "ID")
	protected String id;

	@Version
	protected int version;
	
	@Column(name="USER_ID")
	protected String userId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDM_USER_CD", nullable = false)
	protected IDMUserModel user;

	@Column(name = "NM")
	protected String name;
	
	@Column(name = "IDX")
	protected int idx;

	@Column(name = "IS_DELETE")
	protected String deleteFlag;

	@Column(name = "IS_INACTIVE")
	protected String inactiveFlag;
	
	@Column(name = "INACTIVE_REASON")
	protected String inactiveReason;
	
	@Column(name = "IS_SPECIAL_CH")
	protected String specialChargeFlag;
	
	@Column(name = "IS_SPECIAL_LMT")
	protected String specialLimitFlag;
	
	@Column(name = "HOST_CIF_ID")
	protected String hostCifId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROLE_CD")
	protected IDMRoleModel role;
	
	@Column(name = "ADDRESS_1")
	protected String address1;
	
	@Column(name = "ADDRESS_2")
	protected String address2;
	
	@Column(name = "ADDRESS_3")
	protected String address3;	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "POSTCODE_CD")
	protected PostCodeModel postcode;
	
	@Column(name = "EMAIL_1")
	protected String email1;
	
	@Column(name = "EMAIL_2")
	protected String email2;
	
	@Column(name = "PHONE_NO")
	protected String phoneNo;
	
	@Column(name = "MOBILE_NO")
	protected String mobileNo;
	
	@Column(name = "FAX_NO")
	protected String faxNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CITY_CD")
	protected CityModel city;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORG_UNIT_CD")
	protected BranchModel branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STATE_CD")
	protected StateModel state;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SUBSTATE_CD")
	protected SubstateModel substate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COUNTRY_CD")
	protected CountryModel country;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SRVC_PC_CD")
	protected ServicePackageModel servicePackage;
	
	@Column(name="TAX_ID_NO")
	protected String taxIdNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_CD")
	protected IDMApplicationModel application;
	
	@Column(name="LLD_IS_RES")
	protected String lldIsResidence;
	
	@Column(name="LLD_IS_CTZN")
	protected String lldIsCitizen;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LLD_CTZN_CTRY_CD")
	protected CountryModel citizenCountry;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LLD_RES_CTRY_CD")
	protected CountryModel residenceCountry;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDENTITY_TYP_CD")
	protected IdentityTypeModel identityType;
	
	@Column(name="IDENTITY_TYP_VAL")
	protected String identityTypeValue;
	
	@Column(name = "IS_NOTIFY_MY_TRX")
	protected String isNotifyMyTrx;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getInactiveFlag() {
		return inactiveFlag;
	}

	public void setInactiveFlag(String inactiveFlag) {
		this.inactiveFlag = inactiveFlag;
	}

	public String getInactiveReason() {
		return inactiveReason;
	}

	public void setInactiveReason(String inactiveReason) {
		this.inactiveReason = inactiveReason;
	}

	public String getSpecialChargeFlag() {
		return specialChargeFlag;
	}

	public void setSpecialChargeFlag(String specialChargeFlag) {
		this.specialChargeFlag = specialChargeFlag;
	}

	public String getSpecialLimitFlag() {
		return specialLimitFlag;
	}

	public void setSpecialLimitFlag(String specialLimitFlag) {
		this.specialLimitFlag = specialLimitFlag;
	}

	public String getHostCifId() {
		return hostCifId;
	}

	public void setHostCifId(String hostCifId) {
		this.hostCifId = hostCifId;
	}

	public IDMRoleModel getRole() {
		return role;
	}

	public void setRole(IDMRoleModel role) {
		this.role = role;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getAddress3() {
		return address3;
	}

	public void setAddress3(String address3) {
		this.address3 = address3;
	}

	public PostCodeModel getPostcode() {
		return postcode;
	}

	public void setPostcode(PostCodeModel postcode) {
		this.postcode = postcode;
	}

	public String getEmail1() {
		return email1;
	}

	public void setEmail1(String email1) {
		this.email1 = email1;
	}

	public String getEmail2() {
		return email2;
	}

	public void setEmail2(String email2) {
		this.email2 = email2;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public CityModel getCity() {
		return city;
	}

	public void setCity(CityModel city) {
		this.city = city;
	}

	public BranchModel getBranch() {
		return branch;
	}

	public void setBranch(BranchModel branch) {
		this.branch = branch;
	}

	public StateModel getState() {
		return state;
	}

	public void setState(StateModel state) {
		this.state = state;
	}

	public SubstateModel getSubstate() {
		return substate;
	}

	public void setSubstate(SubstateModel substate) {
		this.substate = substate;
	}

	public CountryModel getCountry() {
		return country;
	}

	public void setCountry(CountryModel country) {
		this.country = country;
	}

	public ServicePackageModel getServicePackage() {
		return servicePackage;
	}

	public void setServicePackage(ServicePackageModel servicePackage) {
		this.servicePackage = servicePackage;
	}

	public String getTaxIdNo() {
		return taxIdNo;
	}

	public void setTaxIdNo(String taxIdNo) {
		this.taxIdNo = taxIdNo;
	}

	public IDMApplicationModel getApplication() {
		return application;
	}

	public void setApplication(IDMApplicationModel application) {
		this.application = application;
	}

	public String getLldIsResidence() {
		return lldIsResidence;
	}

	public void setLldIsResidence(String lldIsResidence) {
		this.lldIsResidence = lldIsResidence;
	}

	public String getLldIsCitizen() {
		return lldIsCitizen;
	}

	public void setLldIsCitizen(String lldIsCitizen) {
		this.lldIsCitizen = lldIsCitizen;
	}

	public CountryModel getCitizenCountry() {
		return citizenCountry;
	}

	public void setCitizenCountry(CountryModel citizenCountry) {
		this.citizenCountry = citizenCountry;
	}

	public CountryModel getResidenceCountry() {
		return residenceCountry;
	}

	public void setResidenceCountry(CountryModel residenceCountry) {
		this.residenceCountry = residenceCountry;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Timestamp getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Timestamp updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public IDMUserModel getUser() {
		return user;
	}

	public void setUser(IDMUserModel user) {
		this.user = user;
	}

	public IdentityTypeModel getIdentityType() {
		return identityType;
	}

	public void setIdentityType(IdentityTypeModel identityType) {
		this.identityType = identityType;
	}

	public String getIdentityTypeValue() {
		return identityTypeValue;
	}

	public void setIdentityTypeValue(String identityTypeValue) {
		this.identityTypeValue = identityTypeValue;
	}

	public String getFaxNo() {
		return faxNo;
	}

	public void setFaxNo(String faxNo) {
		this.faxNo = faxNo;
	}

	public String getIsNotifyMyTrx() {
		return isNotifyMyTrx;
	}

	public void setIsNotifyMyTrx(String isNotifyMyTrx) {
		this.isNotifyMyTrx = isNotifyMyTrx;
	}
	
	
}
