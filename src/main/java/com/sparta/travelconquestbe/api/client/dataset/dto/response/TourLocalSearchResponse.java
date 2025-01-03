package com.sparta.travelconquestbe.api.client.dataset.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class TourLocalSearchResponse {
  private Result result;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Result {
    private Body body;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Body {
      private List<RowContainer> rows;

      @Getter
      @Setter
      @NoArgsConstructor
      public static class RowContainer {
        private List<Row> row;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Row {
          private Number rowNum;
          private String opnSfTeamCode;
          private String mgtNo;
          private String opnSvcId;
          private String updateGbn;
          private String updateDt;
          private String bplcNm;
          private String siteWhlAddr;
          private String rdnWhlAddr;
          private String trdStateGbn;
          private String x;
          private String y;
        }
      }
    }
  }
}
