# MIST FRAMEWORK

### Steps to get you going

#### 1. Docker image creation

        cd mist-docker-container

        docker build -t akaiz/mist-docker .

#### 2. Start Camunda tomcat

        cd camunda-tomcat

        To start tomcat

        sh log-start-camunda.sh    

#### 3. creation of the war file
      if you have already mist installed  run the following commands :

       cd mist-tomcat-app

       mvn install

       This above will generate a war file in the target  which we will use in the deployment for example we using mist-2.1.war

#### 4. Deployment
        cd mist-deployer

        copy and paste the war file here

        Then run the DeployManager java file  with either options
        * deploy      (which will deploy this war file to camunda tomcat )
        * undeploy      (which will un deploy this war file to camunda tomcat )

#### 5.Start  the bpmn by sending a post request below

        {"messageName" : "instantiationMessageOpenHab","businessKey" : "aBusinessKey","processVariables" : {"deviceId" : {"value" : "123","type": "String"} } }



![alt text](bpmn.png)
