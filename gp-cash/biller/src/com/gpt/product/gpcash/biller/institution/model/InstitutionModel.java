package com.gpt.product.gpcash.biller.institution.model;

import java.io.Serializable;
import java.math.BigDecimal;
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

import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.product.gpcash.biller.institutioncategory.model.InstitutionCategoryModel;

@Entity
@Cacheable
@Table(name = "PRO_INST")
public class InstitutionModel implements Serializable {
	@Id
	@Column(name = "CD")
	protected String code;

	@Version
	protected int version;

	@Column(name = "NM")
	protected String name;
	
	@Column(name = "NM_ENG")
	protected String nameEng;
	
	@Column(name = "DSCP")
	protected String dscp;
	
	@Column(name = "IDX")
	protected int idx;
	
	@Column(name = "AMT_SEL")
	protected String amountSelection;
	
	@Column(name = "IS_BILL_ONLINE")
	protected String billOnlineFlag;
	
	@Column(name = "CHARGES", precision=25, scale=7)
	protected BigDecimal charges;
	
	@Column(name = "IS_DELETE")
	protected String deleteFlag;
	
	@Column(name = "INSTITUTION_TYPE")
	protected String institutionType;
	
	@Column(name = "NM_1")
	protected String name1;
	
	@Column(name = "NM_2")
	protected String name2;

	@Column(name = "NM_3")
	protected String name3;
	
	@Column(name = "NM_4")
	protected String name4;
	
	@Column(name = "NM_5")
	protected String name5;
	
	@Column(name = "NM_ENG_1")
	protected String nameEng1;
	
	@Column(name = "NM_ENG_2")
	protected String nameEng2;
	
	@Column(name = "NM_ENG_3")
	protected String nameEng3;
	
	@Column(name = "NM_ENG_4")
	protected String nameEng4;
	
	@Column(name = "NM_ENG_5")
	protected String nameEng5;
	
	@Column(name = "REGEX_1")
	protected String regex1;
	
	@Column(name = "REGEX_2")
	protected String regex2;
	
	@Column(name = "REGEX_3")
	protected String regex3;
	
	@Column(name = "REGEX_4")
	protected String regex4;
	
	@Column(name = "REGEX_5")
	protected String regex5;
	
	//setting parameter type
	@Column(name = "TYPE_1")
	protected String type1;
	
	@Column(name = "TYPE_2")
	protected String type2;

	@Column(name = "TYPE_3")
	protected String type3;
	
	@Column(name = "TYPE_4")
	protected String type4;
	
	@Column(name = "TYPE_5")
	protected String type5;
	
	@Column(name = "TYPE_PARAMETER_1")
	protected String typeParameter1;
	
	@Column(name = "TYPE_PARAMETER_2")
	protected String typeParameter2;

	@Column(name = "TYPE_PARAMETER_3")
	protected String typeParameter3;
	
	@Column(name = "TYPE_PARAMETER_4")
	protected String typeParameter4;
	
	@Column(name = "TYPE_PARAMETER_5")
	protected String typeParameter5;
	
	@Column(name = "TYPE_PARAMETER_DATA_MAP_1")
	protected String typeParameterDataMap1;
	
	@Column(name = "TYPE_PARAMETER_DATA_MAP_2")
	protected String typeParameterDataMap2;

	@Column(name = "TYPE_PARAMETER_DATA_MAP_3")
	protected String typeParameterDataMap3;
	
	@Column(name = "TYPE_PARAMETER_DATA_MAP_4")
	protected String typeParameterDataMap4;
	
	@Column(name = "TYPE_PARAMETER_DATA_MAP_5")
	protected String typeParameterDataMap5;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AMT_CCY_CD", nullable = false)
	protected CurrencyModel amountCurrency;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "INS_CAT_CD", nullable = false)
	protected InstitutionCategoryModel institutionCategory;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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

	public String getDscp() {
		return dscp;
	}

	public void setDscp(String dscp) {
		this.dscp = dscp;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getAmountSelection() {
		return amountSelection;
	}

	public void setAmountSelection(String amountSelection) {
		this.amountSelection = amountSelection;
	}

	public String getBillOnlineFlag() {
		return billOnlineFlag;
	}

	public void setBillOnlineFlag(String billOnlineFlag) {
		this.billOnlineFlag = billOnlineFlag;
	}

	public BigDecimal getCharges() {
		return charges;
	}

	public void setCharges(BigDecimal charges) {
		this.charges = charges;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getInstitutionType() {
		return institutionType;
	}

	public void setInstitutionType(String institutionType) {
		this.institutionType = institutionType;
	}

	public String getName1() {
		return name1;
	}

	public void setName1(String name1) {
		this.name1 = name1;
	}

	public String getName2() {
		return name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}

	public String getName3() {
		return name3;
	}

	public void setName3(String name3) {
		this.name3 = name3;
	}

	public String getName4() {
		return name4;
	}

	public void setName4(String name4) {
		this.name4 = name4;
	}

	public String getName5() {
		return name5;
	}

	public void setName5(String name5) {
		this.name5 = name5;
	}

	public String getNameEng1() {
		return nameEng1;
	}

	public void setNameEng1(String nameEng1) {
		this.nameEng1 = nameEng1;
	}

	public String getNameEng2() {
		return nameEng2;
	}

	public void setNameEng2(String nameEng2) {
		this.nameEng2 = nameEng2;
	}

	public String getNameEng3() {
		return nameEng3;
	}

	public void setNameEng3(String nameEng3) {
		this.nameEng3 = nameEng3;
	}

	public String getNameEng4() {
		return nameEng4;
	}

	public void setNameEng4(String nameEng4) {
		this.nameEng4 = nameEng4;
	}

	public String getNameEng5() {
		return nameEng5;
	}

	public void setNameEng5(String nameEng5) {
		this.nameEng5 = nameEng5;
	}

	public String getRegex1() {
		return regex1;
	}

	public void setRegex1(String regex1) {
		this.regex1 = regex1;
	}

	public String getRegex2() {
		return regex2;
	}

	public void setRegex2(String regex2) {
		this.regex2 = regex2;
	}

	public String getRegex3() {
		return regex3;
	}

	public void setRegex3(String regex3) {
		this.regex3 = regex3;
	}

	public String getRegex4() {
		return regex4;
	}

	public void setRegex4(String regex4) {
		this.regex4 = regex4;
	}

	public String getRegex5() {
		return regex5;
	}

	public void setRegex5(String regex5) {
		this.regex5 = regex5;
	}

	public CurrencyModel getAmountCurrency() {
		return amountCurrency;
	}

	public void setAmountCurrency(CurrencyModel amountCurrency) {
		this.amountCurrency = amountCurrency;
	}

	public InstitutionCategoryModel getInstitutionCategory() {
		return institutionCategory;
	}

	public void setInstitutionCategory(InstitutionCategoryModel institutionCategory) {
		this.institutionCategory = institutionCategory;
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

	public String getType1() {
		return type1;
	}

	public void setType1(String type1) {
		this.type1 = type1;
	}

	public String getType2() {
		return type2;
	}

	public void setType2(String type2) {
		this.type2 = type2;
	}

	public String getType3() {
		return type3;
	}

	public void setType3(String type3) {
		this.type3 = type3;
	}

	public String getType4() {
		return type4;
	}

	public void setType4(String type4) {
		this.type4 = type4;
	}

	public String getType5() {
		return type5;
	}

	public void setType5(String type5) {
		this.type5 = type5;
	}

	public String getTypeParameter1() {
		return typeParameter1;
	}

	public void setTypeParameter1(String typeParameter1) {
		this.typeParameter1 = typeParameter1;
	}

	public String getTypeParameter2() {
		return typeParameter2;
	}

	public void setTypeParameter2(String typeParameter2) {
		this.typeParameter2 = typeParameter2;
	}

	public String getTypeParameter3() {
		return typeParameter3;
	}

	public void setTypeParameter3(String typeParameter3) {
		this.typeParameter3 = typeParameter3;
	}

	public String getTypeParameter4() {
		return typeParameter4;
	}

	public void setTypeParameter4(String typeParameter4) {
		this.typeParameter4 = typeParameter4;
	}

	public String getTypeParameter5() {
		return typeParameter5;
	}

	public void setTypeParameter5(String typeParameter5) {
		this.typeParameter5 = typeParameter5;
	}

	public String getTypeParameterDataMap1() {
		return typeParameterDataMap1;
	}

	public void setTypeParameterDataMap1(String typeParameterDataMap1) {
		this.typeParameterDataMap1 = typeParameterDataMap1;
	}

	public String getTypeParameterDataMap2() {
		return typeParameterDataMap2;
	}

	public void setTypeParameterDataMap2(String typeParameterDataMap2) {
		this.typeParameterDataMap2 = typeParameterDataMap2;
	}

	public String getTypeParameterDataMap3() {
		return typeParameterDataMap3;
	}

	public void setTypeParameterDataMap3(String typeParameterDataMap3) {
		this.typeParameterDataMap3 = typeParameterDataMap3;
	}

	public String getTypeParameterDataMap4() {
		return typeParameterDataMap4;
	}

	public void setTypeParameterDataMap4(String typeParameterDataMap4) {
		this.typeParameterDataMap4 = typeParameterDataMap4;
	}

	public String getTypeParameterDataMap5() {
		return typeParameterDataMap5;
	}

	public void setTypeParameterDataMap5(String typeParameterDataMap5) {
		this.typeParameterDataMap5 = typeParameterDataMap5;
	}

	public String getNameEng() {
		return nameEng;
	}

	public void setNameEng(String nameEng) {
		this.nameEng = nameEng;
	}

}
