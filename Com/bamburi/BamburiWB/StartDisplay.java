package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class StartDisplay extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		VtiUserExitHeaderInfo sessionInfo = getHeaderInfo();
		
		VtiExitLdbTable confLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		if(confLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_CONFIG failed to load.");
		if(queueLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_QUEUE failed to load.");
		
		while(sessionInfo.getFunctionId().equalsIgnoreCase("YSWB_QUEUEDISP"))
		{
		//Get queue qty
		VtiExitLdbSelectCriterion [] configSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "QUEUE"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
		VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("KEYVAL1",true),
			};
		VtiExitLdbTableRow configLdbRows[] = confLdbTable.getMatchingRows(configSelCondGrp, orderBy);
		
		if(configLdbRows.length == 0)
			return new VtiUserExitResult(999, "No queue information availible in the config table");
		
		
			try
			{
				Thread myThread = new Thread().currentThread();
				myThread.sleep(5000);
			}
			catch(InterruptedException ie)
			{
				Log.error("Thread sleep failed.",ie);
			}
			
			//Get the screen table for the Q
			for(int tbl = 0;tbl < configLdbRows.length;tbl++)
			{
				String qTbl = "TB_Q" + (tbl+1);
				VtiUserExitScreenTable scrTableQ = getScreenTable(qTbl);
				
				if(scrTableQ == null)
				{
					break;
				}
				
				if(scrTableQ.getRowCount()>0)
				{
					for(int del = 0; del < scrTableQ.getRowCount();del++)
					{
						if(del < scrTableQ.getRowCount()-1)
							scrTableQ.deleteRowAt(1);
						try
						{
							Thread myThread = new Thread().currentThread();
							myThread.sleep(500);
						}
						catch(InterruptedException ie)
						{
							Log.error("Thread sleep failed.",ie);
						}
					}
					VtiUserExitScreenField scfQ = getScreenField("Q"+(tbl+1));
			
					String queue = scfQ.getFieldValue();
					//Get the trucks in the queue for the current Q	
					VtiExitLdbSelectCriterion [] qSelConds = 
					{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.EQ_OPERATOR, queue),
									new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "PASSED"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		
					VtiExitLdbOrderSpecification [] qOrderBy = 
					{
						new VtiExitLdbOrderSpecification("Q_DATE",true),
						new VtiExitLdbOrderSpecification("Q_TIME",true),
					};
		
					VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp,qOrderBy);
		
					String qField = "Q" + (tbl+1) + "_TIME";
					String tField = "Q" + (tbl+1) + "_TRUCK";	
		
					for(int i = 0;i<qTLdbRows.length;i++)
					{
						VtiUserExitScreenTableRow scrTableQRow = scrTableQ.getNewRow();
								
						scrTableQRow.setFieldValue(qField,qTLdbRows[i].getFieldValue("Q_TIME"));
						scrTableQRow.setFieldValue(tField,qTLdbRows[i].getFieldValue("Q_REGNO"));
					}
				}
			}
		}
		return new VtiUserExitResult();
	}
}
