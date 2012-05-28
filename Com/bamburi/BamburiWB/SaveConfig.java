package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class SaveConfig extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		DBCalls dbCall = new DBCalls();
		try
		{
			String hostName = getHostInterfaceName();
			boolean hostConnected = isHostInterfaceConnected(hostName);
			hostConnected = isHostInterfaceConnected(hostName);

			if (hostConnected)
			{ 
				dbCall.ldbUpload("YSWB_CONFIG", this);
			}
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to refresh now.", ee);
			return new VtiUserExitResult(999,"Unable to refresh.");
		}
		
		return new VtiUserExitResult();
	}
}
