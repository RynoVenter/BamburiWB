package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FillPickingListRSO extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
//Initialising the screenfields to be used
		VtiUserExitScreenField scrSoNo = getScreenField("IS_SO");

		VtiUserExitScreenField scrTruckReg = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrCMtart = getScreenField("C_MTART");
		VtiUserExitScreenField scrPQty = getScreenField("P_QTY");
		VtiUserExitScreenField scrWQty = getScreenField("W_QTY");
		VtiUserExitScreenField scrRQty = getScreenField("R_QTY");
		VtiUserExitScreenField btnSave = getScreenField("BT_SAVE");
		VtiUserExitScreenField scrRefDoc = getScreenField("REFDOC");
		
		VtiUserExitScreenTable tblItems = getScreenTable("TB_ITEMS");
		VtiUserExitScreenTable tblOrders = getScreenTable("TB_ORDERS");
		
		btnSave.setHiddenFlag(false);
		
//Validating the instantiation of the screenfields
		if(scrSoNo == null) return new VtiUserExitResult(999,"Failed to initialise IS_SO");

		if(scrTruckReg == null) return new VtiUserExitResult(999,"Failed to initialise TRUCKREG");
		if(scrVRef == null) return new VtiUserExitResult(999,"Failed to initialise VTI_REF");
		if(scrCMtart == null) return new VtiUserExitResult(999,"Failed to initialise C_MTART");

		if(btnSave == null) return new VtiUserExitResult(999,"Failed to initialise BT_SAVE");
		
		if(tblItems == null) return new VtiUserExitResult(999,"Failed to initialise TB_ITEMS");
		
//Initialising the ldb' to use	
		VtiExitLdbTable registerLdb = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable soItemsLdb = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable configLdb = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable packingLdb = getLocalDatabaseTable("YSWB_PACKING");
		VtiExitLdbTable wbLdb = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable rsoLdb = getLocalDatabaseTable("YSWB_RETAILSALES");
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
//Validating the instantiation of the ldb's
		if(registerLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_REGISTER");	
		if(soItemsLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_SO_ITEMS");
		if(configLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_CONFIG");
		if(packingLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_PACKING");
		if(wbLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_WB");
		if(rsoLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_RETAILSALES");
		if(sessionHeader == null) return new VtiUserExitResult(999,"Failed to get Header Info");
		
//&&&&&&&&&&& Declaring all the variables and constants to be used
				
		boolean hasItems = false;
		String docType = "";
		Date currNow = new Date();
		String currDate = DateFormatter.format("yyyyMMdd", currNow);
		String currTime = DateFormatter.format("HHmmss", currNow);
		String stampNow = currDate + currTime;
		String order = "";
		
		Double doubleStampNow = new Double(stampNow);
		double timeStampNow = doubleStampNow.doubleValue();
		double timeStampMin5m = timeStampNow - 500000000;
		String timeStampMin5Months = Double.toString(timeStampMin5m);
		long bagsUoM = 0;
		long gateWeight = 0;
		long bags = 0;
		String packDocType = "";
		String sErrorMsg = null;
		
		tblItems.clear();
		
		if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_LOADINGRSO"))
		{
			sErrorMsg = AllowLoad();
		
			if(sErrorMsg.equalsIgnoreCase("false"))
			{
				scrTruckReg.setFieldValue("");
				scrVRef.setFieldValue("");
				scrRefDoc.setFieldValue("");
				return new VtiUserExitResult(999,1,"Loading already recorded.");
			}
		}
		
//Config
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "BAGS_UOM"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
        
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbTableRow[] configLdbRows = configLdb.getMatchingRows(configSelCondGrp);
		if(configLdbRows.length == 0)
		{
			bagsUoM = 0;
			return new VtiUserExitResult(000,"Bags Unit of Measure details not maintained in Config.");
		}
		else
		{
			bagsUoM = configLdbRows[0].getLongFieldValue("KEYVAL1");;
		}
		
		//So Items
		for(int c = 0;c < tblOrders.getRowCount();c++)
		{
				docType = "VBELN";
				packDocType = "VBELN";
				order = tblOrders.getRow(c).getFieldValue("VBELN_I");
								
				VtiExitLdbSelectCriterion [] soSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
									new VtiExitLdbSelectCondition("MTART", VtiExitLdbSelectCondition.NE_OPERATOR, scrCMtart.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
		    
				VtiExitLdbSelectConditionGroup soSelCondGrp = new VtiExitLdbSelectConditionGroup(soSelConds, true);
				VtiExitLdbTableRow[] soLdbRows = soItemsLdb.getMatchingRows(soSelCondGrp);

				if(soLdbRows.length == 0)
					return new VtiUserExitResult(999,"No items found for the Truck and Order.");

				hasItems = true;
						
				//WB LDB Count 
				VtiExitLdbSelectCriterion [] wbSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, order),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
										new VtiExitLdbSelectCondition("WEIGHT1", VtiExitLdbSelectCondition.GT_OPERATOR, "0"),
						new VtiExitLdbSelectCondition("PACKLOADER", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
		    
				VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
				VtiExitLdbTableRow[] wbLdbRows = wbLdb.getMatchingRows(wbSelCondGrp);
				
				Log.trace(0,"WB Records truck and order." + wbLdbRows.length);
			
				VtiExitLdbSelectCriterion [] wbRejSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, order),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "REJECTED"),
						new VtiExitLdbSelectCondition("PACKLOADER", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
		    
				VtiExitLdbSelectConditionGroup wbRejSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRejSelConds, true);
				VtiExitLdbTableRow[] wbRejLdbRows = wbLdb.getMatchingRows(wbRejSelCondGrp);

				Log.trace(0,"WB Records reject truck and order." + wbRejLdbRows.length);
				
				int rejCom = 0;
			
				if(wbRejLdbRows.length == 0)
				{
					rejCom = 0;
				}
				else if(wbLdbRows.length > 0)
					if(wbLdbRows[wbLdbRows.length-1].getFieldValue("STATUS").equalsIgnoreCase("REJECTED"))
				{
					rejCom = 1;
				}
				else
				{
					rejCom = 0;
				}
				
				//Packing LDB Count 
				int pCheck = 10;
				int qDiv = 0;
				int pRecs = 0;
				
					VtiExitLdbSelectCriterion [] packingQSelConds = 
						{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition(packDocType, VtiExitLdbSelectCondition.EQ_OPERATOR, order),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
											new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DISPATCH"),
												new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
													new VtiExitLdbSelectCondition("POSNR", VtiExitLdbSelectCondition.EQ_OPERATOR, Integer.toString(pCheck)),
							 new VtiExitLdbSelectCondition("OFF_ORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
		  
					VtiExitLdbSelectConditionGroup packingQSelCondGrp = new VtiExitLdbSelectConditionGroup(packingQSelConds, true);

					VtiExitLdbTableRow[] packingQTLdbRows = packingLdb.getMatchingRows(packingQSelCondGrp);
					Log.trace(0,"Packing records count " + packingQTLdbRows.length);
				if(scrPQty != null)
				{
						VtiExitLdbSelectCriterion [] rsoSelConds = 
						{
								new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("RETAILSALESORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
											new VtiExitLdbSelectCondition("REGISTRATION", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
						};
			        
						VtiExitLdbSelectConditionGroup rsoSelCondGrp = new VtiExitLdbSelectConditionGroup(rsoSelConds, true);
						VtiExitLdbTableRow[] rsoLdbRows = rsoLdb.getMatchingRows(rsoSelCondGrp);
						
					for(int ip = 0;ip < packingQTLdbRows.length;ip++)
					{
						int pPos = 10;
					
						for(int i = 0;i < packingQTLdbRows.length;i++)
						{
							if(packingQTLdbRows[i].getIntegerFieldValue("POSNR") == pPos)
								pRecs++;
						}
					

						
							
						Log.trace(0,"Packing count  Records " + pRecs);
						Log.trace(0,"Reject count = " + rejCom);
						Log.trace(0,"rso's count " + rsoLdbRows.length);

						//pRecs = pRecs / rsoLdbRows.length;
						
						if(wbLdbRows.length + rejCom<= pRecs)
						{
							 scrRQty.setFieldValue(wbRejLdbRows.length);
							 scrWQty.setFieldValue(wbLdbRows.length);
							 scrPQty.setFieldValue(pRecs);
							 
							  btnSave.setHiddenFlag(true);
							  return new VtiUserExitResult(999,1,"Packing has already been done for line " + pPos + " of Sales Order " + order + ".");
						}
						else
						{
								
							  if(scrWQty == null) return new VtiUserExitResult(999,"Failed to initialise W_QTY");
							  if(scrRQty == null) return new VtiUserExitResult(999,"Failed to initialise R_QTY");
								  
							  scrRQty.setFieldValue(wbRejLdbRows.length);
							  scrWQty.setFieldValue(wbLdbRows.length);
							  scrPQty.setFieldValue(pRecs);
						}
						
						pPos = pPos +  10;
						
						pRecs = 0;
					}
				}
			//Register
					VtiExitLdbSelectCriterion [] registerSelConds = 
					{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition(docType, VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefDoc.getFieldValue()),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
											new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
			        
					VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
					VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
					if(registerLdbRows.length == 0)
						return new VtiUserExitResult(999,"Arrival entry not found.");
				
					
					/*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
					 Fill the TB_ITEMS screen table with the items associated with the order number.
					 Calculate the bags for the weight to be packed.
					*****************************************************************************************/
					if(soLdbRows.length > 0  && scrSoNo.getFieldValue().length() > 0)
					{		
						VtiUserExitScreenTableRow newRow;
						
						for(int so = 0;so < soLdbRows.length;so++)
						{
							newRow = tblItems.getNewRow();
							newRow.setFieldValue("DESC",soLdbRows[so].getFieldValue("ARKTX"));
							newRow.setFieldValue("MATNR",soLdbRows[so].getFieldValue("MATNR"));
							
							if(sessionHeader.getFunctionId().equalsIgnoreCase("YSWB_LOADING"))
								newRow.setFieldValue("MEINS",soLdbRows[so].getFieldValue("MEINS"));
							
							try
							{
								newRow.setFieldValue("MAT",soLdbRows[so].getFieldValue("MATNR").substring(soLdbRows[so].getFieldValue("MATNR").length() - 8,soLdbRows[so].getFieldValue("MATNR").length()));
							}
							catch (StringIndexOutOfBoundsException sobe)
							{	
								try
								{
									newRow.setFieldValue("MAT",soLdbRows[so].getFieldValue("MATNR"));
								}
								catch (StringIndexOutOfBoundsException soba)
								{
									Log.error("MAT not filled.Length of content" +  soLdbRows[so].getFieldValue("MATNR").length()+ ", Content :" + soLdbRows[so].getFieldValue("MATNR"), soba );
								}
							}
							
							newRow.setFieldValue("MENGE",soLdbRows[so].getFieldValue("NTGEW"));
							newRow.setFieldValue("POSNR",soLdbRows[so].getFieldValue("POSNR"));
							newRow.setFieldValue("Q_REQ",soLdbRows[so].getFieldValue("BAGS"));
							newRow.setFieldValue("VBELN_IP",soLdbRows[so].getFieldValue("VBELN"));
							newRow.setFieldValue("LBLDAMAGED","Damaged:");
							newRow.setFieldValue("LBLBROKEN","Broken:");
							newRow.setFieldValue("TXTBAGNR","BAG NR:");
							newRow.setFieldValue("Q","Q:");
							newRow.setFieldValue("CHK_SEL", "X");
							
							tblItems.appendRow(newRow);
						}
					}			
		}
		

		
		/****************************************************************************************
		*****************************************************************************************/
		return new VtiUserExitResult();
	}
	
	
	//
	
	private String AllowLoad()throws VtiExitException
	{
		VtiUserExitScreenField scrTruckReg = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrRefDoc = getScreenField("REFDOC");
		
		if(scrRefDoc.getFieldValue().length() == 0)
			scrTruckReg.setFieldValue("");
		
		VtiUserExitScreenTable items = getScreenTable("TB_ITEMS");
				
		int tableRows = items.getRowCount();
		boolean hasSo = false;
		boolean hasSt = false;
		boolean hasIc = false;
		
		Date currNow = new Date();
		String currDate = DateFormatter.format("yyyyMMdd", currNow);
		String currTime = DateFormatter.format("HHmmss", currNow);
		String stampNow = currDate + currTime;
		
		Long longStampNow = new Long(stampNow);
		long timeStampNow = longStampNow.longValue();
		long timeStampMin2d = timeStampNow - 2000000;
		long listCount = 0L;
		String timeStampMin2Days = Long.toString(timeStampMin2d);
		String docType = null;
		String packDocType = null;
		String sErrorMsg = null;
		
		VtiExitLdbTable bagsLdbTable = getLdbTable("YSWB_BAGS");
		VtiExitLdbTable soLdbTable = getLdbTable("YSWB_SO_HEADER");
		VtiExitLdbTable loadingLdbTable = getLdbTable("YSWB_LOADING");
		VtiExitLdbTable packingLdbTable = getLdbTable("YSWB_PACKING");
							
		if(scrTruckReg.getFieldValue().length() > 0)
		{
			
			VtiExitLdbSelectCriterion [] soHeaderSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruckReg.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "ASSIGNED"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "WEIGH 2"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "DELETED"),
							//new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "FAILED"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "NEW"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NS_OPERATOR, "0"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "OVERIDE"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "PASS"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "SAP ERROR"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
				new VtiExitLdbSelectCondition("RETAIL_ORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, "X")
			};
        
			VtiExitLdbSelectConditionGroup soHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderSelConds, true);
			VtiExitLdbTableRow[] soHeaderLdbRows = soLdbTable.getMatchingRows(soHeaderSelCondGrp);
			
			Log.trace(0,"allowload count from so header q " + soHeaderLdbRows.length);
			
			scrTruckReg.clearPossibleValues();
			for(int s = 0;s<soHeaderLdbRows.length;s++)
			{
				//Only add if the loading count is less than the packing count.
				VtiExitLdbSelectCriterion [] packingSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("TRUCK")),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("VBELN")),
									new VtiExitLdbSelectCondition("OFF_ORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("DELIVERY")),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
        
				VtiExitLdbSelectConditionGroup packingSelCondGrp = new VtiExitLdbSelectConditionGroup(packingSelConds, true);
				VtiExitLdbTableRow[] packingLdbRows = packingLdbTable.getMatchingRows(packingSelCondGrp);
				
				Log.trace(0,"LoadAllow packing lenght " + packingLdbRows.length);
				
				VtiExitLdbSelectCriterion [] loadSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("TRUCK")),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("VBELN")),
									new VtiExitLdbSelectCondition("OFF_ORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderLdbRows[s].getFieldValue("DELIVERY")),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")

				};
        
				VtiExitLdbSelectConditionGroup loadSelCondGrp = new VtiExitLdbSelectConditionGroup(loadSelConds, true);
				VtiExitLdbTableRow[] loadLdbRows = loadingLdbTable.getMatchingRows(loadSelCondGrp);
			
				Log.trace(0,"LoadAllow load lenght " + loadLdbRows.length);
			
				if(loadLdbRows.length < packingLdbRows.length)
					listCount++;
			}
		}
		
		if(listCount > 0)
			sErrorMsg = "true";
		else
			sErrorMsg = "false";
	
		return sErrorMsg;
	}
}
