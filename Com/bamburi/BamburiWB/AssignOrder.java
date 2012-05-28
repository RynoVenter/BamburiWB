package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class AssignOrder extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrRegno = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrSchRegNo = getScreenField("SRCH_REGNO");
		VtiUserExitScreenField scrRegRef = getScreenField("REG_REF");
		VtiUserExitScreenField scrSOSrch = getScreenField("SO_SRCH");
		VtiUserExitScreenField scrPOSrch = getScreenField("PO_SRCH");
		VtiUserExitScreenField scrTOSrch = getScreenField("TO_SRCH");
		VtiUserExitScreenField scrICSech = getScreenField("IC_SRCH");
		VtiUserExitScreenTable tblResults = getScreenTable("TBL_RESULTS");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrDate = getScreenField("DATE");
		VtiUserExitScreenField scrCompany = getScreenField("NAME1");
		VtiUserExitScreenField scrDriver = getScreenField("DRIVER");
		VtiUserExitScreenField scrTimestamp = getScreenField("TIMESTAMP");		
		VtiUserExitScreenField scrUserID = getScreenField("USERID");
		VtiUserExitScreenField scrSalesQuote = getScreenField("VBELNQT");
		VtiUserExitScreenField scrVtiRef = getScreenField("VTI_REF");
		VtiUserExitScreenField scrStatus = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrMax = getScreenField("MAX_LOAD");
		VtiUserExitScreenField scrOvR = getScreenField("OVR_REASON");
		VtiUserExitScreenField scrPref = getScreenField("PREF");
		VtiUserExitScreenField scrGatePass = getScreenField("GATEPASS");
		VtiUserExitScreenField scrDocNum = getScreenField("DOC_NMBER");
		VtiUserExitScreenField scrIsWagon = getScreenField("WAGON");
		VtiUserExitScreenField scrQueue = getScreenField("QUEUE");
		VtiUserExitScreenField scrRegion = getScreenField("REGION");
		VtiUserExitScreenField scrPType = getScreenField("PRODUCT_TYPE");
		VtiUserExitScreenField scrContractor = getScreenField("CONTRACTOR");
		VtiUserExitScreenField scrSelf = getScreenField("SELF");
		VtiUserExitScreenField scrAxles = getScreenField("NOAXELS");
		
		if(scrRegno == null) return new VtiUserExitResult (999,"Failed to initialise TRUCKREG.");
		if(scrSchRegNo == null) return new VtiUserExitResult (999,"Failed to initialise SRCH_REGNO");
		if(scrRegRef == null) return new VtiUserExitResult (999,"Failed to initialise REG_REF");
		if(scrSOSrch == null) return new VtiUserExitResult (999,"Failed to initialise SO_SRCH");
		if(scrPOSrch == null) return new VtiUserExitResult (999,"Failed to initialise PO_SRCH");
		if(scrTOSrch == null) return new VtiUserExitResult (999,"Failed to initialise TO_SRCH");
		if(scrICSech == null) return new VtiUserExitResult (999,"Failed to initialise IC_SRCH");
		if(tblResults == null) return new VtiUserExitResult (999,"Failed to initialise TBL_RESULTS");
		if(scrTime == null) return new VtiUserExitResult (999,"Failed to initialise TIME.");
		if(scrDate == null) return new VtiUserExitResult (999,"Failed to initialise S_DATE.");
		if(scrCompany == null) return new VtiUserExitResult (999,"Failed to initialise NAME1.");
		if(scrDriver == null) return new VtiUserExitResult (999,"Failed to initialise DRIVER.");
		if(scrTimestamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");
		if(scrUserID == null) return new VtiUserExitResult (999,"Failed to initialise USERID.");
		if(scrSalesQuote == null) return new VtiUserExitResult (999,"Failed to initialise VBELNQT.");
		if(scrVtiRef == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrStatus == null) return new VtiUserExitResult (999,"Failed to initialise INSPSTATUS.");
		if(scrMax == null) return new VtiUserExitResult (999,"Failed to initialise MAX_LOAD.");
		if(scrOvR == null) return new VtiUserExitResult (999,"Failed to initialise OVR_REASON.");
		if(scrPref == null) return new VtiUserExitResult (999,"Failed to initialise PREF.");
		if(scrGatePass == null) return new VtiUserExitResult (999,"Failed to initialise GATEPASS.");
		if(scrDocNum == null) return new VtiUserExitResult (999,"Failed to initialise DOC_NMBER.");
		if(scrIsWagon == null) return new VtiUserExitResult (999,"Failed to initialise WAGON.");
		if(scrQueue == null) return new VtiUserExitResult (999,"Failed to initialise QUEUE.");
		if(scrRegion == null) return new VtiUserExitResult (999,"Failed to initialise REGION.");
		if(scrPType == null) return new VtiUserExitResult (999,"Failed to initialise PRODUCT_TYPE.");
		if(scrContractor == null) return new VtiUserExitResult (999,"Failed to initialise CONTRACTOR.");
		if(scrSelf == null) return new VtiUserExitResult (999,"Failed to initialise SELF.");
		if(scrAxles == null) return new VtiUserExitResult (999,"Failed to initialise NOAXLES.");
		
		
		if(scrContractor.getFieldValue().length() + scrSelf.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please select either contractor or self.");
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		
		//Variable Declarations
		
		
		DBCalls dbCalls = new DBCalls();
		String so = "";
		String po = "";
		String ic = "";
		String to = "";
		String sloc = "";
		String rso = "";
		long interval = 0;
		
		String hostName = getHostInterfaceName();
		try
		{
			
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		
				//Database TBL Declaration
		VtiExitLdbTable soHeaderCLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable soItemsCLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable poHeaderCLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable icHeaderLdbTable = getLocalDatabaseTable("YSWB_IC_HEADER");
		VtiExitLdbTable icItemsLdbTable = getLocalDatabaseTable("YSWB_IC_ITEMS");
		
		if (soHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (soItemsCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_ITEMS.");
		if (poHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		if (regLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (inspLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");
		if (confLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (icHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_HEADER.");
		if (icItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_ITEMS.");
		
		
		if((scrContractor.getFieldValue().length() + scrSelf.getFieldValue().length()) == 0)
			return new VtiUserExitResult(999, "Please select  collection type.");
		
		if(scrRegion.getFieldValue().length()== 0)
			return new VtiUserExitResult (999,"Please select the region.");
		
		if(scrPType.getFieldValue().length()== 0)
			return new VtiUserExitResult (999,"Please select the type of product.");
		
		scrSchRegNo.clearPossibleValues();
		scrRegRef.clearPossibleValues();
		scrSOSrch.clearPossibleValues();
		scrPOSrch.clearPossibleValues();
		scrTOSrch.clearPossibleValues();
		scrICSech.clearPossibleValues();
		tblResults.clear();
		

		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_SO"))
		{
			VtiUserExitScreenField scrVbeln = getScreenField("VBELN");
			if(scrVbeln == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
			
			if(scrVbeln.getFieldValue().length()>10 || scrVbeln.getFieldValue().length()<10)
				return new VtiUserExitResult(999, "Please select a valid SAP Inter company order.");
			
			so = scrVbeln.getFieldValue();
			
			if(scrVbeln.getFieldValue().length() > 0 )
				if(scrQueue.getFieldValue().length() == 0)
					if(!scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
						return new VtiUserExitResult(999,"Please select a queue for the sales order.");
			
			if(scrVbeln.getFieldValue().length() >10)
			   return new VtiUserExitResult(999, "Please select a valid SAP sales order.");
			
			if(scrSalesQuote.getFieldValue().length() > 0 && scrVbeln.getFieldValue().length() == 0)
				return new VtiUserExitResult(999,"The Sales Inquiry requires a matching Sales Order.");
		}
		
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_PO"))
		{
			VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
			if(scrEbeln == null) return new VtiUserExitResult (999,"Failed to initialise EBELN.");
			
			po = scrEbeln.getFieldValue();
			
			if(scrEbeln.getFieldValue().length()>10 || scrEbeln.getFieldValue().length()<10)
				return new VtiUserExitResult(999, "Please select a valid SAP purchase order.");
		}
		
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_RSO"))
		{
			VtiUserExitScreenField scrRSONumber = getScreenField("RSONUMBER");
			if(scrRSONumber == null) return new VtiUserExitResult (999,"Failed to initialise RSONUMBER.");
			
			VtiUserExitScreenTable tblRetailSo = getScreenTable("TBL_RETAILSO");
			
			rso = scrRSONumber.getFieldValue();
			
			if(scrRSONumber.getFieldValue().length()>10 || scrRSONumber.getFieldValue().length()<10)
				return new VtiUserExitResult(999, "The retail sales order is not valid.");
			
			if(tblRetailSo.getRowCount() == 0)
				return new VtiUserExitResult(999,"No sales orders has been added to the Retail Sales Order list.");
		}
				
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_IC"))
		{
			VtiUserExitScreenField scrDelNum = getScreenField("EBELN2");
			if(scrDelNum == null) return new VtiUserExitResult (999,"Failed to initialise EBELN2.");
			
			ic = scrDelNum.getFieldValue();
			
			if(scrDelNum.getFieldValue().length()>10 || scrDelNum.getFieldValue().length()<10)
				return new VtiUserExitResult(999, "Please select a valid SAP Inter company order.");
				
		}
		
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_P2P"))
		{
			VtiUserExitScreenField scrSTO = getScreenField("EBELN3");
			VtiUserExitScreenField scrSloc = getScreenField("SLOC");
						
			if(scrSTO == null) return new VtiUserExitResult (999,"Failed to initialise EBELN3.");
			if(scrSloc == null) return new VtiUserExitResult (999,"Failed to initialise SLOC.");
			
			VtiExitLdbSelectCriterion [] slocValSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR,getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR,  getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "SERVERS"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup slocValSelCondGrp = new VtiExitLdbSelectConditionGroup(slocValSelConds, true);
			VtiExitLdbTableRow[] slocValLdbRows = confLdbTable.getMatchingRows(slocValSelCondGrp);

			
			if(slocValLdbRows.length == 0)
				return new VtiUserExitResult(999,1,"Supplying plant not maintained in config.");
			
			String server = slocValLdbRows[0].getFieldValue("KEYVAL4");
			
			VtiExitLdbSelectCriterion [] slocPOSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrSTO.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup slocPOSelCondGrp = new VtiExitLdbSelectConditionGroup(slocPOSelConds, true);
			VtiExitLdbTableRow[] slocPOLdbRows = poHeaderCLdbTable.getMatchingRows(slocPOSelCondGrp);
			
			if(slocPOLdbRows.length == 0)
				return new VtiUserExitResult(999,"STO not found in table to determine sloc requirement.");
			
			String issuePlant = slocPOLdbRows[0].getFieldValue("RESWK");
			
			if(server.equalsIgnoreCase(issuePlant))
			   if(scrSloc.getFieldValue().length() == 0)
				   return new VtiUserExitResult(999,1,"Please select the issuing storage location.");
			
			to = scrSTO.getFieldValue();
			sloc = scrSloc.getFieldValue();
			
			if(scrSTO.getFieldValue().length()>0)
				if(scrGatePass.getFieldValue().length() != 10)
					return new VtiUserExitResult(999, "Please assign a gatepass.");
			
			if(scrSTO.getFieldValue().length()>10  || scrSTO.getFieldValue().length()<10)
			return new VtiUserExitResult(999, "Please select a valid SAP Transfer order.");

			if(scrGatePass.getFieldValue().length() > 0 && scrSTO.getFieldValue().length() == 0)
				return new VtiUserExitResult(999,"The Gate Pass requires a matching Transfer or Inter Company document.");

		}
		
		if(scrRegno.getFieldValue().length() < 1 || scrRegno.getFieldValue().length() > 10)
			return new VtiUserExitResult(999, "Please enter Valid Registration Number.");
		
		if(scrDate.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "Please indicate date of registration.");
		
		if(!scrStatus.getFieldValue().equalsIgnoreCase("P"))
			if(!scrStatus.getFieldValue().equalsIgnoreCase("O"))
				return new VtiUserExitResult(999,"Inspection not done.Do inspection first.");
	
		Date currNow = new Date();
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String errorMsg = "Assignment successful";
		
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		boolean hostConnected = isHostInterfaceConnected(hostName);
		
		DBCalls dbCall = new DBCalls();
		
		VtiExitLdbSelectCriterion [] regSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "A"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
		VtiExitLdbTableRow[] regLdbRows = regLdbTable.getMatchingRows(regSelCondGrp);
						
		if(regLdbRows.length > 0)
		{
			return new VtiUserExitResult(999, "Already has order assigned to truck. Delete truck from system before new arrival.");
		}
		//Save Dataset 
		
		VtiExitLdbSelectCriterion [] regWSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup regWSelCondGrp = new VtiExitLdbSelectConditionGroup(regWSelConds, true);
		VtiExitLdbTableRow[] regWLdbRows = regLdbTable.getMatchingRows(regWSelCondGrp);
						
		if(regWLdbRows.length > 0)
		{
			return new VtiUserExitResult(999, "The truck, " + scrRegno.getFieldValue() + ", is being weighed, reject and remove from system before assigning new order.");
		}
			
		long refNo = 0;

		VtiExitLdbTableRow ldbRowReg = regLdbTable.newRow();
		
		VtiExitLdbSelectCriterion [] registerSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
							new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDate.getFieldValue()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "W"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "A"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
		VtiExitLdbTableRow[] registerLdbRows = regLdbTable.getMatchingRows(registerSelCondGrp);
		
		if(registerLdbRows.length == 0)
			return new VtiUserExitResult(999,"Registration not found. Did the truck pass inspection.");	
		
		//Double check the reg and pick the most recent reg
		int arc = 0;
		String regVtiRef = "";
		long regTim = 0;
		
		for(int rc = 0;rc < registerLdbRows.length; rc++)
		{
			
			if(registerLdbRows[rc].getLongFieldValue("AUTIM") + registerLdbRows[rc].getLongFieldValue("AUDAT")  > regTim);
			{
				regTim = registerLdbRows[rc].getLongFieldValue("AUTIM") + registerLdbRows[rc].getLongFieldValue("AUDAT");
				arc = rc;
			}
		}
		
	//Populate TBL Fields
		if(registerLdbRows[arc].getFieldValue("INSPSTATUS").equalsIgnoreCase("P") || 
		   registerLdbRows[arc].getFieldValue("INSPSTATUS").equalsIgnoreCase("O"))
		{
		}
		else
			return new VtiUserExitResult(999,"Multiple arrivals detected, assignment to latest arrival not posibble due to inspection not done. Remove unwanted arrivals for today from system and try again.");
		
		if(sessionHeader.getFunctionId() == "YSWB_CSC_SO" || so.length() > 0)
		{
			registerLdbRows[arc].setFieldValue("VBELN", so);
			registerLdbRows[arc].setFieldValue("ORD_NUM", so);
		}
		
		if(sessionHeader.getFunctionId() == "YSWB_CSC_RSO")
		{
			registerLdbRows[arc].setFieldValue("VBELN", rso);
			registerLdbRows[arc].setFieldValue("ORD_NUM", rso);
		}
				
		if(sessionHeader.getFunctionId() == "YSWB_CSC_PO" || po.length() > 0)	
		{
			registerLdbRows[arc].setFieldValue("EBELN", po);
			registerLdbRows[arc].setFieldValue("ORD_NUM", po);
		}
		if(sessionHeader.getFunctionId() == "YSWB_CSC_IC" || ic.length() > 0)
		{
			registerLdbRows[arc].setFieldValue("DELIVDOC", ic);
			registerLdbRows[arc].setFieldValue("ORD_NUM", ic);
		}
		if(sessionHeader.getFunctionId() == "YSWB_CSC_P2P" || to.length() > 0)
		{
			registerLdbRows[arc].setFieldValue("STOCKTRNF", to);
			registerLdbRows[arc].setFieldValue("ORD_NUM", to);
		}
		
		registerLdbRows[arc].setFieldValue("TIMESTAMP", "");
		registerLdbRows[arc].setFieldValue("ASSTIME", currLdbTime);
		registerLdbRows[arc].setFieldValue("ASSDATE", currLdbDate);
		registerLdbRows[arc].setFieldValue("REGION", scrRegion.getFieldValue());
		registerLdbRows[arc].setFieldValue("ASSUSER", sessionHeader.getUserId());
		registerLdbRows[arc].setFieldValue("CONTRACTOR", scrContractor.getFieldValue());
		registerLdbRows[arc].setFieldValue("SELF", scrSelf.getFieldValue());
		
		if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
		{
			registerLdbRows[arc].setFieldValue("INSPSTATUS", "W");
		}
		else
		{
			registerLdbRows[arc].setFieldValue("INSPSTATUS", "A");

		}
		
		regVtiRef = registerLdbRows[arc].getFieldValue("VTI_REF");
		
		if(registerLdbRows.length > 1)
		{
			errorMsg = "There was " + registerLdbRows.length + " registrations for " + scrRegno.getFieldValue() + " on " + scrDate.getFieldValue();
		}

		if(regVtiRef.length() == 0)
			return new VtiUserExitResult(999,"Invalid Reference.");
		

		
		//Add Registration Number to SO

	if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_SO"))
	{
		VtiUserExitScreenField scrVbeln = getScreenField("VBELN");
		if(scrVbeln == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
		
		if(scrVbeln.getFieldValue().length() != 0)
		{
			if(scrVbeln.getFieldValue().length() < 10)
				return new VtiUserExitResult(999,"Please validate the Sales Order.");
			
			VtiExitLdbSelectCriterion [] soHeaderCheckSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
				new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVbeln.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup soHeaderCheckSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderCheckSelConds, true);
			VtiExitLdbTableRow[] soHeaderCheckLdbRows = soHeaderCLdbTable.getMatchingRows(soHeaderCheckSelCondGrp);
			
			if(soHeaderCheckLdbRows.length == 0)
				return new VtiUserExitResult(999,"This sales order was not found.");
			
			VtiExitLdbSelectCriterion [] soHeaderCSelConds = 
			{
				new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVbeln.getFieldValue()),
					new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup soHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderCSelConds, true);
			VtiExitLdbTableRow[] soHeaderCLdbRows = soHeaderCLdbTable.getMatchingRows(soHeaderCSelCondGrp);
			
			if(soHeaderCLdbRows.length == 0)
				return new VtiUserExitResult(999,"This Sales Order is not availible for assignment, it has been assigned to another truck.");
			
			VtiExitLdbSelectCriterion [] soItemsCSelConds = 
			{
				new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVbeln.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup soItemsCSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemsCSelConds, true);
			VtiExitLdbTableRow[] soItemsCLdbRows = soHeaderCLdbTable.getMatchingRows(soItemsCSelCondGrp);
			
			if(soHeaderCLdbRows.length == 0 || soItemsCSelConds.length == 0)
					return new VtiUserExitResult(999,"The order's header and items is out of sync. Click on refresh.");
		
			if(soHeaderCLdbRows.length != 0)
			{
				if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
					soHeaderCLdbRows[0].setFieldValue("STATUS", "Weigh 1");
				else
					soHeaderCLdbRows[0].setFieldValue("STATUS", "ASSIGNED");
				
				soHeaderCLdbRows[0].setFieldValue("VBELN", scrVbeln.getFieldValue());
				soHeaderCLdbRows[0].setFieldValue("TRUCK", scrRegno.getFieldValue());
				soHeaderCLdbRows[0].setFieldValue("USERID", scrUserID.getFieldValue());
				soHeaderCLdbRows[0].setFieldValue("TIMESTAMP","");
				soHeaderCLdbRows[0].setFieldValue("VTIREF",regVtiRef);
			
				try
				{
					soHeaderCLdbTable.saveRow(soHeaderCLdbRows[0]);				
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Update Sales Order with Registration No.",ee);
					return new VtiUserExitResult(999,"Unable to Update Sales Order with Registration No.");
				}
		
				try
				{
					regLdbTable.saveRow(registerLdbRows[arc]);

				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save data to the Registration table.",ee);
					return new VtiUserExitResult(999,"Unable to Save data to the Registration table, please try again.");
				}
				
				try
				{
					//Add truck to the queue (on the outbound line)
					GetQ addInQ = new GetQ(this,scrVbeln.getFieldValue(), scrRegno.getFieldValue());
					String queue = "";
					
					//if(queue.equalsIgnoreCase("NOQ1"))
						
						if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
						{
							queue = addInQ.getTruckQ("RAIL");
						}
						else
							queue = addInQ.getTruckQ(scrQueue.getFieldValue());
						
							
					if(queue.length() == 0)
						return new VtiUserExitResult(999,"Q#: " + queue + " truck: " + soHeaderCLdbRows[0].getFieldValue("TRUCK") + " vbeln: " + scrVbeln.getFieldValue() + " time: " + registerLdbRows[arc].getFieldValue("AUTIM")+ " date: " + registerLdbRows[arc].getFieldValue("AUDAT"));
				
					try
					{
						interval = getNextNumberFromNumberRange("YSWB_QPOS");
					}
					catch(VtiExitException ee)
					{
						Log.error("Error creating next queue no.",ee);
						return new VtiUserExitResult(999,"Unable to generate next queue pos no.");
					}
					
					AddToQ qTruck = new AddToQ(this, soHeaderCLdbRows[0].getFieldValue("TRUCK"), scrVbeln.getFieldValue()
											,true, queue, scrPType.getFieldValue(),registerLdbRows[arc].getFieldValue("AUTIM"),registerLdbRows[arc].getFieldValue("AUDAT")
											,interval, registerLdbRows[arc].getFieldValue("DRIVER"));
				
					
					if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
						qTruck.addTruck2Q("WEIGH 1");
					else
						qTruck.addTruck2Q();

				
				}
		
				catch (VtiExitException ee)
				{
					errorMsg = errorMsg + "Truck not added to the queue. Inform Parking Yard to add manually";
				}
			}
		}
	}
	
	//RSO
	if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_RSO"))
	{
		//Assign Sales Order associated with the RSO
		
		
		VtiUserExitScreenTable tblRSO = getScreenTable("TBL_RETAILSO");
		if(tblRSO == null) return new VtiUserExitResult (999,"Failed to initialise TBL_RETAILSO.");
		
		VtiUserExitScreenField scrRSONumber = getScreenField("RSONUMBER");
			if(scrRSONumber == null) return new VtiUserExitResult (999,"Failed to initialise RSONUMBER.");
			
		for(int i = 0;i < tblRSO.getRowCount();i++)
		{
			
			String sVbeln =  tblRSO.getRow(i).getStringFieldValue("RSOVBELN");
			
				VtiExitLdbSelectCriterion [] soHeaderCheckSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, sVbeln),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup soHeaderCheckSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderCheckSelConds, true);
				VtiExitLdbTableRow[] soHeaderCheckLdbRows = soHeaderCLdbTable.getMatchingRows(soHeaderCheckSelCondGrp);
				
				if(soHeaderCheckLdbRows.length == 0)
					return new VtiUserExitResult(999,"Sales order " + sVbeln + " was not found.");
				
				VtiExitLdbSelectCriterion [] soHeaderCSelConds = 
				{
					new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, sVbeln),
						new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup soHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderCSelConds, true);
				VtiExitLdbTableRow[] soHeaderCLdbRows = soHeaderCLdbTable.getMatchingRows(soHeaderCSelCondGrp);
				
				if(soHeaderCLdbRows.length == 0)
					return new VtiUserExitResult(999,"This Sales Order " + sVbeln + " is not availible for assignment, it has been assigned to another truck.");
				
				VtiExitLdbSelectCriterion [] soItemsCSelConds = 
				{
					new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, sVbeln),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup soItemsCSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemsCSelConds, true);
				VtiExitLdbTableRow[] soItemsCLdbRows = soHeaderCLdbTable.getMatchingRows(soItemsCSelCondGrp);
				
				if(soHeaderCLdbRows.length == 0 || soItemsCSelConds.length == 0)
						return new VtiUserExitResult(999,"The order's header and items is out of sync. Click on refresh.");
		}
		
		long soWeight = 0;
		
		for(int c = 0; c < tblRSO.getRowCount();c++)
		{
			 String sVbelnA = tblRSO.getRow(c).getStringFieldValue("RSOVBELN");
				VtiExitLdbSelectCriterion [] soHeaderCSelConds = 
				{
					new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, sVbelnA),
						new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup soHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderCSelConds, true);
				VtiExitLdbTableRow[] soHeaderCLdbRows = soHeaderCLdbTable.getMatchingRows(soHeaderCSelCondGrp);
				
				if(soHeaderCLdbRows.length == 0)
					return new VtiUserExitResult(999,"This Sales Order " + sVbelnA + " is not availible for assignment, it has been assigned to another truck.");
				
				
				if(soHeaderCLdbRows.length != 0)
				{
					soHeaderCLdbRows[0].setFieldValue("STATUS", "ASSIGNED");
					soHeaderCLdbRows[0].setFieldValue("TRUCK", scrRegno.getFieldValue());
					soHeaderCLdbRows[0].setFieldValue("USERID", scrUserID.getFieldValue());
					soHeaderCLdbRows[0].setFieldValue("RETAIL_ORDER", "X");
					soHeaderCLdbRows[0].setFieldValue("DELIVERY", scrRSONumber.getFieldValue());
					soHeaderCLdbRows[0].setFieldValue("TIMESTAMP","");
					soHeaderCLdbRows[0].setFieldValue("VTIREF",regVtiRef);
					
					soWeight = soWeight + soHeaderCLdbRows[0].getLongFieldValue("LSMENGE");
				
					try
					{
						soHeaderCLdbTable.saveRow(soHeaderCLdbRows[0]);				
					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to Update Sales Order with Registration No.",ee);
						return new VtiUserExitResult(999,"Unable to Update Sales Order with Registration No.");
					}
				}
				
				VtiExitLdbTableRow rsoVbelnRow = soHeaderCLdbTable.newRow();
					
					rsoVbelnRow.setFieldValue("SERVERGRP", getServerGroup());
					rsoVbelnRow.setFieldValue("SERVERID", getServerId());
					rsoVbelnRow.setFieldValue("VBELN", scrRSONumber.getFieldValue());
					rsoVbelnRow.setFieldValue("ERNAM", "WIMS");
					rsoVbelnRow.setFieldValue("AUDAT", scrDate.getFieldValue());
					rsoVbelnRow.setFieldValue("NAME1", scrCompany.getFieldValue());
					rsoVbelnRow.setFieldValue("AUART", "ZOR");
					rsoVbelnRow.setFieldValue("STATUS", "ASSIGNED");
					rsoVbelnRow.setFieldValue("LSMENGE", soWeight);
					rsoVbelnRow.setFieldValue("TRUCK", scrRegno.getFieldValue());
					rsoVbelnRow.setFieldValue("USERID", scrUserID.getFieldValue());
					rsoVbelnRow.setFieldValue("TIMESTAMP","");
					rsoVbelnRow.setFieldValue("VTIREF",regVtiRef);
				
					try
					{
						soHeaderCLdbTable.saveRow(rsoVbelnRow);				
					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to Update Sales Order with Registration No.",ee);
						return new VtiUserExitResult(999,"Unable to Update Sales Order with Registration No.");
					}
					
					VtiExitLdbTableRow rsoIVbelnRow = soItemsCLdbTable.newRow();
					
					rsoIVbelnRow.setFieldValue("SERVERGRP", getServerGroup());
					rsoIVbelnRow.setFieldValue("SERVERID", getServerId());
					rsoIVbelnRow.setFieldValue("VBELN", scrRSONumber.getFieldValue());
					rsoIVbelnRow.setFieldValue("POSNR", "10");
					rsoIVbelnRow.setFieldValue("NTGEW", "0");
					rsoIVbelnRow.setFieldValue("MTART", "FERT");
					rsoIVbelnRow.setFieldValue("TIMESTAMP","");
				
					try
					{
						soItemsCLdbTable.saveRow(rsoIVbelnRow);				
					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to Update Sales Order with Registration No.",ee);
						return new VtiUserExitResult(999,"Unable to Update Sales Order with Registration No.");
					}
				
					try
					{
						regLdbTable.saveRow(registerLdbRows[arc]);

					}
					catch (VtiExitException ee)
					{
						Log.error("Unable to Save data to the Registration table.",ee);
						return new VtiUserExitResult(999,"Unable to Save data to the Registration table, please try again.");
					}
					
					try
					{
						//Add truck to the queue (on the outbound line)
						GetQ addInQ = new GetQ(this,rso, scrRegno.getFieldValue());
						String queue = "";
						
						//if(queue.equalsIgnoreCase("NOQ1"))
							
							if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
							{
								queue = addInQ.getTruckQ("RAIL");
							}
							else
								queue = addInQ.getTruckQ(scrQueue.getFieldValue());
							
								
						if(queue.length() == 0)
							return new VtiUserExitResult(999,"Q#: " + queue + " truck: " + soHeaderCLdbRows[0].getFieldValue("TRUCK") + " vbeln: " + rso+ " time: " + registerLdbRows[arc].getFieldValue("AUTIM")+ " date: " + registerLdbRows[arc].getFieldValue("AUDAT"));
					
						try
						{
							interval = getNextNumberFromNumberRange("YSWB_QPOS");
						}
						catch(VtiExitException ee)
						{
							Log.error("Error creating next queue no.",ee);
							return new VtiUserExitResult(999,"Unable to generate next queue pos no.");
						}
						
						AddToQ qTruck = new AddToQ(this, soHeaderCLdbRows[0].getFieldValue("TRUCK"), rso
												,true, queue, scrPType.getFieldValue(),registerLdbRows[arc].getFieldValue("AUTIM"),registerLdbRows[arc].getFieldValue("AUDAT")
												,interval, registerLdbRows[arc].getFieldValue("DRIVER"));
					
						
						if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
							qTruck.addTruck2Q("WEIGH 1");
						else
							qTruck.addTruck2Q();

					
					}
		
					catch (VtiExitException ee)
					{
						errorMsg = errorMsg + "Truck not added to the queue. Inform Parking Yard to add manually";
					}
		}
	}
		
		
		//Add Registration Number to PO

	if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_PO"))
	{
	
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		if(scrEbeln == null) return new VtiUserExitResult (999,"Failed to initialise EBELN.");
		
		if(scrEbeln.getFieldValue().length() != 0)
		{
			if(scrEbeln.getFieldValue().length() < 10)
				return new VtiUserExitResult(999,"Please validate the Purchase Order.");
			
			// REDEV this needs to be changed from hard code. Maintain in config and then match BSART with maintained codes.
			VtiExitLdbSelectCriterion [] nbPoHeaderCSelConds = 
			{
				new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrEbeln.getFieldValue()),
					new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.EQ_OPERATOR, "NB"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup nbPoHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(nbPoHeaderCSelConds, true);
			VtiExitLdbTableRow[] nbPoHeaderCLdbRows = poHeaderCLdbTable.getMatchingRows(nbPoHeaderCSelCondGrp);
			
			VtiExitLdbSelectCriterion [] znbPoHeaderCSelConds = 
			{
				new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrEbeln.getFieldValue()),
					new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.EQ_OPERATOR, "ZNB"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup znbPoHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(znbPoHeaderCSelConds, true);
			VtiExitLdbTableRow[] znbPoHeaderCLdbRows = poHeaderCLdbTable.getMatchingRows(znbPoHeaderCSelCondGrp);
			
			if(znbPoHeaderCLdbRows.length == 0  && nbPoHeaderCLdbRows.length == 0)
				return new VtiUserExitResult(999,"Purchase order not a valid PO.");
				


			if(znbPoHeaderCLdbRows.length != 0 || nbPoHeaderCSelConds.length != 0)
			{

				VtiExitLdbTableRow poStatus = statusLdbTable.newRow();
				
				poStatus.setFieldValue("SERVERGRP",getServerGroup());
				poStatus.setFieldValue("SERVERID",getServerId());
				poStatus.setFieldValue("VTIREF",regVtiRef);
				poStatus.setFieldValue("EBELN",scrEbeln.getFieldValue());
				if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
				{
					poStatus.setFieldValue("STATUS","W");
					poStatus.setFieldValue("WGH_STATUS","Weigh 1");
				}
				else
				{
					poStatus.setFieldValue("STATUS","A");
					poStatus.setFieldValue("WGH_STATUS","ASSIGNED");
				}
				poStatus.setFieldValue("ARR_DATE",scrDate.getFieldValue());
				poStatus.setFieldValue("ARR_TIME",registerLdbRows[arc].getFieldValue("AUTIM"));
				poStatus.setFieldValue("TRUCKREG",scrRegno.getFieldValue());
				//poStatus.setFieldValue("USERID",sessionHeader.getUserId());
				if(znbPoHeaderCLdbRows.length > 0)
				{
					poStatus.setFieldValue("DOCTYPE", "NB");

				}
				else
				{
					poStatus.setFieldValue("DOCTYPE", "ZNB");

				}
				
				poStatus.setFieldValue("TIMESTAMP", "");
				
				
				boolean prefd = false;
				long inspRefNo = 0;
				String inspDate = "";
				String currShift = "";
				String inspShift = "";
				int inspTime = 0;
				String shift = "";
				int shiftStart = 0;
				int shiftEnd = 0;

				//Get inspections done for this truck
				VtiExitLdbSelectCriterion [] inspSelConds = 
				{
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
		  
				VtiExitLdbSelectConditionGroup inspSelCondGrp = new VtiExitLdbSelectConditionGroup(inspSelConds, true);
		
				VtiExitLdbOrderSpecification [] orderBy = 
				{
					new VtiExitLdbOrderSpecification("AUTIM",false),
				};
				VtiExitLdbTableRow[] inspLdbRows = inspLdbTable.getMatchingRows(inspSelCondGrp,orderBy);

			
				
					if(inspLdbRows.length != 0 && scrEbeln.getFieldValue().length() != 0)
					{

						inspRefNo = inspLdbRows[0].getLongFieldValue("VTI_REF");
						inspDate = inspLdbRows[0].getFieldValue("AUDAT");
						inspTime = inspLdbRows[0].getIntegerFieldValue("AUTIM");
						
						inspLdbRows[0].setFieldValue("EBELN", scrEbeln.getFieldValue());
	
						//Get qty of shifts
						VtiExitLdbSelectCriterion [] shiftQTYSelConds = 
						{
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "SHIFT"),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
      
						VtiExitLdbSelectConditionGroup shiftQTYSelCondGrp = new VtiExitLdbSelectConditionGroup(shiftQTYSelConds, true);
						VtiExitLdbTableRow[] shiftQTYLdbRows = confLdbTable.getMatchingRows(shiftQTYSelCondGrp);
			
						if(shiftQTYLdbRows.length == 0 && inspLdbRows[0].getFieldValue("PREFD").equalsIgnoreCase("X"))
							return new VtiUserExitResult(999,"Operational Shifts not maintained in the Config Table.");
						
						if(inspLdbRows[0].getFieldValue("PREF").equalsIgnoreCase("X"))
						{
							
							for(int s = 0;s < shiftQTYLdbRows.length;s++)
							{
								shift = "SHIFT"+(s+1);
						
								VtiExitLdbSelectCriterion [] shiftSelConds = 
								{
									new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "SHIFT"),
										new VtiExitLdbSelectCondition("KEYVAL1", VtiExitLdbSelectCondition.EQ_OPERATOR, shift),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
								};
      
								VtiExitLdbSelectConditionGroup shiftSelCondGrp = new VtiExitLdbSelectConditionGroup(shiftSelConds, true);
								VtiExitLdbTableRow[] shiftLdbRows = confLdbTable.getMatchingRows(shiftSelCondGrp);
						//Determine shift and if shift is with in the parameters of the operational shifts as defined in the config table
								shiftStart = shiftLdbRows[s].getIntegerFieldValue("KEYVAL2");
								shiftEnd = shiftLdbRows[s].getIntegerFieldValue("KEYVAL3");
								Integer t = new Integer(currLdbTime);
								int currIntTime = t.intValue();
						
								if(currIntTime >= shiftStart && currIntTime < shiftEnd)
									currShift = shiftLdbRows[s].getFieldValue("KEYVAL1");
						
								if(inspTime >= shiftStart && inspTime < shiftEnd)
									inspShift = shiftLdbRows[s].getFieldValue("KEYVAL1");
						
								if(inspShift == currShift)
									prefd = true;
							}
						}
							
				//Fill status table with the inspection done included in the record
						if(prefd)
						{
							poStatus.setFieldValue("PREFERED",scrPref.getFieldValue());
						}
							poStatus.setFieldValue("INSP_VTI_REF",inspRefNo);
							poStatus.setFieldValue("INSP_DATE",inspDate);
							poStatus.setFieldValue("INSP_TIME",Integer.toString(inspTime));
						
					}
				try
				{
					inspLdbTable.saveRow(inspLdbRows[0]);
					statusLdbTable.saveRow(poStatus);
				}
				catch(VtiExitException ee)
				{
					Log.error("Failed to save truck to the status table.", ee);
					return new VtiUserExitResult(999,"Failed to assign the PO and truck to the Status table.Tracking and weighin not possible anymore.");
				}
				
				try
				{
					regLdbTable.saveRow(registerLdbRows[arc]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save data to the Registration table.",ee);
					return new VtiUserExitResult(999,"Unable to Save data to the Registration table, please try again.");
				}
			
				//Add truck to the queue (on the inbound line)
				try
				{
				
					GetQ addInQ = new GetQ(this,scrEbeln.getFieldValue(), scrRegno.getFieldValue());
					String queue = "";
					
					//if(queue.equalsIgnoreCase("NOQ1"))
						if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
							queue = addInQ.getTruckQ("RAIL");
						else
							queue = addInQ.getTruckQ();
					
					if(queue.length() == 0)
						return new VtiUserExitResult(999,"Q#: " + queue + " truck: " + scrRegno.getFieldValue() + " ebeln: " + scrEbeln.getFieldValue() + " time: " + registerLdbRows[arc].getFieldValue("AUTIM")+ " date: " + registerLdbRows[arc].getFieldValue("AUDAT"));
				
					try
					{
						interval = getNextNumberFromNumberRange("YSWB_QPOS");
					}
					catch(VtiExitException ee)
					{
						Log.error("Error creating next queue no.",ee);
						return new VtiUserExitResult(999,"Unable to generate next queue pos no.");
					}
					
					AddToQ qTruck = new AddToQ(this, scrRegno.getFieldValue(), scrEbeln.getFieldValue()
											,false, queue, scrPType.getFieldValue(),registerLdbRows[arc].getFieldValue("AUTIM"),registerLdbRows[arc].getFieldValue("AUDAT")
											,interval, registerLdbRows[arc].getFieldValue("DRIVER"));
				
					if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
						qTruck.addTruck2Q("WEIGH 1");
					else
						qTruck.addTruck2Q();
					
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Update Purchase Order with Registration No.",ee);
					errorMsg = errorMsg + "Truck not added to the queue. Inform Parking Yard to add manually";
				}
			}
		}
	}
		
		
		//Del Num's
		//Add Registration Number
							
	if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_IC"))
	{
		VtiUserExitScreenField scrDelNum = getScreenField("EBELN2");
		if(scrDelNum == null) return new VtiUserExitResult (999,"Failed to initialise EBELN2.");
		
		if(scrDelNum.getFieldValue().length() !=0)
		{
			VtiExitLdbSelectCriterion [] statHistSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDelNum.getFieldValue()),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup statHistSelCondGrp = new VtiExitLdbSelectConditionGroup(statHistSelConds, true);
			VtiExitLdbTableRow[] statusWLdbRows = statusLdbTable.getMatchingRows(statHistSelCondGrp);
						
			if(statusWLdbRows.length > 0)
			{
				return new VtiUserExitResult(999, "This order is assigned to a different truck/wagon.");
			}
		
			VtiExitLdbSelectCriterion [] icSelConds = 
			{
				new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDelNum.getFieldValue()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup icSelCondGrp = new VtiExitLdbSelectConditionGroup(icSelConds, true);
			VtiExitLdbTableRow[] icLdbRows = icHeaderLdbTable.getMatchingRows(icSelCondGrp);
			
			if(icLdbRows.length == 0)
				return new VtiUserExitResult(999,"Inter Company order not found in header,either already assigned or header and items is out of sync.");
			
			VtiExitLdbSelectCriterion [] icItemsSelConds = 
			{
				new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDelNum.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup icItemsSelCondGrp = new VtiExitLdbSelectConditionGroup(icItemsSelConds, true);
			VtiExitLdbTableRow[] icItemsLdbRows = icItemsLdbTable.getMatchingRows(icItemsSelCondGrp);
			
			if(icLdbRows.length == 0 || icItemsLdbRows.length == 0)
				return new VtiUserExitResult(999,"Inter Company order not found in items,either already assigned or header and items is out of sync.");
			
			if(icLdbRows.length != 0)
			{
				
				VtiExitLdbTableRow poStatus = statusLdbTable.newRow();
				
				poStatus.setFieldValue("SERVERGRP",getServerGroup());
				poStatus.setFieldValue("SERVERID",getServerId());
				poStatus.setFieldValue("VTIREF",regVtiRef);
				poStatus.setFieldValue("INSP_VTI_REF",regVtiRef);
				poStatus.setFieldValue("DELIVDOC",scrDelNum.getFieldValue());
				if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
				{
					poStatus.setFieldValue("STATUS","W");
					poStatus.setFieldValue("WGH_STATUS","Weigh 1");
				}
				else
				{
					poStatus.setFieldValue("STATUS","A");
					poStatus.setFieldValue("WGH_STATUS","ASSIGNED");
				}
				
				poStatus.setFieldValue("ARR_DATE",scrDate.getFieldValue());
				poStatus.setFieldValue("ARR_TIME",registerLdbRows[arc].getFieldValue("AUTIM"));
				poStatus.setFieldValue("TRUCKREG",scrRegno.getFieldValue());
				//poStatus.setFieldValue("USERID",sessionHeader.getUserId());
				poStatus.setFieldValue("DOCTYPE","ZIC");
				poStatus.setFieldValue("TIMESTAMP", "");
				
				icLdbRows[0].setFieldValue("TRUCKREG",scrRegno.getFieldValue());
				icLdbRows[0].setFieldValue("TRAID",scrRegno.getFieldValue());
				icLdbRows[0].setFieldValue("TRATY","0001");
				icLdbRows[0].setFieldValue("BOLNR",scrDriver.getFieldValue());
				icLdbRows[0].setFieldValue("VTIREF",regVtiRef);
				icLdbRows[0].setFieldValue("TIMESTAMP","");
				icLdbRows[0].setFieldValue("USERID",scrUserID.getFieldValue());
				
				icItemsLdbRows[0].setFieldValue("TRUCKREG",scrRegno.getFieldValue());
				icItemsLdbRows[0].setFieldValue("TIMESTAMP","");
				
				try
				{
					statusLdbTable.saveRow(poStatus);
				}
				catch(VtiExitException ee)
				{
					Log.error("Failed to save IC to status table.", ee);
					return new VtiUserExitResult(999,"Failed to add IC and the truck to the Status table.Tracking not possible anymore.");
				}
				
				try
				{
					icHeaderLdbTable.saveRow(icLdbRows[0]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Failed to update IC to header table.", ee);
					return new VtiUserExitResult(999,"Failed to add the truck to the ic header table.Tracking not possible anymore.");
				}
				
				try
				{
					icItemsLdbTable.saveRow(icItemsLdbRows[0]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Failed to update IC to items table.", ee);
					return new VtiUserExitResult(999,"Failed to add the truck to the ic items table.Tracking not possible anymore.");
				}
			
				try
				{
					regLdbTable.saveRow(registerLdbRows[arc]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save data to the Registration table.",ee);
					return new VtiUserExitResult(999,"Unable to Save data to the Registration table, please try again.");
				}

				try
				{
					//Add truck to the queue (on the outbound line)
					GetQ addInQ = new GetQ(this,scrDelNum.getFieldValue(), scrRegno.getFieldValue());
					String queue = "";
					
					
					if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
						queue = addInQ.getTruckQ("RAIL");
					else
						queue = addInQ.getTruckQ();
						
					
					if(queue.length() == 0)
						return new VtiUserExitResult(999,"Q#: " + queue + " truck: " + scrRegno.getFieldValue() + " vbeln: " + scrDelNum.getFieldValue() + " time: " + registerLdbRows[arc].getFieldValue("AUTIM")+ " date: " + registerLdbRows[arc].getFieldValue("AUDAT"));
				
					try
					{
						interval = getNextNumberFromNumberRange("YSWB_QPOS");
					}
					catch(VtiExitException ee)
					{
						Log.error("Error creating next queue no.",ee);
						return new VtiUserExitResult(999,"Unable to generate next queue pos no.");
					}
					
					AddToQ qTruck = new AddToQ(this, scrRegno.getFieldValue(), scrDelNum.getFieldValue()
											,false, queue, scrPType.getFieldValue(),registerLdbRows[arc].getFieldValue("AUTIM"),registerLdbRows[arc].getFieldValue("AUDAT")
											,interval, registerLdbRows[arc].getFieldValue("DRIVER"));
				
					if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
						qTruck.addTruck2Q("WEIGH 1");
					else
						qTruck.addTruck2Q();
					
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Update Inter Order with Registration No.",ee);
					return new VtiUserExitResult(999,"Unable to update Inter Company Order queue with Registration No.");
				}
			}
		}
	}
		
		//End Del Num's
		
		//STO's
		//Add Registration Number
	if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_P2P"))
	{
		VtiUserExitScreenField scrSTO = getScreenField("EBELN3");
		if(scrSTO == null) return new VtiUserExitResult (999,"Failed to initialise EBELN3.");
		
		if(scrSTO.getFieldValue().length() !=0)
		{
			VtiExitLdbSelectCriterion [] poHeaderCSelConds = 
			{
				new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrSTO.getFieldValue()),
					new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.EQ_OPERATOR, "UB"),
			};
      
			VtiExitLdbSelectConditionGroup poHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(poHeaderCSelConds, true);
			VtiExitLdbTableRow[] poHeaderCLdbRows = poHeaderCLdbTable.getMatchingRows(poHeaderCSelCondGrp);
			
			if(poHeaderCLdbRows.length == 0)
				return new VtiUserExitResult(999,"Not a Transfer order.");
		
			if(poHeaderCLdbRows.length != 0)
			{	
				
				VtiExitLdbTableRow poStatus = statusLdbTable.newRow();
				
				poStatus.setFieldValue("SERVERGRP",getServerGroup());
				poStatus.setFieldValue("SERVERID",getServerId());
				poStatus.setFieldValue("VTIREF",regVtiRef);
				poStatus.setFieldValue("STOCKTRNF",scrSTO.getFieldValue());
				poStatus.setFieldValue("EBELN",scrSTO.getFieldValue());
				if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
				{
					poStatus.setFieldValue("STATUS","W");
					poStatus.setFieldValue("WGH_STATUS","Weigh 1");
				}
				else
				{
					poStatus.setFieldValue("STATUS","A");
					poStatus.setFieldValue("WGH_STATUS","ASSIGNED");
				}
				poStatus.setFieldValue("ARR_DATE",registerLdbRows[arc].getFieldValue("AUDAT"));
				poStatus.setFieldValue("ARR_TIME",registerLdbRows[arc].getFieldValue("AUTIM"));
				poStatus.setFieldValue("TRUCKREG",scrRegno.getFieldValue());
				//poStatus.setFieldValue("USERID",sessionHeader.getUserId());
				poStatus.setFieldValue("DOCTYPE", "UB");	
				poStatus.setFieldValue("LGORT", sloc);
				poStatus.setFieldValue("INSP_VTI_REF", regVtiRef);
							
				VtiExitLdbSelectCriterion [] inspSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVtiRef.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
				VtiExitLdbSelectConditionGroup inspSelCondGrp = new VtiExitLdbSelectConditionGroup(inspSelConds, true);
				VtiExitLdbTableRow[] inspLdbRows = inspLdbTable.getMatchingRows(inspSelCondGrp);
		
				if(inspLdbRows.length == 0)
					return new VtiUserExitResult(999,"Inspection details for truck not found.");
				
				inspLdbRows[0].setFieldValue("STOCKTRNF", scrSTO.getFieldValue());
				
				try
				{
					inspLdbTable.saveRow(inspLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
				}

			
				poStatus.setFieldValue("TIMESTAMP", "");
				
				try
				{
					statusLdbTable.saveRow(poStatus);
				}
				catch(VtiExitException ee)
				{
					Log.error("Failed to assign sto to truck",ee);
					return new VtiUserExitResult(999,"Failed to add the truck to the Status table.Tracking not possible anymore.");
				}
			
				try
				{
					regLdbTable.saveRow(registerLdbRows[arc]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Save data to the Registration table.",ee);
					return new VtiUserExitResult(999,"Unable to Save data to the Registration table, please try again.");
				}
				
				try
				{
					//Add truck to the queue (on the outbound line)
					GetQ addInQ = new GetQ(this,scrSTO.getFieldValue(), scrRegno.getFieldValue());
					String queue = "";
							
					if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
						queue = addInQ.getTruckQ("RAIL");
					else
						queue = addInQ.getTruckQ();
					
					if(queue.length() == 0)
						return new VtiUserExitResult(999,"Q#: " + queue + " truck: " + scrRegno.getFieldValue() + " vbeln: " + scrSTO.getFieldValue() + " time: " + registerLdbRows[arc].getFieldValue("AUTIM")+ " date: " + registerLdbRows[arc].getFieldValue("AUDAT"));
				
					try
					{
						interval = getNextNumberFromNumberRange("YSWB_QPOS");
					}
					catch(VtiExitException ee)
					{
						Log.error("Error creating next queue no.",ee);
						return new VtiUserExitResult(999,"Unable to generate next queue pos no.");
					}
					
					AddToQ qTruck = new AddToQ(this, scrRegno.getFieldValue(), scrSTO.getFieldValue()
											,false, queue, scrPType.getFieldValue(),registerLdbRows[arc].getFieldValue("AUTIM"),registerLdbRows[arc].getFieldValue("AUDAT")
											,interval, registerLdbRows[arc].getFieldValue("DRIVER"));
				
					if(scrIsWagon.getFieldValue().equalsIgnoreCase("X"))
						qTruck.addTruck2Q("WEIGH 1");
					else
						qTruck.addTruck2Q();
					
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to Update Purchase Order with Registration No.",ee);
					errorMsg = errorMsg + "Truck not added to the queue. Inform Parking Yard to add manually";
				}
			}
		}
	}

		//End STO's

		
		try
		{					

		// Trigger the uploads to SAP, if a connection is available.
				hostConnected = isHostInterfaceConnected(hostName);

				if (hostConnected)
				{ 
					dbCall.ldbUpload("YSWB_REGISTER", this);
					dbCall.ldbUpload("YSWB_INSPECT", this);
					dbCall.ldbUpload("YSWB_STATUS", this);
					dbCall.ldbUpload("YSWB_SO_HEADER", this);
					dbCall.ldbUpload("YSWB_IC_HEADER", this);
				}
		}
		catch (VtiExitException ee)
		{
				Log.error("Host not connected to SAP to upload register data during Arrival save, check server.");
		}
		
		
		scrRegno.setFieldValue("");
		scrMax.setFieldValue("");
		scrTime.setFieldValue("");
		scrDate.setFieldValue("");
		scrCompany.setFieldValue("");
		scrDriver.setFieldValue("");
		scrSalesQuote.setFieldValue("");
		
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_SO"))
		{
			VtiUserExitScreenField scrVbeln = getScreenField("VBELN");
			scrVbeln.setFieldValue("");
		}
		
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_PO"))
		{
			VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
			scrEbeln.setFieldValue("");
		}
		
		scrVtiRef.setFieldValue("");
		scrStatus.setFieldValue("");
		scrMax.setFieldValue("");
		scrOvR.setFieldValue("");
		scrGatePass.setFieldValue("");
		scrPref.setFieldValue("");
		
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_IC"))
		{
			VtiUserExitScreenField scrDelNum = getScreenField("EBELN2");
			scrDelNum.setFieldValue("");
		}
		
		scrDocNum.setFieldValue("");
		
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_CSC_P2P"))	
		{
			VtiUserExitScreenField scrSTO = getScreenField("EBELN3");
			VtiUserExitScreenField scrSloc = getScreenField("SLOC");
			scrSloc.setFieldValue("");
			scrSTO.setFieldValue("");
		}
		
		dbCalls.ldbUpload("YSWB_QUEUE", this);
		
		scrIsWagon.setFieldValue("");
		scrQueue.setFieldValue("");
		scrRegion.setFieldValue("");
		scrPType.setFieldValue("");
		scrContractor.setFieldValue("");
		scrSelf.setFieldValue("");
		scrAxles.setFieldValue("");
		
		return new VtiUserExitResult(000,1,errorMsg);
	}
}
