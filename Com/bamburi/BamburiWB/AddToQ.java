package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class AddToQ
{
	String queue;
	String prodType;
	VtiUserExit vUE;
	String truck;
	String orderNum;
	boolean isSalesOrder;
	String arrTime;
	String arrDate;
	long interval;
	String driver;
	FormatUtilities fu = new FormatUtilities();
	Date qDate = new Date();
	Date qTime = new Date();
	Date now = new Date();
	String serialDate = DateFormatter.format("yyyyMMdd", now);
	String serialTime = DateFormatter.format("HHmmss", now);
	
	final String status = "ASSIGNED";
	
	public AddToQ (VtiUserExit vUE, String truck, String orderNum, boolean isSalesOrder, String queue, String prodType, String arrTime, String arrDate, long interval, String driver)
	{
		
		this.vUE = vUE;
		this.truck = truck ;
		this.orderNum = orderNum;
		this.isSalesOrder = isSalesOrder;
		this.queue = queue;
		this.prodType = prodType;
		this.arrTime = arrTime;
		this.arrDate = arrDate;
		this.interval = interval;
		this.driver = driver;
	}
	
	public void addTruck2Q () throws VtiExitException
	{
		//getQPos();
				   
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");
		
		VtiExitLdbTableRow addTruck  = queueLdbTable.newRow();
	
		long qRef = vUE.getNextNumberFromNumberRange("YSWB_Q");	
		
		addTruck.setFieldValue("SERVERGRP",vUE.getServerGroup());
		addTruck.setFieldValue("SERVERID",vUE.getServerId());
		addTruck.setFieldValue("Q_DATE", serialDate);
		addTruck.setFieldValue("Q_TIME",serialTime);
		addTruck.setFieldValue("Q_QUEUE",queue);
		addTruck.setFieldValue("PROD_TYPE",prodType);		
		addTruck.setFieldValue("Q_REGNO",truck);
		addTruck.setFieldValue("Q_DRIVER", getDriver());
		addTruck.setFieldValue("Q_REF",orderNum);
		addTruck.setFieldValue("Q_STATUS","ASSIGNED");
		addTruck.setFieldValue("VTI_REF",qRef);
		addTruck.setFieldValue("INSP_DATE",serialDate);
		addTruck.setFieldValue("Q_POSITION",interval);
		
		
		try
		{
			queueLdbTable.saveRow(addTruck);
		}
		catch ( VtiExitException ee)
		{
			vUE.logError("Error refreshing the table.", ee);
		}
		
		VtiExitLdbSelectCriterion [] qSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
					new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, truck),
						new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.NE_OPERATOR, Long.toString(qRef)),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
		
		for(int iQ = 0; iQ < qTLdbRows.length;iQ++)
		{
			qTLdbRows[iQ].setFieldValue("DEL_IND","X");
			qTLdbRows[iQ].setFieldValue("TIMESTAMP","");
			
			try
			{
				queueLdbTable.saveRow(qTLdbRows[iQ]);
			}
			catch ( VtiExitException ee)
			{
				vUE.logError("Error archiving queue.", ee);
			}
		}
		
	}
	
	public void addTruck2Q (String altStatus) throws VtiExitException
	{
		//getQPos();
				   
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");
		
		VtiExitLdbTableRow addTruck  = queueLdbTable.newRow();
	
		long qRef = vUE.getNextNumberFromNumberRange("YSWB_Q");		
		
		addTruck.setFieldValue("SERVERGRP",vUE.getServerGroup());
		addTruck.setFieldValue("SERVERID",vUE.getServerId());
		addTruck.setFieldValue("Q_DATE", serialDate);
		addTruck.setFieldValue("Q_TIME",serialTime);
		addTruck.setFieldValue("Q_QUEUE",queue);
		addTruck.setFieldValue("PROD_TYPE",prodType);
		addTruck.setFieldValue("Q_REGNO",truck);
		addTruck.setFieldValue("Q_DRIVER", getDriver());
		addTruck.setFieldValue("Q_REF",orderNum);
		addTruck.setFieldValue("Q_STATUS",altStatus);
		addTruck.setFieldValue("Q_POSITION",interval);
		addTruck.setFieldValue("VTI_REF",qRef);
		addTruck.setFieldValue("INSP_DATE",serialDate);		
		
		try
		{
			queueLdbTable.saveRow(addTruck);
		}
		catch ( VtiExitException ee)
		{
			vUE.logError("Error adding truck in the queue the table.", ee);
		}
		
		VtiExitLdbSelectCriterion [] qSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
					new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, truck),
						new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.NE_OPERATOR, Long.toString(qRef)),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
		
		for(int iQ = 0; iQ < qTLdbRows.length;iQ++)
		{
			qTLdbRows[iQ].setFieldValue("DEL_IND","X");
			qTLdbRows[iQ].setFieldValue("TIMESTAMP","");
			
			try
			{
				queueLdbTable.saveRow(qTLdbRows[iQ]);
			}
			catch ( VtiExitException ee)
			{
				vUE.logError("Error archiving queue.", ee);
			}
		}
	}
	
	private String getDriver() throws VtiExitException
	{
		String dr = "";
		
		dr = driver;
		
		return dr;
	}

}
