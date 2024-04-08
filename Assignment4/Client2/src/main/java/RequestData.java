public class RequestData {
  private Long startTime;
  private String requestType;
  private Long latency;
  private Integer responseCode;

  public RequestData(Long startTime, String requestType, Long latency, Integer responseCode) {
    this.startTime = startTime;
    this.requestType = requestType;
    this.latency = latency;
    this.responseCode = responseCode;
  }

  public Long getStartTime() {
    return startTime;
  }

  public String getRequestType() {
    return requestType;
  }

  public Long getLatency() {
    return latency;
  }

  public Integer getResponseCode() {
    return responseCode;
  }
}
