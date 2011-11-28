Vibe is a simplistic approach to Java application monitoring. 

Currently it only logs data, but JMX support is planned.

Licensed under Apache 2.0.

## Example

First create a Vibe instance:
```
Vibe vibe = DefaultVibe.builder()
	.setSampleInterval(10, TimeUnit.SECONDS)
	.build();
```

Export a few things:
```
vibe.timeSeries(RuntimeProbes.getCpuUsage())
	.at("sys/cpu")
	.export();
```

Handle events:
```
Events<UnauthorizedAccess> events = vibe.events(UnauthorizedAccess.class)
	.severity(EventSeverity.WARN)
	.create();

events.register(new UnauthorizedAccess(someImportantInfo));
```
