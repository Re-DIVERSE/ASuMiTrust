package com.github.aburaagetarou.asumitrust.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.github.aburaagetarou.asumitrust.ASuMiTrust;
import com.github.aburaagetarou.asumitrust.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DataManager {
	private static final Map<String, PlayerData> players = new HashMap<>();

	public static void read(String uuid) {
		if (players.containsKey(uuid))
			return;
		PlayerData data = new PlayerData(uuid);
		data.lock();
		data.load();
		players.put(uuid, data);
	}

	public static void readAsync(String uuid) {
		if (players.containsKey(uuid))
			return;
		PlayerData data = new PlayerData(uuid);
		data.lock();
		Bukkit.getScheduler().runTaskTimerAsynchronously(ASuMiTrust.instance, task -> {
			if (!data.checkSync()) {
				Objects.requireNonNull(data);
				Bukkit.getScheduler().runTaskAsynchronously(ASuMiTrust.instance, data::load);
				players.put(uuid, data);
				task.cancel();
			}
		}, 60L, 20L);
	}

	public static PlayerData getData(String uuid) {
		if (!players.containsKey(uuid)) {
			read(uuid);
			unloadDataWithTimer(uuid);
		}
		return players.get(uuid);
	}

	public static void saveAll() {
		for (String key : players.keySet()) {
			PlayerData data = players.get(key);
			data.save();
		}
	}

	public static void unloadAll(boolean save) {
		Set<String> keys = players.keySet();
		for (String key : keys)
			unload(key, save);
	}

	public static void unload(String uuid, boolean save) {
		if (!players.containsKey(uuid))
			return;
		PlayerData data = players.get(uuid);
		if (data.isLocked()) {
			players.remove(uuid);
			return;
		}
		if (save) {
			Bukkit.getScheduler().runTaskAsynchronously(ASuMiTrust.instance, () -> {
				data.save();
				data.clearAll();
				players.remove(uuid);
			});
		} else {
			data.clearAll();
			players.remove(uuid);
		}
	}

	public static void unloadDataWithTimer(String uuidStr) {
		UUID uuid = UUID.fromString(uuidStr);
		PlayerData data = getData(uuidStr);
		Bukkit.getScheduler().runTaskTimer(ASuMiTrust.instance, task -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null && player.isOnline()) {
				task.cancel();
				return;
			}
			data.lifeSpan--;
			if (data.lifeSpan <= 0L) {
				unload(uuidStr, true);
				task.cancel();
			}
		}, 20L, 20L);
	}
}
