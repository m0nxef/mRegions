package org.monxef.mregions.flags;

import org.bukkit.entity.Player;
import org.monxef.mregions.models.Flag;

public class DefaultFlags {

    public static class BlockBreakFlag extends Flag {
        public BlockBreakFlag() {
            super(
                    "block_break",
                    "Block Breaking",
                    "Controls who can break blocks in the region",
                    FlagState.WHITELIST,
                    null
            );
        }
    }

    public static class BlockPlaceFlag extends Flag {
        public BlockPlaceFlag() {
            super(
                    "block_place",
                    "Block Placing",
                    "Controls who can place blocks in the region",
                    FlagState.WHITELIST,
                    null
            );
        }
    }

    public static class InteractFlag extends Flag {
        public InteractFlag() {
            super(
                    "interact",
                    "Interaction",
                    "Controls who can interact with blocks/items in the region",
                    FlagState.WHITELIST,
                    null
            );
        }
    }

    public static class EntityDamageFlag extends Flag {
        public EntityDamageFlag() {
            super(
                    "entity_damage",
                    "Entity Damage",
                    "Controls who can damage entities in the region",
                    FlagState.WHITELIST,
                    null
            );
        }
    }
}