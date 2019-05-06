package com.cmccpoc.entity;


public class AirTaskCase
{

	private String TaskId = "";
	private String CaseCode = "";		//案件号
	private String CaseName = "";		//案件名称
	private String Detail = "";
	private String CarNo = "";			//车牌号
	private boolean isLocal = false;


	public String getTaskId() {
		return TaskId;
	}

	public void setTaskId(String taskId) {
		TaskId = taskId;
	}

	public String getCaseCode() {
		return CaseCode;
	}

	public void setCaseCode(String caseCode) {
		CaseCode = caseCode;
	}

	public String getCaseName() {
		return CaseName;
	}

	public void setCaseName(String caseName) {
		CaseName = caseName;
	}

	public String getDetail() {
		return Detail;
	}

	public void setDetail(String detail) {
		Detail = detail;
	}

	public String getCarNo() {
		return CarNo;
	}

	public void setCarNo(String carNo) {
		CarNo = carNo;
	}

	public boolean isLocal() {
		return isLocal;
	}

	public void setLocal(boolean local) {
		isLocal = local;
	}
}
