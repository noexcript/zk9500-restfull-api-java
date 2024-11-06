### Rest API for  Fingerprint reader device ZK9500

#### Before run this Rest API, you need install some of tools on yout local machine
* [Z9500 SDK FOR WINDOWS](https://server.zkteco.eu/ddfb/zkfinger_sdk_v10.0-windows-lite-zk9500.zip)
* [JAVA SDK](https://www.oracle.com/java/technologies/downloads/)
* [Apache Maven](https://maven.apache.org/download.cgi) 


If you are using VSCode, I recommend you install

* # Extension Pack for Java

After install all tools above run some code on terminal on main directory, to install all dependencies

```bash
 $ mvn clean package
```

# Dependencies used
 ```xml
    <dependencies>
        <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>3.8.1</version>
        <scope>test</scope>
        </dependency>
        <dependency>
        <groupId>org.glassfish.jersey.containers</groupId>
        <artifactId>jersey-container-grizzly2-http</artifactId>
        <version>2.35</version>
        </dependency>
        <dependency>
        <groupId>org.glassfish.jersey.core</groupId>
        <artifactId>jersey-server</artifactId>
        <version>2.35</version>
        </dependency>
        <dependency>
        <groupId>org.glassfish.jersey.inject</groupId>
        <artifactId>jersey-hk2</artifactId>
        <version>2.35</version>
        </dependency>
        <dependency>
        <groupId>org.glassfish.jersey.containers</groupId>
        <artifactId>jersey-container-servlet-core</artifactId>
        <version>2.35</version>
        </dependency>
        <dependency>
        <groupId>com.zk</groupId>
        <artifactId>ZKFingerReader</artifactId>
        <version>1.0.0</version>
        </dependency>
        <dependency>
        <groupId>io.socket</groupId>
        <artifactId>socket.io-client</artifactId>
        <version>2.1.1</version>
        </dependency>
        <dependency>
        <groupId>com.corundumstudio.socketio</groupId>
        <artifactId>netty-socketio</artifactId>
        <version>1.7.22</version>
        </dependency>
        <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.79.Final</version>
        </dependency>
        <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.8.7</version>
        </dependency>
        <dependency>
        <groupId>org.glassfish.jersey.media</groupId>
        <artifactId>jersey-media-json-jackson</artifactId>
        <version>2.35</version>
        </dependency>
        <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.12.5</version>
        </dependency>

        <dependency>
        <groupId>org.glassfish.jersey.media</groupId>
        <artifactId>jersey-media-json-processing</artifactId>
        <version>2.42</version>
        </dependency>
    </dependencies>
 ```


# Endpoints for API

POST http://localhost:8080/fingerprint/open -> To open/turn device 

POST http://localhost:8080/fingerprint/enroll -> To Enroll fingeprint after 3 times of scanning 

POST http://localhost:8080/fingerprint/verify-fingerprint -> To verify fingerprint save on cache

POST http://localhost:8080/fingerprint/register -> To register fingerprint after enrolled

POST http://localhost:8080/fingerprint/close -> To close/turn off device


