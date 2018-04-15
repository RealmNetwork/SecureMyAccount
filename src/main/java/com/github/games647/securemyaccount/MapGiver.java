package com.github.games647.securemyaccount;

import java.awt.image.BufferedImage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

class MapGiver implements Runnable {

    private final Player player;
    private final BufferedImage resultImage;

    public MapGiver(Player player, BufferedImage resultImage) {
        this.player = player;
        this.resultImage = resultImage;
    }

    @Override
    public void run() {
        MapView createdView = installRenderer(player, resultImage);
        //stack count 0 prevents the item from being dropped
        ItemStack mapItem = new ItemStack(Material.MAP, 1, createdView.getId());
        player.getInventory().addItem(mapItem);
        player.sendMessage(ChatColor.DARK_GREEN + "Here is your secret code. Just scan it with your phone");
        player.sendMessage(ChatColor.DARK_GREEN + "Then drop it in order to remove it");
    }

    private MapView installRenderer(Player player, BufferedImage image) {
        MapView mapView = Bukkit.createMap(player.getWorld());
        mapView.getRenderers().forEach(mapView::removeRenderer);

        mapView.addRenderer(new ImageRenderer(player, image));
        return mapView;
    }
}
