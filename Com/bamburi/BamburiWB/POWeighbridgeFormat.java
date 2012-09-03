package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class POWeighbridgeFormat extends VtiUserExit
{/*Perform some general screen formatting and preperation of the screen and filling in of known data.
  */

	public VtiUserExitResult execute() throws VtiExitException
	{
		
		Date now = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", now);
		String currLdbTime = DateFormatter.format("HHmmss", now);
		long slipNo = 0;
		double allocWgh = 0;
		boolean reqPack = false;
		String poStatus ="";
		String whBridge ="";
		String packerLD = ""; 
		String shiftLD = "";  
		String packingLine = ""; 
		FormatUtilities fu = new FormatUtilities();
		boolean isTransfer = false;
		String docType = "EBELN";	
		String dbShift = "";
		String currShift = "";
		String getShift = "";
		int shiftStart = 0;
		int shiftEnd = 0;
		
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
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrWTransfer = getScreenField("BSART");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");
		
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
		if(scrWTransfer == null) return new VtiUserExitResult (999,"Failed to initialise BSART.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");
		
		scrFIsStuck.setFieldValue("");
		btnBack.setHiddenFlag(false);
		btnRePrint.setHiddenFlag(true);
		btnTare.setHiddenFlag(true);
		scfTolWarning.setHiddenFlag(true);
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
		VtiUserExitScreenTable scrTblItems = getScreenTable("TB_ITEMS");
		if(scrTblItems == null) return new VtiUserExitResult (999,"Failed to initialise TB_ITEMS.");
		
		VtiExitLdbTable poHeaderLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable poItemsLdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable logonLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		VtiExitLdbTable gatepassLdb = getLocalDatabaseTable("YSWB_GATEPASS");
		
		if (poHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (poItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_Items.");
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (inspLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (logonLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (packingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");
		if(gatepassLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_GATEPASS");
		
		Log.trace(0,"WTransfer had value " + scrWTransfer.getFieldValue());
		
		if(scrWTransfer.getFieldValue().equalsIgnoreCase("UB"))
		{
			isTransfer = true;
			docType = "STOCKTRNF";
		}	
		
		if(scrWTransfer.getFieldValue().equalsIgnoreCase("ZRO"))
		{
			isTransfer = true;
			docType = "EBELN";
		}
				
		VtiExitLdbSelectCriterion [] shiftQTYSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "SHIFT"),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
				
		VtiExitLdbSelectConditionGroup shiftQTYSelCondGrp = new VtiExitLdbSelectConditionGroup(shiftQTYSelConds, true);
		VtiExitLdbTableRow[] shiftQTYLdbRows = configLdbTable.getMatchingRows(shiftQTYSelCondGrp);
		
		if(shiftQTYLdbRows.length == 0)
			return new VtiUserExitResult(999,"Operational Shifts not maintained in the Config Table.");
				
			for(int s = 0;s < shiftQTYLdbRows.length;s++)
			{
				getShift = "SHIFT"+(s+1);
						
				VtiExitLdbSelectCriterion [] shiftSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "SHIFT"),
								new VtiExitLdbSelectCondition("KEYVAL1", VtiExitLdbSelectCondition.EQ_OPERATOR, getShift),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup shiftSelCondGrp = new VtiExitLdbSelectConditionGroup(shiftSelConds, true);
				VtiExitLdbTableRow[] shiftLdbRows = configLdbTable.getMatchingRows(shiftSelCondGrp);
						
		//Determine shift and if shift is with in the parameters of the operational shifts as defined in the config table
				shiftStart = shiftLdbRows[0].getIntegerFieldValue("KEYVAL2");
				shiftEnd = shiftLdbRows[0].getIntegerFieldValue("KEYVAL3");
				dbShift = shiftLdbRows[0].getFieldValue("KEYVAL1");
				Integer t = new Integer(currLdbTime);
				int currIntTime = t.intValue();
						
				if(currIntTime >= shiftStart && currIntTime < shiftEnd)
					currShift = shiftLdbRows[0].getFieldValue("KEYVAL1");
			}
Log.trace(0,"doctype variable is " + docType);
		VtiExitLdbSelectCriterion [] statHeaderSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup statHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(statHeaderSelConds, true);
		VtiExitLdbTableRow[] statHeaderLdbRows = statusLdbTable.getMatchingRows(statHeaderSelCondGrp);

		if(statHeaderLdbRows.length == 0)
			return new VtiUserExitResult(999,"Order not found in the Status table.");
		
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
			return new VtiUserExitResult(999,"Order details not found.");
		
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
			return new VtiUserExitResult(999,"No matching Purchase Order.");
		
		poStatus = statHeaderLdbRows[0].getFieldValue("WGH_STATUS");
		
		if(poStatus.equalsIgnoreCase("REJECTED"))
		{
			btnTare.setHiddenFlag(false);				
		}
		
		if(!poStatus.equalsIgnoreCase("WEIGH 2") && !poStatus.equalsIgnoreCase("WEIGH 1") && !poStatus.equalsIgnoreCase("COMPLETE") 
					&& !poStatus.substring(0,1).equalsIgnoreCase("0")  && !poStatus.equalsIgnoreCase("SAP ERROR"))
		{
			if(poStatus.equalsIgnoreCase("FAILED"))
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
			
			if(poStatus.equalsIgnoreCase("ASSIGNED"))
				btnReject.setHiddenFlag(false);
			else
				btnReject.setHiddenFlag(true);
			
			scrRBWeigh1.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("");
			scrRBWeigh2.setDisplayOnlyFlag(true);
			scrRBWeigh1.setFieldValue("X");
			scrFIsStuck.setDisplayOnlyFlag(true);
			
			//Check if inspection is still valid.
			VtiExitLdbSelectCriterion [] inspSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, statHeaderLdbRows[0].getFieldValue("INSP_VTI_REF")),
	//							new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue())
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			Log.trace(0, "Inspection criteria in PO Wb Format " + scrRegNo.getFieldValue() + " " + statHeaderLdbRows[0].getFieldValue("INSP_VTI_REF") + " " + docType + " = " + scrWFPurchOrd.getFieldValue());
			VtiExitLdbSelectConditionGroup inspSelCondGrp = new VtiExitLdbSelectConditionGroup(inspSelConds, true);
			VtiExitLdbTableRow[] inspLdbRows = inspLdbTable.getMatchingRows(inspSelCondGrp);
		
			if(inspLdbRows.length == 0)
				return new VtiUserExitResult(999,"Inspection details for truck not found.");
			
			Log.trace(0, "Inspections found " + inspLdbRows.length);
			if(inspLdbRows[0].getFieldValue("STOCK").equalsIgnoreCase("X") || inspLdbRows[0].getFieldValue("SHIP").equalsIgnoreCase("X"))
			{
				StringBuffer sbExpireTs = new StringBuffer();
				sbExpireTs.append(inspLdbRows[0].getFieldValue("EXPIREDATE"));
				
				if(inspLdbRows[0].getFieldValue("EXPIRETIME").length() == 6)
					sbExpireTs.append(inspLdbRows[0].getFieldValue("EXPIRETIME"));
				else
				{
					sbExpireTs.append("0");
					sbExpireTs.append(inspLdbRows[0].getFieldValue("EXPIRETIME"));
				}
				
				if(inspLdbRows[0].getFieldValue("EXPIREDATE").length() > 0)
				{
					if(Long.parseLong(currLdbDate+currLdbTime) > Long.parseLong(sbExpireTs.toString()) 
					   && inspLdbRows[0].getFieldValue("EXPIRED").length() > 0)
					{
						sessionHeader.setNextFunctionId("YSWB_TRINBOUND");
						return new VtiUserExitResult(999,1,"Inspection has expired for this truck.");
					}
					else if(Long.parseLong(currLdbDate+currLdbTime) > Long.parseLong(sbExpireTs.toString())
							&& inspLdbRows[0].getFieldValue("EXPIRED").length() == 0)
					{
						inspLdbRows[0].setFieldValue("EXPIRED", "X");
						try
						{
							inspLdbTable.saveRow(inspLdbRows[0]);
						}
						catch ( VtiExitException ee)
						{
							Log.error("Inpection expired value not updated during inspection valid check.", ee);
						}
						return new VtiUserExitResult(000,1,"Truck inspection will be expiring after this, inform driver to do inspection again.");
					}
				}
					
			}
			
		}
		
		if(!scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.equalsIgnoreCase("Weigh 1") )
		{	
			
			VtiExitLdbSelectCriterion [] wbSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 1"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			
			VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
			VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);

			if(wbLdbRows.length == 0)
			{
				//get wb records for truck and order
				VtiExitLdbSelectCriterion [] wbResSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
			
				VtiExitLdbOrderSpecification [] orderBy = 
				{
					new VtiExitLdbOrderSpecification("TIMESTAMP",false),
				};
				
				VtiExitLdbSelectConditionGroup wbResSelCondGrp = new VtiExitLdbSelectConditionGroup(wbResSelConds, true);
				VtiExitLdbTableRow[] wbResLdbRows = wbLdbTable.getMatchingRows(wbResSelCondGrp, orderBy);
				
				if(wbResLdbRows.length == 0)
					return new VtiUserExitResult(999,1,"No truck with this order in the wb table.");
				
				StringBuffer wbStat = new StringBuffer(wbResLdbRows[0].getFieldValue("STATUS"));
				
				//get register for truck
				VtiExitLdbSelectCriterion [] regSelConds = 
				{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
				VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
				
				if(regLdbRows.length == 0)
					return new VtiUserExitResult(999,1,"No truck with this order and vti in the registration table.");
				
				StringBuffer regStat = new StringBuffer(regLdbRows[0].getFieldValue("INSPSTATUS"));
				//get status for so header for truck
				StringBuffer poStat = new StringBuffer(statHeaderLdbRows[0].getFieldValue("WGH_STATUS"));
				
				//correct status
				boolean bSave = false;
				
				if(regStat.equals("W") && poStat.equals("WEIGH 1"))
				{
					bSave = true;
					wbResLdbRows[0].setFieldValue("STATUS", "WEIGH 1");
				}
				
				try
				{
					if(bSave)
						wbLdbTable.saveRow(wbResLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Wb correction during wb format failed.", ee);
				}
					
				wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);	
				
				if(wbLdbRows.length == 0)
					return new VtiUserExitResult(999,1,"Attempts to automatically correct incorrect status failed. Request assistance from support.");
			}
			
			scrTblItems.getRow(0).setFieldValue("ORIGIN",wbLdbRows[0].getFieldValue("ORIGIN"));
			scrTblItems.getRow(0).setDisplayOnlyFlag("ORIGIN",true);
			
			scrRBWeigh1.setDisplayOnlyFlag(true);
			scrRBWeigh2.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("X");  
			btnSave.setHiddenFlag(true);
			scrFIsStuck.setDisplayOnlyFlag(false);
			
			scrFStat.setFieldValue(poStatus + " " + statHeaderLdbRows[0].getFieldValue("EBELN"));
			
			scrFSlip.setFieldValue(wbLdbRows[0].getFieldValue("VTIREF"));
			scrFWeight1.setIntegerFieldValue(wbLdbRows[0].getIntegerFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbLdbRows[0].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime(wbLdbRows[0].getFieldValue("WEIGHT1_T")));
		}
		
		Log.trace(0, "Prepping to update data to screen for weigh 2 or complete status");
		Log.trace(0, "PO Status is " + poStatus);
		Log.trace(0, "RB selected is w1" + scrRBWeigh1.getFieldValue());
		Log.trace(0, "RB selected is w2" + scrRBWeigh2.getFieldValue());
		
		if(!scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.equalsIgnoreCase("Weigh 2") 
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.equalsIgnoreCase("Complete")
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.substring(0,1).equalsIgnoreCase("0")
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.equalsIgnoreCase("SAP ERROR")) 
				
		{	
			
			
		
				VtiExitLdbSelectCriterion [] wbASelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};

			VtiExitLdbSelectConditionGroup wbASelCondGrp = new VtiExitLdbSelectConditionGroup(wbASelConds, true);
			VtiExitLdbTableRow[] wbALdbRows = wbLdbTable.getMatchingRows(wbASelCondGrp);
			
			if(wbALdbRows.length == 0)
				{
				//get wb records for truck and order
				VtiExitLdbSelectCriterion [] wbResSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
			
				VtiExitLdbOrderSpecification [] orderBy = 
				{
					new VtiExitLdbOrderSpecification("TIMESTAMP",false),
				};
				
				VtiExitLdbSelectConditionGroup wbResSelCondGrp = new VtiExitLdbSelectConditionGroup(wbResSelConds, true);
				VtiExitLdbTableRow[] wbResLdbRows = wbLdbTable.getMatchingRows(wbResSelCondGrp, orderBy);
				
				if(wbResLdbRows.length == 0)
					return new VtiUserExitResult(999,1,"No truck with this order in the wb table.");
				
				StringBuffer wbStat = new StringBuffer(wbResLdbRows[0].getFieldValue("STATUS"));
				
				//get register for truck
				VtiExitLdbSelectCriterion [] regSelConds = 
				{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
				VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
				
				if(regLdbRows.length == 0)
					return new VtiUserExitResult(999,1,"No truck with this order and vti in the registration table.");
				
				StringBuffer regStat = new StringBuffer(regLdbRows[0].getFieldValue("INSPSTATUS"));
				//get status for so header for truck
				StringBuffer poStat = new StringBuffer(statHeaderLdbRows[0].getFieldValue("WGH_STATUS"));
				
				//correct status
				boolean bSave = false;
				
				if(regStat.equals("W") && poStat.equals("WEIGH 1"))
				{
					bSave = true;
					wbResLdbRows[0].setFieldValue("STATUS", "WEIGH 1");
				}
				
				try
				{
					if(bSave)
						wbLdbTable.saveRow(wbResLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Wb correction during wb format failed.", ee);
				}
					
				wbALdbRows = wbLdbTable.getMatchingRows(wbASelCondGrp);	
				
				if(wbALdbRows.length == 0)
					return new VtiUserExitResult(999,1,"Attempts to automatically correct incorrect status failed. Request assistance from support.");
			}
			
			btnSave.setHiddenFlag(true);
			scrRBWeigh1.setDisplayOnlyFlag(true);
			scrRBWeigh2.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("X"); 
			
			scrFStat.setFieldValue(poStatus + " " + statHeaderLdbRows[0].getFieldValue("EBELN"));
			
			scrFSlip.setFieldValue(wbALdbRows[0].getFieldValue("VTIREF"));
			scrFWeight1.setIntegerFieldValue(wbALdbRows[0].getIntegerFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbALdbRows[0].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime( wbALdbRows[0].getFieldValue("WEIGHT1_T")));
			scrFWeight2.setIntegerFieldValue(wbALdbRows[0].getIntegerFieldValue("WEIGHT2"));
			scrFWTStamp2.setFieldValue(fu.shortDate(wbALdbRows[0].getFieldValue("WEIGHT2_D")) + " " + fu.shortTime(wbALdbRows[0].getFieldValue("WEIGHT2_T")));
			
			whBridge = wbALdbRows[0].getFieldValue("WEIGHBRIDGE");
			
			scrTblItems.getRow(0).setFieldValue("ORIGIN",wbALdbRows[0].getFieldValue("ORIGIN"));
			scrTblItems.getRow(0).setDisplayOnlyFlag("ORIGIN",true);
			
			scrFNettW.setIntegerFieldValue(wbALdbRows[0].getIntegerFieldValue("NETTWEIGHT"));
			scrFNettTS.setFieldValue(fu.shortTime(wbALdbRows[0].getFieldValue("NETTWEIGHT_T")));
			
			btnSave.setHiddenFlag(false);
		}
		Log.trace(0,"PoStatus before print btn show is.");
		if(poStatus.equalsIgnoreCase("COMPLETE") || poStatus.substring(0,1).equalsIgnoreCase("0") || poStatus.equalsIgnoreCase("SAP ERROR"))
		{
			if(scrTblItems.getRow(0).getFieldValue("ORIGIN") != null)
				scrTblItems.getRow(0).setDisplayOnlyFlag("ORIGIN",true);
			
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
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup wbRegisterSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegisterSelConds, true);
		VtiExitLdbTableRow[] wbRegisterLdbRows = registerLdbTable.getMatchingRows(wbRegisterSelCondGrp);
		Log.trace(0,"Register search returned empty set, STO search values: doctype is " + docType + " = " + scrWFPurchOrd.getFieldValue() + " :: Truck " + scrRegNo.getFieldValue() + " :: VTI REF " + scrVRef.getFieldValue());
		
		if(wbRegisterLdbRows.length == 0)
		{
			VtiExitLdbSelectCriterion [] wbRegisterCorSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup wbRegisterCorSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegisterCorSelConds, true);
			VtiExitLdbTableRow[] wbRegisterCorLdbRows = registerLdbTable.getMatchingRows(wbRegisterCorSelCondGrp);
			Log.trace(0,"STO search values 2nd time :: Truck " + scrRegNo.getFieldValue() + " :: VTI REF " + scrVRef.getFieldValue());
			if(wbRegisterCorLdbRows.length > 0 && btnRePrint.getHiddenFlag() == true)
			{
			   
				wbRegisterCorLdbRows[0].setFieldValue(docType,scrWFPurchOrd.getFieldValue());
				if(scrRBWeigh1.getFieldValue().equalsIgnoreCase("X"))
				{
					wbRegisterCorLdbRows[0].setFieldValue("INSPSTATUS","A");
					Log.trace(1,"Truck " + scrRegNo.getFieldValue() + " po format correction status saved to " + wbRegisterCorLdbRows[0].getFieldValue("INSPSTATUS"));

				}
				else
				{
					wbRegisterCorLdbRows[0].setFieldValue("INSPSTATUS",  "W");
					Log.trace(0,"Truck " + scrRegNo.getFieldValue() + " po format correction status saved to " + wbRegisterCorLdbRows[0].getFieldValue("INSPSTATUS"));

				}
				
				try
				{			
					registerLdbTable.saveRow(wbRegisterCorLdbRows[0]);

				}
				catch (VtiExitException ee)
				{
					Log.warn("Register status not corrected",ee);
				}
				
				return new VtiUserExitResult(000,"No matching register found.The data correction was attempted, please try again by clicking back and selecting the truck again.");	
			}
			else
				wbRegisterLdbRows = registerLdbTable.getMatchingRows(wbRegisterSelCondGrp);
		}
		
		//Get allocated weight
		if(poItemsLdbRows.length == 0)
			return new VtiUserExitResult(999,"No Purchase Order items.");	
		int wghR = 0;
		
		while(poItemsLdbRows.length != wghR)
		{
			if(poItemsLdbRows[wghR].getFieldValue("MEINS").equalsIgnoreCase("TO"))
			{
				allocWgh = allocWgh + poItemsLdbRows[wghR].getDoubleFieldValue("MENGE");
				
			}
			
			if(poItemsLdbRows[0].getFieldValue("MATKL").equalsIgnoreCase("45"))
			{
				scrTblItems.getRow(0).setDisplayOnlyFlag("ARKTX",true);
			}
					
			wghR++;
		}
		
		if(isTransfer)
		{
			
			Log.trace(0,"This is for a transfer.");
			//Set Gate Pass as allocated weight

				VtiExitLdbSelectCriterion [] gateSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, wbRegisterLdbRows[0].getFieldValue("GATE_PASS")),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
			
				VtiExitLdbSelectConditionGroup gateSelCondGrp = new VtiExitLdbSelectConditionGroup(gateSelConds, true);
				VtiExitLdbTableRow[] gateLdbRows = gatepassLdb.getMatchingRows(gateSelCondGrp);

				if(gateLdbRows.length == 0)
				{
					allocWgh = 0d;
				}
				else
				{
					allocWgh = gateLdbRows[0].getDoubleFieldValue("MENGE");
					
				}
				
				Log.trace(0,"Gate weight is " + allocWgh);
			//Done setting alloc weight from gate pass
			
				VtiExitLdbSelectCriterion [] poSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
			
				VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
				VtiExitLdbTableRow[] poLdbRows = poItemsLdbTable.getMatchingRows(poSelCondGrp);	
				
				String itemTyp = "";
				
				if(poLdbRows.length == 0)
					return new VtiUserExitResult(999, "Order items not found, check LDB");
				else
				{
					itemTyp = poLdbRows[0].getFieldValue("MTART");
				}
				
		
			VtiExitLdbSelectCriterion [] packingSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup packingSelCondGrp = new VtiExitLdbSelectConditionGroup(packingSelConds, true);

			VtiExitLdbOrderSpecification [] pOrderBy = 
					{
						new VtiExitLdbOrderSpecification("START_DATE",true),
						new VtiExitLdbOrderSpecification("START_TIME",true),
					};
		
			VtiExitLdbTableRow[] packingTLdbRows = packingLdbTable.getMatchingRows(packingSelCondGrp,pOrderBy);
			
			
			if(packingTLdbRows.length > 0)
			{
					packerLD = packingTLdbRows[packingTLdbRows.length-1].getFieldValue("PACKER"); 
					shiftLD = packingTLdbRows[packingTLdbRows.length-1].getFieldValue("SHIFT"); 
					packingLine = packingTLdbRows[packingTLdbRows.length-1].getFieldValue("QUEUENO"); 
			}
			
			
				int wbRecs = 0;

				VtiExitLdbSelectCriterion [] wbRecSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
			
				VtiExitLdbSelectConditionGroup wbRecSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRecSelConds, true);
				VtiExitLdbTableRow[] wbRecLdbRows = wbLdbTable.getMatchingRows(wbRecSelCondGrp);
		}
		
		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);

		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
		
		if(qTLdbRows.length == 0)
Log.trace(0,"Bridge for " + scrWFPurchOrd.getFieldValue() + " was not determined on order " + scrRegNo.getFieldValue());
		//set weighbridge

		if(qTLdbRows.length != 0)
		{


				VtiExitLdbSelectCriterion [] bridgeQSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, "QLIST"),
								new VtiExitLdbSelectCondition("KEYVAL1", VtiExitLdbSelectCondition.EQ_OPERATOR, qTLdbRows[0].getFieldValue("Q_QUEUE")),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
							
				};
      
				VtiExitLdbSelectConditionGroup bridgeQSelCondGrp = new VtiExitLdbSelectConditionGroup(bridgeQSelConds, true);
				VtiExitLdbTableRow[] bridgeQLdbRows = configLdbTable.getMatchingRows(bridgeQSelCondGrp);
				

				if(bridgeQLdbRows.length == 0)
					cmbWeighBridge.setFieldValue("RAW MATERIALS");
				else
					cmbWeighBridge.setFieldValue(bridgeQLdbRows[0].getFieldValue("KEYVAL5"));

				VtiExitLdbSelectCriterion [] altBridgeQSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, "TQUEUE"),
								new VtiExitLdbSelectCondition("KEYVAL4", VtiExitLdbSelectCondition.EQ_OPERATOR, qTLdbRows[0].getFieldValue("PROD_TYPE")),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
								
				};
				  
				VtiExitLdbSelectConditionGroup altBridgeQSelCondGrp = new VtiExitLdbSelectConditionGroup(altBridgeQSelConds, true);
				VtiExitLdbTableRow[] altBridgeQLdbRows = configLdbTable.getMatchingRows(altBridgeQSelCondGrp);
							
				if(altBridgeQLdbRows.length > 0)
					if(altBridgeQLdbRows[0].getFieldValue("KEYVAL2").length() > 0)
					cmbWeighBridge.setFieldValue(altBridgeQLdbRows[0].getFieldValue("KEYVAL2"));
		}
		else
		{
			cmbWeighBridge.setFieldValue("RAW MATERIALS");
		}
		
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
		 trckReg.setFieldValue("FIELDVALUE",statHeaderLdbRows[0].getFieldValue("TRUCKREG"));
		 scrRegNo.setFieldValue(statHeaderLdbRows[0].getFieldValue("TRUCKREG"));
		 custsupp.setFieldValue("FIELDVALUE",poHeaderLdbRows[0].getFieldValue("NAME1"));// get from po
		 if(wbRegisterLdbRows.length > 0)
		 {
			driv.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("DRIVER"));
			transprtr.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("COMPANY"));
			tranType.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("TRANSTYPE"));
			if(scrNoAxels != null)
			scrNoAxels.setFieldValue( wbRegisterLdbRows[0].getFieldValue("NOAXELS"));
		 }
		 else
		 {
			 return new VtiUserExitResult(000, "Previous registration already archived. No custom detail.");
		 }
		 allwgh.setStringFieldValue("FIELDVALUE",Double.toString(allocWgh * 1000)); 
		 packline.setFieldValue("FIELDVALUE",packingLine);
		 remarks.setFieldValue("FIELDVALUE","");
		 shift.setFieldValue("FIELDVALUE",shiftLD);
		 packLoad.setFieldValue("FIELDVALUE",packerLD);
		 				 
	return new VtiUserExitResult();
	}
}
