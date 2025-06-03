package unblonded.packets.imgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Alert {
    private static final List<Alert> activeAlerts = new ArrayList<>();
    private static int nextId = 1;

    public enum AlertType {
        INFO(0.6f, 0.8f, 1.0f, 1.0f),      // Light Blue
        SUCCESS(0.6f, 1.0f, 0.6f, 1.0f),   // Light Green
        WARNING(1.0f, 1.0f, 0.6f, 1.0f),   // Light Yellow
        ERROR(1.0f, 0.6f, 0.6f, 1.0f);     // Light Red

        public final float r, g, b, a;

        AlertType(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }

    private final int id;
    private final String title;
    private final String message;
    private final AlertType type;
    private final long creationTime;
    private final long duration;
    private boolean isVisible;
    private float alpha;

    public Alert(String message) {
        this("Alert", message, AlertType.INFO, 2000);
    }

    public Alert(String message, AlertType type) {
        this("Alert", message, type, 2000);
    }

    public Alert(String message, AlertType type, long duration) {
        this("Alert", message, type, duration);
    }

    public Alert(String title, String message, AlertType type, long duration) {
        this.id = nextId++;
        this.title = title;
        this.message = message;
        this.type = type;
        this.duration = duration;
        this.creationTime = System.currentTimeMillis();
        this.isVisible = true;
        this.alpha = 1.0f;

        playSound();
        activeAlerts.add(this);
    }

    // Get the current font scale factor from ImGui
    private static float getFontScale() {
        return ImGui.getFontSize() / ImGui.getFont().getFontSize();
    }

    // Scale a value based on current font scale
    private static float scale(float value) {
        return value * getFontScale();
    }

    public static void renderAll() {
        if (activeAlerts.isEmpty()) return;

        try {
            Iterator<Alert> iterator = activeAlerts.iterator();
            int index = 0;

            while (iterator.hasNext()) {
                Alert alert = iterator.next();

                long elapsed = System.currentTimeMillis() - alert.creationTime;
                if (elapsed > alert.duration) {
                    float fadeTime = 500;
                    if (elapsed > alert.duration + fadeTime) {
                        iterator.remove();
                        continue;
                    } else {
                        alert.alpha = 1.0f - ((elapsed - alert.duration) / fadeTime);
                    }
                }

                if (alert.isVisible) {
                    alert.render(index);
                    index++;
                }
            }
        } catch (Exception e) {
            System.err.println("ImGui Alert rendering error: " + e.getMessage());
            activeAlerts.clear();
        }
    }

    private void render(int position) {
        // Scale all dimensions based on font scale
        float windowWidth = scale(320f);
        float windowHeight = scale(90f);
        float padding = scale(12f);
        float yOffset = position * (windowHeight + padding);

        ImVec2 displaySize = ImGui.getIO().getDisplaySize();
        float posX = displaySize.x - windowWidth - padding;
        float posY = padding + yOffset;

        ImGui.setNextWindowPos(posX, posY);
        ImGui.setNextWindowSize(windowWidth, windowHeight);

        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoScrollbar |
                ImGuiWindowFlags.NoFocusOnAppearing |
                ImGuiWindowFlags.NoNav |
                ImGuiWindowFlags.AlwaysAutoResize;

        ImGui.setNextWindowBgAlpha(0.85f * alpha);
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowRounding, scale(8.0f));
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowPadding, scale(16.0f), scale(12.0f));

        String windowName = "Alert##" + id;
        if (ImGui.begin(windowName, flags)) {
            try {
                float startX = ImGui.getCursorPosX();
                float startY = ImGui.getCursorPosY();

                // Scale the colored bar on the left
                ImVec2 windowPos = ImGui.getWindowPos();
                ImGui.getWindowDrawList().addRectFilled(
                        windowPos.x, windowPos.y,
                        windowPos.x + scale(4), windowPos.y + windowHeight,
                        ImGui.getColorU32(type.r, type.g, type.b, alpha)
                );

                String icon = switch (type) {
                    case INFO -> icons.INFO;
                    case SUCCESS -> icons.CHECK;
                    case WARNING -> icons.TRIANGLE_EXCLAMATION;
                    case ERROR -> icons.SKULL_CROSSBONES;
                };

                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text,
                        type.r, type.g, type.b, alpha);

                // Scale icon positioning
                ImGui.setCursorPosX(startX + scale(8));
                ImGui.text(icon);
                ImGui.sameLine();
                ImGui.setCursorPosX(startX + scale(32));

                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 0.9f * alpha);
                ImGui.textWrapped(title);
                ImGui.popStyleColor();
                ImGui.spacing();

                // Scale message positioning
                ImGui.setCursorPosX(startX + scale(8));
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 0.85f, 0.85f, 0.85f, 0.8f * alpha);
                ImGui.textWrapped(message);
                ImGui.popStyleColor();

                long elapsed = System.currentTimeMillis() - creationTime;
                float progress = Math.min(1.0f, (float) elapsed / duration);

                ImGui.spacing();
                ImGui.setCursorPosX(startX + scale(8));

                ImGui.pushStyleColor(imgui.flag.ImGuiCol.PlotHistogram, type.r * 0.8f, type.g * 0.8f, type.b * 0.8f, 0.6f * alpha);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.FrameBg, 0.2f, 0.2f, 0.2f, 0.3f * alpha);
                ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.FrameRounding, scale(2.0f));

                // Scale progress bar width and height
                ImGui.progressBar(progress, windowWidth - scale(60), scale(2));

                ImGui.popStyleVar();
                ImGui.popStyleColor(2);

                // Scale close button positioning and size
                float closeButtonSize = scale(30);
                ImGui.setCursorPos(windowWidth - (scale(10) + closeButtonSize), scale(8));
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, 0, 0, 0, 0); // Invisible background
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonHovered, 1.0f, 0.3f, 0.3f, 0.3f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonActive, 1.0f, 0.2f, 0.2f, 0.5f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 0.8f, 0.8f, 0.8f, alpha);

                if (ImGui.button(icons.XMARK, closeButtonSize, closeButtonSize)) close();
                ImGui.popStyleColor(5);

            } catch (Exception e) { close(); }
        }
        ImGui.end();

        ImGui.popStyleVar(2);
    }

    public void close() {
        isVisible = false;
        activeAlerts.remove(this);
    }

    public static void closeAll() {
        activeAlerts.clear();
    }

    public static int getActiveCount() {
        return activeAlerts.size();
    }

    public static Alert info(String message) {
        return new Alert(message, AlertType.INFO);
    }

    public static Alert success(String message) {
        return new Alert(message, AlertType.SUCCESS);
    }

    public static Alert warning(String message) {
        return new Alert(message, AlertType.WARNING);
    }

    public static Alert error(String message) {
        return new Alert(message, AlertType.ERROR);
    }

    public static Alert info(String title, String message) {
        return new Alert(title, message, AlertType.INFO, 3000);
    }

    public static Alert success(String title, String message) {
        return new Alert(title, message, AlertType.SUCCESS, 3000);
    }

    public static Alert warning(String title, String message) {
        return new Alert(title, message, AlertType.WARNING, 4000);
    }

    public static Alert error(String title, String message) {
        return new Alert(title, message, AlertType.ERROR, 5000);
    }

    public static void playSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0f, 2.0f);
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public AlertType getType() { return type; }
    public boolean isVisible() { return isVisible; }
}