package com.bamburi.bamburiwb;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class WagonRelease extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrRegno = getScreenField("WAGNO");
		VtiUserExitScreenField scrTRegno = getScreenField("REGNO");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrDate = getScreenField("DATE");
		VtiUserExitScreenField scrMaxWeight = getScreenField("MAXWEIGHT");		
		VtiUserExitScreenField scrContractor = getScreenField("CONTRACTOR");
		VtiUserExitScreenField scrSelf = getScreenField("SELF");
		VtiUserExitScreenField scrTimestamp = getScreenField("TIMESTAMP");		
		VtiUserExitScreenField scrUserID = getScreenField("USERID");
		VtiUserExitScreenField scrRefNo = getScreenField("WVTI_REF");
		VtiUserExitScreenField scrInStatus = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrVbeln = getScreenField("VBELN");
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		VtiUserExitScreenField scrIcVbeln = getScreenField("ICVBELN");
		VtiUserExitScreenField scrStEbeln = getScreenField("STEBELN");
	
		if(scrRegno == null) return new VtiUserExitResult (999,"Failed to initialise WAGNO.");
		if(scrTRegno == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrTime == null) return new VtiUserExitResult (999,"Failed to initialise TIME.");
		if(scrDate == null) return new VtiUserExitResult (999,"Failed to initialise S_DATE.");
		if(scrMaxWeight == null) return new VtiUserExitResult (999,"Failed to initialise MAXWEIGHT.");		
		if(scrContractor == null) return new VtiUserExitResult (999,"Failed to initialise CONTRACTOR.");
		if(scrSelf == null) return new VtiUserExitResult (999,"Failed to initialise SELF.");
		if(scrTimestamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");
		if(scrUserID == null) return new VtiUserExitResult (999,"Failed to initialise USERID.");
		if(scrRefNo == null) return new VtiUserExitResult (999,"Failed to initialise WVTI_REF.");
		if(scrInStatus == null) return new VtiUserExitResult (999,"Failed to initialise INSPSTATUS.");
		if(scrVbeln == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
		if(scrEbeln == null) return new VtiUserExitResult (999,"Failed to initialise EBELN.");
		if(scrIcVbeln == null) return new VtiUserExitResult (999,"Failed to initialise ICVBELN.");
		if(scrStEbeln == null) return new VtiUserExitResult (999,"Failed to initialise STEBELN.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		//Variable Declarations
	
		if(scrRegno.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "Please enter Registration Number.");
		
		Date currNow = new Date();
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String date = scrDate.getFieldValue();
		String time = "";
		DBCalls dbCall = new DBCalls();
		String order = scrVbeln.getFieldValue() + scrEbeln.getFieldValue() + scrIcVbeln.getFieldValue() + scrStEbeln.getFieldValue();
		
		String hostName = getHostInterfaceName();
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		
		if(order.length() > 10)
			return new VtiUserExitResult(999,"Order number longer than 10 characters, request admin support.");
		
		//Database TBL Declaration
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable soHeaderLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable icHeaderLdbTable = getLocalDatabaseTable("YSWB_IC_HEADER");
		VtiExitLdbTable qLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		
		if (regLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (soHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (icHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_HEADER.");
		if (qLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		


		//Update registered truck from table
			VtiExitLdbSelectCriterion [] registerSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
			VtiExitLdbTableRow[] registerLdbRows = regLdbTable.getMatchingRows(registerSelCondGrp);
		
		if(registerLdbRows.length == 0)
			return new VtiUserExitResult(999,"Registration not found.");	
		

		//Populate TBL Fields
		//registerLdbRows[0].setFieldValue("TIMESTAMP", scrTimestamp.getFieldValue());
		registerLdbRows[0].setFieldValue("INSPSTATUS", "A");
		registerLdbRows[0].setFieldValue("USERID", sessionHeader.getUserId());
		
		//Update so
		if(scrVbeln.getFieldValue().length() > 0)
		{
			VtiExitLdbSelectCriterion [] soSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 1"),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVbeln.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup soSelCondGrp = new VtiExitLdbSelectConditionGroup(soSelConds, true);
			VtiExitLdbTableRow[] soLdbRows = soHeaderLdbTable.getMatchingRows(soSelCondGrp);
		
			if(soLdbRows.length == 0)
				return new VtiUserExitResult(999,"Sales Order not found.");	
			
				soLdbRows[0].setFieldValue("STATUS","ASSIGNED");

				try
				{
					soHeaderLdbTable.saveRow(soLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable update wagon.",ee);
					return new VtiUserExitResult(999,"Unable to release wagon.");
				}
		}
		
		//Update PO
		if(scrEbeln.getFieldValue().length() > 0)
		{
			VtiExitLdbSelectCriterion [] statusSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
								new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "WEIGH 1"),
									new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrEbeln.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
			VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
		
			if(statusLdbRows.length == 0)
				return new VtiUserExitResult(999,"Purchase order not found.");	
			
			statusLdbRows[0].setFieldValue("WGH_STATUS","ASSIGNED");
			statusLdbRows[0].setFieldValue("STATUS","A");
			
			try
			{
				statusLdbTable.saveRow(statusLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable update wagon.",ee);
				return new VtiUserExitResult(999,"Unable to release wagon.");
			}
		}

		//Update IC
		if(scrIcVbeln.getFieldValue().length() > 0)
		{
			VtiExitLdbSelectCriterion [] icSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrIcVbeln.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup icSelCondGrp = new VtiExitLdbSelectConditionGroup(icSelConds, true);
			VtiExitLdbTableRow[] icLdbRows = icHeaderLdbTable.getMatchingRows(icSelCondGrp);
		
			if(icLdbRows.length == 0)
				return new VtiUserExitResult(999,"Inter Company Order not found.");
			
			VtiExitLdbSelectCriterion [] statusSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
								new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrIcVbeln.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
			VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
		
			if(statusLdbRows.length == 0)
				return new VtiUserExitResult(999,"Inter company order not found in status table.");	


			icLdbRows[0].setFieldValue("STATUS","ASSIGNED");
			
			statusLdbRows[0].setFieldValue("WGH_STATUS","ASSIGNED");
			statusLdbRows[0].setFieldValue("STATUS","A");
			
			try
			{
				statusLdbTable.saveRow(statusLdbRows[0]);
				icHeaderLdbTable.saveRow(icLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable update wagon.",ee);
				return new VtiUserExitResult(999,"Unable to release wagon.");
			}

		}
		
		//Update ST
		if(scrStEbeln.getFieldValue().length() > 0)
		{

			VtiExitLdbSelectCriterion [] statusSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
								new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "WEIGH 1"),
									new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrStEbeln.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
			VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
		
			if(statusLdbRows.length == 0)
				return new VtiUserExitResult(999,"Stock Transfer Order not found.");	

			
			statusLdbRows[0].setFieldValue("WGH_STATUS","ASSIGNED");
			statusLdbRows[0].setFieldValue("STATUS","A");
			
			try
			{
				statusLdbTable.saveRow(statusLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable update wagon.",ee);
				return new VtiUserExitResult(999,"Unable to release wagon.");
			}		
		}
		
		try
		{
			regLdbTable.saveRow(registerLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable update wagon.",ee);
			return new VtiUserExitResult(999,"Unable to release wagon.");
		}
		//Update queue
		
		VtiExitLdbSelectCriterion [] qSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
						new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "Complete"),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);

		VtiExitLdbTableRow[] qTLdbRows = qLdbTable.getMatchingRows(qSelCondGrp);
		
		if(qTLdbRows.length == 0)
			return new VtiUserExitResult(999,"Queue details not found.");	

			
		qTLdbRows[0].setFieldValue("Q_STATUS","ASSIGNED");
	
			
		try
		{
			qLdbTable.saveRow(qTLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable update queue.",ee);
			return new VtiUserExitResult(999,"Unable to release wagon.");
		}
		
		
		
		//Set next screen
				
		sessionHeader.setNextFunctionId("YSWB_MAIN");
		
		try
		{					
		// Trigger the uploads to SAP, if a connection is available.
				boolean hostConnected = isHostInterfaceConnected(hostName);

				if (hostConnected)
				{ 
					dbCall.ldbUpload("YSWB_REGISTER", this);
					dbCall.ldbUpload("YSWB_IC_HEADER", this);
					dbCall.ldbUpload("YSWB_STATUS", this);
					dbCall.ldbUpload("YSWB_SO_HEADER", this);
				}
		}
		catch (VtiExitException ee)
		{
				Log.trace(0,"Host not connected to SAP to upload register data during Arrival Edit save, check server.");
		}
		return new VtiUserExitResult();
	}
		
}