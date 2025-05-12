# Keep main mod initializer so Fabric can call it
-keep class unblonded.packets.Packetedit {
    public <init>();
    public void onInitializeClient();
}

# Keep the entrypoint method if you register via mod.json
-keepclassmembers class * {
    @net.fabricmc.api.ClientModInitializer public void onInitializeClient();
}

# Keep all Mixin configs and annotations
-keep class org.spongepowered.asm.mixin.Mixin { *; }
-keep class org.spongepowered.asm.mixin.MixinEnvironment { *; }
-keep class org.spongepowered.asm.mixin.** { *; }

# Keep mixin targets and configuration classes
-keep class * implements org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
-keep class * {
    @org.spongepowered.asm.mixin.Mixin *;
}

# Keep logger (optional)
-keep class org.slf4j.Logger { *; }
-keep class org.slf4j.LoggerFactory { *; }

# Optional: don't obfuscate class names but obfuscate method/field names
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Allow obfuscation of fields/methods inside classes
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

# Avoid stripping annotations
-keepattributes *Annotation*

# If using lambdas or inner classes
-keepattributes InnerClasses,EnclosingMethod

# General optimizations
-optimizationpasses 5
-dontpreverify
-dontwarn
