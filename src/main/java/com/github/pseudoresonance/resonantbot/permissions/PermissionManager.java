package com.github.pseudoresonance.resonantbot.permissions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.github.pseudoresonance.resonantbot.data.Data;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class PermissionManager {

	private static HashMap<PermissionGroup, PermissionGroup[]> definedGroups = new HashMap<PermissionGroup, PermissionGroup[]>();
	private static HashMap<PermissionGroup, HashSet<PermissionGroup>> inheritanceCache = new HashMap<PermissionGroup, HashSet<PermissionGroup>>();
	
	public static void init() {
		definedGroups.put(PermissionGroup.BOT_OWNER, new PermissionGroup[] {PermissionGroup.BOT_ADMIN});
		definedGroups.put(PermissionGroup.BOT_ADMIN, new PermissionGroup[] {PermissionGroup.OWNER});
		definedGroups.put(PermissionGroup.OWNER, new PermissionGroup[] {PermissionGroup.ADMIN});
		definedGroups.put(PermissionGroup.ADMIN, new PermissionGroup[] {PermissionGroup.MODERATOR});
		definedGroups.put(PermissionGroup.MODERATOR, new PermissionGroup[] {PermissionGroup.DJ});
		definedGroups.put(PermissionGroup.DJ, new PermissionGroup[] {PermissionGroup.MEMBER});
		definedGroups.put(PermissionGroup.MEMBER, new PermissionGroup[] {PermissionGroup.DEFAULT});
		definedGroups.put(PermissionGroup.DEFAULT, new PermissionGroup[0]);
		
		calculateInheritance();
	}

	private static void calculateInheritance() {
		for (PermissionGroup group : definedGroups.keySet()) {
			HashSet<PermissionGroup> inherits = new HashSet<PermissionGroup>();
			Collections.addAll(inherits, definedGroups.get(group));
			inheritanceCache.put(group, inherits);
		}
		for (PermissionGroup group : inheritanceCache.keySet()) {
			HashSet<PermissionGroup> inherits = inheritanceCache.get(group);
			inherits.addAll(calculateInheritedGroups(group));
		}
		for (PermissionGroup group : inheritanceCache.keySet()) {
			HashSet<PermissionGroup> inherits = inheritanceCache.get(group);
			inherits.add(group);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashSet<PermissionGroup> calculateInheritedGroups(PermissionGroup group) {
		HashSet<PermissionGroup> current = inheritanceCache.get(group);
		HashSet<PermissionGroup> inherits = new HashSet<PermissionGroup>();
		if (current != null) {
			inherits.addAll(current);
			for (PermissionGroup g : (HashSet<PermissionGroup>) inherits.clone())
				inherits.addAll(calculateInheritedGroups(g));
		}
		return inherits;
	}

	public static HashSet<PermissionGroup> getUserPermissions(Member member, User user) {
		if (member == null) {
			HashSet<PermissionGroup> groups = new HashSet<PermissionGroup>();
			groups.addAll(getInheritedGroups(Data.getUserPermissions(user.getIdLong())));
			groups.addAll(getInheritedGroups(PermissionGroup.OWNER));
			return groups;
		} else {
			HashSet<PermissionGroup> groups = new HashSet<PermissionGroup>();
			groups.addAll(getInheritedGroups(Data.getUserPermissions(member.getIdLong())));
			if (member.isOwner())
				groups.addAll(getInheritedGroups(PermissionGroup.OWNER));
			else if (member.hasPermission(Permission.ADMINISTRATOR))
				groups.addAll(getInheritedGroups(PermissionGroup.ADMIN));
			DualHashBidiMap<PermissionGroup, Long> guildMap = Data.getGuildRoles(member.getGuild().getIdLong());
			if (guildMap != null) {
				for (Role role : member.getRoles()) {
					PermissionGroup group = guildMap.getKey(role.getIdLong());
					if (group != null)
						groups.addAll(getInheritedGroups(group));
				}
			}
			return groups;
		}
	}

	public static HashSet<PermissionGroup> getInheritedGroups(PermissionGroup group) {
		return inheritanceCache.get(group);
	}

}
