package com.drl.controller;

import com.drl.service.JsonToDrlService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class JsonToDrlController {

    @Autowired
    private JsonToDrlService drlService;

    @PostMapping("/jsonToDrl")
    public String jsonToDrl(@RequestBody String json){
        System.out.println("Request: "+json);
        ObjectMapper mapper=new ObjectMapper();
        StringBuilder drl = new StringBuilder();
        try{
            JsonNode rootNode = mapper.readTree(json);


            if (rootNode.isArray()) {
                for (JsonNode ruleNode : rootNode) {
                    drl.append(drlService.jsonToDrl(ruleNode)+"\n");
                }
            }

            else {
                drl.append(drlService.jsonToDrl(rootNode)+"\n");
            }
        } catch (Exception e) {

        }
        return drl.toString();
    }
}
