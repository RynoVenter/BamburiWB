package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RefreshArrive extends VtiUserExit
{/*Refresh the data and elements in the arrive screen
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.

	
		String hostName = getHostInterfaceName();
		boolean hostConnected = isHostInterfaceConnected(hostName);
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
		DBCalls dbCall = new DBCalls();
		try
		{
				if (hostConnected)
				{ 
					dbCall.ldbUpload("YSWB_IC_HEADER", this);
					dbCall.ldbUpload("YSWB_IC_ITEMS", this);

				}
				else
				{
					return new VtiUserExitResult(999,"The refresh is not currently possible.");
				}
		}
		catch (VtiExitException ee)
		{
				Log.error("Host not connected to SAP to upload register data during Arrival save, check server.");
		}
		
		try
		{
			if (hostConnected)
			{ 
				dbCall.ldbRefresh("YSWB_SO_HEADER", this);
				dbCall.ldbRefresh("YSWB_SO_ITEMS", this);
				dbCall.ldbRefresh("YSWB_PO_HEADER", this);
				dbCall.ldbRefresh("YSWB_PO_ITEMS", this);
				dbCall.ldbRefresh("YSWB_GATEPASS", this);
				dbCall.ldbDownload("YSWB_IC_HEADER", this);
				dbCall.ldbDownload("YSWB_IC_ITEMS", this);
			}
			else
			{
				return new VtiUserExitResult(999,"The refresh is not currently possible.");
			}
		}
		catch ( VtiExitException ee)
		{
			Log.error("Error refreshing the Order tables.", ee);
			return new VtiUserExitResult(999,"The refresh is not currently possible.");
		}
		
		return new VtiUserExitResult();
		
	}
}
