package com.drl.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class CustomerInfoNotPresent {
    public String buildWhen(JsonNode whenNode) {

        String fact = whenNode.get("fact").asText();
        String variableName = "$" + fact.toLowerCase();

        StringBuilder builder = new StringBuilder();
        builder.append("    ")
                .append(variableName)
                .append(" : ")
                .append(fact)
                .append("(");
        if(whenNode.get("conditions") != null) {
            builder.append(buildConditionTree(whenNode.get("conditions")));
        }
        builder.append(")\n");
        return builder.toString();
    }

    private String buildConditionTree(JsonNode node) {

        if (node.has("all")) {
            StringBuilder sb = new StringBuilder("(");
            JsonNode all = node.get("all");

            for (int i = 0; i < all.size(); i++) {
                sb.append(buildConditionTree(all.get(i)));
                if (i < all.size() - 1) sb.append(" && ");
            }
            sb.append(")");
            return sb.toString();
        }
        if (node.has("any")) {
            StringBuilder sb = new StringBuilder("(");
            JsonNode any = node.get("any");

            for (int i = 0; i < any.size(); i++) {
                sb.append(buildConditionTree(any.get(i)));
                if (i < any.size() - 1) sb.append(" || ");
            }
            sb.append(")");
            return sb.toString();
        }

        return buildLeafCondition(node);
    }

    private String buildLeafCondition(JsonNode condition) {

        String path = condition.get("path").asText();
        String operator = condition.get("operator").asText();

        switch (operator) {
            case "equals":
                return path + " == \"" + condition.get("value").asText() + "\"";
            case "isNull":
                return path + " == null";
            case "notNull":
                return path + " != null";
            default:
                System.out.println("Invalid operator : "+operator);
                return "";
        }
    }
    public String buildThen(JsonNode thenNode) {

        StringBuilder builder = new StringBuilder();

        if (thenNode.has("log")) {
            builder.append(" log.info(\"")
                    .append(thenNode.get("log").asText())
                    .append("\");\n\n");
        }

        if (thenNode.has("modify")) {
            builder.append("    modify($case) {\n");

            JsonNode modify = thenNode.get("modify");
            modify.fields().forEachRemaining(entry -> {
                builder.append("        ")
                        .append(entry.getKey())
                        .append(" = \"")
                        .append(entry.getValue().asText())
                        .append("\",\n");
            });

            int lastComma = builder.lastIndexOf(",");
            if (lastComma != -1) builder.deleteCharAt(lastComma);

            builder.append("    };\n\n");
        }

        if (thenNode.has("serviceCalls")) {
            for (JsonNode sCell : thenNode.get("serviceCalls")) {
                builder.append("    ")
                        .append(sCell.get("service").asText())
                        .append(".")
                        .append(sCell.get("method").asText())
                        .append("($case);\n");
            }
        }

        return builder.toString();
    }

}
