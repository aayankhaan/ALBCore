package com.aayan.albcore.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class ParticleBuilder {

    private final Particle particle;
    private Location location;
    private int count = 1;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double extra = 0;
    private Object data = null;

    public ParticleBuilder(Particle particle) {
        this.particle = particle;
    }

    public ParticleBuilder location(Location location) {
        this.location = location;
        return this;
    }

    public ParticleBuilder count(int count) {
        this.count = count;
        return this;
    }

    public ParticleBuilder offset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public ParticleBuilder speed(double speed) {
        this.extra = speed;
        return this;
    }

    public ParticleBuilder data(Object data) {
        this.data = data;
        return this;
    }

    /**
     * Set dust options (color and size) for REDSTONE/DUST particles.
     */
    public ParticleBuilder dust(Color color, float size) {
        this.data = new Particle.DustOptions(color, size);
        return this;
    }

    /**
     * Spawn the particle for everyone in the world.
     */
    public void spawn() {
        if (location == null || location.getWorld() == null) return;
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, data);
    }

    /**
     * Spawn the particle only for a specific player.
     */
    public void spawn(Player player) {
        if (location == null) return;
        player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, data);
    }

    /**
     * Spawn the particle for a collection of players.
     */
    public void spawn(Collection<Player> players) {
        if (location == null) return;
        for (Player player : players) {
            spawn(player);
        }
    }
}
