/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.m;

import java.util.UUID;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.GameEvent.EventType;
import mage.game.stack.Spell;
import mage.watchers.Watcher;

/**
 *
 * @author jeffwadsworth
 */
public class ManaMaze extends CardImpl {

    public ManaMaze(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{1}{U}");

        // Players can't cast spells that share a color with the spell most recently cast this turn.
        this.addAbility(new SimpleStaticAbility(Zone.BATTLEFIELD, new ManaMazeEffect()), new LastSpellCastWatcher());

    }

    public ManaMaze(final ManaMaze card) {
        super(card);
    }

    @Override
    public ManaMaze copy() {
        return new ManaMaze(this);
    }
}

class ManaMazeEffect extends ContinuousRuleModifyingEffectImpl {

    ManaMazeEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Detriment);
        staticText = "Players can't cast spells that share a color with the spell most recently cast this turn";
    }

    ManaMazeEffect(final ManaMazeEffect effect) {
        super(effect);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CAST_SPELL_LATE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Card card = game.getCard(event.getSourceId());
        if (card != null) {
            LastSpellCastWatcher watcher = (LastSpellCastWatcher) game.getState().getWatchers().get(LastSpellCastWatcher.class.getName());
            if (watcher != null 
                    && watcher.lastSpellCast != null) {
                return card.getColor(game).contains(watcher.lastSpellCast.getColor(game));
            }
        }
        return false;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }

    @Override
    public ManaMazeEffect copy() {
        return new ManaMazeEffect(this);
    }
}

class LastSpellCastWatcher extends Watcher {

    Spell lastSpellCast = null;

    public LastSpellCastWatcher() {
        super(LastSpellCastWatcher.class.getName(), WatcherScope.GAME);
    }

    public LastSpellCastWatcher(final LastSpellCastWatcher watcher) {
        super(watcher);
        this.lastSpellCast = watcher.lastSpellCast;
    }

    @Override
    public LastSpellCastWatcher copy() {
        return new LastSpellCastWatcher(this);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (EventType.SPELL_CAST.equals(event.getType())) {
            Spell spell = game.getStack().getSpell(event.getTargetId());
            if (spell == null) {
                MageObject mageObject = game.getLastKnownInformation(event.getTargetId(), Zone.STACK);
                if (mageObject instanceof Spell) {
                    spell = (Spell) mageObject;
                }
            }
            if (spell != null) {
                lastSpellCast = spell;
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        lastSpellCast = null;
    }
}
