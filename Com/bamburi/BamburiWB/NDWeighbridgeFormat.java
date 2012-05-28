package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class NDWeighbridgeFormat extends VtiUserExit
{/*Perform some general screen formatting and preperation of the screen and filling in of known data.
  */
	public VtiUserExitResult execute() throws VtiExitException
	{

		long slipNo = 0;
		double allocWgh = 0;
		String poStatus ="";
		String whBridge ="";
		StringBuffer errorMsg = new StringBuffer();
		Date now = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", now);
		String currLdbTime = DateFormatter.format("HHmmss", now);
		FormatUtilities fu = new FormatUtilities();

		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrWSlip = getScreenField("SVTI_REF");
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
		VtiUserExitScreenField scrCostCent = getScreenField("KOSTL");
		VtiUserExitScreenField scrMaterial = getScreenField("ARKTX");
		VtiUserExitScreenField scrPOMaterial = getScreenField("POARKTX");
		VtiUserExitScreenField scrMaterialNr = getScreenField("MATNR");
		VtiUserExitScreenField scrOrigin = getScreenField("ORIGIN");
		VtiUserExitScreenField scrDest = getScreenField("DESTINATION");
		VtiUserExitScreenField scrFIsStuck = getScreenField("IS_STUCK");
		VtiUserExitScreenField btnBack = getScreenField("BT_BACK");
		VtiUserExitScreenField btnRePrint = getScreenField("BT_PRINT");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrDestination = getScreenField("DESTINATION");
		VtiUserExitScreenField scrCostCentre = getScreenField("KOSTL");
		VtiUserExitScreenField scrPlant = getScreenField("WERKS");
		VtiUserExitScreenField scrSloc = getScreenField("LGORT");
		VtiUserExitScreenField scrVendor = getScreenField("NAME1");
		VtiUserExitScreenField scrGL = getScreenField("GENERALLEDGER");	
		VtiUserExitScreenField scrMatNr = getScreenField("MATNR");
		VtiUserExitScreenField scrVendorNo = getScreenField("LIFNR");
		VtiUserExitScreenField scrBsart = getScreenField("BSART");
		VtiUserExitScreenField scrOrder = getScreenField("EBELN");
		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");
		
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
		if(btnOk == null) return new VtiUserExitResult (999,"Failed to initialise BT_OKAY.");
		if(btnReject == null) return new VtiUserExitResult (999,"Failed to initialise BT_REJECT.");
		if(btnSave == null) return new VtiUserExitResult (999,"Failed to initialise BT_SAVE.");
		if(btnTare == null) return new VtiUserExitResult (999,"Failed to initialise GET_TARE.");
		if(cmbWeighBridge == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHBRIDGE.");
		if(scrFIsStuck == null) return new VtiUserExitResult (999,"Failed to initialise IS_STUCK.");
		if(btnBack == null) return new VtiUserExitResult (999,"Failed to initialise BT_BACK.");
		if(btnRePrint == null) return new VtiUserExitResult (999,"Failed to initialise BT_PRINT.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");
		if(scrCostCent == null) return new VtiUserExitResult (999,"Failed to initialise KOSTL");
		if(scrSloc == null) return new VtiUserExitResult (999,"Failed to initialise LGORT");
		if(scrVendor == null) return new VtiUserExitResult (999,"Failed to initialise NAME1");
		if(scrMaterial == null) return new VtiUserExitResult (999,"Failed to initialise ARKTX");
		if(scrOrigin == null) return new VtiUserExitResult (999,"Failed to initialise ORIGIN");
		if(scrDest == null) return new VtiUserExitResult (999,"Failed to initialise DESTINATION");
		if(scrGL == null) return new VtiUserExitResult (999,"Failed to initialise GENERALLEDGER");
		
		scrFIsStuck.setFieldValue("");
		btnBack.setHiddenFlag(false);
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable poLdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable logonLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (poLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_ITEMS.");
		if (inspLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (logonLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		
		btnRePrint.setHiddenFlag(true);
		btnTare.setHiddenFlag(true);
		
		VtiExitLdbSelectCriterion [] regSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

		};
			
		VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
		VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
		
		if(regLdbRows.length == 0)
			return new VtiUserExitResult(999, "Please check the inspection registration.");
			
		if(regLdbRows[0].getFieldValue("TRUCKTYPE").equalsIgnoreCase("SCRAP"))
		{
			scrDestination.setHiddenFlag(true); 	
			scrVendor.setHiddenFlag(true);
			scrSloc.setHiddenFlag(true);
			scrCostCent.setHiddenFlag(true);
			scrGL.setHiddenFlag(true);
			scrVendorNo.setHiddenFlag(true);
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
		
			VtiExitLdbSelectCriterion [] wbSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
							new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWSlip.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			
			VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
			VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);

			if(wbLdbRows.length == 0)
				poStatus = "ASSIGNED";
			else
				poStatus = wbLdbRows[0].getFieldValue("STATUS");
			
		if(poStatus.equalsIgnoreCase("REJECTED"))
		{
			btnTare.setHiddenFlag(false);				
		}

		//Check if inspection is still valid.
			VtiExitLdbSelectCriterion [] statHeaderSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup statHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(statHeaderSelConds, true);
			VtiExitLdbTableRow[] statHeaderLdbRows = statusLdbTable.getMatchingRows(statHeaderSelCondGrp);

			if(statHeaderLdbRows.length == 0)
				return new VtiUserExitResult(999,"Truck not found in the Status table.");
			
			poStatus = statHeaderLdbRows[0].getFieldValue("WGH_STATUS");
			
			scrBsart.setFieldValue( statHeaderLdbRows[0].getFieldValue("DOCTYPE"));
			
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
			
			
		
			VtiExitLdbSelectCriterion [] inspSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, statHeaderLdbRows[0].getFieldValue("INSP_VTI_REF")),
//								new VtiExitLdbSelectCondition("ROTATE", VtiExitLdbSelectCondition.EQ_OPERATOR, "X")
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
						
			VtiExitLdbSelectConditionGroup inspSelCondGrp = new VtiExitLdbSelectConditionGroup(inspSelConds, true);
			VtiExitLdbTableRow[] inspLdbRows = inspLdbTable.getMatchingRows(inspSelCondGrp);

			if(inspLdbRows.length != 0)
			{
				if(inspLdbRows[0].getFieldValue("INSPSTATUS").equalsIgnoreCase("F"))
				{
					return new VtiUserExitResult(999,1,"Inspection was not completed.");
				}
				
				if(inspLdbRows[0].getFieldValue("EXPIREDATE").length() > 0)
				{
					if(inspLdbRows[0].getFieldValue("ROTATE").equalsIgnoreCase("X"))
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
						

						if(Long.parseLong(currLdbDate+currLdbTime) > Long.parseLong(sbExpireTs.toString()) 
						   && inspLdbRows[0].getFieldValue("EXPIRED").length() > 0)
						{
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
								Log.error("Inpection expired value not update during inspection valid check.", ee);
							}
							errorMsg.append("Truck inspection will be expiring after this, inform driver to do inspection again.");
						}
					}
				}
			}
			
		}
		
		Log.trace(0, "PO STATUS = " + poStatus);
		if(!scrRBWeigh1.getFieldValue().equalsIgnoreCase("X") && poStatus.equalsIgnoreCase("Weigh 1") )
		{			
			VtiExitLdbSelectCriterion [] wghbSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "WEIGH 1"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			
			VtiExitLdbSelectConditionGroup wghbSelCondGrp = new VtiExitLdbSelectConditionGroup(wghbSelConds, true);
			
			VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("VTIREF",false),
			};
			
			VtiExitLdbTableRow[] wghbLdbRows = wbLdbTable.getMatchingRows(wghbSelCondGrp, orderBy);

			if(wbLdbRows.length == 0)
				return new VtiUserExitResult(999,"No previous wb data, weigh 1 failed.");
			
			scrRBWeigh1.setDisplayOnlyFlag(true);
			scrRBWeigh2.setDisplayOnlyFlag(false);
			scrRBWeigh2.setFieldValue("X");
			
			scrCostCent.setDisplayOnlyFlag(true);
			scrSloc.setDisplayOnlyFlag(true);
			scrVendor.setDisplayOnlyFlag(true);
			scrMaterial.setDisplayOnlyFlag(true);
			scrOrigin.setDisplayOnlyFlag(true);
			scrDest.setDisplayOnlyFlag(true);
			scrGL.setDisplayOnlyFlag(true);
			scrVendorNo.setDisplayOnlyFlag(true);
		
			btnSave.setHiddenFlag(true);
			scrFIsStuck.setDisplayOnlyFlag(false);
			
			scrFStat.setFieldValue(poStatus);	

			scrFSlip.setFieldValue(wbLdbRows[0].getFieldValue("VTIREF"));
			scrFWeight1.setIntegerFieldValue(wbLdbRows[0].getIntegerFieldValue("WEIGHT1"));
			Log.trace(0,"Weigh recs count " + wbLdbRows.length);
			scrFWTStamp1.setFieldValue(fu.shortDate(wbLdbRows[0].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime(wbLdbRows[0].getFieldValue("WEIGHT1_T")));

			scrCostCent.setFieldValue(wbLdbRows[0].getFieldValue("KOSTL"));
			scrSloc.setFieldValue(wbLdbRows[0].getFieldValue("LGORT"));
			scrMatNr.setFieldValue(wbLdbRows[0].getFieldValue("MATNR"));
			//get material desc
			scrOrigin.setFieldValue(wbLdbRows[0].getFieldValue("ORIGIN"));
			scrDest.setFieldValue(wbLdbRows[0].getFieldValue("DEST"));
			scrGL.setFieldValue(wbLdbRows[0].getFieldValue("SAKNR"));
			scrVendorNo.setFieldValue(wbLdbRows[0].getFieldValue("LIFNR"));				
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
			
			scrCostCent.setDisplayOnlyFlag(true);
			scrSloc.setDisplayOnlyFlag(true);
			scrVendor.setDisplayOnlyFlag(true);
			scrMaterial.setDisplayOnlyFlag(true);
			scrOrigin.setDisplayOnlyFlag(true);
			scrDest.setDisplayOnlyFlag(true);
			scrGL.setDisplayOnlyFlag(true);
			scrVendorNo.setDisplayOnlyFlag(true);
		
			scrFStat.setFieldValue(poStatus);
			
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
		
			
		if(poStatus.equalsIgnoreCase("COMPLETE") || poStatus.substring(0,1).equalsIgnoreCase("0") || poStatus.equalsIgnoreCase("SAP ERROR"))
		{
			btnOk.setHiddenFlag(true);

			if(logonAuthLdbRows.length > 0)
			{
				if(logonAuthLdbRows[0].getFieldValue("AUTHLEVEL").equalsIgnoreCase("REPRINT"))
				{
					btnRePrint.setHiddenFlag(false);
				}
			}
			
			scrCostCent.setDisplayOnlyFlag(true);
			scrSloc.setDisplayOnlyFlag(true);
			scrVendor.setDisplayOnlyFlag(true);
			scrMaterial.setDisplayOnlyFlag(true);
			scrOrigin.setDisplayOnlyFlag(true);
			scrDest.setDisplayOnlyFlag(true);
			scrGL.setDisplayOnlyFlag(true);
			scrVendorNo.setDisplayOnlyFlag(true);
		
			btnReject.setHiddenFlag(true);
			btnSave.setHiddenFlag(true);
		}
		
		if(regLdbRows[0].getFieldValue("EBELN").length() == 10)
		{
			scrOrder.setFieldValue(regLdbRows[0].getFieldValue("EBELN"));
			
			VtiExitLdbSelectCriterion [] poSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, regLdbRows[0].getFieldValue("EBELN")),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

			};
				
			VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
			VtiExitLdbTableRow[] poLdbRows = poLdbTable.getMatchingRows(poSelCondGrp);
			
			if(poLdbRows.length == 0)
			{
				scrOrder.setFieldValue("");
				return new VtiUserExitResult(999,1,"The PO " + regLdbRows[0].getFieldValue("EBELN") + " was not found in the Purchase order table. The material could not be determined.");
			}
			
			scrPOMaterial.setFieldValue(poLdbRows[0].getFieldValue("TXZ01"));
		}
		else
			scrOrder.setFieldValue("");

		//Set WB Custom Fields
		VtiExitLdbSelectCriterion [] wbRegisterSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup wbRegisterSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRegisterSelConds, true);
		VtiExitLdbTableRow[] wbRegisterLdbRows = registerLdbTable.getMatchingRows(wbRegisterSelCondGrp);

		if(wbRegisterLdbRows.length == 0)
			return new VtiUserExitResult(999,"No matching register.");	
		
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
		 trckReg.setFieldValue("FIELDVALUE",scrRegNo.getFieldValue());
		 if(wbRegisterLdbRows.length > 0)
		 {
			driv.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("DRIVER"));
			transprtr.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("COMPANY"));
			transprtr.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("COMPANY"));
			tranType.setFieldValue("FIELDVALUE",wbRegisterLdbRows[0].getFieldValue("TRANSTYPE"));
			scrNoAxels.setFieldValue( wbRegisterLdbRows[0].getFieldValue("NOAXELS"));
		 }
		 else
		 {
			 return new VtiUserExitResult(000, "Previous registration already archived. No custom detail.");
		 }
		 allwgh.setFieldValue("FIELDVALUE",""); 
		 remarks.setFieldValue("FIELDVALUE","");
		 
		 
		 
		 if(errorMsg.length() > 0)
			return new VtiUserExitResult(000,1,errorMsg.toString());
		 else
			return new VtiUserExitResult();
	}
}
