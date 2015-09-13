console.log('Loading function');
var AWS = require('aws-sdk')

exports.handler = function(event, context) {
    event.Records.forEach(function(record) {

        console.log('DynamoDB Record2: %j', record.dynamodb.Keys.id.S);


        var params = {
            TableName: "PhoneNumbers",
            Key: {
                "social" : {
                    S: record.dynamodb.Keys.SSN.S
                }
            }
        }

        dynamoDB = new AWS.DynamoDB();
        console.log("about to retrieve item");
        dynamoDB.getItem(params, function(err, data) {
            console.log("getting item " + JSON.stringify(data));
            if(err) console.log(err, err.stack);
            else {
                var sns = new AWS.SNS();


                var params = {
                    Message: 'There is a warrant for your arrest',
                    MessageAttributes: {
                        key1: {
                            DataType: 'String',
                            StringValue: 'Hi, Alex.'
                        }
                    },
                    Subject: 'There is a warrant out for your arrest.',
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