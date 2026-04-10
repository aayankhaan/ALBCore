package com.aayan.albcore.effect;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundEffect {

    public void play(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public void play(Player player, Sound sound) {
        play(player, sound, 1.0f, 1.0f);
    }

    public void play(Player player, String sound, float volume, float pitch) {
        try {
            play(player, Sound.valueOf(sound.toUpperCase()), volume, pitch);
        } catch (IllegalArgumentException e) {
            java.util.logging.Logger.getLogger("ALBCore")
                    .warning("[ALBCore | SoundEffect] Unknown sound: " + sound);
        }
    }
}