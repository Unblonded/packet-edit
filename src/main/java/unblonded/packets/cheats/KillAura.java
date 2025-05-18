package unblonded.packets.cheats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import unblonded.packets.util.util;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class KillAura {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Random random = new Random();

    // Main settings
    public static boolean enabled = false;
    public static float range = 4.5f;
    public static float fov = 120f; // Field of view in degrees
    public static boolean targetPlayers = true;
    public static boolean targetMobs = true;
    public static boolean throughWalls = false;

    // Attack settings
    public static boolean autoDisable = false; // Auto-disable in certain situations
    public static float minCPS = 10f; // Minimum clicks per second
    public static float maxCPS = 14f; // Maximum clicks per second
    public static boolean smartDelay = true; // Adjust timing based on weapon type
    public static boolean respectCooldown = true; // Wait for weapon cooldown
    public static float cooldownThreshold = 0.9f; // Attack when cooldown reaches this value (0-1)
    public static boolean criticals = true; // Try to perform critical hits

    // Rotation settings
    public static boolean rotations = true; // Whether to look at targets
    public static float rotationSpeed = 8.0f; // How fast to rotate (lower = smoother)
    public static boolean randomizeRotations = true; // Add slight inaccuracy to rotations

    // Anti-detection features
    public static boolean swing = true; // Whether to swing arm
    public static boolean rayTrace = true; // Use ray tracing to validate targets
    public static boolean silentRotations = true; // Server-side rotations only
    public static boolean stopOnEating = true; // Don't attack while eating
    public static boolean stopOnMining = true; // Don't attack while mining

    // Internal state variables
    private static long lastAttackTime = 0;
    private static LivingEntity currentTarget = null;
    private static float serverYaw = 0f;
    private static float serverPitch = 0f;
    private static boolean attackedThisTick = false;

    public static void tick() {
        if (!enabled || client.player == null || client.world == null) return;

        // Auto-disable checks
        if (autoDisable && shouldDisable()) {
            enabled = false;
            return;
        }

        if (client.player.getAttackCooldownProgress(0.0f) < cooldownThreshold) return;


        // Reset attack flag at the start of each tick
        attackedThisTick = false;

        // Find best target
        LivingEntity target = findTarget();
        currentTarget = target;

        if (target != null) {
            // Handle rotations
            if (rotations) {
                handleRotations(target);
            }

            // Handle attack timing
            if (shouldAttack()) {
                attack(target);
            }
        }
    }

    private static LivingEntity findTarget() {
        if (client.player == null || client.world == null) return null;

        List<LivingEntity> targets = client.world.getEntitiesByClass(LivingEntity.class, getTargetBox(), entity ->
                entity != client.player &&
                        entity.isAlive() &&
                        isValidTarget(entity)
        );

        // Sort by priority and distance
        return targets.stream()
                .sorted(Comparator.<LivingEntity>comparingInt(entity -> {
                    // Prioritize players over mobs if both are enabled
                    if (targetPlayers && entity instanceof PlayerEntity) return 0;
                    if (targetMobs && !(entity instanceof PlayerEntity)) return 1;
                    return 2;
                }).thenComparingDouble(entity -> entity.squaredDistanceTo(client.player)))
                .filter(entity -> isInFOV(entity, fov))
                .findFirst()
                .orElse(null);
    }

    private static Box getTargetBox() {
        return client.player.getBoundingBox().expand(range);
    }

    private static boolean isValidTarget(LivingEntity entity) {
        // Check if the entity type is valid for targeting
        if (entity instanceof PlayerEntity && !targetPlayers) return false;
        if (!(entity instanceof PlayerEntity) && !targetMobs) return false;

        // Check line of sight if needed
        if (!throughWalls && !client.player.canSee(entity)) return false;

        // Additional checks for player entities
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;

            // Skip creative mode players
            if (player.getAbilities().creativeMode) return false;

            // Skip team members (if on same team)
            if (client.player.isTeammate(entity)) return false;

            // Skip invisible players (optional - could be a setting)
            if (player.isInvisible()) return false;
        }

        // Make sure the entity can actually be hit
        if (entity.isInvulnerable()) return false;

        // Check if there's a clear path to the target if rayTrace is enabled
        if (rayTrace) {
            Vec3d eyePos = client.player.getEyePos();
            Vec3d targetPos = entity.getBoundingBox().getCenter();

            BlockHitResult result = client.world.raycast(new RaycastContext(
                    eyePos,
                    targetPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    client.player
            ));

            if (result.getType() == HitResult.Type.BLOCK) return false;
        }

        return true;
    }

    private static boolean isInFOV(Entity entity, float fov) {
        Vec3d lookVec = client.player.getRotationVec(1.0F);
        Vec3d targetVec = new Vec3d(
                entity.getX() - client.player.getX(),
                entity.getEyeY() - client.player.getEyeY(),
                entity.getZ() - client.player.getZ()
        ).normalize();

        double dotProduct = lookVec.dotProduct(targetVec);
        double angle = Math.toDegrees(Math.acos(dotProduct));

        return angle <= fov / 2;
    }

    private static void handleRotations(LivingEntity target) {
        // Calculate ideal rotations to the target
        Vec3d eyePos = client.player.getEyePos();

        // Target the upper body/head region instead of center for more realistic hits
        Vec3d targetPos = new Vec3d(
                target.getX(),
                target.getY() + target.getHeight() * 0.85,
                target.getZ()
        );

        // Calculate direction vector
        double deltaX = targetPos.x - eyePos.x;
        double deltaY = targetPos.y - eyePos.y;
        double deltaZ = targetPos.z - eyePos.z;

        // Calculate yaw and pitch
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));

        // Add some randomization to rotations if enabled
        if (randomizeRotations) {
            targetYaw += (random.nextFloat() - 0.5f) * 2f;
            targetPitch += (random.nextFloat() - 0.5f) * 1f;
        }

        // Ensure angles are within bounds
        targetYaw = MathHelper.wrapDegrees(targetYaw);
        targetPitch = MathHelper.clamp(targetPitch, -90F, 90F);

        // Smooth rotations
        float yawDifference = MathHelper.wrapDegrees(targetYaw - serverYaw);
        float pitchDifference = targetPitch - serverPitch;

        serverYaw += yawDifference / rotationSpeed;
        serverPitch += pitchDifference / rotationSpeed;

        // Apply rotations to player (client or server side)
        if (silentRotations) {
            // Only send rotation packets without changing client-side rotation
            // This would require sending a custom packet or modifying rotation packets
            // Implementation depends on your mod framework
            sendRotationPacket(serverYaw, serverPitch);
        } else {
            // Change client-side rotation directly
            client.player.setYaw(serverYaw);
            client.player.setPitch(serverPitch);
        }
    }

    private static void sendRotationPacket(float yaw, float pitch) {
        // Implementation depends on your mod framework
        // Example: client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(yaw, pitch, client.player.isOnGround()));
    }

    private static boolean shouldAttack() {
        if (client.player == null) return false;

        // Don't attack if we're eating or mining
        if (stopOnEating && client.player.isUsingItem()) return false;
        if (stopOnMining && client.options.attackKey.isPressed()) return false;

        // Check weapon cooldown if enabled
        if (respectCooldown) {
            float cooldown = client.player.getAttackCooldownProgress(0.0f);
            if (cooldown < (float)util.signedRandom(0.06)) return false;
        }

        // Check if enough time has passed since last attack based on CPS
        long currentTime = System.currentTimeMillis();
        float cps = random.nextFloat() * (maxCPS - minCPS) + minCPS;
        long attackDelay = (long) (1000 / cps);

        // Add weapon-specific delays if smart delay is enabled
        if (smartDelay) {
            Item heldItem = client.player.getMainHandStack().getItem();
            // Add additional delay based on weapon type
            if (heldItem instanceof SwordItem) {
                attackDelay += 50; // Additional delay for swords
            } else if (heldItem instanceof AxeItem) {
                attackDelay += 100; // More delay for axes (slower weapons)
            }
        }

        return currentTime - lastAttackTime >= attackDelay;
    }

    private static void attack(LivingEntity target) {
        if (client.player == null || client.interactionManager == null) return;

        // Set the attack time
        lastAttackTime = System.currentTimeMillis();
        attackedThisTick = true;

        client.interactionManager.attackEntity(client.player, target);
        if (swing) client.player.swingHand(Hand.MAIN_HAND);
    }

    private static boolean shouldDisable() {
        if (client.player == null) return false;
        if (client.currentScreen != null) return true;
        if (!client.player.isAlive()) return true;
        return client.interactionManager.getCurrentGameMode().equals(GameMode.SPECTATOR);
    }

    // Call this method to toggle the module
    public static void toggle() {
        enabled = !enabled;
        if (enabled) {
            // Reset state when enabling
            lastAttackTime = 0;
            currentTarget = null;
            serverYaw = client.player.getYaw();
            serverPitch = client.player.getPitch();
        }
    }
}