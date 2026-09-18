package com.github.re_diverse.asumitrust;

import java.util.concurrent.TimeUnit;

import com.github.re_diverse.asumitrust.data.PlayerData;
import com.github.re_diverse.asumitrust.manager.DataManager;
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

	public static void connect() {
		instance.saveDefaultConfig();
		instance.reloadConfig();
		String address = instance.getConfig().getString("server.address");
		int port = instance.getConfig().getInt("server.port");
		String user = instance.getConfig().getString("server.user");
		String pass = instance.getConfig().getString("server.password");
		String schema = instance.getConfig().getString("server.schema");
		String driver = instance.getConfig().getString("database.class_name");
		int lifespan = instance.getConfig().getInt("database.lifespan");
		int maxPoolSize = instance.getConfig().getInt("database.max_pool_size", 0);
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
		if (maxPoolSize > 0)
			dataSource.setMaximumPoolSize(maxPoolSize);
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
