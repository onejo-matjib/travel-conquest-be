package com.sparta.travelconquestbe.common.batch.itemreader;

import com.sparta.travelconquestbe.common.batch.util.BatchUtil;
import com.sparta.travelconquestbe.domain.locationdata.entity.LocationData;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.core.io.Resource;

@RequiredArgsConstructor
public class Tour2MultiResourceItemReader extends MultiResourceItemReader<LocationData> {
  private final BatchUtil batchUtil;

  @Override
  public void setResources(Resource[] resources) {
    super.setResources(resources);
    setDelegate(new Tour2FlatFileItemReader(batchUtil).tour2flatFileItemReader());
    setSaveState(true);
  }
}
