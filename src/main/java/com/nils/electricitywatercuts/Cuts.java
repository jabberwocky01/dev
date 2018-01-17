/**
 * 
 */
package com.nils.electricitywatercuts;

/**
 * @author NilS
 *
 */
public class Cuts {
	
	private String operatorName;
	private String startDate;
	private String endDate;
	private String location;
	private String reason;
	private String detail;
	private String type;
	private int iconResourceId;

	public String getOperatorName() {
		if (operatorName==null)
			operatorName="";
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	
	public String getStartDate() {
		if (startDate==null)
			startDate="";
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		if (endDate==null)
			endDate="";
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getLocation() {
		if (location==null)
			location="";
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getReason() {
		if (reason==null)
			reason="";
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getDetail() {
		if (detail==null)
			detail="";
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	public String getType() {
		if (type==null)
			type="";
		return type;
	}
	
	public void setType(String type) {
		this.type = type;		
	}

	public int getIconResourceId() {
		return iconResourceId;
	}

	public void setIconResourceId(int iconResourceId) {
		this.iconResourceId = iconResourceId;
	}

	@Override
	public String toString() {
		return getOperatorName() + "\n" + getStartDate() + " - " + getEndDate() + "\n" 
				+ getLocation();
	}
	
	public String getDetailedText(String operatorTitle, String startEndDateTitle, String locationTitle, String reasonTitle) {
		return ("<b>" + operatorTitle + "</b> " + getOperatorName() + "<br />" + 
				 "<b>" + startEndDateTitle + "</b> " + getStartDate() + " - " + getEndDate() + "<br />" + 
				 "<b>" + locationTitle + "</b> " + getLocation() + "<br />" +
				 "<b>" + reasonTitle + "</b> " + getReason());
	}
	
	public String getPlainText(String electricityTitle, String waterTitle, String operatorTitle, String startEndDateTitle, 
			String locationTitle, String reasonTitle) {
		String plainText = waterTitle;
		if ("e".equals(type))
			plainText = electricityTitle;
			
		plainText += " > " + operatorTitle + " " + getOperatorName() + ", " + startEndDateTitle + " " + 
				getStartDate() + " - " + getEndDate() + ", " + locationTitle + " " + getLocation() + ", " + 
				reasonTitle + " " + getReason();
		return plainText;
	}

}
