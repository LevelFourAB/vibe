module se.l4.vibe {
	requires transitive jdk.management;
	requires transitive org.slf4j;

	exports se.l4.vibe;
	exports se.l4.vibe.backend;

	exports se.l4.vibe.check;
	exports se.l4.vibe.events;
	exports se.l4.vibe.mapping;
	exports se.l4.vibe.operations;
	exports se.l4.vibe.percentile;
	exports se.l4.vibe.probes;
	exports se.l4.vibe.sampling;
	exports se.l4.vibe.timer;
}
