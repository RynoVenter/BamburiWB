package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class DelTruckSave extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrWDelivDoc = getScreenField("VBELN");
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
		VtiUserExitScreenField scrWpos = getScreenField("WPOS");
		VtiUserExitScreenField scrMblnr = getScreenField("MBLNR");
		VtiUserExitScreenField scrRegNo = getScreenField("TRUCK_REG");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");

		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrWDelivDoc == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
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
		if(scrWpos == null) return new VtiUserExitResult (999,"Failed to initialise WPOS.");
		if(scrMblnr == null) return new VtiUserExitResult (999,"Failed to initialise MBLNR.");
		if(scrRegNo == null) return new VtiUserExitResult (999,"Failed to initialise TRUCK_REG.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");

		if(scrFNettW.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"No Nett weight.");
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");

		if(scrFWeight1.getFieldValue().length() == 0 || scrFWeight2.getFieldValue().length() == 0)
			return new VtiUserExitResult (999,"No weight measured.");
		
		if(scrWpos.getFieldValue().length() == 0)
			return new VtiUserExitResult (999,"Please select item to Deliver.");
		
		//Variable Declarations
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String weighTS = currDate + " " + currTime;
		String customer = "";
		String truckRno = "";
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
		
		long w1 = 0;
		long w2 = 0;
		long nett = 0;

		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Order is not ready for processing, please check the status.");
		
		//Database TBL Declaration
		VtiExitLdbTable icHeaderCLdbTable = getLocalDatabaseTable("YSWB_IC_HEADER");
		VtiExitLdbTable tranQueLdbTable = getLocalDatabaseTable("YSWB_TRAN_QUEUE");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable icItemsTWLdbTable = getLocalDatabaseTable("YSWB_IC_ITEMS");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		
		if (icHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_HEADER.");
		if (tranQueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_TRAN_QUEUE.");
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (icItemsTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_ITEMS.");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (packingLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PACKING.");

		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		if(queueLdbTable == null) return new VtiUserExitResult (999,"Failed to initialise YSWB_QUEUE.");
		if(configLdbTable == null) return new VtiUserExitResult (999,"Failed to initialise YSWB_CONFIG.");
		
		//Dataset Declaration
		VtiExitLdbSelectCriterion [] icHeaderCSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup icHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(icHeaderCSelConds, true);
		VtiExitLdbTableRow[] icHeaderCLdbRows = icHeaderCLdbTable.getMatchingRows(icHeaderCSelCondGrp);
		
		if(icHeaderCLdbRows.length == 0)
			return new VtiUserExitResult(999, "Could not find the Order to complete.");		
				
		VtiExitLdbSelectCriterion [] statusSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Weigh 2"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
		VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);
		
		if(statusLdbRows.length == 0)
			return new VtiUserExitResult(999, "Could not find the Order to complete.");
		
		
		VtiExitLdbSelectCriterion [] icItemsTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup icItemsTWSelCondGrp = new VtiExitLdbSelectConditionGroup(icItemsTWSelConds, true);
		VtiExitLdbTableRow[] icItemsTWLdbRows = icItemsTWLdbTable.getMatchingRows(icItemsTWSelCondGrp);
		
		if(icItemsTWLdbRows.length == 0)
			return new VtiUserExitResult(999, "No Items found for the Order.");
		
		if(statusLdbRows[0].getFieldValue("WGH_STATUS").equalsIgnoreCase("Failed"))
		{
			return new VtiUserExitResult(999, "No vehicle inspection. No weigh in allowed.");
		}
		
		if(statusLdbRows[0].getFieldValue("WGH_STATUS").equalsIgnoreCase("Weigh 1"))
		{
			return new VtiUserExitResult(999, "No weigh 2 data yet. Save not allowed.");
		}
		
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
				
		for(int ib = 0;icItemsTWLdbRows.length > ib;ib++)
		{
			icMatNr = icItemsTWLdbRows[ib].getFieldValue("MATNR");

			for(int ibcon = 0;exclMatLdbRows.length > ibcon;ibcon++)
			{
				bulkMat = exclMatLdbRows[ibcon].getFieldValue("KEYVAL1");
				getBulk = icMatNr.equalsIgnoreCase(bulkMat);
				if(getBulk)
					isBulk = true;
			}
		}
		
		//WB Dataset 
		VtiExitLdbSelectCriterion [] wbSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
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
		ldbRowTranQ.setFieldValue("VBELN", scrWDelivDoc.getFieldValue());
		ldbRowTranQ.setFieldValue("VTIREF", scrVRef.getFieldValue());
		ldbRowTranQ.setFieldValue("TRUCK", scrRegNo.getFieldValue());
		if(!isBulk)
			ldbRowTranQ.setFieldValue("TRANTYPE", "INTERCOMPANY");
		else
			ldbRowTranQ.setFieldValue("TRANTYPE", "IC_BULK");
		ldbRowTranQ.setFieldValue("TRANDATE", currLdbDate);
		ldbRowTranQ.setFieldValue("TIMESTAMP", "");
		
		if(!scrWDelivDoc.getFieldValue().startsWith("0"))
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
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
			VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
			
			if(regLdbRows.length == 0)
				return new VtiUserExitResult(999,"Intercompany order not found in the register.");		
			
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
			
			statusLdbRows[0].setFieldValue("WGH_STATUS","ASSIGNED");
			statusLdbRows[0].setFieldValue("STATUS","A");
			statusLdbRows[0].setFieldValue("TIMESTAMP","");
			
			stuckStatLdbRow.setFieldValue("SERVERGRP",regLdbRows[0].getFieldValue("SERVERGRP"));
			stuckStatLdbRow.setFieldValue("SERVERID",regLdbRows[0].getFieldValue("SERVERID"));
			stuckStatLdbRow.setFieldValue("TRUCKREG",statusLdbRows[0].getFieldValue("TRUCKREG"));
			stuckStatLdbRow.setFieldValue("STATUS","A");
			stuckStatLdbRow.setFieldValue("WGH_STATUS","ASSIGNED");
			stuckStatLdbRow.setFieldValue("ARR_DATE",currLdbDate);
			stuckStatLdbRow.setFieldValue("ARR_TIME",currLdbTime);
			//stuckStatLdbRow.setFieldValue("USERID",statusLdbRows[0].getFieldValue("USERID"));
			stuckStatLdbRow.setFieldValue("DELIVDOC",scrWDelivDoc.getFieldValue());
			stuckStatLdbRow.setFieldValue("PREFERED",statusLdbRows[0].getFieldValue("PREFERED"));
			stuckStatLdbRow.setFieldValue("INSP_VTI_REF",statusLdbRows[0].getFieldValue("INSP_VTI_REF"));
			stuckStatLdbRow.setFieldValue("INSP_DATE",statusLdbRows[0].getFieldValue("INSP_DATE"));
			stuckStatLdbRow.setFieldValue("INSP_TIME",statusLdbRows[0].getFieldValue("INSP_TIME"));
			stuckStatLdbRow.setFieldValue("DOCTYPE","ZIC");
			stuckStatLdbRow.setFieldValue("VTIREF",refNo);
			stuckStatLdbRow.setFieldValue("TIMESTAMP","");
			
			customer = icHeaderCLdbRows[0].getFieldValue("NAME1");
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
			registerLdbRow.setFieldValue("DELIVDOC",regLdbRows[0].getFieldValue("DELIVDOC"));
			registerLdbRow.setFieldValue("TRANSPORTER",regLdbRows[0].getFieldValue("TRANSPORTER"));
			registerLdbRow.setFieldValue("INSPSTATUS","A");

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
			statusLdbRows[0].setFieldValue("TIMESTAMP","");
			customer = icHeaderCLdbRows[0].getFieldValue("NAME1");
			truckRno = statusLdbRows[0].getFieldValue("TRUCKREG");
		}
		
		//statusLdbRows[0].setFieldValue("TIMESTAMP",scrWStamp.getFieldValue());

		
		regLdbRows[0].setFieldValue("INSPSTATUS","C");

		regLdbRows[0].setFieldValue("DEPTIME",currLdbTime);
		regLdbRows[0].setFieldValue("TIMESTAMP","");
		
		try
		{
			registerLdbTable.saveRow(regLdbRows[0]);
			statusLdbTable.saveRow(statusLdbRows[0]);
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
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
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
		if(!isBulk)
		{
			//Do the packing
			VtiExitLdbSelectCriterion [] packSelConds = 
				{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DISPATCH"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
				VtiExitLdbTableRow[] packLdbRows = packingLdbTable.getMatchingRows(packSelCondGrp);
		
				if(packLdbRows.length == 0)
					return new VtiUserExitResult(999, "Could not find the packing details for Inter Company Order " + scrWDelivDoc.getFieldValue() + " to do the bags movement.");
		
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
				//Set Dispatch flag for Rob's bapi
				VtiExitLdbSelectCriterion [] packDSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "PACKED"),
										new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiRefInPack),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup packDSelCondGrp = new VtiExitLdbSelectConditionGroup(packDSelConds, true);
				VtiExitLdbTableRow[] packDLdbRows = packingLdbTable.getMatchingRows(packDSelCondGrp);
		
				if(packDLdbRows.length == 0)
					return new VtiUserExitResult(999, "Could not find the packing details for Inter Company Order " + scrWDelivDoc.getFieldValue() + " to do the bags movement.");
		
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
				//remove issued values for non dispatched
				VtiExitLdbSelectCriterion [] packNonDSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "PACKED"),
										new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.NE_OPERATOR, vtiRefInPack),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup packNonDSelCondGrp = new VtiExitLdbSelectConditionGroup(packNonDSelConds, true);
				VtiExitLdbTableRow[] packNonDLdbRows = packingLdbTable.getMatchingRows(packNonDSelCondGrp);
		
				if(packNonDLdbRows.length == 0)
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
					ldbTQPacking.setFieldValue("VBELN", scrWDelivDoc.getFieldValue());
					ldbTQPacking.setFieldValue("TRUCK", scrRegNo.getFieldValue());
					ldbTQPacking.setFieldValue("VTIREF", packLdbRows[tq].getFieldValue("VTIREF"));
					ldbTQPacking.setFieldValue("TRANTYPE", "PACKING_PO");
					ldbTQPacking.setFieldValue("TIMESTAMP", "");
					ldbTQPacking.setFieldValue("TRANDATE", currLdbDate);
					
					if(!scrWDelivDoc.getFieldValue().startsWith("0"))
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
		wbLdbRows[0].setFieldValue("PACKLINE",packline.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("PACKLOADER",packLoad.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("SEGMENT",segtype.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("SHIFT",shift.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("REMARKS",remarks.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("TRAILERNO",tralnum.getFieldValue("FIELDVALUE"));
		wbLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
		wbLdbRows[0].setFieldValue("EBELP",scrWpos.getFieldValue());
		wbLdbRows[0].setFieldValue("TIMESTAMP","");
		
		try
		{
			wbLdbTable.saveRow(wbLdbRows[0]);
			icHeaderCLdbTable.saveRow(icHeaderCLdbRows[0]);
			if(qTLdbRows.length > 0)
				queueLdbTable.saveRow(qTLdbRows[0]);
			
			hostConnected = isHostInterfaceConnected(hostName);

				if (hostConnected)
				{ 
					dbCall.ldbUpload("YSWB_IC_HEADER", this);
					dbCall.ldbUpload("YSWB_IC_ITEMS", this);
					dbCall.ldbUpload("YSWB_WB", this);
					dbCall.ldbUpload("YSWB_TRAN_QUEUE", this);
					dbCall.ldbUpload("YSWB_INSPECT", this);
					dbCall.ldbUpload("YSWB_REGISTER", this);
					dbCall.ldbUpload("YSWB_STATUS", this);
					dbCall.ldbUpload("YSWB_QUEUE", this);
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
			 ptype.append("Inter Company");
			 slipN.append(scrFSlip.getFieldValue());
			 oNum.append(scrWDelivDoc.getFieldValue());
			 soTime.append(currTime);
			 truck.append(scrRegNo.getFieldValue());
			 cust.append("Bamburi Inter Company - NGP");
			 trnsprt.append(wbLdbRows[0].getFieldValue("TRANSPORTER"));
			 trlr.append(wbLdbRows[0].getFieldValue("TRAILERNO"));
			 dNote.append(wbLdbRows[0].getFieldValue("DELVNO"));
			 pType.append(wbLdbRows[0].getFieldValue("TRANSPORTTYPE"));
			 allocWght.append(wbLdbRows[0].getFieldValue("ALLOC_WHT"));
			 product.append(icItemsTWLdbRows[0].getFieldValue("ARKTX"));
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
		

		return new VtiUserExitResult();
	}
}
