package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetQ
{
	VtiUserExit vUE;
	String type;
	Date currNow = new Date();
	String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
	String currTime = DateFormatter.format("HH:mm:ss", currNow);	
	int shrtQueueInterval = 0;
	
	public GetQ (VtiUserExit vUE, String type)
	{
		this.vUE = vUE;
		this.type = type;
	}
	
	public String getShortestQ() throws VtiExitException
	{
		String sQueue = "";
		int sQ = 1000000;
		int sQCalc = 0;
		
		int qTimeLapse = 0;
		
		VtiExitLdbTable confLdbTable = vUE.getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable queueLdbTable = vUE.getLocalDatabaseTable("YSWB_QUEUE");
				
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "QUEUE"),
							new VtiExitLdbSelectCondition("KEYVAL3", VtiExitLdbSelectCondition.EQ_OPERATOR, type),
								new VtiExitLdbSelectCondition("KEYVAL5", VtiExitLdbSelectCondition.EQ_OPERATOR, "X"),
		};
      
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbTableRow[] configLdbRows = confLdbTable.getMatchingRows(configSelCondGrp);
		
		if(configLdbRows.length == 0)
		{
			return sQueue = "NOQ1";
		}
		
		String queue = "";
		
		try
		{
			for(int qR = 0;qR <= configLdbRows.length-1;qR++)
			{
				queue = configLdbRows[qR].getFieldValue("KEYVAL1");
				qTimeLapse = configLdbRows[qR].getIntegerFieldValue("KEYVAL4");
				
				VtiExitLdbSelectCriterion [] qSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vUE.getServerId()),
							new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, queue),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "ASSIGNED"),
					
				};
      
				VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
				VtiExitLdbTableRow[] qLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
				
				if(qLdbRows.length == 0)
				{
					sQCalc = 0;
				}
				else
				{
					sQCalc = qLdbRows.length * qTimeLapse;
				}
				
				if(sQCalc <= sQ)
				{
					sQ = sQCalc;
					shrtQueueInterval = configLdbRows[qR].getIntegerFieldValue("KEYVAL4");
					queue = configLdbRows[qR].getFieldValue("KEYVAL1");
					sQueue = queue;
				}
				
			}
		}
		catch (VtiExitException ee)
		{
			vUE.logError("Unable to determine shortest queue.",ee);
		}
		return sQueue;
	}
	
	public int getShortestQInterval()
	{
		int interval = 0;
		interval = shrtQueueInterval;
		return interval;
	}
	
}
