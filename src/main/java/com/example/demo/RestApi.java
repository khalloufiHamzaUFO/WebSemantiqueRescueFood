package com.example.demo;


import java.io.File;
import java.io.OutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestApi {

    Model model = JenaEngine.readModel("data/rescuefood.owl");


    @GetMapping("/avion")
    @CrossOrigin(origins = "http://localhost:3000")
    public String afficherAvion() {
        String NS = "";
        if (model != null) {

            NS = model.getNsPrefixURI("");


            Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            OutputStream res = JenaEngine.executeQuery(inferedModel, "data/query_Avion.txt");


            System.out.println(res);
            return res.toString();


        } else {
            return ("Error when reading model from ontology");
        }
    }
    @GetMapping("/bateau")
    @CrossOrigin(origins = "http://localhost:3000")
    public String afficherBateau() {
        String NS = "";
        if (model != null) {
            NS = model.getNsPrefixURI("");
            Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            // Load query as string
            File queryFile = new File("data/query_Bateau.txt");
            String queryString = FileTool.getContents(queryFile);
            Query query = QueryFactory.create(queryString);

            // Check the query type and execute accordingly
            if (query.isAskType()) {
                boolean result = JenaEngine.executeAskQueryFile(inferedModel, "data/query_Bateau.txt");
                return Boolean.toString(result);
            } else {
                OutputStream res = JenaEngine.executeQuery(inferedModel, queryString);
                System.out.println(res);
                return res.toString();
            }
        } else {
            return "Error when reading model from ontology";
        }
    }

    @GetMapping("/belgo")
    @CrossOrigin(origins = "http://localhost:3000")
    public String afficherbelgo() {
        String NS = "";
        if (model != null) {
            NS = model.getNsPrefixURI("");
            Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");

            // Load query as string
            File queryFile = new File("data/query_Bateau_Securite.txt");
            String queryString = FileTool.getContents(queryFile);
            Query query = QueryFactory.create(queryString);

            // Check the query type and execute accordingly
            if (query.isAskType()) {
                boolean result = JenaEngine.executeAskQueryFile(inferedModel, "data/query_Bateau_Securite.txt");
                return Boolean.toString(result);
            } else {
                OutputStream res = JenaEngine.executeQuery(inferedModel, queryString);
                System.out.println(res);
                return res.toString();
            }
        } else {
            return "Error when reading model from ontology";
        }
    }





}






