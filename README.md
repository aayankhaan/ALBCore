# 💎 ALBCore API Documentation

Welcome to the **ALBCore** SDK. This core provides a robust, fluent, and high-performance framework for building modern Minecraft plugins.

## 🚀 Getting Started

To access the API, simply use the static accessor:
```java
ALBCoreAPI api = ALBCore.api();
```

---

## 🛠️ Item Management

### ItemBuilder
Create complex items with NBT, rarities, and custom textures in seconds.

```java
ItemStack item = api.item(Material.DIAMOND_SWORD)
    .name("<gradient:#00aaff:#aa00ff>Excalibur</gradient>")
    .lore("<gray>A legendary blade.", "<yellow>Rarity: %rarity%")
    .rarity("LEGENDARY")
    .unbreakable(true)
    .glow()
    .enchant(Enchantment.SHARPNESS, 5)
    .customNBT("ability_id", "lightning_strike")
    .build();
```

### Custom Head (Base64)
```java
ItemStack head = api.item(Material.PLAYER_HEAD)
    .skullBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I0...")
    .build();
```

### Item Serialization
Turn items into strings for database storage and back.
```java
String encoded = api.toBase64(item);
ItemStack decoded = api.fromBase64(encoded);
```

---

## ⚡ Ability & Trigger System

Register callbacks for items without writing complex event listeners.

```java
api.item(Material.BLAZE_ROD)
    .name("<red>Fire Wand")
    .onClick(5000, "<red>Wait {remaining}!", player -> {
        api.particles(Particle.FLAME).location(player.getLocation()).count(50).spawn();
        player.sendMessage("🔥 FIRE!");
    })
    .buildAndRegister("fire_wand");
```

**Supported Triggers:**
`ON_HOLD`, `ON_SNEAK`, `ON_CLICK`, `ON_BREAK`, `ON_FISH`, `ON_BLOCK_DAMAGE`, `ON_ATTACK`, `ON_DEFEND`, `ON_WEAR`.

---

## 🖥️ User Interface (GUI)

### Simple GUI
```java
api.gui()
    .title("<dark_gray>My Menu")
    .rows(3)
    .filler(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
    .slot(13, myItem, player -> player.sendMessage("Clicked!"))
    .open(player);
```

### Paged GUI
```java
api.pagedGui()
    .title("All Players")
    .addItem(item1)
    .addItem(item2)
    .open(player);
```

---

## ⌨️ Player Input (Anvil & Sign)

Get text input from players safely using ProtocolLib and Paper 1.21 APIs.

### Sign Input (Requires ProtocolLib)
```java
api.signInput()
    .lines("", "^^^^^^^", "Enter Name", "")
    .onComplete(lines -> {
        String input = lines[0];
        player.sendMessage("You entered: " + input);
    })
    .open(player);
```

### Anvil Input
```java
api.anvilInput()
    .title("Rename Item")
    .text("Initial Value")
    .onComplete(text -> player.sendMessage("Result: " + text))
    .open(player);
```

---

## ⏱️ Cooldown Manager

Support for both temporary and database-persistent cooldowns.

```java
// Memory only (resets on restart)
api.cooldowns().set(uuid, "ability", 5000);

// Persistent (survives restart)
api.cooldowns().setPersistent(uuid, "daily_reward", 86400000);

if (api.cooldowns().isOnCooldown(uuid, "daily_reward")) {
    String time = api.cooldowns().getFormatted(uuid, "daily_reward");
    player.sendMessage("Wait " + time);
}
```

---

## ✨ Visuals & Particles

### ParticleBuilder
Fluent particle spawning.
```java
api.particles(Particle.DUST)
    .location(loc)
    .count(30)
    .offset(0.5, 0.5, 0.5)
    .dust(Color.RED, 1.5f)
    .speed(0.1)
    .spawn();
```

### Text, Titles & ActionBars
Full support for **MiniMessage**, **Legacy Colors**, and **PlaceholderAPI**.

```java
// Send rich chat messages
TextUtil.send(player, "<gradient:#ff0000:#ffff00>Gradients and <bold>MiniMessage</bold> support!");

// Support for legacy & codes
TextUtil.send(player, "&aLegacy support works too!");

// Action bars
TextUtil.sendActionbar(player, "<yellow>Action Bar Message");

// Titles & Subtitles
TextUtil.sendTitle(player, "<green>Victory!", "<gray>You won the match", 10, 70, 20);

// Component parsing
Component comp = TextUtil.parse("<red>Custom Component");
```

---

## 🔗 Hooks & Integrations

ALBCore automatically detects and hooks into these plugins:
*   **Vault:** `api.vault().give(player, 100);`
*   **ShopGUI+:** `double price = api.shop().getBuyPrice(item);`
*   **PlaceholderAPI:** `String parsed = api.placeholders().resolve("stat_kills", player);`
*   **WorldGuard/Iridium:** `boolean canBuild = ProtectionHook.canBreakBlock(player, loc);`
*   **FancyHolograms:** `FancyHologramsHook.create("test", loc, List.of("Line 1"));`

---

## 📊 Database & Stats

Safe async player data storage.
```java
// Stats (Double based)
api.stats().incrementAsync(uuid, "kills", 1.0);

// Generic Data (String based)
api.db().setDataAsync(uuid, "rank", "MVP+");
```
