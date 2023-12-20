package com.igloosec.smartguard.next.agentmanager.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigGlobalManager {

	private static ConfigGlobalManager configGlobalInstance;

	private static Map<String, String> applicationCacheMap;

	public static synchronized ConfigGlobalManager getInstance(List<Map> list, boolean configGlobalReload) {
		if (configGlobalInstance == null) {
			configGlobalInstance = ConfigGlobalManagerHolder.instance;
		}

		if (configGlobalReload) {
			reload(list);
		}

		return configGlobalInstance;
	}

	public static void reload(List<Map> list) {
		if (list != null && !list.isEmpty()) {
			Map<String, String> configGlobalMap = new HashMap<String, String>();
			for(Map obj : list){
				configGlobalMap.put((String)obj.get("ITEM_NM"),(String) obj.get("ITEM_VALUE"));
			}

			if (configGlobalMap != null && !configGlobalMap.isEmpty()) {
				applicationCacheMap = configGlobalMap;
			}
		}
	}

	public static String getConfigGlobalValue(String itemName) {
		String itemValue = null;

		if (applicationCacheMap != null) {
			if (applicationCacheMap.containsKey(itemName)) {
				itemValue = applicationCacheMap.get(itemName);
			}
		}

		return itemValue;
	}

	private static class ConfigGlobalManagerHolder {
		static ConfigGlobalManager instance = new ConfigGlobalManager();
	}


}
