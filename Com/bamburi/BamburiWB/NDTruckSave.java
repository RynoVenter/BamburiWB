package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class NDTruckSave extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrRBWeigh1 = getScreenField("RB_WEIGH1");
		VtiUserExitScreenField scrRBWeigh2 = getScreenField("RB_WEIGH2");
		VtiUserExitScreenField scrFWeight = getScreenField("WEIGHT");
		VtiUserExitScreenField scrFWeight1 = getScreenField("WEIGHT1");
		VtiUserExitScreenField scrFWTStamp1 = getScreenField("WGH1_TIMESTAMP");
		VtiUserExitScreenField scrFWeight2 = getScreenField("WEIGHT2");
		VtiUserExitScreenField scrFWTStamp2 = getScreenField("WGH2_TIMESTAMP");
		VtiUserExitScreenField scrChkPrn = getScreenField("CHK_PRINT");
		VtiUserExitScreenField scrFNettW = getScreenField("NETT_WEIGHT");
		VtiUserExitScreenField scrFNettTS = getScreenField("NETT_TIMESTAMP");
		VtiUserExitScreenField scrWStamp = getScreenField("TIMESTAMP");
		VtiUserExitScreenField scrFIsStuck = getScreenField("IS_STUCK");	
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		VtiUserExitScreenField scrMatDisc = getScreenField("ARKTX");
		VtiUserExitScreenField scrMatNr = getScreenField("MATNR");
		VtiUserExitScreenField scrDestination = getScreenField("DESTINATION");
		VtiUserExitScreenField scrVendor = getScreenField("LIFNR");
		VtiUserExitScreenField scrSloc = getScreenField("LGORT");
		VtiUserExitScreenField scrCostCentre = getScreenField("KOSTL");
		VtiUserExitScreenField scrGL = getScreenField("GENERALLEDGER");
		

		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrRBWeigh1 == null) return new VtiUserExitResult (999,"Failed to initialise RB_WEIGH1.");
		if(scrRBWeigh2 == null) return new VtiUserExitResult (999,"Failed to initialise RB_WEIGH2.");
		if(scrFWeight == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT.");
		if(scrFWeight1 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT1.");
		if(scrFWTStamp1 == null) return new VtiUserExitResult (999,"Failed to initialise WGH1_TIMESTAMP.");
		if(scrFWeight2 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT2.");
		if(scrFWTStamp2 == null) return new VtiUserExitResult (999,"Failed to initialise WGH2_TIMESTAMP.");
		if(scrChkPrn == null) return new VtiUserExitResult (999,"Failed to initialise CHK_PRINT.");
		if(scrFNettW == null) return new VtiUserExitResult (999,"Failed to initialise NETT_WEIGHT.");
		if(scrFNettTS == null) return new VtiUserExitResult (999,"Failed to initialise NETT_TIMESTAMP.");
		if(scrWStamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");
		if(scrFIsStuck == null) return new VtiUserExitResult (999,"Failed to initialise IS_STUCK.");
		if(scrRegNo == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");

		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");

		if(scrFWeight1.getFieldValue().length() == 0 || scrFWeight2.getFieldValue().length() == 0)
			return new VtiUserExitResult (999,"No weight measured.");
		
		
		//Variable Declarations
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String weighTS = currDate + " " + currTime;
		String customer = "";
		String truckRno = "";
		String docType = "EBELN";
		DBCalls dbCall = new DBCalls();
		FormatUtilities fu = new FormatUtilities();
		
		String hostName = getHostInterfaceName();
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		
		long w1 = 0;
		long w2 = 0;
		long nett = 0;
		
		long tranQNo = 0;

		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Purchase Order is not ready for processing, please check the status.");
		
		//Database TBL Declaration
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable poILdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable tranQueueLdbTable = getLocalDatabaseTable("YSWB_TRAN_QUEUE");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (poILdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (queueLdbTable == null) return new VtiUserExitResult (999,"Failed to initialise YSWB_QUEUE.");
		if (tranQueueLdbTable == null) return new VtiUserExitResult (999,"Failed to initialise YSWB_TRAN_QUEUE.");
		if (statusLdbTable == null) return new VtiUserExitResult (999,"Failed to initialise YSWB_TRAN_QUEUE.");
		
		//WB Dataset 
		
		VtiExitLdbSelectCriterion [] wbSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFSlip.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
		VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);
		
		if(wbLdbRows.length == 0)
			return new VtiUserExitResult(999,"Weighbridge detail of weigh-in not found.");
		
		if(wbLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("Failed"))
		{
			return new VtiUserExitResult(999, "No vehicle inspection. No weigh in allowed.");
		}
		
		if(wbLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("Weigh 1"))
		{
			return new VtiUserExitResult(999, "No weigh 2 data yet. Save not allowed.");
		}
		
		
		//Set next Function
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		
		//Change Status
		
			VtiExitLdbSelectCriterion [] regSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
			VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
			
			if(regLdbRows.length == 0)
			if(wbLdbRows[0].getDoubleFieldValue("NETTWEIGHT") > 0)
				{
					VtiExitLdbSelectCriterion [] regCSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup regCSelCondGrp = new VtiExitLdbSelectConditionGroup(regCSelConds, true);
					VtiExitLdbTableRow[] regCLdbRows = registerLdbTable.getMatchingRows(regCSelCondGrp);
			
					regCLdbRows[0].setFieldValue("INSPSTATUS","W");
					
					try
					{
						registerLdbTable.saveRow(regCLdbRows[0]);
					}
					catch(VtiExitException ee)
					{
						Log.error("WB reg correction save failed.", ee);
					}
				}
			
			regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
			
			if(regLdbRows.length == 0)
				return new VtiUserExitResult(999,"Truck not found in the register.");
			
			if(scrDestination.getFieldValue().length() == 0 
			   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
				return new VtiUserExitResult(999,1,"Please indicate Destination.");
				
			if(scrMatNr.getFieldValue().length() == 0
			   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
				return new VtiUserExitResult(999,1,"Please indicate Material.");
		
			if(scrVendor.getFieldValue().length() == 0
			   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
				return new VtiUserExitResult(999,1,"Please indicate Vendor.");
		
			if(scrSloc.getFieldValue().length() == 0
			   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
				return new VtiUserExitResult(999,1,"Please indicate SLOC.");
		
			if(scrCostCentre.getFieldValue().length() == 0
			   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
				return new VtiUserExitResult(999,1,"Please indicate Cost Centre.");
		
			if(scrGL.getFieldValue().length() == 0
			   && !regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
				return new VtiUserExitResult(999,1,"Please indicate GL.");
			
			if(regLdbRows[0].getFieldValue("EBELN").length() == 10)
			{
				scrEbeln.setFieldValue(regLdbRows[0].getFieldValue("EBELN"));
				
				VtiExitLdbSelectCriterion [] poSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, regLdbRows[0].getFieldValue("EBELN")),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
					
				VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
				VtiExitLdbTableRow[] poLdbRows = poILdbTable.getMatchingRows(poSelCondGrp);
				
				if(poLdbRows.length == 0)
				{
					scrEbeln.setFieldValue("");
					return new VtiUserExitResult(999,1,"The PO " + regLdbRows[0].getFieldValue("EBELN") + " was not found in the Purchase order table. The material could not be determined.");			
				}
			}
			else
				scrEbeln.setFieldValue("");
			
		if(scrFIsStuck.getFieldValue().equalsIgnoreCase("X"))
		{
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
			
			truckRno = wbLdbRows[0].getFieldValue("TRUCKREG");
			VtiExitLdbTableRow registerLdbRow = registerLdbTable.newRow();
			
			registerLdbRow.setFieldValue("SERVERGRP",regLdbRows[0].getFieldValue("SERVERGRP"));
			registerLdbRow.setFieldValue("SERVERID",regLdbRows[0].getFieldValue("SERVERID"));
			registerLdbRow.setFieldValue("AUDAT",currLdbDate);
			registerLdbRow.setFieldValue("TRUCKREG",regLdbRows[0].getFieldValue("TRUCKREG"));
			registerLdbRow.setFieldValue("AUTIM",currLdbTime);
			registerLdbRow.setFieldValue("SELF",regLdbRows[0].getFieldValue("SELF"));
			registerLdbRow.setFieldValue("CONTRACTOR",regLdbRows[0].getFieldValue("CONTRACTOR"));
			registerLdbRow.setFieldValue("COMPANY",regLdbRows[0].getFieldValue("COMPANY"));
			registerLdbRow.setFieldValue("DRIVER",regLdbRows[0].getFieldValue("DRIVER"));
			registerLdbRow.setFieldValue("IDNUMBER",regLdbRows[0].getFieldValue("IDNUMBER"));
			registerLdbRow.setFieldValue("TRANSTYPE",regLdbRows[0].getFieldValue("TRANSTYPE"));
			registerLdbRow.setFieldValue("NOAXELS",regLdbRows[0].getFieldValue("NOAXELS"));
			registerLdbRow.setFieldValue("MAXWEIGHT",regLdbRows[0].getFieldValue("MAXWEIGHT"));
			registerLdbRow.setFieldValue(docType,regLdbRows[0].getFieldValue(docType));
			registerLdbRow.setFieldValue("TRANSPORTER",regLdbRows[0].getFieldValue("TRANSPORTER"));

			registerLdbRow.setFieldValue("INSPSTATUS","A");
			registerLdbRow.setFieldValue("VTI_REF",refNo);
			registerLdbRow.setFieldValue("TIMESTAMP","");
			
			try
			{
				registerLdbTable.saveRow(registerLdbRow);
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating a new Register entry for the stuck load.",ee);
				return new VtiUserExitResult(999,"Error creating a new Register entry for the stuck load.");
			}
		}
		else
		{
			truckRno = wbLdbRows[0].getFieldValue("TRUCKREG");
		}
		
		regLdbRows[0].setFieldValue("INSPSTATUS","C");
		regLdbRows[0].setFieldValue("DEPTIME",currLdbTime);
		regLdbRows[0].setFieldValue("TIMESTAMP","");
		
		try
		{
			registerLdbTable.saveRow(regLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Error updating Register status, please try again.",ee);
			return new VtiUserExitResult(999,"Unable to change the Register.");
		}
		
		
		VtiExitLdbSelectCriterion [] statusSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "WEIGH 2"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
		VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
		
		if(statusLdbRows.length == 0)
			return new VtiUserExitResult(999, "Could not find the truck status details to complete.");
		
		statusLdbRows[0].setFieldValue("STATUS","C");
		statusLdbRows[0].setFieldValue("WGH_STATUS","Complete");
		statusLdbRows[0].setFieldValue("TIMESTAMP", "");
		
		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
			if(qTLdbRows.length > 0)
			{			
				qTLdbRows[0].setFieldValue("Q_STATUS","Complete");
				qTLdbRows[0].setFieldValue("TIMESTAMP", "");
			}
		//Set WB Custom Fields
		
		int tr = 0; 
		
		VtiUserExitScreenTableRow wghbr = scrTblCustom.getRow(tr);
		VtiUserExitScreenTableRow trckReg = scrTblCustom.getRow(tr + 1);
		VtiUserExitScreenTableRow driv = scrTblCustom.getRow(tr + 2);
		VtiUserExitScreenTableRow custsupp = scrTblCustom.getRow(tr + 3);
		VtiUserExitScreenTableRow transprtr = scrTblCustom.getRow(tr + 4);
		VtiUserExitScreenTableRow tranType = scrTblCustom.getRow(tr + 5);
		VtiUserExitScreenTableRow allwgh = scrTblCustom.getRow(tr + 6);
		VtiUserExitScreenTableRow ordNo = scrTblCustom.getRow(tr + 7);
		VtiUserExitScreenTableRow rebag = scrTblCustom.getRow(tr + 8);
		VtiUserExitScreenTableRow packline = scrTblCustom.getRow(tr + 9);
		VtiUserExitScreenTableRow packLoad = scrTblCustom.getRow(tr + 10);
		VtiUserExitScreenTableRow segtype = scrTblCustom.getRow(tr + 11);
		VtiUserExitScreenTableRow shift = scrTblCustom.getRow(tr + 12);
		VtiUserExitScreenTableRow remarks = scrTblCustom.getRow(tr + 13);
		VtiUserExitScreenTableRow tralnum = scrTblCustom.getRow(tr + 14);

		wbLdbRows[0].setFieldValue("WEIGHBRIDGE2",wghbr.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("TRUCKREG",trckReg.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("DRIVER",driv.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("CUSTOMER",custsupp.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("EBELN",scrEbeln.getFieldValue());
		wbLdbRows[0].setFieldValue("TRANSPORTTYPE",tranType.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("ALLOC_WHT",allwgh.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("DELVNO",ordNo.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("REBAG",rebag.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("PACKLINE",packline.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("PACKLOADER",packLoad.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("SEGMENT",segtype.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("SHIFT",shift.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("REMARKS",remarks.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("TRAILERNO",tralnum.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
		wbLdbRows[0].setFieldValue("TIMESTAMP","");
		
		if(!regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
		{
			try
			{
				tranQNo = 0;
				tranQNo = getNextNumberFromNumberRange("YSWB_TRANS");
			}
			catch(VtiExitException ee)
			{
				Log.error("Unable to create number from YSWB_TRANS",ee);
				return new VtiUserExitResult(999,"Unable to retrieve a transaction number from number range YSWB_TRANS");
			}
					
			VtiExitLdbTableRow tranQueueRow = tranQueueLdbTable.newRow();
			tranQueueRow.setFieldValue("SERVERID", getServerId());
			tranQueueRow.setFieldValue("TRAN_NO", scrFSlip.getFieldValue());
			tranQueueRow.setFieldValue("TRUCK", scrRegNo.getFieldValue());
			tranQueueRow.setFieldValue("VTIREF", scrVRef.getFieldValue());
			if(scrEbeln.getFieldValue().length() > 0 
			   && regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("ROTATE OW"))
			{
				tranQueueRow.setFieldValue("TRANTYPE", "GR");
				tranQueueRow.setFieldValue("EBELN",scrEbeln.getFieldValue());
			}
			else
				tranQueueRow.setFieldValue("TRANTYPE", "GI_PROD");
			
			tranQueueRow.setFieldValue("TIMESTAMP", "");
			tranQueueRow.setFieldValue("TRANDATE", currLdbDate);
											
			try
			{	 
				tranQueueLdbTable.saveRow(tranQueueRow);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable to Save data to the YSWB_TRAN_QUEUE table.",ee);
				return new VtiUserExitResult(999,"Unable to Save data to the Transaction Queue table.");
			}
		}
		try
		{
			wbLdbTable.saveRow(wbLdbRows[0]);
			statusLdbTable.saveRow(statusLdbRows[0]);
			if(qTLdbRows.length > 0)
			{
				queueLdbTable.saveRow(qTLdbRows[0]);
			}
			
			boolean hostConnected = isHostInterfaceConnected(hostName);
			hostConnected = isHostInterfaceConnected(hostName);

			if (hostConnected)
			{ 
				dbCall.ldbUpload("YSWB_WB", this);
				dbCall.ldbUpload("YSWB_TRAN_QUEUE", this);
				dbCall.ldbUpload("YSWB_INSPECT", this);
				dbCall.ldbUpload("YSWB_REGISTER", this);
				dbCall.ldbUpload("YSWB_STATUS", this);
			}
			
		}
		catch (VtiExitException ee)
		{
			Log.error("Error updating weighin status, please try again.",ee);
			return new VtiUserExitResult(999,"Unable to update Sales Order, check status.");
		}
		
//Print Slip
			StringBuffer Header = new StringBuffer();
			StringBuffer ptype = new StringBuffer();
			StringBuffer addDet = new StringBuffer();
			StringBuffer slipN = new StringBuffer();
			StringBuffer soTime = new StringBuffer();
			StringBuffer oNum = new StringBuffer();
			StringBuffer truck = new StringBuffer();
			StringBuffer cust = new StringBuffer();
			StringBuffer trnsprt = new StringBuffer();
			StringBuffer trlr = new StringBuffer();
			StringBuffer dNote = new StringBuffer();
			StringBuffer pType = new StringBuffer();
			StringBuffer allocWght = new StringBuffer();
			StringBuffer product = new StringBuffer();
			StringBuffer d1 = new StringBuffer();
			StringBuffer d2 = new StringBuffer();
			StringBuffer t1 = new StringBuffer();
			StringBuffer t2 = new StringBuffer();
			StringBuffer wh1 = new StringBuffer();
			StringBuffer wh2 = new StringBuffer();
			StringBuffer nettw = new StringBuffer();
			StringBuffer wb1 = new StringBuffer();
			StringBuffer wb2 = new StringBuffer();
			StringBuffer user = new StringBuffer();
			StringBuffer driver = new StringBuffer();
			StringBuffer pl = new StringBuffer();
			
			StringBuffer feedFiller = new StringBuffer();
		
		if(wbLdbRows.length == 0)
			return new VtiUserExitResult(999, "The weighbridge slip cannot be printed, the second weight have not been recorded yet.");
		
		if(scrChkPrn.getFieldValue().equalsIgnoreCase("X"))
		{
			 feedFiller.append(System.getProperty("line.separator"));
			
			 Header.append("NAIROBI GRINDING PLANT");
			 addDet.append("P.O Box 524, ATHI RIVER");
			 oNum.append(scrEbeln.getFieldValue());
			 product.append(scrMatDisc.getFieldValue());
			 ptype.append("Purchase Order");
			 slipN.append(scrFSlip.getFieldValue());
			 soTime.append(currTime);
			 truck.append(wbLdbRows[0].getFieldValue("TRUCKREG"));
			 cust.append(customer);
			 trnsprt.append(wbLdbRows[0].getFieldValue("TRANSPORTER"));
			 trlr.append(wbLdbRows[0].getFieldValue("TRAILERNO"));
			 dNote.append(wbLdbRows[0].getFieldValue("DELVNO"));
			 pType.append(wbLdbRows[0].getFieldValue("TRANSPORTTYPE"));
			 allocWght.append(wbLdbRows[0].getFieldValue("ALLOC_WHT"));
			 d1.append(fu.shortDate(wbLdbRows[0].getStringFieldValue("WEIGHT1_D")));
			 d2.append(fu.shortDate(wbLdbRows[0].getStringFieldValue("WEIGHT2_D")));
			 t1.append(fu.shortTime(wbLdbRows[0].getStringFieldValue("WEIGHT1_T")));
			 t2.append(fu.shortTime(wbLdbRows[0].getStringFieldValue("WEIGHT2_T")));
			 wh1.append(wbLdbRows[0].getFieldValue("WEIGHT1"));
			 wh2.append(wbLdbRows[0].getFieldValue("WEIGHT2"));
			 nettw.append(wbLdbRows[0].getFieldValue("NETTWEIGHT"));
			 wb1.append(wbLdbRows[0].getFieldValue("WEIGHBRIDGE"));
			 wb2.append(wbLdbRows[0].getFieldValue("WEIGHBRIDGE2"));
			 user.append(sessionHeader.getUserId());
			 driver.append(wbLdbRows[0].getFieldValue("DRIVER"));
			 pl.append(wbLdbRows[0].getFieldValue("PACKLINE"));
			 
			VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", Header.toString()),
				new VtiExitKeyValuePair("&addDet&", addDet.toString()),
				new VtiExitKeyValuePair("&slipN&", slipN.toString()),
				new VtiExitKeyValuePair("&onum&", oNum.toString()),
				new VtiExitKeyValuePair("&otype&", ptype.toString()),
				new VtiExitKeyValuePair("&soTime&", soTime.toString()),
				new VtiExitKeyValuePair("&truck&", truck.toString()),
				new VtiExitKeyValuePair("&cust&", cust.toString()),
				new VtiExitKeyValuePair("&trnsprt&", trnsprt.toString()),
				new VtiExitKeyValuePair("&trlr&", trlr.toString()),
				new VtiExitKeyValuePair("&dNote&", dNote.toString()),
				new VtiExitKeyValuePair("&pType&", pType.toString()),
				new VtiExitKeyValuePair("&allocWght&", allocWght.toString()),
				new VtiExitKeyValuePair("&product&", product.toString()),
				new VtiExitKeyValuePair("&d1&", d1.toString()),
				new VtiExitKeyValuePair("&d2&", d2.toString()),
				new VtiExitKeyValuePair("&t1&", t1.toString()),
				new VtiExitKeyValuePair("&t2&", t2.toString()),
				new VtiExitKeyValuePair("&w1&", wh1.toString()),
				new VtiExitKeyValuePair("&w2&", wh2.toString()),
				new VtiExitKeyValuePair("&nett&", nettw.toString()),
				new VtiExitKeyValuePair("&wb1&", wb1.toString()),
				new VtiExitKeyValuePair("&wb2&", wb2.toString()),
				new VtiExitKeyValuePair("&user&", user.toString()),
				new VtiExitKeyValuePair("&driver&", driver.toString()),
				new VtiExitKeyValuePair("&pl&", pl.toString()),
			};
					
			VtiUserExitHeaderInfo headerInfo = getHeaderInfo();		
			int deviceNumber = headerInfo.getDeviceNumber();
			//Invoking the print
			try
			{
				invokePrintTemplate("WBSlip" + deviceNumber, keyValuePairs);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error with Printout", ee);
				//return new VtiUserExitResult(999, "Printout Failed.");
			}
		}
		sessionHeader.setNextFunctionId("YSWB_MAIN");
		
		ArchiveWB(scrRegNo.getFieldValue(), scrVRef.getFieldValue());

		return new VtiUserExitResult();
	}
	
	private String ArchiveWB(String sRegNo, String sVti) throws VtiExitException
	{
		String sErrorMsg = "";
		
		if(sVti.length() > 0)
		{
			
			Date currNow = new Date();
			String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
			String currLdbTime = DateFormatter.format("HHmmss", currNow);
		
		
			VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		
			if (wbLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_WB.";
		
			//WB
			VtiExitLdbSelectCriterion [] wbArcSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
								new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.NE_OPERATOR, sVti),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup wbArcSelCondGrp = new VtiExitLdbSelectConditionGroup(wbArcSelConds, true);
			VtiExitLdbTableRow[] wbArcLdbRows = wbLdbTable.getMatchingRows(wbArcSelCondGrp);
		
			if(wbArcLdbRows.length > 0)
			{
				for(int i =0; i < wbArcLdbRows.length;i++)
				{
					if(!wbArcLdbRows[i].getFieldValue("VTIREFA").equalsIgnoreCase(sVti) && sVti.length() > 0)
				    {
						wbArcLdbRows[i].setFieldValue("DEL_IND","X");
						wbArcLdbRows[i].setFieldValue("TIMESTAMP","");	
						
						try
						{
							wbLdbTable.saveRow(wbArcLdbRows[i]);
						}
						catch(VtiExitException ee)
						{
							Log.error("WB not archiving.", ee);
						}
					}
				}
			}

		}
		
		return sErrorMsg;
	}
}
