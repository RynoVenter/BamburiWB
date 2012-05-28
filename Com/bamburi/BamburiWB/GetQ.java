package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetQ
{
	VtiUserExit vUE;
	String order;
	String truck;
	String vtiRef;
	Date currNow = new Date();
	String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
	String currTime = DateFormatter.format("HH:mm:ss", currNow);	
	int shrtQueueInterval = 0;
	
	public GetQ (VtiUserExit vUE, String order, String truck)
	{
		this.vUE = vUE;
		this.order = order;
		this.truck = truck;
	}

	public String getTruckQ() throws VtiExitException
	{
		String sQueue = "";
		String matType = "";
		String ordTyp = "";
		int sQ = 1000000;
		int sQCalc = 0;
		
		int qTimeLapse = 0;
		
		VtiExitLdbTable confLdbTable = vUE.getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable soItemsLdbTable = vUE.getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable poItemsLdbTable = vUE.getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable icItemsLdbTable = vUE.getLocalDatabaseTable("YSWB_IC_ITEMS");
		
		VtiExitLdbTable soLdbTable = vUE.getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable poLdbTable = vUE.getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable icLdbTable = vUE.getLocalDatabaseTable("YSWB_IC_HEADER");
		
		//Determine order details to get the queue for allocation
		
		//SO det
		VtiExitLdbSelectCriterion [] soSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, truck),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soSelCondGrp = new VtiExitLdbSelectConditionGroup(soSelConds, true);
		VtiExitLdbTableRow[] soLdbRows = soLdbTable.getMatchingRows(soSelCondGrp);
		
		//PO det
		VtiExitLdbSelectCriterion [] poSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
		VtiExitLdbTableRow[] poLdbRows = poLdbTable.getMatchingRows(poSelCondGrp);
			
		//IC det
		VtiExitLdbSelectCriterion [] icSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truck),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup icSelCondGrp = new VtiExitLdbSelectConditionGroup(icSelConds, true);
		VtiExitLdbTableRow[] icLdbRows = icLdbTable.getMatchingRows(icSelCondGrp);
		
		
		//SO I det
		VtiExitLdbSelectCriterion [] soISelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soISelCondGrp = new VtiExitLdbSelectConditionGroup(soISelConds, true);
		VtiExitLdbTableRow[] soILdbRows = soItemsLdbTable.getMatchingRows(soISelCondGrp);
		
		//PO I det
		VtiExitLdbSelectCriterion [] poISelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup poISelCondGrp = new VtiExitLdbSelectConditionGroup(poISelConds, true);
		VtiExitLdbTableRow[] poILdbRows = poItemsLdbTable.getMatchingRows(poISelCondGrp);
			
		//IC I det
		VtiExitLdbSelectCriterion [] icISelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup icISelCondGrp = new VtiExitLdbSelectConditionGroup(icISelConds, true);
		VtiExitLdbTableRow[] icILdbRows = icItemsLdbTable.getMatchingRows(icISelCondGrp);
		//
		if(soILdbRows.length > 0)
		{
			matType = soILdbRows[0].getFieldValue("MTART");
			ordTyp = soLdbRows[0].getFieldValue("AUART");
		}
		if(poILdbRows.length > 0)
		{
			matType = poILdbRows[0].getFieldValue("MTART");
			ordTyp = poLdbRows[0].getFieldValue("BSART");
			
			if(ordTyp.equalsIgnoreCase("ZRO"))
				matType = "ROH";
		}
		if(icILdbRows.length > 0)
		{
			matType = "FERT";
			ordTyp = "ZIC";
		}
		
		//if none found, then set ordtyp ZRO and matType ROH
		if(soILdbRows.length == 0 
		&& poILdbRows.length == 0 
		&& icILdbRows.length == 0)
		{
			matType = "ROH";
			ordTyp = "ZRO";
		}
		
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, "TQUEUE"),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, matType),
								new VtiExitLdbSelectCondition("KEYVAL3", VtiExitLdbSelectCondition.EQ_OPERATOR, ordTyp),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbTableRow[] configLdbRows = confLdbTable.getMatchingRows(configSelCondGrp);
		
		if(configLdbRows.length == 0)
		{
			/*Log.error("No Queue found, please correct details for config search was :");
			Log.error("SERVERGRP being matched was " + vUE.getServerGroup());
			Log.error("SERVERID being matched was " + vUE.getServerId());
			Log.error("USERID being matched was " + "TQUEUE");
			Log.error("KEYFIELD being matched was " + matType);
			Log.error("KEYVAL3 being matched was " + ordTyp);*/
			
			
			return sQueue = "RAW";
		}
		
		String queue = "";
		
		queue = configLdbRows[0].getFieldValue("KEYVAL4");

		sQueue = queue;
				
		return sQueue;
	}
	
	public String getTruckQ(String screenQueue) throws VtiExitException
	{
		String sQueue = "";
		String matType = "";
		String ordTyp = "";
		int sQ = 1000000;
		int sQCalc = 0;
		
		int qTimeLapse = 0;
		
		VtiExitLdbTable confLdbTable = vUE.getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable soItemsLdbTable = vUE.getLocalDatabaseTable("YSWB_SO_ITEMS");
		
		VtiExitLdbTable soLdbTable = vUE.getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable poLdbTable = vUE.getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable icLdbTable = vUE.getLocalDatabaseTable("YSWB_IC_HEADER");
		//Determine order details to get the queue for allocation
		
		//SO det
		VtiExitLdbSelectCriterion [] soSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, truck),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soSelCondGrp = new VtiExitLdbSelectConditionGroup(soSelConds, true);
		VtiExitLdbTableRow[] soLdbRows = soLdbTable.getMatchingRows(soSelCondGrp);
		
		//PO det
		VtiExitLdbSelectCriterion [] poSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
		VtiExitLdbTableRow[] poLdbRows = poLdbTable.getMatchingRows(poSelCondGrp);
			
		//IC det
		VtiExitLdbSelectCriterion [] icSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, order),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, truck),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup icSelCondGrp = new VtiExitLdbSelectConditionGroup(icSelConds, true);
		VtiExitLdbTableRow[] icLdbRows = icLdbTable.getMatchingRows(icSelCondGrp);
		
		//
		if(soLdbRows.length > 0)
		{
			ordTyp = soLdbRows[0].getFieldValue("AUART");
		}
		if(poLdbRows.length > 0)
		{
			ordTyp = poLdbRows[0].getFieldValue("BSART");
		}
		if(icLdbRows.length > 0)
		{
			ordTyp = "ZIC";
		}
		
		//if none found, then set ordtyp ZRO and matType ROH
		if(soLdbRows.length == 0 
		&& poLdbRows.length == 0 
		&& icLdbRows.length == 0)
		{
			ordTyp = "ZRO";
		}
		
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, "TQUEUE"),
							new VtiExitLdbSelectCondition("KEYVAL3", VtiExitLdbSelectCondition.EQ_OPERATOR, ordTyp),
								new VtiExitLdbSelectCondition("KEYVAL4", VtiExitLdbSelectCondition.EQ_OPERATOR, screenQueue),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbTableRow[] configLdbRows = confLdbTable.getMatchingRows(configSelCondGrp);
		
		if(configLdbRows.length == 0)
		{
			return sQueue = "RAW";
		}
		
		String queue = "";
		
		shrtQueueInterval = configLdbRows[0].getIntegerFieldValue("KEYVAL2");
		queue = screenQueue;
		sQueue = queue;
				
		return sQueue;
	}
	
}
