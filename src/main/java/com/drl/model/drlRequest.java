package com.drl.model;

import lombok.Data;

@Data
public class drlRequest {

    private String ruleName;
    private String salience;
    private String activationGroup;
    private String timer;
    private String noLoop;
    private WhenCondition when;
    private ThenCondition then;
}
