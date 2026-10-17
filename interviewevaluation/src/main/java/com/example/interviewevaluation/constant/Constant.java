package com.example.interviewevaluation.constant;

import org.springframework.stereotype.Service;

@Service 
public class Constant {
  public Integer PENDING_STATUS = 1;
  public Integer SUCCEEDD_STATUS = 2;
  public Integer FIRST_ROUND = 1;
  public Integer SECOND_ROUND = 2;
  // A candidate whose round-1 score is above this gets a round-2 (coding round) link
  public Double ROUND_TWO_PASS_SCORE = 6.0;
  // Same validity window as the round-1 interview link (7200 minutes = 5 days)
  public Long LINK_EXPIRY_MINUTES = 7200L;
}
