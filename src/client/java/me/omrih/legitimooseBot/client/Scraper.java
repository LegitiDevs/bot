package me.omrih.legitimooseBot.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import static me.omrih.legitimooseBot.client.LegitimooseBotClient.LOGGER;

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
            ItemStack itemStack = inv.getStack(0);
            LOGGER.info("ItemStack String: " + itemStack.toString());
            LOGGER.info("ItemStack translation key: " + itemStack.getTranslationKey());
            LOGGER.info("Components: " + itemStack.getComponents().toString());
            NbtCompound customData = itemStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
            NbtElement publicBukkitValues = customData.get("PublicBukkitValues");
            assert publicBukkitValues != null;

            ScrapedWorld world = new ScrapedWorld();
            world.creation_date = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:creation_date")).asString();
            world.enforce_whitelist = Boolean.getBoolean(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:enforce_whitelist")).asString());
            world.locked = Boolean.getBoolean(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:locked")).asString());
            world.owner_uuid = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:owner")).asString();
            world.player_count = Integer.parseInt(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:player_count")).asString());
            world.resource_pack_url = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:resource_pack_url")).asString();
            world.world_uuid = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:uuid")).asString();
            world.version = Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:version")).asString();
            world.visits = Integer.parseInt(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:visits")).asString());
            world.votes = Integer.parseInt(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:votes")).asString());
            world.whitelist_on_version_change = Boolean.getBoolean(Objects.requireNonNull(((NbtCompound) publicBukkitValues).get("datapackserverpaper:whitelist_on_version_change")).asString());
            world.name = Objects.requireNonNull(inv.getStack(0).get(DataComponentTypes.CUSTOM_NAME)).getString();
            world.description = Objects.requireNonNull(inv.getStack(0).get(DataComponentTypes.LORE)).lines().getFirst().getString();
            world.icon = Objects.requireNonNull(inv.getStack(0).toString().substring(2));

            LOGGER.info(world.toString());
/*
                Sample data:
                minecraft:custom_data=>{PublicBukkitValues:{
                "datapackserverpaper:creation_date":"Sep 29, 2024, 3:24 AM",
                "datapackserverpaper:enforce_whitelist":"false",
                "datapackserverpaper:locked":"true",
                "datapackserverpaper:owner":"929cffe4-872e-47c6-97c4-dccb05e9f64b",
                "datapackserverpaper:player_count":"1",
                "datapackserverpaper:resource_pack_url":"https://download.mc-packs.net/pack/74afb347ecaa619a00cd2f50f6d2d2db49551a6b.zip",
                "datapackserverpaper:uuid":"11071f4b-0944-4d54-8451-8bd3417ab9b5",
                "datapackserverpaper:version":"1.21.1",
                "datapackserverpaper:visits":"149",
                "datapackserverpaper:votes":"12",
                "datapackserverpaper:whitelist_on_version_change":"false"}}
                 */

            MinecraftClient.getInstance().interactionManager.clickSlot(syncId, 0, 0, SlotActionType.PICKUP, client.player);
        }).start();
    }

    // inner class
    public static class ScrapedWorld {
        String creation_date;
        boolean enforce_whitelist;
        boolean locked;
        String owner_uuid;
        int player_count;
        String resource_pack_url;
        String world_uuid;
        String version;
        int visits;
        int votes;
        boolean whitelist_on_version_change;
        String name;
        String description;
        String icon;

        @Override
        public String toString() {
            return toJsonObject().toString();
        }
        public JsonObject toJsonObject() {
            JsonObject obj = new JsonObject();
            obj.add("creation_date", new JsonPrimitive(creation_date));
            obj.add("enforce_whitelist", new JsonPrimitive(enforce_whitelist));
            obj.add("locked", new JsonPrimitive(locked));
            obj.add("owner_uuid", new JsonPrimitive(owner_uuid));
            obj.add("player_count", new JsonPrimitive(player_count));
            obj.add("resource_pack_url", new JsonPrimitive(resource_pack_url));
            obj.add("world_uuid", new JsonPrimitive(world_uuid));
            obj.add("version", new JsonPrimitive(version));
            obj.add("visits", new JsonPrimitive(visits));
            obj.add("votes", new JsonPrimitive(votes));
            obj.add("whitelist_on_version_change", new JsonPrimitive(whitelist_on_version_change));
            obj.add("name", new JsonPrimitive(name));
            obj.add("description", new JsonPrimitive(description));
            obj.add("icon", new JsonPrimitive(icon));
            return obj;
        }
    }

    private static NbtCompound encodeStack(ItemStack stack, DynamicOps<NbtElement> ops) {
        DataResult<NbtElement> result = ComponentChanges.CODEC.encodeStart(ops, stack.getComponentChanges());
        result.ifError(e->{

        });
        NbtElement nbtElement = result.getOrThrow();
        // cast here, as soon as this breaks, the mod will need to update anyway
        return (NbtCompound) nbtElement;
    }
}
