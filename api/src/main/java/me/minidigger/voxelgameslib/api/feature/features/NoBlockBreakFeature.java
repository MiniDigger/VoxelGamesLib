package me.minidigger.voxelgameslib.api.feature.features;

import com.google.gson.annotations.Expose;

import java.util.Arrays;

import me.minidigger.voxelgameslib.api.event.EventListener;
import me.minidigger.voxelgameslib.api.event.events.block.BlockBreakEvent;
import me.minidigger.voxelgameslib.api.feature.AbstractFeature;
import me.minidigger.voxelgameslib.api.feature.Feature;
import me.minidigger.voxelgameslib.api.feature.FeatureInfo;
import me.minidigger.voxelgameslib.api.item.Material;

@FeatureInfo(name = "NoBlockBreakFeature", author = "MiniDigger", version = "1.0",
        description = "Small feature that blocks block breaking if active")
public class NoBlockBreakFeature extends AbstractFeature {

    @Expose
    private Material[] whitelist = new Material[0];
    @Expose
    private Material[] blacklist = new Material[0];

    /**
     * Sets the list with whitelisted materials. Enabling the whitelist means that ppl are allowed
     * to break only materials which are on the whitelist. to disabled the whitelist, pass an
     * empty array.
     *
     * @param whitelist the new whitelist
     */
    public void setWhitelist(Material[] whitelist) {
        this.whitelist = whitelist;
    }

    /**
     * Sets the list with blacklisted materials. Enabling the blacklist means that ppl are allowed
     * to break every material other than those on the blacklist. to disabled the blacklist, pass an
     * empty array
     *
     * @param blacklist the new blacklist
     */
    public void setBlacklist(Material[] blacklist) {
        this.blacklist = blacklist;
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void tick() {

    }

    @Override
    public void init() {

    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Feature>[] getDependencies() {
        return new Class[0];
    }

    @EventListener
    @SuppressWarnings({"JavaDoc", "Duplicates"})
    public void onBlockBreak(BlockBreakEvent event) {
        if (getPhase().getGame().isPlaying(event.getUser())) {
            if (blacklist.length != 0) {
                if (Arrays.stream(blacklist).anyMatch(m -> m.equals(event.getBlock().getMaterial()))) {
                    event.setCanceled(true);
                }
            } else if (whitelist.length != 0) {
                if (Arrays.stream(whitelist).noneMatch(m -> m.equals(event.getBlock().getMaterial()))) {
                    event.setCanceled(true);
                }
            } else {
                event.setCanceled(true);
            }
        }
    }
}
