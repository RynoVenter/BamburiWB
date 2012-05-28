package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GatePassUpload extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		DBCalls dbCalls = new DBCalls();
		
		dbCalls.ldbUpload("YSWB_GATEPASS", this);
		return new VtiUserExitResult();
	}
}
