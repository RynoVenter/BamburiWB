package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class WIMSQueueUpdate extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
	
		DBCalls qUpdateCall = new DBCalls();
		
		qUpdateCall.ldbUpload("YSWB_QUEUE", this);
					
		return new VtiUserExitResult();
	}
}
