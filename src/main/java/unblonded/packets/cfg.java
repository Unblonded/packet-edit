package unblonded.packets;

import imgui.type.*;
import unblonded.packets.util.BlockColor;
import unblonded.packets.util.Color;
import unblonded.packets.util.KitSlot;

import java.util.*;

public class cfg {
    public static ImBoolean drawBlocks = new ImBoolean(false);
    public static ImBoolean drawBlockTracer = new ImBoolean(false);
    public static List<BlockColor> espBlockList = new ArrayList<>();
    public static ImBoolean forwardTunnel = new ImBoolean(false);
    public static ImBoolean autoCrystal = new ImBoolean(false);
    public static int[] crystalAttackTime = {20};
    public static int[] crystalPlaceTime = {20};
    public static ImBoolean cancelInteraction = new ImBoolean(false);
    public static ImBoolean autoAnchor = new ImBoolean(false);
    public static int[] autoAnchorDelay = {15};
    public static int[] autoAnchorHumanity = {3};
    public static ImLong oreSimSeed = new ImLong(0L);
    public static ImBoolean oreSim = new ImBoolean(false);
    public static ImBoolean oreSimDrawMode = new ImBoolean(false);
    public static int[] oreSimDistance = {3};
    public static Color[] oreColors = new Color[] {
            new Color(0.10f, 0.15f, 0.30f), // Coal
            new Color(0.55f, 0.66f, 0.50f), // Iron
            new Color(0.95f, 0.05f, 0.65f), // Gold
            new Color(0.00f, 0.70f, 0.70f), // Redstone
            new Color(1.00f, 0.35f, 0.05f), // Diamond
            new Color(0.30f, 0.90f, 0.10f), // Lapis
            new Color(0.40f, 0.30f, 0.80f), // Copper
            new Color(1.00f, 0.95f, 0.00f), // Emerald
            new Color(0.60f, 0.85f, 1.00f), // Quartz
            new Color(0.10f, 1.00f, 0.10f)  // Ancient Debris
    };
    public static ImBoolean[] oreSimOptions = new ImBoolean[10];
    public static ImBoolean autoTotem = new ImBoolean(false);
    public static int[] autoTotemDelay = {50};
    public static int[] autoTotemHumanity = {0};
    public static boolean triggerAutoSell = false;
    public static int[] autoSellDelay = {300};
    public static ImString autoSellPrice = new ImString(256);
    public static ImInt[] autoSellEndpoints = {new ImInt(0), new ImInt(8)};
    public static int filterMode = 0;
    public static ImBoolean chatFilter = new ImBoolean(false);
    public static ImString blockMsg = new ImString(512);
    public static ImBoolean storageScan = new ImBoolean(false);
    public static ImString storageScanSearch = new ImString(512);
    public static float[] storageScanColor = {1.0f, 0.0f, 0.0f, 1.0f};
    public static ImBoolean storageScanShowInGui = new ImBoolean(true);
    public static ImBoolean aimAssistToggle = new ImBoolean(false);
    public static boolean aimAssistVisibility = false;
    public static ImBoolean crystalSpam = new ImBoolean(false);
    public static int[] crystalSpamSearchRadius = {5};
    public static int[] crystalSpamBreakDelay = {10};
    public static ImBoolean selfCrystal = new ImBoolean(false);
    public static int[] selfCrystalDelay = {15};
    public static int[] selfCrystalHumanity = {3};
    public static boolean showMenu;
    public static boolean showAll;
    public static ImBoolean displayPlayers = new ImBoolean(false);
    public static ImBoolean advEsp = new ImBoolean(false);
    public static ImBoolean advEspDrawType = new ImBoolean(false);
    public static ImString blockName = new ImString(256);
    public static float[] blockColor = {1.f, 1.f, 1.f, 1.f};
    public static int[] espRadius = {32};
    public static int[] espSearchTime = {1};
    public static ImBoolean checkPlayerAirSafety = new ImBoolean(false);
    public static ImBoolean isPlayerAirSafeShowStatus = new ImBoolean(true);
    public static ImBoolean freezePlayers = new ImBoolean(false);
    public static String tunnelBlockStatus;
    public static ImBoolean autoSell = new ImBoolean(false);
    public static ImBoolean autoDc = new ImBoolean(false);
    public static ImBoolean autoDcPrimed = new ImBoolean(false);
    public static ImFloat autoDcProximity = new ImFloat(5.0f);
    public static String[] chatFilterItems = new String[]{ "Mute", "Contains", "Not Contain" };
    public static ImBoolean fontSizeOverride = new ImBoolean(false);;
    public static float[] fontSize = {1.5f};
    public static boolean storageScanShow;
    public static float[] aimAssistRange = {6.8f};
    public static float[] aimAssistFov = {128.2f};
    public static float[] aimAssistSmoothness = {1.0f};
    public static float[] aimAssistMinSpeed = {143.0f};
    public static float[] aimAssistMaxSpeed = {146.0f};
    public static ImBoolean aimAssistVisibilityCheck = new ImBoolean(true);
    public static int[] aimAssistUpdateRate = {1};
    public static ImBoolean nightFx = new ImBoolean(false);
    public static float[] nightFxSize = {20.f};
    public static ImBoolean nightFxCrosshairLines = new ImBoolean(false);
    public static ImBoolean showFpsChart = new ImBoolean(false);
    public static ImBoolean showFpsChartInGame = new ImBoolean(false);
    public static int[] fpsChartSampleRate = {5};
    public static final Map<String, List<KitSlot>> savedLoadouts = new HashMap<>();
    public static final ImString loadoutNameInput = new ImString(16);
    public static String selectedLoadout = null;
    public static ImBoolean showLoadouts = new ImBoolean(false);
    public static ImBoolean handRender = new ImBoolean(false);
    public static float[] handRenderScale = {1.f};
    public static float[][] handRenderXYZ = {{0f},{0f},{0f}};
    public static ImBoolean totemNotifier = new ImBoolean(false);
    public static ImBoolean noRender = new ImBoolean(false);
    public static ImBoolean[] noRenderItems = new ImBoolean[] {
            new ImBoolean(false),
            new ImBoolean(false),
            new ImBoolean(false),
            new ImBoolean(false),
            new ImBoolean(false),
    };
    public static ImBoolean timeChanger = new ImBoolean(false);
    public static long[] timeChangerLTime = {1000L};
    public static ImBoolean playerEsp = new ImBoolean(false);
    public static Color playerEspColor = new Color(0f, 1f, 0.777f, .9f);
    public static ImBoolean playerEspObeyLighting = new ImBoolean(false);

    public static ImBoolean useMenuMode = new ImBoolean(false);
    public static ImBoolean autoCrystalCfg = new ImBoolean(false);
    public static ImBoolean advEspCfg = new ImBoolean(false);
    public static ImBoolean checkPlayerAirSafetyCfg = new ImBoolean(false);
    public static ImBoolean autoSellCfg = new ImBoolean(false);
    public static ImBoolean autoDcCfg = new ImBoolean(false);
    public static ImBoolean oreSimCfg = new ImBoolean(false);
    public static ImBoolean autoTotemCfg = new ImBoolean(false);
    public static ImBoolean chatFilterCfg = new ImBoolean(false);
    public static ImBoolean fontSizeCfg = new ImBoolean(false);
    public static ImBoolean aimAssistCfg = new ImBoolean(false);
    public static ImBoolean storageScanCfg = new ImBoolean(false);
    public static ImBoolean crystalSpamCfg = new ImBoolean(false);
    public static ImBoolean nightFxCfg = new ImBoolean(false);
    public static ImBoolean autoAnchorCfg = new ImBoolean(false);
    public static ImBoolean selfCrystalCfg = new ImBoolean(false);
    public static ImBoolean fpsChartCfg = new ImBoolean(false);
    public static ImBoolean handRenderCfg = new ImBoolean(false);
    public static ImBoolean noRenderCfg = new ImBoolean(false);
    public static ImBoolean timeChangerCfg = new ImBoolean(false);
    public static ImBoolean playerEspCfg = new ImBoolean(false);

    static {
        for (int i = 0; i < cfg.oreSimOptions.length; i++)
            cfg.oreSimOptions[i] = new ImBoolean(false);
    }
}
