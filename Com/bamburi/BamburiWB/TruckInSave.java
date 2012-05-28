package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class TruckInSave extends VtiUserExit
{
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
		VtiUserExitScreenField scrWStamp = getScreenField("TIMESTAMP");
		VtiUserExitScreenField scrFIsStuck = getScreenField("IS_STUCK");	
		VtiUserExitScreenField scrWTransfer = getScreenField("BSART");
		VtiUserExitScreenField scrWpos = getScreenField("WPOS");
		VtiUserExitScreenField scrMblnr = getScreenField("MBLNR");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrCurrentWght	=  getScreenField("KTMNG");
		//VtiUserExitScreenField scrPoQty = getScreenField("POQTY");

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
		if(scrWStamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");
		if(scrFIsStuck == null) return new VtiUserExitResult (999,"Failed to initialise IS_STUCK.");
		if(scrWTransfer == null) return new VtiUserExitResult (999,"Failed to initialise BSART.");
		if(scrWpos == null) return new VtiUserExitResult (999,"Failed to initialise WPOS.");
		if(scrMblnr == null) return new VtiUserExitResult (999,"Failed to initialise MBLNR.");
		if(scrRegNo == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");

		if(scrFNettW.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"No Nett weight.");
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
		VtiUserExitScreenTable scrTblItems = getScreenTable("TB_ITEMS");
		if(scrTblItems == null) return new VtiUserExitResult (999,"Failed to initialise TB_ITEMS.");

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
		String packingLine = "RAW";
		String packer = "RAW";
			
		
		String hostName = getHostInterfaceName();
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		boolean hostConnected = isHostInterfaceConnected(hostName);
		
		boolean isTransfer = false;
		
		if(scrWTransfer.getFieldValue().equalsIgnoreCase("UB"))
		{
			isTransfer = true;
			docType = "STOCKTRNF";
		}
		
		long w1 = 0;
		long w2 = 0;
		long nett = 0;

		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Purchase Order is not ready for processing, please check the status.");
		
		//Database TBL Declaration
		VtiExitLdbTable poHeaderCLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable tranQueLdbTable = getLocalDatabaseTable("YSWB_TRAN_QUEUE");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable poItemsTWLdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		
		if (poHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		if (tranQueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_TRAN_QUEUE.");
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (poItemsTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_ITEMS.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (packingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");

		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		if(queueLdbTable == null) return new VtiUserExitResult (999,"Failed to initialise YSWB_QUEUE.");
		
		//Dataset Declaration
		VtiExitLdbSelectCriterion [] poHeaderCSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("BSART", VtiExitLdbSelectCondition.CS_OPERATOR, scrWTransfer.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup poHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(poHeaderCSelConds, true);
		VtiExitLdbTableRow[] poHeaderCLdbRows = poHeaderCLdbTable.getMatchingRows(poHeaderCSelCondGrp);
		
		if(poHeaderCLdbRows.length == 0)
			return new VtiUserExitResult(999, "Could not find the Purchase Order to complete.");		
				
		VtiExitLdbSelectCriterion [] statusSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
		VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
		
		if(statusLdbRows.length == 0)
			return new VtiUserExitResult(999, "Could not find the Purchase Order to complete.");
		
		
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
			return new VtiUserExitResult(999, "No PO Items found for the Purchase Order.");
		
		//Update Item
			poItemsTWLdbRows[0].setFieldValue("KTMNG",scrCurrentWght.getDoubleFieldValue() - (scrFNettW.getDoubleFieldValue() / 1000));	
			poItemsTWLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				poItemsTWLdbTable.saveRow( poItemsTWLdbRows[0]);
			}
			catch (VtiExitException ie)
			{
				return new VtiUserExitResult(999,1,"PO item was not updated witht he latest quantity levels from SAP.");
			}
		
		if(statusLdbRows[0].getFieldValue("WGH_STATUS").equalsIgnoreCase("Failed"))
		{
			return new VtiUserExitResult(999, "No vehicle inspection. No weigh in allowed.");
		}
		
		if(statusLdbRows[0].getFieldValue("WGH_STATUS").equalsIgnoreCase("Weigh 1"))
		{
			return new VtiUserExitResult(999, "No weigh 2 data yet. Save not allowed.");
		}
		
	
		//WB Dataset 
		VtiExitLdbSelectCriterion [] wbSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFSlip.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
		VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);
		
		if(wbLdbRows.length == 0)
			return new VtiUserExitResult(999,"Weighbridge detail of weigh-in not found.");
		
		//Set next Function
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		sessionHeader.setNextFunctionId("YSWB_MAIN");
		
		//Create weight tran transactions in YSWB_TRAN_QUEUE
		VtiExitLdbTableRow ldbRowTranQ = tranQueLdbTable.newRow();
			
		//Populate TBL Fields
		ldbRowTranQ.setFieldValue("SERVERID", getServerId());
		ldbRowTranQ.setFieldValue("TRAN_NO", scrFSlip.getFieldValue());
		ldbRowTranQ.setFieldValue("EBELN", scrWFPurchOrd.getFieldValue());
		ldbRowTranQ.setFieldValue("VTIREF", scrVRef.getFieldValue());
		ldbRowTranQ.setFieldValue("TRUCK", scrRegNo.getFieldValue());
		ldbRowTranQ.setFieldValue("TIMESTAMP", "");
		
		long wgh1 = 0;
		long wgh2 = 0;
		
		//if wgh1 > wgh2 transfer
		
		if(isTransfer)
		{
			wgh1 = wbLdbRows[0].getLongFieldValue("WEIGHT1");
			wgh2 = wbLdbRows[0].getLongFieldValue("WEIGHT2");
				
			if(wgh1 < wgh2)
			{
				ldbRowTranQ.setFieldValue("TRANTYPE", "TRANSFER");
				
				VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
				if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		
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
				
				if(!isBulk)
				{
					VtiExitLdbSelectCriterion [] packSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "PACKED"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
					VtiExitLdbTableRow[] packLdbRows = packingLdbTable.getMatchingRows(packSelCondGrp);
		
					if(packLdbRows.length == 0)
						return new VtiUserExitResult(999, 1, "Could not find the packing details for STO Order " + scrWFPurchOrd.getFieldValue() + " to do the bags movement. Criteria used, Servergroup = " 
															 + getServerGroup() + ", Serverid = " + getServerId() + ", EBELN = " + scrWFPurchOrd.getFieldValue() + ", TruckReg = " + scrRegNo.getFieldValue()
															 + ", Status =  PACKED .");
		
					long packTime = 0;
					int issuePack = 0;
		
					for(int lp = 0;lp < packLdbRows.length;lp++)
					{
						if(packLdbRows[lp].getLongFieldValue("START_DATE") + packLdbRows[lp].getLongFieldValue("START_TIME") > packTime)
						{
						   issuePack = lp;
						   packTime = packLdbRows[lp].getLongFieldValue("START_DATE") + packLdbRows[lp].getLongFieldValue("START_TIME");
						   packingLine = packLdbRows[lp].getFieldValue("QUEUENO");
						   packer = packLdbRows[lp].getFieldValue("PACKER");
						}
					}
		
					String vtiRefInPack = "";
					vtiRefInPack = packLdbRows[issuePack].getFieldValue("VTIREF");
					//Set Dispatch Flag
					VtiExitLdbSelectCriterion [] packDSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "PACKED"),
											new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiRefInPack),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup packDSelCondGrp = new VtiExitLdbSelectConditionGroup(packDSelConds, true);
					VtiExitLdbTableRow[] packDLdbRows = packingLdbTable.getMatchingRows(packDSelCondGrp);
		
					if(packDLdbRows.length == 0)
						return new VtiUserExitResult(999, 1,"Could not find the packing details for STO Order " + scrWFPurchOrd.getFieldValue() + " to do the bags movement. Criteria used, Servergroup = " 
															 + getServerGroup() + ", Serverid = " + getServerId() + ", EBELN = " + scrWFPurchOrd.getFieldValue() + ", TruckReg = " + scrRegNo.getFieldValue()
															 + ", Status =  PACKED, VTIREF = " + vtiRefInPack + ".");
		
					for(int d = 0;d < packDLdbRows.length;d++)
					{
						packDLdbRows[d].setFieldValue("STATUS","Dispatch");
						packDLdbRows[d].setFieldValue("TIMESTAMP","");
		
						try
						{
							packingLdbTable.saveRow(packDLdbRows[d]);
						}
						catch (VtiExitException ee)
						{
							Log.error("Unable to Save dispatch data to the Packing table.",ee);
							return new VtiUserExitResult(999,"Unable to Save dispatched data to the Packing table.");
						}
					}
					//Remove issued from NonDispatched
					VtiExitLdbSelectCriterion [] packNonDSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "PACKED"),
											new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.NE_OPERATOR, vtiRefInPack),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup packNonDSelCondGrp = new VtiExitLdbSelectConditionGroup(packNonDSelConds, true);
					VtiExitLdbTableRow[] packNonDLdbRows = packingLdbTable.getMatchingRows(packNonDSelCondGrp);
		
					if(packNonDLdbRows.length > 0)			
					{
						for(int nd = 0;nd < packNonDLdbRows.length;nd++)
						{
							packNonDLdbRows[nd].setFieldValue("ISSUED","");
							packNonDLdbRows[nd].setFieldValue("TIMESTAMP","");
		
							try
							{
								packingLdbTable.saveRow(packNonDLdbRows[nd]);
							}
							catch (VtiExitException ee)
							{
								Log.error("Unable to Save dispatch data to the Packing table.",ee);
								return new VtiUserExitResult(999,"Unable to Save dispatched data to the Packing table.");
							}
						}
					}
		
					long tranQNo;
					for(int tq = 0;tq < packLdbRows.length;tq++)
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
				
						VtiExitLdbTableRow ldbTQPacking = tranQueLdbTable.newRow();
						ldbTQPacking.setFieldValue("SERVERID", getServerId());
						ldbTQPacking.setFieldValue("TRAN_NO", tranQNo);
						ldbTQPacking.setFieldValue("EBELN", scrWFPurchOrd.getFieldValue());
						ldbTQPacking.setFieldValue("TRUCK", scrRegNo.getFieldValue());
						ldbTQPacking.setFieldValue("VTIREF", packLdbRows[tq].getFieldValue("VTIREF"));
						ldbTQPacking.setFieldValue("TRANTYPE", "PACKING_PO");
						if(!scrWFPurchOrd.getFieldValue().startsWith("0"))
							ldbTQPacking.setFieldValue("YOFFLINE", "X");
						ldbTQPacking.setFieldValue("TIMESTAMP", "");
						ldbTQPacking.setFieldValue("TRANDATE", currLdbDate);
										
						try
						{	 
							tranQueLdbTable.saveRow(ldbTQPacking);
							if (hostConnected)
							{
								dbCall.ldbUpload("YSWB_PACKING", this);
							}
						}
						catch (VtiExitException ee)
						{
							Log.error("Unable to Save data to the YSWB_TRAN_QUEUE table.",ee);
							return new VtiUserExitResult(999,"Unable to Save data to the Transaction Queue table.");
						}
					}
				}
			}
			else if(wgh1 > wgh2)
			{
				ldbRowTranQ.setFieldValue("TRANTYPE", "GR");
			}
		}
		else
		{
			ldbRowTranQ.setFieldValue("TRANTYPE", "GR");
		}
		
		//if wgh1 < wgh2 GR
		ldbRowTranQ.setFieldValue("TRANDATE", currLdbDate);
		if(!scrWFPurchOrd.getFieldValue().startsWith("0"))
							ldbRowTranQ.setFieldValue("YOFFLINE", "X");
		
		try
		{
			tranQueLdbTable.saveRow(ldbRowTranQ);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to Save data to the Transaction Queue.",ee);
			return new VtiUserExitResult(999,"Unable to Save data to the Transaction Queue.");
		}
		//Change Status
		
			VtiExitLdbSelectCriterion [] regSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
			VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
			
			if(regLdbRows.length == 0)
				return new VtiUserExitResult(999,"Purchase order not found in the register.");	
			
			
		if(scrFIsStuck.getFieldValue().equalsIgnoreCase("X"))
		{
			VtiExitLdbTableRow stuckStatLdbRow = statusLdbTable.newRow();
			
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
			
			statusLdbRows[0].setFieldValue("WGH_STATUS","Complete");
			statusLdbRows[0].setFieldValue("STATUS","C");
			statusLdbRows[0].setFieldValue("TIMESTAMP","");
			
			stuckStatLdbRow.setFieldValue("SERVERGRP",regLdbRows[0].getFieldValue("SERVERGRP"));
			stuckStatLdbRow.setFieldValue("SERVERID",regLdbRows[0].getFieldValue("SERVERID"));
			stuckStatLdbRow.setFieldValue("TRUCKREG",statusLdbRows[0].getFieldValue("TRUCKREG"));
			stuckStatLdbRow.setFieldValue("STATUS","A");
			stuckStatLdbRow.setFieldValue("WGH_STATUS","ASSIGNED");
			stuckStatLdbRow.setFieldValue("ARR_DATE",currLdbDate);
			stuckStatLdbRow.setFieldValue("ARR_TIME",currLdbTime);
			//stuckStatLdbRow.setFieldValue("USERID",statusLdbRows[0].getFieldValue("USERID"));
			stuckStatLdbRow.setFieldValue("EBELN",scrWFPurchOrd.getFieldValue());
			stuckStatLdbRow.setFieldValue("LGORT",statusLdbRows[0].getFieldValue("LGORT"));
			stuckStatLdbRow.setFieldValue(docType,scrWFPurchOrd.getFieldValue());
			stuckStatLdbRow.setFieldValue("PREFERED",statusLdbRows[0].getFieldValue("PREFERED"));
			stuckStatLdbRow.setFieldValue("INSP_VTI_REF",statusLdbRows[0].getFieldValue("INSP_VTI_REF"));
			stuckStatLdbRow.setFieldValue("INSP_DATE",statusLdbRows[0].getFieldValue("INSP_DATE"));
			stuckStatLdbRow.setFieldValue("INSP_TIME",statusLdbRows[0].getFieldValue("INSP_TIME"));
			stuckStatLdbRow.setFieldValue("DOCTYPE",scrWTransfer.getFieldValue());
			stuckStatLdbRow.setFieldValue("VTIREF",refNo);
			stuckStatLdbRow.setFieldValue("TIMESTAMP","");
			
			customer = poHeaderCLdbRows[0].getFieldValue("NAME1");
			truckRno = statusLdbRows[0].getFieldValue("TRUCKREG");
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
			registerLdbRow.setFieldValue("EBELN",scrWFPurchOrd.getFieldValue());
			registerLdbRow.setFieldValue(docType,regLdbRows[0].getFieldValue(docType));
			registerLdbRow.setFieldValue("TRAILREG",regLdbRows[0].getFieldValue("TRAILREG"));
			registerLdbRow.setFieldValue("LICENSENO",regLdbRows[0].getFieldValue("LICENSENO"));
			registerLdbRow.setFieldValue("TELNO",regLdbRows[0].getFieldValue("TELNO"));
			registerLdbRow.setFieldValue("GATE_PASS",regLdbRows[0].getFieldValue("GATE_PASS"));
			registerLdbRow.setFieldValue("ASSTIME",currLdbTime);
			registerLdbRow.setFieldValue("TRANSPORTER",regLdbRows[0].getFieldValue("TRANSPORTER"));
			registerLdbRow.setFieldValue("INSPSTATUS","A");
			registerLdbRow.setFieldValue("USERID",statusLdbRows[0].getFieldValue("USERID"));
			registerLdbRow.setFieldValue("VTI_REF",refNo);
			registerLdbRow.setFieldValue("TIMESTAMP","");
			
			
			try
			{
				statusLdbTable.saveRow(stuckStatLdbRow);
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
			statusLdbRows[0].setFieldValue("WGH_STATUS","Complete");
			statusLdbRows[0].setFieldValue("STATUS","C");
			statusLdbRows[0].setFieldValue("TIMESTAMP", "");
			customer = poHeaderCLdbRows[0].getFieldValue("NAME1");
			truckRno = statusLdbRows[0].getFieldValue("TRUCKREG");
		}
		
		//statusLdbRows[0].setFieldValue("TIMESTAMP",scrWStamp.getFieldValue());
		
		regLdbRows[0].setFieldValue("INSPSTATUS","C");
		regLdbRows[0].setFieldValue("DEPTIME",currLdbTime);
		regLdbRows[0].setFieldValue("TIMESTAMP","");
		
		try
		{
			statusLdbTable.saveRow(statusLdbRows[0]);
			registerLdbTable.saveRow(regLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Error updating Register status, please try again.",ee);
			return new VtiUserExitResult(999,"Unable to change the Register.");
		}
		
		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFPurchOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
			if(qTLdbRows.length > 0)	
			{
				qTLdbRows[0].setFieldValue("Q_STATUS","Complete");
				qTLdbRows[0].setFieldValue("TIMESTAMP","");
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
		wbLdbRows[0].setFieldValue("TRUCKREG",scrRegNo.getFieldValue());
		wbLdbRows[0].setFieldValue("DRIVER",driv.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("CUSTOMER",custsupp.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("TRANSPORTTYPE",tranType.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("ALLOC_WHT",allwgh.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("DELVNO",ordNo.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("REBAG",rebag.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("PACKLINE",packingLine);
		wbLdbRows[0].setFieldValue("PACKLOADER",packer);
		wbLdbRows[0].setFieldValue("SEGMENT",segtype.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("SHIFT",shift.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("REMARKS",remarks.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("TRAILERNO",tralnum.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
		//wbLdbRows[0].setFieldValue("EBELP",scrWpos.getFieldValue());
		wbLdbRows[0].setFieldValue("TIMESTAMP","");		
		wbLdbRows[0].setFieldValue("PASS_NUMB",regLdbRows[0].getFieldValue("GATE_PASS"));
		
		try
		{
			
			wbLdbTable.saveRow(wbLdbRows[0]);
				
			if(qTLdbRows.length > 0)
				queueLdbTable.saveRow(qTLdbRows[0]);
			
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
			 ptype.append("Purchase Order");
			 slipN.append(scrFSlip.getFieldValue());
			 oNum.append(scrWFPurchOrd.getFieldValue());
			 soTime.append(currTime);
			 truck.append(scrRegNo.getFieldValue());
			 cust.append(customer);
			 trnsprt.append(wbLdbRows[0].getFieldValue("TRANSPORTER"));
			 trlr.append(wbLdbRows[0].getFieldValue("TRAILERNO"));
			 dNote.append(wbLdbRows[0].getFieldValue("DELVNO"));
			 pType.append(wbLdbRows[0].getFieldValue("TRANSPORTTYPE"));
			 allocWght.append(wbLdbRows[0].getFieldValue("ALLOC_WHT"));
			 product.append(poItemsTWLdbRows[0].getFieldValue("TXZ01"));
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
		
		ArchiveWB(scrWFPurchOrd.getFieldValue(), scrRegNo.getFieldValue(), scrVRef.getFieldValue());

		return new VtiUserExitResult();
	}
	
	private String ArchiveWB(String sEBELN, String sRegNo, String sVti) throws VtiExitException
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


