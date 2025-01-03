package com.sparta.travelconquestbe.common.batch.Policy;

import com.sparta.travelconquestbe.common.exception.BusinessStatusSkipException;
import com.sparta.travelconquestbe.common.exception.DuplicateLocationException;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

public class MultiSkipPolicy implements SkipPolicy {

  @Override
  public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
    // 데이터셋 내부 개행문자 등으로 형식깨짐
    if (t instanceof FlatFileParseException) {
      return true;
    }
    // 데이터셋 내부 개행문자 등으로 형식깨짐
    if (t instanceof ArrayIndexOutOfBoundsException) {
      return true;
    }
    // 영업중이 아닌 장소
    if (t instanceof BusinessStatusSkipException) {
      return true;
    }
    // 중복 장소
    if (t instanceof DuplicateLocationException) {
      return true;
    }
    return false;
  }
}
