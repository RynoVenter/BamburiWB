package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class RailWagonRegisterSave extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrRegno = getScreenField("REGNO");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrDate = getScreenField("S_DATE");
		VtiUserExitScreenField scrWDate = getScreenField("DATE");
		VtiUserExitScreenField scrTimestamp = getScreenField("TIMESTAMP");		
		VtiUserExitScreenField scrUserID = getScreenField("USERID");
		VtiUserExitScreenField scrSelf = getScreenField("RDB_SELF");
		VtiUserExitScreenField scrContractor = getScreenField("RDB_CONTRACTOR");
		
		VtiUserExitScreenTable tblWagons = getScreenTable("TB_WAGON");
		
		if(scrRegno == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrTime == null) return new VtiUserExitResult (999,"Failed to initialise TIME.");
		if(scrDate == null) return new VtiUserExitResult (999,"Failed to initialise S_DATE.");
		if(scrWDate == null) return new VtiUserExitResult (999,"Failed to initialise DATE.");
		if(scrTimestamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");
		if(scrUserID == null) return new VtiUserExitResult (999,"Failed to initialise USERID.");
		
		if(tblWagons == null) return new VtiUserExitResult (999,"Failed to initialise table TB_WAGONS.");
		
		//Variable Declarations
		
		if(scrRegno.getFieldValue().length() == 0 || scrRegno.getFieldValue().length() > 10 )
			return new VtiUserExitResult(999, "Please enter a valid Registration Number.Number can not be longer than 10.");
		
		Date currNow = new Date();
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String capTime = "";
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String errorMsg = "";

		DBCalls dbCall = new DBCalls();
		
		//Database TBL Declaration
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable gpLdbTable = getLocalDatabaseTable("YSWB_GATEPASS");
		
		if (regLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (inspLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");
		if (confLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (gpLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_GATEPASS.");

		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
			if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
				
		int wagonCount = 0;
		
		wagonCount = tblWagons.getRowCount();
		
		for(int chkWCount = 0; chkWCount < wagonCount; chkWCount++)
		{
			if(tblWagons.getRow(chkWCount).getFieldValue("CHK_USE").length() == 0)
			{ 
				if(tblWagons.getRow(chkWCount).getFieldValue("GATEPASS").length() > 0 || 
				   tblWagons.getRow(chkWCount).getFieldValue("MAX_WGH").length() > 0 ||
				   tblWagons.getRow(chkWCount).getFieldValue("WAGNO").length() > 0)
						return new VtiUserExitResult(999,1,"Please clear any information that will not be needed. Ensure that all the wagons that will be used have a check mark in the checkbox.");
			}
			
			if(tblWagons.getRow(chkWCount).getFieldValue("CHK_USE").length() > 0)
			{ 
				if(tblWagons.getRow(chkWCount).getFieldValue("MAX_WGH").length() == 0 )
						return new VtiUserExitResult(999,1,"Please ensure that the Max tonnage per wagon has been entered..");
			}
		}
		
		for(int wCount = 0; wCount < wagonCount; wCount++)
		{
			if(tblWagons.getRow(wCount).getFieldValue("CHK").length() > 0)
			{ 
				if(tblWagons.getRow(wCount).getFieldValue("GATEPASS").length() != 0)
				{
					VtiExitLdbSelectCriterion [] gpvSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
										new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, tblWagons.getRow(wCount).getFieldValue("GATEPASS")),
						};
      
						VtiExitLdbSelectConditionGroup gpvSelCondGrp = new VtiExitLdbSelectConditionGroup(gpvSelConds, true);
						VtiExitLdbTableRow[] gpvLdbRows = gpLdbTable.getMatchingRows(gpvSelCondGrp);

						if(gpvLdbRows.length == 0)
						{
							Log.warn("Gatepass " + tblWagons.getRow(wCount).getFieldValue("GATEPASS") + " is not an SAP gatepass.");
							return new VtiUserExitResult(999,1, "This is not a valid Gate Pass, please validate.");
						}
				}
		
				
				//Save Dataset 
		
				VtiExitLdbSelectCriterion [] regSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, tblWagons.getRow(wCount).getFieldValue("WAGNO")),
									new VtiExitLdbSelectCondition("TRAIN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
				};
      
				VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
				VtiExitLdbTableRow[] regLdbRows = regLdbTable.getMatchingRows(regSelCondGrp);
						
				if(regLdbRows.length > 0)
					return new VtiUserExitResult(999,1, "Already has order assigned to Wagon " + tblWagons.getRow(wCount).getFieldValue("WAGNO") + " . Archive wagon from system before new arrival.");
				
			}
		}
		
		String driver;
		
		VtiExitLdbSelectCriterion [] regTrainSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("TRAIN", VtiExitLdbSelectCondition.EQ_OPERATOR, "X"),
		};
      
		VtiExitLdbSelectConditionGroup regTrainSelCondGrp = new VtiExitLdbSelectConditionGroup(regTrainSelConds, true);
		VtiExitLdbTableRow[] regTrainLdbRows = regLdbTable.getMatchingRows(regTrainSelCondGrp);
						
		if(regTrainLdbRows.length == 0)
			driver = "No Driver";
		else
			driver = regTrainLdbRows[0].getFieldValue("DRIVER");

		
		long refNo = 0;
		
		for(int SWCount = 0; SWCount < wagonCount; SWCount++)
		{
			if(tblWagons.getRow(SWCount).getFieldValue("CHK_USE").length() > 0)
			{ 				
				
				try
				{
					refNo = getNextNumberFromNumberRange("YSWB_KEY");
				}
				catch(VtiExitException ee)
				{
					Log.error("Unable to create number from YSWB_KEY",ee);
					return new VtiUserExitResult(999,"Unable to retrieve a transaction number from number range YSWB_KEY");
				}
		
				VtiExitLdbTableRow ldbRowReg = regLdbTable.newRow();
				capTime = Integer.toString(Integer.parseInt(currLdbTime) + SWCount);
				//Populate TBL Fields
				ldbRowReg.setFieldValue("SERVERID", getServerId());
				ldbRowReg.setFieldValue("SERVERGRP", getServerGroup());
				ldbRowReg.setFieldValue("TRUCKREG", tblWagons.getRow(SWCount).getFieldValue("WAGNO"));
				ldbRowReg.setFieldValue("WAGON", "X");
				ldbRowReg.setFieldValue("SELF", scrSelf.getFieldValue());
				ldbRowReg.setFieldValue("CONTRACTOR", scrContractor.getFieldValue());
				ldbRowReg.setFieldValue("TRAIN", scrRegno.getFieldValue());
				ldbRowReg.setFieldValue("DRIVER", driver);
				ldbRowReg.setFieldValue("MAXWEIGHT", tblWagons.getRow(SWCount).getFieldValue("MAX_WGH"));		
				ldbRowReg.setFieldValue("AUDAT", currLdbDate);
				ldbRowReg.setFieldValue("AUTIM", capTime);
				ldbRowReg.setFieldValue("VTI_REF", refNo);
				ldbRowReg.setFieldValue("TIMESTAMP", scrTimestamp.getFieldValue());
				ldbRowReg.setFieldValue("INSPSTATUS", "R");
				ldbRowReg.setFieldValue("GATE_PASS", tblWagons.getRow(SWCount).getFieldValue("GATEPASS"));
				ldbRowReg.setFieldValue("USERID", scrUserID.getFieldValue());
			
				try
				{
					regLdbTable.saveRow(ldbRowReg);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save data to the Registration table.",ee);
					return new VtiUserExitResult(999,"Unable to Save data to the Registration table, please try again.");
				}
			
				if(tblWagons.getRow(SWCount).getFieldValue("GATEPASS").length() != 0)
				{
					VtiExitLdbSelectCriterion [] gpSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
									new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, tblWagons.getRow(SWCount).getFieldValue("GATEPASS")),
					};
      
					VtiExitLdbSelectConditionGroup gpSelCondGrp = new VtiExitLdbSelectConditionGroup(gpSelConds, true);
					VtiExitLdbTableRow[] gpLdbRows = gpLdbTable.getMatchingRows(gpSelCondGrp);

					if(gpLdbRows.length == 0)
					{
						Log.warn("Gatepass " + tblWagons.getRow(SWCount).getFieldValue("GATEPASS") + " is not an SAP gatepass.");
						errorMsg = "This gatepass is not an SAP gatepass.";
					}
				
					if(gpLdbRows.length > 0)
					{
						gpLdbRows[0].setFieldValue("STATUS","R");
						gpLdbRows[0].setFieldValue("VTIREF",refNo);
						gpLdbRows[0].setFieldValue("TIMESTAMP","");
							
						try
						{
							gpLdbTable.saveRow(gpLdbRows[0]);
						}
						catch(VtiExitException ee)
						{
							Log.error(refNo + "<:>" + gpLdbRows[0].getFieldValue("PASS_NUMB") + " scr val > " + tblWagons.getRow(SWCount).getFieldValue("GATEPASS"), ee);
							return new VtiUserExitResult(999,"Failed to save status to Gate Pass. Registration was done, do not repeat.");
						}
					}
					else
					{
						Log.error(refNo + "<VTI:GP>" + tblWagons.getRow(SWCount).getFieldValue("GATEPASS"));
					}
				}
			}
		}
			try
			{					
			// Trigger the uploads to SAP, if a connection is available.
				String hostName = getHostInterfaceName();
				boolean hostConnected = isHostInterfaceConnected(hostName);

				if (hostConnected)
				{ 
					dbCall.ldbUpload("YSWB_REGISTER", this);
					dbCall.ldbUpload("YSWB_GATEPASS", this);
				}
				else
				{
					Log.error("Host not connected to SAP to upload register data during Arrival save, check server.");
				}
					
			}
			catch (VtiExitException ee)
			{
				Log.error("Host not connected to SAP to upload register data during Arrival save, check server.");
			}
		
		String reg = scrRegno.getFieldValue();
				
		scrRegno.setFieldValue("");
		scrTime.setFieldValue(currTime);
		scrDate.setFieldValue("");
		tblWagons.clear();
	
		return new VtiUserExitResult(000,1,"Saved registration. " + errorMsg);
	}
		
}
