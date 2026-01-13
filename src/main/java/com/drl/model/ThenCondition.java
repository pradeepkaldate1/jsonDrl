package com.drl.model;

import lombok.Data;

@Data
public class ThenCondition {
    private String log;
    private Modify modify;
    private ServiceCells serviceCells;

    public static class Modify{
        private String statuscaseStatusTypeCd;
        private String statuscaseStatusCd;
    }

    public static class ServiceCells{
        private String service;
        private String method;
    }
}
