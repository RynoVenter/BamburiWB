package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RefreshGP extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		String hostName = getHostInterfaceName();
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}
		boolean hostConnected = isHostInterfaceConnected(hostName);
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
		DBCalls dbCall = new DBCalls();
		
		try
		{
			if (hostConnected)
			{ 
				dbCall.ldbRefresh("YSWB_GATEPASS", this);

			}
			else
			{
				return new VtiUserExitResult(999,"The refresh is not currently possible.");
			}
		}
		catch ( VtiExitException ee)
		{
			Log.error("Error refreshing the Purchase Order tables.", ee);
			return new VtiUserExitResult(999,"The refresh is not currently possible.");
		}
		
		return new VtiUserExitResult();
	}
}
