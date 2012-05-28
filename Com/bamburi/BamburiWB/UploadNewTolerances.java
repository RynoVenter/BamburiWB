package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class UploadNewTolerances extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		DBCalls dbCall = new DBCalls();
		
		
		
		String hostName = getHostInterfaceName();
		boolean hostConnected = isHostInterfaceConnected(hostName);
		
		if(hostConnected)
		{
			dbCall.ldbUpload("YSWB_CONFIG",this);
			return new VtiUserExitResult(000,"Upload requested.");
		}
		else
			return new VtiUserExitResult(000,"Host not connected, upload not possible..");
	}
}
