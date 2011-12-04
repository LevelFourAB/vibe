Vibe is a simplistic approach to Java application monitoring. 

Licensed under Apache 2.0.

## Current version

There is currently no release available. See Other Notes for how you can
use snaphots.

Using Maven:

```xml
<dependency>
	<groupId>se.l4.vibe</groupId>
	<artifactId>vibe-api</artifactId>
	<version>version</version>
</dependency>
```

## Creating a new instance

First create a Vibe instance:

```java
Vibe vibe = DefaultVibe.builder()
	.setSampleInterval(10, TimeUnit.SECONDS)
	.build();
```

## Exporting probes

Export a few things:

```java
vibe.timeSeries(JvmProbes.cpuUsage())
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
vibe.timeSeries(JvmProbes.cpuUsage())
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