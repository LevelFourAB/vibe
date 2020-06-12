module se.l4.vibe {
	requires transitive jdk.management;
	requires transitive org.slf4j;

	exports se.l4.vibe;

	exports se.l4.vibe.checks;
	exports se.l4.vibe.events;
	exports se.l4.vibe.snapshots;
	exports se.l4.vibe.operations;
	exports se.l4.vibe.percentiles;
	exports se.l4.vibe.probes;
	exports se.l4.vibe.sampling;
	exports se.l4.vibe.timers;
}
