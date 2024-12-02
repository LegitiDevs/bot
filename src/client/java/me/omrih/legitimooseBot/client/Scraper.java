package me.omrih.legitimooseBot.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.LOGGER;

public class Scraper {
    /*
    Scrape all worlds (will only scrape world name, description, votes and owner)
     */
    public static void scrapeAll() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        client.player.networkHandler.sendChatCommand("worlds");

        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assert MinecraftClient.getInstance().interactionManager != null;
            ScreenHandler currentScreenHandler = client.player.currentScreenHandler;
            int syncId = currentScreenHandler.syncId;
            Inventory inv = currentScreenHandler.getSlot(0).inventory;
            try {
                LOGGER.info(Objects.requireNonNull(inv.getStack(0).getItem().getComponents().get(DataComponentTypes.LORE)).lines().getFirst().getString());
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
            }
            MinecraftClient.getInstance().interactionManager.clickSlot(syncId, 0, 0, SlotActionType.PICKUP, client.player);
        }).start();
    }

    // inner class
    public static class ScrapedWorld {
        String name;
        String description;
        String ownerName;
        boolean resourcePackPresent;
        String uuidString;
        int votes;
    }
}
