package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class RegisterEditSave extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrRegno = getScreenField("WAGNO");
		VtiUserExitScreenField scrNRegno = getScreenField("NREGNO");
		VtiUserExitScreenField scrTRegno = getScreenField("REGNO");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrDate = getScreenField("DATE");
		VtiUserExitScreenField scrCompany = getScreenField("COMPANY");
		VtiUserExitScreenField scrDriver = getScreenField("DRIVER");
		VtiUserExitScreenField scrIDNo = getScreenField("IDNUMBER");
		VtiUserExitScreenField scrMaxWeight = getScreenField("MAXWEIGHT");		
		VtiUserExitScreenField scrContractor = getScreenField("CONTRACTOR");
		VtiUserExitScreenField scrSelf = getScreenField("SELF");
		VtiUserExitScreenField scrTimestamp = getScreenField("TIMESTAMP");		
		VtiUserExitScreenField scrUserID = getScreenField("USERID");
		VtiUserExitScreenField scrLicNo = getScreenField("LICENSENO");
		VtiUserExitScreenField scrTelNo = getScreenField("TELNO");
		VtiUserExitScreenField scrRefNo = getScreenField("WVTI_REF");
		VtiUserExitScreenField scrInStatus = getScreenField("INSPSTATUS");
	
		if(scrRegno == null) return new VtiUserExitResult (999,"Failed to initialise WAGNO.");
		if(scrNRegno == null) return new VtiUserExitResult (999,"Failed to initialise NREGNO.");
		if(scrTRegno == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrTime == null) return new VtiUserExitResult (999,"Failed to initialise TIME.");
		if(scrDate == null) return new VtiUserExitResult (999,"Failed to initialise S_DATE.");
		if(scrCompany == null) return new VtiUserExitResult (999,"Failed to initialise COMPANY.");
		if(scrDriver == null) return new VtiUserExitResult (999,"Failed to initialise DRIVER.");
		if(scrIDNo == null) return new VtiUserExitResult (999,"Failed to initialise IDNUMBER.");
		if(scrMaxWeight == null) return new VtiUserExitResult (999,"Failed to initialise MAXWEIGHT.");		
		if(scrContractor == null) return new VtiUserExitResult (999,"Failed to initialise CONTRACTOR.");
		if(scrSelf == null) return new VtiUserExitResult (999,"Failed to initialise SELF.");
		if(scrTimestamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");
		if(scrUserID == null) return new VtiUserExitResult (999,"Failed to initialise USERID.");
		if(scrLicNo == null) return new VtiUserExitResult (999,"Failed to initialise LICENSENO.");
		if(scrTelNo == null) return new VtiUserExitResult (999,"Failed to initialise TELNO.");
		if(scrRefNo == null) return new VtiUserExitResult (999,"Failed to initialise WVTI_REF.");
		if(scrInStatus == null) return new VtiUserExitResult (999,"Failed to initialise INSPSTATUS.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		if(scrNRegno.getFieldValue().length() == 0)	
			return new VtiUserExitResult(999,"Please type the truck registration number.");
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
		
		//Database TBL Declaration
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		
		if (regLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");


		//Delete incorrect registered truck from table
			VtiExitLdbSelectCriterion [] registerSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "R"),
			};
      
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
			VtiExitLdbTableRow[] registerLdbRows = regLdbTable.getMatchingRows(registerSelCondGrp);
		
		if(registerLdbRows.length == 0)
			return new VtiUserExitResult(999,"Registration not found.");	
		
			try
			{
				time = registerLdbRows[0].getFieldValue("AUTIM");
				date = registerLdbRows[0].getFieldValue("AUDAT");
				regLdbTable.deleteRow(registerLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Failed to delete original arrival.", ee);
				return new VtiUserExitResult(999,"No record to remove, unable to edit data.Please check Status.");
			}
		
		
		
			

		//Save New Dataset 
		
		VtiExitLdbTableRow ldbRowReg = regLdbTable.newRow();
			
		//Populate TBL Fields
		ldbRowReg.setFieldValue("SERVERID", getServerId());
		ldbRowReg.setFieldValue("SERVERGRP", getServerGroup());
		ldbRowReg.setFieldValue("CONTRACTOR", scrContractor.getFieldValue());
		ldbRowReg.setFieldValue("SELF", scrSelf.getFieldValue());
		ldbRowReg.setFieldValue("TRUCKREG", scrNRegno.getFieldValue());
		ldbRowReg.setFieldValue("WAGON", "X");
		ldbRowReg.setFieldValue("TRAIN", scrTRegno.getFieldValue());
		ldbRowReg.setFieldValue("COMPANY", scrCompany.getFieldValue());
		ldbRowReg.setFieldValue("DRIVER", scrDriver.getFieldValue());
		ldbRowReg.setFieldValue("LICENSENO", scrLicNo.getFieldValue());
		ldbRowReg.setFieldValue("IDNUMBER", scrIDNo.getFieldValue());
		ldbRowReg.setFieldValue("TELNO", scrTelNo.getFieldValue());
		ldbRowReg.setFieldValue("MAXWEIGHT", scrMaxWeight.getFieldValue());		
		ldbRowReg.setFieldValue("AUDAT", date);
		ldbRowReg.setFieldValue("AUTIM", time);
		ldbRowReg.setFieldValue("VTI_REF", scrRefNo.getFieldValue());
		ldbRowReg.setFieldValue("TIMESTAMP", scrTimestamp.getFieldValue());
		ldbRowReg.setFieldValue("INSPSTATUS", "R");
		ldbRowReg.setFieldValue("USERID", sessionHeader.getUserId());
		
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
		}
		catch (VtiExitException ee)
		{
				Log.error("Host not connected to SAP to upload register data during Arrival Edit save, check server.");
		}
		
		scrRegno.setFieldValue("");
		scrNRegno.setFieldValue("");
		scrCompany.setFieldValue("");
		scrDriver.setFieldValue("");
		scrIDNo.setFieldValue("");
		scrMaxWeight.setFieldValue("");
		scrSelf.setFieldValue("");
		scrContractor.setFieldValue("");
		scrTelNo.setFieldValue("");
		scrLicNo.setFieldValue("");
		scrDate.setFieldValue("");
		scrTime.setFieldValue("");
		scrRefNo.setFieldValue("");
	
		sessionHeader.setNextFunctionId("YSWB_VEHINSPT");
		return new VtiUserExitResult();
	}
		
}