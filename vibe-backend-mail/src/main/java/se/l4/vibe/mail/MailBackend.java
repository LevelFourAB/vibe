package se.l4.vibe.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import se.l4.vibe.backend.VibeBackend;
import se.l4.vibe.event.EventListener;
import se.l4.vibe.event.EventSeverity;
import se.l4.vibe.event.Events;
import se.l4.vibe.probes.Probe;
import se.l4.vibe.probes.Sampler;
import se.l4.vibe.timer.Timer;

/**
 * Backend that will e-mail events.
 *
 * @author Andreas Holstenson
 *
 */
public class MailBackend
	implements VibeBackend
{
	private final EventSeverity minimumSeverity;

	private final ExecutorService executor;

	private final String from;
	private final String receivers[];
	private final String subject;

	private final AuthenticatorImpl authenticator;

	private final String smtpServer;
	private final int smtpPort;
	private final boolean tls;
	private final boolean ssl;

	public MailBackend(
			EventSeverity minimumSeverity,
			String smtpServer,
			int smtpPort,
			String from,
			String[] receivers,
			String subject,
			boolean tls,
			boolean ssl,
			AuthenticatorImpl authenticator)
	{
		this.minimumSeverity = minimumSeverity;
		this.smtpServer = smtpServer;
		this.smtpPort = smtpPort;
		this.from = from;
		this.receivers = receivers;
		this.subject = subject;
		this.tls = tls;
		this.ssl = ssl;
		this.authenticator = authenticator;

		executor = Executors.newCachedThreadPool(new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				return new Thread(r, "vibe-mail");
			}
		});
	}

	/**
	 * Stop the mail backend.
	 */
	public void stop()
	{
		executor.shutdown();
	}

	private void send(String path, long time, EventSeverity severity, Object event)
		throws MessagingException
	{
		Properties props = System.getProperties();
		props.put("mail.smtp.host", smtpServer);
		props.put("mail.smtp.port", smtpPort);

		if(tls)
		{
			props.put("mail.smtp.starttls.enable", "true");
		}
		else if(ssl)
		{
			props.put("mail.smtp.socketFactory.port", smtpPort);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}

		if(authenticator != null)
		{
			props.setProperty("mail.smtp.submitter", authenticator
				.getPasswordAuthentication()
				.getUserName());
			props.setProperty("mail.smtp.auth", "true");
		}

		Session session = Session.getDefaultInstance(props, authenticator);

		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));

		InternetAddress[] receivers = new InternetAddress[this.receivers.length];
		for(int i=0, n=receivers.length; i<n; i++)
		{
			receivers[i] = new InternetAddress(this.receivers[i]);
		}

		msg.setRecipients(Message.RecipientType.TO, receivers);

		msg.setSubject(subject
			.replace("{severity}", severity.toString())
			.replace("{path}", path)
		);

		StringBuilder body = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		body
			.append(path)
			.append(": The following ")
			.append(severity)
			.append(" event was received at ")
			.append(sdf.format(new Date(time)))
			.append(":\n\n")
			.append(event);

		msg.setText(body.toString());

		msg.setHeader("X-Mailer", "Vibe");
		msg.setHeader("X-Vibe-Severity", severity.toString());
		msg.setHeader("X-Vibe-Path", path);
		msg.setSentDate(new Date());

		Transport.send(msg);
	}

	@Override
	public void export(String path, Sampler<?> series)
	{
		// Do nothing, not supported by mailer
	}

	@Override
	public void export(String path, Probe<?> probe)
	{
		// Do nothing, not supported by mailer
	}

	@Override
	public void export(String path, Timer timer)
	{
		// Do nothing, not supported by mailer
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void export(final String path, Events<?> events)
	{
		events.addListener(new EventListener()
		{
			@Override
			public void eventRegistered(Events events,
					final EventSeverity severity,
					final Object event)
			{
				if(severity.ordinal() < minimumSeverity.ordinal())
				{
					return;
				}

				final long time = System.currentTimeMillis();
				executor.submit(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							send(path, time, severity, event);
						}
						catch(MessagingException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		});
	}

	@Override
	public void close()
	{
		executor.shutdown();
	}

	/**
	 * Start building a new mail backend.
	 *
	 * @return
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	private static class AuthenticatorImpl
		extends javax.mail.Authenticator
	{
		private PasswordAuthentication authentication;

		public AuthenticatorImpl(String username, String password)
		{
			authentication = new PasswordAuthentication(username, password);
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication()
		{
			return authentication;
		}
	}

	public static class Builder
	{
		private EventSeverity minimumSeverity;
		private String subject;
		private String from;
		private List<String> receivers;
		private String smtpServer;
		private int smtpPort;
		private boolean smtpTls;
		private boolean smtpSsl;

		private AuthenticatorImpl authenticator;

		public Builder()
		{
			receivers = new ArrayList<String>();

			minimumSeverity = EventSeverity.ERROR;
			subject = "{path}: Event with {severity} severity received";

			smtpPort = 25;
		}

		/**
		 * Set the subject of sent e-mails. Use {@literal {severity}} to replace
		 * part of the subject with the severity of the event.
		 *
		 * @param subject
		 * @return
		 */
		public Builder setSubject(String subject)
		{
			this.subject = subject;

			return this;
		}

		/**
		 * Set the minimum severity for events that should be mailed.
		 *
		 * @param minimumSeverity
		 */
		public Builder setMinimumSeverity(EventSeverity minimumSeverity)
		{
			this.minimumSeverity = minimumSeverity;

			return this;
		}

		/**
		 * Set which e-mail to use as the sender.
		 *
		 * @param from
		 * @return
		 */
		public Builder setSender(String from)
		{
			this.from = from;

			return this;
		}

		/**
		 * Add a recipient to the backend.
		 *
		 * @param email
		 * @return
		 */
		public Builder addRecipient(String email)
		{
			receivers.add(email);

			return this;
		}

		/**
		 * Set which SMTP server to use.
		 *
		 * @param server
		 * @return
		 */
		public Builder setSmtpServer(String server)
		{
			this.smtpServer = server;

			return this;
		}

		/**
		 * Set which port the SMTP server uses.
		 *
		 * @param port
		 * @return
		 */
		public Builder setSmtpPort(int port)
		{
			this.smtpPort = port;

			return this;
		}

		/**
		 * Indicate that TLS should be used with the SMTP server.
		 *
		 * @param tls
		 * @return
		 */
		public Builder setSmtpTls(boolean tls)
		{
			this.smtpTls = tls;

			return this;
		}

		/**
		 * Indicate that SSL should be used with the SMTP server.
		 *
		 * @param ssl
		 * @return
		 */
		public Builder setSmtpSsl(boolean ssl)
		{
			this.smtpSsl = ssl;

			return this;
		}

		/**
		 * Set authentication for the SMTP server.
		 *
		 * @param username
		 * @param password
		 * @return
		 */
		public Builder setAuthentication(String username, String password)
		{
			authenticator = new AuthenticatorImpl(username, password);

			return this;
		}

		/**
		 * Create the backend.
		 *
		 * @return
		 */
		public MailBackend build()
		{
			if(minimumSeverity == null)
			{
				throw new IllegalArgumentException("Minimum severity can not be null");
			}

			if(smtpServer == null)
			{
				throw new IllegalArgumentException("SMTP server needs to be specified");
			}

			if(receivers.isEmpty())
			{
				throw new IllegalArgumentException("At least one recipient needs to be specified");
			}

			if(from == null)
			{
				throw new IllegalArgumentException("A sender needs to be specified");
			}

			return new MailBackend(
				minimumSeverity,
				smtpServer,
				smtpPort,
				from,
				receivers.toArray(new String[receivers.size()]),
				subject,
				smtpTls,
				smtpSsl,
				authenticator
			);
		}
	}
}
