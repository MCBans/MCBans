package com.mcbans.firestar.mcbans.logEvidence;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class MCBansBlockChange {
	public int blockID;
	public int blockData;
	public Location position;
	public int action;
	
	public MCBansBlockChange() {
		
	}
	
	public MCBansBlockChange(Block block) {
		action = 0;
		position = block.getLocation();
		blockID = block.getTypeId();
		blockData = block.getData();
	}
	
	@Override
	public String toString() {
		return position.getBlockX()+","+position.getBlockY()+","+position.getBlockZ()+","+blockID+","+blockData+","+action;
	}
	
	public String toString(int centerX, int centerY, int centerZ) {
		return (position.getBlockX() - centerX)+","+(position.getBlockY() - centerY)+","+(position.getBlockZ() - centerZ)+","+blockID+","+blockData+","+action;
	}
	
	@Override
	public int hashCode() {
		return position.hashCode();
	}
}
