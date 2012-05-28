package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class TruckOutSave extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

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
		VtiUserExitScreenField scrWStamp = getScreenField("TIMESTAMP");
		VtiUserExitScreenField scrFTruckReg = getScreenField("TRUCK_REG");


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
		if(scrWStamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");

		if(scrFNettW.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"No Nett weight.");
		//Database TBL Declaration
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable soHeaderCLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable tranQueLdbTable = getLocalDatabaseTable("YSWB_TRAN_QUEUE");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable soItemsTWLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		VtiExitLdbTable loadingLdbTable = getLocalDatabaseTable("YSWB_LOADING");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		
		if (soHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (tranQueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_TRAN_QUEUE.");
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (soItemsTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_ITEMS.");
		if (packingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (queueLdbTable == null) return new VtiUserExitResult (999,"Failed to initialise YSWB_QUEUE.");
		if (registerLdbTable == null) return new VtiUserExitResult (999,"Failed to initialise YSWB_REGISTER.");
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");

		if(	scrFWeight1.getFieldValue().length() == 0 || scrFWeight2.getFieldValue().length() == 0)
			return new VtiUserExitResult (999,"No weight measured.");
		
		
		//Variable Declarations
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		long w1 = 0;
		long w2 = 0;
		long nett = 0;
		String weighTS = currDate + " " + currTime;
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
		
		boolean hostConnected = isHostInterfaceConnected(hostName);
		String sErrorMsg = "";

		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Sales Order is not ready for processing, please check the status.");


		//Dataset Declaration
		VtiExitLdbSelectCriterion [] soHeaderCSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderCSelConds, true);
		VtiExitLdbTableRow[] soHeaderCLdbRows = soHeaderCLdbTable.getMatchingRows(soHeaderCSelCondGrp);
		
		if(soHeaderCLdbRows.length == 0)
			return new VtiUserExitResult(999, "Could not find the Sales Order to complete, please ensure second weight has been taken.");
		
		VtiExitLdbSelectCriterion [] soItemsTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soItemsTWSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemsTWSelConds, true);
		VtiExitLdbTableRow[] soItemsTWLdbRows = soItemsTWLdbTable.getMatchingRows(soItemsTWSelCondGrp);
		
		if(soHeaderCLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("New") || soHeaderCLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("Failed"))
		{
			return new VtiUserExitResult(999, "No vehicle inspection. No weigh in allowed.");
		}
		
		if(soHeaderCLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("Weigh 1"))
		{
			return new VtiUserExitResult(999, "No weigh 2 data yet. Save not allowed..");
		}
		
		//WB Dataset 
		VtiExitLdbSelectCriterion [] wbSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
		VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);
		
		if(wbLdbRows.length == 0)
			return new VtiUserExitResult(999, "No weigh 2 data yet. Save not allowed.");

		//Set next Function
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		sessionHeader.setNextFunctionId("YSWB_MAIN");
		
		//Create weight tran transactions in YSWB_TRAN_QUEUE
		VtiExitLdbTableRow ldbRowTranQ = tranQueLdbTable.newRow();
			
		//Populate TBL Fields
		ldbRowTranQ.setFieldValue("SERVERID", getServerId());
		ldbRowTranQ.setFieldValue("TRAN_NO", scrFSlip.getFieldValue());
		ldbRowTranQ.setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
		ldbRowTranQ.setFieldValue("VTIREF", soHeaderCLdbRows[0].getFieldValue("VTIREF"));
		ldbRowTranQ.setFieldValue("TRANTYPE", "DELIVERY");
		ldbRowTranQ.setFieldValue("TRANDATE", currLdbDate);
		ldbRowTranQ.setFieldValue("TIMESTAMP", "");
		
		if(!scrWFSalesOrd.getFieldValue().startsWith("0"))
			ldbRowTranQ.setFieldValue("YOFFLINE", "X");

		
		//Change Status
		soHeaderCLdbRows[0].setFieldValue("STATUS","Complete");
		soHeaderCLdbRows[0].setFieldValue("VTIREF",scrFSlip.getFieldValue());
		soHeaderCLdbRows[0].setFieldValue("TIMESTAMP","");
		soHeaderCLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
		
		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
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
		
			VtiExitLdbSelectCriterion [] regSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
			VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
			
			if(regLdbRows.length == 0)
			{
				
				if(wbLdbRows[0].getDoubleFieldValue("NETTWEIGHT") > 0)
				{
					VtiExitLdbSelectCriterion [] regCSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
									new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup regCSelCondGrp = new VtiExitLdbSelectConditionGroup(regCSelConds, true);
					VtiExitLdbTableRow[] regCLdbRows = registerLdbTable.getMatchingRows(regCSelCondGrp);
			
					if(regCLdbRows.length == 0)
					return new VtiUserExitResult(999,"Sales order not found in the register.");
					   
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
					return new VtiUserExitResult(999,"Registration in weigh status not found.");	
			}
			
			regLdbRows[0].setFieldValue("INSPSTATUS","C");
			regLdbRows[0].setFieldValue("DEPTIME",currLdbTime);
			regLdbRows[0].setFieldValue("TIMESTAMP","");
			
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
		wbLdbRows[0].setFieldValue("TRUCKREG",soHeaderCLdbRows[0].getFieldValue("TRUCK"));
		wbLdbRows[0].setFieldValue("DRIVER",driv.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("CUSTOMER",custsupp.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
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

		
		wbLdbRows[0].setFieldValue("TIMESTAMP","");
		soHeaderCLdbRows[0].setFieldValue("TIMESTAMP","");
		regLdbRows[0].setFieldValue("TIMESTAMP","");
		
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
		boolean isBulk = false;
		boolean getBulk = false;
		String soMatNr = "";
				
		for(int ib = 0;soItemsTWLdbRows.length > ib;ib++)
		{
			soMatNr = soItemsTWLdbRows[ib].getFieldValue("MATNR");

			for(int ibcon = 0;exclMatLdbRows.length > ibcon;ibcon++)
			{
				bulkMat = exclMatLdbRows[ibcon].getFieldValue("KEYVAL1");
				getBulk = soMatNr.equalsIgnoreCase(bulkMat);
				if(getBulk)
					isBulk = true;
			}
		}
		
		//VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		//if (packingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");
		if(!isBulk)
		{
			VtiExitLdbSelectCriterion [] packSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DISPATCH"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
			VtiExitLdbTableRow[] packLdbRows = packingLdbTable.getMatchingRows(packSelCondGrp);
		
			if(packLdbRows.length == 0)
				return new VtiUserExitResult(999, "Could not find the packing details for Sales Order " + scrWFSalesOrd.getFieldValue() + " to do the bags movement.");
		
			long packTime = 0;
			int issuePack = 0;
		
		
			for(int lp = 0;lp < packLdbRows.length;lp++)
			{
				if(packLdbRows[lp].getLongFieldValue("START_DATE") + packLdbRows[lp].getLongFieldValue("START_TIME") > packTime)
				{
				   issuePack = lp;
				   packTime = packLdbRows[lp].getLongFieldValue("START_DATE") + packLdbRows[lp].getLongFieldValue("START_TIME");
				}
			}
			String vtiRefInPack = "";
		
			vtiRefInPack = packLdbRows[issuePack].getFieldValue("VTIREF");
			//Set Dispatch flag
			VtiExitLdbSelectCriterion [] packDSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "PACKED"),
									new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiRefInPack),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup packDSelCondGrp = new VtiExitLdbSelectConditionGroup(packDSelConds, true);
			VtiExitLdbTableRow[] packDLdbRows = packingLdbTable.getMatchingRows(packDSelCondGrp);
		
			if(packDLdbRows.length == 0)
				return new VtiUserExitResult(999, "Could not find the packing details for Sales Order " + scrWFSalesOrd.getFieldValue() + " to do the bags movement.");

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
			
			//Removed issued from non dispatched
			VtiExitLdbSelectCriterion [] packNonDSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFTruckReg.getFieldValue()),
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
					ldbTQPacking.setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
					ldbTQPacking.setFieldValue("TRUCK", scrFTruckReg.getFieldValue());
					ldbTQPacking.setFieldValue("VTIREF", packLdbRows[tq].getFieldValue("VTIREF"));
					ldbTQPacking.setFieldValue("TRANTYPE", "PACKING");
					ldbTQPacking.setFieldValue("TIMESTAMP", "");
					ldbTQPacking.setFieldValue("TRANDATE", currLdbDate);
					if(!scrWFSalesOrd.getFieldValue().startsWith("0"))
						ldbTQPacking.setFieldValue("YOFFLINE", "X");
														
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
		
		try
		{
			tranQueLdbTable.saveRow(ldbRowTranQ);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to Save data to the Transaction Queue.",ee);
			return new VtiUserExitResult(999,"Unable to Save data to the Transaction Queue.");
		}
		
		try
		{
			
			wbLdbTable.saveRow(wbLdbRows[0]);
			soHeaderCLdbTable.saveRow(soHeaderCLdbRows[0]);
			if(qTLdbRows.length > 0)
				queueLdbTable.saveRow(qTLdbRows[0]);
			
			registerLdbTable.saveRow(regLdbRows[0]);
			
			hostConnected = isHostInterfaceConnected(hostName);
			
			if (hostConnected)
			{ 
				dbCall.ldbUpload("YSWB_PACKING", this);
				dbCall.ldbUpload("YSWB_SO_HEADER", this);
				dbCall.ldbUpload("YSWB_SO_ITEMS", this);
				dbCall.ldbUpload("YSWB_LOADING", this);
				dbCall.ldbUpload("YSWB_WB", this);

				dbCall.ldbUpload("YSWB_REGISTER", this);
				dbCall.ldbUpload("YSWB_TRAN_QUEUE", this);
			}
		}
		catch (VtiExitException ee)
		{
			Log.error("Error updating weighin status, please try again.",ee);
			return new VtiUserExitResult(999,"Unable to update Sales Order, check status.");
		}
		
//Print Slip
			StringBuffer Header = new StringBuffer();
			StringBuffer stype = new StringBuffer();
			StringBuffer addDet = new StringBuffer();
			StringBuffer slipN = new StringBuffer();
			StringBuffer oNum = new StringBuffer();
			StringBuffer soTime = new StringBuffer();
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
			 stype.append("Sales Order");
			 oNum.append(scrWFSalesOrd.getFieldValue());
			 slipN.append(scrFSlip.getFieldValue());
			 soTime.append(currTime);
			 truck.append(soHeaderCLdbRows[0].getFieldValue("TRUCK"));
			 cust.append(soHeaderCLdbRows[0].getFieldValue("NAME1"));
			 trnsprt.append(wbLdbRows[0].getFieldValue("TRANSPORTER"));
			 trlr.append(wbLdbRows[0].getFieldValue("TRAILERNO"));
			 dNote.append(wbLdbRows[0].getFieldValue("DELVNO"));
			 pType.append(wbLdbRows[0].getFieldValue("TRANSPORTTYPE"));
			 allocWght.append(wbLdbRows[0].getFieldValue("ALLOC_WHT"));
			 product.append(soItemsTWLdbRows[0].getFieldValue("ARKTX"));
			 d1.append(fu.shortDate(wbLdbRows[0].getStringFieldValue("WEIGHT1_D")));//Added the String to getStringFieldValue, 
			 d2.append(fu.shortDate(wbLdbRows[0].getStringFieldValue("WEIGHT2_D")));//hoping it will retrieve the date better
			 t1.append(fu.shortTime(wbLdbRows[0].getStringFieldValue("WEIGHT1_T")));//
			 t2.append(fu.shortTime(wbLdbRows[0].getStringFieldValue("WEIGHT2_T")));//
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
				new VtiExitKeyValuePair("&otype&", stype.toString()),
				new VtiExitKeyValuePair("&onum&", oNum.toString()),
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
			
			if(deviceNumber == 0)
				return new VtiUserExitResult(999,"Serial device number is not set up for this station.");
			
			//Invoking the print
			try
			{
				
				invokePrintTemplate("WBSlip" + deviceNumber, keyValuePairs);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error with Printout", ee);
			}
		}
				
		//Archive old WB
		sErrorMsg = ArchiveWB(scrWFSalesOrd.getFieldValue(),scrFTruckReg.getFieldValue());
		if(sErrorMsg.length() > 0)
			return new VtiUserExitResult(999, sErrorMsg);
		
		return new VtiUserExitResult();
	}
	
	private String ArchiveWB(String sVBELN, String sRegNo) throws VtiExitException
	{
		
		String sErrorMsg = "";
		
		if(sVBELN.length() == 10)
		{
			
			Date currNow = new Date();
			String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
			String currLdbTime = DateFormatter.format("HHmmss", currNow);
		
		
			VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
			VtiExitLdbTable soHeadLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
			VtiExitLdbTable soItemsLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		
			if (wbLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_WB.";
			if (soHeadLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_SO_HEADER.";
			if (soItemsLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_SO_ITEMS.";
		
			//WB
			VtiExitLdbSelectCriterion [] wbArcSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.NE_OPERATOR, sVBELN),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup wbArcSelCondGrp = new VtiExitLdbSelectConditionGroup(wbArcSelConds, true);
			VtiExitLdbTableRow[] wbArcLdbRows = wbLdbTable.getMatchingRows(wbArcSelCondGrp);
		
			if(wbArcLdbRows.length > 0)
			{
				for(int i =0; i < wbArcLdbRows.length;i++)
				{
					if(!wbArcLdbRows[i].getFieldValue("VBELN").equalsIgnoreCase(sVBELN) && sVBELN.length() == 10)
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
		
			//SO HEADER
			VtiExitLdbSelectCriterion [] soHArcSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.NE_OPERATOR, sVBELN),
			};
      
			VtiExitLdbSelectConditionGroup soHArcSelCondGrp = new VtiExitLdbSelectConditionGroup(soHArcSelConds, true);
			VtiExitLdbTableRow[] soHArcLdbRows = soHeadLdbTable.getMatchingRows(soHArcSelCondGrp);
		
		
			if(soHArcLdbRows.length > 0)
			{
				for(int i =0; i < soHArcLdbRows.length;i++)
				{
						soHArcLdbRows[i].setFieldValue("DEL_IND","X");
						soHArcLdbRows[i].setFieldValue("TIMESTAMP","");	
						
							//SO ITEMS
							VtiExitLdbSelectCriterion [] soIArcSelConds = 
							{
									new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
										new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
												new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, soHArcLdbRows[i].getFieldValue("VBELN")),
													new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.NE_OPERATOR,sVBELN),
							};
      
							VtiExitLdbSelectConditionGroup soIArcSelCondGrp = new VtiExitLdbSelectConditionGroup(soIArcSelConds, true);
							VtiExitLdbTableRow[] soIArcLdbRows = soItemsLdbTable.getMatchingRows(soIArcSelCondGrp);
				
							if(soIArcLdbRows.length > 0)
							{
								for(int si = 0; si < soIArcLdbRows.length;si++)
								{
									soIArcLdbRows[si].setFieldValue("DEL_IND","X");
									soIArcLdbRows[si].setFieldValue("TIMESTAMP","");				
														
									try
									{
										soItemsLdbTable.saveRow(soIArcLdbRows[si]);
									}
									catch(VtiExitException ee)
									{
										Log.error("SO Item not archiving.", ee);
									}
								}
							}
										
					try
					{
						soHeadLdbTable.saveRow(soHArcLdbRows[i]);
					}
					catch(VtiExitException ee)
					{
						Log.error("SO not archiving.", ee);
					}
				}
			}
		}
		
		return sErrorMsg;
	}
}
