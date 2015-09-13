/*
 * Copyright 2012-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.util.*;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.util.encoders.Hex;
import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayOutputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.util.Tables;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class AmazonDynamoDBSample {

    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (~/.aws/credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WARNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

    static AmazonDynamoDBClient dynamoDB;
    static Set<String> set1;
    static Set<String> set2;
    static List<String> ssnList;
    static Map<String, String> ssnHashMap;
    static Map<String, String> saltHashMap;
    static Map<String, String> hashedssnHashMap;
    static int ssnCounter = 189999;

    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.ProfilesConfigFile
     * @see com.amazonaws.ClientConfiguration
     */
    private static void init() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = new AmazonDynamoDBClient(credentials);
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        dynamoDB.setRegion(usEast1);
    }

    public static void main(String[] args) throws Exception {
        init();

        try {
            String violationsTable = "violations-table";
            String citationsTable = "citations-table";
            String warrantsTable = "warrants-table";
            String contactsTable = "contacts-table";
            String notificationsTable = "notifications-table";

            // VIOLATIONS TABLE
            //
            //

            // Create violationsTable if it does not exist yet
            if (Tables.doesTableExist(dynamoDB, violationsTable)) {
                System.out.println("Table " + violationsTable + " is already ACTIVE");
            } else {
                // Create a table with a primary hash key named 'name', which holds a string
                CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(violationsTable)
                    .withKeySchema(new KeySchemaElement().withAttributeName("citation_number").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("citation_number").withAttributeType(ScalarAttributeType.N))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(25L).withWriteCapacityUnits(25L));
                    TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
                System.out.println("Created Table: " + createdTableDescription);

                // Wait for it to become active
                System.out.println("Waiting for " + violationsTable + " to become ACTIVE...");
                Tables.awaitTableToBecomeActive(dynamoDB, violationsTable);
            }

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(violationsTable);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

            // CITATIONS TABLE
            //
            //
            // Create citationsTable if it does not exist yet
            if (Tables.doesTableExist(dynamoDB, citationsTable)) {
                System.out.println("Table " + citationsTable + " is already ACTIVE");
            } else {
                // Create a table with a primary hash key named 'name', which holds a string
/*
                GlobalSecondaryIndex _firstLast = new GlobalSecondaryIndex()
                    .withIndexName("first_last")
                    .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 10)
                        .withWriteCapacityUnits((long) 1))
                        .withProjection(new Projection().withProjectionType(ProjectionType.ALL));

                GlobalSecondaryIndex _firstLastDOB = new GlobalSecondaryIndex()
                    .withIndexName("first_last_dob")
                    .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 10)
                        .withWriteCapacityUnits((long) 1))
                        .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
*/
                CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(citationsTable)
                    .withKeySchema(new KeySchemaElement().withAttributeName("first_last").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("first_last").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(25L).withWriteCapacityUnits(25L));
                    //.withGlobalSecondaryIndexes(_firstLast, _firstLastDOB);
                    TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
                System.out.println("Created Table: " + createdTableDescription);

                // Wait for it to become active
                System.out.println("Waiting for " + citationsTable + " to become ACTIVE...");
                Tables.awaitTableToBecomeActive(dynamoDB, citationsTable);
            }

            // Describe our new table
            describeTableRequest = new DescribeTableRequest().withTableName(citationsTable);
            tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

            // WARRANTS TABLE
            //
            //
            // Create warrantsTable if it does not exist yet
            if (Tables.doesTableExist(dynamoDB, warrantsTable)) {
                System.out.println("Table " + warrantsTable + " is already ACTIVE");
            } else {
/*
                GlobalSecondaryIndex _firstLastDOB = new GlobalSecondaryIndex()
                    .withIndexName("first_last_dob")
                    .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 10)
                        .withWriteCapacityUnits((long) 1))
                        .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
*/
                // Create a table with a primary hash key named 'name', which holds a string
                CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(warrantsTable)
                    .withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(25L).withWriteCapacityUnits(25L));
                    //.withGlobalSecondaryIndexes(_firstLastDOB);
                    TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
                System.out.println("Created Table: " + createdTableDescription);

                // Wait for it to become active
                System.out.println("Waiting for " + warrantsTable + " to become ACTIVE...");
                Tables.awaitTableToBecomeActive(dynamoDB, warrantsTable);
            }

            // Describe our new table
            describeTableRequest = new DescribeTableRequest().withTableName(warrantsTable);
            tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

            // CONTACTS TABLE
            //
            //

            // Create violationsTable if it does not exist yet
            if (Tables.doesTableExist(dynamoDB, contactsTable)) {
                System.out.println("Table " + contactsTable + " is already ACTIVE");
            } else {
                // Create a table with a primary hash key named 'name', which holds a string
                CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(contactsTable)
                    .withKeySchema(new KeySchemaElement().withAttributeName("first_last").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("first_last").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(25L).withWriteCapacityUnits(25L));
                    TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
                System.out.println("Created Table: " + createdTableDescription);

                // Wait for it to become active
                System.out.println("Waiting for " + contactsTable + " to become ACTIVE...");
                Tables.awaitTableToBecomeActive(dynamoDB, contactsTable);
            }

            // Describe our new table
            describeTableRequest = new DescribeTableRequest().withTableName(violationsTable);
            tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

            ssnHashMap = new HashMap<>();
            saltHashMap = new HashMap<>();
            hashedssnHashMap = new HashMap<>();

            ssnList = new ArrayList<>();

            for (int i = 1010001; i < 1200001; i++) {
                SocialSecurityNumber ssn = new SocialSecurityNumber(i);
                String ssnString = ssn.toString();
                ssnList.add(ssnString);
            }
/*
            for (String str : ssnList) {
                System.out.println("SSN: " + str);
            }
            */

            //oneTimeAddViolations();
            //oneTimeAddCitations();
            oneTimeAddWarrants();
            //oneTimeAddContacts();

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    //
    // ONE TIME ADD VIOLATIONS
    //
    //
    private static void oneTimeAddViolations() {

        String jsonString = "";

        try {
            jsonString = readFile("./json/violations.json", StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(e);
        }        

        try {
            JSONObject rootObject = new JSONObject(jsonString); // Parse the JSON to a JSONObject
            JSONArray rows = rootObject.getJSONArray("stuff"); // Get all JSONArray rows

            System.out.println("row lengths: " + rows.length());

            for(int j=0; j < rows.length(); j++) { // Iterate each element in the elements array
                JSONObject element =  rows.getJSONObject(j); // Get the element object

                int id = element.getInt("id");
                int citationNumber = element.getInt("citation_number");
                String violationNumber = element.getString("violation_number");
                String violationDescription = element.getString("violation_description");
                String warrantStatus = element.getString("warrant_status");
                String warrantNumber = " ";
                Boolean isWarrantNumberNull = element.isNull("warrant_number");
                if (!isWarrantNumberNull) warrantNumber = element.getString("warrant_number");
                String status = element.getString("status");
                String statusDate = element.getString("status_date");
                String fineAmount = " ";
                Boolean isFineAmountNull = element.isNull("fine_amount");
                if (!isFineAmountNull) fineAmount = element.getString("fine_amount");
                String courtCost = " ";
                Boolean isCourtCostNull = element.isNull("court_cost");
                if (!isCourtCostNull) courtCost = element.getString("court_cost");
/*
                Map<String, AttributeValue> item = newViolationItem(citationNumber, violationNumber, violationDescription, warrantStatus, warrantNumber, status, statusDate, fineAmount, courtCost);
                PutItemRequest putItemRequest = new PutItemRequest("violations-table", item);
                PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
                */
            }
        } catch (JSONException e) {
            // JSON Parsing error
            e.printStackTrace();
        }
    }

    //
    // ONE TIME ADD CITATIONS
    //
    //
    private static void oneTimeAddCitations() {

        String jsonString = "";

        try {
            jsonString = readFile("./json/citations.json", StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(e);
        }        

        try {
            JSONObject rootObject = new JSONObject(jsonString); // Parse the JSON to a JSONObject
            JSONArray rows = rootObject.getJSONArray("stuff"); // Get all JSONArray rows

            System.out.println("row lengths: " + rows.length());

            set1 = new HashSet<>();

            for(int j=0; j < rows.length(); j++) { // Iterate each element in the elements array
                JSONObject element =  rows.getJSONObject(j); // Get the element object

                int id = element.getInt("id");
                int citationNumber = element.getInt("citation_number");
                String citationDate = " ";
                Boolean isCitationDateNull = element.isNull("citation_date");
                if (!isCitationDateNull) citationDate = element.getString("citation_date");
                String firstName = element.getString("first_name");
                String lastName = element.getString("last_name");
                String firstLastName = firstName + lastName;
                firstLastName = firstLastName.toLowerCase();
                set1.add(firstLastName);

                //System.out.println(firstLastName);
                String dob = " ";
                Boolean isDobNull = element.isNull("date_of_birth");
                if (!isDobNull) {
                    dob = element.getString("date_of_birth");
                    dob = (dob.split(" "))[0];
                }

                // pick a ssn from list
                String ssn = ssnList.get(ssnCounter);
                ssnCounter--;
                ssnHashMap.put(firstLastName,ssn);

                System.out.println(firstLastName + " " + ssn);

                // compute salt
                final Random ran = new SecureRandom();
                byte[] salt = new byte[32];
                ran.nextBytes(salt);
                String saltString = Base64.encodeBase64String(salt);

                //System.out.println("saltstring: " + saltString);
                saltHashMap.put(firstLastName,saltString);

                // compute ripemd160 hash of ssn + salt
                String saltPlusSsn = saltString + ssn;
                //System.out.println("salt plus ssn: " + saltPlusSsn);

                String resultingHash = "";
                try {
                    byte[] r = saltPlusSsn.getBytes("US-ASCII");
                    RIPEMD160Digest d = new RIPEMD160Digest();
                    d.update (r, 0, r.length);
                    byte[] o = new byte[d.getDigestSize()];
                    d.doFinal (o, 0);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(40);
                    Hex.encode (o, baos);
                    resultingHash = new String(baos.toByteArray(), StandardCharsets.UTF_8);

                    hashedssnHashMap.put(firstLastName, resultingHash);
                } catch(UnsupportedEncodingException e) {
                    System.out.println(e);
                } catch(IOException i) {
                    System.out.println(i);
                } 

                String fldob = firstLastName + dob;
                String da = " ";
                Boolean isDaNull = element.isNull("defendant_address");
                if (!isDaNull) da = element.getString("defendant_address");
                String dc = " ";
                Boolean isDcNull = element.isNull("defendant_city");
                if (!isDcNull) dc = element.getString("defendant_city");
                String ds = " ";
                Boolean isDsNull = element.isNull("defendant_state");
                if (!isDsNull) ds = element.getString("defendant_state");
                String dln = " ";
                Boolean isDlnNull = element.isNull("drivers_license_number");
                if (!isDlnNull) dln = element.getString("drivers_license_number");
                String cd = " ";
                Boolean isCdNull = element.isNull("court_date");
                if (!isCdNull) cd = element.getString("court_date");
                String cl = " ";
                Boolean isClNull = element.isNull("court_location");
                if (!isClNull) cl = element.getString("court_location");
                String ca = " ";
                Boolean isCaNull = element.isNull("court_address");
                if (!isCaNull) ca = element.getString("court_address");
                /*
                Map<String, AttributeValue> item = newCitationItem(citationNumber, citationDate, firstName, lastName, firstLastName, dob, fldob, resultingHash, da, dc, ds, dln, cd, cl, ca);
                PutItemRequest putItemRequest = new PutItemRequest("citations-table", item);
                PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
                */
            }
        } catch (JSONException e) {
            // JSON Parsing error
            e.printStackTrace();
        }
    }

    //
    // ONE TIME ADD WARRANTS
    //
    //
    private static void oneTimeAddWarrants() {

        String jsonString = "";

        try {
            jsonString = readFile("./json/warrants.json", StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println(e);
        }        

        try {
            JSONObject rootObject = new JSONObject(jsonString); // Parse the JSON to a JSONObject
            JSONArray rows = rootObject.getJSONArray("stuff"); // Get all JSONArray rows

            //System.out.println("row lengths: " + rows.length());

            set2 = new HashSet<>();

            for(int j=0; j < rows.length(); j++) { // Iterate each element in the elements array
                JSONObject element =  rows.getJSONObject(j); // Get the element object

                String defendant = element.getString("Defendant");

                String strarr[] = defendant.split(" ");
                String temp = strarr[1];
                int len = strarr[0].length();
                strarr[0] = strarr[0].substring(0, len-1);
                strarr[1] = strarr[0];
                strarr[0] = temp;

                String firstLast = strarr[0] + strarr[1];
                firstLast = firstLast.toLowerCase();

                set2.add(firstLast);
                //System.out.println(firstLast);

                int zipCode = 0;
                Boolean isZipCodeNull = element.isNull("ZIP Code");
                if (!isZipCodeNull) zipCode = element.getInt("ZIP Code");
                String dob = element.getString("Date of Birth");
                String caseNumber = element.getString("Case Number");

                String firstLastDOB = firstLast + dob;

                // pick a ssn from list
                String ssn = ssnList.get(ssnCounter);
                ssnCounter--;
                ssnHashMap.put(firstLast,ssn);

                // compute salt
                final Random ran = new SecureRandom();
                byte[] salt = new byte[32];
                ran.nextBytes(salt);
                String saltString = Base64.encodeBase64String(salt);

                //System.out.println("saltstring: " + saltString);
                saltHashMap.put(firstLast,saltString);

                // compute ripemd160 hash of ssn + salt
                String saltPlusSsn = saltString + ssn;
                //System.out.println("salt plus ssn: " + saltPlusSsn);

                String resultingHash = "";
                try {
                    byte[] r = saltPlusSsn.getBytes("US-ASCII");
                    RIPEMD160Digest d = new RIPEMD160Digest();
                    d.update (r, 0, r.length);
                    byte[] o = new byte[d.getDigestSize()];
                    d.doFinal (o, 0);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(40);
                    Hex.encode (o, baos);
                    resultingHash = new String(baos.toByteArray(), StandardCharsets.UTF_8);

                    hashedssnHashMap.put(firstLast, resultingHash);
                } catch(UnsupportedEncodingException e) {
                    System.out.println(e);
                } catch(IOException i) {
                    System.out.println(i);
                } 

                //compareNames();

                Map<String, AttributeValue> item = newWarrantItem(firstLast, firstLastDOB, resultingHash, defendant, zipCode, dob, caseNumber);
                PutItemRequest putItemRequest = new PutItemRequest("warrants-table", item);
                PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
                
            }
        } catch (JSONException e) {
            // JSON Parsing error
            e.printStackTrace();
        }
    }

    //
    // ONE TIME ADD CONTACTS
    //
    //
    private static void oneTimeAddContacts() {

        String firstNameLastName = "";
        String saltS = "";
        String ssnhash = "";

        Set<String> unionSet = new HashSet<>();
        unionSet.addAll(set1);
        unionSet.addAll(set2);

        String strArrr[] = unionSet.toArray(new String[unionSet.size()]);

        int numEntries = saltHashMap.size();

        for (int i = 0; i < numEntries; i++) {
            firstNameLastName = strArrr[i];
            saltS = saltHashMap.get(strArrr[i]);
            ssnhash = hashedssnHashMap.get(strArrr[i]);

            Map<String, AttributeValue> item = newContactItem(firstNameLastName, saltS, ssnhash);
            PutItemRequest putItemRequest = new PutItemRequest("contacts-table", item);
            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
        }
    }

    private static Map<String, AttributeValue> newViolationItem(int cn, String vn, String vd, String ws, String wn, String s, String sd, String fa, String cc) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("citation_number", new AttributeValue().withN(Integer.toString(cn)));
        item.put("violation_number", new AttributeValue(vn));
        item.put("violation_description", new AttributeValue(vd));
        item.put("warrant_status", new AttributeValue(ws));
        item.put("warrant_number", new AttributeValue(wn));
        item.put("status", new AttributeValue(s));
        item.put("status_date", new AttributeValue(sd));
        item.put("fine_amount", new AttributeValue(fa));
        item.put("court_cost", new AttributeValue(cc));

        return item;
    }

    private static Map<String, AttributeValue> newCitationItem(int cn, String cda, String fn, String ln, String fln, String dob, String fldob, String ssn, String da, String dc, String ds, String dln, String cd, String cl, String ca) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("citation_number", new AttributeValue().withN(Integer.toString(cn)));
        item.put("citation_date", new AttributeValue(cda));
        item.put("first_name", new AttributeValue(fn));
        item.put("last_name", new AttributeValue(ln));
        item.put("first_last", new AttributeValue(fln));
        item.put("date_of_birth", new AttributeValue(dob));
        item.put("first_last_dob", new AttributeValue(fldob));
        item.put("ssn", new AttributeValue(ssn));
        item.put("defendant_address", new AttributeValue(da));
        item.put("defendant_city", new AttributeValue(dc));
        item.put("defendant_state", new AttributeValue(ds));
        item.put("drivers_license_number", new AttributeValue(dln));
        item.put("court_date", new AttributeValue(cd));
        item.put("court_location", new AttributeValue(cl));
        item.put("court_address", new AttributeValue(ca));

        return item;
    }

    private static Map<String, AttributeValue> newWarrantItem(String k, String fld, String ssn, String d, int zc, String dob, String cn) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("id", new AttributeValue().withS(k));
        item.put("First + Last + DOB", new AttributeValue(fld));
        item.put("SSN", new AttributeValue(ssn));
        item.put("Defendant", new AttributeValue(d));
        item.put("ZIP Code", new AttributeValue().withN(Integer.toString(zc)));
        item.put("Date of Birth", new AttributeValue(dob));
        item.put("Case Number", new AttributeValue(cn));        

        return item;
    }

    private static Map<String, AttributeValue> newContactItem(String flnn, String salt, String ssn) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("first_last", new AttributeValue().withS(flnn));
        item.put("salt", new AttributeValue(salt));
        item.put("ssn", new AttributeValue(ssn));        

        return item;
    }

    static String readFile(String path, Charset encoding) throws IOException 
    {
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      return new String(encoded, encoding);
    }

    private static void compareNames() {
        for (String str : set1) {
            if (set2.contains(str)) System.out.println(str);
        }
    }

}