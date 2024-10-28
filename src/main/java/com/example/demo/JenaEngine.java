/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.io.File;

import java.io.InputStream;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileManager;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author DO.ITSUDPARIS
 */
public class JenaEngine {

    static private String RDF = "http://www.rescuefood.org/ontologies/2024/rescue-food-ontology#";

    /**
     * Charger un mod�le � partir d�un fichier owl
     *
     * @param args + Entree: le chemin vers le fichier owl
     *             + Sortie: l'objet model jena
     */
    static public Model readModel(String inputDataFile) {
// create an empty model
        Model model = ModelFactory.createDefaultModel();

// use the FileManager to find the input file
        InputStream in = FileManager.get().open(inputDataFile);
        if (in == null) {
            System.out.println("Ontology file: " + inputDataFile + "not found");
            return null;
        }
// read the RDF/XML file
        model.read(in, "");

        try {
            in.close();
        } catch (IOException e) {
// TODO Auto-generated catch block
            return null;
        }
        return model;
    }

    /**
     * Faire l'inference
     *
     * @param args + Entree: l'objet model Jena avec le chemin du fichier de
     *             regles
     *             + Sortie: l'objet model infere Jena
     */
    static public Model readInferencedModelFromRuleFile(Model model,
                                                        String inputRuleFile) {
        InputStream in = FileManager.get().open(inputRuleFile);
        if (in == null) {
            System.out.println("Rule File: " + inputRuleFile + " not found");
            return null;
        } else {
            try {
                in.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                return null;
            }
        }
        List rules = Rule.rulesFromURL(inputRuleFile);
        GenericRuleReasoner reasoner = new
                GenericRuleReasoner(rules);
        reasoner.setDerivationLogging(true);
        reasoner.setOWLTranslation(true); // not needed in RDFS case
        reasoner.setTransitiveClosureCaching(true);
        InfModel inf = ModelFactory.createInfModel(reasoner, model);
        return inf;
    }

    /**
     * Executer une requete
     *
     * @param args + Entree: l'objet model Jena avec une chaine des caracteres
     *             SparQL
     *             + Sortie: le resultat de la requete en String
     */
    static public OutputStream executeQuery(Model model, String queryString) {
        Query query = QueryFactory.create(queryString);

        // Create an output stream to store the results
        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            public void write(int b) throws IOException {
                this.string.append((char) b);
            }

            public String toString() {
                return this.string.toString();
            }
        };

        // Execute the query based on its type
        try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
            if (query.isAskType()) {
                // For ASK queries, output true or false
                boolean result = qe.execAsk();
                output.write((result ? "true" : "false").getBytes());
            } else if (query.isSelectType()) {
                // For SELECT queries, output the results as JSON
                ResultSet results = qe.execSelect();
                ResultSetFormatter.outputAsJSON(output, results);
            } else if (query.isDescribeType()) {
                // For DESCRIBE queries, output the model as RDF/XML
                Model describeModel = qe.execDescribe();
                describeModel.write(output, "RDF/XML");
            } else if (query.isConstructType()) {
                // For CONSTRUCT queries, output the model as RDF/XML
                Model constructModel = qe.execConstruct();
                constructModel.write(output, "RDF/XML");
            } else {
                throw new IllegalArgumentException("Unsupported query type.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }


    /**
     * Execute an ASK query from a file
     *
     * @param model   the Jena model
     * @param filepath the path to the query file
     * @return the result of the ASK query as a boolean
     */
    static public boolean executeAskQueryFile(Model model, String filepath) {
        File queryFile = new File(filepath);
        InputStream in = FileManager.get().open(filepath);
        if (in == null) {
            System.out.println("Query file: " + filepath + " not found");
            return false;
        } else {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        String queryString = FileTool.getContents(queryFile);
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);

        boolean result = false;
        if (query.isAskType()) {
            result = qe.execAsk();
        } else {
            System.out.println("Query is not of ASK type");
        }
        qe.close();
        return result;
    }

}