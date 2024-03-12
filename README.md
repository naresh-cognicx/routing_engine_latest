# routing-engine
Routing Engine

11/24/2022:

Enabled Maven and spring profile to load environment specific properties.
Maven profile to supply the spring.profiles.active at compile time without any modification to the properties file as they are maintained separate for each environment. Default is local, others - dev.

Build command for dev is eg: <b> mvn clean install -Pdev </b> , no need to mention -P for local as it is the default profile.

While running as spring boot application, specify the profile in the run window. default is local.

General properties are placed in application.properties and the environment properties have to be specified in the application-{env}.properties. eg: application-dev.properties will have the dev specific properties.