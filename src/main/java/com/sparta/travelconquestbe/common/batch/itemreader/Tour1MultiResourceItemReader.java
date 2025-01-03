package com.sparta.travelconquestbe.common.batch.itemreader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.Resource;

@RequiredArgsConstructor
public class Tour1MultiResourceItemReader extends MultiResourceItemReader<FieldSet> {

    @Override
    public void setResources(Resource[] resources) {
        super.setResources(resources);
        setDelegate(new Tour1FlatFileItemReader().tour1flatFileItemReader());
        setSaveState(true);
    }
}