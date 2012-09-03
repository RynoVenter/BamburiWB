package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class MySQLQueueUpdate extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
	
		DBCalls qUpdateCall = new DBCalls();
		
		qUpdateCall.ldbRefreshOnly("YSWB_QUEUE", this);
		qUpdateCall.ldbDownload("YSWB_REGISTER", this);
					
		return new VtiUserExitResult();
	}
}
