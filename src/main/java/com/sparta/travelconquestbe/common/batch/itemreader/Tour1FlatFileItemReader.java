package com.sparta.travelconquestbe.common.batch.itemreader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;

@RequiredArgsConstructor
public class Tour1FlatFileItemReader extends FlatFileItemReader<FieldSet> {

  public FlatFileItemReader<FieldSet> tour1flatFileItemReader() {
    FlatFileItemReader<FieldSet> reader = new FlatFileItemReader<>();
    reader.setLinesToSkip(1);
    reader.setEncoding("EUC-KR");

    DefaultLineMapper<FieldSet> lineMapper = new DefaultLineMapper<>();
    lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
    lineMapper.setFieldSetMapper(fieldSet -> fieldSet);
    reader.setLineMapper(lineMapper);
    return reader;
  }
}
