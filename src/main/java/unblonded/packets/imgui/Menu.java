package unblonded.packets.imgui;

import imgui.*;
import imgui.flag.*;
import imgui.type.ImBoolean;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import unblonded.packets.cfg;
import unblonded.packets.cheats.AirUnderCheck;
import unblonded.packets.cheats.AutoLoadout;
import unblonded.packets.cheats.OreSimulator;
import unblonded.packets.cheats.PlayerTracker;
import unblonded.packets.util.BlockColor;
import unblonded.packets.util.Color;
import unblonded.packets.util.KitSlot;

import java.util.*;

public class Menu {
    private static List<PlayerTracker.PlayerInfo> frozenPlayerList = null;

    public static void render() {
        if (!cfg.showAll) return;

        float pulse_speed = 3.5f;
        float pulse = (float) (0.5f + 0.5f * Math.sin((ImGui.getTime()) * pulse_speed));
        ImVec4 neon_pink = new ImVec4(1.0f, 0.1f, 0.6f, 1.0f);
        ImVec4 neon_blue = new ImVec4(0.1f, 0.9f, 1.0f, 1.0f);
        ImVec4 neon_purple = new ImVec4(0.7f, 0.3f, 1.0f, 1.0f);

        if (cfg.showMenu) {
            ImGui.pushStyleColor(ImGuiCol.Border, new ImVec4(
                    neon_purple.x * pulse,
                    neon_purple.y * pulse,
                    neon_purple.z * pulse,
                    0.8f
            ));
            ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 2.0f + 1.5f * pulse);

            ImGui.begin("Modules - Unblonded");

            ImGui.pushStyleColor(ImGuiCol.FrameBg, new ImVec4(0.08f, 0.05f, 0.12f, 0.8f));
            ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, new ImVec4(
                    neon_blue.x * 0.3f,
                    neon_blue.y * 0.3f,
                    neon_blue.z * 0.3f,
                    0.6f
            ));
            ImGui.pushStyleColor(ImGuiCol.CheckMark, neon_pink);

            if (ImGui.beginTabBar("MainTabBar", ImGuiTabBarFlags.None))
            {
                // Combat  PvP Tab
                if (ImGui.beginTabItem(icons.CROSSHAIRS + " Combat"))
                {
                    ImGui.checkbox(icons.GEM + " Auto Crystal", cfg.autoCrystal);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##crystal")) cfg.autoCrystalCfg.set(!cfg.autoCrystalCfg.get());

                    ImGui.checkbox(icons.SHIELD_HALVED + " Auto Totem", cfg.autoTotem);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##totem")) cfg.autoTotemCfg.set(!cfg.autoTotemCfg.get());

                    ImGui.checkbox(icons.BOMB + " Auto Anchor", cfg.autoAnchor);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##anchor")) cfg.autoAnchorCfg.set(!cfg.autoAnchorCfg.get());

                    ImGui.checkbox(icons.CROSSHAIRS + " Aim Assist", cfg.aimAssistToggle);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##aimAssist")) cfg.aimAssistCfg.set(!cfg.aimAssistCfg.get());

                    ImGui.checkbox(icons.BOMB + " Crystal Spam", cfg.crystalSpam);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##crystalspam")) cfg.crystalSpamCfg.set(!cfg.crystalSpamCfg.get());

                    ImGui.checkbox(icons.BOMB + " Self Crystal", cfg.selfCrystal);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##selfcrystal")) cfg.selfCrystalCfg.set(!cfg.selfCrystalCfg.get());

                    ImGui.endTabItem();
                }

                // ESP  Visual Tab
                if (ImGui.beginTabItem(icons.EYE + " Visuals"))
                {
                    ImGui.checkbox(icons.TEXT_HEIGHT + " Font Size Override", cfg.fontSizeOverride);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##fontsize")) cfg.fontSizeCfg.set(!cfg.fontSizeCfg.get());

                    ImGui.checkbox(icons.USERS + " Show Player List", cfg.displayPlayers);

                    ImGui.checkbox(icons.EYE + " Advanced ESP", cfg.advEsp);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##advesp")) cfg.advEspCfg.set(!cfg.advEspCfg.get());

                    //ImGui.checkbox(icons.IMAGE + " Show Background Effects", cfg.backgroundFx);

                    ImGui.checkbox(icons.STAR + " Show Cosmic Crosshair", cfg.nightFx);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##crosshair")) cfg.nightFxCfg.set(!cfg.nightFxCfg.get());

                    ImGui.endTabItem();
                }

                // Utility Tab
                if (ImGui.beginTabItem(icons.TOOLBOX + " Utility"))
                {
                    ImGui.checkbox(icons.HAND + " Interaction Canceler", cfg.cancelInteraction);

                    ImGui.checkbox(icons.PLUG + " Auto Disconnect", cfg.autoDc);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##autodc")) cfg.autoDcCfg.set(!cfg.autoDcCfg.get());

                    ImGui.checkbox(icons.MONEY_CHECK_DOLLAR + " Auto Sell", cfg.autoSell);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##autosell")) cfg.autoSellCfg.set(!cfg.autoSellCfg.get());

                    ImGui.checkbox(icons.COMMENT_SLASH + " Chat Filter", cfg.chatFilter);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##chatfilter")) cfg.chatFilterCfg.set(!cfg.chatFilterCfg.get());

                    ImGui.checkbox(icons.BOX_OPEN + " Storage Scan", cfg.storageScan);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##storagescan")) cfg.storageScanCfg.set(!cfg.storageScanCfg.get());

                    ImGui.checkbox(icons.TACHOGRAPH_DIGITAL + " FPS Chart", cfg.showFpsChart);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##fpschart")) cfg.fpsChartCfg.set(!cfg.fpsChartCfg.get());

                    if (ImGui.button(icons.BOOK_OPEN + " Loadout Manager##loadouts")) cfg.showLoadouts.set(!cfg.showLoadouts.get());

                    ImGui.endTabItem();
                }

                // Mining  Economy Tab
                if (ImGui.beginTabItem(icons.GEM + " Mining"))
                {
                    ImGui.checkbox(icons.HELMET_SAFETY + " Player Dig Safety", cfg.checkPlayerAirSafety);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##digsafety")) cfg.checkPlayerAirSafetyCfg.set(!cfg.checkPlayerAirSafetyCfg.get());

                    //ImGui.checkbox(icons.ROUTE + " Straight Tunnel", cfg.forwardTunnel);

                    ImGui.checkbox(icons.SEEDLING + " Seed-Ray", cfg.oreSim);
                    ImGui.sameLine();
                    if (ImGui.button(icons.GEARS + "##oresim")) cfg.oreSimCfg.set(!cfg.oreSimCfg.get());

                    ImGui.endTabItem();
                }

                ImGui.endTabBar();
            }


            ImGui.popStyleColor(3);

            ImGui.end();
            ImGui.popStyleVar();
            ImGui.popStyleColor();

            if (cfg.showLoadouts.get()) {
                loadOutTest();
            }

            if (cfg.fpsChartCfg.get()) {
                ImGui.begin("FPS Chart Config", cfg.fpsChartCfg);
                ImGui.text("FPS Chart is " + (cfg.showFpsChart.get() ? "enabled" : "disabled"));
                ImGui.sliderInt("Sample Rate (frames)", cfg.fpsChartSampleRate, 1, 100);
                ImGui.checkbox("Show In Game", cfg.showFpsChartInGame);
                ImGui.end();
            }

            if (cfg.selfCrystalCfg.get()) {
                ImGui.begin("Self Crystal", cfg.selfCrystalCfg);
                ImGui.text("Self Crystal is " + (cfg.selfCrystal.get() ? "enabled" : "disabled"));
                ImGui.sliderInt("Delay (ms)", cfg.selfCrystalDelay, 1, 300);
                ImGui.sliderInt("Humanity (ms)", cfg.selfCrystalHumanity, 1, 100);
                ImGui.end();
            }

            if (cfg.autoAnchorCfg.get()) {
                ImGui.begin("Auto Anchor", cfg.autoAnchorCfg);
                ImGui.text("Auto Anchor is " + (cfg.selfCrystal.get() ? "enabled" : "disabled"));
                ImGui.sliderInt("Delay (ms)", cfg.autoAnchorDelay, 1, 50);
                ImGui.sliderInt("Humanity (ms)", cfg.autoAnchorHumanity, 1, 50);
                ImGui.end();
            }

            if (cfg.nightFxCfg.get()) {
                ImGui.begin("Cosmic Crosshair", cfg.nightFxCfg);
                ImGui.text("Cosmic Crosshair is " + (cfg.nightFx.get() ? "enabled" : "disabled"));
                ImGui.sliderFloat("Size", cfg.nightFxSize, 1.0f, 100.0f, "%.1f");
                ImGui.checkbox("Show Crosshair Lines", cfg.nightFxCrosshairLines);
                ImGui.end();
            }

            if (cfg.crystalSpamCfg.get()) {
                ImGui.begin("Crystal Spam", cfg.crystalSpamCfg);
                ImGui.text("Crystal Spam is " + (cfg.crystalSpam.get() ? "enabled" : "disabled"));
                ImGui.sliderInt("Search Radius", cfg.crystalSpamSearchRadius, 1, 6);
                ImGui.sliderInt("Break Delay (ms)", cfg.crystalSpamBreakDelay, 1, 1000);
                ImGui.end();
            }

            if (cfg.aimAssistCfg.get()) {
                ImGui.begin("Aim Assist", cfg.aimAssistCfg);
                ImGui.text("Aim Assist is " + (cfg.aimAssistToggle.get() ? "enabled" : "disabled"));

                ImGui.sliderFloat("Range", cfg.aimAssistRange, 1.0f, 20.0f, "%.1f");
                ImGui.sliderFloat("Field of View", cfg.aimAssistFov, 1.0f, 180.0f, "%.1f");
                ImGui.sliderFloat("Smoothness", cfg.aimAssistSmoothness, 0.1f, 10.0f, "%.1f");
                ImGui.sliderFloat("Min Speed", cfg.aimAssistMinSpeed, 1.0f, 360.0f, "%.1f/s");
                ImGui.sliderFloat("Max Speed", cfg.aimAssistMaxSpeed, 1.0f, 360.0f, "%.1f/s");
                ImGui.checkbox("Visibility Check", cfg.aimAssistVisibilityCheck);
                ImGui.sliderInt("Update Rate (ms)", cfg.aimAssistUpdateRate, 1, 100);

                ImGui.end();
            }

            if (cfg.fontSizeCfg.get()) {
                ImGui.begin("Font Size Override", cfg.fontSizeCfg);
                ImGui.text("Use a custom font size.");
                ImGui.sliderFloat("Font Size", cfg.fontSize, 0.25f, 4.0f, "%.1f");
                ImGui.end();
            }

            if (cfg.chatFilterCfg.get()) {
                ImGui.begin("Chat Filter", cfg.chatFilterCfg);
                if (cfg.filterMode > 0) ImGui.text("Block Chat If: ");

                if (cfg.filterMode < 0 || cfg.filterMode >= cfg.chatFilterItems.length) cfg.filterMode = 0;

                if (ImGui.beginCombo("Filter Mode", cfg.chatFilterItems[cfg.filterMode])) {
                    for (int n = 0; n < cfg.chatFilterItems.length; n++) {
                        boolean isSelected = (cfg.filterMode == n);
                        if (ImGui.selectable(cfg.chatFilterItems[n], isSelected)) cfg.filterMode = n;
                        if (isSelected) ImGui.setItemDefaultFocus();
                    }
                    ImGui.endCombo();
                }

                if (cfg.filterMode > 0)
                    ImGui.inputText("Message", cfg.blockMsg);
                ImGui.end();
            }

            if (cfg.autoDcCfg.get()) {
                ImGui.begin("Auto Disconnect", cfg.autoDcCfg);
                ImGui.text("Player Proximity Condition");
                ImGui.inputFloat("##prox", cfg.autoDcProximity, 0, 0);
                if (ImGui.button(cfg.autoDcPrimed.get() ? "Primed, Disconnect Ready" : "Not Primed!")) cfg.autoDcPrimed.set(!cfg.autoDcPrimed.get());
                ImGui.end();
            }

            if (cfg.autoSellCfg.get()) {
                ImGui.begin("Auto Sell", cfg.autoSellCfg);
                ImGui.sliderInt("Delay (ms)", cfg.autoSellDelay, 5, 500);
                ImGui.inputText("Price", cfg.autoSellPrice);
                ImGui.text("Endpoints:");
                ImGui.inputInt("Start Slot", cfg.autoSellEndpoints[0]);
                ImGui.inputInt("Stop Slot", cfg.autoSellEndpoints[1]);
                if (ImGui.button("Trigger Sell")) cfg.triggerAutoSell = true;
                ImGui.end();

                if (cfg.autoSellEndpoints[0].get() < 1) cfg.autoSellEndpoints[0].set(1);
                if (cfg.autoSellEndpoints[0].get() > 9) cfg.autoSellEndpoints[0].set(9);
                if (cfg.autoSellEndpoints[1].get() > 9) cfg.autoSellEndpoints[1].set(9);
                if (cfg.autoSellEndpoints[1].get() < 1) cfg.autoSellEndpoints[1].set(1);
            }

            if (cfg.autoTotemCfg.get()) {
                ImGui.begin("Auto Totem", cfg.autoTotemCfg);
                ImGui.text("Auto Totem is " + (cfg.autoTotem.get() ? "enabled" : "disabled"));
                ImGui.sliderInt("Delay (ms)", cfg.autoTotemDelay, 5, 500);
                ImGui.sliderInt("Humanity", cfg.autoTotemHumanity, 0, 200);
                ImGui.end();
            }

            if (cfg.oreSimCfg.get()) {
                ImGui.pushStyleColor(ImGuiCol.WindowBg, new ImVec4(0.05f, 0.03f, 0.08f, 0.95f));
                ImGui.begin("Seed-Ray Config", cfg.oreSimCfg);

                ImGui.textColored(neon_blue, "Seed-Ray is " + (cfg.oreSim.get() ? "enabled" : "disabled"));
                if (cfg.oreSimDistance[0] > 8)
                    ImGui.textColored(neon_pink, "Warning: High render distance may use lots of CPU");

                ImGui.pushStyleColor(ImGuiCol.FrameBg, new ImVec4(0.12f, 0.08f, 0.18f, 0.8f));
                ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, new ImVec4(0.18f, 0.12f, 0.24f, 0.8f));
                ImGui.pushStyleColor(ImGuiCol.SliderGrab, neon_purple);
                ImGui.sliderInt("Render Distance", cfg.oreSimDistance, 0, 32);

                ImGui.popStyleColor(3);

                ImGui.inputScalar("Seed", cfg.oreSimSeed);
                if (ImGui.button("Use Donut Seed")) cfg.oreSimSeed.set(6608149111735331168L);
                if (ImGui.button((cfg.oreSimDrawMode.get() ? "Glow" : "Wire") + " Mode##drawTypeOresim")) cfg.oreSimDrawMode.set(!cfg.oreSimDrawMode.get());
                ImGui.sameLine();
                ImGui.colorEdit4("##espcolor", cfg.oreSimColor, ImGuiColorEditFlags.NoLabel | ImGuiColorEditFlags.NoInputs);
                ImGui.sameLine();
                ImGui.text("Esp Color");


                renderDebrisGraphAnimated();
                ImGui.end();
                ImGui.popStyleColor();
            }

            if (cfg.autoCrystalCfg.get()) {
                ImGui.begin("Auto Crystal", cfg.autoCrystalCfg);
                ImGui.text("Auto Crystal is " + (cfg.autoCrystal.get() ? "enabled" : "disabled"));
                ImGui.sliderInt("Attack Time (ms)", cfg.crystalAttackTime, 5, 500);
                ImGui.sliderInt("Place Time (ms)", cfg.crystalPlaceTime, 5, 500);
                ImGui.end();
            }

            if (cfg.checkPlayerAirSafetyCfg.get()) {
                ImGui.begin("Player Dig Safety");
                ImGui.text("Player Dig Safety is " + (cfg.checkPlayerAirSafety.get() ? "enabled" : "disabled"));
                ImGui.checkbox("Show Status In-Game", cfg.isPlayerAirSafeShowStatus);

                ImGui.end();
            }

            if (cfg.advEspCfg.get()) {
                ImGui.begin("Advanced ESP", cfg.advEspCfg);

                ImGui.text("ESP Settings:");

                if (cfg.espRadius[0] > 64) ImGui.textColored(neon_pink, "Warning: High radius may eat the cpu");
                ImGui.sliderInt("Esp Radius", cfg.espRadius, 16, 256);

                ImGui.checkbox("Draw Blocks", cfg.drawBlocks);
                ImGui.sameLine();
                if (ImGui.button((cfg.advEspDrawType.get() ? "Glow" : "Wire") + " Mode##drawType")) cfg.advEspDrawType.set(!cfg.advEspDrawType.get());
                if (cfg.drawBlocks.get()) ImGui.checkbox("Draw Tracers", cfg.drawBlockTracer);

                ImGui.inputText("Block Name", cfg.blockName);
                ImGui.colorEdit4("Block Color", cfg.blockColor);

                if (ImGui.button("Add Block to ESP")) {
                    String blockName = cfg.blockName.get().trim();

                    if (!blockName.isEmpty()) {
                        Identifier blockId = Identifier.tryParse(blockName.contains(":") ? blockName : "minecraft:" + blockName);
                        if (blockId != null) {
                            Block block = Registries.BLOCK.get(blockId);

                            if (!block.equals(Blocks.AIR)) {
                                boolean exists = cfg.espBlockList.stream()
                                        .anyMatch(blockColor -> blockColor.getBlock().equals(block));

                                if (!exists) {
                                    cfg.espBlockList.add(new BlockColor(block, new Color(cfg.blockColor), true));
                                    cfg.blockName.set("");
                                }
                            } else {
                                System.out.println("Invalid block name: " + blockName);
                            }
                        }
                    }
                }

                ImGui.separator();
                ImGui.text("Current ESP Blocks:");
                ImGui.beginChild("BlockListChild", 0, 150, true);

                for (int i = 0; i < cfg.espBlockList.size(); i++) {
                    BlockColor blockColor = cfg.espBlockList.get(i);
                    ImGui.pushID(i);

                    ImBoolean enabled = new ImBoolean(blockColor.isEnabled());
                    if (ImGui.checkbox("##enabled", enabled)) blockColor.setEnabled(enabled.get());

                    ImGui.sameLine();

                    float[] color = blockColor.getColorF();
                    if (ImGui.colorEdit4("##color", color, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoLabel))
                        blockColor.setColor(new Color(color[0], color[1], color[2], color[3]));

                    ImGui.sameLine();

                    String blockName = Registries.BLOCK.getId(blockColor.getBlock()).toString();
                    ImGui.textUnformatted(blockName);

                    ImGui.sameLine(ImGui.getWindowWidth() - 120);

                    if (ImGui.smallButton("Remove")) {
                        cfg.espBlockList.remove(i);
                        i--;
                    }

                    ImGui.popID();
                }

                ImGui.endChild();
                ImGui.end();
            }
        }

        if (cfg.storageScanCfg.get() || (cfg.storageScanShow && cfg.storageScanShowInGui.get())) {
            if (!cfg.showMenu) cfg.storageScanCfg.set(false);
            ImGui.begin("Storage Scan", cfg.storageScanCfg);
            ImGui.text("Storage Scan is " + (cfg.storageScan.get() ? "enabled" : "disabled"));
            ImGui.colorEdit4("Highlight Color", cfg.storageScanColor, ImGuiColorEditFlags.NoInputs);
            ImGui.inputText("Search For", cfg.storageScanSearch);
            ImGui.checkbox("Show Config In Gui", cfg.storageScanShowInGui);
            ImGui.end();
        }

        if (cfg.displayPlayers.get()) {
            ImGui.begin("Nearby Players");

            ImGui.checkbox(icons.SNOWFLAKE + " Freeze List", cfg.freezePlayers);
            if (cfg.freezePlayers.get()) {
                ImGui.textColored(new ImVec4(0.8f, 0.5f, 0.2f, 1.0f), "List is frozen.");
            }
            ImGui.separator();

            List<PlayerTracker.PlayerInfo> nearbyPlayers;

            if (cfg.freezePlayers.get()) {
                if (frozenPlayerList == null) frozenPlayerList = new ArrayList<>(PlayerTracker.getNearbyPlayers());
                nearbyPlayers = frozenPlayerList;
            } else {
                nearbyPlayers = PlayerTracker.getNearbyPlayers();
                frozenPlayerList = null;
            }

            for (PlayerTracker.PlayerInfo entry : nearbyPlayers) {
                String name = entry.name;
                float distance = entry.distance;
                String[] armor = entry.armor;
                String mainhand = entry.mainhand;
                String offhand = entry.offhand;
                float health = entry.health;
                int armorToughness = entry.armorTuffness;
                boolean isSneaking = entry.isSneaking;
                boolean isSprinting = entry.isSprinting;

                String headerLabel = String.format("%s - %.1fm", name, distance);
                String treeID = name + "##tree";

                if (ImGui.treeNode(treeID, headerLabel)) {
                    if (ImGui.treeNode("Stats")) {
                        ImGui.text(String.format("Health -> %.1f", health));
                        ImGui.text(String.format("Armor Rating -> %d", armorToughness));
                        ImGui.text("Sprinting -> " + (isSprinting ? "yes" : "no"));
                        ImGui.text("Sneaking -> " + (isSneaking ? "yes" : "no"));
                        ImGui.treePop();
                    }

                    if (ImGui.treeNode("Armor")) {
                        ImGui.text("Helm -> " + armor[0]);
                        ImGui.text("Chest -> " + armor[1]);
                        ImGui.text("Leg -> " + armor[2]);
                        ImGui.text("Boot -> " + armor[3]);
                        ImGui.treePop();
                    }

                    if (ImGui.treeNode("Hands")) {
                        ImGui.text("Main -> " + mainhand);
                        ImGui.text("Off -> " + offhand);
                        ImGui.treePop();
                    }

                    ImGui.treePop();
                }
            }

            ImGui.end();
        }

        if (cfg.checkPlayerAirSafety.get() && cfg.isPlayerAirSafeShowStatus.get()) {
            AirUnderCheck.checkSafety();
            ImGui.begin("Dig Safety");

            if (!AirUnderCheck.isSafe)
                ImGui.textColored(0.0f, 1.0f, 0.0f, 1.0f, "Status: SAFE");
            else
                ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, "Status: DANGER!");

            ImGui.sameLine();
            ImGui.text(AirUnderCheck.playerAirSafety);

            ImGui.end();
        }

        if (cfg.forwardTunnel.get()) {
            ImGui.begin("Tunneling Status");
            ImGui.text(cfg.tunnelBlockStatus);
            ImGui.end();
        }

        if ((cfg.showFpsChart.get() && cfg.showMenu) || cfg.showFpsChartInGame.get()) {
            ImGui.begin("FPS Chart", cfg.showFpsChart);
            renderFpsPlot();
            ImGui.end();
        }
    }

    private static final int GRAPH_HISTORY_SIZE = 100;
    private static final int POINT_REPEAT = 3;
    private static final Deque<Float> debrisHistory = new ArrayDeque<>();
    private static float lastAverageDensity = -1f;

    public static void renderDebrisGraphAnimated() {
        int totalDebris = OreSimulator.chunkDebrisPositions.values().stream()
                .mapToInt(Set::size).sum();
        float averageDensity = OreSimulator.horizontalRadius == 0
                ? 0f
                : totalDebris / (float) OreSimulator.horizontalRadius;

        if (averageDensity != lastAverageDensity) {
            for (int j = 0; j < POINT_REPEAT; j++) {
                if (debrisHistory.size() >= GRAPH_HISTORY_SIZE) debrisHistory.pollFirst();
                debrisHistory.addLast(averageDensity);
            }
            lastAverageDensity = averageDensity;
        }

        while (debrisHistory.size() < GRAPH_HISTORY_SIZE) debrisHistory.addLast(lastAverageDensity);

        float[] historyArray = new float[debrisHistory.size()];
        int i = 0;
        for (Float f : debrisHistory) historyArray[i++] = f;

        float graphWidth = ImGui.getContentRegionAvailX();
        ImGui.plotLines("##DebrisDensity", historyArray, historyArray.length, 0,
                "Debris Density (dynamic)", 0f, 30f, new ImVec2(graphWidth, 100));
    }

    private static final int FPS_HISTORY_SIZE = 100;
    private static final Deque<Float> fpsHistory = new ArrayDeque<>();
    private static int frameCounter = 0;

    public static void renderFpsPlot() {
        float currentFps = 1.0f / ImGui.getIO().getDeltaTime();

        frameCounter++;
        if (frameCounter >= cfg.fpsChartSampleRate[0]) {
            float fps = ImGui.getIO().getFramerate();
            if (fpsHistory.size() >= FPS_HISTORY_SIZE) fpsHistory.pollFirst();
            fpsHistory.addLast(fps);
            frameCounter = 0;
        }

        float[] historyArray = new float[fpsHistory.size()];
        int i = 0;
        for (Float f : fpsHistory) historyArray[i++] = f;

        float width = ImGui.getContentRegionAvailX();
        ImGui.plotLines("FPS", historyArray, historyArray.length, 0, String.format("FPS: %.1f", currentFps),
                0f, 200f, new ImVec2(width, 100));
    }

    private static void loadOutTest() {
        ImGui.setNextWindowSize(320, 350, ImGuiCond.FirstUseEver);
        ImGui.begin("Loadouts", cfg.showLoadouts, ImGuiWindowFlags.NoCollapse);

        ImGui.pushStyleColor(ImGuiCol.Text, 0.8f, 0.9f, 1.0f, 1.0f);
        ImGui.text("New Loadout");
        ImGui.popStyleColor();

        ImGui.setNextItemWidth(160);
        ImGui.inputText("##LoadoutName", cfg.loadoutNameInput, ImGuiInputTextFlags.None);
        ImGui.sameLine();

        ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.7f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.3f, 0.8f, 0.3f, 1.0f);
        if (ImGui.button("Save")) {
            String name = cfg.loadoutNameInput.get().trim();
            if (!name.isEmpty()) {
                AutoLoadout.saveCurrentLoadout(name);
                cfg.loadoutNameInput.set("");
            }
        }
        ImGui.popStyleColor(2);

        ImGui.separator();

        Map<String, List<KitSlot>> loadouts = cfg.savedLoadouts;

        ImGui.pushStyleColor(ImGuiCol.Text, 0.8f, 0.9f, 1.0f, 1.0f);
        ImGui.text("Saved (" + loadouts.size() + ")");
        ImGui.popStyleColor();

        if (loadouts.isEmpty()) {
            ImGui.pushStyleColor(ImGuiCol.Text, 0.6f, 0.6f, 0.6f, 1.0f);
            ImGui.text("No loadouts saved");
            ImGui.popStyleColor();
        } else {
            ImGui.beginChild("LoadoutList", 0, 140, true, ImGuiWindowFlags.None);

            for (String name : loadouts.keySet()) {
                boolean selected = name.equals(cfg.selectedLoadout);
                List<KitSlot> kit = loadouts.get(name);

                if (selected) {
                    ImGui.pushStyleColor(ImGuiCol.Header, 0.3f, 0.5f, 0.8f, 0.8f);
                    ImGui.pushStyleColor(ImGuiCol.HeaderHovered, 0.4f, 0.6f, 0.9f, 0.8f);
                }

                if (ImGui.selectable(name + " (" + kit.size() + ")", selected)) cfg.selectedLoadout = name;
                if (selected) ImGui.popStyleColor(2);

                if (ImGui.isItemHovered() && ImGui.isMouseClicked(1)) {
                    cfg.selectedLoadout = name;
                    ImGui.openPopup("LoadoutContext");
                }
            }

            ImGui.endChild();

            if (ImGui.beginPopup("LoadoutContext")) {
                if (cfg.selectedLoadout != null) {
                    if (ImGui.menuItem("Apply")) {
                        AutoLoadout.applyLoadout(cfg.selectedLoadout);
                    }
                    if (ImGui.menuItem("Delete")) {
                        AutoLoadout.removeLoadout(cfg.selectedLoadout);
                        cfg.selectedLoadout = null;
                    }
                }
                ImGui.endPopup();
            }

            if (cfg.selectedLoadout != null) {
                ImGui.separator();
                ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.7f, 0.2f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.3f, 0.8f, 0.3f, 1.0f);
                if (ImGui.button("Apply")) {
                    AutoLoadout.applyLoadout(cfg.selectedLoadout);
                }
                ImGui.popStyleColor(2);

                ImGui.sameLine();
                ImGui.pushStyleColor(ImGuiCol.Button, 0.8f, 0.2f, 0.2f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.9f, 0.3f, 0.3f, 1.0f);
                if (ImGui.button("Delete")) {
                    AutoLoadout.removeLoadout(cfg.selectedLoadout);
                    cfg.selectedLoadout = null;
                }
                ImGui.popStyleColor(2);

                ImGui.sameLine();
                ImGui.pushStyleColor(ImGuiCol.Button, 0.8f, 0.5f, 0.2f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.9f, 0.6f, 0.3f, 1.0f);
                if (ImGui.button("Update")) {
                    AutoLoadout.saveCurrentLoadout(cfg.selectedLoadout);
                }
                ImGui.popStyleColor(2);

                if (ImGui.collapsingHeader("Items")) {
                    List<KitSlot> selectedKit = loadouts.get(cfg.selectedLoadout);
                    if (selectedKit != null) {
                        ImGui.beginChild("Details", 0, 60, true, ImGuiWindowFlags.None);
                        ImGui.pushStyleColor(ImGuiCol.Text, 0.9f, 0.9f, 0.9f, 1.0f);

                        for (KitSlot slot : selectedKit) {
                            ImGui.text(getSlotTypeName(slot.slotIndex) + ": " + getItemDisplayName(slot.item));
                        }

                        ImGui.popStyleColor();
                        ImGui.endChild();
                    }
                }
            }

            ImGui.separator();
            ImGui.pushStyleColor(ImGuiCol.Button, 0.6f, 0.2f, 0.2f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.7f, 0.3f, 0.3f, 1.0f);
            if (ImGui.button("Clear All")) {
                if (ImGui.getIO().getKeyCtrl()) {
                    AutoLoadout.clearAllLoadouts();
                    cfg.selectedLoadout = null;
                }
            }
            ImGui.popStyleColor(2);

            if (ImGui.isItemHovered()) ImGui.setTooltip("Hold Ctrl to confirm");
        }

        ImGui.end();
    }

    private static String getSlotTypeName(int slotIndex) {
        if (slotIndex >= 0 && slotIndex <= 8) {
            return "H" + slotIndex;
        } else if (slotIndex >= 9 && slotIndex <= 35) {
            return "I" + (slotIndex - 8);
        } else if (slotIndex == 36) {
            return "Boots";
        } else if (slotIndex == 37) {
            return "Legs";
        } else if (slotIndex == 38) {
            return "Chest";
        } else if (slotIndex == 39) {
            return "Head";
        } else if (slotIndex == 40) {
            return "Off";
        }
        return "S" + slotIndex;
    }

    private static String getItemDisplayName(net.minecraft.item.Item item) {
        String name = item.toString();
        if (name.startsWith("minecraft:")) name = name.substring(10);
        name = name.replace("_", " ");
        if (!name.isEmpty()) name = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
        return name;
    }
}