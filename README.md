# Vibe

Vibe is a simplistic approach to Java application monitoring. Vibe can be used
to create probes, sample metrics and collect events for a JVM based application.
This information can then be exported over backends, allowing it to be accessed
over JMX, sent to services such as InfluxDB, logged or e-mailed.

Licensed under Apache 2.0.

```java
/*
 * Create an instance that will log samples and make metrics available over
 * JMX.
 */
Vibe vibe = Vibe.builder()
  .withBackend(LoggingBackend.logSamples().build())
  .withBackend(JmxBackend.builder().build())
  .build();

/*
 * Sample CPU usage using the default interval (10 seconds) and export it.
 * The logging backend will log the value every minute and it will be available
 * over JMX.
 */
vibe
  .export(JvmProbes.cpuUsage())
  .at("jvm", "cpu")
  .done();
```

## Using

The latest version is **0.4.0-SNAPSHOT**.

Using Maven:

```xml
<dependency>
  <groupId>se.l4.vibe</groupId>
  <artifactId>vibe-api</artifactId>
  <version>0.4.0-SNAPSHOT</version>
</dependency>
```

## Probes and sampling

Probes are the most basic building block in Vibe. Probes can be created to
read any type of value and come into two variants, `Probe` are probes that 
directly read a value and `SampledProbe` that require sampling to be able to
read a value.

### Standard probes

Vibe comes with a few standard probes, such as JVM probes via the class
`JvmProbes`:

```java
SampledProbe<Double> cpuUsage = JvmProbes.cpuUsage();
Probe<Double> heapUsage = JvmProbes.heapMemoryAsFraction();
```

### Implementing a probe

Both `Probe` and `SampledProbe` are functional interfaces with one method,
the difference being that a regular probe can be read whenever while a sampled
probe must be used via `Sampler`. Probes can return any `Number`, `String` or
object that implements `KeyValueMappable`.

A small probe that reads a single value might look like this:

```java
class ThreadPoolExecutorActiveCountProbe implements Probe<Integer> {
  private final ThreadPoolExecutor executor;

  public ThreadPoolExecutorActiveCountProbe(ThreadPoolExecutor executor) {
    this.executor = executor;
  }

  public Integer read() {
    return executor.getActiveCount();
  }
}
```

Or more simply:

```java
ThreadPoolExecutor executor = ...;

Probe<Integer> probe = executor::getActiveCount;
```

For probes that read several values `Probe.merged()` and `SampleProbe.merged()`
can be used to create probes that read several values:

```java
ThreadPoolExecutor executor = ...;

Probe<KeyValueMap> probe = Probe.merged()
  .add("maxSize", executor::getMaximumPoolSize)
  .add("size", executor:getPoolSize)
  .add("active", executor::getActiveCount)
  .build();
```

### Sampling a probe

Sampling of a probe is done via `Sampler`. An instance of `Sampler` will collect
samples at a certain interval (10 seconds by default) if it is has a listener,
if it's exported or used by another Vibe object.

Example:

```java
Sampler<?> sampler = Sampler.forProbe(sampledProbe)
  .withInterval(Duration.ofMinutes(1))
  .build();
```

It's possible to sample a probe as well using `Probe` by referencing it's
`read` method:

```java
Probe<Double> probe = ...;

Sampler<Double> sampler = Sampler.forProbe(probe::read)
  .build();
```

### Modifying probes and samplers

Probes and samplers can have operations applied to them to modify their
results. Vibe has operations available for things like calculating the
average of a time period, extract minimum and maximum values or detecting the
how much something changes.

```java
Sampler<Double> averageOverFiveMinutes = sampler
  .apply(Average.averageOver(Duration.ofMinutes(5)));
```

These modified probes and samplers can then be exported or used to create
checks.

## Events

Events are useful to send specific information to other systems via a backend.
The `LoggingBackend` will log these events, but `MailBackend` will deliver try
to deliver events over e-mail and `InfluxDBBackend` will push them to an
InfluxDB server.

```java
Events<UnauthorizedAccess> events = Events.<UnauthorizedAccess>builder()
  .withSeverity(EventSeverity.WARN)
  .create();

events.register(new UnauthorizedAccess(someImportantInfo));
```

## Checks

Checks are objects that can report if their conditions are met. These can be
used a health checks, such as checking if CPU usage over 5 minutes is above
a certain value:

```java
Sampler<Double> cpuUsage = Sampler.forProbe(JvmProbes.cpuUsage())
  .build();

Check check = Check.builder()
  .when(cpuUsage)
    .apply(Average.averageOver(Duration.ofMinutes(5)))
    .is(Conditions.above(0.9))
  .build();
```

Checks have listeners that will trigger whenever they change state:

```java
check.addListener(event -> {
  if(event.isConditionsMet()) {
    // Check is passing, so CPU usage is above 90% for the last 5 minutes
    events.register(...);
  } else {
    // Check is not passing
    events.register(...);
  }
});
```

The default behavior of `Check` is to only trigger an event when the state
of conditions being met changes, but checks can also repeat events:

```java
Check check = Check.builder()
  .when(cpuUsage)
    .apply(Average.averageOver(Duration.ofMinutes(5)))
    .is(Conditions.above(0.9))
  .whenMetRepeatEvery(Duration.ofMinutes(30))
  .build();
```

The above example will repeat the event every 30 minutes as long as conditions
are met, which is useful for scenarios where you might want to send reminders.
`whenUnmetRepeatEvery` can be used to do the same for when conditions are not
met.

## Timing calls

Vibe supports timing of actions, such as monitoring the time it takes for your
application to handle a request.

To create a timer:

```java
Timer timer = Timer.builder()
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

## Exporting metrics

Creating probes, samplers, timers and so on is only half the work. These metrics
should then be exported.

Create an instance of Vibe:

```java
Vibe vibe = Vibe.builder()
  .withBackend(LoggingBackend.logSamples().build())
  .withBackend(JmxBackend.builder().build())
  .build();
```

Export using the `export` method:

```java
Export<Sampler<Double>> exportedCpuUsage = vibe.export(cpuUsageSampler)
  .at("jvm", "cpu")
  .done();
```

If the thing you exported is no longer needed it can be removed via `remove`:

```java
exportedCpuUsage.remove();
```

### Send e-mail on events

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
  .withSender("system@example.org")
  .withSmtpServer("smtp.example.org")
  .withSubject("{severity} event for {path}")
  .withMinimumSeverity(EventSeverity.WARN)
  .addRecipient("sysadmin@example.org")
  .build();
```

### Sending data to InfluxDB

Samples, timers and events can be exported to InfluxDB.


Dependency:
```xml
<dependency>
  <groupId>se.l4.vibe</groupId>
  <artifactId>vibe-backend-influxdb</artifactId>
  <version>current version</version>
</dependency>
``` 

For InfluxDB 1.x:

```java
VibeBackend backed = InfluxDBBackend.builder()
  .withUrl("http://localhost:8086")
  .withAuthentication("user", "password")
  .addTag("host", "server-1")
  .v1()
    .withDatabase("metrics")
    .done()
  .build();
```

For InfluxDB 2.x:

```java
VibeBackend backed = InfluxDBBackend.builder()
  .withUrl("http://localhost:9999")
  .withAuthentication("user", "password")
  .addTag("host", "server-1")
  .v2()
    .withBucket("metrics")
    .withOrganization("org")
    .done()
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
