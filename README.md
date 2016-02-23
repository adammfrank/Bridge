Bridge

A project demonstrating a web application aimed at the civil sector.
Built for GlobalHack5. Received First Place, College Division. 

Bridge allows anyone to look themselves up in a database of municipal citations, violations, and warrants.
It's simple design is aimed at users with minimal computer experience and limited internet access.
Users can sign up for a text message service which notifies them if a warrant has been issued in their name.
This streamlines the fine collection process, and keeps nonviolent offenders out of jail, where they can continue
to contribute to society.
Texts are sent through Amazon SNS whenever a matching name is added to the table of warrants in DynamoDB.

Technology: Nodejs, Express, Backbone, Java, AWS (Lambda, DynamoDB, SNS, EC2), SASS

To run locally:
Install Nodejs
Inside /api, run "npm install"
Run "npm start"
Navigate to http://localhost:3000 in your browser

Team: Who Broke The Build?

Contributors:
Adam Frank,
Nikolai Laba,
Curtis Meuth,
Josh Epstein,
Alex Bolinsky
