package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class POProcessTruckWeigh extends VtiUserExit
{/*Procees the weight reading from the weighbridge according to weither it is the 1st weight or second weight.
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		//Declarations of variables and elements. Followed by the checking of the elements.

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
		VtiUserExitScreenField scfTolWarning = getScreenField("TOLL_MESSAGE");
		VtiUserExitScreenField scrCBBridge = getScreenField("WEIGHBRIDGE");
		VtiUserExitScreenField scrRBFull = getScreenField("RB_FULL");
		VtiUserExitScreenField scrRBPartial = getScreenField("RB_PARTIAL");
		VtiUserExitScreenField btnSave = getScreenField("BT_SAVE");
		VtiUserExitScreenField scrFIsStuck = getScreenField("IS_STUCK");	
		VtiUserExitScreenField btnBack = getScreenField("BT_BACK");
		VtiUserExitScreenField scrWTransfer = getScreenField("BSART");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField btnOk = getScreenField("BT_OKAY");
		VtiUserExitScreenField scrCscIntervention = getScreenField("GETCSC");
		VtiUserExitScreenField scrSAPPoWght	=  getScreenField("POQTY");
		VtiUserExitScreenField scrCurrentWght	=  getScreenField("KTMNG");
		VtiUserExitScreenField scrProceed	=  getScreenField("PROCEED");
		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");

		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrWFPurchOrd == null) return new VtiUserExitResult (999,"Failed to initialise EBELN.");
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
		if(scfTolWarning == null) return new VtiUserExitResult (999,"Failed to initialise TOLL_MESSAGE.");
		if(scrCBBridge == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHBRIDGE.");
		if(scrRBFull == null) return new VtiUserExitResult (999,"Failed to initialise RB_FULL.");
		if(scrRBPartial == null) return new VtiUserExitResult (999,"Failed to initialise RB_PARTIAL.");
		if(btnSave == null) return new VtiUserExitResult (999,"Failed to initialise SAVE.");
		if(scrFIsStuck == null) return new VtiUserExitResult (999,"Failed to initialise IS_STUCK.");
		if(btnBack == null) return new VtiUserExitResult (999,"Failed to initialise BT_BACK.");
		if(scrWTransfer == null) return new VtiUserExitResult (999,"Failed to initialise BSART.");
		if(scrRegNo == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");
		if(btnOk == null) return new VtiUserExitResult (999,"Failed to initialise BT_OKAY.");
		
		VtiUserExitScreenTable scrTblItems = getScreenTable("TB_ITEMS");
		if(scrTblItems == null) return new VtiUserExitResult (999,"Failed to initialise TB_ITEMS.");

		DBCalls dbCalls = new DBCalls();
		if(scrFWeight.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "No weight.");
		
		if(scrFWeight.getFieldValue().equalsIgnoreCase(scrFWeight1.getFieldValue()))
			return new VtiUserExitResult(999,1,"Weigh 1 and bridge weight is the same.");
		
		if(scrFIsStuck.getFieldValue().equalsIgnoreCase("X"))
		{
			btnBack.setHiddenFlag(true);
		}
		
		scfTolWarning.setHiddenFlag(true);
		//Variable Declarations
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		float w1 = 0;
		float w2 = 0;
		float nett = 0;
		String weighTS = currDate + " " + currTime;
		String currStatus = "";
		String errorMsg ="";
		float gateWeight = 0;
		
		boolean isTransfer = false;
		String docType = "EBELN";
		
		if(scrWTransfer.getFieldValue().equalsIgnoreCase("UB"))
		{
			isTransfer = true;
			docType = "STOCKTRNF";
		}
		
		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Purchase Order is not ready for processing, please check the status.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
			if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
			
		if(scrFNettW.getFieldValue().length() > 0)
			return new VtiUserExitResult(999, "This truck has been weighed a second time, please reject the weight if it was inconrrect.");
		
		if(scrRBWeigh1.getFieldValue().equalsIgnoreCase("X"))
		{
			if(!scrTblItems.getTopRow().getFieldValue("KEY").equalsIgnoreCase("X"))
				return new VtiUserExitResult(999, 1, "Please validate the quantity for " + scrWFPurchOrd.getFieldValue() + ".");
		}
		
		if(!scrProceed.getFieldValue().equalsIgnoreCase("X"))
		{
			Log.trace(0, "scrProceed has no value");
			if(scrCscIntervention.getFieldValue().equalsIgnoreCase("X"))
			{
				sessionHeader.setNextFunctionId("YSWB_POQTYCHECK");
				scrProceed.setFieldValue("X");
					return new VtiUserExitResult(000, "Please validate the action for " + scrWFPurchOrd.getFieldValue() + ".");
			}
		}
		
		
		

		//Database TBL Declaration
		VtiExitLdbTable poHeaderTWLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable poItemsTWLdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable wbTWLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable registerLdb = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable gatepassLdb = getLocalDatabaseTable("YSWB_GATEPASS");
		VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		VtiExitLdbTable loadingLdbTable = getLocalDatabaseTable("YSWB_LOADING");
							
		if (poHeaderTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		if (poItemsTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table poItemsTWLdbTable.");
		if (wbTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (registerLdb == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (gatepassLdb == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_GATEPASS.");
		if (packingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");
		if (loadingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOADING.");
		
		//Get Custom field values.
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
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
		
		//Dataset Declaration
		VtiExitLdbSelectCriterion [] poHeaderTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.CS_OPERATOR, scrWTransfer.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup poHeaderTWSelCondGrp = new VtiExitLdbSelectConditionGroup(poHeaderTWSelConds, true);
		VtiExitLdbTableRow[] poHeaderTWLdbRows = poHeaderTWLdbTable.getMatchingRows(poHeaderTWSelCondGrp);

		if(poHeaderTWLdbRows.length == 0)
			return new VtiUserExitResult(999, "No purchase order detail exist.");
		
		
		VtiExitLdbSelectCriterion [] statusSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
		VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);

		if(statusLdbRows.length == 0)
			return new VtiUserExitResult(999, "No order in Status table.");
		
		
		
		VtiExitLdbSelectCriterion [] poItemsTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup poItemsTWSelCondGrp = new VtiExitLdbSelectConditionGroup(poItemsTWSelConds, true);
		VtiExitLdbTableRow[] poItemsTWLdbRows = poItemsTWLdbTable.getMatchingRows(poItemsTWSelCondGrp);
	
		if(poItemsTWLdbRows.length == 0)
			return new VtiUserExitResult(999,"Purchase Order details corrupt.");
		
		if(statusLdbRows[0].getFieldValue("STOCK").equalsIgnoreCase("X") ||
				   poItemsTWLdbRows[0].getFieldValue("MATKL").equalsIgnoreCase("45"))
		{
			scrTblItems.getRow(0).setDisplayOnlyFlag("ARKTX",true);
			if(scrTblItems.getRow(0).getFieldValue("ORIGIN").length() == 0)
				return new VtiUserExitResult(999, "Please indicate the origin.");
		}
		
		currStatus = statusLdbRows[0].getFieldValue("WGH_STATUS");
		
		if(statusLdbRows[0].getFieldValue("WGH_STATUS").equalsIgnoreCase("FAILED"))
		{
			return new VtiUserExitResult(999, "No vehicle inspection. No weigh in allowed.");
		}
		
		VtiExitLdbSelectCriterion [] packingSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
						new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup packingSelCondGrp = new VtiExitLdbSelectConditionGroup(packingSelConds, true);

		VtiExitLdbTableRow[] packingTLdbRows = packingLdbTable.getMatchingRows(packingSelCondGrp);
		
		
		VtiExitLdbSelectCriterion [] loadingSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup loadingSelCondGrp = new VtiExitLdbSelectConditionGroup(loadingSelConds, true);

		VtiExitLdbTableRow[] loadingTLdbRows = loadingLdbTable.getMatchingRows(loadingSelCondGrp);
		
			
		/*offline code
		if(poHeaderTWLdbRows[0].getFieldValue("MBLNR").equalsIgnoreCase("X"))
		{
			VtiUserExitScreenTable scrTItems = getScreenTable("TB_ITEMS");
			VtiUserExitScreenTable customTable = getScreenTable("TB_CUSTOM");
			VtiUserExitScreenTableRow customerRow = customTable.getRow(3);
			
			for(int inspRow = 0;inspRow < 1;inspRow++)
			{
				VtiUserExitScreenTableRow aItemRow = scrTItems.getRow(inspRow);
				
				if(inspRow == 0)
				{
					if(aItemRow.getFieldValue("ARKTX").length()  == 0 || aItemRow.getFieldValue("TOTAL").length()  == 0)
						return new VtiUserExitResult(999, "Please fill the Material and Total columns in the Items table.");
								
					poItemsTWLdbRows[inspRow].setFieldValue("MATNR",aItemRow.getFieldValue("ARKTX"));
					poItemsTWLdbRows[inspRow].setFieldValue("TXZ01",aItemRow.getFieldValue("ARKTX"));
					poItemsTWLdbRows[inspRow].setFieldValue("NTGEW",aItemRow.getFieldValue("TOTAL"));
					poItemsTWLdbRows[inspRow].setFieldValue("MENGE",aItemRow.getFieldValue("TOTAL"));
					poItemsTWLdbRows[inspRow].setFieldValue("TIMESTAMP","");
					
					poHeaderTWLdbRows[inspRow].setFieldValue("NAME1",customerRow.getFieldValue("FIELDVALUE"));
					poHeaderTWLdbRows[inspRow].setFieldValue("LSMENGE",aItemRow.getFieldValue("TOTAL"));
					poHeaderTWLdbRows[inspRow].setFieldValue("MBLNR","");
					poHeaderTWLdbRows[inspRow].setFieldValue("TIMESTAMP","");
					try
					{
						poItemsTWLdbTable.saveRow(poItemsTWLdbRows[inspRow]);
						poHeaderTWLdbTable.saveRow(poHeaderTWLdbRows[inspRow]);
					}
					catch (VtiExitException ee)
					{
						Log.error("Failed so save offline details",ee);
						return new VtiUserExitResult(999, "Unable to save new information to Purchase order header and items table.");
					}
				}
				if(inspRow == 1)
				{
					if(aItemRow.getFieldValue("ARKTX").length()  == 0 || aItemRow.getFieldValue("TOTAL").length()  == 0)
					{
						poItemsTWLdbTable.deleteRow(poItemsTWLdbRows[1]);
					}
					else
					{
						poItemsTWLdbRows[inspRow].setFieldValue("ARKTX",aItemRow.getFieldValue("ARKTX"));
						poItemsTWLdbRows[inspRow].setFieldValue("BAGS",aItemRow.getFieldValue("BAGS"));
						poItemsTWLdbRows[inspRow].setFieldValue("NTGEW",aItemRow.getFieldValue("TOTAL"));
						poItemsTWLdbRows[inspRow].setFieldValue("TIMESTAMP","");
						
						try
						{
							poItemsTWLdbTable.saveRow(poItemsTWLdbRows[inspRow]);
						}
						catch (VtiExitException ee)
						{
							Log.error("Failed to remove second line item.",ee);
							return new VtiUserExitResult(999, "Unable to save new information to Purchase order items table.");
						}
					}
				}
			}
		}
		offline code ends*/
		
		if(scrRBWeigh1.getFieldValue().equalsIgnoreCase("X"))
		{
			if(scrRBPartial.getFieldValue().equalsIgnoreCase("X"))
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight1.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight1.setFieldValue(scrFWeight.getFieldValue() + scrFWeight1.getFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
				return new VtiUserExitResult(999, "Partial weight collected, move truck and take the next weight. Remember to select ''Full'' when taking the last weight.");
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight1.getFieldValue().length()!=0)
			{
				scrFWeight1.setFieldValue(scrFWeight.getFieldValue() + scrFWeight1.getFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight1.getFieldValue().length()==0)
			{
				scrFWeight1.setFieldValue(scrFWeight.getFieldValue());			
				scrFWTStamp1.setFieldValue(weighTS);
			}
			
			//Set weight and time fields
			if(scrFWeight1.getFieldValue().length() == 0)
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight1.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight1.setFieldValue(scrFWeight.getFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
			}

			if(scrNoAxels != null)
			{
				VtiExitLdbSelectCriterion [] configSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "TONSPERAXLE"),
								new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, scrNoAxels.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
				VtiExitLdbTableRow[] configLdbRows = configLdbTable.getMatchingRows(configSelCondGrp);
		
					if(configLdbRows.length == 0)
					{
						return new VtiUserExitResult (999,"Maximum weight per axle not configured.");		
					}
			
					int axleVariance  = configLdbRows[0].getIntegerFieldValue("KEYVAL3") ;
					int maxLegalWght = configLdbRows[0].getIntegerFieldValue("KEYVAL1");
					
					if(axleVariance > 0)
						maxLegalWght = (maxLegalWght * axleVariance) / 100 + maxLegalWght;
					
					if(maxLegalWght < scrFWeight1.getFloatFieldValue())
					{
						return new VtiUserExitResult(999, "The Gross Vehicle Weight exceeds the legal axle weight for this truck by " + (scrFWeight1.getIntegerFieldValue() - maxLegalWght));
					}
			}
			//Create weight transactions in YSWB_WB
			VtiExitLdbTableRow ldbRowWeigh1 = wbTWLdbTable.newRow();
			
			//Populate TBL Fields
			ldbRowWeigh1.setFieldValue("SERVERGROUP", getServerGroup());
			ldbRowWeigh1.setFieldValue("SERVERID", getServerId());
			ldbRowWeigh1.setFieldValue("TRANDATE", currLdbDate);
			ldbRowWeigh1.setFieldValue("VTIREF", scrFSlip.getFieldValue());
			ldbRowWeigh1.setFieldValue(docType, scrWFPurchOrd.getFieldValue());
			ldbRowWeigh1.setFieldValue("EBELN", scrWFPurchOrd.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1_D", currLdbDate);
			ldbRowWeigh1.setFieldValue("WEIGHT1_T", currLdbTime);
			ldbRowWeigh1.setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
			ldbRowWeigh1.setFieldValue("STATUS", "Weigh 1");
			ldbRowWeigh1.setFieldValue("WEIGHBRIDGE",scrCBBridge.getFieldValue());
			ldbRowWeigh1.setFieldValue("USERID",sessionHeader.getUserId());
			ldbRowWeigh1.setFieldValue("TRUCKREG",scrRegNo.getFieldValue());
			ldbRowWeigh1.setFieldValue("VTIREFA",scrVRef.getFieldValue());
			ldbRowWeigh1.setFieldValue("DRIVER",driv.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("CUSTOMER",custsupp.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("TRANSPORTTYPE",tranType.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("ALLOC_WHT",allwgh.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("DELVNO",ordNo.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("REBAG",rebag.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("SHIFT",shift.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("TRAILERNO",tralnum.getFieldValue("FIELDVALUE"));
			
			
			if(statusLdbRows[0].getFieldValue("STOCK").equalsIgnoreCase("X") ||
				   poItemsTWLdbRows[0].getFieldValue("MATKL").equalsIgnoreCase("45") || poItemsTWLdbRows[0].getFieldValue("MATKL").equalsIgnoreCase("46"))
			{
				if(scrTblItems.getRow(0).getFieldValue("ORIGIN").length() > 0)
					ldbRowWeigh1.setFieldValue("ORIGIN",scrTblItems.getRow(0).getFieldValue("ORIGIN"));
				else
					return new VtiUserExitResult(999,1,"Please indicate Origin.");
			}
			
			ldbRowWeigh1.setFieldValue("TIMESTAMP","");
			

			
			//Change Status
			statusLdbRows[0].setFieldValue("STATUS","W");
			statusLdbRows[0].setFieldValue("WGH_STATUS","Weigh 1");
			statusLdbRows[0].setFieldValue("TIMESTAMP", "");
			//statusLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
			
			try
			{
				statusLdbTable.saveRow(statusLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin status, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to update the Purchase Order.");
			}
			
			VtiExitLdbSelectCriterion [] registerSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "A"),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
											new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
						
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
				
			VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
		
			if(registerLdbRows.length == 0)
				return new VtiUserExitResult(999,"The trucks register status could not be updated. An assigned status is expected.");
			
			registerLdbRows[0].setFieldValue("INSPSTATUS","W");
			registerLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				registerLdbTable.saveRow(registerLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				return new VtiUserExitResult(999,"The trucks register status could not be updated.");
			}
			
			ldbRowWeigh1.setFieldValue("PASS_NUMB",registerLdbRows[0].getFieldValue("GATE_PASS"));
			
			try
			{
				wbTWLdbTable.saveRow(ldbRowWeigh1);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save weighin to Purchase Order.");
			}
			
			VtiExitLdbSelectCriterion [] registerCheckSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "A"),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
						
			VtiExitLdbSelectConditionGroup registerSelCheckCondGrp = new VtiExitLdbSelectConditionGroup(registerCheckSelConds, true);
					
			VtiExitLdbTableRow[] registerCheckLdbRows = registerLdbTable.getMatchingRows(registerSelCheckCondGrp);
		
			if(registerCheckLdbRows.length != 0)
			{
				
				registerCheckLdbRows[0].setFieldValue("INSPSTATUS","W");
				registerCheckLdbRows[0].setFieldValue("TIMESTAMP","");
				
				try
				{
					registerLdbTable.saveRow(registerCheckLdbRows[0]);
					
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating weighin status for the register.",ee);
					return new VtiUserExitResult(999,"The trucks register status could not be updated.");
				}
			}
			else
			{

			}
			//change queue status for truck

			VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "Complete"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
			if(qTLdbRows.length > 0)
			{	
			
				qTLdbRows[0].setFieldValue("Q_STATUS","Weigh 1");
				qTLdbRows[0].setFieldValue("TIMESTAMP","");
			
				try
				{
					queueLdbTable.saveRow(qTLdbRows[0]);
					dbCalls.ldbUpload("YSWB_QUEUE", this);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating queue status, please correct in the queue.",ee);
					return new VtiUserExitResult(000,"Unable to update the Order.");
				}
			}
			//Set next Function

			
			if(isTransfer)
				sessionHeader.setNextFunctionId("YSWB_TRANSFER");
			else
				sessionHeader.setNextFunctionId("YSWB_TRINBOUND");
			
			//Update PO header and Item with new PO levels from SAP
			
			//Update Header	
			if(scrSAPPoWght.getDoubleFieldValue() > poHeaderTWLdbRows[0].getDoubleFieldValue("LSMENGE"))
			{
				poHeaderTWLdbRows[0].setFieldValue("MENGE", scrSAPPoWght.getFieldValue());
				poHeaderTWLdbRows[0].setFieldValue("TIMESTAMP","");
			
				try
				{
					poHeaderTWLdbTable.saveRow( poHeaderTWLdbRows[0]);
				}
				catch (VtiExitException ie)
				{
					return new VtiUserExitResult(999,1,"PO header was not updated witht he latest quantity levels from SAP.");
				}
			}
			
			//Update Item
			poItemsTWLdbRows[0].setFieldValue("KTMNG",scrSAPPoWght.getDoubleFieldValue());	
			if(scrSAPPoWght.getDoubleFieldValue() > poItemsTWLdbRows[0].getDoubleFieldValue("MENGE"))
				poItemsTWLdbRows[0].setFieldValue("MENGE", scrSAPPoWght.getFieldValue());
			poItemsTWLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				poItemsTWLdbTable.saveRow( poItemsTWLdbRows[0]);
			}
			catch (VtiExitException ie)
			{
				return new VtiUserExitResult(999,1,"PO item was not updated witht he latest quantity levels from SAP.");
			}
					
		}
		else
		{
			if(scrRBPartial.getFieldValue().equalsIgnoreCase("X"))
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight2.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight2.setFieldValue(scrFWeight.getFieldValue() + scrFWeight2.getFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
				return new VtiUserExitResult(999, "Partial weight collected, move truck and take the next weight. Remeber to select Full when taking the last weight..");
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight2.getFieldValue().length()!=0)
			{
				scrFWeight2.setFieldValue(scrFWeight.getFieldValue() + scrFWeight2.getFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
			}
			
			if(scrRBFull.getFieldValue().equalsIgnoreCase("X") && scrFWeight2.getFieldValue().length()==0)
			{
				scrFWeight2.setFieldValue(scrFWeight.getFieldValue());	
				scrFWTStamp2.setFieldValue(weighTS);
			}
			
			//Set weight and time fields
			if(scrFWeight2.getFieldValue().length() == 0)
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight2.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight2.setFieldValue(scrFWeight.getFieldValue());
				scrFWTStamp2.setFieldValue(weighTS);
			}
			//Check if the packing was done
				VtiExitLdbSelectCriterion [] exclMatSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "STO_BULK_MAT"),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup exclMatSelCondGrp = new VtiExitLdbSelectConditionGroup(exclMatSelConds, true);
				VtiExitLdbTableRow[] exclMatLdbRows = configLdbTable.getMatchingRows(exclMatSelCondGrp);
				
				if(exclMatLdbRows.length == 0)
					return new VtiUserExitResult(999, "Bulk tolerance configuration not maintained in YSWB_CONFIG");
				
				String bulkMat = "";
				boolean isBulk = false;
				boolean getBulk = false;
				String stoMatNr = "";
				
				for(int ib = 0;poItemsTWLdbRows.length > ib;ib++)
				{
					stoMatNr = poItemsTWLdbRows[ib].getFieldValue("MATNR");

					for(int ibcon = 0;exclMatLdbRows.length > ibcon;ibcon++)
					{
						bulkMat = exclMatLdbRows[ibcon].getFieldValue("KEYVAL1");
						getBulk = stoMatNr.equalsIgnoreCase(bulkMat);
						if(getBulk)
							isBulk = true;
					}
				}
				if(isBulk == false)
				{
					if(scrFWeight2.getLongFieldValue() > scrFWeight1.getLongFieldValue())
					{
						if(isTransfer)
						{		
							if(packingTLdbRows.length == 0)
							{
								if(scrRBWeigh2.getFieldValue().equalsIgnoreCase("X"))
								{
									btnOk.setHiddenFlag(true);
									btnSave.setHiddenFlag(true);
									return new VtiUserExitResult(999,"No packing done for order items.");
								}
							}
						}
					}
				}
			//Update weight transaction in YSWB_WB

			VtiExitLdbSelectCriterion [] wbTWSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 1"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup wbTWSelCondGrp = new VtiExitLdbSelectConditionGroup(wbTWSelConds, true);
			VtiExitLdbTableRow[] ldbRowWeigh2 = wbTWLdbTable.getMatchingRows(wbTWSelCondGrp);

			if(ldbRowWeigh2.length == 0)
				new VtiUserExitResult(999,"No valid rows availible to update in YSWB_WB.");
			
			if(scrRBWeigh2.getFieldValue().equalsIgnoreCase("X"))
			{
				if(scrNoAxels != null)
				{
					VtiExitLdbSelectCriterion [] configSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "TONSPERAXLE"),
										new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, scrNoAxels.getFieldValue()),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
		
					VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
					VtiExitLdbTableRow[] configLdbRows = configLdbTable.getMatchingRows(configSelCondGrp);
		
					if(configLdbRows.length == 0)
					{
						return new VtiUserExitResult (999,"Maximum weight per axle not configured.");		
					}
				
					int axleVariance  = configLdbRows[0].getIntegerFieldValue("KEYVAL2") ;
					int maxLegalWght = configLdbRows[0].getIntegerFieldValue("KEYVAL1");

					if(axleVariance > 0)
					{
						maxLegalWght = (maxLegalWght * axleVariance) / 100 + maxLegalWght;
					}
				
					if(maxLegalWght < scrFWeight2.getFloatFieldValue())
					{
						return new VtiUserExitResult(999, "The Gross Vehicle Weight exceeds the legal axle weight for this truck by " + (scrFWeight2.getIntegerFieldValue() - maxLegalWght));
					}
				}				
				if(scrFWeight2.getLongFieldValue() > scrFWeight1.getLongFieldValue())
				{
					if(isTransfer)
					{
						if(isBulk == false)
						{
							int plLength = 0;
								
							if(packingTLdbRows.length == loadingTLdbRows.length)
								plLength = packingTLdbRows.length;
							else if(packingTLdbRows.length > loadingTLdbRows.length)
								errorMsg = packingTLdbRows.length + " Packing instances with only " + loadingTLdbRows.length + " loading instances recorded.";
									
							
							VtiExitLdbSelectCriterion [] wbPCSelConds = 
							{
								new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
												new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
							};
      
							VtiExitLdbSelectConditionGroup wbPCSelCondGrp = new VtiExitLdbSelectConditionGroup(wbPCSelConds, true);
							VtiExitLdbTableRow[] ldbRowWBPack = wbTWLdbTable.getMatchingRows(wbPCSelCondGrp);

							if(ldbRowWBPack.length == 0)
								new VtiUserExitResult(999,"No valid rows availible to update in YSWB_WB.");
							
							if(ldbRowWBPack.length > packingTLdbRows.length)
							{
								btnOk.setHiddenFlag(true);
								btnSave.setHiddenFlag(true);
								
								return new VtiUserExitResult(999,"This truck has more weigh records(" + ldbRowWBPack.length + ") than packing records(" + packingTLdbRows.length + ")." + errorMsg);
							}	
							//Check truck tolerence before allowing the save 
							if(poHeaderTWLdbRows[0].getFieldValue("BSART").equalsIgnoreCase("UB"))
							{
								//Gate Pass
								VtiExitLdbSelectCriterion [] registerSelConds = 
								{
										new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
											new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
												new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
													new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
														new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
													new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
								};
        
								VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
								VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);

								if(registerLdbRows.length == 0)
										return new VtiUserExitResult(999,"Arrival entry not found.");
								
								if(registerLdbRows[0].getFieldValue("GATE_PASS").length() > 0)
								{
									VtiExitLdbSelectCriterion [] gateSelConds = 
									{
										new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
											new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
												new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, registerLdbRows[0].getFieldValue("GATE_PASS")),
													new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
									};
			
									VtiExitLdbSelectConditionGroup gateSelCondGrp = new VtiExitLdbSelectConditionGroup(gateSelConds, true);
									VtiExitLdbTableRow[] gateLdbRows = gatepassLdb.getMatchingRows(gateSelCondGrp);

									if(gateLdbRows.length == 0)
									{
										gateWeight = 0;
									}
									else
									{
										
										gateWeight = gateLdbRows[0].getFloatFieldValue("MENGE");
										//set gate weight as allocweight
										allwgh.setFieldValue("FIELDVALUE",Double.toString(gateWeight*1000)); 
										//allocweight set
										float overTol = 0F;
										float underTol = 0F;
								
										if((overTol + underTol) == 0)
										{
											
											
												//upper limit get
												VtiExitLdbSelectCriterion [] configUTolSelConds = 
												{
													new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
														new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
															new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "CONFIG"),
																new VtiExitLdbSelectCondition("KEYVAL1", VtiExitLdbSelectCondition.EQ_OPERATOR, "ICUTL"),
																new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
												};
      
												VtiExitLdbSelectConditionGroup configUTolSelCondGrp = new VtiExitLdbSelectConditionGroup(configUTolSelConds, true);
												VtiExitLdbTableRow[] configUTolLdbRows = configLdbTable.getMatchingRows(configUTolSelCondGrp);
		
												if(configUTolLdbRows.length == 0)
												{
													return new VtiUserExitResult (999,"Order tolerance upper limit not determined.");		
												}
												//lower limit get
												VtiExitLdbSelectCriterion [] configLTolSelConds = 
												{
													new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
														new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
															new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "CONFIG"),
																new VtiExitLdbSelectCondition("KEYVAL1", VtiExitLdbSelectCondition.EQ_OPERATOR, "ICLTL"),
																new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
												};
      
												VtiExitLdbSelectConditionGroup configLTolSelCondGrp = new VtiExitLdbSelectConditionGroup(configLTolSelConds, true);
												VtiExitLdbTableRow[] configLTolLdbRows = configLdbTable.getMatchingRows(configLTolSelCondGrp);
		
												if(configUTolLdbRows.length == 0)
												{
													return new VtiUserExitResult (999,"Order tolerance upper limit not determined.");		
												}
			
											overTol= configUTolLdbRows[0].getFloatFieldValue("KEYVAL2");
											underTol = configLTolLdbRows[0].getFloatFieldValue("KEYVAL2");
											
										}
			
										float tol = overTol + underTol;			
										float poItemWht =0;
				
										float underWTol = 0;
										float overWTol = 0;
				
										
				
										poItemWht = gateWeight * 1000;
										w1 = scrFWeight1.getFloatFieldValue();
										w2 = scrFWeight2.getFloatFieldValue();
										
				
										if(w2 > w1)
										{
											nett = w2 - w1;
											if(tol != 0)
											{
									
												underWTol = poItemWht - poItemWht * underTol;
												
												overWTol = poItemWht * overTol + poItemWht;
												if(nett < underWTol)
												{
													scrFWeight2.setFieldValue("");
													scrFWTStamp2.setFieldValue("");
													scrFNettW.setFieldValue("");
													scrFNettTS.setFieldValue("");
													scfTolWarning.setHiddenFlag(false);
													return new VtiUserExitResult(999,"Nett weight is under order weight by " + (underWTol - nett));
												}
				
												if(nett > overWTol)
												{
													scrFWeight2.setFieldValue("");
													scrFWTStamp2.setFieldValue("");
													scrFNettW.setFieldValue("");
													scrFNettTS.setFieldValue("");
													scfTolWarning.setHiddenFlag(false);
													return new VtiUserExitResult(999,"Nett weight is over order weight by " + (nett - overWTol)); 
												}
											}
											else
											{
												return new VtiUserExitResult(999,"Tolerance config missing");
											}
										}
									}
								}
							}
				
							//End Tolerence check
						}
					}
				}
			}
			//Populate LDB Fields 
			

				ldbRowWeigh2[0].setFieldValue("WEIGHT2", scrFWeight2.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("WEIGHT2_D", currLdbDate);
				ldbRowWeigh2[0].setFieldValue("WEIGHT2_T", currLdbTime);
				ldbRowWeigh2[0].setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
				ldbRowWeigh2[0].setFieldValue("STATUS", "Weigh 2");
				ldbRowWeigh2[0].setFieldValue("USERID",sessionHeader.getUserId());
				ldbRowWeigh2[0].setFieldValue("DRIVER",driv.getFieldValue("FIELDVALUE"));
				ldbRowWeigh2[0].setFieldValue("CUSTOMER",custsupp.getFieldValue("FIELDVALUE"));
				ldbRowWeigh2[0].setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
				ldbRowWeigh2[0].setFieldValue("TRANSPORTTYPE",tranType.getFieldValue("FIELDVALUE"));
				ldbRowWeigh2[0].setFieldValue("ALLOC_WHT",allwgh.getFieldValue("FIELDVALUE"));
				ldbRowWeigh2[0].setFieldValue("DELVNO",ordNo.getFieldValue("FIELDVALUE"));
				ldbRowWeigh2[0].setFieldValue("REBAG",rebag.getFieldValue("FIELDVALUE"));
				ldbRowWeigh2[0].setFieldValue("SHIFT",shift.getFieldValue("FIELDVALUE"));
				ldbRowWeigh2[0].setFieldValue("TRAILERNO",tralnum.getFieldValue("FIELDVALUE"));
				ldbRowWeigh2[0].setFieldValue("PACKLINE",packline.getFieldValue("FIELDVALUE"));
				
				if(statusLdbRows[0].getFieldValue("STOCK").equalsIgnoreCase("X") ||
					poItemsTWLdbRows[0].getFieldValue("MATKL").equalsIgnoreCase("45"))
				{
					ldbRowWeigh2[0].setFieldValue("ORIGIN",scrTblItems.getRow(0).getFieldValue("ORIGIN"));
				}
							
				ldbRowWeigh2[0].setFieldValue("TIMESTAMP","");
				
				
				//Calculate Nettweight and write to screen
				w1 = scrFWeight1.getFloatFieldValue();
				w2 = scrFWeight2.getFloatFieldValue();
				
				nett = w1 - w2;
				
				if(nett < 0)
					nett = nett * -1;
			
				
				scrFNettW.setFieldValue(nett);
				scrFNettTS.setFieldValue(currTime);
				
				
				//Save Nett Weight
				ldbRowWeigh2[0].setFloatFieldValue("NETTWEIGHT", nett);
				ldbRowWeigh2[0].setFieldValue("NETTWEIGHT_T", currLdbTime);
				ldbRowWeigh2[0].setFieldValue("TIMESTAMP", "");
			
				try
				{
					wbTWLdbTable.saveRow(ldbRowWeigh2[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating weighin, please try again.",ee);
					return new VtiUserExitResult(999,"Unable to save Nett Weight data to the Purchase Order.");
				}
			
			
			//Change Status
				statusLdbRows[0].setFieldValue("WGH_STATUS","Weigh 2");
				statusLdbRows[0].setFieldValue("STATUS","W");
				statusLdbRows[0].setFieldValue("TIMESTAMP","");
				//statusLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
			
			try
			{
				statusLdbTable.saveRow(statusLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin status, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to update weigh 2 status.");
			}
			btnSave.setHiddenFlag(false);
			//change queue status for truck

			VtiExitLdbSelectCriterion [] qw2SelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "Complete"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qw2SelCondGrp = new VtiExitLdbSelectConditionGroup(qw2SelConds, true);
			VtiExitLdbTableRow[] qw2TLdbRows = queueLdbTable.getMatchingRows(qw2SelCondGrp);
			
			if(qw2TLdbRows.length > 0)
			{
				qw2TLdbRows[0].setFieldValue("Q_STATUS","Weigh 2");
				qw2TLdbRows[0].setFieldValue("TIMESTAMP","");
			
				try
				{
					queueLdbTable.saveRow(qw2TLdbRows[0]);
					dbCalls.ldbUpload("YSWB_QUEUE", this);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating queue status, please correct in the queue.",ee);
					return new VtiUserExitResult(000,"Unable to update the queue details for the Purchase Order.");
				}
			}
		}
		
		if(scrRBWeigh1.getFieldValue().length() > 1)
		{
			if(docType.equalsIgnoreCase("STOCKTRNF"))
				sessionHeader.setNextFunctionId("YSWB_TRANSFER");
			else
				sessionHeader.setNextFunctionId("YSWB_TRINBOUND");
		}
		
		
		Log.trace(0, " Next screen is " + sessionHeader.getNextFunctionId());
		
		return new VtiUserExitResult();
	}
}
