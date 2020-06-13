package se.l4.vibe.influxdb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import se.l4.vibe.Handle;
import se.l4.vibe.VibeBackend;
import se.l4.vibe.events.Event;
import se.l4.vibe.events.EventData;
import se.l4.vibe.events.EventListener;
import se.l4.vibe.events.Events;
import se.l4.vibe.influxdb.internal.DataPoint;
import se.l4.vibe.influxdb.internal.DataQueue;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.sampling.Sample;
import se.l4.vibe.sampling.SampleListener;
import se.l4.vibe.sampling.SampledProbe;
import se.l4.vibe.sampling.TimeSampler;
import se.l4.vibe.snapshots.KeyValueReceiver;
import se.l4.vibe.snapshots.Snapshot;
import se.l4.vibe.timers.Timer;
import se.l4.vibe.timers.TimerEvent;
import se.l4.vibe.timers.TimerListener;

/**
 * {@link VibeBackend Backend} that sends data to InfluxDB.
 *
 * <pre>
 * VibeBackend backed = InfluxDBBackend.builder()
 *   .withUrl("http://localhost:8086")
 *   .withAuthentication("user", "password")
 *   .addTag("host", "server-1")
 *   .v1()
 *     .withDatabase("metrics")
 *     .done()
 *   .build();
 * </pre>
 */
public class InfluxDBBackend
	implements VibeBackend
{
	private static final Logger logger = LoggerFactory.getLogger(InfluxDBBackend.class);
	private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain");

	private final Duration samplingInterval;

	private final String url;
	private final String auth;
	private final Map<String, String> tags;

	private final OkHttpClient client;
	private final DataQueue queue;

	private final ScheduledExecutorService executor;

	private InfluxDBBackend(
		Duration samplingInterval,
		String url,
		String username,
		String password,
		Map<String, String> params,
		Map<String, String> tags
	)
	{
		this.samplingInterval = samplingInterval;
		this.tags = tags;
		client = new OkHttpClient();

		HttpUrl.Builder builder = HttpUrl.parse(url)
			.newBuilder()
			.addPathSegment("write");

		for(Map.Entry<String, String> e : params.entrySet())
		{
			builder = builder.addQueryParameter(e.getKey(), e.getValue());
		}

		this.url = builder
			.addQueryParameter("precision", "ms")
			.build()
			.toString();

		if(username != null)
		{
			auth = "Basic " + Base64.getMimeEncoder().encodeToString((username + ':' + password).getBytes(StandardCharsets.UTF_8));
		}
		else
		{
			auth = null;
		}

		executor = Executors.newScheduledThreadPool(1, new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread thread = new Thread(r);
				thread.setName("InfluxDB[" + url + "]");
				return thread;
			}
		});
		queue = new DataQueue(this::send, executor);
	}

	private void send(String data)
	{
		Request.Builder builder = new Request.Builder()
			.url(url)
			.post(RequestBody.create(MEDIA_TYPE, data));

		if(auth != null)
		{
			builder.addHeader("Authorization", auth);
		}

		Request request = builder.build();
		try
		{
			Response response = client.newCall(request)
				.execute();

			response.body().close();

			if(response.code() < 200 || response.code() >= 300)
			{
				logger.warn("Unable to store values; Got response code " + response.code());
				throw new RuntimeException("Failed sending");
			}
		}
		catch(IOException e)
		{
			logger.warn("Unable to store values; " + e.getMessage(), e);
			throw new RuntimeException("Failed sending; " + e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Handle export(String path, TimeSampler<?> sampler)
	{
		return ((TimeSampler) sampler).addListener(new SampleQueuer(path));
	}

	@Override
	public Handle export(String path, Probe<?> probe)
	{
		return sampleAndExport(path, SampledProbe.over(probe));
	}

	@Override
	public Handle export(String path, SampledProbe<?> probe)
	{
		return sampleAndExport(path, probe);
	}

	private Handle sampleAndExport(String path, SampledProbe<?> probe)
	{
		TimeSampler<?> sampler = TimeSampler.forProbe(probe)
			.withInterval(samplingInterval)
			.build();

		return export(path, sampler);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Handle export(String path, Events<?> events)
	{
		return ((Events) events).addListener(new EventQueuer(path));
	}

	@Override
	public Handle export(String path, Timer timer)
	{
		return timer.addListener(new TimerQueuer(path));
	}

	@Override
	public void close()
	{
		queue.close();

		executor.shutdown();

		try
		{
			executor.awaitTermination(5, TimeUnit.SECONDS);
		}
		catch(InterruptedException e)
		{
		}
	}

	private class SampleQueuer
		implements SampleListener<Object>
	{
		private final String path;

		public SampleQueuer(String path)
		{
			this.path = path;
		}

		@Override
		public void sampleAcquired(Sample<Object> sample)
		{
			Object value = sample.getValue();
			Map<String, Object> values = new HashMap<>();
			KeyValueReceiver receiver = createReceiver(values);

			if(value instanceof Snapshot)
			{
				((Snapshot) value).mapToKeyValues(receiver);
			}
			else
			{
				receiver.add("value", value);
			}

			// TODO: Can a probe provide extra tags?

			DataPoint point = new DataPoint(path, sample.getTime(), tags, values);
			queue.add(point);
		}

	}

	private class TimerQueuer
		implements TimerListener
	{
		private final String path;

		public TimerQueuer(String path)
		{
			this.path = path;
		}

		@Override
		public void timingComplete(TimerEvent event)
		{
			HashMap<String, Object> map = new HashMap<>();
			map.put("value", event.getDuration());

			DataPoint point = new DataPoint(path, System.currentTimeMillis(), tags, map);
			queue.add(point);
		}
	}

	private class EventQueuer
		implements EventListener<EventData>
	{
		private final String path;

		public EventQueuer(String path)
		{
			this.path = path;
		}

		@Override
		public void eventRegistered(Event<EventData> event)
		{
			long time = System.currentTimeMillis();
			Map<String, Object> values = new HashMap<>();
			values.put("severity", event.getSeverity());

			KeyValueReceiver receiver = createReceiver(values);

			EventData data = event.getData();
			if(data instanceof Snapshot)
			{
				((Snapshot) data).mapToKeyValues(receiver);
			}
			else
			{
				receiver.add("value", data.toHumanReadable());
			}

			DataPoint point = new DataPoint(path, time, tags, values);
			queue.add(point);
		}
	}

	protected KeyValueReceiver createReceiver(Map<String, Object> values)
	{
		return (key, v) -> {
			if((v instanceof Double && Double.isNaN((Double) v))
				|| (v instanceof Float && Float.isNaN((Float) v)))
			{
				// Skip NaN values
				return;
			}

			values.put(key, v);
		};
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private final Map<String, String> tags;

		private Duration samplingInterval;
		private String url;
		private String username;
		private String password;

		private Map<String, String> queryParams;

		public Builder()
		{
			tags = new HashMap<>();
			samplingInterval = Duration.ofSeconds(10);
		}

		/**
		 * Set the sampling interval this backend should use for {@link Probe}s
		 * and {@link SampledProbe}s.
		 *
		 * @param interval
		 *   interval to use
		 * @return
		 */
		public Builder withSamplingInterval(Duration interval)
		{
			Objects.requireNonNull(interval, "interval can not be null");
			this.samplingInterval = interval;

			return this;
		}

		/**
		 * Set the URL of the the InfluxDB instance.
		 *
		 * @param url
		 * @return
		 */
		public Builder withUrl(String url)
		{
			this.url = url;
			return this;
		}

		/**
		 * Set the authentication to use to connect to InfluxDB.
		 *
		 * @param username
		 * @param password
		 * @return
		 */
		public Builder withAuthentication(String username, String password)
		{
			this.username = username;
			this.password = password;
			return this;
		}

		/**
		 * Setup this backend for use with the InfluxDB 1.x series.
		 *
		 * @return
		 */
		public InfluxDB1 v1()
		{
			return new InfluxDB1(this::receiveParams);
		}

		/**
		 * Setup this backend for use with the InfluxDB 2.x series.
		 *
		 * @return
		 */
		public InfluxDB2 v2()
		{
			return new InfluxDB2(this::receiveParams);
		}

		protected Builder receiveParams(Map<String, String> params)
		{
			this.queryParams = params;
			return this;
		}

		/**
		 * Add a tag to this instance. This is useful to provide information
		 * about the host or anything else that is shared by everything in
		 * this Vibe instance.
		 *
		 * @param key
		 * @param value
		 * @return
		 */
		public Builder addTag(String key, String value)
		{
			tags.put(key, value);
			return this;
		}

		/**
		 * Build the instance.
		 *
		 * @return
		 */
		public VibeBackend build()
		{
			Objects.requireNonNull(url, "URL to InfluxDB is required");
			Objects.requireNonNull(queryParams, "V1 or V2 must be selected");
			return new InfluxDBBackend(
				samplingInterval,

				url,
				username,
				password,
				queryParams,
				tags
			);
		}
	}

	public static class InfluxDB1
	{
		private final Function<Map<String, String>, Builder> resultReceiver;

		private String database;

		public InfluxDB1(
			Function<Map<String, String>, Builder> resultReceiver
		)
		{
			this.resultReceiver = resultReceiver;
		}

		public InfluxDB1 withDatabase(String database)
		{
			this.database = database;
			return this;
		}

		public Builder build()
		{
			Objects.requireNonNull(database, "database must be set");

			return resultReceiver.apply(Collections.singletonMap("db", database));
		}
	}

	public static class InfluxDB2
	{
		private final Function<Map<String, String>, Builder> resultReceiver;

		private String bucket;
		private String organization;

		public InfluxDB2(
			Function<Map<String, String>, Builder> resultReceiver
		)
		{
			this.resultReceiver = resultReceiver;
		}

		public InfluxDB2 withBucket(String bucket)
		{
			this.bucket = bucket;
			return this;
		}

		public InfluxDB2 withOrganization(String organization)
		{
			this.organization = organization;
			return this;
		}

		public Builder build()
		{
			Objects.requireNonNull(bucket, "bucket must be set");
			Objects.requireNonNull(organization, "organization must be set");

			return resultReceiver.apply(Map.of(
				"bucket", bucket,
				"org", organization
			));
		}
	}
}
