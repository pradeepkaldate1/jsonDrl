package com.drl.model;

import lombok.Data;

import java.util.List;

@Data
public class WhenCondition {
    private String fact;
    private Conditions conditions;

    @Data
    public static class Conditions{
        private List<All> all;
    }

    @Data
    public static class All{
        private List<Any> any;
    }

    @Data
    public static class Any{
        private String path;
        private String operator;
        private String value;
    }
}
