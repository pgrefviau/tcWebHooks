package webhook.teamcity.executor;

import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.settings.WebHookConfig;

public class ResponsibilityChangedWebHookRunner extends AbstractWebHookExecutor implements WebHookRunner {
	
	private WebHookResponsibilityHolder responsibilityHolder;

	public ResponsibilityChangedWebHookRunner(
			WebHookPayloadManager webhookPayloadManager,
			WebHookContentBuilder webHookContentBuilder,
			WebHookHistoryRepository webHookHistoryRepository,
			WebHookHistoryItemFactory webHookHistoryItemFactory,
			WebHookConfig whc,
			BuildStateEnum state,
			boolean isOverrideEnabled,
			WebHook webhook,
			WebHookResponsibilityHolder responsibilityHolder,
			boolean isTest) 
	{
		super (webhookPayloadManager,
			 webHookContentBuilder,
			 webHookHistoryRepository,
			 webHookHistoryItemFactory,
			 whc,
			 state,
			 isOverrideEnabled,
			 webhook,
			 isTest);
		this.responsibilityHolder = responsibilityHolder;
	}

	@Override
	protected WebHook getWebHookContent() {
		return webHookContentBuilder.buildWebHookContent(webhook, whc, responsibilityHolder, state, overrideIsEnabled);
	}
	
	@Override
	protected WebHookHistoryItem buildWebHookHistoryItem(WebHookErrorStatus errorStatus) {
		if (this.isTest) {
			if (this.responsibilityHolder.getSBuildType() != null) {
				return webHookHistoryItemFactory.getWebHookHistoryTestItem(
						whc,
						webhook.getExecutionStats(), 
						this.responsibilityHolder.getSBuildType(),
						errorStatus
				);				
			} else {
				return webHookHistoryItemFactory.getWebHookHistoryTestItem(
						whc,
						webhook.getExecutionStats(), 
						this.responsibilityHolder.getSProject(),
						errorStatus
				);
			}
		} else {
			if (this.responsibilityHolder.getSBuildType() != null) {
				return webHookHistoryItemFactory.getWebHookHistoryItem(
						whc,
						webhook.getExecutionStats(), 
						this.responsibilityHolder.getSBuildType(),
						errorStatus
				);				
			} else {
				return webHookHistoryItemFactory.getWebHookHistoryItem(
						whc,
						webhook.getExecutionStats(), 
						this.responsibilityHolder.getSProject(),
						errorStatus
				);
			}
		}
	}

}
