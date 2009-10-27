/**
 * 
 */
package webhook.teamcity.payload.format;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.teamcity.BuildState;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;

public class WebHookPayloadNameValuePairs implements WebHookPayload {
	
	SortedMap<String,Object> paramList;
	WebHookPayloadManager myManager;
	Integer rank = 100;
	String charset = "UTF-8";
	
	public WebHookPayloadNameValuePairs(WebHookPayloadManager manager){
		this.setPayloadManager(manager);
		paramList =  new TreeMap<String,Object>();
	}

	public void setPayloadManager(WebHookPayloadManager manager){
		myManager = manager;
	}
	
	public void register(){
		myManager.registerPayloadFormat(this);
	}
	
	public String getFormatDescription() {
		return "Name Value Pairs";
	}

	public String getFormatShortName() {
		return "nvpairs";
	}

	public String getFormatToolTipText() {
		return "Send the payload as a set of normal Name/Value Pairs";
	}

	
	public String beforeBuildFinish(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		this.addCommonParams(runningBuild, previousBuild, BuildState.BEFORE_BUILD_FINISHED);
		this.addMessageParam(runningBuild, BuildState.getDescriptionSuffix(BuildState.BEFORE_BUILD_FINISHED));
		paramList.putAll(extraParameters);
		return this.getStatusAsString();
	}

	public String buildChangedStatus(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String, String> extraParameters) {
		this.addCommonParams(runningBuild, previousBuild, BuildState.BUILD_CHANGED_STATUS);
		this.addMessageParam(runningBuild, "changed Status from "  + oldStatus.getText() + " to " + newStatus.getText());
		paramList.put("buildStatus", newStatus.getText());
		paramList.put("buildStatusPrevious", oldStatus.getText());
		paramList.putAll(extraParameters);
		return this.getStatusAsString();
	}

	public String buildFinished(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		this.addCommonParams(runningBuild, previousBuild, BuildState.BUILD_FINISHED);
		this.addMessageParam(runningBuild, BuildState.getDescriptionSuffix(BuildState.BUILD_FINISHED));
		paramList.putAll(extraParameters);
		return this.getStatusAsString();
	}

	public String buildInterrupted(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		this.addCommonParams(runningBuild, previousBuild, BuildState.BUILD_INTERRUPTED);
		this.addMessageParam(runningBuild, BuildState.getDescriptionSuffix(BuildState.BUILD_INTERRUPTED));
		paramList.putAll(extraParameters);
		return this.getStatusAsString();
	}

	public String buildStarted(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		this.addCommonParams(runningBuild, previousBuild, BuildState.BUILD_STARTED);
		this.addMessageParam(runningBuild, BuildState.getDescriptionSuffix(BuildState.BUILD_STARTED));
		paramList.putAll(extraParameters);
		return this.getStatusAsString();
	}

	public String responsibleChanged(SBuildType buildType,
			ResponsibilityInfo responsibilityInfoOld,
			ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
			SortedMap<String, String> extraParameters) {
		
		this.addCommonParams(buildType, BuildState.RESPONSIBILITY_CHANGED);
		
		
		paramList.put("oldResponsibility", responsibilityInfoOld.getUser().getDescriptiveName().toString());
		paramList.put("newResponsibility", responsibilityInfoOld.getUser().getDescriptiveName().toString());
		paramList.put("oldResponsibility1", responsibilityInfoOld.getUser().getDescriptiveName());
		paramList.put("newResponsibility1", responsibilityInfoOld.getUser().getDescriptiveName());

		paramList.put("message", "Build " + buildType.getFullName().toString()
				+ " has changed responsibility from " 
				+ responsibilityInfoOld.getUser().getDescriptiveName()
				+ " to "
				+ responsibilityInfoNew.getUser().getDescriptiveName()
			);
		
		paramList.put("text", buildType.getFullName().toString()
				+ " changed responsibility from " 
				+ responsibilityInfoOld.getUser().getUsername()
				+ " to "
				+ responsibilityInfoNew.getUser().getUsername()
			);

		paramList.put("comment", responsibilityInfoNew.getComment());
		paramList.putAll(extraParameters);
		return this.getStatusAsString();
	}

	private String getStatusAsString(){
		// TODO Need to convert this into POST payload.
		String returnString = ""; 
		if (paramList.size() > 0){
			for(Iterator<String> param = paramList.keySet().iterator(); param.hasNext();)
			{
				String key = param.next();
				String pair = "&";
				try {
					if (key != null){
						System.out.println(this.getClass().getSimpleName() + ": key is " + key);
						pair += URLEncoder.encode(key, this.charset);
						System.out.println(this.getClass().getSimpleName() + ": value is " + (String)paramList.get(key));
						if (paramList.get(key) != null){
							pair += "=" + URLEncoder.encode((String)paramList.get(key), this.charset);
						} else {
							pair += "=" + URLEncoder.encode("null", this.charset);
						}
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Need a better way to handle to string.
					e.printStackTrace();
					pair = "";
				} catch (ClassCastException e){
					// TODO Need a better way to handle to string.
					e.printStackTrace();
					pair = "";
				}
				returnString += pair;
				//Loggers.SERVER.debug(this.getClass().getSimpleName() + ": payload is " + returnString);
			}
		}
		
		Loggers.SERVER.debug(this.getClass().getSimpleName() + ": finished payload is " + returnString);
		if (returnString.length() > 0){
			return returnString.substring(1);
		} else {
			return returnString;
		}
	}

	private void addCommonParams(SRunningBuild sRunningBuild, SFinishedBuild previousBuild, Integer buildState) {
		paramList.put("buildStatus", sRunningBuild.getStatusDescriptor().getText());
		paramList.put("notifyType", BuildState.getShortName(buildState));
		paramList.put("buildRunner", sRunningBuild.getBuildType().getBuildRunner().getDisplayName());
		paramList.put("buildFullName", sRunningBuild.getBuildType().getFullName().toString());
		paramList.put("buildName", sRunningBuild.getBuildType().getName());
		paramList.put("buildTypeId", sRunningBuild.getBuildType().getBuildTypeId());
		paramList.put("buildId", String.valueOf(sRunningBuild.getBuildId()));
		paramList.put("projectName", sRunningBuild.getBuildType().getProjectName());
		paramList.put("projectId", sRunningBuild.getBuildType().getProjectId());
		paramList.put("buildNumber", sRunningBuild.getBuildNumber());
		paramList.put("agentName", sRunningBuild.getAgentName());
		paramList.put("agentOs", sRunningBuild.getAgent().getOperatingSystemName());
		paramList.put("agentHostname", sRunningBuild.getAgent().getHostName());
		paramList.put("triggeredBy", sRunningBuild.getTriggeredBy().getAsString());
		if (sRunningBuild.isFinished()){ 
			if (sRunningBuild.getStatusDescriptor().isSuccessful()){
				paramList.put("buildResult","success");
			} else {
				paramList.put("buildResult","failure");
			}
		} else {
			paramList.put("buildResult","running");
		}
		if (previousBuild.isFinished()){ 
			if (previousBuild.getStatusDescriptor().isSuccessful()){
				paramList.put("buildResultPrevious","success");
			} else {
				paramList.put("buildResultPrevious","failure");
			}
		} else {
			paramList.put("buildResultPrevious","running");
		}
	}
	
	private void addCommonParams(SBuildType buildType, Integer buildState) {
		paramList.put("notifyType", BuildState.getShortName(buildState));
		paramList.put("buildRunner", buildType.getBuildRunner().getDisplayName());
		paramList.put("buildFullName", buildType.getFullName().toString());
		paramList.put("buildName", buildType.getName());
		paramList.put("buildTypeId", buildType.getBuildTypeId());
		paramList.put("projectName", buildType.getProjectName());
		paramList.put("projectId", buildType.getProjectId());
	}
	
	private void addMessageParam(SRunningBuild sRunningBuild, String msgType){
		// Message is a long form message, for on webpages or in email.
		paramList.put("message", "Build " + sRunningBuild.getBuildType().getFullName().toString() 
				+ " has " + msgType + ". This is build number " + sRunningBuild.getBuildNumber() 
				+ ", has a status of \"" + sRunningBuild.getStatusDescriptor().getText() + "\" and was triggered by " + sRunningBuild.getTriggeredBy().getAsString());
		// Text is designed to be shorter, for use in Text messages and the like.
		paramList.put("text", sRunningBuild.getBuildType().getFullName().toString() 
				+ " has " + msgType + ". Status: " + sRunningBuild.getStatusDescriptor().getText());

	}

	public String getContentType() {
		return "application/x-www-form-urlencoded";
	}

	public Integer getRank() {
		return this.rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getCharset() {
		return this.charset;
	}

	
}
