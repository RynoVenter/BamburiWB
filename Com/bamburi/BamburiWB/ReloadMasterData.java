package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class ReloadMasterData extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiExitLdbTable poHeadLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		VtiExitLdbTable poItemsLdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		VtiExitLdbTable loginLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		VtiExitLdbTable soHLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable soILdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable icHLdbTable = getLocalDatabaseTable("YSWB_IC_HEADER");
		VtiExitLdbTable icILdbTable = getLocalDatabaseTable("YSWB_IC_ITEMS");
		
		
		if (poHeadLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		if (poItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_ITEMS.");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");
		if (loginLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		if (soHLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (soILdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_ITEMS.");
		if (icHLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_HEADER.");
		if (icILdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_ITEMS.");
		
		DBCalls dbCall = new DBCalls();
		
		//Upload all data to SAP, then Delete all data from the DB structure, then Download everything again.
		//This will wipe the DB clean and reload the data.
		
		try
		{
			String hostName = getHostInterfaceName();
			forceHeartbeat(hostName,true,250);
			boolean hostConnected = isHostInterfaceConnected(hostName);
			hostConnected = isHostInterfaceConnected(hostName);

			if (hostConnected)
			{ 
			
				dbCall.ldbUpload("YSWB_PO_HEADER", this);
				dbCall.ldbUpload("YSWB_PO_ITEMS", this);
				dbCall.ldbUpload("YSWB_CONFIG", this);
				dbCall.ldbUpload("YSWB_LOGON", this);
				dbCall.ldbUpload("YSWB_SO_HEADER", this);
				dbCall.ldbUpload("YSWB_SO_ITEMS", this);
				dbCall.ldbUpload("YSWB_IC_HEADER", this);
				dbCall.ldbUpload("YSWB_IC_ITEMS", this);
			}
			
			
		}
		catch (VtiExitException ee)
		{
			return new VtiUserExitResult(999,"Unable to upload all data. Reload failed.");
		}
		
		//try
		//{
			//Thread.sleep(5000);
		//}
		//catch (InterruptedException ie)
		//{
		//	Log.error("Sleep Thread during Master reload failed.", ie);
		//}
		
		poHeadLdbTable.deleteAllRows();
		poItemsLdbTable.deleteAllRows();
		configLdbTable.deleteAllRows();
		loginLdbTable.deleteAllRows();
		soHLdbTable.deleteAllRows();
		soILdbTable.deleteAllRows();
		icHLdbTable.deleteAllRows();
		icILdbTable.deleteAllRows();
		
		try
		{
			
			dbCall.ldbDownload("YSWB_PO_HEADER", this);
			dbCall.ldbDownload("YSWB_PO_ITEMS", this);
			dbCall.ldbDownload("YSWB_CONFIG", this);
			dbCall.ldbDownload("YSWB_LOGON", this);
			//dbCall.ldbDownload("YSWB_SO_HEADER", this);
			//dbCall.ldbDownload("YSWB_SO_ITEMS", this);
			//dbCall.ldbDownload("YSWB_IC_HEADER", this);
			//dbCall.ldbDownload("YSWB_IC_ITEMS", this);
			
		}
		catch (VtiExitException ee)
		{
			return new VtiUserExitResult(999,"Unable to upload all data. Reload failed.");
		}
		return new VtiUserExitResult();
	}
}
