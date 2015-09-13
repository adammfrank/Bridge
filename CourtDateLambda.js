console.log('Loading function');
var AWS = require('aws-sdk')

exports.handler = function(event, context) {
    //console.log('Received event:', JSON.stringify(event, null, 2));
    event.Records.forEach(function(record) {
        //console.log(record.identifier.Keys.identifier.S);
        //console.log(record.eventName);
        //console.log('DynamoDB Record: %j', record.dynamodb.Keys.identifier);
        console.log('DynamoDB Record2: %j', record.dynamodb.Keys.social.S);

        
        var params = {
            TableName: "PhoneNumbers",
            Key: {
                "social" : {
                    S: record.dynamodb.Keys.social.S
                }
            }
        }
    
    dynamoDB = new AWS.DynamoDB();
    console.log("about to retrieve item");
    dynamoDB.getItem(params, function(err, data) {
        console.log("getting item " + JSON.stringify(data));
        //console.log("was there an error? " + JSON.stringify(err));
        if(err) console.log(err, err.stack);
        else {
            //console.log("HERE IS SOME DATA " + data);
            var sns = new AWS.SNS();
            
            //console.log("Retreived Data " + data);
            
            var params = {
              Message: 'There is a warrant for your arrest',
              MessageAttributes: {
                  key1: {
                      DataType: 'String',
                      StringValue: 'Hi, Alex.'
                  }
              },
              Subject: 'You have a court date on ' + record.dynamodb.Keys.court_date + '.',
              TopicArn: data.Item.topic_arn.S
            };
            sns.publish(params, function(err, data){
                if(err) console.log(err, err.stack);
                else {
                    
                    context.succeed("Successfully processed " + event.Records.length + " records.");
                }
                
            })
        }
    });
        
    });
    
    
    
    
   
}