# Vibe

Vibe is a simplistic approach to Java application monitoring. Vibe can be used
to create probes, sample metrics and collect events for a JVM based application.
This information can then be exported over backends, allowing it to be accessed
over JMX, sent to services such as InfluxDB, logged or e-mailed.

Licensed under Apache 2.0.

```java
/*
 * Create an instance make metrics available over JMX.
 */
Vibe vibe = Vibe.builder()
  .withBackend(
    JmxBackend.builder().build()
  )
  .build();

/*
 * Export CPU usage of the JVM.
 */
vibe.export(JvmProbes.cpuUsage())
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

Probes can return any `Number` (such as integers, longs, floats, doubles etc),
`Boolean`, `String` or object that implements `Snapshot`.

#### `Probe`

`Probe` is a functional interface with one method that should return the
probed value. 

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

#### `SampledProbe`

`SampledProbe` works as a factory for instances of `Sampler` that perform the
actual sampling. This allows sampled probes to be exported over different
backends or used with several instances of `TimeSampler`.

A `Sampler` like a `Probe` is a functional interface that should return a 
single value.

```java
class ThreadPoolExecutorCompletedTasksProbe implements SampledProbe<Integer> {
  private final ThreadPoolExecutor executor;

  public ThreadPoolExecutorCompletedTasksProbe(ThreadPoolExecutor executor) {
    this.executor = executor;
  }

  public Sampler<Integer> create() {
    return new Sampler<Integer>() {
      private int lastTotal = 0;

      public Integer sample() {
        int total = executor.getCompletedTaskCount();
        int change = total - lastTotal;
        lastTotal = total;
        return change;
      }
    }
  }
}
```

In many cases a `SampledProbe` is easier to create via another `Probe` with
an operation applied to. For example the above class can be simplified to:

```java
Probe<Integer> totalCompletedTasks = executor::getCompletedTaskCount;

SampledProbe<Long> completedTasks = totalCompletedTasks.apply(Change.changeAsLong());
```

#### Multiple values

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
TimeSampler<?> sampler = TimeSampler.forProbe(sampledProbe)
  .withInterval(Duration.ofMinutes(1))
  .build();
```

It's possible to sample a probe as well using `Probe` by referencing it's
`read` method:

```java
Probe<Double> probe = ...;

TimeSampler<Double> sampler = TimeSampler.forProbe(probe::read)
  .build();
```

### Modifying probes and samplers

Probes and samplers can have operations applied to them to modify their
results. Vibe has operations available for things like calculating the
average of a time period, extract minimum and maximum values or detecting the
how much something changes.

```java
SampledProbe<Long> maxValue = probe.apply(Range.maxAsLong());

TimeSampler<Double> averageOverFiveMinutes = sampler
  .applyResampling(Average.averageOver(Duration.ofMinutes(5)));
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
used a health checks, such as checking every minute if average CPU usage over
the last 5 minutes is above a certain value:

```java
Check check = Check.builder()
  .whenProbe(JvmProbes.cpuUsage())
    .withCheckInterval(Duration.ofMinutes(1))
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
  .whenProbe(JvmProbes.cpuUsage())
    .withCheckInterval(Duration.ofMinutes(1))
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
  .build();
```

To use a timer:

```java
try(Stopwatch stopwatch = timer.start()) {
  // Code to measure here
}
```

Timers can either be exported directly or the `getSnapshotProbe` method can
be used to export snapshot information:

```java
vibe.export(timer.getSnapshotProbe())
  .at("http", "requests")
  .done();
```

Timers support estimating percentiles, the easiest way to activate this support
is via `withBuckets` that will sort timings into buckets:

```java
Timer timer = Timer.builder()
  .withBuckets(
    Duration.ofMillis(0), 
    Duration.ofMillis(50),
    Duration.ofMillis(100),
    Duration.ofMillis(200),
    Duration.ofMillis(500),
    Duration.ofMillis(1000)
  )
  .build();
```

## Exporting metrics

Probes, samplers, timers and other objects from Vibe can be exported over
different backends.

Create an instance of Vibe:

```java
Vibe vibe = Vibe.builder()
  .withBackend(
    LoggingBackend.builder().logSamples().build()
  )
  .withBackend(
    JmxBackend.builder().build()
  )
  .build();
```

Export using the `export` method:

```java
Export<SampledProbe<Double>> exportedCpuUsage = vibe.export(JvmProbes.cpuUsage())
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
