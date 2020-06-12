/**
 * Classes related to {@link Check checks}. Checks are objects that can be used
 * to verify certain conditions and listen for changes on those conditions.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * Sampler<Double> cpuUsage = Sampler.forProbe(JvmProbes.sampledCpuUsage())
 *  .build();
 *
 * Check check = Check.builder()
 *   .whenSampler(cpuUsage)
 *     .apply(Average.averageOver(Duration.ofMinutes(5)))
 *     .is(Conditions.above(0.9))
 *   .build();
 *
 * check.addListener(event -> {
 *   if(event.isConditionsMet()) {
 *     // Conditions are currently met
 *   } else {
 *     // Conditions are not met
 *   }
 * });
 * </pre>
 *
 * @see se.l4.vibe.checks.Check
 * @see se.l4.vibe.Vibe#export(se.l4.vibe.Exportable)
 */
package se.l4.vibe.checks;
