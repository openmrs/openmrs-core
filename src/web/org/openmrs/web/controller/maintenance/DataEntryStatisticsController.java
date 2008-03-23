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
package org.openmrs.web.controller.maintenance;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DataEntryStatistic;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.DataTable;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class DataEntryStatisticsController extends SimpleFormController {

	protected final Log log = LogFactory.getLog(getClass());

	public class StatisticsCommand {
		private Date fromDate;
		private Date toDate;
		private DataTable table;
		private String encUserColumn;
		private String orderUserColumn;
		private String groupBy;
		private Boolean hideAverageObs = false;
		
		public StatisticsCommand() { }
		public Date getFromDate() {
			return fromDate;
		}
		public void setFromDate(Date fromDate) {
			this.fromDate = fromDate;
		}
		public DataTable getTable() {
			return table;
		}
		public void setTable(DataTable table) {
			this.table = table;
		}
		public Date getToDate() {
			return toDate;
		}
		public void setToDate(Date toDate) {
			this.toDate = toDate;
		}
		public String getEncUserColumn() {
			return encUserColumn;
		}
		public void setEncUserColumn(String encUserColumn) {
			this.encUserColumn = encUserColumn;
		}
		public String getOrderUserColumn() {
			return orderUserColumn;
		}
		public void setOrderUserColumn(String orderUserColumn) {
			this.orderUserColumn = orderUserColumn;
		}
		public String getGroupBy() {
			return groupBy;
		}
		public void setGroupBy(String groupBy) {
			this.groupBy = groupBy;
		}
		public Boolean getHideAverageObs() {
        	return hideAverageObs;
        }
		public void setHideAverageObs(Boolean hideAverageObs) {
        	this.hideAverageObs = hideAverageObs;
        }
	}
	
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(OpenmrsUtil.getDateFormat(), true, 10));
	}

	
	protected Object formBackingObject(HttpServletRequest request) throws ServletException { 
		StatisticsCommand ret = new StatisticsCommand();
		Calendar c = new GregorianCalendar();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		ret.setFromDate(c.getTime());
		ret.setToDate(null);
		
		Date toDateToUse = OpenmrsUtil.lastSecondOfDay(ret.getToDate());
		String encUserColumn = ret.getEncUserColumn();
		String orderUserColumn = ret.getOrderUserColumn();
		List<DataEntryStatistic> stats = Context.getAdministrationService().getDataEntryStatistics(ret.getFromDate(), toDateToUse, encUserColumn, orderUserColumn, ret.getGroupBy());
		DataTable table = DataEntryStatistic.tableByUserAndType(stats, ret.getHideAverageObs());
		ret.setTable(table);
		
		return ret;
	}
	
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors) throws Exception {
		StatisticsCommand command = (StatisticsCommand) commandObj;
		Date toDateToUse = OpenmrsUtil.lastSecondOfDay(command.getToDate());
		String encUserColumn = command.getEncUserColumn();
		String orderUserColumn = command.getOrderUserColumn();
		List<DataEntryStatistic> stats = Context.getAdministrationService().getDataEntryStatistics(command.getFromDate(), toDateToUse, encUserColumn, orderUserColumn, command.getGroupBy());
		DataTable table = DataEntryStatistic.tableByUserAndType(stats, command.getHideAverageObs());
		command.setTable(table);
		return showForm(request, response, errors);
	}

}
