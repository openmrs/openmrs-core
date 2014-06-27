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
package org.openmrs;

import java.util.Locale;

/**
 * @since 1.10
 */
public class SimpleDosingInstructions implements DosingInstructions {

    private DrugOrder.DosingType type = DrugOrder.DosingType.SIMPLE;
    private Double dose;
    private Concept doseUnits;
    private Concept route;
    private OrderFrequency frequency;
    private Double duration;
    private Concept durationUnits;
    private Boolean asNeeded;
    private String asNeededCondition;
    private String administrationInstructions;

    /**
     * @see DosingInstructions#getType()
     */
    @Override
    public DrugOrder.DosingType getType() {
        return this.type;
    }

    /**
     * @see DosingInstructions#getDosingInstructionsAsString(java.util.Locale)
     */
    @Override
    public String getDosingInstructionsAsString(Locale locale) {
        StringBuilder dosingInstructions=new StringBuilder();
        dosingInstructions.append(this.dose);
        dosingInstructions.append(" ");
        dosingInstructions.append(this.doseUnits.getName(locale).getName());
        dosingInstructions.append(" ");
        dosingInstructions.append(this.route.getName(locale).getName());
        dosingInstructions.append(" ");
        dosingInstructions.append(this.frequency);
        if (duration != null) {
            dosingInstructions.append(" ");
            dosingInstructions.append(this.duration);
            dosingInstructions.append(" ");
            dosingInstructions.append(this.durationUnits.getName(locale).getName());
        }
        if (this.asNeeded) {
            dosingInstructions.append(" ");
            dosingInstructions.append("PRN");
            if (this.asNeededCondition != null) {
                dosingInstructions.append(" ");
                dosingInstructions.append(this.asNeededCondition);
            }
        }
        if (this.administrationInstructions != null) {
            dosingInstructions.append(" ");
            dosingInstructions.append(this.administrationInstructions);
        }
        return dosingInstructions.toString();
    }

    /**
     * @see DosingInstructions#setDosingInstructions(DrugOrder)
     */
    @Override
    public void setDosingInstructions(DrugOrder order) {
        order.setDosingType(this.type);
        order.setDose(this.dose);
        order.setDoseUnits(this.doseUnits);
        order.setRoute(this.route);
        order.setFrequency(this.frequency);
        order.setDuration(this.duration);
        order.setDurationUnits(this.durationUnits);
        order.setAsNeeded(this.asNeeded);
        order.setAsNeededCondition(this.asNeededCondition);
        order.setDosingInstructions(this.administrationInstructions);
    }

    /**
     * @see DosingInstructions#getDosingInstructions(DrugOrder)
     */
    @Override
    public DosingInstructions getDosingInstructions(DrugOrder order) throws Exception {
        if(this.getType()!=order.getDosingType()) {
            throw new Exception("Dosing type of drug order is mismatched.Expected:"+this.getType()+"but received:"+order.getDosingType());
        }
        SimpleDosingInstructions di=new SimpleDosingInstructions();
        di.setDose(order.getDose());
        di.setDoseUnits(order.getDoseUnits());
        di.setRoute(order.getRoute());
        di.setFrequency(order.getFrequency());
        di.setDuration(order.getDuration());
        di.setDurationUnits(order.getDurationUnits());
        di.setAsNeeded(order.getAsNeeded());
        di.setAsNeededCondition(order.getAsNeededCondition());
        di.setAdministrationInstructions(order.getInstructions());
        return di;
    }

    public Double getDose() {
        return dose;
    }

    public void setDose(Double dose) {
        this.dose = dose;
    }

    public Concept getDoseUnits() {
        return doseUnits;
    }

    public void setDoseUnits(Concept doseUnits) {
        this.doseUnits = doseUnits;
    }

    public Concept getRoute() {
        return route;
    }

    public void setRoute(Concept route) {
        this.route = route;
    }

    public OrderFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(OrderFrequency frequency) {
        this.frequency = frequency;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Concept getDurationUnits() {
        return durationUnits;
    }

    public void setDurationUnits(Concept durationUnits) {
        this.durationUnits = durationUnits;
    }

    public Boolean getAsNeeded() {
        return asNeeded;
    }

    public void setAsNeeded(Boolean asNeeded) {
        this.asNeeded = asNeeded;
    }

    public String getAsNeededCondition() {
        return asNeededCondition;
    }

    public void setAsNeededCondition(String asNeededCondition) {
        this.asNeededCondition = asNeededCondition;
    }

    public String getAdministrationInstructions() {
        return administrationInstructions;
    }

    public void setAdministrationInstructions(String administrationInstructions) {
        this.administrationInstructions = administrationInstructions;
    }
}
