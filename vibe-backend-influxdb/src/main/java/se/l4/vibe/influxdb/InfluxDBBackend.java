package se.l4.vibe.influxdb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.event.EventListener;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.influxdb.internal.DataPoint;
import se.l4.vibe.influxdb.internal.DataQueue;
import se.l4.vibe.mapping.KeyValueMappable;
import se.l4.vibe.mapping.KeyValueReceiver;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.SampleListener;
import se.l4.vibe.probes.SampledProbe;
import se.l4.vibe.probes.Sampler;
import se.l4.vibe.timer.Timer;
import se.l4.vibe.timer.TimerListener;

/**
 * {@link VibeBackend Backend} that sends data to InfluxDB.
 *
 * @author Andreas Holstenson
 *
 */
public class InfluxDBBackend
	implements VibeBackend
{
	private static final Logger logger = LoggerFactory.getLogger(InfluxDBBackend.class);
	private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain");

	private final String url;
	private final String auth;
	private final Map<String, String> tags;

	private final OkHttpClient client;
	private final DataQueue queue;

	private final ScheduledExecutorService executor;

	private InfluxDBBackend(String url, String username, String password, String db, Map<String, String> tags)
	{
		this.tags = tags;
		client = new OkHttpClient();

		this.url = HttpUrl.parse(url).newBuilder()
			.addPathSegment("write")
			.addQueryParameter("db", db)
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
	public void export(String path, Sampler<?> sampler)
	{
		((Sampler) sampler).addListener(new SampleQueuer(path));
	}

	@Override
	public void export(String path, Probe<?> probe)
	{
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void export(String path, Events<?> events)
	{
		((Events) events).addListener(new EventQueuer(path));
	}

	@Override
	public void export(String path, Timer timer)
	{
		timer.addListener(new TimerQueuer(path));
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
		public void sampleAcquired(SampledProbe<Object> probe, Sampler.Entry<Object> entry)
		{
			Object value = entry.getValue();
			Map<String, Object> values = new HashMap<>();
			KeyValueReceiver receiver = (key, v) -> {
				if((v instanceof Double && Double.isNaN((Double) v))
					|| (v instanceof Float && Float.isNaN((Float) v)))
				{
					// Skip NaN values
					return;
				}

				values.put(key, v);
			};

			if(value instanceof KeyValueMappable)
			{
				((KeyValueMappable) value).mapToKeyValues(receiver);
			}
			else
			{
				receiver.add("value", value);
			}

			// TODO: Can a probe provide extra tags?

			DataPoint point = new DataPoint(path, entry.getTime(), tags, values);
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
		public void timerEvent(long now, long timeInNanoseconds)
		{
			HashMap<String, Object> map = new HashMap<>();
			map.put("value", timeInNanoseconds);

			DataPoint point = new DataPoint(path, now, tags, map);
			queue.add(point);
		}
	}

	private class EventQueuer
		implements EventListener<Object>
	{
		private final String path;

		public EventQueuer(String path)
		{
			this.path = path;
		}

		@Override
		public void eventRegistered(Events<Object> events, EventSeverity severity, Object event)
		{
			long time = System.currentTimeMillis();
			Map<String, Object> values = new HashMap<>();
			values.put("severity", severity);
			KeyValueReceiver receiver = (key, v) -> {
				if((v instanceof Double && Double.isNaN((Double) v))
					|| (v instanceof Float && Float.isNaN((Float) v)))
				{
					// Skip NaN values
					return;
				}

				values.put(key, v);
			};

			if(event instanceof KeyValueMappable)
			{
				((KeyValueMappable) event).mapToKeyValues(receiver);
			}
			else
			{
				receiver.add("value", event);
			}

			DataPoint point = new DataPoint(path, time, tags, values);
			queue.add(point);
		}
	}

	public static class Builder
	{
		private final Map<String, String> tags;

		private String url;
		private String username;
		private String password;

		private String db;

		public Builder()
		{
			tags = new HashMap<>();
		}

		/**
		 * Set the URL of the the InfluxDB instance.
		 *
		 * @param url
		 * @return
		 */
		public Builder setUrl(String url)
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
		public Builder setAuthentication(String username, String password)
		{
			this.username = username;
			this.password = password;
			return this;
		}

		public Builder setDatabase(String db)
		{
			this.db = db;
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
			Objects.requireNonNull(db, "Database to use is required");
			return new InfluxDBBackend(url, username, password, db, tags);
		}
	}

	public static Builder builder()
	{
		return new Builder();
	}
}
