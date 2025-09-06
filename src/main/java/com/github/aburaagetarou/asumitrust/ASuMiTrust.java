package com.github.aburaagetarou.asumitrust;

import java.util.concurrent.TimeUnit;

import com.github.aburaagetarou.asumitrust.data.PlayerData;
import com.github.aburaagetarou.asumitrust.manager.DataManager;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ASuMiTrust extends JavaPlugin {
	public static ASuMiTrust instance;

	public static HikariDataSource dataSource;

	@Override
	public void onEnable() {
		instance = this;
		Bukkit.getPluginManager().registerEvents(new Listeners(), this);
		connect();
		PlayerData.createTable();
		long savePeriod = getConfig().getLong("data.save_period", 30000L);
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, DataManager::saveAll, savePeriod, savePeriod);
	}

	@Override
	public void onDisable() {
		instance = null;
		Bukkit.getScheduler().cancelTasks(this);
		DataManager.unloadAll(true);
		dataSource.close();
	}

	public void connect() {
		saveDefaultConfig();
		reloadConfig();
		String address = getConfig().getString("server.address");
		int port = getConfig().getInt("server.port");
		String user = getConfig().getString("server.user");
		String pass = getConfig().getString("server.password");
		String schema = getConfig().getString("server.schema");
		String driver = getConfig().getString("database.class_name");
		int lifespan = getConfig().getInt("database.lifespan");
		if (!Utilities.strNullCheck(address, user, pass, schema, driver))
			throw new IllegalStateException("いずれかの設定が正しくありません。");
		if (!Utilities.portCheck(port))
			throw new IllegalStateException("ポート番号の設定が正しくありません。");
		if (lifespan == 0)
			throw new IllegalStateException("接続維持時間の設定が正しくありません。");
		dataSource = new HikariDataSource();
		dataSource.setDriverClassName(driver);
		String url = String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s&useSSL=false", address, port, schema, user, pass);
		dataSource.setJdbcUrl(url);
		if (lifespan > 0)
			dataSource.setMaxLifetime(TimeUnit.MINUTES.toMillis(lifespan));
	}

	public static void error(String... messages) {
		if (instance == null)
			return;
		instance.getLogger().warning("========== DiversePlayerData Error ==========");
		instance.getLogger().warning("");
		for (String msg : messages)
			instance.getLogger().warning(msg);
		instance.getLogger().warning("");
		instance.getLogger().warning("=============================================");
	}

	public static void error(String message, String uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null)
			error(message, player.getName());
	}
}
