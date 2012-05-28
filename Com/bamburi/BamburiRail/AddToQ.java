package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class AddToQ
{
	String queue;
	VtiUserExit vUE;
	String truck;
	String orderNum;
	boolean isSalesOrder;
	String arrTime;
	String arrDate;
	int interval;
	String driver;
	FormatUtilities fu = new FormatUtilities();
	Date qDate = new Date();
	Date qTime = new Date();
	Date now = new Date();
	String serialDate = DateFormatter.format("yyyyMMdd", now);
	String serialTime = DateFormatter.format("HHmmss", now);
	
	final String status = "ASSIGNED";
	
	public AddToQ (VtiUserExit vUE, String truck, String orderNum, boolean isSalesOrder, String queue, String arrTime, String arrDate, int interval, String driver)
	{
		
		this.vUE = vUE;
		this.truck = truck ;
		this.orderNum = orderNum;
		this.isSalesOrder = isSalesOrder;
		this.queue = queue;
		this.arrTime = arrTime;
		this.arrDate = arrDate;
		this.interval = interval;
		this.driver = driver;
	}
	
	public void addTruck2Q () throws VtiExitException
	{
		getQPos();
				   
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");
		
		VtiExitLdbTableRow addTruck  = queueLdbTable.newRow();
		//Calibrate date going into SAP
		String monthFill = "";
		String minFill = "";
		String dayFill = "";
		String hourFill = "";
		int incM = fu.getMonth()+1;
		int incY = 0;
		
		if(incM == 13)
		{
			incY = 1;
			incM = -11;
		}
		else
		{
			incM = 1;
			incY = 0;
		}

		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, queue),
							new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, status),
			};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
		
		if(qTLdbRows.length > 0)
		{
			incM = 0;
			incY = 0;
		}
		
		if(Integer.toString(fu.getMonth()+incM).length() == 1)
			monthFill = "0";
		if(Integer.toString(fu.getDay()).length() == 1)
			dayFill = "0";
		if(Integer.toString(fu.getMin()).length() == 1)
			minFill = "0";
		if(Integer.toString(fu.getHour()).length() == 1)
			hourFill = "0";
		
		//Calibration complete
	
		long qRef = vUE.getNextNumberFromNumberRange("YSWB_Q");
		
		String date2Ldb = Integer.toString(fu.getYear() + incY) + monthFill + Integer.toString(fu.getMonth()+ incM) + dayFill + Integer.toString(fu.getDay());
		String time2Ldb = hourFill + Integer.toString(fu.getHour()) + minFill + Integer.toString(fu.getMin())  + "00";
		addTruck.setFieldValue("SERVERGRP",vUE.getServerGroup());
		addTruck.setFieldValue("SERVERID",vUE.getServerId());
		addTruck.setFieldValue("Q_DATE", date2Ldb);
		addTruck.setFieldValue("Q_TIME",time2Ldb);
		addTruck.setFieldValue("Q_QUEUE",queue);
		addTruck.setFieldValue("Q_REGNO",truck);
		addTruck.setFieldValue("Q_DRIVER", getDriver());
		addTruck.setFieldValue("Q_REF",orderNum);
		addTruck.setFieldValue("Q_STATUS","ASSIGNED");
		addTruck.setFieldValue("VTI_REF",qRef);
		addTruck.setFieldValue("INSP_DATE",date2Ldb);		
		
		try
		{
			queueLdbTable.saveRow(addTruck);
		}
		catch ( VtiExitException ee)
		{
			vUE.logError("Error refreshing the table.", ee);
		}
	}
	
	private void getQPos() throws VtiExitException
	{
	
		String dt = "";
		String t = "";
		String d = "";
		
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");
		
		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, queue),
							new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, status),
			};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
		
		if(qTLdbRows.length > 0)
		{
			t = qTLdbRows[qTLdbRows.length - 1].getFieldValue("Q_TIME"); 
			d = qTLdbRows[qTLdbRows.length - 1].getFieldValue("Q_DATE");
			
			int compDA = Integer.parseInt(d) + Integer.parseInt(t);
			int compDB = Integer.parseInt(serialDate) + Integer.parseInt(serialTime);
		
			if(compDA < compDB)
			{
				d = serialDate;
				t = serialTime;
			}
			
			fu.setCalendar(d,t).add(Calendar.MINUTE,getInterval());
			fu.upDate();
			}
			else
			{
				fu.setCalendar().add(Calendar.MINUTE, getInterval());
				fu.upDate();
			};
	}
	
	private String getDriver() throws VtiExitException
	{
		String dr = "";
		
		dr = driver;
		
		return dr;
	}

	private int getInterval() throws VtiExitException
	{
		return interval;
	}
	
	public String getDate() throws VtiExitException
	{
		
		String monthFill = "";
		String dayFill = "";
		int incM = fu.getMonth()+1;
		int incY = 0;
		
		if(incM == 13)
		{
			incY = 1;
			incM = -11;
		}
		else
		{
			incM = 1;
			incY = 0;
		}
		
		if(Integer.toString(fu.getDay()).length() == 1)
			dayFill = "0";
		if(Integer.toString(fu.getMonth()+1).length() == 1)
			monthFill = "0";

		
		return Integer.toString(fu.getYear()) + monthFill + Integer.toString(fu.getMonth()) + dayFill + Integer.toString(fu.getDay());
	}
}
