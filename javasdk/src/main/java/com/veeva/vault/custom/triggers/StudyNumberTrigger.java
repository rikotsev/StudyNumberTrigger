package com.veeva.vault.custom.triggers;

import com.veeva.vault.sdk.api.core.ServiceLocator;
import com.veeva.vault.sdk.api.core.ValueType;
import com.veeva.vault.sdk.api.core.VaultCollections;
import com.veeva.vault.sdk.api.data.*;
import com.veeva.vault.sdk.api.query.QueryResponse;
import com.veeva.vault.sdk.api.query.QueryService;

import java.util.List;

@RecordTriggerInfo(object="study__v", events = {RecordEvent.BEFORE_INSERT})
public class StudyNumberTrigger implements RecordTrigger {

    @Override
    public void execute(RecordTriggerContext recordTriggerContext) {
        QueryService queryService = ServiceLocator.locate(QueryService.class);
        String studyId;

        while(true) {
            studyId = getGoodStudyId(queryService);

            final QueryResponse response = queryService.query("SELECT name__v FROM study__v WHERE name__v = '" + studyId + "'");

            if (response.getResultCount() == 0) {
                break;
            }
        }

        for(RecordChange inputRecord : recordTriggerContext.getRecordChanges()) {
            inputRecord.getNew().setValue("name__v", studyId);
        }

    }

    private String getGoodStudyId(final QueryService queryService) {
        RecordService recordService = ServiceLocator.locate(RecordService.class);
        final Record studyIdRecord = recordService.newRecord("study_id__c");
        final List<Record> newRecords = VaultCollections.newList();
        final List<String> savedRecordIds = VaultCollections.newList();
        newRecords.add(studyIdRecord);

        final String recordId = null;
        recordService.batchSaveRecords(newRecords).onSuccesses(success -> {
            success.stream().forEach(positionalRecordId -> {
                savedRecordIds.add(positionalRecordId.getRecordId());
            });
        }).ignoreErrors().execute();

        final QueryResponse response = queryService.query("SELECT name__v FROM study_id__c WHERE id = '" + savedRecordIds.stream().findAny().get() + "'");
        return response.streamResults().findFirst().get().getValue("name__v", ValueType.STRING);

    }
}
