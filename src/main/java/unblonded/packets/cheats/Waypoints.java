package unblonded.packets.cheats;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import unblonded.packets.cfg;
import unblonded.packets.util.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Waypoints {
    public static class Point {
        public final BlockPos pos;
        public final Color color;
        public final String name;

        public Point(BlockPos pos, String name, Color color) {
            this.pos = pos;
            this.name = name;
            this.color = color;
        }

        public Point(BlockPos pos, String name) { this(pos, name, new Color()); }

        public Point(BlockPos pos) { this(pos, "Waypoint " + Waypoints.saved.size(), new Color()); }
    }

    public static final Queue<Point> saved = new ConcurrentLinkedQueue<>();

    public static class WaypointManager {
        private static final ImString newWaypointName = new ImString(64);
        private static final ImFloat[] newWaypointColor = new ImFloat[]{new ImFloat(1.0f), new ImFloat(1.0f), new ImFloat(1.0f)};
        private static final ImString editingName = new ImString(64);
        private static final ImFloat[] editingColor = new ImFloat[]{new ImFloat(1.0f), new ImFloat(1.0f), new ImFloat(1.0f)};
        private static Waypoints.Point editingPoint = null;
        private static boolean showAddDialog = false;
        private static boolean showEditDialog = false;
        private static String toDelete = null;

        public static void render() {
            ImGui.setNextWindowSize(600, 400, imgui.flag.ImGuiCond.FirstUseEver);
            if (ImGui.begin("Waypoint Manager", cfg.waypointsCfg)) {
                renderMainMenu();
            }
            ImGui.end();

            if (showAddDialog) {
                renderAddDialog();
            }

            if (showEditDialog && editingPoint != null) {
                renderEditDialog();
            }
        }

        private static void renderMainMenu() {
            // Header buttons
            if (ImGui.button("Add Current Position")) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null) {
                    BlockPos pos = mc.player.getBlockPos();
                    newWaypointName.set("Waypoint " + (Waypoints.saved.size() + 1));
                    showAddDialog = true;
                }
            }

            ImGui.sameLine();
            if (ImGui.button("Add Custom Position")) {
                newWaypointName.set("Custom Waypoint");
                showAddDialog = true;
            }

            ImGui.sameLine();
            if (ImGui.button("Clear All")) {
                if (ImGui.isItemHovered()) {
                    ImGui.setTooltip("Delete all waypoints");
                }
                Waypoints.saved.clear();
            }

            ImGui.separator();

            // Waypoint table
            if (ImGui.beginTable("Waypoints", 6, ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.Resizable)) {
                ImGui.tableSetupColumn("Name");
                ImGui.tableSetupColumn("X");
                ImGui.tableSetupColumn("Y");
                ImGui.tableSetupColumn("Z");
                ImGui.tableSetupColumn("Color");
                ImGui.tableSetupColumn("Actions");
                ImGui.tableHeadersRow();

                // Convert to list for indexed access
                List<Point> waypointList = new ArrayList<>(Waypoints.saved);

                for (int i = 0; i < waypointList.size(); i++) {
                    Waypoints.Point point = waypointList.get(i);
                    ImGui.tableNextRow();

                    // Name
                    ImGui.tableNextColumn();
                    ImGui.text(point.name);

                    // Coordinates
                    ImGui.tableNextColumn();
                    ImGui.text(String.valueOf(point.pos.getX()));
                    ImGui.tableNextColumn();
                    ImGui.text(String.valueOf(point.pos.getY()));
                    ImGui.tableNextColumn();
                    ImGui.text(String.valueOf(point.pos.getZ()));

                    // Color preview
                    ImGui.tableNextColumn();
                    float[] color = point.color.asFloatArr();
                    ImGui.colorButton("##color" + i, color);

                    // Actions
                    ImGui.tableNextColumn();

                    if (ImGui.smallButton("Edit##" + i)) {
                        editingPoint = point;
                        editingName.set(point.name);
                        editingColor[0].set(point.color.R());
                        editingColor[1].set(point.color.G());
                        editingColor[2].set(point.color.B());
                        showEditDialog = true;
                    }

                    ImGui.sameLine();
                    if (ImGui.smallButton("Delete##" + i)) {
                        toDelete = point.name;
                    }
                }

                ImGui.endTable();
            }

            // Handle deletion
            if (toDelete != null) {
                Iterator<Point> iterator = Waypoints.saved.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().name.equals(toDelete)) {
                        iterator.remove();
                        break;
                    }
                }
                toDelete = null;
            }
        }

        private static void renderAddDialog() {
            ImGui.setNextWindowSize(400, 300, imgui.flag.ImGuiCond.FirstUseEver);
            ImGui.setNextWindowPos(ImGui.getMainViewport().getCenterX() - 200, ImGui.getMainViewport().getCenterY() - 150, imgui.flag.ImGuiCond.FirstUseEver);

            if (ImGui.begin("Add Waypoint", new ImBoolean(showAddDialog))) {
                ImGui.text("Add a new waypoint:");
                ImGui.separator();

                // Name input
                ImGui.text("Name:");
                ImGui.inputText("##name", newWaypointName);

                // Position input
                MinecraftClient mc = MinecraftClient.getInstance();
                int[] pos = new int[3];
                if (mc.player != null) {
                    BlockPos playerPos = mc.player.getBlockPos();
                    pos[0] = playerPos.getX();
                    pos[1] = playerPos.getY();
                    pos[2] = playerPos.getZ();
                }

                ImGui.text("Position:");
                ImGui.inputInt3("##pos", pos);

                // Color picker
                ImGui.text("Color:");
                ImGui.colorEdit3("##color", new float []{newWaypointColor[0].get(), newWaypointColor[1].get(), newWaypointColor[2].get()});

                ImGui.separator();

                // Buttons
                if (ImGui.button("Add Waypoint")) {
                    Color color = new Color(
                            (int)(newWaypointColor[0].get() * 255),
                            (int)(newWaypointColor[1].get() * 255),
                            (int)(newWaypointColor[2].get() * 255)
                    );

                    Waypoints.Point newPoint = new Waypoints.Point(
                            new BlockPos(pos[0], pos[1], pos[2]),
                            newWaypointName.get(),
                            color
                    );

                    Waypoints.saved.add(newPoint);
                    showAddDialog = false;
                    newWaypointName.set("");
                }

                ImGui.sameLine();
                if (ImGui.button("Cancel")) {
                    showAddDialog = false;
                }
            }
            ImGui.end();
        }

        private static void renderEditDialog() {
            ImGui.setNextWindowSize(400, 300, imgui.flag.ImGuiCond.FirstUseEver);
            ImGui.setNextWindowPos(ImGui.getMainViewport().getCenterX() - 200, ImGui.getMainViewport().getCenterY() - 150, imgui.flag.ImGuiCond.FirstUseEver);

            if (ImGui.begin("Edit Waypoint", new ImBoolean(showEditDialog))) {
                ImGui.text("Edit waypoint: " + editingPoint.name);
                ImGui.separator();

                // Name input
                ImGui.text("Name:");
                ImGui.inputText("##editname", editingName);

                // Position display (read-only for now, you can make it editable)
                ImGui.text("Position:");
                ImGui.text("X: " + editingPoint.pos.getX() + ", Y: " + editingPoint.pos.getY() + ", Z: " + editingPoint.pos.getZ());

                // Color picker
                ImGui.text("Color:");
                ImGui.colorEdit3("##editcolor", new float[]{editingColor[0].get(), editingColor[1].get(), editingColor[2].get()});

                ImGui.separator();

                // Buttons
                if (ImGui.button("Save Changes")) {
                    // Remove old waypoint
                    Waypoints.saved.removeIf(p -> p == editingPoint);

                    // Add updated waypoint
                    Color newColor = new Color(
                            (int)(editingColor[0].get() * 255),
                            (int)(editingColor[1].get() * 255),
                            (int)(editingColor[2].get() * 255)
                    );

                    Waypoints.Point updatedPoint = new Waypoints.Point(
                            editingPoint.pos,
                            editingName.get(),
                            newColor
                    );

                    Waypoints.saved.add(updatedPoint);
                    showEditDialog = false;
                    editingPoint = null;
                }

                ImGui.sameLine();
                if (ImGui.button("Cancel")) {
                    showEditDialog = false;
                    editingPoint = null;
                }
            }
            ImGui.end();
        }

        // Utility method to get distance to waypoint
        public static double getDistanceToWaypoint(Waypoints.Point point) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return 0;

            BlockPos playerPos = mc.player.getBlockPos();
            return Math.sqrt(
                    Math.pow(point.pos.getX() - playerPos.getX(), 2) +
                            Math.pow(point.pos.getY() - playerPos.getY(), 2) +
                            Math.pow(point.pos.getZ() - playerPos.getZ(), 2)
            );
        }
    }
}

