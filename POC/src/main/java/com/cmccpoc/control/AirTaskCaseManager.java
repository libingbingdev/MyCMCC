package com.cmccpoc.control;

import android.text.TextUtils;

import com.airtalkee.sdk.controller.AirTaskController;
import com.airtalkee.sdk.controller.AirTaskController$AirTaskListener;
import com.airtalkee.sdk.entity.AirTask;
import com.airtalkee.sdk.entity.AirTaskDetail;
import com.airtalkee.sdk.entity.AirTaskReport;
import com.airtalkee.sdk.util.IOoperate;
import com.cmccpoc.dao.DBProxyTaskCase;
import com.cmccpoc.entity.AirTaskCase;
import com.cmccpoc.listener.OnMmiAirTaskCaseListener;
import com.cmccpoc.services.AirServices;

import java.util.ArrayList;
import java.util.List;

/**
 * 定损案件管理类
 * @author Yao
 */
public class AirTaskCaseManager implements AirTaskController$AirTaskListener
{
	private static AirTaskCaseManager mInstance;
	private IOoperate iOperate = new IOoperate();
	private static DBProxyTaskCase mDbProxy = null;

	private List<AirTaskCase> mTaskCaseList = new ArrayList<AirTaskCase>();
	private AirTaskCase mTaskCaseCurrent = null;
	private OnMmiAirTaskCaseListener mAirTaskCaseListener = null;
	private boolean mTaskCaseListReady = false;

	public static AirTaskCaseManager getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AirTaskCaseManager();
			mDbProxy = (DBProxyTaskCase) AirServices.getInstance().dbProxy();
			AirTaskController.getInstance().AirTaskListenerRegister(mInstance);
		}
		return mInstance;
	}

	public void setTaskCaseListener(OnMmiAirTaskCaseListener listener)
	{
		mAirTaskCaseListener = listener;
	}

	public List<AirTaskCase> getTaskCaseList()
	{
		return mTaskCaseList;
	}

	public AirTaskCase getTask(String taskId)
	{
		AirTaskCase task = null;
		for (int i = 0; i < mTaskCaseList.size(); i ++)
		{
			if (TextUtils.equals(mTaskCaseList.get(i).getTaskId(), taskId))
			{
				task = mTaskCaseList.get(i);
			}
		}
		return task;
	}

	public AirTaskCase getTaskCurrent()
	{
		return mTaskCaseCurrent;
	}

	public void setTaskCurrent(AirTaskCase task)
	{
		mTaskCaseCurrent = task;
	}


	public void LoadTasks()
	{
		mDbProxy.TaskCaseLoad(mTaskCaseList);
	}

	public void TaskCreate(String caseCode, String caseName, String carNo, String detail)
	{
		AirTaskController.getInstance().doTaskCaseOprCreate(caseCode, caseName, carNo, detail);
	}

	public void TaskUpdate(String taskId, String caseCode, String caseName, String carNo, String detail)
	{
		AirTaskController.getInstance().doTaskCaseOprUpdate(taskId, caseCode, caseName, carNo, detail);
	}

	public void TaskDelete(String taskId)
	{
		//AirTaskController.getInstance().doTaskCaseOprDelete(taskId);
		for (int i = 0; i < mTaskCaseList.size(); i ++)
		{
			if (TextUtils.equals(mTaskCaseList.get(i).getTaskId(), taskId))
			{
				if (mTaskCaseCurrent == mTaskCaseList.get(i))
					mTaskCaseCurrent = null;
				mTaskCaseList.remove(i);
				mDbProxy.TaskCaseDelete(taskId);
				break;
			}
		}
	}

	public boolean TaskListGet()
	{
		boolean isReady = true;
		if (!mTaskCaseListReady)
		{
			isReady = false;
			AirTaskController.getInstance().doTaskListGet(0, AirTask.TASK_TYPE_NORMAL, "", "", true);
		}
		return isReady;
	}

	public AirTaskCase TaskListPush(AirTask task)
	{
		AirTaskCase t = new AirTaskCase();
		t.setTaskId(task.getTaskCode());
		t.setCaseName(task.getTaskTitle());
		t.setDetail(task.getTaskDescription());
		mTaskCaseList.add(0, t);
		if (mTaskCaseListReady)
		{
			if (mAirTaskCaseListener != null)
				mAirTaskCaseListener.onTaskCaseListGet(true, mTaskCaseList);
		}
		return t;
	}


	//===================================
	//
	// AirTaskController.AirTaskListener
	//
	//===================================

	@Override
	public void onTaskOpr(boolean isOk, int opr, AirTask task)
	{
		if (task == null)
			isOk = false;

		if (opr == AirTaskController.TSK_CASE_OPR_CREATE)
		{
			AirTaskCase taskCase = null;
			if (isOk)
			{
				taskCase = new AirTaskCase();
				taskCase.setTaskId(task.getTaskCode());
				taskCase.setCaseCode(task.getTaskCase());
				taskCase.setCaseName(task.getTaskTitle());
				taskCase.setCarNo(task.getTaskCarNo());
				taskCase.setDetail(task.getTaskDescription());
				taskCase.setLocal(true);
				mTaskCaseList.add(0, taskCase);
				mDbProxy.TaskCaseNew(taskCase);
			}
			if (mAirTaskCaseListener != null)
				mAirTaskCaseListener.onTaskCaseCreated(isOk, taskCase);
		}
		else if (opr == AirTaskController.TSK_CASE_OPR_UPDATE)
		{
			AirTaskCase taskCase = null;
			if (isOk)
			{
				taskCase = getTask(task.getTaskCode());
				if (taskCase != null)
				{
					taskCase.setCaseCode(task.getTaskCase());
					taskCase.setCarNo(task.getTaskCarNo());
					taskCase.setCaseName(task.getTaskTitle());
					taskCase.setDetail(task.getTaskDescription());
					mDbProxy.TaskCaseUpdate(taskCase);
				}
				else
					isOk = false;
				if (mAirTaskCaseListener != null)
					mAirTaskCaseListener.onTaskCaseUpdated(isOk, taskCase);
			}

		}
		else if (opr == AirTaskController.TSK_CASE_OPR_DELETE)
		{
			/*
			AirTaskCase taskCase = null;
			if (isOk)
			{
				for (int i = 0; i < mTaskCaseList.size(); i ++)
				{
					if (TextUtils.equals(mTaskCaseList.get(i).getTaskId(), task.getTaskCode()))
					{
						if (mTaskCaseCurrent == mTaskCaseList.get(i))
							mTaskCaseCurrent = null;
						taskCase = mTaskCaseList.get(i);
						mTaskCaseList.remove(i);
						mDbProxy.TaskCaseDelete(task.getTaskCode());
						break;
					}
				}
			}

			if (mAirTaskCaseListener != null)
				mAirTaskCaseListener.onTaskCaseDeleted(isOk, taskCase);
			*/
		}
	}

	@Override
	public void onTaskListGet(boolean isOk, List<AirTask> tasks) {
		if (isOk)
		{
			mTaskCaseListReady = true;
			mTaskCaseList.clear();
			for (int i = 0; i < tasks.size(); i ++)
			{
				AirTaskCase t = new AirTaskCase();
				t.setTaskId(tasks.get(i).getTaskCode());
				t.setCaseName(tasks.get(i).getTaskTitle());
				t.setDetail(tasks.get(i).getTaskDescription());
				mTaskCaseList.add(t);
			}
		}
		if (mAirTaskCaseListener != null)
			mAirTaskCaseListener.onTaskCaseListGet(isOk, mTaskCaseList);
	}

	@Override
	public void onTaskState(boolean isOk, String taskCode) {
		//Nothing to do!
	}

	@Override
	public void onTaskContentListGet(boolean isOk, ArrayList<AirTaskReport> tasks, String taskCode) {
		//Nothing to do!
	}

	@Override
	public void onTaskDetailGet(boolean isOk, AirTaskDetail taskDetail, String taskCode) {
		//Nothing to do!
	}

	@Override
	public void onTaskAlarm(boolean isOk) {
		//Nothing to do!
	}
}
