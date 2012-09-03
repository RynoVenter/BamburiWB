package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class DelWeighbridgeFormat extends VtiUserExit
{/*Perform some general screen formatting and preperation of the screen and filling in of known data.
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
	//Generate Slip number into vti_ref field from number range object from yswb_slip
		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrWFDelOrd = getScreenField("VBELN");
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
		VtiUserExitScreenField scrRegNo = getScreenField("TRUCK_REG");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");
		
		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrWFDelOrd == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
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
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");
		
		
		scrFIsStuck.setFieldValue("");
		btnBack.setHiddenFlag(false);
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
		VtiExitLdbTable icHeaderLdbTable = getLocalDatabaseTable("YSWB_IC_HEADER");
		VtiExitLdbTable icItemsLdbTable = getLocalDatabaseTable("YSWB_IC_ITEMS");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable logonLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		
		if (icHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (icItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_Items.");
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (logonLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (packingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");
		
		Date now = new Date();
		long slipNo = 0;
		double allocWgh = 0;
		String icStatus ="";
		String whBridge ="";
		String packerLD = null; 
		String shiftLD = null; 
		String packingLine = null;
		FormatUtilities fu = new FormatUtilities();
		btnRePrint.setHiddenFlag(true);
		btnTare.setHiddenFlag(true);
		scfTolWarning.setHiddenFlag(true);
		
		//Set Shift
		String dbShift = "";
		String currShift = "";
		String getShift = "";
		String currLdbTime = DateFormatter.format("HHmmss", now);
		int shiftStart = 0;
		int shiftEnd = 0;
		//Get qty of shifts
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
			
		
	//If status from purchase order is new or changed or rejected then it's 1 weight, else set to weight 2
		VtiExitLdbSelectCriterion [] statHeaderSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup statHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(statHeaderSelConds, true);
		VtiExitLdbTableRow[] statHeaderLdbRows = statusLdbTable.getMatchingRows(statHeaderSelCondGrp);

		if(statHeaderLdbRows.length == 0)
			return new VtiUserExitResult(999,"Order not found in the Status table.");
		
		VtiExitLdbSelectCriterion [] icHeaderSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup icHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(icHeaderSelConds, true);
		VtiExitLdbTableRow[] icHeaderLdbRows = icHeaderLdbTable.getMatchingRows(icHeaderSelCondGrp);

		if(icHeaderLdbRows.length == 0)
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
		
		VtiExitLdbSelectCriterion [] icItemsSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup icItemsSelCondGrp = new VtiExitLdbSelectConditionGroup(icItemsSelConds, true);
		VtiExitLdbTableRow[] icItemsLdbRows = icItemsLdbTable.getMatchingRows(icItemsSelCondGrp);

		if(icItemsLdbRows.length == 0)
			return new VtiUserExitResult(999,"No Inter Company Order that match.");
		
		//Determine if product is bulk
		VtiExitLdbSelectCriterion [] exclMatSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "IC_BULK_MAT"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup exclMatSelCondGrp = new VtiExitLdbSelectConditionGroup(exclMatSelConds, true);
		VtiExitLdbTableRow[] exclMatLdbRows = configLdbTable.getMatchingRows(exclMatSelCondGrp);
				
		if(exclMatLdbRows.length == 0)
			return new VtiUserExitResult(999, "Bulk tolerance configuration not maintained in YSWB_CONFIG");
				
		String bulkMat = "";
		boolean isBulk = false;
		boolean getBulk = false;
		String icMatNr = "";
				
		for(int ib = 0;icItemsLdbRows.length > ib;ib++)
		{
			icMatNr = icItemsLdbRows[ib].getFieldValue("MATNR");

			for(int ibcon = 0;exclMatLdbRows.length > ibcon;ibcon++)
			{
				bulkMat = exclMatLdbRows[ibcon].getFieldValue("KEYVAL1");
				getBulk = icMatNr.equalsIgnoreCase(bulkMat);
				if(getBulk)
					isBulk = true;
			}
		}
		
		icStatus = statHeaderLdbRows[0].getFieldValue("WGH_STATUS");
		if(icStatus.equalsIgnoreCase("REJECTED"))
		{
			btnTare.setHiddenFlag(false);				
		}
		
		if(!icStatus.equalsIgnoreCase("WEIGH 2") && !icStatus.equalsIgnoreCase("WEIGH 1") && !icStatus.equalsIgnoreCase("COMPLETE") 
					&& !icStatus.substring(0,1).equalsIgnoreCase("0")  && !icStatus.equalsIgnoreCase("SAP ERROR"))
		{
			if(icStatus.equalsIgnoreCase("FAILED"))
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
			
			if(icStatus.equalsIgnoreCase("ASSIGNED"))
				btnReject.setHiddenFlag(false);
			else
				btnReject.setHiddenFlag(true);
			
			scrRBWeigh1.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("");
			scrRBWeigh2.setDisplayOnlyFlag(true);
			scrRBWeigh1.setFieldValue("X");
			scrFIsStuck.setDisplayOnlyFlag(true);
			
		}
		
		if(!scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && icStatus.equalsIgnoreCase("Weigh 1") )
		{	
			
			VtiExitLdbSelectCriterion [] wbSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
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
							new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
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
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
				VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
				
				if(regLdbRows.length == 0)
					return new VtiUserExitResult(999,1,"No truck with this order and vti in the registration table.");
				
				scrNoAxels.setFieldValue(regLdbRows[0].getFieldValue("NOAXELS"));
				
				
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
			
			scrRBWeigh1.setDisplayOnlyFlag(true);
			scrRBWeigh2.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("X");  
			btnSave.setHiddenFlag(true);
			scrFIsStuck.setDisplayOnlyFlag(false);
			
			scrFStat.setFieldValue(icStatus + " " + statHeaderLdbRows[0].getFieldValue("DELIVDOC"));
			
			scrFSlip.setFieldValue(wbLdbRows[0].getFieldValue("VTIREF"));
			scrFWeight1.setIntegerFieldValue(wbLdbRows[0].getIntegerFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbLdbRows[0].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime(wbLdbRows[0].getFieldValue("WEIGHT1_T")));
		}
		
		if(!scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && icStatus.equalsIgnoreCase("Weigh 2") 
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && icStatus.equalsIgnoreCase("Complete")
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && icStatus.substring(0,1).equalsIgnoreCase("0")
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && icStatus.equalsIgnoreCase("SAP ERROR")) 
				
		{	
		
				VtiExitLdbSelectCriterion [] wbASelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
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
			
			scrFStat.setFieldValue(icStatus + " " + statHeaderLdbRows[0].getFieldValue("DELIVDOC"));
			
			scrFSlip.setFieldValue(wbALdbRows[0].getFieldValue("VTIREF"));
			scrFWeight1.setIntegerFieldValue(wbALdbRows[0].getIntegerFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbALdbRows[0].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime( wbALdbRows[0].getFieldValue("WEIGHT1_T")));
			scrFWeight2.setIntegerFieldValue(wbALdbRows[0].getIntegerFieldValue("WEIGHT2"));
			scrFWTStamp2.setFieldValue(fu.shortDate(wbALdbRows[0].getFieldValue("WEIGHT2_D")) + " " + fu.shortTime(wbALdbRows[0].getFieldValue("WEIGHT2_T")));
			
			whBridge = wbALdbRows[0].getFieldValue("WEIGHBRIDGE");
			
			scrFNettW.setIntegerFieldValue(wbALdbRows[0].getIntegerFieldValue("NETTWEIGHT"));
			scrFNettTS.setFieldValue(fu.shortTime(wbALdbRows[0].getFieldValue("NETTWEIGHT_T")));
			
			btnSave.setHiddenFlag(false);
		}
		
		if(icStatus.equalsIgnoreCase("COMPLETE") || icStatus.substring(0,1).equalsIgnoreCase("0") || icStatus.equalsIgnoreCase("SAP ERROR"))
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
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup wbRegisterSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegisterSelConds, true);
		VtiExitLdbTableRow[] wbRegisterLdbRows = registerLdbTable.getMatchingRows(wbRegisterSelCondGrp);

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
			
			if(wbRegisterCorLdbRows.length > 0 && btnRePrint.getHiddenFlag() == true)
			{
						wbRegisterCorLdbRows[0].setFieldValue("DELIVDOC",scrWFDelOrd.getFieldValue());
						wbRegisterCorLdbRows[0].setFieldValue("INSPSTATUS",  "W");
						
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
		if(icItemsLdbRows.length == 0)
			return new VtiUserExitResult(999,"No Purchase Order items.");	
		int wghR = 0;
		int icProd = 0;
		
		while(icItemsLdbRows.length != wghR)
		{
			if(icItemsLdbRows[wghR].getFieldValue("MEINS").equalsIgnoreCase("TO"))
			{
				allocWgh = allocWgh + icItemsLdbRows[wghR].getDoubleFieldValue("LFIMG");
				icProd++;
				
			}
			wghR++;
		}
				
		VtiExitLdbSelectCriterion [] packingSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
		VtiExitLdbSelectConditionGroup packingSelCondGrp = new VtiExitLdbSelectConditionGroup(packingSelConds, true);
		
		VtiExitLdbOrderSpecification [] pOrderBy = 
					{
						new VtiExitLdbOrderSpecification("START_DATE",true),
						new VtiExitLdbOrderSpecification("START_TIME",true),
					};

		VtiExitLdbTableRow[] packingTLdbRows = packingLdbTable.getMatchingRows(packingSelCondGrp, pOrderBy);	
		
		packerLD = ""; 
		shiftLD = ""; 
		packingLine = "";
					
		if(!isBulk)
		{
				int wbRecs = 0;

				VtiExitLdbSelectCriterion [] wbRecSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
										new VtiExitLdbSelectCondition("WEIGHT1", VtiExitLdbSelectCondition.GT_OPERATOR, "0"),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
			
				VtiExitLdbSelectConditionGroup wbRecSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRecSelConds, true);
				VtiExitLdbTableRow[] wbRecLdbRows = wbLdbTable.getMatchingRows(wbRecSelCondGrp);

				if(wbRecLdbRows.length > 0)
				{
					wbRecs = wbRecLdbRows.length;
				
					//if(wbRecLdbRows[wbRecLdbRows.length - 1].getFieldValue("STATUS").equalsIgnoreCase("REJECTED"))
						//wbRecs++;
				
					double wbRecords = wbRecLdbRows.length;
					double packRecords =  packingTLdbRows.length;
					double packDone = (wbRecords * icProd) / packRecords;
					
					Log.trace(1,wbRecords + " * " + icProd + " / " + packRecords  + " = " + (wbRecords * icProd) / packRecords);
					Log.trace(1,(wbRecLdbRows.length * icProd)  + " > " + packRecords);
					
					if(packDone != 1  &&  (wbRecLdbRows.length * icProd) > packingTLdbRows.length)
					{
							btnOk.setHiddenFlag(true);
							btnReject.setHiddenFlag(true);
							btnSave.setHiddenFlag(true);
							btnTare.setHiddenFlag(true);
							return new VtiUserExitResult(999,"This truck has been weighed " + wbRecs + " times, but only packed " + (packingTLdbRows.length /  icProd)+ " times.");
					}
				}

				if(packingTLdbRows.length > 0)
				{
					packerLD = packingTLdbRows[packingTLdbRows.length-1].getFieldValue("PACKER"); 
					shiftLD = packingTLdbRows[packingTLdbRows.length-1].getFieldValue("SHIFT"); 
					packingLine = packingTLdbRows[packingTLdbRows.length-1].getFieldValue("QUEUENO"); 
				}
			
		}
		
			VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFDelOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "Complete"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);

			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
		
			if(qTLdbRows.length == 0)
				Log.info("Bridge for " + scrWFDelOrd.getFieldValue() + " was not determined on order " + scrRegNo.getFieldValue());
		
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
				cmbWeighBridge.setFieldValue("Cement");
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
				if(bridgeQLdbRows[0].getFieldValue("KEYVAL2").length() > 0)
				cmbWeighBridge.setFieldValue(bridgeQLdbRows[0].getFieldValue("KEYVAL2"));
		}
		else
		{
			cmbWeighBridge.setFieldValue("Cement");
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
		 //custsupp.setFieldValue("FIELDVALUE",icHeaderLdbRows[0].getFieldValue("NAME1"));// get from ic
		 driv.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("DRIVER"));
		 scrNoAxels.setFieldValue( wbRegisterLdbRows[0].getFieldValue("NOAXELS"));
		 transprtr.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("COMPANY"));
		 tranType.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("TRANSTYPE"));
		 allwgh.setFieldValue("FIELDVALUE",Double.toString(allocWgh * 1000)); 
		 
		 if(scrRBWeigh2.getFieldValue().equalsIgnoreCase("X"))
		 {
			 packline.setFieldValue("FIELDVALUE",packingLine);
		 }
		 
		 packLoad.setFieldValue("FIELDVALUE",packerLD);
		 if(shiftLD.length() == 0)
			 shift.setFieldValue("FIELDVALUE",currShift);
		 else
			 shift.setFieldValue("FIELDVALUE",shiftLD);
		 remarks.setFieldValue("FIELDVALUE","");
		 
		return new VtiUserExitResult();
	}
}
