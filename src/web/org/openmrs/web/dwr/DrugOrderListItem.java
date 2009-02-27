/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.dwr;

import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.util.OpenmrsUtil;

public class DrugOrderListItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer orderId;
	
	private Integer orderTypeId;
	
	private Integer conceptId;
	
	private String conceptName;
	
	private String instructions;
	
	private String startDate;
	
	private String autoExpireDate;
	
	private Integer encounterId;
	
	private Integer ordererId;
	
	private Boolean discontinued;
	
	private Integer discontinuerId;
	
	private String discontinuedDate;
	
	private Integer discontinueReason;
	
	private Integer drugId;
	
	private String drugName;
	
	private Double dose;
	
	private String units;
	
	private String frequency;
	
	private Boolean prn;
	
	private Boolean complex;
	
	private Integer quantity;
	
	private Boolean voided;
	
	private Integer voiderId;
	
	private String voidedDate;
	
	private String voidReason;
	
	private Integer creatorId;
	
	private String createdDate;
	
	private Integer drugSetId;
	
	private String drugSetLabel;
	
	public DrugOrderListItem() {
	}
	
	public DrugOrderListItem(DrugOrder drugOrder) {
		orderId = drugOrder.getOrderId();
		if (drugOrder.getOrderType() != null)
			orderTypeId = drugOrder.getOrderType().getOrderTypeId();
		if (drugOrder.getConcept() != null) {
			conceptId = drugOrder.getConcept().getConceptId();
			conceptName = drugOrder.getConcept().getName().getName();
		}
		instructions = drugOrder.getInstructions();
		
		SimpleDateFormat sdf = OpenmrsUtil.getDateFormat();
		
		if (drugOrder.getStartDate() != null)
			startDate = sdf.format(drugOrder.getStartDate());
		if (drugOrder.getAutoExpireDate() != null)
			autoExpireDate = sdf.format(drugOrder.getAutoExpireDate());
		if (drugOrder.getEncounter() != null)
			encounterId = drugOrder.getEncounter().getEncounterId();
		if (drugOrder.getOrderer() != null)
			ordererId = drugOrder.getOrderer().getUserId();
		discontinued = drugOrder.getDiscontinued();
		if (drugOrder.getDiscontinuedBy() != null)
			discontinuerId = drugOrder.getDiscontinuedBy().getUserId();
		if (drugOrder.getDiscontinuedDate() != null)
			discontinuedDate = sdf.format(drugOrder.getDiscontinuedDate());
		if (drugOrder.getDiscontinuedReason() != null)
			discontinueReason = drugOrder.getDiscontinuedReason().getConceptId();
		if (drugOrder.getDrug() != null)
			drugId = drugOrder.getDrug().getDrugId();
		if (drugOrder.getDrug() != null)
			drugName = drugOrder.getDrug().getName();
		dose = drugOrder.getDose();
		units = drugOrder.getUnits();
		frequency = drugOrder.getFrequency();
		prn = drugOrder.getPrn();
		complex = drugOrder.getComplex();
		quantity = drugOrder.getQuantity();
		voided = drugOrder.getVoided();
		if (drugOrder.getVoidedBy() != null)
			voiderId = drugOrder.getVoidedBy().getUserId();
		if (drugOrder.getDateVoided() != null)
			voidedDate = sdf.format(drugOrder.getDateVoided());
		voidReason = drugOrder.getVoidReason();
		if (drugOrder.getCreator() != null)
			creatorId = drugOrder.getCreator().getUserId();
		if (drugOrder.getDateCreated() != null)
			createdDate = sdf.format(drugOrder.getDateCreated());
	}
	
	/**
	 * @return Returns the complex.
	 */
	public Boolean getComplex() {
		return complex;
	}
	
	/**
	 * @param complex The complex to set.
	 */
	public void setComplex(Boolean complex) {
		this.complex = complex;
	}
	
	/**
	 * @return Returns the conceptId.
	 */
	public Integer getConceptId() {
		return conceptId;
	}
	
	/**
	 * @param conceptId The conceptId to set.
	 */
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	/**
	 * @return Returns the creatorId.
	 */
	public Integer getCreatorId() {
		return creatorId;
	}
	
	/**
	 * @param creatorId The creatorId to set.
	 */
	public void setCreatorId(Integer creatorId) {
		this.creatorId = creatorId;
	}
	
	/**
	 * @return Returns the discontinued.
	 */
	public Boolean getDiscontinued() {
		return discontinued;
	}
	
	/**
	 * @param discontinued The discontinued to set.
	 */
	public void setDiscontinued(Boolean discontinued) {
		this.discontinued = discontinued;
	}
	
	/**
	 * @return Returns the discontinueReason.
	 */
	public Integer getDiscontinueReason() {
		return discontinueReason;
	}
	
	/**
	 * @param discontinueReason The discontinueReason to set.
	 */
	public void setDiscontinueReason(Integer discontinueReason) {
		this.discontinueReason = discontinueReason;
	}
	
	/**
	 * @return Returns the discontinuerId.
	 */
	public Integer getDiscontinuerId() {
		return discontinuerId;
	}
	
	/**
	 * @param discontinuerId The discontinuerId to set.
	 */
	public void setDiscontinuerId(Integer discontinuerId) {
		this.discontinuerId = discontinuerId;
	}
	
	/**
	 * @return Returns the dose.
	 */
	public Double getDose() {
		return dose;
	}
	
	/**
	 * @param dose The dose to set.
	 */
	public void setDose(Double dose) {
		this.dose = dose;
	}
	
	/**
	 * @return Returns the drugId.
	 */
	public Integer getDrugId() {
		return drugId;
	}
	
	/**
	 * @param drugId The drugId to set.
	 */
	public void setDrugId(Integer drugId) {
		this.drugId = drugId;
	}
	
	/**
	 * @return Returns the drugName.
	 */
	public String getDrugName() {
		return drugName;
	}
	
	/**
	 * @param drugName The drugName to set.
	 */
	public void setDrugName(String drugName) {
		this.drugName = drugName;
	}
	
	/**
	 * @return Returns the encounterId.
	 */
	public Integer getEncounterId() {
		return encounterId;
	}
	
	/**
	 * @param encounterId The encounterId to set.
	 */
	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}
	
	/**
	 * @return Returns the frequency.
	 */
	public String getFrequency() {
		return frequency;
	}
	
	/**
	 * @param frequency The frequency to set.
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * @return Returns the instructions.
	 */
	public String getInstructions() {
		if (instructions == null)
			return "";
		else
			return instructions;
	}
	
	/**
	 * @param instructions The instructions to set.
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	
	/**
	 * @return Returns the ordererId.
	 */
	public Integer getOrdererId() {
		return ordererId;
	}
	
	/**
	 * @param ordererId The ordererId to set.
	 */
	public void setOrdererId(Integer ordererId) {
		this.ordererId = ordererId;
	}
	
	/**
	 * @return Returns the orderId.
	 */
	public Integer getOrderId() {
		return orderId;
	}
	
	/**
	 * @param orderId The orderId to set.
	 */
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	
	/**
	 * @return Returns the orderTypeId.
	 */
	public Integer getOrderTypeId() {
		return orderTypeId;
	}
	
	/**
	 * @param orderTypeId The orderTypeId to set.
	 */
	public void setOrderTypeId(Integer orderTypeId) {
		this.orderTypeId = orderTypeId;
	}
	
	/**
	 * @return Returns the prn.
	 */
	public Boolean getPrn() {
		return prn;
	}
	
	/**
	 * @param prn The prn to set.
	 */
	public void setPrn(Boolean prn) {
		this.prn = prn;
	}
	
	/**
	 * @return Returns the quantity.
	 */
	public Integer getQuantity() {
		return quantity;
	}
	
	/**
	 * @param quantity The quantity to set.
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	/**
	 * @return Returns the units.
	 */
	public String getUnits() {
		return units;
	}
	
	/**
	 * @param units The units to set.
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	
	/**
	 * @return Returns the voided.
	 */
	public Boolean getVoided() {
		return voided;
	}
	
	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * @return Returns the voiderId.
	 */
	public Integer getVoiderId() {
		return voiderId;
	}
	
	/**
	 * @param voiderId The voiderId to set.
	 */
	public void setVoiderId(Integer voiderId) {
		this.voiderId = voiderId;
	}
	
	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}
	
	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	/**
	 * @return Returns the autoExpireDate.
	 */
	public String getAutoExpireDate() {
		if (autoExpireDate == null)
			return "";
		else
			return autoExpireDate;
	}
	
	/**
	 * @param autoExpireDate The autoExpireDate to set.
	 */
	public void setAutoExpireDate(String autoExpireDate) {
		this.autoExpireDate = autoExpireDate;
	}
	
	/**
	 * @return Returns the createdDate.
	 */
	public String getCreatedDate() {
		return createdDate;
	}
	
	/**
	 * @param createdDate The createdDate to set.
	 */
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	
	/**
	 * @return Returns the discontinuedDate.
	 */
	public String getDiscontinuedDate() {
		if (discontinuedDate == null)
			return "";
		else
			return discontinuedDate;
	}
	
	/**
	 * @param discontinuedDate The discontinuedDate to set.
	 */
	public void setDiscontinuedDate(String discontinuedDate) {
		this.discontinuedDate = discontinuedDate;
	}
	
	/**
	 * @return Returns the startDate.
	 */
	public String getStartDate() {
		return startDate;
	}
	
	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * @return Returns the voidedDate.
	 */
	public String getVoidedDate() {
		return voidedDate;
	}
	
	/**
	 * @param voidedDate The voidedDate to set.
	 */
	public void setVoidedDate(String voidedDate) {
		this.voidedDate = voidedDate;
	}
	
	/**
	 * @return Returns the drugSetId.
	 */
	public Integer getDrugSetId() {
		return drugSetId;
	}
	
	/**
	 * @param drugSetId The drugSetId to set.
	 */
	public void setDrugSetId(Integer drugSetId) {
		this.drugSetId = drugSetId;
	}
	
	/**
	 * @return Returns the drugSetLabel.
	 */
	public String getDrugSetLabel() {
		return drugSetLabel;
	}
	
	/**
	 * @param drugSetLabel The drugSetLabel to set.
	 */
	public void setDrugSetLabel(String drugSetLabel) {
		this.drugSetLabel = drugSetLabel;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		boolean isSame = false;
		
		if (other instanceof DrugOrderListItem) {
			DrugOrderListItem otherItem = (DrugOrderListItem) other;
			if (otherItem != null) {
				if (otherItem.getOrderId().equals(this.orderId))
					isSame = true;
			}
		}
		
		return isSame;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.orderId;
	}
	
	public String getConceptName() {
		return conceptName;
	}
	
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}
}
