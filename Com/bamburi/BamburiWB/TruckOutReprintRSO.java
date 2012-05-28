package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class TruckOutReprintRSO extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
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

		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
		VtiUserExitScreenTable scrTblItems = getScreenTable("TB_ITEMS");
		if(scrTblItems == null) return new VtiUserExitResult (999,"Failed to initialise TB_ITEMS.");

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
		StringBuffer oNum = new StringBuffer();
		StringBuffer rsoNum = new StringBuffer();
		String soProd = " ";

		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Sales Order is not ready for processing, please check the status.");
		//Database TBL Declaration

		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable soItemsTWLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");

		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (soItemsTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_ITEMS.");

		//Dataset Declaration

		oNum.append(scrWFSalesOrd.getFieldValue());
		rsoNum.append("------------------");
		rsoNum.append(System.getProperty("line.separator"));
		
		for(int i = 0;i < scrTblItems.getRowCount();i++)
		{
			VtiExitLdbSelectCriterion [] soItemsTWSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(i).getFieldValue("VBELN_I")),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup soItemsTWSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemsTWSelConds, true);
			VtiExitLdbTableRow[] soItemsTWLdbRows = soItemsTWLdbTable.getMatchingRows(soItemsTWSelCondGrp);
			
					rsoNum.append(System.getProperty("line.separator"));
					rsoNum.append("                                                  " + soItemsTWLdbRows[0].getFieldValue("VBELN"));
					soProd = soProd + soItemsTWLdbRows[0].getFieldValue("ARKTX") + " : ";
		
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

		//Set next Function
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		sessionHeader.setNextFunctionId("YSWB_MAIN");
		
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
		
//Print Slip
			StringBuffer Header = new StringBuffer();
			StringBuffer stype = new StringBuffer();
			StringBuffer addDet = new StringBuffer();
			StringBuffer slipN = new StringBuffer();
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
		
			String b1 = " ";
			String b2 = " ";
			
			
		if(wbLdbRows.length > 0)
		{
			b1 = wbLdbRows[0].getFieldValue("WEIGHBRIDGE");
			b2 = wbLdbRows[0].getFieldValue("WEIGHBRIDGE2");
		}
			
		
			
					
		rsoNum.append("                                                  ____________");
		
		if(scrChkPrn.getFieldValue().equalsIgnoreCase("X"))
		{
			 feedFiller.append(System.getProperty("line.separator"));
			
			 slipN.append(scrFSlip.getFieldValue() + " REPRINT");
			 soTime.append(currTime);
			 stype.append("Sales Order");
			
			 truck.append(trckReg.getFieldValue("FIELDVALUE"));
			 cust.append(custsupp.getFieldValue("FIELDVALUE"));
			 trnsprt.append(transprtr.getFieldValue("FIELDVALUE"));
			 trlr.append(tralnum.getFieldValue("FIELDVALUE"));
			 dNote.append("");
			 pType.append(tranType.getFieldValue("FIELDVALUE"));
			 allocWght.append(allwgh.getFieldValue("FIELDVALUE"));
			 product.append(soProd);
			 d1.append(scrFWTStamp1.getFieldValue());
			 d2.append(scrFWTStamp2.getFieldValue());
			 t1.append("");
			 t2.append("");
			 wh1.append(scrFWeight1.getFieldValue());
			 wh2.append(scrFWeight2.getFieldValue());
			 nettw.append(scrFNettW.getFieldValue());
			 wb1.append(b1);
			 wb2.append(b2);
			 user.append(sessionHeader.getUserId());
			 driver.append(driv.getFieldValue("FIELDAVALUE"));
			 pl.append(packline.getFieldValue("FIELDVALUE"));
			 
			VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&slipN&", slipN.toString()),
				new VtiExitKeyValuePair("&otype&", stype.toString()),
				new VtiExitKeyValuePair("&soTime&", soTime.toString()),
				new VtiExitKeyValuePair("&onum&", oNum.toString()),
				new VtiExitKeyValuePair("&rsonum&", rsoNum.toString()),
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
		return new VtiUserExitResult();
	}
}
