# HTTP/2

- Install Tomcat Native on Macos
```bash
brew install tomcat-native
```

- The library folder must be made available, if not already, to the JVM library
  path; this can be done with a JVM argument such as
  `-Djava.library.path=/usr/local/opt/tomcat-native/lib`


- Add the property `server.http2.enabled` to the application.properties file

- Declare a bean sithin the Application class to update the protocol and include `Http2Protocol`
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

## Deploy project
```bash
mvn clean fabric8:deploy -Popenshift 
```