Vibe is a simplistic approach to Java application monitoring. 

Licensed under Apache 2.0.

## Using

The latest version is **0.2**.

Using Maven:

```xml
<dependency>
	<groupId>se.l4.vibe</groupId>
	<artifactId>vibe-api</artifactId>
	<version>0.2</version>
</dependency>
```

## Creating a new instance

First create a Vibe instance:

```java
Vibe vibe = DefaultVibe.builder()
	.setSampleInterval(10, TimeUnit.SECONDS)
	.build();
```

## Sampling a probe

Export a few things:

```java
vibe.sample(JvmProbes.cpuUsage())
	.at("jvm/cpu")
	.export();
```

## Handle events

```java
Events<UnauthorizedAccess> events = vibe.events(UnauthorizedAccess.class)
	.severity(EventSeverity.WARN)
	.create();

events.register(new UnauthorizedAccess(someImportantInfo));
```

## Automatically trigger events

Time series can have triggers:


```java
vibe.sample(JvmProbes.cpuUsage())
	.at("jvm/cpu")
	.when(averageOver(5, TimeUnit.MINUTES), above(0.9))
		.sendEvent(EventSeverity.CRITICAL)
	.export();
```

This will trigger a critical event (at the same path as the series) if the average CPU usage over five minutes exceeds 90%.

It is possible to use a trigger on an automatically calculated value:

```java
vibe.timeSeries(JvmProbes.totalUsedMemory())
	.at("jvm/mem/total")
	.when(changeAsFraction(), on(averageOver(5, TimeUnit.MINUTES)), above(0.1))
		.sendEvent(EventSeverity.WARN)
	.export();
```

This will create a trigger that will be activated if the average value over 5 minutes changes more than 10%.

## Timing calls

Vibe supports timing of actions, such as monitoring the time it takes for your application to handle a request.

To create a timer:
```java
Timer timer = vibe.timer()
	.at("web/requests")
	.withBuckets(0, 50, 100, 200, 500, 1000) // for calculating percentiles
	.export();
```

To use a timer:
```java
Stopwatch stopwatch = timer.start();
try {
	// Code to measure here
} finally {
	stopwatch.stop();
}
```

## System probes

Vibe can monitor the system it runs on via the use of [Sigar](http://www.hyperic.com/products/sigar).

```xml
<dependency>
	<groupId>se.l4.vibe</groupId>
	<artifactId>vibe-sigar</artifactId>
	<version>current version</version>
</dependency>
```

Create probes for the system via `SigarProbes`

## Send e-mail on events

When you have started sampling some data with triggers for those you can
configure an e-mail backend so that you will receive notifications when
things go wrong.

First include the backend:

```xml
<dependency>
	<groupId>se.l4.vibe</groupId>
	<artifactId>vibe-backend-mail</artifactId>
	<version>current version</version>
</dependency>
``` 

Build a backend with the builder:

```java
MailBackend backend = MailBackend.builder()
	.setSender("system@example.org")
	.setSmtpServer("smtp.example.org")
	.setSubject("{severity} event for {path}")
	.setMinimumSeverity(EventSeverity.WARN)
	.addRecipient("sysadmin@example.org")
	.build();
```

## Sending data to InfluxDB

Samples, timers and events can be exported to InfluxDB.


Dependency:
```xml
<dependency>
	<groupId>se.l4.vibe</groupId>
	<artifactId>vibe-backend-influxdb</artifactId>
	<version>current version</version>
</dependency>
``` 

```java
VibeBackend backed = InfluxDBBackend.builder()
	.setUrl("http://localhost:8086")
	.setDatabase("metrics")
	.setAuthentication("user", "password")
	.addTag("host", "server-1")
	.build();
```

## Other notes

The current development version can also be accessed via a Maven snapshot
repository. Include the following in your POM:

If you want to you can include this repository to use snapshot releases:

```xml
<repository>
	<releases>
		<enabled>false</enabled>
	</releases>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
	<id>sonatype-nexus-snapshots</id>
	<name>Sonatype Nexus Snapshots</name>
	<url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```
