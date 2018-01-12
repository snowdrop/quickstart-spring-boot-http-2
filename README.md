# HTTP/2

- Install Tomcat Native on Macos
```bash
brew install tomcat-native
```

- The library folder must be made available, if not already, to the JVM library
  path; this can be done with a JVM argument such as
  `-Djava.library.path=/usr/local/opt/tomcat-native/lib`

- Add the property `server.http2.enabled` to the application.properties file

- Declare a bean within the Application class to update the protocol and include `Http2Protocol`
```java
	@Bean
	public EmbeddedServletContainerCustomizer tomcatCustomizer() {
		return (container) -> {
			if (container instanceof TomcatEmbeddedServletContainerFactory) {
				((TomcatEmbeddedServletContainerFactory) container)
						.addConnectorCustomizers((connector) -> {
							connector.addUpgradeProtocol(new Http2Protocol());
						});
			}
		};
	}
```
- Add a second Apache Tomcat Connector to expose the standard Http protocol or Https using TLS/SSL

## Create self signed certificate
```bash
keytool -genkey -keyalg RSA -alias selfsigned -keystore src/main/keystore.jks -storepass secret -validity 360 -keysize 2048
What is your first and last name?
  [Unknown]:  localhost
What is the name of your organizational unit?
  [Unknown]:  Spring Boot 
What is the name of your organization?
  [Unknown]:  Red Hat
What is the name of your City or Locality?
  [Unknown]:  Florennes
What is the name of your State or Province?
  [Unknown]:  Namur
What is the two-letter country code for this unit?
  [Unknown]:  BE
Is CN=localhost, OU=Spring Boot, O=Red Hat, L=Florennes, ST=Namur, C=BE correct?
  [no]:  yes

Enter key password for <selfsigned>
        (RETURN if same as keystore password):  
Re-enter new password: 

Warning:The JKS keystore uses a proprietary format. It is recommended to migrate to PKCS12 which is an industry standard format using "keytool -importkeystore -srckeystore src/main/keystore.jks -destkeystore src/main/keystore.jks -deststoretype pkcs12".
```

## Run locally

In order to run spring Boot locally, it is required first to export the Apache tomcat Native Lib ti be used
```bash
export DYLD_LIBRARY_PATH=/usr/local/opt/tomcat-native/lib
mvn spring-boot:run
```

or

```bash
export DYLD_LIBRARY_PATH=/usr/local/opt/tomcat-native/lib
mvn clean package  && java -jar target/http-2-1.0.0-SNAPSHOT.jar
```

and test it

```bash
curl -v --http2 http://localhost:8080/

or  

curl -vk --http2 https://localhost:8443/
```

## JUnit Test
```bash
export DYLD_LIBRARY_PATH=/usr/local/opt/tomcat-native/lib
mvn clean test -Pjetty-client-http2             
```

## Deploy on OpenShift
```bash
export DYLD_LIBRARY_PATH=/usr/local/opt/tomcat-native/lib
mvn clean install fabric8:deploy -Popenshift -DskipTests=true 
```

## Issues

- Added AprListener to resolve this issue

```bash
2017-11-13 09:19:45.507 ERROR 18807 --- [           main] o.a.coyote.http11.Http11NioProtocol      : The upgrade handler [org.apache.coyote.http2.Http2Protocol] for [h2] only supports upgrade via ALPN but has been configured for the ["https-jsse-nio-8443"] connector that does not support ALPN.
```

- Use `export LD_LIBRARY_PATH=/usr/local/opt/tomcat-native/lib` to avoid this error: `UnsatisfiedLinkError: no tcnative-1 in java.library.path` is reported when Spring Boot starts

```bash
mvn spring-boot:run -Djava.library.path=/usr/local/opt/tomcat-native/lib
...
java.lang.UnsatisfiedLinkError: no tcnative-1 in java.library.path
        at java.lang.ClassLoader.loadLibrary(ClassLoader.java:1867) ~[na:1.8.0_151]
        at java.lang.Runtime.loadLibrary0(Runtime.java:870) ~[na:1.8.0_151]
        at java.lang.System.loadLibrary(System.java:1122) ~[na:1.8.0_151]
        at org.apache.tomcat.jni.Library.<init>(Library.java:42) ~[tomcat-embed-core-8.5.23.jar:8.5.23]
        at org.apache.tomcat.jni.Library.initialize(Library.java:178) ~[tomcat-embed-core-8.5.23.jar:8.5.23]
        at org.apache.catalina.core.AprLifecycleListener.init(AprLifecycleListener.java:198) [tomcat-embed-core-8.5.23.jar:8.5.23]
        
ll /usr/local/opt/tomcat-native/lib
total 744
-r--r--r--  1 dabou  admin   157K Nov 10 18:11 libtcnative-1.0.dylib
-r--r--r--  1 dabou  admin   207K Aug 29 16:14 libtcnative-1.a
lrwxr-xr-x  1 dabou  admin    21B Aug 29 16:14 libtcnative-1.dylib -> libtcnative-1.0.dylib
drwxr-xr-x  3 dabou  admin   102B Nov 10 18:11 pkgconfig   

The problem is described here : https://access.redhat.com/solutions/631953
```

## To debug
```bash
mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

## Trick to update Httpie tool to support http/2 protocol
```bash
pip install -U httpie httpie-http2

next, reopen the terminal and test

http https://nghttp2.org/httpbin/get
HTTP/2 200 

{
    "args": {}, 
    "headers": {
        "Accept": "*/*", 
        "Accept-Encoding": "gzip,deflate", 
        "Host": "nghttp2.org", 
        "User-Agent": "HTTPie/0.9.9", 
        "Via": "2 nghttpx"
    }, 
    "origin": "213.49.111.94", 
    "url": "https://nghttp2.org/httpbin/get"
}
```

- Using curl with http2 protocol

```bash
curl -vI --http2 http://localhost:8080/
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> HEAD / HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.56.1
> Accept: */*
> Connection: Upgrade, HTTP2-Settings
> Upgrade: h2c
> HTTP2-Settings: AAMAAABkAARAAAAAAAIAAAAA
>
< HTTP/1.1 101
HTTP/1.1 101
< Connection: Upgrade
Connection: Upgrade
< Upgrade: h2c
Upgrade: h2c
< Date: Sat, 11 Nov 2017 12:12:06 GMT
Date: Sat, 11 Nov 2017 12:12:06 GMT
* Received 101
* Using HTTP2, server supports multi-use
* Connection state changed (HTTP/2 confirmed)
* Copying HTTP/2 data in stream buffer to connection buffer after upgrade: len=0

* Connection state changed (MAX_CONCURRENT_STREAMS updated)!
< HTTP/2 200
HTTP/2 200
< content-type: text/plain;charset=UTF-8
content-type: text/plain;charset=UTF-8
< date: Sat, 11 Nov 2017 12:12:06 GMT
date: Sat, 11 Nov 2017 12:12:06 GMT

curl -v --http2 http://localhost:8080/ 
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET / HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.56.1
> Accept: */*
> Connection: Upgrade, HTTP2-Settings
> Upgrade: h2c
> HTTP2-Settings: AAMAAABkAARAAAAAAAIAAAAA
> 
< HTTP/1.1 101 
< Connection: Upgrade
< Upgrade: h2c
< Date: Mon, 13 Nov 2017 18:35:30 GMT
* Received 101
* Using HTTP2, server supports multi-use
* Connection state changed (HTTP/2 confirmed)
* Copying HTTP/2 data in stream buffer to connection buffer after upgrade: len=0
* Connection state changed (MAX_CONCURRENT_STREAMS updated)!
< HTTP/2 200 
< content-type: text/plain;charset=UTF-8
< date: Mon, 13 Nov 2017 18:35:30 GMT
< 
* Connection #0 to host localhost left intact
hello!%                                                 
```