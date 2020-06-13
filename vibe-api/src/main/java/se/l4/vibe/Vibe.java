package se.l4.vibe;

import java.util.Optional;

import se.l4.vibe.internal.VibeImpl;

/**
 * Main entry point to export statistics and events. An instance of {@link Vibe}
 * may be created using a {@link Builder} created via {@link #builder()}. When
 * building an instance the backends to be used must be specified, different
 * backends are that provide things such as {@link JmxBackend availability over JMX}
 * or {@link LoggingBackend logging of samples and events}.
 *
 * <p>
 * Example of building an instance:
 *
 * <pre>
 * Vibe vibe = Vibe.builder()
 *   .addBackend(
 *     LoggingBackend.builder().logSamples().build()
 *   )
 *   .build();
 * </pre>
 *
 * <h2>Exporting objects</h2>
 *
 * When an instance has been built it can be used to export statistics via
 * the {@link #export(Exportable)} method. This works for objects that
 * implement {@link Exportable} via {@link se.l4.vibe.probes.Probe},
 * {@link se.l4.vibe.sampling.SampledProbe}, {@link se.l4.vibe.sampling.TimeSampler},
 * {@link se.l4.vibe.timers.Timer}, {@link se.l4.vibe.events.Events} or
 * {@link se.l4.vibe.checks.Check}.
 *
 * <p>
 * Example exporting a CPU usage probe:
 *
 * <pre>
 * vibe.export(JvmProbes.cpuUsage())
 *   .at("jvm", "cpu")
 *   .export();
 * </pre>
 *
 * <h2>Scoping instances</h2>
 *
 * One useful feature of Vibe is that it's possible to create a scoped instance
 * via {@link #scope(String...)} that can be used to easily export objects with
 * a certain prefix.
 *
 * <pre>
 * // Create a scoped instance that prefixes all exports with jvm
 * Vibe jvmVibe = vibe.scope("jvm");
 * </pre>
 *
 * <p>
 * For libraries that want to support exporting metrics over Vibe it is
 * recommended to allow the user to specify an instance and path directly,
 * like:
 *
 * <pre>
 * public Builder withVibe(Vibe vibe, String... hierarchy) {
 *   this.vibe = vibe.scope(hierarchy);
 *   return this;
 * }
 * </pre>
 *
 * <p>
 * Always creating a scoped instance makes it easier if you ever want to remove
 * all exported objects such as when stopping a service.
 *
 * <h2>Destroying instances</h2>
 *
 * Instances can be destroyed using {@link #destroy()}. For the main instance
 * this will remove all exports and shut down the backends. For a scoped
 * instance this will remove all exported objects.
 */
public interface Vibe
{
	/**
	 * Start to export the given object. This will return a builder that
	 * can be used to define the path the object will be exported at.
	 *
	 * <p>
	 * Supports exporting {@link se.l4.vibe.probes.Probe},
	 * {@link se.l4.vibe.sampling.SampledProbe}, {@link se.l4.vibe.sampling.TimeSampler},
	 * {@link se.l4.vibe.timers.Timer}, {@link se.l4.vibe.events.Events} and
	 * {@link se.l4.vibe.checks.Check}.
	 *
	 * @param <T>
	 *   type of object being exported
	 * @param object
	 *   the object to export
	 * @return
	 *   builder for the export
	 */
	<T extends Exportable> ExportBuilder<T> export(T object);

	/**
	 * Create a Vibe instance for the given hierarchy. This creates an instance
	 * that exports objects prefixed using the hierarchy of the current instance
	 * plus the given instance.
	 *
	 * <p>
	 * The individual segments of the hierarchy will be merged with the
	 * character {@code /}.
	 *
	 * <p>
	 * <pre>
	 * // Create an instance using the prefix jvm
	 * Vibe jvmVibe = rootVibe.scope("jvm");
	 *
	 * // Create an instance that will have the prefix jvm/buffers
	 * Vibe buffers = jvmVibe.scope("buffers");
	 * </pre>
	 *
	 * @param hierarchy
	 *   the hierarchy to scope to
	 * @return
	 *   instance scoped to the given hierarchy
	 */
	Vibe scope(String... hierarchy);

	/**
	 * Destroy this Vibe instance. For the top level instance this will stop
	 * all backends, for scoped instances this will stop collection of any
	 * metrics exported via the scoped instance.
	 */
	void destroy();

	/**
	 * Start building a new instance.
	 *
	 * @return
	 */
	static Builder builder()
	{
		return new VibeImpl.BuilderImpl();
	}

	/**
	 * Builder for instances of {@link Vibe}.
	 */
	interface Builder
	{
		/**
		 * Add a backend to use with the {@link Vibe} instance.
		 *
		 * @param backend
		 * @return
		 */
		Builder addBackend(VibeBackend backend);

		/**
		 * Add a backend to use. This takes an optional that may be empty.
		 *
		 * @param backend
		 * @return
		 */
		Builder addBackend(Optional<VibeBackend> backend);

		/**
		 * Build the instance.
		 *
		 * @return
		 */
		Vibe build();
	}
}
