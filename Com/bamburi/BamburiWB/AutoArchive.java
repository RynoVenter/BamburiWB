package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class AutoArchive extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{

		VtiUserExitScreenField scrContractor = getScreenField("CONTRACTOR");
		if(scrContractor == null) return new VtiUserExitResult (999,"Failed to initialise CONTRACTOR.");
		VtiUserExitScreenField scrTruckReg = getScreenField("TRUCKREG");
		if(scrTruckReg == null) return new VtiUserExitResult (999,"Failed to initialise TRUCKREG.");
		VtiUserExitScreenField btnAssign = getScreenField("BT_SAVE");
		if(btnAssign == null) return new VtiUserExitResult (999,"Failed to initialise BT_SAVE.");
		scrContractor.setFieldValue("");
		

		VtiExitLdbTable gpLdbTable = getLocalDatabaseTable("YSWB_GATEPASS");
		VtiExitLdbTable logonLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		VtiExitLdbTable icHLdbTable = getLocalDatabaseTable("YSWB_IC_HEADER");
		VtiExitLdbTable icILdbTable = getLocalDatabaseTable("YSWB_IC_ITEMS");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable loadLdbTable = getLocalDatabaseTable("YSWB_LOADING");
		VtiExitLdbTable packLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable soHLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable soILdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable statLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable poHLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable poILdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		
		
		if (gpLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_GATEPASS.");
		if (logonLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		if (icHLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_HEADER.");
		if (icILdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_ITEMS.");
		if (inspLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");
		if (loadLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOADING.");
		if (packLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (regLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (soHLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (soILdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_ITEMS.");
		if (statLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		DBCalls dbCall = new DBCalls();
		
		Date currNow = new Date();
		
		ChangeQ cQ = new ChangeQ(this);
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currMidnight = currLdbDate + "" + "000001";
		String oneOClock = currLdbDate + "" + "005959";
		String currSysTime = currLdbDate + "" + currLdbTime;
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		
		Log.trace(0,"AutoArchive active");
		Log. trace(0," CurrTime = " + Long.parseLong(currSysTime));
		Log. trace(0," CurrMidnight = " + Long.parseLong(currMidnight));
		Log. trace(0," oneOClock = " + Long.parseLong(oneOClock));
		
		
		if(Long.parseLong(currSysTime) > Long.parseLong(currMidnight) && Long.parseLong(currSysTime) < Long.parseLong(oneOClock))
		{
			
			Log.trace(0,"Auto Archive was run.");
			cQ.resetQPos();
			String hostName = getHostInterfaceName();
			
			try
			{
				forceHeartbeat(hostName,true,250);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable to auto upload the tables.",ee);
			}
			
			boolean hostConnected = isHostInterfaceConnected(hostName);
	
			hostConnected = isHostInterfaceConnected(hostName);

				if (hostConnected)
				{ 
					try
					{
						dbCall.ldbUpload("YSWB_GATEPASS", this);
						dbCall.ldbUpload("YSWB_IC_HEADER", this);
						dbCall.ldbUpload("YSWB_IC_ITEMS", this);
						dbCall.ldbUpload("YSWB_INSPECT", this);
						dbCall.ldbUpload("YSWB_LOADING", this);
						dbCall.ldbUpload("YSWB_PACKING", this);
						dbCall.ldbUpload("YSWB_QUEUE", this);
						dbCall.ldbUpload("YSWB_REGISTER", this);
						dbCall.ldbUpload("YSWB_SO_HEADER", this);
						dbCall.ldbUpload("YSWB_SO_ITEMS", this);
						dbCall.ldbUpload("YSWB_PO_HEADER", this);
						dbCall.ldbUpload("YSWB_PO_ITEMS", this);
						dbCall.ldbUpload("YSWB_STATUS", this);
						dbCall.ldbUpload("YSWB_WB", this);
					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to auto upload the tables.",ee);
					}
				}
		
				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException ie)
				{
					Log.error("Sleep Thread during Master reload failed.", ie);
				}
		
				
				//Clear records from the tables that do not belong to the servergroup.
		
		
				VtiExitLdbSelectCriterion nonGroupRecordsSelConds = new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.NE_OPERATOR, getServerGroup());
					
				if(wbLdbTable != null)
				{
					VtiExitLdbSelectCriterion nonGroupWBRecordsSelConds = new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.NE_OPERATOR, getServerGroup());
					wbLdbTable.deleteMatchingRows(nonGroupWBRecordsSelConds);
				}
		
				if(statLdbTable != null)
				{
					statLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}
		
				if(regLdbTable != null)
				{
					regLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}
		
				if(queueLdbTable != null)
				{
					queueLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}
		
				if(packLdbTable != null)
				{
					packLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}
		
				if(loadLdbTable != null)
				{
					loadLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}
		
				if(logonLdbTable != null)
				{
					logonLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}

				if(inspLdbTable != null)
				{
					inspLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}	
		
				if(gpLdbTable != null)
				{
					gpLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}	
				
				if(soHLdbTable != null)
				{
					soHLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}	
				
				if(soILdbTable != null)
				{
					soILdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}	
				
				if(poHLdbTable != null)
				{
					poHLdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}	
				
				if(poILdbTable != null)
				{
					poILdbTable.deleteMatchingRows(nonGroupRecordsSelConds);
				}	
		}
		
		{
			
			
			VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
			if(configLdbTable == null) return new VtiUserExitResult(999,"Config table not availible.");
		
			VtiExitLdbSelectCriterion [] appFilterSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "APPFILTER"),
			};
      
			VtiExitLdbSelectConditionGroup appFilterSelCondGrp = new VtiExitLdbSelectConditionGroup(appFilterSelConds, true);
			VtiExitLdbTableRow[] appFilterLdbRows = configLdbTable.getMatchingRows(appFilterSelCondGrp);
					
			Log.trace(0,"Appfilter length " + appFilterLdbRows.length);
			
			if(appFilterLdbRows.length == 0)
			{
				scrTruckReg.setHiddenFlag(true);
				btnAssign.setHiddenFlag(true);
				return new VtiUserExitResult(999,1, "Application filter required to process screen.");
				
			}
			
			if(appFilterLdbRows[0].getFieldValue("KEYVAL2").length() == 0)
			{
				scrTruckReg.setHiddenFlag(true);
				btnAssign.setHiddenFlag(true);
				return new VtiUserExitResult(999,1, "Application filter required to process screen.");
				
			}
			
			VtiExitLdbSelectCriterion [] serverSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "SERVERS"),
			};
      
			VtiExitLdbSelectConditionGroup serverSelCondGrp = new VtiExitLdbSelectConditionGroup(serverSelConds, true);
			VtiExitLdbTableRow[] serverLdbRows = configLdbTable.getMatchingRows(serverSelCondGrp);
			
			Log.trace(0,"Server Group is " + getServerGroup() + " server is " + serverLdbRows[0].getFieldValue("KEYVAL4"));
					
			if(serverLdbRows.length == 0)
			{
				scrTruckReg.setHiddenFlag(true);
				btnAssign.setHiddenFlag(true);
				return new VtiUserExitResult(999,1, "Please maintain application key.");
				
			}
			
			if(serverLdbRows[0].getFieldValue("KEYVAL4").length() == 0)
			{
				scrTruckReg.setHiddenFlag(true);
				btnAssign.setHiddenFlag(true);
				return new VtiUserExitResult(999,1, "Please maintain application key.");
			}
				
			
			String filterKey = appFilterLdbRows[0].getFieldValue("KEYVAL2");
			String screenState = "20" + filterKey.charAt(3) + "" + filterKey.charAt(11) + "/" +
								filterKey.charAt(8) + filterKey.charAt(0) + "/" + filterKey.charAt(15) + filterKey.charAt(7);
			String serv = filterKey.charAt(6) + "" + filterKey.charAt(9) + "" +  filterKey.charAt(16) + "" +  filterKey.charAt(1);
			
			if(!serverLdbRows[0].getFieldValue("KEYVAL4").equalsIgnoreCase(serv))
			{
				scrTruckReg.setHiddenFlag(true);
				btnAssign.setHiddenFlag(true);
				return new VtiUserExitResult(999,1, "Please maintain application key.");
			}
			
			int year = Integer.parseInt("20" + filterKey.charAt(3) + "" + filterKey.charAt(11));
			int month = Integer.parseInt(filterKey.charAt(8) + "" + filterKey.charAt(0));
			int day = Integer.parseInt(filterKey.charAt(15) + "" +  filterKey.charAt(7));
			String screenBuild = filterKey.charAt(18) + "" + filterKey.charAt(19);
			Calendar currCalDate = Calendar.getInstance();
			String currLDate = DateFormatter.format("yyyy/MM/dd", currNow);
						
			Integer screenV = new Integer(screenBuild);
			int d = screenV.intValue();
			Calendar cal = Calendar.getInstance();
			
			cal.set(year,month-1,day);
			cal.add(Calendar.DATE,d * -1);
			
			if(currLDate.equals(screenState))
			{
				Log.trace(0,"Cal period off Instance = " + cal);
				Log.trace(0,"Curr Instance = " + currCalDate);
				Log.abort("Please consult Britehouse. Functional license key expired. Sky Technologies license still valid.");
			}			
			
			if(currCalDate.after(cal))
			{
				Log.trace(0,"Cal period off Instance = " + cal);
				Log.trace(0,"Curr Instance = " + currCalDate);
			   return new VtiUserExitResult(000,1,"Please consult Britehouse. Functional license key will expire on " + screenState +".");
			}
		}
		
		
	
		
		return new VtiUserExitResult();
	}
}
