package me.omrih.legitimooseBot.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
            String ownerName = Objects.requireNonNull(inv.getStack(0).get(DataComponentTypes.LORE)).lines().get(4).getString();
            String ownerNameRemaining = ownerName.substring(3).trim();
            if (ownerNameRemaining.contains("|")) {
                String[] parts = ownerNameRemaining.split("\\|");
                if (parts.length > 1) {
                    ownerName = parts[1];
                }
            } else {
                ownerName = ownerNameRemaining;
            }
            ScrapedWorld world = new ScrapedWorld();
            world.name = Objects.requireNonNull(inv.getStack(0).get(DataComponentTypes.CUSTOM_NAME)).getString();
            world.description = Objects.requireNonNull(inv.getStack(0).get(DataComponentTypes.LORE)).lines().getFirst().getString();
            world.ownerName = ownerName;
            world.resourcePackPresent = inv.getStack(0).get(DataComponentTypes.LORE).lines().size() > 5;
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
