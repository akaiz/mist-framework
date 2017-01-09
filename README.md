# MIST FRAMEWORK

### Steps to get you going

#### 1. Docker image creation

        cd mist-docker-container

        docker build -t akaiz/mist-docker .

#### 2. Start Camunda tomcat

        cd camunda-tomcat

        To start tomcat

        sh log-start-camunda.sh  

        To stop tomcat

        sh stop-camunda.sh  

#### 3. creation of the war file
      if you have already mist installed  run the following commands :

       cd mist-tomcat-app

       mvn install

       This above will generate a war file in the target  which we will use in the deployment for example
       we using mist-0.war

#### 4. Deployment
        cd mist-deployer

        docker build -t akaiz/mist-deployer .

        docker run -it akaiz/mist-deployer

        Then the deployer will be started and the following request can be made to it :

        * deploy      (which will deploy this war file to camunda tomcat and also it will take in your initial
          mist request values which are in the mist file )
         Post : localhost:8098/deploy

         Request Body  
         Required "war"="file path to war"
         Required "mist"="file path mist file with bpmn start request "

        You can use the mist-0.war in the root directory as the war file in
        case you didn't generate yours in step 3

        You can also use the post_data.txt in the root directory as mist file incase you don't have any

        * start  (it will send the start message to the deployed war)  
          Get  : localhost:8098/start

        * undeploy (which will un deploy this war file to camunda tomcat )
          Get  : localhost:8098/stop

#### 5.   Remark
         * For test purposes you can change the deviceId in the post file to any value beside 123 so
            that it can behave undesired and the other way is true  . Therefore by default it is 123


![alt text](bpmn.png)
