package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class UpLoadConfig extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		DBCalls upConf = new DBCalls();
		String hostName = getHostInterfaceName();
		boolean hostConnected = isHostInterfaceConnected(hostName);
		hostConnected = isHostInterfaceConnected(hostName);

		if (hostConnected)
		{ 
			upConf.ldbUpload("YSWB_CONFIG", this);
			upConf.ldbUpload("YSWB_MATERIALS", this);
			upConf.ldbUpload("VTI_VALUE_LIST", this);
			upConf.ldbUpload("YSWB_LOGON", this);
		}
		
		return new VtiUserExitResult();
	}
}
