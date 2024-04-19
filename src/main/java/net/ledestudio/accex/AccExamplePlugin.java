package net.ledestudio.accex;

import net.kyori.adventure.text.Component;
import net.ledestudio.acc.client.AccMessage;
import net.ledestudio.acc.service.AfreecaTvChatCrawler;
import net.ledestudio.acc.service.AfreecaTvMessageReceiveEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class AccExamplePlugin extends JavaPlugin {

    public static AccExamplePlugin instance;
    private AfreecaTvChatCrawler crawler;

    @Override
    public void onEnable() {
        // Bind Plugin Instance;
        instance = this;

        // Save Config
        saveDefaultConfig();

        // Load Url from Config
        final String afreecaTvLiveUrl = getConfig().getString("afreecatv-live-url");
        if (afreecaTvLiveUrl == null) {
            getLogger().warning("Failed to retrieve the URL from config, causing server shutdown.");
            Bukkit.shutdown();
            return;
        }

        // Create Chat Crawler
        crawler = new AfreecaTvChatCrawler(afreecaTvLiveUrl);

        // Create and Bind Message Event
        crawler.registerMessageReceiveEvent(new AfreecaTvMessageReceiveEvent() {
            @Override
            public void onMessageReceive(@NotNull AccMessage message) {
                // Send the chat from the main thread of the plugin,
                // as the receiving thread is different from the main thread of the plugin.
                Bukkit.getScheduler().runTask(AccExamplePlugin.instance, () -> {
                    // Broadcast Message
                    Bukkit.broadcast(Component.text(String.format(
                            "%s[%s] : %s",
                            message.getSenderNickname(),
                            message.getSenderId(),
                            message.getMessage()
                    )));
                });
            }
        });

        // Start Chat Crawler
        crawler.connect();
    }

    @Override
    public void onDisable() {
        if (crawler != null) {
            crawler.close();
        }
    }
}
