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

## Run locally
```bash
mvn spring-boot:run
```

or

```bash
mvn clean package  
java -Djava.library.path=/usr/local/opt/tomcat-native/lib -jar target/http-2-1.0.0-SNAPSHOT.jar
```

and test it

```bash
http http://localhost:8080                                                                     
HTTP/1.1 200 
Content-Length: 6
Content-Type: text/plain;charset=UTF-8
Date: Fri, 10 Nov 2017 18:12:44 GMT

hello!
```

- To debug
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
```