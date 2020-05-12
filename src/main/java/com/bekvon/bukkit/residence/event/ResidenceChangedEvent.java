package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * The {@link ResidenceChangedEvent} is fired when a player transitions between residences and/or
 * residence subzones. Possible transitions include:
 * <ul>
 * <li>Moving from no residence into a residence/subzone;</li>
 * <li>Moving from a residence/subzone to no residence; or</li>
 * <li>Moving between residences/subzones</li>
 * </ul>
 * <p>
 * {@link ResidenceChangedEvent} is a replacement for {@link ResidenceEnterEvent} and
 * {@link ResidenceLeaveEvent}, which have been marked as deprecated and will be removed in future
 * releases. Using this event benefits developers as it encapsulates enter/leave conditions in a
 * single event and doesn't require additional correlation to detect and residence-to-residence
 * transition.
 * <p/>
 * <p>
 * This event is fired whenever conditions are met when a player moves or teleports to a new
 * location. The event is also triggered when players appear in a residence upon logging in.
 *
 * @author frelling
 */
public class ResidenceChangedEvent extends ResidencePlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private ClaimedResidence from = null;
    private ClaimedResidence to = null;

    /**
     * Constructs a {@link ResidenceChangedEvent} to identify a residence transition for the
     * given player
     *
     * @param from   the residence that the player left or {@code null} if coming from an
     *               unprotected area.
     * @param to     the residence that the player entered or {@code null} if entering an
     *               unprotected area.
     * @param player player involved in the transition
     */
    public ResidenceChangedEvent(ClaimedResidence from, ClaimedResidence to, Player player) {
        super("RESIDENCE_CHANGE", null, player);
        this.from = from;
        this.to = to;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Returns the residence from which player came.
     *
     * @return the residence from which player came or {@code null} if player came from an
     * unprotected area
     */
    public ClaimedResidence getFrom() {
        return from;
    }

    /**
     * Returns the residence that player has entered.
     *
     * @return the residence that player has entered or {@code null} if player enters an
     * unprotected area
     */
    public ClaimedResidence getTo() {
        return to;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
