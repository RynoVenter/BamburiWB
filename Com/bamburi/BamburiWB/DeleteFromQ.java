package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class DeleteFromQ extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		VtiUserExitScreenField scrWFQRefresh = getScreenField("Q_REFRESH");
		if(scrWFQRefresh.getFieldValue() == null)
			return new VtiUserExitResult(999, "Queue Refresh did not load properly.");
		
		VtiUserExitScreenTable qList = getScreenTable("TB_QUEUE");
		
		if(qList == null) return new VtiUserExitResult(999,"Table TB_QUEUE failed to load.");
		
		VtiUserExitScreenTableRow qListRow = qList.getActiveRow();
		
		FormatUtilities fu = new FormatUtilities();
			
		String preQ = qListRow.getFieldValue("Q_QUEUE");
		String nProdType = qListRow.getFieldValue("Q_PROD_TYPE");
		long newQPos = qListRow.getLongFieldValue("Q_NEW_POS");
		String driver = qListRow.getFieldValue("Q_DRIVER");
		String regNo = qListRow.getFieldValue("Q_REGNO");
		String qRef = qListRow.getFieldValue("Q_REF");
		String qStatus = qListRow.getFieldValue("Q_STATUS");
		String gateP = "";
		String vRef = "";
		String erroMsg = "Truck deleted.";
		Date currNow = new Date();		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		DBCalls dbCall = new DBCalls();
		
		final String status = "ASSIGNED";
		
		if(!qStatus.equalsIgnoreCase(status))
			if(!qStatus.equalsIgnoreCase("REJECTED"))
			   return new VtiUserExitResult(999,"It is not possible to remove this truck from the queue. Reject the weighin and try again.");
		
		VtiExitLdbTable qChngLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable soLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable icLdbTable = getLocalDatabaseTable("YSWB_IC_HEADER");
		VtiExitLdbTable poLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable gatepassLdb = getLocalDatabaseTable("YSWB_GATEPASS");
		VtiExitLdbTable retailSalesLdb = getLocalDatabaseTable("YSWB_RETAILSALES");
		
		if(qChngLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_QUEUE failed to load.");
		if(regLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_REGISTER failed to load.");
		if(soLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_SO_HEADER failed to load.");
		if(icLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_IC_HEADER failed to load.");
		if(poLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_PO_HEADER failed to load.");
		if(statusLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_STATUS failed to load.");
		if(retailSalesLdb == null) return new VtiUserExitResult(999,"LDB Table YSWB_RETAILSALES failed to load.");

		VtiExitLdbSelectCriterion [] qChngSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, preQ),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, regNo),
								new VtiExitLdbSelectCondition("Q_DRIVER", VtiExitLdbSelectCondition.EQ_OPERATOR, driver),
									new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
		};
      
		VtiExitLdbSelectConditionGroup qChngSelCondGrp = new VtiExitLdbSelectConditionGroup(qChngSelConds, true);
		VtiExitLdbTableRow[] qChngTLdbRows = qChngLdbTable.getMatchingRows(qChngSelCondGrp);
		
		
		if(qChngTLdbRows.length == 0)
			return new VtiUserExitResult(999,"Queue was not changed.No matching item for truck " + regNo + " being driven by " + driver + " on Q " + preQ + " with order " + qRef + ".");
		
		int nTime = 0;
		int nDate = 0;

		
		nDate = qChngTLdbRows[0].getIntegerFieldValue("Q_DATE");
		nTime = qChngTLdbRows[0].getIntegerFieldValue("Q_TIME");
		qChngTLdbRows[0].setFieldValue("TIMESTAMP", "");
		qChngTLdbRows[0].setFieldValue("DEL_IND", "X");
		
		
		try
		{
			qChngLdbTable.saveRow(qChngTLdbRows[0]);
		}
		catch(VtiExitException ee)
		{
			Log.error("Truck was not deleted", ee);
			return new VtiUserExitResult(999,"The truck was not removed from the row, refresh the screen and try again.");
		}
		
		boolean outbound = false;
		
		VtiExitLdbSelectCriterion [] confSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, "TQUEUE"),
							new VtiExitLdbSelectCondition("KEYVAL4", VtiExitLdbSelectCondition.EQ_OPERATOR, preQ),
			};
			
		VtiExitLdbSelectConditionGroup confSelCondGrp = new VtiExitLdbSelectConditionGroup(confSelConds, true);
		VtiExitLdbTableRow[] confTLdbRows = confLdbTable.getMatchingRows(confSelCondGrp);

		if(confTLdbRows.length == 0)
			return new VtiUserExitResult(999,"No queue config obtained.");
		
		//Determine orderType
		String orderType = "";
		
		VtiExitLdbSelectCriterion [] soSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, regNo),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 1"),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NS_OPERATOR, "0"),
											
			};
			
			VtiExitLdbSelectConditionGroup soSelCondGrp = new VtiExitLdbSelectConditionGroup(soSelConds, true);
			VtiExitLdbTableRow[] soTLdbRows = soLdbTable.getMatchingRows(soSelCondGrp);
			
			if(soTLdbRows.length > 0)
				orderType = "VBELN";
			
			VtiExitLdbSelectCriterion [] stPOSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, regNo),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASSED"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 1"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REGISTERED"),
			};
			
			VtiExitLdbSelectConditionGroup stPOSelCondGrp = new VtiExitLdbSelectConditionGroup(stPOSelConds, true);
			VtiExitLdbTableRow[] stPOTLdbRows = statusLdbTable.getMatchingRows(stPOSelCondGrp);
			
			if(stPOTLdbRows.length > 0)
				orderType = "EBELN";
			
			VtiExitLdbSelectCriterion [] stSTOSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASSED"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 1"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REGISTERED"),
			};
			
			VtiExitLdbSelectConditionGroup stSTOSelCondGrp = new VtiExitLdbSelectConditionGroup(stSTOSelConds, true);
			VtiExitLdbTableRow[] stSTOTLdbRows = statusLdbTable.getMatchingRows(stSTOSelCondGrp);
			
			if(stSTOTLdbRows.length > 0)
				orderType = "STOCKTRNF";
			
			VtiExitLdbSelectCriterion [] stICSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASSED"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 1"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
										new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REGISTERED"),
			};
			
			VtiExitLdbSelectConditionGroup stICSelCondGrp = new VtiExitLdbSelectConditionGroup(stICSelConds, true);
			VtiExitLdbTableRow[] stICTLdbRows = statusLdbTable.getMatchingRows(stICSelCondGrp);
			
			if(stICTLdbRows.length > 0)
				orderType = "DELIVDOC";
		
			if(orderType.length() == 0)
			{
				VtiExitLdbSelectCriterion [] sotypSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
											new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
											new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
											new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 1"),
											new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
											new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
											new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
											new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NS_OPERATOR, "0"),
											
				};
			
				VtiExitLdbSelectConditionGroup sotypSelCondGrp = new VtiExitLdbSelectConditionGroup(sotypSelConds, true);
				VtiExitLdbTableRow[] sotypTLdbRows = soLdbTable.getMatchingRows(sotypSelCondGrp);
			
				if(sotypTLdbRows.length > 0)
					orderType = "VBELN";
			
				VtiExitLdbSelectCriterion [] sttypPOSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASSED"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 1"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REGISTERED"),
				};
			
				VtiExitLdbSelectConditionGroup sttypPOSelCondGrp = new VtiExitLdbSelectConditionGroup(sttypPOSelConds, true);
				VtiExitLdbTableRow[] sttypPOTLdbRows = statusLdbTable.getMatchingRows(sttypPOSelCondGrp);
			
				if(sttypPOTLdbRows.length > 0)
					orderType = "EBELN";
			
				VtiExitLdbSelectCriterion [] sttypSTOSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASSED"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 1"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REGISTERED"),
				};
			
				VtiExitLdbSelectConditionGroup sttypSTOSelCondGrp = new VtiExitLdbSelectConditionGroup(sttypSTOSelConds, true);
				VtiExitLdbTableRow[] sttypSTOTLdbRows = statusLdbTable.getMatchingRows(sttypSTOSelCondGrp);
			
				if(sttypSTOTLdbRows.length > 0)
					orderType = "STOCKTRNF";
			
				VtiExitLdbSelectCriterion [] sttypICSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASSED"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 1"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
											new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REGISTERED"),
				};
			
				VtiExitLdbSelectConditionGroup sttypICSelCondGrp = new VtiExitLdbSelectConditionGroup(sttypICSelConds, true);
				VtiExitLdbTableRow[] sttypICTLdbRows = statusLdbTable.getMatchingRows(sttypICSelCondGrp);
			
				if(sttypICTLdbRows.length > 0)
					orderType = "DELIVDOC";
			}
			else if(orderType.length() <= 2)
				return new VtiUserExitResult(999,"The order with the appropriate status not found. Order type not determined.Ensure status of process is assigned.");
			
			VtiExitLdbSelectCriterion [] regSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, regNo),
								new VtiExitLdbSelectCondition("DRIVER", VtiExitLdbSelectCondition.EQ_OPERATOR, driver),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "P"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "W"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "O"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
										new VtiExitLdbSelectCondition(orderType, VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
			};
			
			VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
			VtiExitLdbTableRow[] regTLdbRows = regLdbTable.getMatchingRows(regSelCondGrp);

			if(regTLdbRows.length == 0)
				return new VtiUserExitResult(999,"The associated order can not be found, this truck was not released from the order or the register.");
			
			gateP = regTLdbRows[0].getFieldValue("GATE_PASS");
			vRef = regTLdbRows[0].getFieldValue("VTI_REF");
			
			regTLdbRows[0].setFieldValue("INSPSTATUS", "P");
			regTLdbRows[0].setFieldValue(orderType, "");
			regTLdbRows[0].setFieldValue("TIMESTAMP", "");
			try
			{
				regLdbTable.saveRow(regTLdbRows[0]);
			}
			catch(VtiExitException ee)
			{
				Log.error("Truck was not deleted", ee);
				return new VtiUserExitResult(999,"The truck was not removed from the row, refresh the screen and try again.");
			}

			if(soTLdbRows.length == 1)
			{
				soTLdbRows[0].setFieldValue("TRUCK","");
				soTLdbRows[0].setFieldValue("VTIREF","");
				soTLdbRows[0].setFieldValue("USERID","");
				soTLdbRows[0].setFieldValue("TIMESTAMP", "");
				soTLdbRows[0].setFieldValue("STATUS","NEW");//what if you made it NEW/Post Go Live
				soTLdbRows[0].setFieldValue("RETAIL_ORDER","");
			
				try
				{
					soLdbTable.saveRow(soTLdbRows[0]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Truck was not cleared from the sales order, the order will not be availible collection.", ee);
					return new VtiUserExitResult(999,"Truck was not cleared from the sales order, the order will not be availible collection.");
				}
				
				Log.trace(0,"Order for release - " + soTLdbRows[0].getFieldValue("VBELN"));
				
				if(soTLdbRows[0].getFieldValue("VBELN").startsWith("R"))
				{
					
					
					//Get list of rso related so's
					
						VtiExitLdbSelectCriterion [] rsoSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("REGISTRATION", VtiExitLdbSelectCondition.EQ_OPERATOR, regNo),
										new VtiExitLdbSelectCondition("RETAILSALESORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, soTLdbRows[0].getFieldValue("VBELN")),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
													
						};
			
						VtiExitLdbSelectConditionGroup rsoSelCondGrp = new VtiExitLdbSelectConditionGroup(rsoSelConds, true);
						VtiExitLdbTableRow[] rsoTLdbRows = retailSalesLdb.getMatchingRows(rsoSelCondGrp);
					
						Log.trace(0,"Order for release - " + soTLdbRows[0].getFieldValue("VBELN") + ": Total records " + rsoTLdbRows.length);
					//loop through each record and reset the so
					
					for(int ri = 0;ri < rsoTLdbRows.length;ri++)
					{
							
						VtiExitLdbSelectCriterion [] rsoiSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, rsoTLdbRows[ri].getFieldValue("REGISTRATION")),
										new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, rsoTLdbRows[ri].getFieldValue("VBELN")),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")		
														
						};
						
						VtiExitLdbSelectConditionGroup rsoiSelCondGrp = new VtiExitLdbSelectConditionGroup(rsoiSelConds, true);
						VtiExitLdbTableRow[] rsoiTLdbRows = soLdbTable.getMatchingRows(rsoiSelCondGrp);

						rsoiTLdbRows[0].setFieldValue("TRUCK","");
						rsoiTLdbRows[0].setFieldValue("VTIREF","");
						rsoiTLdbRows[0].setFieldValue("USERID","");
						rsoiTLdbRows[0].setFieldValue("TIMESTAMP", "");
						rsoiTLdbRows[0].setFieldValue("STATUS","NEW");
						rsoiTLdbRows[0].setFieldValue("RETAIL_ORDER","");
						
						try
						{
							soLdbTable.saveRow(rsoiTLdbRows[0]);
						}
						catch (VtiExitException ee)
						{
						}
						
					}
				}
			}
			else
				erroMsg = "Sales order not released, request admin to assist.";
			
			if(stPOTLdbRows.length == 1)
			{
				//stPOTLdbRows[0].setFieldValue("TRUCKREG","");
				//stPOTLdbRows[0].setFieldValue("USERID","");
				stPOTLdbRows[0].setFieldValue("WGH_STATUS","DELETED");
				stPOTLdbRows[0].setFieldValue("STATUS","D");
				stPOTLdbRows[0].setFieldValue("DEL_IND","X");
				stPOTLdbRows[0].setFieldValue("TIMESTAMP", "");
			
				try
				{
					statusLdbTable.saveRow(stPOTLdbRows[0]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Truck was not cleared from the sales order, the order will not be availible collection.", ee);
					return new VtiUserExitResult(999,"Truck was not cleared from the sales order, the order will not be availible collection.");
				}
			}
			
			if(stSTOTLdbRows.length == 1)
			{
				//stSTOTLdbRows[0].setFieldValue("TRUCKREG","");
				//stSTOTLdbRows[0].setFieldValue("USERID","");
				stSTOTLdbRows[0].setFieldValue("WGH_STATUS","DELETED");
				stSTOTLdbRows[0].setFieldValue("STATUS","D");
				stSTOTLdbRows[0].setFieldValue("DEL_IND","X");
				stSTOTLdbRows[0].setFieldValue("TIMESTAMP", "");
			
				try
				{
					statusLdbTable.saveRow(stSTOTLdbRows[0]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Truck was not cleared from the sales order, the order will not be availible collection.", ee);
					return new VtiUserExitResult(999,"Truck was not cleared from the sales order, the order will not be availible collection.");
				}
			}
			
			if(stICTLdbRows.length == 1)
			{
				//stICTLdbRows[0].setFieldValue("TRUCKREG","");
				//stICTLdbRows[0].setFieldValue("USERID","");
				stICTLdbRows[0].setFieldValue("WGH_STATUS","DELETED");
				stICTLdbRows[0].setFieldValue("STATUS","D");
				stICTLdbRows[0].setFieldValue("DEL_IND","X");
				stICTLdbRows[0].setFieldValue("TIMESTAMP", "");
			
				try
				{
					statusLdbTable.saveRow(stICTLdbRows[0]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Truck was not cleared from the order, the order will not be availible collection.", ee);
					return new VtiUserExitResult(999,"Truck was not cleared from the sales order, the order will not be availible collection.");
				}
				
				VtiExitLdbSelectCriterion [] icSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, qRef),
				};
			
				VtiExitLdbSelectConditionGroup icSelCondGrp = new VtiExitLdbSelectConditionGroup(icSelConds, true);
				VtiExitLdbTableRow[] icLdbRows = icLdbTable.getMatchingRows(icSelCondGrp);
			
					if(icLdbRows.length == 1)
					{
						icLdbRows[0].setFieldValue("TRUCKREG","");
						icLdbRows[0].setFieldValue("VTIREF","");
						icLdbRows[0].setFieldValue("USERID","");
						icLdbRows[0].setFieldValue("TIMESTAMP", "");
						icLdbRows[0].setFieldValue("STATUS","NEW");//Make this new/Post Go Live
			
						try
						{
							icLdbTable.saveRow(icLdbRows[0]);
						}
						catch(VtiExitException ee)
						{
							Log.error("Truck was not cleared from the sales order, the order will not be availible collection.", ee);
							return new VtiUserExitResult(999,"Truck was not cleared from the sales order, the order will not be availible collection.");
						}
					}
					else
						erroMsg = "Inter company order not released, request admin to assist.";
			}
		
		
			
		
		//bump starts here
				
		ChangeQ shiftTruck = new ChangeQ(this, preQ, nProdType);
		
		shiftTruck.bumpTruckUp(newQPos);
		
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_QUEUE"))
			sessionHeader.setNextFunctionId("YSWB_QUEUE2");
		else
			sessionHeader.setNextFunctionId("YSWB_QUEUE");
		
		dbCall.ldbUpload("YSWB_QUEUE", this);
		dbCall.ldbUpload("YSWB_REGISTER", this);
		dbCall.ldbUpload("YSWB_SO_HEADER", this);
		dbCall.ldbUpload("YSWB_IC_HEADER", this);
		dbCall.ldbUpload("YSWB_STATUS", this);
		dbCall.ldbUpload("YSWB_GATEPASS", this);
			
		return new VtiUserExitResult(000,erroMsg);
	}
	
}
