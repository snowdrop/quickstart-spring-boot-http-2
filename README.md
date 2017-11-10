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
mvn spring-boot:run -Djava.library.path=/usr/local/opt/tomcat-native/lib
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
mvn spring-boot:run -Djava.library.path=/usr/local/opt/tomcat-native/lib -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
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