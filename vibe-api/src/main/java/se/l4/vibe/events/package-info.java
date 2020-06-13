/**
 * Classes for collecting {@link Events events}. Events is a way to collect
 * important events in your system, such as start up messages, collecting
 * {@link Error}s or for emitting warnings via a {@link se.l4.vibe.checks.Check}.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * Events<EventDataClass> events = Events.<EventDataClass>builder()
 *   .withSeverity(EventSeverity.WARN)
 *   .build();
 *
 * events.addListener(event -> {
 *   System.out.println(event.getSeverity() + " " + event.toHumanReadable());
 * });
 *
 * events.register(new EventDataClass());
 * </pre>
 *
 * @see Events
 */
package se.l4.vibe.events;
