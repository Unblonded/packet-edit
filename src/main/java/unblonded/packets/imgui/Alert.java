package unblonded.packets.imgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
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

    // Constructors
    public Alert(String message) {
        this("Alert", message, AlertType.INFO, 3000);
    }

    public Alert(String message, AlertType type) {
        this("Alert", message, type, 3000);
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

        activeAlerts.add(this);
    }

    // Static method to render all active alerts
    public static void renderAll() {
        if (activeAlerts.isEmpty()) return;

        // Use try-catch to prevent ImGui assertion errors from crashing
        try {
            Iterator<Alert> iterator = activeAlerts.iterator();
            int index = 0;

            while (iterator.hasNext()) {
                Alert alert = iterator.next();

                // Check if alert should expire
                long elapsed = System.currentTimeMillis() - alert.creationTime;
                if (elapsed > alert.duration) {
                    // Fade out effect
                    float fadeTime = 500; // 500ms fade
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
            // If ImGui context is invalid, clear alerts to prevent further issues
            System.err.println("ImGui Alert rendering error: " + e.getMessage());
            activeAlerts.clear();
        }
    }

    private void render(int position) {
        // Calculate position (stack alerts vertically from top-right)
        float windowWidth = 320f;
        float windowHeight = 90f;
        float padding = 12f;
        float yOffset = position * (windowHeight + padding);

        // Get display size for positioning
        ImVec2 displaySize = ImGui.getIO().getDisplaySize();
        float posX = displaySize.x - windowWidth - padding;
        float posY = padding + yOffset;

        // Set window position and size
        ImGui.setNextWindowPos(posX, posY);
        ImGui.setNextWindowSize(windowWidth, windowHeight);

        // Window flags for sleek overlay
        int flags = ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoScrollbar |
                ImGuiWindowFlags.NoFocusOnAppearing |
                ImGuiWindowFlags.NoNav |
                ImGuiWindowFlags.AlwaysAutoResize;

        // Sleek semi-transparent background with slight blur effect
        ImGui.setNextWindowBgAlpha(0.85f * alpha);

        // Push rounded corners style
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowRounding, 8.0f);
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowPadding, 16.0f, 12.0f);

        // Create unique window name
        String windowName = "Alert##" + id;

        // Only render if ImGui context is valid
        if (ImGui.begin(windowName, flags)) {
            try {
                // Get current cursor position for layout
                float startX = ImGui.getCursorPosX();
                float startY = ImGui.getCursorPosY();

                // Color-coded left border/accent
                ImVec2 windowPos = ImGui.getWindowPos();
                ImGui.getWindowDrawList().addRectFilled(
                        windowPos.x, windowPos.y,
                        windowPos.x + 4, windowPos.y + windowHeight,
                        ImGui.getColorU32(type.r, type.g, type.b, alpha)
                );

                // Icon based on alert type (using Unicode symbols)
                String icon = switch (type) {
                    case INFO -> icons.INFO;
                    case SUCCESS -> icons.CHECK;
                    case WARNING -> icons.TRIANGLE_EXCLAMATION;
                    case ERROR -> icons.SKULL_CROSSBONES;
                };

                // Push larger font for icon if available
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text,
                        type.r, type.g, type.b, alpha);

                // Icon and title on same line
                ImGui.setCursorPosX(startX + 8); // Account for accent border
                ImGui.text(icon);
                ImGui.sameLine();
                ImGui.setCursorPosX(startX + 32);

                // Title with subtle emphasis
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text,
                        1.0f, 1.0f, 1.0f, 0.9f * alpha); // Bright white for title
                ImGui.textWrapped(title);
                ImGui.popStyleColor();

                // Small spacing instead of ugly separator
                ImGui.spacing();

                // Message with softer color
                ImGui.setCursorPosX(startX + 8);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text,
                        0.85f, 0.85f, 0.85f, 0.8f * alpha); // Soft white for message
                ImGui.textWrapped(message);
                ImGui.popStyleColor();

                // Progress bar showing remaining time (sleeker design)
                long elapsed = System.currentTimeMillis() - creationTime;
                float progress = Math.min(1.0f, (float) elapsed / duration);

                ImGui.spacing();
                ImGui.setCursorPosX(startX + 8);

                // Custom styled progress bar
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.PlotHistogram,
                        type.r * 0.8f, type.g * 0.8f, type.b * 0.8f, 0.6f * alpha);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.FrameBg,
                        0.2f, 0.2f, 0.2f, 0.3f * alpha);
                ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.FrameRounding, 2.0f);

                ImGui.progressBar(progress, windowWidth - 60, 2);

                ImGui.popStyleVar();
                ImGui.popStyleColor(2);

                // Elegant close button (top-right corner)
                ImGui.setCursorPos(windowWidth - (10 + 30), 8);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, 0, 0, 0, 0); // Invisible background
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonHovered, 1.0f, 0.3f, 0.3f, 0.3f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonActive, 1.0f, 0.2f, 0.2f, 0.5f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 0.8f, 0.8f, 0.8f, alpha);

                if (ImGui.button(icons.XMARK, 30, 30)) {
                    close();
                }

                ImGui.popStyleColor(5); // Pop all text and button colors

            } catch (Exception e) {
                // If there's an error rendering this alert, remove it
                close();
            }
        }
        ImGui.end();

        // Pop window styling
        ImGui.popStyleVar(2);
    }

    // Utility methods
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

    // Static convenience methods for quick alerts
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

    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public AlertType getType() { return type; }
    public boolean isVisible() { return isVisible; }
}