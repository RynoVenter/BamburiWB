package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class RailTrainRegisterSave extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrRegno = getScreenField("REGNO");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrDate = getScreenField("S_DATE");
		VtiUserExitScreenField scrWDate = getScreenField("DATE");
		VtiUserExitScreenField scrCompany = getScreenField("COMPANY");
		VtiUserExitScreenField scrDriver = getScreenField("DRIVER");
		VtiUserExitScreenField scrWDriver = getScreenField("DRIVER_S");
		VtiUserExitScreenField scrIDNo = getScreenField("IDNUMBER");
		VtiUserExitScreenField scrContractor = getScreenField("CONTRACTOR");
		VtiUserExitScreenField scrSelf = getScreenField("SELF");
		VtiUserExitScreenField scrTimestamp = getScreenField("TIMESTAMP");		
		VtiUserExitScreenField scrUserID = getScreenField("USERID");
		VtiUserExitScreenField scrLicNo = getScreenField("LICENSENO");
		VtiUserExitScreenField scrTelNo = getScreenField("TELNO");
		VtiUserExitScreenField btnWagReg = getScreenField("BT_BACK");
		
	
		if(scrRegno == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrTime == null) return new VtiUserExitResult (999,"Failed to initialise TIME.");
		if(scrDate == null) return new VtiUserExitResult (999,"Failed to initialise S_DATE.");
		if(scrWDate == null) return new VtiUserExitResult (999,"Failed to initialise DATE.");
		if(scrCompany == null) return new VtiUserExitResult (999,"Failed to initialise COMPANY.");
		if(scrDriver == null) return new VtiUserExitResult (999,"Failed to initialise DRIVER.");
		if(scrWDriver == null) return new VtiUserExitResult (999,"Failed to initialise DRIVER_S.");
		if(scrIDNo == null) return new VtiUserExitResult (999,"Failed to initialise IDNUMBER.");
		if(scrContractor == null) return new VtiUserExitResult (999,"Failed to initialise CONTRACTOR.");
		if(scrSelf == null) return new VtiUserExitResult (999,"Failed to initialise SELF.");
		if(scrTimestamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");
		if(scrUserID == null) return new VtiUserExitResult (999,"Failed to initialise USERID.");
		if(scrLicNo == null) return new VtiUserExitResult (999,"Failed to initialise LICENSENO.");
		if(scrTelNo == null) return new VtiUserExitResult (999,"Failed to initialise TELNO.");
		if(btnWagReg == null) return new VtiUserExitResult (999,"Failed to initialise BT_BACK.");
		//Variable Declarations

		
		if(scrRegno.getFieldValue().length() == 0 || scrRegno.getFieldValue().length() > 10 )
			return new VtiUserExitResult(999, "Please enter a valid Registration Number.");
		
		if((scrContractor.getFieldValue().length() + scrSelf.getFieldValue().length()) < 1)
			return new VtiUserExitResult(999, "Please indicate whether it is a Contractor of Self collecting truck.");
		
		
		
		//if(scrVbeln.getFieldValue().length() > 1 && scrEbeln.getFieldValue().length() > 1)
		//	return new VtiUserExitResult(999, "Please select either a Purchase Order or Sales Order.");
		
		Date currNow = new Date();
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String errorMsg = "";

		DBCalls dbCall = new DBCalls();
		
		//Database TBL Declaration
		VtiExitLdbTable soHeaderCLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable poHeaderCLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		
		if (soHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (poHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		if (regLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (inspLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");
		if (confLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");

		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		//Save Dataset 
		
		VtiExitLdbSelectCriterion [] regSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "A"),
				};
      
				VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
				VtiExitLdbTableRow[] regLdbRows = regLdbTable.getMatchingRows(regSelCondGrp);
						
				if(regLdbRows.length > 0)
					return new VtiUserExitResult(999, "Already has order assigned to truck. Delete truck from system before new arrival.");
				
			VtiExitLdbSelectCriterion [] regwSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
				};
      
				VtiExitLdbSelectConditionGroup regwSelCondGrp = new VtiExitLdbSelectConditionGroup(regwSelConds, true);
				VtiExitLdbTableRow[] regwLdbRows = regLdbTable.getMatchingRows(regwSelCondGrp);
						
				if(regwLdbRows.length > 0)
					return new VtiUserExitResult(999, "Truck is in weigh process.");
						
		
		long refNo = 0;
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
			
		//Populate TBL Fields
		ldbRowReg.setFieldValue("SERVERID", getServerId());
		ldbRowReg.setFieldValue("SERVERGRP", getServerGroup());
		ldbRowReg.setFieldValue("CONTRACTOR", scrContractor.getFieldValue());
		ldbRowReg.setFieldValue("SELF", scrSelf.getFieldValue());
		ldbRowReg.setFieldValue("TRUCKREG", scrRegno.getFieldValue());
		ldbRowReg.setFieldValue("TRAIN", "X");
		ldbRowReg.setFieldValue("COMPANY", scrCompany.getFieldValue());
		ldbRowReg.setFieldValue("DRIVER", scrDriver.getFieldValue());
		ldbRowReg.setFieldValue("LICENSENO", scrLicNo.getFieldValue());//Require Table FieldName
		ldbRowReg.setFieldValue("IDNUMBER", scrIDNo.getFieldValue());
		ldbRowReg.setFieldValue("TELNO", scrTelNo.getFieldValue());//Require Table FieldName
		ldbRowReg.setFieldValue("AUDAT", currLdbDate);
		ldbRowReg.setFieldValue("AUTIM", currLdbTime);
		ldbRowReg.setFieldValue("VTI_REF", refNo);
		ldbRowReg.setFieldValue("TIMESTAMP", scrTimestamp.getFieldValue());
		ldbRowReg.setFieldValue("INSPSTATUS", "R");
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
		
		try
		{					
		// Trigger the uploads to SAP, if a connection is available.
				String hostName = getHostInterfaceName();
				boolean hostConnected = isHostInterfaceConnected(hostName);

				if (hostConnected)
				{ 
					dbCall.ldbUpload("YSWB_REGISTER", this);
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
		scrCompany.setFieldValue("");
		scrDriver.setFieldValue("");
		scrWDriver.setFieldValue("");
		scrIDNo.setFieldValue("");
		scrSelf.setFieldValue("");
		scrContractor.setFieldValue("");
		scrTelNo.setFieldValue("");
		scrLicNo.setFieldValue("");
		btnWagReg.setHiddenFlag(false);
		return new VtiUserExitResult(000,1,"Saved registration as " + reg + " with reference " +refNo + "." + errorMsg);
	}
		
}
