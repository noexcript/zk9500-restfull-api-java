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


# Endpoints for API

POST http://localhost:8080/fingerprint/open -> To open/turn device 

POST http://localhost:8080/fingerprint/enroll -> To Enroll fingeprint after 3 times of scanning 

POST http://localhost:8080/fingerprint/verify-fingerprint -> To verify fingerprint save on cache

POST http://localhost:8080/fingerprint/register -> To register fingerprint after enrolled

POST http://localhost:8080/fingerprint/close -> To close/turn off device


