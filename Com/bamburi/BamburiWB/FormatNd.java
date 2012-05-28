package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatNd extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
	//Generate Slip number into vti_ref field from number range object from yswb_slip
		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrWFPurchOrd = getScreenField("EBELN");
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
		VtiUserExitScreenField scrFStat = getScreenField("STAT");
		VtiUserExitScreenField btnOk = getScreenField("BT_OKAY");
		VtiUserExitScreenField btnReject = getScreenField("BT_REJECT");
		VtiUserExitScreenField btnSave = getScreenField("BT_SAVE");
		VtiUserExitScreenField btnTare = getScreenField("GET_TARE");
		VtiUserExitScreenField cmbWeighBridge = getScreenField("WEIGHBRIDGE");
		VtiUserExitScreenField scfTolWarning = getScreenField("TOLL_MESSAGE");
		VtiUserExitScreenField scrFIsStuck = getScreenField("IS_STUCK");
		VtiUserExitScreenField btnBack = getScreenField("BT_BACK");
		VtiUserExitScreenField btnRePrint = getScreenField("BT_PRINT");
		
		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrWFPurchOrd == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
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
		if(btnOk == null) return new VtiUserExitResult (999,"Failed to initialise BT_OKAY.");
		if(btnReject == null) return new VtiUserExitResult (999,"Failed to initialise BT_REJECT.");
		if(btnSave == null) return new VtiUserExitResult (999,"Failed to initialise BT_SAVE.");
		if(btnTare == null) return new VtiUserExitResult (999,"Failed to initialise GET_TARE.");
		if(cmbWeighBridge == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHBRIDGE.");
		if(scfTolWarning == null) return new VtiUserExitResult (999,"Failed to initialise scfTolWarning.");
		if(scrFIsStuck == null) return new VtiUserExitResult (999,"Failed to initialise IS_STUCK.");
		if(btnBack == null) return new VtiUserExitResult (999,"Failed to initialise BT_BACK.");
		if(btnRePrint == null) return new VtiUserExitResult (999,"Failed to initialise BT_PRINT.");
		
		scrFIsStuck.setFieldValue("");
		btnBack.setHiddenFlag(false);
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
		VtiExitLdbTable poHeaderLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable poItemsLdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable logonLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		
		if (poHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (poItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_Items.");
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (logonLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		
		long slipNo = 0;
		double allocWgh = 0;
		String poStatus ="";
		String whBridge ="";
		FormatUtilities fu = new FormatUtilities();
		btnRePrint.setHiddenFlag(true);
		btnTare.setHiddenFlag(true);
		scfTolWarning.setHiddenFlag(true);
	//If status from sales order is new or changed or rejected then it's 1 weight, else set to weight 2
		VtiExitLdbSelectCriterion [] poHeaderSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup poHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(poHeaderSelConds, true);
		VtiExitLdbTableRow[] poHeaderLdbRows = poHeaderLdbTable.getMatchingRows(poHeaderSelCondGrp);

		if(poHeaderLdbRows.length == 0)
			return new VtiUserExitResult(999,"No new Purchase Orders.");
		
		VtiExitLdbSelectCriterion [] logonAuthSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, sessionHeader.getUserId()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			
			VtiExitLdbSelectConditionGroup logonAuthSelCondGrp = new VtiExitLdbSelectConditionGroup(logonAuthSelConds, true);
			VtiExitLdbTableRow[] logonAuthLdbRows = logonLdbTable.getMatchingRows(logonAuthSelCondGrp);
			
			
			if(logonAuthLdbRows.length > 0)
			{
				if(logonAuthLdbRows[0].getFieldValue("AUTHLEVEL").equalsIgnoreCase("OVERIDE"))
				{
					scrFWeight.setDisplayOnlyFlag(false);
				}
			}
		
		VtiExitLdbSelectCriterion [] poItemsSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup poItemsSelCondGrp = new VtiExitLdbSelectConditionGroup(poItemsSelConds, true);
		VtiExitLdbTableRow[] poItemsLdbRows = poItemsLdbTable.getMatchingRows(poItemsSelCondGrp);

		if(poItemsLdbRows.length == 0)
			return new VtiUserExitResult(999,"No new Orders.");
		
		VtiExitLdbSelectCriterion [] wbRegSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			
			VtiExitLdbSelectConditionGroup wbRegSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegSelConds, true);
			VtiExitLdbTableRow[] wbRegLdbRows = wbLdbTable.getMatchingRows(wbRegSelCondGrp);

		poStatus = poHeaderLdbRows[0].getFieldValue("STATUS");
		if(poStatus.equalsIgnoreCase("Rejected"))
		{
			btnTare.setHiddenFlag(false);				
		}
		
		if(!poStatus.equalsIgnoreCase("Weigh 2") && !poStatus.equalsIgnoreCase("Weigh 1") && !poStatus.equalsIgnoreCase("Complete") 
					&& !poStatus.substring(0,1).equalsIgnoreCase("0")  && !poStatus.equalsIgnoreCase("SAP ERROR"))
		{
			if(poStatus.equalsIgnoreCase("Failed") || poStatus.equalsIgnoreCase("New"))
			{
				btnOk.setHiddenFlag(true);
				btnReject.setHiddenFlag(true);
				btnSave.setHiddenFlag(true);
				scrRBWeigh1.setDisplayOnlyFlag(true);
				scrRBWeigh2.setDisplayOnlyFlag(true);
				scrFIsStuck.setDisplayOnlyFlag(true);
			
				return new VtiUserExitResult(999,"This truck did not complete the inspection.");
			}
			try
			{
				slipNo = getNextNumberFromNumberRange("YSWB_SLIP");
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating next Slip No.",ee);
				return new VtiUserExitResult(999,"Unable to generate slip no.");
			}
		
			scrFSlip.setFieldValue(Long.toString(slipNo));
			
			btnSave.setHiddenFlag(true);
			btnReject.setHiddenFlag(true);
			
			scrRBWeigh1.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("");
			scrRBWeigh2.setDisplayOnlyFlag(true);
			scrRBWeigh1.setFieldValue("X");
			scrFIsStuck.setDisplayOnlyFlag(true);
			
		}
		
		if(!scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.equalsIgnoreCase("Weigh 1") )
		{	
			
			VtiExitLdbSelectCriterion [] wbSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 1"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			
			VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
			VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);

			if(wbLdbRows.length == 0)
				return new VtiUserExitResult(999,"No previous wb data, weigh 1 failed.");
			scrRBWeigh1.setDisplayOnlyFlag(true);
			scrRBWeigh2.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("X");  
			btnSave.setHiddenFlag(true);
			scrFIsStuck.setDisplayOnlyFlag(false);
			
			scrFStat.setFieldValue(poStatus + " " + poHeaderLdbRows[0].getFieldValue("EBELN"));
			
			scrFSlip.setFieldValue(wbLdbRows[0].getFieldValue("VTIREF"));
			scrFWeight1.setFieldValue(wbLdbRows[0].getFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbLdbRows[0].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime(wbLdbRows[0].getFieldValue("WEIGHT1_T")));
		}
		
		if(!scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.equalsIgnoreCase("Weigh 2") 
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.equalsIgnoreCase("Complete")
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.substring(0,1).equalsIgnoreCase("0")
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.equalsIgnoreCase("SAP ERROR")) 
				
		{	
		
				VtiExitLdbSelectCriterion [] wbASelConds = 
				{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "WEIGH 2"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
				 


			VtiExitLdbSelectConditionGroup wbASelCondGrp = new VtiExitLdbSelectConditionGroup(wbASelConds, true);
			VtiExitLdbTableRow[] wbALdbRows = wbLdbTable.getMatchingRows(wbASelCondGrp);
			
			if(wbALdbRows.length == 0)
				return new VtiUserExitResult(999,"No matching wb data, weigh 1 has corrupt data.");
			
			btnSave.setHiddenFlag(true);
			scrRBWeigh1.setDisplayOnlyFlag(true);
			scrRBWeigh2.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("X"); 
			
			scrFStat.setFieldValue(poStatus + " " + poHeaderLdbRows[0].getFieldValue("EBELN"));
			
			scrFSlip.setFieldValue(wbALdbRows[0].getFieldValue("VTIREF"));
			scrFWeight1.setFieldValue(wbALdbRows[0].getFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbALdbRows[0].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime( wbALdbRows[0].getFieldValue("WEIGHT1_T")));
			scrFWeight2.setFieldValue(wbALdbRows[0].getFieldValue("WEIGHT2"));
			scrFWTStamp2.setFieldValue(fu.shortDate(wbALdbRows[0].getFieldValue("WEIGHT2_D")) + " " + fu.shortTime(wbALdbRows[0].getFieldValue("WEIGHT2_T")));
			
			whBridge = wbALdbRows[0].getFieldValue("WEIGHBRIDGE");
			
			scrFNettW.setFieldValue(wbALdbRows[0].getFieldValue("NETTWEIGHT"));
			scrFNettTS.setFieldValue(fu.shortTime(wbALdbRows[0].getFieldValue("NETTWEIGHT_T")));
			
			btnSave.setHiddenFlag(false);
		}
		
		if(poStatus.equalsIgnoreCase("Complete") || poStatus.substring(0,1).equalsIgnoreCase("0") || poStatus.equalsIgnoreCase("SAP ERROR"))
		{
			btnOk.setHiddenFlag(true);

			if(logonAuthLdbRows.length > 0)
			{
				if(logonAuthLdbRows[0].getFieldValue("AUTHLEVEL").equalsIgnoreCase("REPRINT"))
				{
					btnRePrint.setHiddenFlag(false);
				}
			}
			btnReject.setHiddenFlag(true);
			btnSave.setHiddenFlag(true);
		}

		//Set WB Custom Fields
		VtiExitLdbSelectCriterion [] wbRegisterSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup wbRegisterSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegisterSelConds, true);
		VtiExitLdbTableRow[] wbRegisterLdbRows = registerLdbTable.getMatchingRows(wbRegisterSelCondGrp);

		if(wbRegisterLdbRows.length == 0)
			return new VtiUserExitResult(999,"No matching register.");	
		
		//Get allocated weight
		if(poItemsLdbRows.length == 0)
			return new VtiUserExitResult(999,"No Purchase Order items.");	
		int wghR = 0;
		
		while(poItemsLdbRows.length != wghR)
		{
			if(poItemsLdbRows[wghR].getFieldValue("MEINS").equalsIgnoreCase("TO"))
			{
				allocWgh = allocWgh + poItemsLdbRows[wghR].getDoubleFieldValue("MENGE");
				wghR++;
			}
		}
		
		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);

		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
		
		if(qTLdbRows.length ==0)
			return new VtiUserExitResult(999,"No additional info availible from the Queue.");
		
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
		
		 wghbr.setFieldValue("FIELDVALUE",whBridge);
		 trckReg.setFieldValue("FIELDVALUE",poHeaderLdbRows[0].getFieldValue("TRUCK"));
		 custsupp.setFieldValue("FIELDVALUE",poHeaderLdbRows[0].getFieldValue("NAME1"));
		 driv.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("DRIVER"));
		 transprtr.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("COMPANY"));
		 transprtr.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("COMPANY"));
		 tranType.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("TRANSTYPE"));
		 allwgh.setFieldValue("FIELDVALUE",Double.toString(allocWgh * 1000)); 
		 if(qTLdbRows.length > 0)
			packline.setFieldValue("FIELDVALUE",qTLdbRows[0].getFieldValue("Q_QUEUE"));
		 remarks.setFieldValue("FIELDVALUE","");
		 
		return new VtiUserExitResult();
	}
}
