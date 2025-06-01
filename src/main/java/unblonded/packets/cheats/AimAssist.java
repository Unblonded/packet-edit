package unblonded.packets.cheats;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;
import unblonded.packets.Packetedit;

import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.Timer;

public class AimAssist {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // Configurable parameters
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static boolean enabled = false;
    private static float range = 6.0f;              // Maximum distance to target players
    private static float fov = 60.0f;               // Field of view angle in degrees
    private static float smoothness = 1.5f;         // Higher = smoother (takes longer to reach target)
    private static float minSpeed = 90f;           // Minimum speed of aim adjustment
    private static float maxSpeed = 100f;           // Maximum speed of aim adjustment
    private static boolean visibilityCheck = true;  // Only target visible players
    private static int updateRate = 10;             // Timer update rate in milliseconds

    // Internal state
    private static PlayerEntity currentTarget = null;
    private static long lastUpdateTime = 0;
    private static Timer aimTimer;
    private static boolean timerRunning = false;


    public static void onInitializeClient() {
        executor.submit(() -> {
            while (true) {
                try {
                    if (enabled && mc.player != null && mc.world != null) {
                        mc.execute(AimAssist::update);
                    }
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static void update() {
        if (!enabled || mc.player == null || mc.world == null) {
            if (timerRunning) {
                stopTimer();
            }
            return;
        }

        // Find the best target
        PlayerEntity target = findTarget();
        currentTarget = target;

        if (target != null) {
            if (!timerRunning) {
                startTimer();
            }
        } else if (timerRunning) {
            stopTimer();
        }
    }

    private static void startTimer() {
        if (timerRunning) {
            return;
        }

        aimTimer = new Timer("AimAssistTimer", true);
        aimTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mc.player == null || currentTarget == null || !enabled) {
                    stopTimer();
                    return;
                }

                // Calculate deltaTime based on actual elapsed time
                long currentTime = System.currentTimeMillis();
                float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
                lastUpdateTime = currentTime;

                // Prevent unusually large delta times
                if (deltaTime > 0.1f) {
                    deltaTime = 0.01f;
                }

                adjustAim(currentTarget, deltaTime);
            }
        }, 0, updateRate); // Update every few milliseconds for smoother movement

        timerRunning = true;
        lastUpdateTime = System.currentTimeMillis();
    }

    private static void stopTimer() {
        if (aimTimer != null) {
            aimTimer.cancel();
            aimTimer = null;
        }
        timerRunning = false;
    }

    private static PlayerEntity findTarget() {
        if (mc.player == null || mc.world == null) {
            return null;
        }

        ClientPlayerEntity player = mc.player;
        Box searchBox = player.getBoundingBox().expand(range);

        List<PlayerEntity> potentialTargets = mc.world.getEntitiesByClass(
                PlayerEntity.class,
                searchBox,
                AimAssist::isValidTarget
        );

        if (potentialTargets.isEmpty()) {
            return null;
        }

        // Sort by priority (closest to crosshair and closest distance)
        return potentialTargets.stream()
                .sorted(Comparator.comparingDouble(AimAssist::getTargetPriority))
                .collect(Collectors.toList())
                .get(0);
    }

    private static double getTargetPriority(PlayerEntity target) {
        // Distance factor
        double distance = mc.player.squaredDistanceTo(target);

        // Angle factor - how close the target is to our crosshair
        float[] rotations = calculateRotation(target);
        float yawDiff = Math.abs(MathHelper.wrapDegrees(rotations[0] - mc.player.getYaw()));
        float pitchDiff = Math.abs(MathHelper.wrapDegrees(rotations[1] - mc.player.getPitch()));
        double angleDistance = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

        // Combined score (weight angle higher than distance)
        return angleDistance * 3.0 + distance;
    }

    private static void adjustAim(PlayerEntity target, float deltaTime) {
        // Calculate target rotation to the player
        float[] targetRotation = calculateRotation(target);
        float targetYaw = targetRotation[0];
        float targetPitch = targetRotation[1];

        // Current rotations
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        // Calculate delta between current and target angles
        float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDelta = MathHelper.wrapDegrees(targetPitch - currentPitch);

        // Don't move if target is outside FOV
        if (Math.abs(yawDelta) > fov || Math.abs(pitchDelta) > fov) {
            return;
        }

        // Calculate the magnitude of the total angular movement needed
        double totalDelta = Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
        if (totalDelta <= 0.001) return; // Avoid division by zero

        // Calculate a single movement speed based on the total angle difference
        float movementSpeed = calculateSpeed((float)totalDelta) * deltaTime;

        // Limit the total movement to our calculated speed
        float movementFactor = movementSpeed / (float)totalDelta;
        if (movementFactor > 1.0f) movementFactor = 1.0f;

        // Apply movement proportionally to both axes to maintain direction
        float yawMovement = yawDelta * movementFactor / smoothness;
        float pitchMovement = pitchDelta * movementFactor / smoothness;

        // Apply the smooth rotations while maintaining direction
        float newYaw = currentYaw + yawMovement;
        float newPitch = MathHelper.clamp(currentPitch + pitchMovement, -90.0f, 90.0f);

        // Set the new rotations
        mc.player.setYaw(newYaw);
        mc.player.setPitch(newPitch);
    }

    private static float[] calculateRotation(PlayerEntity target) {
        Vec3d eyePos = mc.player.getEyePos();

        // Aim for upper chest/head area
        Vec3d targetPos = target.getPos().add(0, target.getStandingEyeHeight() * 0.9, 0);

        double diffX = targetPos.x - eyePos.x;
        double diffY = targetPos.y - eyePos.y;
        double diffZ = targetPos.z - eyePos.z;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        return new float[] {yaw, pitch};
    }

    private static float calculateSpeed(float angleDifference) {
        // Map angleDifference to speed (small angles = slow, large angles = faster)
        return MathHelper.clamp(angleDifference / 5.0f, minSpeed, maxSpeed);
    }

    private static boolean isValidTarget(PlayerEntity entity) {
        if (entity == mc.player) {
            return false;
        }

        if (entity.isRemoved() || !entity.isAlive() || entity.isInvisible()) {
            return false;
        }

        if (visibilityCheck && !canSee(entity)) {
            return false;
        }

        return true;
    }

    private static boolean canSee(PlayerEntity entity) {
        if (mc.player == null || mc.world == null) return false;

        Vec3d start = mc.player.getCameraPosVec(1.0f);
        Vec3d end = entity.getPos().add(0, entity.getEyeHeight(entity.getPose()) * 0.9, 0);

        RaycastContext context = new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        );

        return mc.world.raycast(context).getType() == HitResult.Type.MISS;
    }

    public static void applySettings(float newRange, float newFov, float newSmoothness,
                                     float newMinSpeed, float newMaxSpeed, boolean newVisibilityCheck,
                                     int newUpdateRate) {
        range = newRange;
        fov = newFov;
        smoothness = newSmoothness;
        minSpeed = newMinSpeed;
        maxSpeed = newMaxSpeed;
        visibilityCheck = newVisibilityCheck;
        updateRate = newUpdateRate;
    }

    public static void setState(boolean enabled) {
        if (AimAssist.enabled == enabled) return;
        AimAssist.enabled = enabled;

        System.out.println("AimAssist " + (enabled ? "enabled" : "disabled"));

        if (enabled) {
            applySettings(
                    range,
                    fov,
                    smoothness,
                    minSpeed,
                    maxSpeed,
                    visibilityCheck,
                    updateRate
            );

            AimAssist.lastUpdateTime = System.currentTimeMillis();
            if (currentTarget != null && !timerRunning) {
                startTimer();
            }
        }
    }

}