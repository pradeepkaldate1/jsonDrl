package com.drl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JsonToDrlService {

    @Autowired
    private  CustomerInfoNotPresent notPresent;

    public String jsonToDrl(JsonNode rootNode){
        ObjectMapper mapper=new ObjectMapper();
        StringBuilder stringBuilder=new StringBuilder();
        try {
//            JsonNode rootNode=mapper.readTree(json);
//            if(rootNode.isArray()){
//
//            }
            System.out.println("RootNode: "+rootNode);
            if(rootNode.has("ruleName")){
                stringBuilder.append("rule\t"+rootNode.get("ruleName").asText()+"\n");
            }
            if(rootNode.has("salience")){
                stringBuilder.append("salience\t"+rootNode.get("salience").asText()+"\n");
            }
            if(rootNode.has("activationGroup")){
                stringBuilder.append("activationGroup\t"+rootNode.get("activationGroup").asText()+"\n");
            }
            if (rootNode.has("noLoop")) {
                stringBuilder.append("no-loop ") .append(rootNode.get("noLoop").asBoolean())
                        .append("\n");
            }
            if(rootNode.has("when")){
                System.out.println("Fond when");
                if(rootNode.get("ruleName").asText().equals("Customer Information Not Present")){
                    String when = notPresent.buildWhen(rootNode.get("when"));
                    stringBuilder.append("\twhen\n\t" + when + "\n");
                }else {
                    String when = whenCondition(rootNode.get("when"));
                    stringBuilder.append("\twhen\n\t" + when + "\n");
                }

            }


            if(rootNode.has("then")){
                System.out.println("Fond then");
                if(rootNode.get("ruleName").asText().equals("Customer Information Not Present")){
                    String then = notPresent.buildThen(rootNode.get("then"));
                    stringBuilder.append("\t then\n\t" + then + "\n");
                }else {
                    String then = buildThenBlock(rootNode.get("then"));
                    stringBuilder.append("\t then\n\t" + then + "\n");
                }
            }
            stringBuilder.append("end\n");
            System.out.println("Drl: "+stringBuilder);
        } catch (Exception e) {
            System.out.println("Exception due to: "+e.getMessage());
        }
        return stringBuilder.toString();
    }

    public String whenCondition(JsonNode node){

        StringBuilder builder=new StringBuilder();
        if (node.has("fact")) {
            builder.append(buildSingleFact(node));
        }
        if (node.has("facts")) {

            for (JsonNode fact : node.get("facts")) {

                String factName = fact.get("fact").asText();
                String variableName = "$" + factName.toLowerCase();

                builder.append(variableName)
                        .append(" : ")
                        .append(factName)
                        .append("(");

                JsonNode conditions = fact.get("conditions");

                for (int i = 0; i < conditions.size(); i++) {
                    JsonNode condition = conditions.get(i);

                    String path = condition.get("path").asText();
                    String operator = condition.get("operator").asText();
                    String value = condition.get("value").asText();

                    builder.append(path)
                            .append(" ")
                            .append(toDroolsOperator(operator))
                            .append(" \"")
                            .append(value)
                            .append("\"");

                    if (i < conditions.size() - 1) {
                        builder.append(", ");
                    }
                }

                builder.append(")\n");
            }
        }

        return builder.toString();
    }
    private String toDroolsOperator(String operator) {
        switch (operator) {
            case "equals":
                return "==";
            case "notEquals":
                return "!=";
            case "greaterThan":
                return ">";
            case "lessThan":
                return "<";
            default:
                System.out.println("operator operator: "+operator);
                return "";
        }
    }
    private String buildSingleFact(JsonNode factNode) {

        StringBuilder builder = new StringBuilder();

        String fact = factNode.get("fact").asText();
        String variableName = "$" + fact.toLowerCase();

        builder.append("    ")
                .append(variableName)
                .append(" : ")
                .append(fact)
                .append("(");

        JsonNode conditionsNode = factNode.get("conditions");
        JsonNode conditionArray = null;

        if (conditionsNode.isArray()) {
            conditionArray = conditionsNode;
        } else if (conditionsNode.has("all")) {
            conditionArray = conditionsNode.get("all");
        } else if (conditionsNode.has("any")) {
            conditionArray = conditionsNode.get("any");
        }

        for (int i = 0; i < conditionArray.size(); i++) {
            JsonNode condition = conditionArray.get(i);

            if (condition.has("expression")) {
                builder.append(condition.get("expression").asText());
            }
            else if ("in".equals(condition.get("operator").asText())) {
                builder.append(condition.get("path").asText())
                        .append(" in (");

                for (int j = 0; j < condition.get("value").size(); j++) {
                    builder.append("\"")
                            .append(condition.get("value").get(j).asText())
                            .append("\"");

                    if (j < condition.get("value").size() - 1) {
                        builder.append(", ");
                    }
                }
                builder.append(")");
            }

            else if ("isNull".equals(condition.get("operator").asText())) {
                builder.append(condition.get("path").asText())
                        .append(" == null");
            }

            else {
                builder.append(condition.get("path").asText())
                        .append(" ")
                        .append(toDroolsOperator(condition.get("operator").asText()))
                        .append(" \"")
                        .append(condition.get("value").asText())
                        .append("\"");
            }

            if (i < conditionArray.size() - 1) {
                builder.append(" && ");
            }
        }

        builder.append(")\n");

        return builder.toString();
    }


    private String buildThenBlock(JsonNode thenNode) {

        StringBuilder builder = new StringBuilder();


        if (thenNode.has("modify")) {
            builder.append("    modify($case) {\n");

            JsonNode modifyNode = thenNode.get("modify");
            modifyNode.fields().forEachRemaining(entry -> {
                builder.append("        ")
                        .append(entry.getKey())
                        .append(" = \"")
                        .append(entry.getValue().asText())
                        .append("\",\n");
            });


            int lastComma = builder.lastIndexOf(",");
            if (lastComma != -1) {
                builder.deleteCharAt(lastComma);
            }

            builder.append("    }\n\n");
        }


        if (thenNode.has("serviceCalls")) {
            for (JsonNode serviceCall : thenNode.get("serviceCalls")) {
                builder.append("    ")
                        .append(serviceCall.get("service").asText())
                        .append(".")
                        .append(serviceCall.get("method").asText())
                        .append("($case)\n");
            }
        }

        return builder.toString();
    }

}
