package com.github.pseudoresonance.resonantbot;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

public class BotUtils {

	// Handles the creation and getting of a IDiscordClient object for a token
	static IDiscordClient getBuiltDiscordClient(String token) {

		// The ClientBuilder object is where you will attach your params for
		// configuring the instance of your bot.
		// Such as withToken, setDaemon etc
		IDiscordClient client = new ClientBuilder().withToken(token).online(Config.getPrefix() + "help | " + Config.getName()).build();
		return client;

	}

	// Helper functions to make certain aspects of the bot easier to use.
	public static void sendMessage(IChannel channel, String message) {

		// This might look weird but it'll be explained in another page.
		RequestBuffer.request(() -> {
			try {
				int zero = 0x200B;
				channel.sendMessage(Character.toString((char) zero) + message);
			} catch (RateLimitException e) {
				System.err.println("Message could not be sent due to rate limit!");
				throw e;
			} catch (DiscordException e) {
				System.err.println("Message could not be sent with error: ");
				e.printStackTrace();
				throw e;
			}
		});
	}
	
	public static void sendMessage(IChannel channel, EmbedObject embed) {

		// This might look weird but it'll be explained in another page.
		RequestBuffer.request(() -> {
			try {
				channel.sendMessage(embed);
				return;
			} catch (RateLimitException e) {
				System.err.println("Message could not be sent due to rate limit!");
				return;
			} catch (DiscordException e) {
				System.err.println("Message could not be sent with error: ");
				e.printStackTrace();
				return;
			}
		});
	}
}