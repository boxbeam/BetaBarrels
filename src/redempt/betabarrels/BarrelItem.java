package redempt.betabarrels;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import redempt.redlib.itemutils.ItemBuilder;
import redempt.redlib.itemutils.ItemUtils;
import redempt.redlib.json.JSONMap;
import redempt.redlib.json.JSONParser;

public class BarrelItem {
	
	private static NamespacedKey key = new NamespacedKey(BetaBarrels.plugin, "item");
	
	public static ItemStack get(ItemStack contained, int count) {
		JSONMap map = new JSONMap();
		map.put("count", count);
		map.put("item", ItemUtils.toString(contained));
		return new ItemBuilder(Material.BARREL)
				.addPersistentTag(key, PersistentDataType.STRING, map.toString())
				.addLore(BetaBarrels.getDisplay(contained, count));
	}
	
	public static void handlePlace(Barrel barrel, ItemStack data) {
		ItemMeta meta = data.getItemMeta();
		if (meta == null) {
			return;
		}
		String json = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
		if (json == null) {
			return;
		}
		JSONMap map = JSONParser.parseMap(json);
		ItemStack item = ItemUtils.fromString(map.getString("item"));
		int count = map.getInt("count");
		barrel.setItem(item);
		barrel.setCount(count);
	}
	
}
