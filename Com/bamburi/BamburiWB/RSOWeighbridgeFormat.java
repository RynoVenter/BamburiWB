package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RSOWeighbridgeFormat extends VtiUserExit
{
	public String serialDeviceName = null;
	
	public VtiUserExitResult execute() throws VtiExitException
	{
	//Generate Slip number into vti_ref field from number range object from yswb_slip
		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrWFSalesOrd = getScreenField("VBELN");
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
		VtiUserExitScreenField btnRePrint = getScreenField("BT_PRINT");
		VtiUserExitScreenField scrTruckReg = getScreenField("TRUCK_REG");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");
		
		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrWFSalesOrd == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
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
		if(btnRePrint == null) return new VtiUserExitResult (999,"Failed to initialise BT_PRINT.");
		if(scrTruckReg == null) return new VtiUserExitResult (999,"Failed to initialise TRUCK_REG.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");

		Date now = new Date();
		String serialDate = DateFormatter.format("yyyyMMdd", now);
		
		FormatUtilities tfu = new FormatUtilities();
		tfu.setCalendar().add(Calendar.MINUTE, 15);
		tfu.upDate();
		String customer = "";
		String truck = "";
		String packerLD = ""; 
		String shiftLD = ""; 
		String packingLine = ""; 

		String soStatus ="";
		FormatUtilities fu = new FormatUtilities();
		
		long slipNo = 0;
		double allocWgh = 0;
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
		VtiUserExitScreenTable scrTblItems = getScreenTable("TB_ITEMS");
		if(scrTblItems == null) return new VtiUserExitResult (999,"Failed to initialise TB_ITEMS.");
		
		VtiExitLdbTable soHeaderLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable soItemsLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		VtiExitLdbTable loadingLdbTable = getLocalDatabaseTable("YSWB_LOADING");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable logonLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		
		if (soHeaderLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (soItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_ITEMS.");
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (packingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");
		if (loadingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOADING.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (logonLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");

		
		btnTare.setHiddenFlag(true);
		scfTolWarning.setHiddenFlag(true);
		btnRePrint.setHiddenFlag(true);
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
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
			
			
		//End of setting shift
			
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
	//If status from sales order is new or changed or rejected then it's 1 weight, else set to weight 2
		VtiExitLdbSelectCriterion [] soHeaderSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup soHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderSelConds, true);
		VtiExitLdbTableRow[] soHeaderLdbRows = soHeaderLdbTable.getMatchingRows(soHeaderSelCondGrp);

		if(soHeaderLdbRows.length == 0)
			return new VtiUserExitResult(999,"Sales Order " + scrWFSalesOrd.getFieldValue() + " not found in Header table.");
		
		boolean isBulk = false;
		
		
		for(int i = 0; i < scrTblItems.getRowCount();i++)
		{
			VtiExitLdbSelectCriterion [] soItemsSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(i).getFieldValue("VBELN_I")),
								new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, "DIEN"),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup soItemsSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemsSelConds, true);
			VtiExitLdbTableRow[] soItemsLdbRows = soItemsLdbTable.getMatchingRows(soItemsSelCondGrp);

			if(soItemsLdbRows.length == 0)
				return new VtiUserExitResult(999,"No Sales Order items found for " + scrTblItems.getRow(i).getFieldValue("VBELN_I") + ".");
		
		
			//Determine if product is bulk
			VtiExitLdbSelectCriterion [] exclMatSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "BULK"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup exclMatSelCondGrp = new VtiExitLdbSelectConditionGroup(exclMatSelConds, true);
			VtiExitLdbTableRow[] exclMatLdbRows = configLdbTable.getMatchingRows(exclMatSelCondGrp);
					
			if(exclMatLdbRows.length == 0)
				return new VtiUserExitResult(999, "Bulk tolerance configuration not maintained in YSWB_CONFIG");
					
			String bulkMat = "";
			
			boolean getBulk = false;
			String soMatNr = "";
					
			for(int ib = 0;soItemsLdbRows.length > ib;ib++)
			{
				soMatNr = soItemsLdbRows[ib].getFieldValue("MATNR");

				for(int ibcon = 0;exclMatLdbRows.length > ibcon;ibcon++)
				{
					bulkMat = exclMatLdbRows[ibcon].getFieldValue("KEYVAL1");
					getBulk = soMatNr.equalsIgnoreCase(bulkMat);
					if(getBulk)
						isBulk = true;
				}
			}
		}
				
				
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
		
			VtiExitLdbSelectCriterion [] wbRegSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			
			VtiExitLdbSelectConditionGroup wbRegSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegSelConds, true);
			VtiExitLdbTableRow[] wbRegLdbRows = wbLdbTable.getMatchingRows(wbRegSelCondGrp);

		soStatus = soHeaderLdbRows[0].getFieldValue("STATUS");
		   
		if(soStatus.equalsIgnoreCase("Rejected"))
		{
			btnTare.setHiddenFlag(false);				
		}
		
		if(!soStatus.equalsIgnoreCase("Weigh 2") && !soStatus.equalsIgnoreCase("Weigh 1") && !soStatus.equalsIgnoreCase("Complete") 
					&& !soStatus.substring(0,1).equalsIgnoreCase("0")  && !soStatus.equalsIgnoreCase("SAP ERROR"))
		{
			if(soStatus.equalsIgnoreCase("Failed") || soStatus.equalsIgnoreCase("New"))
			{
				btnOk.setHiddenFlag(true);
				btnReject.setHiddenFlag(true);
				btnSave.setHiddenFlag(true);
				scrRBWeigh1.setDisplayOnlyFlag(true);
				scrRBWeigh2.setDisplayOnlyFlag(true);
			
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
			if(soStatus.equalsIgnoreCase("ASSIGNED"))
				btnReject.setHiddenFlag(false);
			else
				btnReject.setHiddenFlag(true);
						
			scrRBWeigh1.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("");
			scrRBWeigh2.setDisplayOnlyFlag(true);
			scrRBWeigh1.setFieldValue("X");
		}
		
		if(!scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && soStatus.equalsIgnoreCase("Weigh 1"))
		{	
			
			VtiExitLdbSelectCriterion [] wbSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 1"),
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
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
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
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
				VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
				
				if(regLdbRows.length == 0)
					return new VtiUserExitResult(999,1,"No truck with this order and vti in the registration table.");
				
				StringBuffer regStat = new StringBuffer(regLdbRows[0].getFieldValue("INSPSTATUS"));
				//get status for so header for truck
				StringBuffer soStat = new StringBuffer(soHeaderLdbRows[0].getFieldValue("STATUS"));
				
				//correct status
				boolean bSave = false;
				
				if(regStat.equals("W") && soStat.equals("WEIGH 1"))
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
			
			scrFSlip.setFieldValue(wbLdbRows[0].getFieldValue("VTIREF"));
			scrFWeight1.setIntegerFieldValue(wbLdbRows[0].getIntegerFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbLdbRows[0].getStringFieldValue("WEIGHT1_D")) + " " + fu.shortTime(wbLdbRows[0].getStringFieldValue("WEIGHT1_T")));
		}
		
		if(!scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && soStatus.equalsIgnoreCase("Weigh 2") 
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && soStatus.equalsIgnoreCase("Complete")
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && soStatus.substring(0,1).equalsIgnoreCase("0")
				|| !scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && soStatus.equalsIgnoreCase("SAP ERROR"))
		{	
			
			VtiExitLdbSelectCriterion [] wbASelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
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
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
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
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
				VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
				
				if(regLdbRows.length == 0)
					return new VtiUserExitResult(999,1,"No truck with this order and vti in the registration table.");
				
				StringBuffer regStat = new StringBuffer(regLdbRows[0].getFieldValue("INSPSTATUS"));
				//get status for so header for truck
				StringBuffer soStat = new StringBuffer(soHeaderLdbRows[0].getFieldValue("STATUS"));
				
				//correct status
				boolean bSave = false;
				
				if(regStat.equals("W") && soStat.equals("WEIGH 2")
				   || regStat.equals("C"))
				{
					bSave = true;
					wbResLdbRows[0].setFieldValue("STATUS", "WEIGH 2");
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
					return new VtiUserExitResult(999,1,"Attempts to automatically correct the incorrect status failed. Request assistance from support.");
			}
				
			
			scrRBWeigh1.setDisplayOnlyFlag(true);
			scrRBWeigh2.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("X"); 
			
			scrFStat.setFieldValue(soStatus + " " + soHeaderLdbRows[0].getFieldValue("VBELN"));
			
			scrFSlip.setFieldValue(wbALdbRows[0].getFieldValue("VTIREF"));
			scrFWeight1.setIntegerFieldValue(wbALdbRows[0].getIntegerFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbALdbRows[0].getStringFieldValue("WEIGHT1_D")) + " " + fu.shortTime( wbALdbRows[0].getStringFieldValue("WEIGHT1_T")));
			scrFWeight2.setIntegerFieldValue(wbALdbRows[0].getIntegerFieldValue("WEIGHT2"));
			scrFWTStamp2.setFieldValue(fu.shortDate(wbALdbRows[0].getFieldValue("WEIGHT2_D")) + " " + fu.shortTime(wbALdbRows[0].getFieldValue("WEIGHT2_T")));
			scrFNettW.setIntegerFieldValue(wbALdbRows[0].getIntegerFieldValue("NETTWEIGHT"));
			scrFNettTS.setFieldValue(fu.shortTime(wbALdbRows[0].getFieldValue("NETTWEIGHT_T")));
		}
		
				//Set WB Custom Fields
		VtiExitLdbSelectCriterion [] wbRegisterSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup wbRegisterSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegisterSelConds, true);
		VtiExitLdbTableRow[] wbRegisterLdbRows = registerLdbTable.getMatchingRows(wbRegisterSelCondGrp);
		
		if(soStatus.equalsIgnoreCase("Complete") || soStatus.substring(0,1).equalsIgnoreCase("0") || soStatus.equalsIgnoreCase("SAP ERROR"))
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
			
			if(wbRegisterLdbRows.length == 0)
			{
				VtiExitLdbSelectCriterion [] wbRegisterCorSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
									new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
				VtiExitLdbSelectConditionGroup wbRegisterCorSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegisterCorSelConds, true);
				VtiExitLdbTableRow[] regCorLdbRows = registerLdbTable.getMatchingRows(wbRegisterCorSelCondGrp);
				
				
				if(regCorLdbRows.length > 0 && btnRePrint.getHiddenFlag() == false)
				{
					
					regCorLdbRows[0].setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
					if(scrRBWeigh1.getFieldValue().equalsIgnoreCase("X"))
					{

						regCorLdbRows[0].setFieldValue("INSPSTATUS",  "C");
						Log.error("Truck " + scrTruckReg.getFieldValue() + " outbound format correction status saved to " + regCorLdbRows[0].getFieldValue("INSPSTATUS"));
					}
						
					try
					{							
						registerLdbTable.saveRow(regCorLdbRows[0]);
					}
					catch (VtiExitException ee)
					{
						Log.warn("Register status not corrected",ee);
					}
				
					//return new VtiUserExitResult(000,"No matching register found.The data correction was attempted, please try again by clicking back and selecting the truck again.");	
				}
			}
		
			wbRegisterLdbRows = registerLdbTable.getMatchingRows(wbRegisterSelCondGrp);
		}



		if(wbRegisterLdbRows.length == 0)
		{
			VtiExitLdbSelectCriterion [] wbRegisterCorSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup wbRegisterCorSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegisterCorSelConds, true);
			VtiExitLdbTableRow[] regCorLdbRows = registerLdbTable.getMatchingRows(wbRegisterCorSelCondGrp);
			
			
			if(regCorLdbRows.length > 0 && btnRePrint.getHiddenFlag() == true)
			{
				
				regCorLdbRows[0].setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
				if(scrRBWeigh1.getFieldValue().equalsIgnoreCase("X"))
				{

					regCorLdbRows[0].setFieldValue("INSPSTATUS",  "A");
					Log.error("Truck " + scrTruckReg.getFieldValue() + " outbound format correction status saved to " + regCorLdbRows[0].getFieldValue("INSPSTATUS"));
				}
				else
				{
					regCorLdbRows[0].setFieldValue("INSPSTATUS",  "W");
					Log.error("Truck " + scrTruckReg.getFieldValue() + " outbound format correction status saved to " + regCorLdbRows[0].getFieldValue("INSPSTATUS"));
				}
					
				try
				{							
					registerLdbTable.saveRow(regCorLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.warn("Register status not corrected",ee);
				}
			
				//return new VtiUserExitResult(000,"No matching register found.The data correction was attempted, please try again by clicking back and selecting the truck again.");	
			}
		}
		
		wbRegisterLdbRows = registerLdbTable.getMatchingRows(wbRegisterSelCondGrp);
		
		//Get allocated weight
		
		for(int c = 0;c < scrTblItems.getRowCount();c++)
		{
			VtiExitLdbSelectCriterion [] soItemsSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(c).getFieldValue("VBELN_I")),
								new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, "DIEN"),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup soItemsSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemsSelConds, true);
			VtiExitLdbTableRow[] soItemsLdbRows = soItemsLdbTable.getMatchingRows(soItemsSelCondGrp);

			if(soItemsLdbRows.length == 0)
				return new VtiUserExitResult(999,"No Sales Order items found for " + scrTblItems.getRow(c).getFieldValue("VBELN_I") + ".");
		
			if(soItemsLdbRows.length == 0)
				return new VtiUserExitResult(999,"No Sales Order items.");	
			
			int wghR = 0;
			
			while (soItemsLdbRows.length != wghR)
			{
				if(soItemsLdbRows[wghR].getFieldValue("MEINS").equalsIgnoreCase("TO"))
				{
					allocWgh = allocWgh + soItemsLdbRows[wghR].getDoubleFieldValue("KWMENG");		
				}
				wghR++;
			}		
		}
		
		int pCheck = 10;
		int qDiv = 0;
		int pRecs = 0;
		
		boolean hasItems = false;
		
		if(!soHeaderLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("Complete"))
		{
		   
			for(int c = 0;c < scrTblItems.getRowCount();c++)
			{								
					VtiExitLdbSelectCriterion [] soSelConds = 
					{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(c).getFieldValue("VBELN_I")),
										new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, "DIEN"),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
			    
					VtiExitLdbSelectConditionGroup soSelCondGrp = new VtiExitLdbSelectConditionGroup(soSelConds, true);
					VtiExitLdbTableRow[] soLdbRows = soItemsLdbTable.getMatchingRows(soSelCondGrp);

					if(soLdbRows.length == 0)
						return new VtiUserExitResult(999,"No items found for the Truck and Order.");

					hasItems = true;
							
					//WB LDB Count 
					VtiExitLdbSelectCriterion [] wbSelConds = 
					{
							new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(c).getFieldValue("VBELN_I")),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
											new VtiExitLdbSelectCondition("WEIGHT1", VtiExitLdbSelectCondition.GT_OPERATOR, "0"),
							new VtiExitLdbSelectCondition("PACKLOADER", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
			    
					VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
					VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);
					
					Log.trace(0,"wb count " + wbLdbRows.length + " for order " + scrTblItems.getRow(c).getFieldValue("VBELN_I"));
									
					//Packing LDB Count 
									
						VtiExitLdbSelectCriterion [] packingQSelConds = 
							{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(c).getFieldValue("VBELN_I")),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
												new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DISPATCH"),
													new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
														new VtiExitLdbSelectCondition("POSNR", VtiExitLdbSelectCondition.EQ_OPERATOR, Integer.toString(pCheck)),
								new VtiExitLdbSelectCondition("OFF_ORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
							};
			  
						VtiExitLdbSelectConditionGroup packingQSelCondGrp = new VtiExitLdbSelectConditionGroup(packingQSelConds, true);

						VtiExitLdbTableRow[] packingQTLdbRows = packingLdbTable.getMatchingRows(packingQSelCondGrp);
						
						Log.trace(0,"packingQTLdbRows count " + packingQTLdbRows.length + " for order " + scrTblItems.getRow(c).getFieldValue("VBELN_I"));
						
						int pPos = 10;	
						
						for(int ip = 0;ip < packingQTLdbRows.length;ip++)
						{
							Log.trace(0,"ip = " + ip);
							for(int i = 0;i < packingQTLdbRows.length;i++)
							{
								if(packingQTLdbRows[i].getIntegerFieldValue("POSNR") == pPos)
								{
									pRecs++;
								}
							}
								if(wbLdbRows.length > pRecs)
								{
									  Log.trace(0,"wbLdbRows count " + wbLdbRows.length +  " > " + pRecs + " for order " + scrTblItems.getRow(c).getFieldValue("VBELN_I"));
									  btnSave.setHiddenFlag(true);
									  return new VtiUserExitResult(999,1,"Packing has not been done for line " + pPos + " of Sales Order " +  scrTblItems.getRow(c).getFieldValue("VBELN_I") + ".");
								
							}							
						}
					
					
				if(!isBulk)
				{
					VtiExitLdbSelectCriterion [] packQSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(c).getFieldValue("VBELN_I")),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
											new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DISPATCH"),
												new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
													new VtiExitLdbSelectCondition("POSNR", VtiExitLdbSelectCondition.EQ_OPERATOR, Integer.toString(pCheck)),
							new VtiExitLdbSelectCondition("OFF_ORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
			  
						VtiExitLdbSelectConditionGroup packQSelCondGrp = new VtiExitLdbSelectConditionGroup(packQSelConds, true);

						VtiExitLdbTableRow[] packQTLdbRows = packingLdbTable.getMatchingRows(packQSelCondGrp);
						
						packerLD = ""; 
						shiftLD = ""; 
						packingLine = "";
						
					if(pRecs == 0)
				   {			
						if(scrRBWeigh2.getFieldValue().equalsIgnoreCase("X"))
						{
							btnOk.setHiddenFlag(true);
							btnReject.setHiddenFlag(true);
							btnSave.setHiddenFlag(true);
							btnTare.setHiddenFlag(true);
							return new VtiUserExitResult(999,"No packing done for items.");
						}
					}
					else
					{
						packerLD = packQTLdbRows[packQTLdbRows.length-1].getFieldValue("PACKER"); 
						shiftLD = packQTLdbRows[packQTLdbRows.length-1].getFieldValue("SHIFT"); 
						packingLine = packQTLdbRows[packQTLdbRows.length-1].getFieldValue("QUEUENO"); 
					}
					
					if(scrRBWeigh2.getFieldValue().equalsIgnoreCase("X"))
					{
						int wbRecs = 0;

						VtiExitLdbSelectCriterion [] wbRecSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
											new VtiExitLdbSelectCondition("WEIGHT1", VtiExitLdbSelectCondition.GT_OPERATOR, "0"),
							new VtiExitLdbSelectCondition("PACKLOADER", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
					
						VtiExitLdbSelectConditionGroup wbRecSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRecSelConds, true);
						VtiExitLdbTableRow[] wbRecLdbRows = wbLdbTable.getMatchingRows(wbRecSelCondGrp);

						if(wbRecLdbRows.length == 0)
							return new VtiUserExitResult(999,"No wb data collected.");
					
						wbRecs = wbRecLdbRows.length;

						//if(wbRecLdbRows[wbRecLdbRows.length - 1].getFieldValue("STATUS").equalsIgnoreCase("REJECTED"))
							//wbRecs++;
						
						if(wbRecs > pRecs)
						{
								btnOk.setHiddenFlag(true);
								btnReject.setHiddenFlag(true);
								btnSave.setHiddenFlag(true);
								btnTare.setHiddenFlag(true);
								return new VtiUserExitResult(999,"This truck has been weighed " + wbRecs + " times, but only packed " + pRecs + " times.");
						}
					}
				}
			}
		}
		
		
		VtiExitLdbSelectCriterion [] qSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
						new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);

		VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
		
		if(qTLdbRows.length == 0)
		{
			Log.trace(0,"Bridge for " + scrTruckReg.getFieldValue() + " was not determined on order " + scrWFSalesOrd.getFieldValue());
		}
		
		//set weighbridge	
		
		cmbWeighBridge.setFieldValue("Cement");
		
		/*if(qTLdbRows.length != 0)
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
		
			*/
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
		
		if(wbRegisterLdbRows.length == 0)
		{
		
			VtiExitLdbSelectCriterion [] wbRegReSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
									new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup wbRegReSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegReSelConds, true);
			VtiExitLdbTableRow[] wbRegReLdbRows = registerLdbTable.getMatchingRows(wbRegReSelCondGrp);
			
			if(wbRegReLdbRows.length > 0)
			{
				driv.setFieldValue("FIELDVALUE",wbRegReLdbRows[0].getFieldValue("DRIVER"));
				transprtr.setFieldValue("FIELDVALUE",wbRegReLdbRows[0].getFieldValue("COMPANY"));
				tranType.setFieldValue("FIELDVALUE",wbRegReLdbRows[0].getFieldValue("TRANSTYPE"));
			}
		}

		if(wbRegisterLdbRows.length > 0)
		{
			driv.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("DRIVER"));
			transprtr.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("COMPANY"));
			tranType.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("TRANSTYPE"));
			scrNoAxels.setFieldValue( wbRegisterLdbRows[0].getFieldValue("NOAXELS"));
		}
		
 		 trckReg.setFieldValue("FIELDVALUE",soHeaderLdbRows[0].getFieldValue("TRUCK"));
		 custsupp.setFieldValue("FIELDVALUE",soHeaderLdbRows[0].getFieldValue("NAME1"));

		 allwgh.setFieldValue("FIELDVALUE",Double.toString(allocWgh * 1000)); 
		 ordNo.setFieldValue("FIELDVALUE","");
		 rebag.setFieldValue("FIELDVALUE","");
		 packline.setFieldValue("FIELDVALUE",packingLine);
		 packLoad.setFieldValue("FIELDVALUE",packerLD);
		 segtype.setFieldValue("FIELDVALUE","");
		 if(shiftLD.length() == 0)
			 shift.setFieldValue("FIELDVALUE",currShift);
		 else
			 shift.setFieldValue("FIELDVALUE",shiftLD);
		 tralnum.setFieldValue("FIELDVALUE","");
		 wghbr.setFieldValue("FIELDVALUE",cmbWeighBridge.getFieldValue());
		 
		 return new VtiUserExitResult();
	}
}
