package org.monxef.mregions.models;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.function.BiPredicate;

public abstract class Flag {
    @Getter
    private final String id;
    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final FlagState defaultState;

    // BiPredicate for custom validation logic
    private final BiPredicate<Player, FlagState> validator;

    protected Flag(String id, String name, String description, FlagState defaultState,
                   BiPredicate<Player, FlagState> validator) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultState = defaultState;
        this.validator = validator;
    }

    public boolean validate(Player player, FlagState state) {
        return validator != null ? validator.test(player, state) : true;
    }

    public enum FlagState {
        EVERYONE("Everyone", "Allows everyone to perform this action"),
        WHITELIST("Whitelist", "Only whitelisted players can perform this action"),
        NONE("None", "Nobody can perform this action");

        @Getter private final String display;
        @Getter private final String description;

        FlagState(String display, String description) {
            this.display = display;
            this.description = description;
        }
    }
}