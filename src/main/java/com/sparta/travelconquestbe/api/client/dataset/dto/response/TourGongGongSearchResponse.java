package com.sparta.travelconquestbe.api.client.dataset.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TourGongGongSearchResponse {
  private Response response;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Response {
    private Body body;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Body {
      private List<Item> items;

      @Getter
      @Setter
      @NoArgsConstructor
      public static class Item {
        private String trrsrtNm;
        private String rdnmadr;
        private String lnmadr;
        private String latitude;
        private String longitude;
        private String referenceDate;
      }
    }
  }
}
