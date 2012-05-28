package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class QuickInfo
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		/*//get status from
		
		//registered = register //
		//inspection = register //
		//assigned = register include order //
		//weigh 1 = wb table incluse order //
		//packing = packing table include order//
		//weigh 2 = wb table include order//
		//complete = register include order//
		//failed  = register//
		//overide = register//
		//rejected = wb table include order//
		VtiUserExitScreenField scrFDate = getScreenField("S_DATE");
		VtiUserExitScreenTable scrTPurchOrd = getScreenTable("TB_SALES");
		
		
		
		String dispStatus = "";
		String dispTime  = "";
		String dispDate = "";
		
		boolean statFound = false;
		
		
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable packingLdbTable = getLocalDatabaseTable("YSWB_PACKING");
		
		//Get current register status
		VtiExitLdbSelectCriterion [] regSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
			};
      
			VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
			VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);

			
			if(regLdbRows.length > 0)	
			{
				String status = regLdbRows[0].getFieldValue("INSPSTATUS");
			
				switch (status) 
				{
					case "R": 
						code_block_1;
					case "P": 
						code_block_2;
					case "F": 
						code_block_n;
					case "O": 
						code_block_n;
					case "W": 
					{
						//Get current wb status if any
						VtiExitLdbSelectCriterion [] wbSelConds = 
						{
								new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
						};
      
						VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
						VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);

						if(wbLdbRows.length > 0)
						{
							//if( weigh 1 show weigh 1
								//check packing
							//Get current packing status if any
							String wbStatus = wbLdbRows[0].getFieldValue("STATUS");
							switch (wbStatus) 
							{
								case "WEIGH 1": 
								{
									VtiExitLdbSelectCriterion [] packSelConds = 
									{
											new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
												new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
													new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
									};
      
									VtiExitLdbSelectConditionGroup packSelCondGrp = new VtiExitLdbSelectConditionGroup(packSelConds, true);
									VtiExitLdbTableRow[] packLdbRows = packingLdbTable.getMatchingRows(packSelCondGrp);

									if(packLdbRows.length > 0)	
									{
									}
								}
								case "WEIGH 2": 
									code_block_2;
								case "REJECTED": 
									code_block_2;
							
							//if(weigh 2 show weigh 2
							//if rejected show rejected and reason
							}

					}
					case "C": 
						code_block_n;
						
					break;
					default: code_block_default;
				
				}
		}*/
		return new VtiUserExitResult();
	}
}
