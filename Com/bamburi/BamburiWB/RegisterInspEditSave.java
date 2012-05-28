package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class RegisterInspEditSave extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrRegno = getScreenField("REGNO");
		VtiUserExitScreenField scrNRegno = getScreenField("NREGNO");
		VtiUserExitScreenField scrVbeln = getScreenField("VBELN");
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrDate = getScreenField("DATE");
		VtiUserExitScreenField scrCompany = getScreenField("COMPANY");
		VtiUserExitScreenField scrDriver = getScreenField("DRIVER");
		VtiUserExitScreenField scrIDNo = getScreenField("IDNUMBER");
		VtiUserExitScreenField scrTransType = getScreenField("TRANSTYPE");
		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");
		VtiUserExitScreenField scrMaxWeight = getScreenField("MAXWEIGHT");		
//		VtiUserExitScreenField scrContractor = getScreenField("CONTRACTOR");
//		VtiUserExitScreenField scrSelf = getScreenField("SELF");
		VtiUserExitScreenField scrTimestamp = getScreenField("TIMESTAMP");		
		VtiUserExitScreenField scrUserID = getScreenField("USERID");
		VtiUserExitScreenField scrLicNo = getScreenField("LICENSENO");
		VtiUserExitScreenField scrTelNo = getScreenField("TELNO");
		VtiUserExitScreenField scrSalesQuote = getScreenField("DOC_NMBER");
		VtiUserExitScreenField scrGP = getScreenField("GATEPASS");
		VtiUserExitScreenField scrRefNo = getScreenField("VTI_REF");
		VtiUserExitScreenField scrInStatus = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrInspStatus = getScreenField("INSPSTATUS2");
	
		if(scrRegno == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrNRegno == null) return new VtiUserExitResult (999,"Failed to initialise NREGNO.");
		if(scrVbeln == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
		if(scrEbeln == null) return new VtiUserExitResult (999,"Failed to initialise EBELN.");
		if(scrTime == null) return new VtiUserExitResult (999,"Failed to initialise TIME.");
		if(scrDate == null) return new VtiUserExitResult (999,"Failed to initialise S_DATE.");
		if(scrCompany == null) return new VtiUserExitResult (999,"Failed to initialise COMPANY.");
		if(scrDriver == null) return new VtiUserExitResult (999,"Failed to initialise DRIVER.");
		if(scrIDNo == null) return new VtiUserExitResult (999,"Failed to initialise IDNUMBER.");
		if(scrTransType == null) return new VtiUserExitResult (999,"Failed to initialise TRANSTYPE.");
		if(scrNoAxels == null) return new VtiUserExitResult (999,"Failed to initialise NOAXELS.");
		if(scrMaxWeight == null) return new VtiUserExitResult (999,"Failed to initialise MAXWEIGHT.");		
//		if(scrContractor == null) return new VtiUserExitResult (999,"Failed to initialise CONTRACTOR.");
//		if(scrSelf == null) return new VtiUserExitResult (999,"Failed to initialise SELF.");
		if(scrTimestamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");
		if(scrUserID == null) return new VtiUserExitResult (999,"Failed to initialise USERID.");
		if(scrLicNo == null) return new VtiUserExitResult (999,"Failed to initialise LICENSENO.");
		if(scrTelNo == null) return new VtiUserExitResult (999,"Failed to initialise TELNO.");
		if(scrSalesQuote == null) return new VtiUserExitResult (999,"Failed to initialise DOC_NMBER.");
		if(scrGP == null) return new VtiUserExitResult (999,"Failed to initialise GATEPASS.");
		if(scrRefNo == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrInStatus == null) return new VtiUserExitResult (999,"Failed to initialise INSPSTATUS.");
		if(scrInspStatus == null) return new VtiUserExitResult (999,"Failed to initialise INSPSTATUS2.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		if(scrNRegno.getFieldValue().length() == 0)	
			return new VtiUserExitResult(999,"Please type the truck registration number.");
		//Variable Declarations
		if(scrNoAxels.getFieldValue().length() == 0) return new VtiUserExitResult(999,"Please select an axle amount.");	
		if(scrMaxWeight.getDoubleFieldValue() < 1) return new VtiUserExitResult (999,"Please indicate what the Max Weight for this truck is.");		
		if(scrRegno.getFieldValue().length() < 1) return new VtiUserExitResult(999, "Please enter search Registration Number.");
		if(scrNRegno.getFieldValue().length() < 1) return new VtiUserExitResult(999, "Please enter Actual Registration Number.");
		
		Date currNow = new Date();
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String date = scrDate.getFieldValue();
		String time = "";
		DBCalls dbCall = new DBCalls();
		
		String hostName = getHostInterfaceName();
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		
		//Database TBL Declaration
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		
		if (regLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (inspLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");


		//Delete incorrect registered truck from table
			VtiExitLdbSelectCriterion [] registerSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "A"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "W"),
									new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
									new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
									new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			}; 

			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
			VtiExitLdbTableRow[] registerLdbRows = regLdbTable.getMatchingRows(registerSelCondGrp);
		
			if(registerLdbRows.length == 0)
				return new VtiUserExitResult(999,"Registration not found.The truck might already be assigned. Check Quick Info at CSC.");	
		
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
	//	ldbRowReg.setFieldValue("CONTRACTOR", scrContractor.getFieldValue());
	//	ldbRowReg.setFieldValue("SELF", scrSelf.getFieldValue());
		ldbRowReg.setFieldValue("TRUCKREG", scrNRegno.getFieldValue());
		ldbRowReg.setFieldValue("VBELN", scrVbeln.getFieldValue());
		ldbRowReg.setFieldValue("EBELN", scrEbeln.getFieldValue());
		ldbRowReg.setFieldValue("COMPANY", scrCompany.getFieldValue());
		ldbRowReg.setFieldValue("DRIVER", scrDriver.getFieldValue());
		ldbRowReg.setFieldValue("LICENSENO", scrLicNo.getFieldValue());
		ldbRowReg.setFieldValue("IDNUMBER", scrIDNo.getFieldValue());
		ldbRowReg.setFieldValue("TELNO", scrTelNo.getFieldValue());
		ldbRowReg.setFieldValue("TRANSTYPE", scrTransType.getFieldValue());
		ldbRowReg.setFieldValue("NOAXELS", scrNoAxels.getFieldValue());
		ldbRowReg.setFieldValue("MAXWEIGHT", scrMaxWeight.getFieldValue());		
		ldbRowReg.setFieldValue("AUDAT", date);
		ldbRowReg.setFieldValue("AUTIM", time);
		ldbRowReg.setFieldValue("VTI_REF", scrRefNo.getFieldValue());
		//ldbRowReg.setFieldValue("TIMESTAMP", scrTimestamp.getFieldValue());
		ldbRowReg.setFieldValue("INSPSTATUS", scrInspStatus.getFieldValue());
		Log.trace(1,"Truck " + scrNRegno.getFieldValue() + " register inspect edit status saved to " + ldbRowReg.getFieldValue("INSPSTATUS"));
		ldbRowReg.setFieldValue("DOC_NMBER", scrSalesQuote.getFieldValue());
		ldbRowReg.setFieldValue("GATE_PASS", scrGP.getFieldValue());
		ldbRowReg.setFieldValue("USERID", sessionHeader.getUserId());
		
		//Change incorrect inspection truck from table
			VtiExitLdbSelectCriterion [] inspSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "A"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "W"),
									new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
									new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
									new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			}; 

			VtiExitLdbSelectConditionGroup inspSelCondGrp = new VtiExitLdbSelectConditionGroup(inspSelConds, true);
			VtiExitLdbTableRow[] inspLdbRows = inspLdbTable.getMatchingRows(inspSelCondGrp);
		
			if(inspLdbRows.length == 0)
				return new VtiUserExitResult(999,"Inspection not found.The truck might already be assigned. Check Quick Info at CSC.");	
		
			inspLdbRows[0].setFieldValue("TRUCKREG",scrNRegno.getFieldValue());
			inspLdbRows[0].setFieldValue("TIMESTAMP","");
			
		try
		{
			regLdbTable.saveRow(ldbRowReg);
			inspLdbTable.saveRow(inspLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to Save data to the Registration table and Inspection table.",ee);
			return new VtiUserExitResult(999,"Unable to Save data to the Registration table and Inspection table. Ensure that data was not corrupted by this action.");
		}
			
		try
		{					
		// Trigger the uploads to SAP, if a connection is available.

	
			boolean hostConnected = isHostInterfaceConnected(hostName);

			if (hostConnected)
			{ 
				dbCall.ldbUpload("YSWB_REGISTER", this);
				dbCall.ldbUpload("YSWB_INSPECT", this);
			}
		}
		catch (VtiExitException ee)
		{
				Log.error("Host not connected to SAP to upload register data during Arrival Edit save, check server.");
		}
		
		sessionHeader.setNextFunctionId("YSWB_MAIN");
		return new VtiUserExitResult();
	}
		
}