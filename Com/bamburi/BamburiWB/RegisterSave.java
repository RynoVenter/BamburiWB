package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class RegisterSave extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		Date currNow = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String errorMsg = "";
		long interval = 0;
		boolean prefd = false;
		boolean ship = false;
		boolean stock = false;
		boolean rotate = false;
		boolean bInspexpire = false;
		boolean bExtend = false;
		long inspRefNo = 0;
		boolean hasRotate = false;
		String inspDate = "";
		String inspExDate = "";
		String inspExTime = "";
		String currShift = "currShift";
		String inspShift = "inspShift";
		int inspTime = 0;
		String shift = "";
		int shiftStart = 0;
		int shiftEnd = 0;
		String dbShift = "";
		String sErrorMsg;
		long refNo = 0;
		String sTruckType = "Normal";
		StringBuffer sbInspEndMsg = new StringBuffer();
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrRegno = getScreenField("REGNO");
		VtiUserExitScreenField scrSRegno = getScreenField("REGNO_S");
		VtiUserExitScreenField scrTrailer = getScreenField("TRAILREG");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrDate = getScreenField("S_DATE");
		VtiUserExitScreenField scrWDate = getScreenField("DATE");
		VtiUserExitScreenField scrCompany = getScreenField("COMPANY");
		VtiUserExitScreenField scrDriver = getScreenField("DRIVER");
		VtiUserExitScreenField scrWDriver = getScreenField("DRIVER_S");
		VtiUserExitScreenField scrIDNo = getScreenField("IDNUMBER");
		VtiUserExitScreenField scrTransType = getScreenField("TRANSTYPE");
		VtiUserExitScreenField scrNoAxels = getScreenField("NOAXELS");
		VtiUserExitScreenField scrMaxWeight = getScreenField("MAXWEIGHT");		
	    VtiUserExitScreenField scrVbeln = getScreenField("VBELN");
		VtiUserExitScreenField scrWVbeln = getScreenField("VBELN_S");
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		VtiUserExitScreenField scrWEbeln = getScreenField("EBELN_S");
//		VtiUserExitScreenField scrContractor = getScreenField("CONTRACTOR");
//		VtiUserExitScreenField scrSelf = getScreenField("SELF");
		VtiUserExitScreenField scrTimestamp = getScreenField("TIMESTAMP");		
		VtiUserExitScreenField scrUserID = getScreenField("USERID");
		VtiUserExitScreenField scrLicNo = getScreenField("LICENSENO");
		VtiUserExitScreenField scrTelNo = getScreenField("TELNO");
		VtiUserExitScreenField scrDocNum = getScreenField("VBELNQT");
		VtiUserExitScreenField scrGatePass = getScreenField("GATEPASS");
		VtiUserExitScreenField scrTruckType = getScreenField("TRUCKTYPE");
		VtiUserExitScreenField rdoNormal = getScreenField("RDONORMAL");
		VtiUserExitScreenField rdoScrap = getScreenField("RDOSCRAP");
		VtiUserExitScreenField rdoStock = getScreenField("RDOSTOCK");
		VtiUserExitScreenField rdoRotate = getScreenField("RDOROTATE");
		VtiUserExitScreenField rdoRotateOs = getScreenField("RDOROTATEOS");
		VtiUserExitScreenField chkExpired = getScreenField("CHKEXPIRED");
	
		if(scrRegno == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");
		if(scrTrailer == null) return new VtiUserExitResult (999,"Failed to initialise TRAILREG.");
		if(scrTime == null) return new VtiUserExitResult (999,"Failed to initialise TIME.");
		if(scrDate == null) return new VtiUserExitResult (999,"Failed to initialise S_DATE.");
		if(scrWDate == null) return new VtiUserExitResult (999,"Failed to initialise DATE.");
		if(scrCompany == null) return new VtiUserExitResult (999,"Failed to initialise COMPANY.");
		if(scrDriver == null) return new VtiUserExitResult (999,"Failed to initialise DRIVER.");
		if(scrWDriver == null) return new VtiUserExitResult (999,"Failed to initialise DRIVER_S.");
		if(scrIDNo == null) return new VtiUserExitResult (999,"Failed to initialise IDNUMBER.");
		if(scrTruckType == null) return new VtiUserExitResult (999,"Failed to initialise TRUCKTYPE.");
		if(scrNoAxels == null) return new VtiUserExitResult (999,"Failed to initialise NOAXELS.");
		if(scrMaxWeight == null) return new VtiUserExitResult (999,"Failed to initialise MAXWEIGHT.");		
		if(scrVbeln == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
		if(scrEbeln == null) return new VtiUserExitResult (999,"Failed to initialise EBELN.");
		if(scrWVbeln == null) return new VtiUserExitResult (999,"Failed to initialise VBELN_S.");
		if(scrWEbeln == null) return new VtiUserExitResult (999,"Failed to initialise EBELN_S.");
//		if(scrContractor == null) return new VtiUserExitResult (999,"Failed to initialise CONTRACTOR.");
//		if(scrSelf == null) return new VtiUserExitResult (999,"Failed to initialise SELF.");
		if(scrTimestamp == null) return new VtiUserExitResult (999,"Failed to initialise TIMESTAMP.");
		if(scrUserID == null) return new VtiUserExitResult (999,"Failed to initialise USERID.");
		if(scrLicNo == null) return new VtiUserExitResult (999,"Failed to initialise LICENSENO.");
		if(scrTelNo == null) return new VtiUserExitResult (999,"Failed to initialise TELNO.");
		if(scrDocNum == null) return new VtiUserExitResult (999,"Failed to initialise VBELNQT.");
		if(scrGatePass == null) return new VtiUserExitResult (999,"Failed to initialise GATEPASS.");
		if(chkExpired == null) return new VtiUserExitResult (999,"Failed ti initialise CHKEXPIRED.");
		
		DBCalls dbCall = new DBCalls();
		
		//Database TBL Declaration
		VtiExitLdbTable soHeaderCLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable poHeaderCLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable gpLdbTable = getLocalDatabaseTable("YSWB_GATEPASS");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");

		//Variable Declarations
		if(scrNoAxels.getFieldValue().length() == 0)
		{
			return new VtiUserExitResult(999,"Please select an axle amount.");		
		}
		if(scrMaxWeight.getDoubleFieldValue() < 1) return new VtiUserExitResult (999,"Please indicate what the Max Weight for this truck is.");		
		
		if(rdoStock.getFieldValue().length() > 0 && scrEbeln.getFieldValue().length() == 0)
			return new VtiUserExitResult (999,1,"Please indicate the PO for the consignment stock.");
		
		if(rdoRotateOs.getFieldValue().length()> 0 && scrEbeln.getFieldValue().length() == 0)
			return new VtiUserExitResult (999,1,"Please indicate the PO for the rotate own stock.");
		
		if((rdoScrap.getFieldValue().length()> 0  
			|| rdoRotate.getFieldValue().length()> 0 ) 
		   && scrGatePass.getFieldValue().length() > 0)
			return new VtiUserExitResult (999,"Gate Pass not needed for scrap or rotate cs.");
		
		if(scrEbeln.getFieldValue().length() > 0)
			if(scrGatePass.getFieldValue().length() > 0)
				return new VtiUserExitResult (999,"Please select either a Purchase Order or a Gate Pass.");
		
		if(scrVbeln.getFieldValue().length() > 0)
			if(scrGatePass.getFieldValue().length() > 0)
				return new VtiUserExitResult (999,"Please select a Sales Inquiry only.");

		if(scrEbeln.getFieldValue().length() > 0)
			if(scrVbeln.getFieldValue().length() > 0) return new VtiUserExitResult (999,"Please select a Sales Order or a Purchase Order");
		
		if(scrRegno.getFieldValue().length() == 0 || scrRegno.getFieldValue().length() > 10 )
			return new VtiUserExitResult(999, "Please enter a valid Registration Number.");
		
		if(scrIDNo.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "Please provide the ID number or passport number.");
		
		if(scrDriver.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "Please provide the Drivers name.");
		
		if(scrTruckType.getFieldValue().equalsIgnoreCase("SCRAP"))
		{
		   scrEbeln.setFieldValue("");
		   scrGatePass.setFieldValue("");
		   sTruckType = scrTruckType.getFieldValue();
		}
		else
		{
			 sTruckType = scrTruckType.getFieldValue();
		}
		
		//if((scrContractor.getFieldValue().length() + scrSelf.getFieldValue().length()) < 1)
		//	return new VtiUserExitResult(999, "Please indicate whether it is a Contractor of Self collecting truck.");
		
		//if(scrVbeln.getFieldValue().length() > 1 && scrEbeln.getFieldValue().length() > 1)
		//	return new VtiUserExitResult(999, "Please select either a Purchase Order or Sales Order.");
		
		//Validate db tables created
		if (soHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (poHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		if (regLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (inspLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_INSPECT.");
		if (confLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (gpLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_GATEPASS.");

		if(scrGatePass.getFieldValue().length() != 0)
		{
			VtiExitLdbSelectCriterion [] gpvSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
								new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, scrGatePass.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup gpvSelCondGrp = new VtiExitLdbSelectConditionGroup(gpvSelConds, true);
				VtiExitLdbTableRow[] gpvLdbRows = gpLdbTable.getMatchingRows(gpvSelCondGrp);

				if(gpvLdbRows.length == 0)
				{
					Log.warn("Gatepass " + scrGatePass.getFieldValue() + " is not an SAP gatepass.");
					return new VtiUserExitResult(999, "This is not a valid Gate Pass, please validate.");
				}
		}
		
	
			VtiExitLdbSelectCriterion [] regSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "A"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
				VtiExitLdbTableRow[] regLdbRows = regLdbTable.getMatchingRows(regSelCondGrp);
		
				if(regLdbRows.length > 0)
				{
					for(int i = 0; i < regLdbRows.length;i++)
					{
						boolean bOrderCheck = false;
						
						//check for status and so order
						
						VtiExitLdbSelectCriterion [] statusExistSelConds = 
							{
								new VtiExitLdbSelectCondition("SERVERGRP",VtiExitLdbSelectCondition.EQ_OPERATOR,getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR,getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
							};
										
							VtiExitLdbSelectConditionGroup statusExistSelCondGrp = new VtiExitLdbSelectConditionGroup(statusExistSelConds, true);
							VtiExitLdbTableRow[] statusExistLdbRows = statusLdbTable.getMatchingRows(statusExistSelCondGrp);		
			
							if(statusExistLdbRows.length > 0)
							{
								for(int s = 0;s < statusExistLdbRows.length;s++)
								{
									if(statusExistLdbRows[s].getFieldValue("WGH_STATUS").equalsIgnoreCase("ASSIGNED")
										|| statusExistLdbRows[s].getFieldValue("WGH_STATUS").equalsIgnoreCase("WEIGH 1")
										|| statusExistLdbRows[s].getFieldValue("WGH_STATUS").equalsIgnoreCase("WEIGH 2")
										|| statusExistLdbRows[s].getFieldValue("WGH_STATUS").equalsIgnoreCase("REJECTED"))
										{
											bOrderCheck = true;
											break;
										}
								}
							}
							VtiExitLdbSelectCriterion [] soCheckSelConds = 
							{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
											new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, regLdbRows[i].getFieldValue("VBELN")),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
							};
      
							VtiExitLdbSelectConditionGroup soCeckSelCondGrp = new VtiExitLdbSelectConditionGroup(soCheckSelConds, true);
							VtiExitLdbTableRow[] soCheckLdbRows = soHeaderCLdbTable.getMatchingRows(soCeckSelCondGrp);		
		
							if(soCheckLdbRows.length > 0)
								if(soCheckLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("ASSIGNED")
								   || soCheckLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("WEIGH 1")
									|| soCheckLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("WEIGH 2")
								     || soCheckLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("REJECTED"))
										bOrderCheck = true;
							
							if(bOrderCheck)
							{
								break;
							}
							else if(!bOrderCheck)
							{
								regLdbRows[i].setFieldValue("DEL_IND","X");
								regLdbRows[i].setFieldValue("TIMESTAMP","");
								Log.warn("Archived the register for " + scrRegno.getFieldValue() + " with an assigned status but with no other records paired.");
								try
								{
									regLdbTable.saveRow(regLdbRows[i]);
									break;
								}
								catch( VtiExitException ee)
								{
									Log.error("Failed to archive assigned status register", ee);
								}
							}
					}
					
					regLdbRows = regLdbTable.getMatchingRows(regSelCondGrp);
					
					if(regLdbRows.length > 0)
						return new VtiUserExitResult(999,1, "Truck has an order assigned. Delete truck from system before new arrival is done.");
				}
				
				VtiExitLdbSelectCriterion [] regwSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "W"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup regwSelCondGrp = new VtiExitLdbSelectConditionGroup(regwSelConds, true);
				VtiExitLdbTableRow[] regwLdbRows = regLdbTable.getMatchingRows(regwSelCondGrp);
						

				if(regwLdbRows.length > 0)
				{
					for(int i = 0; i < regwLdbRows.length;i++)
					{
						boolean bOrderCheck = false;
						
						//check for status and so order
						
						VtiExitLdbSelectCriterion [] statusExistSelConds = 
							{
								new VtiExitLdbSelectCondition("SERVERGRP",VtiExitLdbSelectCondition.EQ_OPERATOR,getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR,getServerId()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
							};
										
							VtiExitLdbSelectConditionGroup statusExistSelCondGrp = new VtiExitLdbSelectConditionGroup(statusExistSelConds, true);
							VtiExitLdbTableRow[] statusExistLdbRows = statusLdbTable.getMatchingRows(statusExistSelCondGrp);		

							if(statusExistLdbRows.length > 0)
							{
								
								for(int s = 0;s < statusExistLdbRows.length;s++)
								{
									Log.trace(1,s + " being checked " + statusExistLdbRows[s].getFieldValue("WGH_STATUS"));
									if(statusExistLdbRows[s].getFieldValue("WGH_STATUS").equalsIgnoreCase("ASSIGNED")
										|| statusExistLdbRows[s].getFieldValue("WGH_STATUS").equalsIgnoreCase("WEIGH 1")
										|| statusExistLdbRows[s].getFieldValue("WGH_STATUS").equalsIgnoreCase("WEIGH 2")
										|| statusExistLdbRows[s].getFieldValue("WGH_STATUS").equalsIgnoreCase("REJECTED"))
										{
											
											bOrderCheck = true;Log.trace(1, "Found " + bOrderCheck);
											break;
										}
								}
							}
							VtiExitLdbSelectCriterion [] soCheckSelConds = 
							{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
											new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, regwLdbRows[i].getFieldValue("VBELN")),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
							};
      
							VtiExitLdbSelectConditionGroup soCeckSelCondGrp = new VtiExitLdbSelectConditionGroup(soCheckSelConds, true);
							VtiExitLdbTableRow[] soCheckLdbRows = soHeaderCLdbTable.getMatchingRows(soCeckSelCondGrp);		
		
							if(soCheckLdbRows.length > 0)
								if(soCheckLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("ASSIGNED")
								   || soCheckLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("WEIGH 1")
									|| soCheckLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("WEIGH 2")
								     || soCheckLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("REJECTED"))
										bOrderCheck = true;
							

							if(bOrderCheck)
							{
								break;
							}
							else if(!bOrderCheck)
							{
								regwLdbRows[i].setFieldValue("DEL_IND","X");
								regwLdbRows[i].setFieldValue("TIMESTAMP","");

								try
								{
									regLdbTable.saveRow(regwLdbRows[i]);
									break;
								}
								catch( VtiExitException ee)
								{
									Log.error("Failed to archive assigned status register", ee);
								}
							}
					}
					
					regwLdbRows = regLdbTable.getMatchingRows(regwSelCondGrp);
					
					if(regwLdbRows.length > 0)
						return new VtiUserExitResult(999,1, "Truck is in weigh process.");		
				}	
					
						
		
		if(scrEbeln.getFieldValue().length() > 0)
		{
			if(scrEbeln.getFieldValue().length() < 10)
				return new VtiUserExitResult(999,1, "Please assign a valid Purchase Order.");
			VtiExitLdbSelectCriterion [] poCMBSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrEbeln.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup poCMBSelCondGrp = new VtiExitLdbSelectConditionGroup(poCMBSelConds, true);
			VtiExitLdbTableRow[] poCMBLdbRows = poHeaderCLdbTable.getMatchingRows(poCMBSelCondGrp);
		
			//REDEV This need to be changed from hardcode to matching it in config.
			if(poCMBLdbRows.length == 0)
			{
				return new VtiUserExitResult(999,1, "This is not a valid Purchase Order.");
			}
			else
			{
				 boolean validPO = false;
				if(poCMBLdbRows[0].getFieldValue("BSART").equalsIgnoreCase("NB"))
					validPO = true;
				if(poCMBLdbRows[0].getFieldValue("BSART").equalsIgnoreCase("ZNB"))
				    validPO = true;
				if(poCMBLdbRows[0].getFieldValue("BSART").equalsIgnoreCase("ZRO"))
				{
				    validPO = true;
				}
				if(!validPO)
					return new VtiUserExitResult(999,1, "This is not a valid Purchase Order.");
			}
		}
		
		if(scrVbeln.getFieldValue().length() > 0)
		{
			if(scrVbeln.getFieldValue().length() < 10)
				return new VtiUserExitResult(999, "Please assign a valid Sales Inquiery.");
				
			VtiExitLdbSelectCriterion [] soCMBSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVbeln.getFieldValue()),
							new VtiExitLdbSelectCondition("AUART", VtiExitLdbSelectCondition.EQ_OPERATOR, "ZIN"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup soCMBSelCondGrp = new VtiExitLdbSelectConditionGroup(soCMBSelConds, true);
			VtiExitLdbTableRow[] soCMBLdbRows = soHeaderCLdbTable.getMatchingRows(soCMBSelCondGrp);		
		
			if(soCMBLdbRows.length > 0)
				return new VtiUserExitResult(999, "This is not a valid Sales Inquiery.");
		}
			

		try
		{
			refNo = getNextNumberFromNumberRange("YSWB_KEY");
		}
		catch(VtiExitException ee)
		{
			Log.error("Unable to create number from YSWB_KEY",ee);
			return new VtiUserExitResult(999,"Unable to retrieve a transaction number from number range YSWB_KEY");
		}
		VtiExitLdbSelectCriterion regBlankSelConds = new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, "");
		
		try
		{
			if(regLdbRows.length != 0)
				regLdbTable.deleteMatchingRows(regBlankSelConds);
		}
		catch(VtiExitException ee)
		{
			Log.warn("Null line record not terminated.", ee);
		}
		
		sErrorMsg = ArchiveOldRegistrations(scrRegno.getFieldValue());
				if(sErrorMsg.length() > 0)
				   return new VtiUserExitResult(999, sErrorMsg);
		  			
		//Check if inspection is still valid.
		VtiExitLdbSelectCriterion [] inspSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
//						new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
//							new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrEbeln.getFieldValue())
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
							
		VtiExitLdbSelectConditionGroup inspSelCondGrp = new VtiExitLdbSelectConditionGroup(inspSelConds, true);
		VtiExitLdbTableRow[] inspLdbRows = inspLdbTable.getMatchingRows(inspSelCondGrp);
			
		if(inspLdbRows.length > 0)
		   if(inspLdbRows[0].getFieldValue("ROTATE").equalsIgnoreCase("X"))
			   hasRotate = true;
			   
		if(scrEbeln.getFieldValue().length()>0 || hasRotate)
		{

			if(inspLdbRows.length > 0 )
				if(inspLdbRows[0].getFieldValue("STOCK").equalsIgnoreCase("X") 
				   || inspLdbRows[0].getFieldValue("SHIP").equalsIgnoreCase("X") 
				   || inspLdbRows[0].getFieldValue("ROTATE").equalsIgnoreCase("X"))
				{
					if(inspLdbRows[0].getFieldValue("EXPIREDATE").length() > 0)
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
						   && inspLdbRows[0].getFieldValue("EXPIRED").length() > 0 || chkExpired.getFieldValue().equalsIgnoreCase("X"))
						{
							if(chkExpired.getFieldValue().equalsIgnoreCase("X"))
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
							}
							bInspexpire = true;
							if(sbInspEndMsg.length() == 0)
								sbInspEndMsg.append("Truck inspection has expired, inform driver to do inspection again.");
						}
						else if(Long.parseLong(currLdbDate+currLdbTime) > Long.parseLong(sbExpireTs.toString()) 
								&& inspLdbRows[0].getFieldValue("EXPIRED").length() == 0 || chkExpired.getFieldValue().equalsIgnoreCase("X"))
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
							bInspexpire = true;
							sbInspEndMsg.append("Truck inspection has expired, inform driver to do inspection again.");
						}
					}
				}
			
			
			VtiExitLdbSelectCriterion [] statusExistSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP",VtiExitLdbSelectCondition.EQ_OPERATOR,getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR,getServerId()),
				new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
			//	new VtiExitLdbSelectCondition("EBELN",VtiExitLdbSelectCondition.EQ_OPERATOR,scrEbeln.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
						
			VtiExitLdbSelectConditionGroup statusExistSelCondGrp = new VtiExitLdbSelectConditionGroup(statusExistSelConds, true);
			VtiExitLdbTableRow[] statusExistLdbRows = statusLdbTable.getMatchingRows(statusExistSelCondGrp);		
			
			if(statusExistLdbRows.length > 0)
			{

				if(!sTruckType.equalsIgnoreCase ("SCRAP"));
				{
					sErrorMsg = PreferedTrucks(scrRegno.getFieldValue(), scrWDate.getFieldValue(), scrTime.getFieldValue(), scrEbeln.getFieldValue(), scrDriver.getFieldValue(), refNo);
					if(sErrorMsg.length() > 0 && !sErrorMsg.equalsIgnoreCase("true"))
						return new VtiUserExitResult(999, sErrorMsg);
					
					if(sErrorMsg.equalsIgnoreCase("true"))
					{
						prefd = true;
						sErrorMsg = "";
					}
					else
						prefd = false;
					
					if(!bInspexpire)
					{
						VtiExitLdbSelectCriterion [] truckInspSelConds = 
						{			
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
											new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
								  
						VtiExitLdbSelectConditionGroup truckInspSelCondGrp = new VtiExitLdbSelectConditionGroup(truckInspSelConds, true);
		
						VtiExitLdbOrderSpecification [] orderBy = 
							{
								new VtiExitLdbOrderSpecification("AUTIM",false),
							};
						VtiExitLdbTableRow[] truckInspLdbRows = inspLdbTable.getMatchingRows(truckInspSelCondGrp,orderBy);
					
	
						if(truckInspLdbRows.length > 0)
						{
Log.trace(0,"sTruckType is " + sTruckType);
							if(truckInspLdbRows[0].getFieldValue("SHIP").equalsIgnoreCase("X"))
							{
								sErrorMsg = ShippingTrucks(scrRegno.getFieldValue(), scrWDate.getFieldValue(), scrTime.getFieldValue(), scrEbeln.getFieldValue(), scrDriver.getFieldValue(), refNo);
							}
								if(sErrorMsg.length() > 0)		
									return new VtiUserExitResult(999, sErrorMsg);
								else
									bExtend = true;
				
							if(sTruckType.equalsIgnoreCase ("STOCK"))
							{
								sErrorMsg = StockPileTrucks(scrRegno.getFieldValue(), scrWDate.getFieldValue(), scrTime.getFieldValue(), scrEbeln.getFieldValue(), scrDriver.getFieldValue(),refNo);
							}
								if(sErrorMsg.length() > 0)		
									return new VtiUserExitResult(999, sErrorMsg);
								else
									bExtend = true;
				
							if(sTruckType.equalsIgnoreCase ("ROTATE") || sTruckType.equalsIgnoreCase ("ROTATE OW") )
							{
								sErrorMsg = RotatingTrucks(scrRegno.getFieldValue(), scrWDate.getFieldValue(), scrTime.getFieldValue(), scrEbeln.getFieldValue(), scrDriver.getFieldValue(), refNo);
							}
								if(sErrorMsg.length() > 0)		
									return new VtiUserExitResult(999, sErrorMsg);	
								else
									bExtend = true;
						}
					}
			
					VtiExitLdbSelectCriterion [] statusSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP",VtiExitLdbSelectCondition.EQ_OPERATOR,getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR,getServerId()),
						new VtiExitLdbSelectCondition("INSP_VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, inspLdbRows[0].getFieldValue("VTI_REF")),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegno.getFieldValue()),
				//		new VtiExitLdbSelectCondition("DOCTYPE",VtiExitLdbSelectCondition.EQ_OPERATOR,"NB"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
							
					VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
					VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);		

					if(statusLdbRows.length > 0)
					{
							
						if(	statusLdbRows[0].getFieldValue("PREFERED").equalsIgnoreCase("X"))
						{
							//prefd = true;
							sTruckType = "NORMAL";
						}
						if(statusLdbRows[0].getFieldValue("SHIP").equalsIgnoreCase("X"))
						{
							
							sTruckType = "SHIP";
							ship = true;
						}
						if(statusLdbRows[0].getFieldValue("STOCK").equalsIgnoreCase("X"))
						{
								
							sTruckType = "STOCK";
							stock = true;
						}
						if(statusLdbRows[0].getFieldValue("ROTATE").equalsIgnoreCase("X"))
						{
								
							sTruckType = "ROTATE";
							rotate = true;
						}
					}
				}
			}
		}
		
		
		VtiExitLdbTableRow ldbRowReg = regLdbTable.newRow();
			
		//Populate TBL Fields
		ldbRowReg.setFieldValue("SERVERID", getServerId());
		ldbRowReg.setFieldValue("SERVERGRP", getServerGroup());
//		ldbRowReg.setFieldValue("CONTRACTOR", scrContractor.getFieldValue());
//		ldbRowReg.setFieldValue("SELF", scrSelf.getFieldValue());
		ldbRowReg.setFieldValue("TRUCKREG", scrRegno.getFieldValue());
		ldbRowReg.setFieldValue("TRAILREG", scrTrailer.getFieldValue());
		ldbRowReg.setFieldValue("COMPANY", scrCompany.getFieldValue());
		ldbRowReg.setFieldValue("DRIVER", scrDriver.getFieldValue());
		ldbRowReg.setFieldValue("LICENSENO", scrLicNo.getFieldValue());//Require Table FieldName
		ldbRowReg.setFieldValue("IDNUMBER", scrIDNo.getFieldValue());
		ldbRowReg.setFieldValue("TELNO", scrTelNo.getFieldValue());//Require Table FieldName
		ldbRowReg.setFieldValue("TRANSTYPE", scrTransType.getFieldValue());
		ldbRowReg.setFieldValue("NOAXELS", scrNoAxels.getFieldValue());
		ldbRowReg.setFieldValue("MAXWEIGHT", scrMaxWeight.getDoubleFieldValue());		
		ldbRowReg.setFieldValue("VBELN", scrVbeln.getFieldValue());
		ldbRowReg.setFieldValue("EBELN", scrEbeln.getFieldValue());
		ldbRowReg.setFieldValue("AUDAT", currLdbDate);
		ldbRowReg.setFieldValue("AUTIM", currLdbTime);
		ldbRowReg.setFieldValue("ORD_NUM", scrEbeln.getFieldValue());
		
		//if(scrScrap.getFieldValue().equalsIgnoreCase("X"))
		ldbRowReg.setFieldValue("TRUCKTYPE", scrTruckType.getFieldValue());
		ldbRowReg.setFieldValue("VTI_REF", refNo);
		ldbRowReg.setFieldValue("TIMESTAMP", scrTimestamp.getFieldValue());

		if((prefd || ship || rotate || stock) && (!inspLdbRows[0].getFieldValue("EXPIRED").equalsIgnoreCase("X") && !chkExpired.getFieldValue().equalsIgnoreCase("X")))
		{
			ldbRowReg.setFieldValue("INSPSTATUS", "A");
			ldbRowReg.setFieldValue("ASSTIME",currLdbTime);
		}
		else
		{
			ldbRowReg.setFieldValue("INSPSTATUS", "R");
		}
		ldbRowReg.setFieldValue("DOC_NMBER", scrDocNum.getFieldValue());
		ldbRowReg.setFieldValue("GATE_PASS", scrGatePass.getFieldValue());
		ldbRowReg.setFieldValue("USERID", scrUserID.getFieldValue());
		ldbRowReg.setFieldValue("TRUCKTYPE",scrTruckType.getFieldValue());
		
		try
		{
			regLdbTable.saveRow(ldbRowReg);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to Save data to the Registration table.",ee);
			return new VtiUserExitResult(999,"Unable to Save data to the Registration table, please try again.");
		}
		
		if(scrGatePass.getFieldValue().length() != 0)
		{
			VtiExitLdbSelectCriterion [] gpSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
								new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, scrGatePass.getFieldValue()),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup gpSelCondGrp = new VtiExitLdbSelectConditionGroup(gpSelConds, true);
				VtiExitLdbTableRow[] gpLdbRows = gpLdbTable.getMatchingRows(gpSelCondGrp);

				if(gpLdbRows.length == 0)
				{
					
					errorMsg = "This gatepass is not an SAP gatepass.";
					return new VtiUserExitResult(999,"Gatepass " + scrGatePass.getFieldValue() + " is not an SAP gatepass.");
				}
				
				if(gpLdbRows.length > 0)
				{
					gpLdbRows[0].setFieldValue("STATUS","R");
					gpLdbRows[0].setFieldValue("VTIREF",refNo);
					gpLdbRows[0].setFieldValue("TIMESTAMP","");
							
					try
					{
						gpLdbTable.saveRow(gpLdbRows[0]);
					}
					catch(VtiExitException ee)
					{
						Log.error(refNo + "<:>" + gpLdbRows[0].getFieldValue("PASS_NUMB") + " scr val > " + scrGatePass.getFieldValue(), ee);
						return new VtiUserExitResult(999,"Failed to save status to Gate Pass. Gatepass assigned to registration.");
					}
				}
				else
				{
					Log.trace(0,refNo + "<VTI:GP>" + scrGatePass.getFieldValue());
				}
		}
		
		
		try
		{					
		// Trigger the uploads to SAP, if a connection is available.
				String hostName = getHostInterfaceName();
				boolean hostConnected = isHostInterfaceConnected(hostName);

				if (hostConnected)
				{ 
					dbCall.ldbUpload("YSWB_REGISTER", this);
					dbCall.ldbUpload("YSWB_STATUS", this);
					dbCall.ldbUpload("YSWB_GATEPASS", this);
				}
				else
				{
					Log.error("Host not connected to SAP to upload register data during Arrival save, check server.");
				}
					
		}
		catch (VtiExitException ee)
		{
				Log.error("Host not connected to SAP to upload register data during Arrival save, check server.");
		}
		
		String reg = scrRegno.getFieldValue();
				
		scrRegno.setFieldValue("");
		scrSRegno.setFieldValue("");
		scrTrailer.setFieldValue("");
		scrTime.setFieldValue(currTime);
		scrDate.setFieldValue("");
		scrCompany.setFieldValue("");
		scrDriver.setFieldValue("");
		scrWDriver.setFieldValue("");
		scrIDNo.setFieldValue("");
		scrTransType.setFieldValue("");
		scrTruckType.setFieldValue("NORMAL");
		scrNoAxels.setFieldValue("");
		scrMaxWeight.setFieldValue("");
//		scrSelf.setFieldValue("");
	//	scrContractor.setFieldValue("");
		scrDocNum.setFieldValue("");
		scrTelNo.setFieldValue("");
		scrLicNo.setFieldValue("");
		scrVbeln.setFieldValue("");
		scrWEbeln.setFieldValue("");
		scrWVbeln.setFieldValue("");
		scrEbeln.setFieldValue("");
		scrGatePass.setFieldValue("");
		rdoNormal.setFieldValue("X");
		rdoStock.setFieldValue("");
		rdoScrap.setFieldValue("");
		rdoRotate.setFieldValue("");
		rdoRotateOs.setFieldValue("");
		chkExpired.setFieldValue("");
	    
		if(sbInspEndMsg.length() > 0)
		{
			sbInspEndMsg.append("Saved registration as " + reg + " with reference " +refNo + ".");
			return new VtiUserExitResult(000,1,sbInspEndMsg.toString());
		}
		else
			return new VtiUserExitResult(000,1,"Saved registration as " + reg + " with reference " +refNo + "." + errorMsg);
	}		
/*Prepping PREFD trucks
	@@@@@@@@@ internal method @@@@@@@@@@@
*/	
	private String PreferedTrucks(String sRegNo, String sDate, String sTime, String sEbeln, String sDriver, long regVti) throws VtiExitException
	{
		Date currNow = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String errorMsg = "";
		long interval = 0;
		long inspRefNo = 0;
		String inspDate = "";
		String currShift = "currShift";
		String inspShift = "inspShift";
		int inspTime = 0;
		String shift = "";
		int shiftStart = 0;
		int shiftEnd = 0;
		String dbShift = "";
		String sErrorMsg = "";
		long refNo = 0;
		String sTruckType = "Normal";
		
		boolean prefd = false;
				
		//Database TBL Declaration
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");

		//Validate db tables created
		if (regLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_REGISTER.";
		if (statusLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_STATUS.";
		if (inspLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_INSPECT.";
		if (confLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_CONFIG.";
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return sErrorMsg = "Error Retrieving Session Header Info";

		//Get preffered inspections done for this truck
		VtiExitLdbSelectCriterion [] prefInspSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
			new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
			new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
				//new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, sDate),
					new VtiExitLdbSelectCondition("PREF", VtiExitLdbSelectCondition.EQ_OPERATOR, "X"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "P"),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
		  
		VtiExitLdbSelectConditionGroup prefInspSelCondGrp = new VtiExitLdbSelectConditionGroup(prefInspSelConds, true);
		
		VtiExitLdbTableRow[] prefInspLdbRows = inspLdbTable.getMatchingRows(prefInspSelCondGrp);

		if(prefInspLdbRows.length != 0 && sEbeln.length() != 0)
		{
			
			inspRefNo = prefInspLdbRows[0].getLongFieldValue("VTI_REF");
			inspDate = prefInspLdbRows[0].getFieldValue("AUDAT");
			inspTime = prefInspLdbRows[0].getIntegerFieldValue("AUTIM");
					
				
			//Get qty of shifts
			VtiExitLdbSelectCriterion [] shiftQTYSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "SHIFT"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
					
			VtiExitLdbSelectConditionGroup shiftQTYSelCondGrp = new VtiExitLdbSelectConditionGroup(shiftQTYSelConds, true);
			VtiExitLdbTableRow[] shiftQTYLdbRows = confLdbTable.getMatchingRows(shiftQTYSelCondGrp);
			
			if(shiftQTYLdbRows.length == 0)
				return sErrorMsg = "Operational Shifts not maintained in the Config Table.";
					
			
				for(int s = 0;s < shiftQTYLdbRows.length;s++)
				{
					inspShift = "";
					shift = "SHIFT"+(s+1);
					
					VtiExitLdbSelectCriterion [] shiftSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "SHIFT"),
									new VtiExitLdbSelectCondition("KEYVAL1", VtiExitLdbSelectCondition.EQ_OPERATOR, shift),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup shiftSelCondGrp = new VtiExitLdbSelectConditionGroup(shiftSelConds, true);
					VtiExitLdbTableRow[] shiftLdbRows = confLdbTable.getMatchingRows(shiftSelCondGrp);
					
					//Determine shift and if shift is with in the parameters of the operational shifts as defined in the config table
					shiftStart = shiftLdbRows[0].getIntegerFieldValue("KEYVAL2");
					shiftEnd = shiftLdbRows[0].getIntegerFieldValue("KEYVAL3");
					dbShift = shiftLdbRows[0].getFieldValue("KEYVAL1");
					Integer t = new Integer(currLdbTime);
										
					int currIntTime = t.intValue();
					
					
					if(shiftStart < shiftEnd || currIntTime >= shiftStart)
					{
						if(shiftStart < shiftEnd)
						if(currIntTime >= shiftStart && currIntTime < shiftEnd)
						{
							currShift = shiftLdbRows[0].getFieldValue("KEYVAL1");
						}
						
						if(shiftStart > shiftEnd)
						if(currIntTime >= shiftStart)
						{
							currShift = shiftLdbRows[0].getFieldValue("KEYVAL1");
						}
					}
					else if(currIntTime < shiftEnd )
					{
						currShift = shiftLdbRows[0].getFieldValue("KEYVAL1");
					}
						
							
					//boolean a,b;
					if(shiftStart < shiftEnd || inspTime >= shiftStart)
					{
						if(shiftStart < shiftEnd)
						if(inspTime >= shiftStart && inspTime < shiftEnd)
						{
							inspShift = shiftLdbRows[0].getFieldValue("KEYVAL1");
						}
						
						if(shiftStart > shiftEnd)
						if(inspTime >= shiftStart)
						{
							inspShift = shiftLdbRows[0].getFieldValue("KEYVAL1");
						}
					}
					else if(inspTime < shiftEnd )
					{
						inspShift = shiftLdbRows[0].getFieldValue("KEYVAL1");
					}
					
						
					if(inspShift == currShift)
					{
						prefd = true;
						sErrorMsg = "true";
					}
					
				}
		}
		
		//Fill status table with the inspection done included in the record
		if(prefd)
		{
			VtiExitLdbTableRow poStatus = statusLdbTable.newRow();
					
			poStatus.setFieldValue("SERVERGRP",getServerGroup());
			poStatus.setFieldValue("SERVERID",getServerId());
			poStatus.setFieldValue("VTIREF", regVti);
			poStatus.setFieldValue("EBELN",sEbeln);
			poStatus.setFieldValue("STATUS","A");
			poStatus.setFieldValue("WGH_STATUS","ASSIGNED");
			poStatus.setFieldValue("ARR_DATE",sDate);
			poStatus.setFieldValue("ARR_TIME",sTime);
			poStatus.setFieldValue("TRUCKREG",sRegNo);
			poStatus.setFieldValue("PREFERED","X");
			poStatus.setFieldValue("INSP_VTI_REF",inspRefNo);
			poStatus.setFieldValue("INSP_DATE",inspDate);
			poStatus.setFieldValue("INSP_TIME",inspTime);
		//	poStatus.setFieldValue("USERID",sessionHeader.getUserId());
			poStatus.setFieldValue("DOCTYPE","NB");
			poStatus.setFieldValue("TIMESTAMP","");
			try
			{
				statusLdbTable.saveRow(poStatus);
			}
			catch(VtiExitException ee)
			{
				Log.error("Status save failed.",ee);
				return sErrorMsg = "Failed to add the truck to the Status table.Tracking not possible anymore.";
			}
					

			//Add truck to the queue (on the inbound line)
			GetQ addInQ = new GetQ(this, sEbeln,sRegNo);
			String queue = addInQ.getTruckQ();
								
			if(queue.length() == 0)
				return sErrorMsg = "Q#: " + queue + " truck: " + sRegNo + " Ebeln: " + sEbeln + " time: " + sTime+ " date: " + sDate;
				
			try
			{
				interval = getNextNumberFromNumberRange("YSWB_QPOS");
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating next queue no.",ee);
				return sErrorMsg  ="Unable to generate next queue pos no.";
			}
					
			AddToQ qTruck = new AddToQ(this,sRegNo , sEbeln
										,false, queue, "RAW",currLdbTime,currLdbDate
										,interval, sDriver);
			qTruck.addTruck2Q();
		}
		
		return sErrorMsg;
	}

/*Prepping shipping trucks
@@@@@@@@@ internal method @@@@@@@@@@@
*/	
	
	//shipping trucks 
	private String ShippingTrucks(String sRegNo, String sDate, String sTime, String sEbeln, String sDriver, long refNo) throws VtiExitException
	{
		Date currNow = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		long inspRefNo = 0;
		String inspDate = "";
		String inspExDate = "";
		String inspExTime = "";
		int inspTime = 0;
		String sErrorMsg = "";
		String sTruckType = "SHIP";
	
		//Database TBL Declaration
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");

		//Validate db tables created
		if (regLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_REGISTER.";
		if (statusLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_STATUS.";
		if (inspLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_INSPECT.";
		if (confLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_CONFIG.";
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return sErrorMsg = "Error Retrieving Session Header Info";
	
		VtiExitLdbSelectCriterion [] shipInspSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
			new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
			new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
				//new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, sDate),
					new VtiExitLdbSelectCondition("SHIP", VtiExitLdbSelectCondition.EQ_OPERATOR, "X"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "W"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "A"),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
		  
		VtiExitLdbSelectConditionGroup shipInspSelCondGrp = new VtiExitLdbSelectConditionGroup(shipInspSelConds, true);
		
		VtiExitLdbOrderSpecification [] orderShipBy = 
			{
				new VtiExitLdbOrderSpecification("AUTIM",false),
			};
		VtiExitLdbTableRow[] shipInspLdbRows = inspLdbTable.getMatchingRows(shipInspSelCondGrp,orderShipBy);
		
		if(shipInspLdbRows.length != 0 && sEbeln.length() != 0)
		 {
			
				inspRefNo = shipInspLdbRows[0].getLongFieldValue("VTI_REF");	
				inspDate = shipInspLdbRows[0].getFieldValue("AUDAT");			
				inspTime = shipInspLdbRows[0].getIntegerFieldValue("AUTIM");	
				inspExDate = shipInspLdbRows[0].getFieldValue("EXPIREDATE");	
				inspExTime = shipInspLdbRows[0].getFieldValue("EXPIRETIME");	
				
				
				
				
				long iCurDateTime = Long.parseLong(sDate + sTime);				
				long iExpDateTime = 0L;
				
				if(shipInspLdbRows[0].getFieldValue("EXPIRETIME").length() < 6)
					iExpDateTime = Long.parseLong(shipInspLdbRows[0].getFieldValue("EXPIREDATE") + "0" + shipInspLdbRows[0].getFieldValue("EXPIRETIME"));
				else
					iExpDateTime = Long.parseLong(shipInspLdbRows[0].getFieldValue("EXPIREDATE") + shipInspLdbRows[0].getFieldValue("EXPIRETIME"));

				
				if(iCurDateTime <=  iExpDateTime)
				{
					sErrorMsg = ExtendInspectionValidity(true,true,false,false, sEbeln,sDate,sTime,sRegNo,sDriver,refNo,inspRefNo);
					if(sErrorMsg.length() > 0)
						return sErrorMsg;
				}
		}
		sTruckType = "Ship";
		return sErrorMsg;
	}
/*Prepping Stockpile trucks
	@@@@@@@@@ internal method @@@@@@@@@@@
*/		
	private String StockPileTrucks(String sRegNo, String sDate, String sTime, String sEbeln, String sDriver, long refNo) throws VtiExitException
	{
		Date currNow = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		long inspRefNo = 0;
		String inspDate = "";
		String inspExDate = "";
		String inspExTime = "";
		int inspTime = 0;
		String sErrorMsg = "";
		String sTruckType = "STOCK";	
		
		//stock pile
		
		//Database TBL Declaration
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");

		//Validate db tables created
		if (regLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_REGISTER.";
		if (statusLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_STATUS.";
		if (inspLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_INSPECT.";
		if (confLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_CONFIG.";
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return sErrorMsg = "Error Retrieving Session Header Info";
		
		VtiExitLdbSelectCriterion [] stockInspSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
			new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
			new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
				//new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, sDate),
					//new VtiExitLdbSelectCondition("STOCK", VtiExitLdbSelectCondition.EQ_OPERATOR, "X"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "W"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "A"),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
		  
		VtiExitLdbSelectConditionGroup stockInspSelCondGrp = new VtiExitLdbSelectConditionGroup(stockInspSelConds, true);
		
		VtiExitLdbOrderSpecification [] orderstockBy = 
			{
				new VtiExitLdbOrderSpecification("AUTIM",false),
			};
		VtiExitLdbTableRow[] stockInspLdbRows = inspLdbTable.getMatchingRows(stockInspSelCondGrp,orderstockBy);
		
		if(stockInspLdbRows.length != 0 && sEbeln.length() != 0)
		 {
			
				inspRefNo = stockInspLdbRows[0].getLongFieldValue("VTI_REF");
				inspDate = stockInspLdbRows[0].getFieldValue("AUDAT");
				inspTime = stockInspLdbRows[0].getIntegerFieldValue("AUTIM");
				inspExDate = stockInspLdbRows[0].getFieldValue("EXPIREDATE");
				inspExTime = stockInspLdbRows[0].getFieldValue("EXPIRETIME");
				
				long iCurDateTime = Long.parseLong(sDate + sTime);				
				long iExpDateTime = 0L;
				
				if(stockInspLdbRows[0].getFieldValue("EXPIRETIME").length() < 6)
					iExpDateTime = Long.parseLong(stockInspLdbRows[0].getFieldValue("EXPIREDATE") + "0" + stockInspLdbRows[0].getFieldValue("EXPIRETIME"));
				else
					iExpDateTime = Long.parseLong(stockInspLdbRows[0].getFieldValue("EXPIREDATE") + stockInspLdbRows[0].getFieldValue("EXPIRETIME"));
				
				if(iCurDateTime <=  iExpDateTime)
				{
					sErrorMsg = ExtendInspectionValidity(true,false,true,false, sEbeln,sDate,sTime,sRegNo,sDriver,refNo, inspRefNo);
					if(sErrorMsg.length() > 0)
						return sErrorMsg;
				}
				
		}
		sTruckType = "Stock";
		return sErrorMsg;
	}
/*Prepping Rotate trucks
	@@@@@@@@@ internal method @@@@@@@@@@@
*/	
	
	private String RotatingTrucks(String sRegNo, String sDate, String sTime, String sEbeln, String sDriver, long refNo) throws VtiExitException
	{
		Date currNow = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		long inspRefNo = 0;
		String inspDate = "";
		String inspExDate = "";
		String inspExTime = "";
		int inspTime = 0;
		String sErrorMsg = "";
		String sTruckType = "ROTATE";	
		
		//Rotating
		
		//Database TBL Declaration
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");

		//Validate db tables created
		if (regLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_REGISTER.";
		if (statusLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_STATUS.";
		if (inspLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_INSPECT.";
		if (confLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_CONFIG.";
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return sErrorMsg = "Error Retrieving Session Header Info";
		
				//rotate 
		VtiExitLdbSelectCriterion [] rotateInspSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
			new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
			new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
			//	new VtiExitLdbSelectCondition("ROTATE", VtiExitLdbSelectCondition.EQ_OPERATOR, "X"),
					new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
						new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
		  
		VtiExitLdbSelectConditionGroup rotateInspSelCondGrp = new VtiExitLdbSelectConditionGroup(rotateInspSelConds, true);
		
		VtiExitLdbOrderSpecification [] orderrotateBy = 
			{
				new VtiExitLdbOrderSpecification("AUTIM",false),
			};
		VtiExitLdbTableRow[] rotateInspLdbRows = inspLdbTable.getMatchingRows(rotateInspSelCondGrp,orderrotateBy);
		
		if(rotateInspLdbRows.length != 0)
		 {
			
				inspRefNo = rotateInspLdbRows[0].getLongFieldValue("VTI_REF");
				inspDate = rotateInspLdbRows[0].getFieldValue("AUDAT");
				inspTime = rotateInspLdbRows[0].getIntegerFieldValue("AUTIM");
				inspExDate = rotateInspLdbRows[0].getFieldValue("EXPIREDATE");
				inspExTime = rotateInspLdbRows[0].getFieldValue("EXTIME");
				
				StringBuffer sbExpireTs = new StringBuffer();
				sbExpireTs.append(rotateInspLdbRows[0].getFieldValue("EXPIREDATE"));
				if(rotateInspLdbRows[0].getFieldValue("EXPIRETIME").length() == 6)
					sbExpireTs.append(rotateInspLdbRows[0].getFieldValue("EXPIRETIME"));
				else
				{
					sbExpireTs.append("0");
					sbExpireTs.append(rotateInspLdbRows[0].getFieldValue("EXPIRETIME"));
				}
						
				long iCurDateTime = Long.parseLong(sDate + sTime);
				long iExpDateTime = Long.parseLong(sbExpireTs.toString());				

				if(iCurDateTime <= iExpDateTime)
				{
	
					sErrorMsg = ExtendInspectionValidity(true,false,false,true, sEbeln,sDate,sTime,sRegNo,sDriver, refNo, inspRefNo);
					if(sErrorMsg.length() > 0)
						return sErrorMsg;
				}
		}
		sTruckType = "Rotate";
		return sErrorMsg;
	}
	
/*Extending registration inspection validity
	@@@@@@@@@ internal method @@@@@@@@@@@
*/	
	private String ExtendInspectionValidity (boolean hasOrder,boolean ship,boolean stock,boolean rotate, 
											 String sEbeln, String sDate, String sTime, String sRegNo, String sDriver, 
											 long refNo, long inspRefNo) throws VtiExitException
	{
		String inspDate = "";
		String inspExDate = "";
		String inspExTime = "";
		long interval = 0;
		int inspTime = 0;
		String sErrorMsg = "";
		Date currNow = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
			
		//Database TBL Declaration

		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable poHeaderLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		
		//Validate db tables created

		if (statusLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_STATUS.";
		if (inspLdbTable == null) sErrorMsg = "Unable to initialise table YSWB_INSPECT.";
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return sErrorMsg = "Error Retrieving Session Header Info";

Log.trace(0,"Stock switch is " + stock);
Log.trace(0,"Rotate switch is " + rotate);


		if(rotate || stock)
		{
Log.trace(0,"Is either Rotate or Stock");

				VtiExitLdbSelectCriterion [] exInspSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
				  
				VtiExitLdbSelectConditionGroup exInspSelCondGrp = new VtiExitLdbSelectConditionGroup(exInspSelConds, true);
		
				VtiExitLdbOrderSpecification [] orderBy = 
					{
						new VtiExitLdbOrderSpecification("AUTIM",false),
					};
				VtiExitLdbTableRow[] exInspLdbRows = inspLdbTable.getMatchingRows(exInspSelCondGrp,orderBy);
				
Log.trace(0,"Inspection has been found for extension change.");

				if(exInspLdbRows.length != 0)
				{
					if(rotate)
					{
Log.trace(0,"Rotate switch set to " + rotate);
						exInspLdbRows[0].setFieldValue("STOCK","");
						exInspLdbRows[0].setFieldValue("ROTATE","X");
						exInspLdbRows[0].setFieldValue("TIMESTAMP","");
					}
					
					if(stock)
					{
Log.trace(0,"Stock switch set to " + stock);
						exInspLdbRows[0].setFieldValue("STOCK","X");
						exInspLdbRows[0].setFieldValue("ROTATE","");
						exInspLdbRows[0].setFieldValue("TIMESTAMP","");
					}
					
					
					try
					{
Log.trace(0,"Saved extension of inspection.");
						inspLdbTable.saveRow(exInspLdbRows[0]);
					}
					catch(VtiExitException ee)
					{
						Log.error("Inspect update save failed.",ee);
						return sErrorMsg = "Failed to update the inspection with the rotate/stock detail.";
					}
					
				}	
		}
		
		
		VtiExitLdbSelectCriterion [] poCMBSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, sEbeln),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup poCMBSelCondGrp = new VtiExitLdbSelectConditionGroup(poCMBSelConds, true);
		VtiExitLdbTableRow[] poLdbRows = poHeaderLdbTable.getMatchingRows(poCMBSelCondGrp);
		//Fill status table with the inspection done included in the record
			VtiExitLdbTableRow poStatus = statusLdbTable.newRow();
			
			poStatus.setFieldValue("SERVERGRP",getServerGroup());
			poStatus.setFieldValue("SERVERID",getServerId());
			poStatus.setFieldValue("VTIREF", refNo);
			//if(hasOrder)
			poStatus.setFieldValue("EBELN",sEbeln);
			poStatus.setFieldValue("STATUS","A");
			poStatus.setFieldValue("WGH_STATUS","ASSIGNED");
			poStatus.setFieldValue("ARR_DATE",sDate);
			poStatus.setFieldValue("ARR_TIME",sTime);
			poStatus.setFieldValue("TRUCKREG",sRegNo);
			if(ship)
				poStatus.setFieldValue("SHIP","X");
			if(stock)
				poStatus.setFieldValue("STOCK","X");
			if(rotate)
				poStatus.setFieldValue("ROTATE","X");
			poStatus.setFieldValue("INSP_VTI_REF",inspRefNo);
			poStatus.setFieldValue("INSP_DATE",inspDate);
			poStatus.setFieldValue("INSP_TIME",inspTime);
			//poStatus.setFieldValue("USERID",sessionHeader.getUserId());
			
			if(poLdbRows.length > 0)
			{
				poStatus.setFieldValue("DOCTYPE",poLdbRows[0].getFieldValue("BSART"));
			}
			else
				poStatus.setFieldValue("DOCTYPE","NB");
			
			poStatus.setFieldValue("TIMESTAMP","");
			
			try
			{
				statusLdbTable.saveRow(poStatus);
			}
			catch(VtiExitException ee)
			{
				Log.error("Status save failed.",ee);
				return sErrorMsg = "Failed to add the truck to the Status table.Tracking not possible anymore.";
			}
					

			//Add truck to the queue (on the inbound line)
			//if(poLdbRows.length > 0 && sEbeln.length() > 9 || )
			//{

				if(sEbeln.length() == 0)
					sEbeln  = "0";
			    GetQ addInQ = new GetQ(this, sEbeln,sRegNo);
				String queue = addInQ.getTruckQ();
									
				if(queue.length() == 0)
					return sErrorMsg = "Q#: " + queue + " truck: " + sRegNo + " Ebeln: " + sEbeln + " time: " + sTime+ " date: " + sDate;
					
				try
				{
					interval = getNextNumberFromNumberRange("YSWB_QPOS");
				}
				catch(VtiExitException ee)
				{
					Log.error("Error creating next queue no.",ee);
					return sErrorMsg  ="Unable to generate next queue pos no.";
				}
						
				AddToQ qTruck = new AddToQ(this,sRegNo , sEbeln
											,false, queue, "RAW",currLdbTime,currLdbDate
											,interval, sDriver);
					
				qTruck.addTruck2Q();
			//}
		
		return sErrorMsg;
	}
		
/*Archiving old registration records
	@@@@@@@@@ internal method @@@@@@@@@@@
*/
	 
	private String ArchiveOldRegistrations(String sRegNo) throws VtiExitException
	{
		String sErrorMsg = "";

		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable gpLdbTable = getLocalDatabaseTable("YSWB_GATEPASS");
		
				//Validate db tables created
		if (regLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_REGISTER.";

		//Archive old registrations with status R,P,F,O
					
		VtiExitLdbSelectCriterion [] regArcSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
							new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "A"),
							new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "W"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup regArcSelCondGrp = new VtiExitLdbSelectConditionGroup(regArcSelConds, true);
		VtiExitLdbTableRow[] regArcLdbRows = regLdbTable.getMatchingRows(regArcSelCondGrp);
				
		if(regArcLdbRows.length > 0)
		{
			for(int iA =0; iA < regArcLdbRows.length;iA++)
			{
				if(regArcLdbRows[iA].getFieldValue("DEPTIME").length() > 1)
					regArcLdbRows[iA].setFieldValue("INSPSTATUS","C");
				
				regArcLdbRows[iA].setFieldValue("DEL_IND","X");
				regArcLdbRows[iA].setFieldValue("TIMESTAMP","");
				
				if(regArcLdbRows[iA].getFieldValue("INSPSTATUS").equalsIgnoreCase("C") 
					&& regArcLdbRows[iA].getFieldValue("GATE_PASS").length()>0)
					{
						VtiExitLdbSelectCriterion [] gpvSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, regArcLdbRows[iA].getFieldValue("GATE_PASS")),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
      
						VtiExitLdbSelectConditionGroup gpvSelCondGrp = new VtiExitLdbSelectConditionGroup(gpvSelConds, true);
						VtiExitLdbTableRow[] gpvLdbRows = gpLdbTable.getMatchingRows(gpvSelCondGrp);

						if(gpvLdbRows.length > 0)
						{
							gpvLdbRows[0].setFieldValue("DEL_IND","X");
							gpvLdbRows[0].setFieldValue("TIMESTAMP","");
							
							try
							{
								gpLdbTable.saveRow(gpvLdbRows[0]);
							}
							catch(VtiExitException ee)
							{
							}
						}
					}
				try
				{
					regLdbTable.saveRow(regArcLdbRows[iA]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Registration not archived.", ee);
				}
			}
		}
		return sErrorMsg;
	}
}