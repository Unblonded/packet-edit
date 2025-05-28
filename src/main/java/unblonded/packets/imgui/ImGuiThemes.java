package unblonded.packets.imgui;

import imgui.*;
import imgui.flag.ImGuiCol;
import unblonded.packets.cfg;

import java.util.Random;

public class ImGuiThemes{

    public static void cyberpunk() {
        ImGuiStyle style = ImGui.getStyle();

        // QUANTUM PRECISION SPACING - Premium ultra-wide spacing
        style.setWindowPadding(22, 22);              // Increased for more premium feel
        style.setFramePadding(16, 10);               // Enhanced button/control padding
        style.setItemSpacing(16, 12);                // More breathing room
        style.setItemInnerSpacing(10, 10);           // Consistent inner spacing
        style.setTouchExtraPadding(4, 4);            // Better touch responsiveness
        style.setIndentSpacing(30.0f);               // More pronounced indentation
        style.setScrollbarSize(18.0f);               // Slightly larger scrollbars
        style.setGrabMinSize(16.0f);                 // Larger grab controls
        style.setTabMinWidthForCloseButton(0.0f);

        // HOLOGRAPHIC ROUNDING - Perfect harmony between future and function
        style.setWindowRounding(10.0f);              // More pronounced window curves
        style.setChildRounding(8.0f);                // Consistent child window rounding
        style.setFrameRounding(8.0f);                // Consistent frame rounding
        style.setPopupRounding(10.0f);               // Match window rounding
        style.setScrollbarRounding(14.0f);           // More curved scrollbars
        style.setGrabRounding(6.0f);                 // Better grab rounding
        style.setTabRounding(8.0f);                  // Consistent tab rounding
        style.setWindowTitleAlign(0.5f, 0.5f);       // Centered titles

        // BORDER STYLING - Enhanced digital boundary
        style.setWindowBorderSize(1.5f);             // Slightly thicker borders
        style.setChildBorderSize(1.5f);              // Consistency in borders
        style.setPopupBorderSize(1.5f);              // Consistency in borders
        style.setFrameBorderSize(1.5f);              // Consistency in borders
        style.setTabBorderSize(1.5f);                // Consistency in borders

        // ULTRA ENHANCED COLOR PALETTE - Quantum spectrum visualization

        // BASE LAYERS - Black hole depths with neural backlight
        final ImVec4 voidBlack = new ImVec4(0.012f, 0.012f, 0.06f, 0.97f);          // Deeper, cleaner black
        final ImVec4 shadowPurple = new ImVec4(0.055f, 0.025f, 0.14f, 0.92f);       // Richer dark matter
        final ImVec4 deepViolet = new ImVec4(0.09f, 0.05f, 0.19f, 0.90f);           // Enhanced quantum foam
        final ImVec4 mediumPurple = new ImVec4(0.14f, 0.09f, 0.24f, 0.85f);         // Upgraded digital mist

        // NEON ARSENAL - Hypersaturated energy spectrum
        final ImVec4 plasmaPink = new ImVec4(1.00f, 0.07f, 0.70f, 0.95f);           // Intensified plasma
        final ImVec4 electricCyan = new ImVec4(0.05f, 0.95f, 1.00f, 0.95f);         // Enhanced lightning
        final ImVec4 voidPurple = new ImVec4(0.80f, 0.30f, 1.00f, 0.95f);           // Stronger psychic energy
        final ImVec4 matrixGreen = new ImVec4(0.28f, 1.00f, 0.38f, 0.95f);          // Enhanced code rain
        final ImVec4 solarOrange = new ImVec4(1.00f, 0.48f, 0.05f, 0.95f);          // Brighter digital fire
        final ImVec4 neuralBlue = new ImVec4(0.18f, 0.75f, 1.00f, 0.95f);           // Upgraded brain waves
        final ImVec4 quantumYellow = new ImVec4(1.00f, 0.97f, 0.20f, 0.95f);        // NEW: Quantum particles
        final ImVec4 bionicRed = new ImVec4(1.00f, 0.20f, 0.25f, 0.95f);            // NEW: Bionic circuits

        // SOLID VARIANTS - For maximum visibility when needed
        final ImVec4 solidPlasma = new ImVec4(1.00f, 0.07f, 0.70f, 1.00f);
        final ImVec4 solidCyan = new ImVec4(0.05f, 0.95f, 1.00f, 1.00f);
        final ImVec4 solidPurple = new ImVec4(0.80f, 0.30f, 1.00f, 1.00f);
        final ImVec4 solidGreen = new ImVec4(0.28f, 1.00f, 0.38f, 1.00f);
        final ImVec4 solidYellow = new ImVec4(1.00f, 0.97f, 0.20f, 1.00f);
        final ImVec4 solidRed = new ImVec4(1.00f, 0.20f, 0.25f, 1.00f);

        // TEXT HIERARCHY - Crystal clear communication
        final ImVec4 hologramWhite = new ImVec4(0.99f, 0.99f, 1.00f, 1.00f);       // Pure information
        final ImVec4 ghostGray = new ImVec4(0.78f, 0.80f, 0.88f, 0.95f);           // Secondary data
        final ImVec4 whisperGray = new ImVec4(0.58f, 0.60f, 0.68f, 0.75f);         // Disabled state

        // GLOW EFFECTS - Simulated with alpha layering
        final ImVec4 glowPink = new ImVec4(1.00f, 0.07f, 0.70f, 0.30f);
        final ImVec4 glowCyan = new ImVec4(0.05f, 0.95f, 1.00f, 0.30f);
        final ImVec4 glowPurple = new ImVec4(0.80f, 0.30f, 1.00f, 0.30f);
        final ImVec4 glowGreen = new ImVec4(0.28f, 1.00f, 0.38f, 0.30f);
        final ImVec4 glowYellow = new ImVec4(1.00f, 0.97f, 0.20f, 0.30f);
        final ImVec4 glowRed = new ImVec4(1.00f, 0.20f, 0.25f, 0.30f);

        // BACKGROUND GRADIENTS - Deep space layers
        final ImVec4 bgDeepSpace = new ImVec4(0.015f, 0.01f, 0.04f, 0.97f);
        final ImVec4 bgNebula = new ImVec4(0.04f, 0.02f, 0.10f, 0.95f);

        // FOUNDATION - The canvas of cyberspace
        style.setColor(ImGuiCol.Text, hologramWhite.x, hologramWhite.y, hologramWhite.z, hologramWhite.w);
        style.setColor(ImGuiCol.TextDisabled, whisperGray.x, whisperGray.y, whisperGray.z, whisperGray.w);
        style.setColor(ImGuiCol.WindowBg, voidBlack.x, voidBlack.y, voidBlack.z, voidBlack.w);
        style.setColor(ImGuiCol.ChildBg, shadowPurple.x, shadowPurple.y, shadowPurple.z, 0.65f);
        style.setColor(ImGuiCol.PopupBg, deepViolet.x, deepViolet.y, deepViolet.z, 0.97f);

        // BORDERS - The electric edges of reality
        style.setColor(ImGuiCol.Border, voidPurple.x, voidPurple.y, voidPurple.z, 0.60f);
        style.setColor(ImGuiCol.BorderShadow, plasmaPink.x, plasmaPink.y, plasmaPink.z, 0.15f);

        // INTERACTIVE SURFACES - Where user meets machine
        style.setColor(ImGuiCol.FrameBg, deepViolet.x, deepViolet.y, deepViolet.z, 0.70f);
        style.setColor(ImGuiCol.FrameBgHovered, voidPurple.x * 0.7f, voidPurple.y * 0.7f, voidPurple.z * 0.7f, 0.50f);
        style.setColor(ImGuiCol.FrameBgActive, voidPurple.x * 0.9f, voidPurple.y * 0.9f, voidPurple.z * 0.9f, 0.70f);

        // TITLE BARS - Command center aesthetics
        style.setColor(ImGuiCol.TitleBg, shadowPurple.x, shadowPurple.y, shadowPurple.z, 0.90f);
        style.setColor(ImGuiCol.TitleBgActive, voidPurple.x * 0.5f, voidPurple.y * 0.5f, voidPurple.z * 0.5f, 0.95f);
        style.setColor(ImGuiCol.TitleBgCollapsed, shadowPurple.x, shadowPurple.y, shadowPurple.z, 0.50f);

        // BUTTONS - The keys to digital power - Now with ALTERNATING COLORS
        style.setColor(ImGuiCol.Button, plasmaPink.x * 0.8f, plasmaPink.y * 0.8f, plasmaPink.z * 0.8f, 0.75f);
        style.setColor(ImGuiCol.ButtonHovered, plasmaPink.x * 1.1f, plasmaPink.y * 1.1f, plasmaPink.z * 1.1f, 0.90f);
        style.setColor(ImGuiCol.ButtonActive, Math.min(1.0f, plasmaPink.x + 0.20f), Math.min(1.0f, plasmaPink.y + 0.20f), Math.min(1.0f, plasmaPink.z + 0.20f), 1.00f);

        // HEADERS - Information superhighway
        style.setColor(ImGuiCol.Header, electricCyan.x * 0.7f, electricCyan.y * 0.7f, electricCyan.z * 0.7f, 0.70f);
        style.setColor(ImGuiCol.HeaderHovered, electricCyan.x * 1.1f, electricCyan.y * 1.1f, electricCyan.z * 1.1f, 0.85f);
        style.setColor(ImGuiCol.HeaderActive, Math.min(1.0f, electricCyan.x + 0.15f), Math.min(1.0f, electricCyan.y + 0.15f), Math.min(1.0f, electricCyan.z + 0.15f), 0.95f);

        // TABS - Enhanced navigation system
        style.setColor(ImGuiCol.Tab, deepViolet.x * 1.2f, deepViolet.y * 1.2f, deepViolet.z * 1.2f, 0.80f);
        style.setColor(ImGuiCol.TabHovered, voidPurple.x * 1.1f, voidPurple.y * 1.1f, voidPurple.z * 1.1f, 0.90f);
        style.setColor(ImGuiCol.TabActive, voidPurple.x, voidPurple.y, voidPurple.z, 0.95f);
        style.setColor(ImGuiCol.TabUnfocused, shadowPurple.x * 0.8f, shadowPurple.y * 0.8f, shadowPurple.z * 0.8f, 0.70f);
        style.setColor(ImGuiCol.TabUnfocusedActive, deepViolet.x * 1.2f, deepViolet.y * 1.2f, deepViolet.z * 1.2f, 0.80f);

        // SEPARATOR LINES - The grid of cyberspace
        style.setColor(ImGuiCol.Separator, electricCyan.x, electricCyan.y, electricCyan.z, 0.65f);
        style.setColor(ImGuiCol.SeparatorHovered, plasmaPink.x, plasmaPink.y, plasmaPink.z, 0.85f);
        style.setColor(ImGuiCol.SeparatorActive, matrixGreen.x, matrixGreen.y, matrixGreen.z, 0.95f);

        // SCROLLBARS - Navigating the data stream
        style.setColor(ImGuiCol.ScrollbarBg, shadowPurple.x, shadowPurple.y, shadowPurple.z, 0.45f);
        style.setColor(ImGuiCol.ScrollbarGrab, voidPurple.x, voidPurple.y, voidPurple.z, 0.75f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, Math.min(1.0f, voidPurple.x + 0.20f), Math.min(1.0f, voidPurple.y + 0.20f), Math.min(1.0f, voidPurple.z + 0.20f), 0.90f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, plasmaPink.x, plasmaPink.y, plasmaPink.z, 1.00f);

        // CONTROL ELEMENTS - Precision instruments
        style.setColor(ImGuiCol.CheckMark, solidGreen.x, solidGreen.y, solidGreen.z, solidGreen.w);
        style.setColor(ImGuiCol.SliderGrab, plasmaPink.x, plasmaPink.y, plasmaPink.z, 0.90f);
        style.setColor(ImGuiCol.SliderGrabActive, electricCyan.x, electricCyan.y, electricCyan.z, 1.00f);

        // TEXT SELECTION - Highlighting the important bits
        style.setColor(ImGuiCol.TextSelectedBg, electricCyan.x, electricCyan.y, electricCyan.z, 0.40f);

        // DRAG AND DROP - Moving data through space
        style.setColor(ImGuiCol.DragDropTarget, matrixGreen.x, matrixGreen.y, matrixGreen.z, 0.85f);

        // NAVIGATION - Moving through digital space
        style.setColor(ImGuiCol.NavHighlight, plasmaPink.x, plasmaPink.y, plasmaPink.z, 0.90f);
        style.setColor(ImGuiCol.NavWindowingHighlight, electricCyan.x, electricCyan.y, electricCyan.z, 0.80f);
        style.setColor(ImGuiCol.NavWindowingDimBg, 0.15f, 0.15f, 0.20f, 0.65f);

        // MODAL OVERLAYS - The focus field
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.06f, 0.06f, 0.12f, 0.75f);

        // TABLE SYSTEM - Organized data streams
        style.setColor(ImGuiCol.TableHeaderBg, deepViolet.x * 1.3f, deepViolet.y * 1.3f, deepViolet.z * 1.3f, 0.85f);
        style.setColor(ImGuiCol.TableBorderStrong, voidPurple.x, voidPurple.y, voidPurple.z, 0.75f);
        style.setColor(ImGuiCol.TableBorderLight, voidPurple.x, voidPurple.y, voidPurple.z, 0.40f);
        style.setColor(ImGuiCol.TableRowBg, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.TableRowBgAlt, shadowPurple.x, shadowPurple.y, shadowPurple.z, 0.30f);

        // RESIZE CONTROLS - Shaping reality
        style.setColor(ImGuiCol.ResizeGrip, voidPurple.x, voidPurple.y, voidPurple.z, 0.50f);
        style.setColor(ImGuiCol.ResizeGripHovered, plasmaPink.x, plasmaPink.y, plasmaPink.z, 0.75f);
        style.setColor(ImGuiCol.ResizeGripActive, electricCyan.x, electricCyan.y, electricCyan.z, 0.95f);

        // DATA VISUALIZATION - Making numbers beautiful
        style.setColor(ImGuiCol.PlotLines, matrixGreen.x, matrixGreen.y, matrixGreen.z, 0.90f);
        style.setColor(ImGuiCol.PlotLinesHovered, plasmaPink.x, plasmaPink.y, plasmaPink.z, 1.00f);
        style.setColor(ImGuiCol.PlotHistogram, electricCyan.x, electricCyan.y, electricCyan.z, 0.90f);
        style.setColor(ImGuiCol.PlotHistogramHovered, voidPurple.x, voidPurple.y, voidPurple.z, 1.00f);

        // FINAL TOUCHES - The polish that makes it shine
        style.setAlpha(0.97f);
    }

    public static void cosmicCrosshair() {
        float time = (float) ImGui.getTime();
        ImDrawList drawList = ImGui.getBackgroundDrawList();
        ImVec2 size = ImGui.getIO().getDisplaySize();
        ImVec2 center = new ImVec2(size.x / 2.0f, size.y / 2.0f);

        float radius = cfg.nightFxSize[0];
        float rotation = time * 1.2f;

        for (int ring = 0; ring < 5; ring++) {
            float ringRadius = radius * (0.4f + ring * 0.15f);
            float ringAlpha = 0.8f - ring * 0.15f;
            float ringPulse = 0.7f + 0.3f * (float) Math.sin(time * 3.0f + ring);

            float hue = 0.28f + 0.3f * ((float) Math.sin(time + ring * 0.5f) * 0.5f + 0.5f);
            float[] hsv = {hue, 0.9f, 1.0f};
            float[] rgb = new float[3];
            ImGui.colorConvertHSVtoRGB(hsv, rgb);
            float r = rgb[0], g = rgb[1], b = rgb[2];

            final int segments = 36;
            for (int i = 0; i < segments; i++) {
                float angle1 = rotation + 2.0f * (float) Math.PI * i / segments;
                float angle2 = rotation + 2.0f * (float) Math.PI * (i + 1) / segments;
                float wave = 1.0f + 0.15f * (float) Math.sin(angle1 * 6.0f + time * 4.0f);

                ImVec2 p1 = new ImVec2(
                        center.x + (float) Math.cos(angle1) * ringRadius * wave,
                        center.y + (float) Math.sin(angle1) * ringRadius * wave
                );
                ImVec2 p2 = new ImVec2(
                        center.x + (float) Math.cos(angle2) * ringRadius * wave,
                        center.y + (float) Math.sin(angle2) * ringRadius * wave
                );

                int segmentColor = ImGui.getColorU32(
                        r, g, b, ringAlpha * ringPulse * 0.95f
                );
                drawList.addLine(p1.x, p1.y, p2.x, p2.y, segmentColor, 2.0f);
            }
        }

        final int rays = 8;
        for (int i = 0; i < rays; i++) {
            float rayAngle = rotation * 0.5f + 2.0f * (float) Math.PI * i / rays;
            float rayLength = radius * 1.5f * (0.5f + 0.5f * (float) Math.sin(time * 2.0f + i));
            ImVec2 rayEnd = new ImVec2(
                    center.x + (float) Math.cos(rayAngle) * rayLength,
                    center.y + (float) Math.sin(rayAngle) * rayLength
            );

            float rayHue = 0.3f + 0.1f * (float) Math.sin(time * 2.0f + i);
            float[] hsv = {rayHue, 0.9f, 1.0f};
            float[] rgb = new float[3];
            ImGui.colorConvertHSVtoRGB(hsv, rgb);
            float r = rgb[0], g = rgb[1], b = rgb[2];
            float rayAlpha = 0.7f + 0.3f * (float) Math.sin(time * 5.0f + i);

            int rayColor = ImGui.getColorU32(
                    r, g, b, rayAlpha * 0.95f
            );
            drawList.addLine(center.x, center.y, rayEnd.x, rayEnd.y, rayColor, 2.5f);
        }

        if (cfg.nightFxCrosshairLines.get()) {
            final float crossLength = radius * 0.7f;
            int crossColor = ImGui.getColorU32(1.0f, 1.0f, 1.0f, 0.95f);

            float horizontalPulse = 0.8f + 0.2f * (float) Math.sin(time * 4.0f);
            drawList.addLine(
                    center.x - crossLength * horizontalPulse, center.y,
                    center.x + crossLength * horizontalPulse, center.y,
                    crossColor, 2.0f
            );

            float verticalPulse = 0.8f + 0.2f * (float) Math.sin(time * 4.0f + 0.5f);
            drawList.addLine(
                    center.x, center.y - crossLength * verticalPulse,
                    center.x, center.y + crossLength * verticalPulse,
                    crossColor, 2.0f
            );
        }
    }
}