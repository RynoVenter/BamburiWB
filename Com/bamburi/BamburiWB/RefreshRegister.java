package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RefreshRegister extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		DBCalls dbCall = new DBCalls();
		try
		{
			String hostName = getHostInterfaceName();

			forceHeartbeat(hostName,true,250);
	
			boolean hostConnected = isHostInterfaceConnected(hostName);
			hostConnected = isHostInterfaceConnected(hostName);

			if (hostConnected)
			{ 
				dbCall.ldbUpload("YSWB_IC_HEADER", this);
				dbCall.ldbUpload("YSWB_IC_ITEMS", this);
				dbCall.ldbUpload("YSWB_REGISTER", this);
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException ie)
				{
				}
				
				
				
			
				dbCall.ldbDownload("YSWB_PO_HEADER", this);
				dbCall.ldbDownload("YSWB_PO_ITEMS", this);
				dbCall.ldbDownload("YSWB_IC_HEADER", this);
				dbCall.ldbDownload("YSWB_IC_ITEMS", this);
				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException ie)
				{
				}
				
				dbCall.ldbDownload("YSWB_GATEPASS", this);
				dbCall.ldbUpload("YSWB_INSPECT", this);
			}
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to refresh now.", ee);
			return new VtiUserExitResult(999,"Unable to refresh.");
		}
		
		return new VtiUserExitResult(0,1,"Refresh started.");
	}
	
}
