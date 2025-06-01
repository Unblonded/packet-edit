package unblonded.packets.imgui;

import imgui.*;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiFreeTypeBuilderFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.InputStream;

public class ImGuiManager {
    private static ImGuiManager instance;
    private ImGuiImplGlfw imGuiImplGlfw;
    private ImGuiImplGl3 imGuiImplGl3;
    private boolean initialized = false;

    public static ImGuiManager getInstance() {
        if (instance == null) instance = new ImGuiManager();
        return instance;
    }

    public void destroy() {
        instance = null;
    }

    public void init() {
        if (initialized) return;

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();

        io.setIniFilename("menu.ini");
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.setConfigViewportsNoTaskBarIcon(true);

        imGuiImplGlfw = new ImGuiImplGlfw();
        imGuiImplGl3 = new ImGuiImplGl3();

        imGuiImplGlfw.init(MinecraftClient.getInstance().getWindow().getHandle(), true);
        imGuiImplGl3.init("#version 330");

        io.getFonts().clear();
        loadFont("assets/packet-edit/fonts/segoeui.ttf", 18);
        loadFontAwesome("assets/packet-edit/fonts/fa-solid-900.ttf", 18);

        ImGuiThemes.cyberpunk();

        initialized = true;
    }


    public void newFrame() {
        if (!initialized) return;

        imGuiImplGl3.newFrame();
        imGuiImplGlfw.newFrame();
        ImGui.newFrame();
    }

    public void render() {
        if (!initialized) return;

        ImGui.endFrame();
        ImGui.render();
        imGuiImplGl3.renderDrawData(ImGui.getDrawData());


        ImGuiIO io = ImGui.getIO();
        if ((io.getConfigFlags() & ImGuiConfigFlags.ViewportsEnable) != 0) {
            final long backupWindowPtr = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            org.lwjgl.glfw.GLFW.glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    public void loadFont(String resourcePath, float fontSize) {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) return;

            ImGuiIO io = ImGui.getIO();
            io.getFonts().addFontFromMemoryTTF(
                    is.readAllBytes(),
                    ImGuiFreeTypeBuilderFlags.ForceAutoHint |
                            ImGuiFreeTypeBuilderFlags.LightHinting,
                    fontSize, io.getFonts().getGlyphRangesDefault()
            );
            io.setFonts(io.getFonts());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void loadFontAwesome(String fontloc, float fontSize) {
        ImGuiIO io = ImGui.getIO();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fontloc)) {
            if (is == null) return;

            ImFontConfig fontConfig = new ImFontConfig();
            fontConfig.setMergeMode(true);
            fontConfig.setPixelSnapH(true);

            ImFontGlyphRangesBuilder rangeBuilder = new ImFontGlyphRangesBuilder();
            rangeBuilder.addRanges(new short[]{(short) icons.ICON_MIN, (short) icons.ICON_MAX, 0});
            short[] ranges = rangeBuilder.buildRanges();

            io.getFonts().addFontFromMemoryTTF(
                    is.readAllBytes(),
                    fontSize,
                    fontConfig,
                    ranges
            );

            fontConfig.destroy();

        } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isInit() { return initialized; }
}