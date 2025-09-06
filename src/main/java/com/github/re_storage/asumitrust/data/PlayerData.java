package com.github.re_storage.asumitrust.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.github.re_storage.asumitrust.ASuMiTrust;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class PlayerData {
	public int exp;

	public int money;

	public int rate;

	public Location spawnPoint;

	public String syougouNow;

	public short syougouMax;

	public List<String> syougou;

	public Map<Short, ItemData> stash;

	public long lifeSpan;

	boolean locked;

	String uuid;

	public static boolean createTable() {
		try {
			Connection con = ASuMiTrust.dataSource.getConnection();
			try {
				StringBuilder sql = new StringBuilder();
				sql.append(" CREATE TABLE IF NOT EXISTS PlayerDataSync ( ");
				sql.append("     owner       varchar(40)    NOT NULL     ");
				sql.append("   , status      smallint       NOT NULL     ");
				sql.append(" )                                           ");
				PreparedStatement stmtSync = con.prepareStatement(sql.toString());
				stmtSync.executeUpdate();
				stmtSync.close();

				sql = new StringBuilder();
				sql.append(" CREATE TABLE IF NOT EXISTS PlayerData (      ");
				sql.append("     uuid             varchar(40)    NOT NULL ");
				sql.append("   , exp              int            NOT NULL ");
				sql.append("   , money            int            NOT NULL ");
				sql.append("   , spawnpoint_world varchar(256)   NULL     ");
				sql.append("   , spawnpoint_x     float          NULL     ");
				sql.append("   , spawnpoint_y     float          NULL     ");
				sql.append("   , spawnpoint_z     float          NULL     ");
				sql.append("   , spawnpoint_yaw   float          NULL     ");
				sql.append("   , spawnpoint_pitch float          NULL     ");
				sql.append("   , PRIMARY KEY (uuid)                       ");
				sql.append(" )                                            ");
				PreparedStatement stmtData = con.prepareStatement(sql.toString());
				stmtData.executeUpdate();
				stmtData.close();
				boolean bool = true;
				con.close();
				return bool;
			} catch (Throwable throwable) {
				if (con != null)
					try {
						con.close();
					} catch (Throwable throwable1) {
						throwable.addSuppressed(throwable1);
					}
				throw throwable;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public PlayerData(String uuid) {
		this.uuid = uuid;
		this.lifeSpan = 1800L;
	}

	public void lock() {
		lock(true);
	}

	public void lock(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}

	public void clearAll() {
		this.exp = 0;
		this.money = 0;
		this.rate = 0;
		this.spawnPoint = null;
		this.syougouNow = null;
		this.syougouMax = 0;
		this.syougou = null;
		this.stash = null;
	}

	public boolean load() {
		try {
			Connection con = ASuMiTrust.dataSource.getConnection();
			try {
				this.locked = true;
				clearAll();
				int success = 0;
				StringBuilder sql = new StringBuilder();
				sql.append(" SELECT uuid             ");
				sql.append("      , exp              ");
				sql.append("      , money            ");
				sql.append("      , rate             ");
				sql.append("      , spawnpoint_world ");
				sql.append("      , spawnpoint_x     ");
				sql.append("      , spawnpoint_y     ");
				sql.append("      , spawnpoint_z     ");
				sql.append("      , spawnpoint_yaw   ");
				sql.append("      , spawnpoint_pitch ");
				sql.append("      , sg_now           ");
				sql.append("      , sg_max           ");
				sql.append(" FROM   PlayerData       ");
				sql.append(" WHERE  uuid = ?         ");
				PreparedStatement stmtPlayerData = con.prepareStatement(sql.toString());
				stmtPlayerData.setString(1, this.uuid);
				ResultSet result = stmtPlayerData.executeQuery();
				if (result.next()) {
					this.exp = result.getInt("exp");
					this.money = result.getInt("money");
					this.rate = result.getInt("rate");
					String worldName = result.getString("spawnpoint_world");
					float x = result.getFloat("spawnpoint_x");
					float y = result.getFloat("spawnpoint_y");
					float z = result.getFloat("spawnpoint_z");
					float yaw = result.getFloat("spawnpoint_yaw");
					float pitch = result.getFloat("spawnpoint_pitch");
					this.syougouNow = result.getString("sg_now");
					this.syougouMax = result.getShort("sg_max");
					if (worldName != null) {
						World world = Bukkit.getWorld(worldName);
						if (world != null)
							this.spawnPoint = new Location(world, x, y, z, yaw, pitch);
					}
					success++;
				}
				result.close();
				stmtPlayerData.close();
//				sql = new StringBuilder();
//				sql.append(" SELECT uuid          ");
//				sql.append("      , row_nmb       ");
//				sql.append("      , syougou       ");
//				sql.append(" FROM   PlayerSyougou ");
//				sql.append(" WHERE  uuid = ?      ");
//				PreparedStatement stmtSyougou = con.prepareStatement(sql.toString());
//				stmtSyougou.setNString(1, this.uuid);
//				result = stmtSyougou.executeQuery();
//				this.syougou = new ArrayList<>();
//				while (result.next()) {
//					if (result.isFirst())
//						success += 16;
//					this.syougou.add(result.getString("syougou"));
//				}
//				result.close();
//				stmtSyougou.close();
//				sql = new StringBuilder();
//				sql.append(" SELECT uuid        ");
//				sql.append("      , slot        ");
//				sql.append("      , box         ");
//				sql.append("      , id          ");
//				sql.append("      , amount      ");
//				sql.append("      , nbt         ");
//				sql.append(" FROM   PlayerStash ");
//				sql.append(" WHERE  uuid = ?    ");
//				PreparedStatement stmtStash = con.prepareStatement(sql.toString());
//				stmtStash.setNString(1, this.uuid);
//				result = stmtStash.executeQuery();
//				this.stash = new HashMap<>();
//				while (result.next()) {
//					if (result.isFirst())
//						success += 256;
//					short slot = result.getShort("slot");
//					ItemData item = new ItemData();
//					item.box = result.getInt("box");
//					item.id = result.getInt("id");
//					item.amount = result.getShort("amount");
//					item.nbt = result.getString("nbt");
//					this.stash.put(slot, item);
//				}
//				result.close();
//				stmtStash.close();
				this.locked = false;
				if (success >= 273) {
					boolean bool1 = true;
					con.close();
					return bool1;
				}
				boolean bool = false;
				con.close();
				return bool;
			} catch (Throwable throwable) {
				if (con != null)
					try {
						con.close();
					} catch (Throwable throwable1) {
						throwable.addSuppressed(throwable1);
					}
				throw throwable;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			this.locked = false;
			return false;
		}
	}

	public boolean checkSync() {
		try {
			Connection con = ASuMiTrust.dataSource.getConnection();
			try {
				StringBuilder sql = new StringBuilder();
				sql.append(" SELECT status         ");
				sql.append(" FROM   PlayerDataSync ");
				sql.append(" WHERE  owner = ?      ");
				PreparedStatement stmt = con.prepareStatement(sql.toString());
				stmt.setString(1, this.uuid);
				ResultSet result = stmt.executeQuery();
				boolean isSync = false;
				if (result.next())
					isSync = true;
				result.close();
				stmt.close();
				boolean bool1 = isSync;
				con.close();
				return bool1;
			} catch (Throwable throwable) {
				if (con != null)
					try {
						con.close();
					} catch (Throwable throwable1) {
						throwable.addSuppressed(throwable1);
					}
				throw throwable;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void save() {
		ASuMiTrust.instance.getLogger().info("saving " + this.uuid);
		try {
			Connection con = ASuMiTrust.dataSource.getConnection();
			try {
				con.setAutoCommit(false);
				StringBuilder sql = new StringBuilder();
				sql.append(" INSERT INTO PlayerDataSync ( ");
				sql.append("         owner                ");
				sql.append("       , status               ");
				sql.append(" ) VALUES (                   ");
				sql.append("        ?                     ");
				sql.append("      , ?                     ");
				sql.append(" )                            ");
				PreparedStatement stmtSync = con.prepareStatement(sql.toString());
				stmtSync.setString(1, this.uuid);
				stmtSync.setShort(2, (short)1);
				int upd = stmtSync.executeUpdate();
				stmtSync.close();
				if (upd != 1) {
					con.rollback();
					con.close();
					return;
				}
				sql = new StringBuilder();
				sql.append(" UPDATE PlayerData SET       ");
				sql.append("        exp              = ? ");
				sql.append("      , money            = ? ");
				sql.append("      , rate             = ? ");
				sql.append("      , spawnpoint_world = ? ");
				sql.append("      , spawnpoint_x     = ? ");
				sql.append("      , spawnpoint_y     = ? ");
				sql.append("      , spawnpoint_z     = ? ");
				sql.append("      , spawnpoint_yaw   = ? ");
				sql.append("      , spawnpoint_pitch = ? ");
				sql.append("      , sg_now           = ? ");
				sql.append("      , sg_max           = ? ");
				sql.append(" WHERE  uuid             = ? ");
				PreparedStatement stmtPlayerDataUpd = con.prepareStatement(sql.toString());
				String world = null;
				float x = 0.0f;
				float y = 0.0f;
				float z = 0.0f;
				float yaw = 0.0f;
				float pitch = 0.0f;
				if(this.spawnPoint != null) {
					world = this.spawnPoint.getWorld().getName();
					x = (float)this.spawnPoint.getX();
					y = (float)this.spawnPoint.getY();
					z = (float)this.spawnPoint.getZ();
					yaw = this.spawnPoint.getYaw();
					pitch = this.spawnPoint.getPitch();
				}
				int cnt = 1;
				stmtPlayerDataUpd.setInt(cnt++, this.exp);
				stmtPlayerDataUpd.setInt(cnt++, this.money);
				stmtPlayerDataUpd.setInt(cnt++, this.rate);
				stmtPlayerDataUpd.setString(cnt++, world);
				stmtPlayerDataUpd.setFloat(cnt++, x);
				stmtPlayerDataUpd.setFloat(cnt++, y);
				stmtPlayerDataUpd.setFloat(cnt++, z);
				stmtPlayerDataUpd.setFloat(cnt++, yaw);
				stmtPlayerDataUpd.setFloat(cnt++, pitch);
				stmtPlayerDataUpd.setString(cnt++, this.syougouNow);
				stmtPlayerDataUpd.setShort(cnt++, this.syougouMax);
				stmtPlayerDataUpd.setString(cnt, this.uuid);
				int resCnt = stmtPlayerDataUpd.executeUpdate();
				stmtPlayerDataUpd.close();
				if (resCnt <= 0) {
					sql = new StringBuilder();
					sql.append(" INSERT INTO PlayerData ( ");
					sql.append("        uuid              ");
					sql.append("      , exp               ");
					sql.append("      , money             ");
					sql.append("      , rate              ");
					sql.append("      , spawnpoint_world  ");
					sql.append("      , spawnpoint_x      ");
					sql.append("      , spawnpoint_y      ");
					sql.append("      , spawnpoint_z      ");
					sql.append("      , spawnpoint_yaw    ");
					sql.append("      , spawnpoint_pitch  ");
					sql.append("      , sg_now            ");
					sql.append("      , sg_max            ");
					sql.append(" ) VALUES (               ");
					sql.append("        ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append("      , ?                 ");
					sql.append(" )                        ");
					PreparedStatement stmtPlayerDataIns = con.prepareStatement(sql.toString());
					cnt = 1;
					stmtPlayerDataIns.setString(cnt++, this.uuid);
					stmtPlayerDataIns.setInt(cnt++, this.exp);
					stmtPlayerDataIns.setInt(cnt++, this.money);
					stmtPlayerDataIns.setInt(cnt++, this.rate);
					stmtPlayerDataIns.setString(cnt++, world);
					stmtPlayerDataIns.setFloat(cnt++, x);
					stmtPlayerDataIns.setFloat(cnt++, y);
					stmtPlayerDataIns.setFloat(cnt++, z);
					stmtPlayerDataIns.setFloat(cnt++, yaw);
					stmtPlayerDataIns.setFloat(cnt++, pitch);
					stmtPlayerDataIns.setString(cnt++, this.syougouNow);
					stmtPlayerDataIns.setShort(cnt, this.syougouMax);
					stmtPlayerDataIns.executeUpdate();
					stmtPlayerDataIns.close();
				}
				short rowNumber = 1;
				if (this.syougou != null)
					for (String sg : this.syougou) {
						sql = new StringBuilder();
						sql.append(" UPDATE PlayerSyougou SET ");
						sql.append("        syougou = ?       ");
						sql.append(" WHERE  uuid    = ?       ");
						sql.append(" AND    row_nmb = ?       ");
						PreparedStatement stmtPlayerSyougouUpd = con.prepareStatement(sql.toString());
						stmtPlayerSyougouUpd.setString(1, sg);
						stmtPlayerSyougouUpd.setString(2, this.uuid);
						stmtPlayerSyougouUpd.setShort(3, rowNumber);
						resCnt = stmtPlayerSyougouUpd.executeUpdate();
						stmtPlayerSyougouUpd.close();
						if (resCnt <= 0) {
							sql = new StringBuilder();
							sql.append(" INSERT INTO PlayerSyougou ( ");
							sql.append("        uuid                 ");
							sql.append("      , row_nmb              ");
							sql.append("      , syougou              ");
							sql.append(" ) VALUES (                  ");
							sql.append("        ?                    ");
							sql.append("      , ?                    ");
							sql.append("      , ?                    ");
							sql.append(" )                           ");
							PreparedStatement stmtPlayerSyougouIns = con.prepareStatement(sql.toString());
							stmtPlayerSyougouIns.setString(1, this.uuid);
							stmtPlayerSyougouIns.setShort(2, rowNumber);
							stmtPlayerSyougouIns.setString(3, sg);
							stmtPlayerSyougouIns.executeUpdate();
							stmtPlayerSyougouIns.close();
						}
						rowNumber = (short)(rowNumber + 1);
					}
				if (this.stash != null) {
					short slot;
					for (slot = 0; slot < 27; slot = (short)(slot + 1)) {
						ItemData item;
						if (!this.stash.containsKey(Short.valueOf(slot))) {
							item = new ItemData();
							item.box = -1;
							item.id = -1;
							item.amount = 0;
							item.nbt = "";
						} else {
							item = this.stash.get(Short.valueOf(slot));
						}
						sql = new StringBuilder();
						sql.append(" UPDATE PlayerStash SET  ");
						sql.append("        box    = ?       ");
						sql.append("      , id     = ?       ");
						sql.append("      , amount = ?       ");
						sql.append("      , nbt    = ?       ");
						sql.append(" WHERE  uuid   = ?       ");
						sql.append(" AND    slot   = ?       ");
						PreparedStatement stmtPlayerStashUpd = con.prepareStatement(sql.toString());
						stmtPlayerStashUpd.setInt(1, item.box);
						stmtPlayerStashUpd.setInt(2, item.id);
						stmtPlayerStashUpd.setShort(3, item.amount);
						stmtPlayerStashUpd.setString(4, item.nbt);
						stmtPlayerStashUpd.setString(5, this.uuid);
						stmtPlayerStashUpd.setShort(6, slot);
						resCnt = stmtPlayerStashUpd.executeUpdate();
						stmtPlayerStashUpd.close();
						if (resCnt <= 0) {
							sql = new StringBuilder();
							sql.append(" INSERT INTO PlayerStash ( ");
							sql.append("        uuid               ");
							sql.append("      , slot               ");
							sql.append("      , box                ");
							sql.append("      , id                 ");
							sql.append("      , amount             ");
							sql.append("      , nbt                ");
							sql.append(" ) VALUES (                ");
							sql.append("        ?                  ");
							sql.append("      , ?                  ");
							sql.append("      , ?                  ");
							sql.append("      , ?                  ");
							sql.append("      , ?                  ");
							sql.append("      , ?                  ");
							sql.append(" )                         ");
							PreparedStatement stmtPlayerStashIns = con.prepareStatement(sql.toString());
							stmtPlayerStashIns.setString(1, this.uuid);
							stmtPlayerStashIns.setShort(2, slot);
							stmtPlayerStashIns.setInt(3, item.box);
							stmtPlayerStashIns.setInt(4, item.id);
							stmtPlayerStashIns.setShort(5, item.amount);
							stmtPlayerStashIns.setString(6, item.nbt);
							stmtPlayerStashIns.executeUpdate();
							stmtPlayerStashIns.close();
						}
					}
				}
				con.commit();
				con.setAutoCommit(true);
				con.close();
			} catch (Throwable throwable) {
				if (con != null)
					try {
						con.close();
					} catch (Throwable throwable1) {
						throwable.addSuppressed(throwable1);
					}
				throw throwable;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			try {
				Connection con = ASuMiTrust.dataSource.getConnection();
				try {
					StringBuilder sql = new StringBuilder();
					sql.append(" DELETE FROM PlayerDataSync ");
					sql.append(" WHERE  owner = ?           ");
					PreparedStatement stmtSync = con.prepareStatement(sql.toString());
					stmtSync.setString(1, this.uuid);
					int count = stmtSync.executeUpdate();
					stmtSync.close();
					if (count != 1)
						con.rollback();
					con.commit();
					con.setAutoCommit(true);
					con.close();
				} catch (Throwable throwable) {
					if (con != null)
						try {
							con.close();
						} catch (Throwable throwable1) {
							throwable.addSuppressed(throwable1);
						}
					throw throwable;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int hashCode() {
		int result = 0;
		result += this.uuid.hashCode();
		result += this.locked ? 1 : 0;
		return result;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof PlayerData))
			return false;
		PlayerData data = (PlayerData)obj;
		return (data.uuid.equals(this.uuid) && data.locked == this.locked);
	}
}
