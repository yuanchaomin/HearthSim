package com.hearthsim.test.minion;


import java.util.List;

import com.hearthsim.Game;
import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.concrete.BoulderfistOgre;
import com.hearthsim.card.minion.concrete.RaidLeader;
import com.hearthsim.card.minion.concrete.YoungPriestess;
import com.hearthsim.card.spellcard.concrete.TheCoin;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.tree.HearthTreeNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestYoungPriestess {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private HearthTreeNode board;
    private Deck deck;

    @Before
    public void setup() throws HSException {
        board = new HearthTreeNode(new BoardModel());

        Minion minion0_0 = new BoulderfistOgre();
        Minion minion0_1 = new RaidLeader();
        Minion minion1_1 = new RaidLeader();

        board.data_.getCurrentPlayer().placeCardHand(minion0_0);
        board.data_.getCurrentPlayer().placeCardHand(minion0_1);

        board.data_.getWaitingPlayer().placeCardHand(minion1_1);

        Card cards[] = new Card[10];
        for (int index = 0; index < 10; ++index) {
            cards[index] = new TheCoin();
        }

        deck = new Deck(cards);

        Card fb = new YoungPriestess();
        board.data_.getCurrentPlayer().placeCardHand(fb);

        board.data_.getCurrentPlayer().setMana((byte)18);
        board.data_.getWaitingPlayer().setMana((byte)18);

        board.data_.getCurrentPlayer().setMaxMana((byte)8);
        board.data_.getWaitingPlayer().setMaxMana((byte)8);

        HearthTreeNode tmpBoard = new HearthTreeNode(board.data_.flipPlayers());
        tmpBoard.data_.getCurrentPlayer().getHand().get(0).useOn(PlayerSide.CURRENT_PLAYER, tmpBoard.data_.getCurrentPlayer().getHero(), tmpBoard, deck, null);

        board = new HearthTreeNode(tmpBoard.data_.flipPlayers());
        board.data_.getCurrentPlayer().getHand().get(0).useOn(PlayerSide.CURRENT_PLAYER, board.data_.getCurrentPlayer().getHero(), board, deck, null);
        board.data_.getCurrentPlayer().getHand().get(0).useOn(PlayerSide.CURRENT_PLAYER, board.data_.getCurrentPlayer().getHero(), board, deck, null);

        board.data_.resetMana();
        board.data_.resetMinions();

    }



    @Test
    public void test0() throws HSException {
        Card theCard = board.data_.getCurrentPlayer().getHand().get(0);
        HearthTreeNode ret = theCard.useOn(PlayerSide.WAITING_PLAYER, 0, board, deck, null);

        assertNull(ret);
        PlayerModel currentPlayer = board.data_.modelForSide(PlayerSide.CURRENT_PLAYER);
        PlayerModel waitingPlayer = board.data_.modelForSide(PlayerSide.WAITING_PLAYER);

        assertEquals(board.data_.getCurrentPlayer().getHand().size(), 1);
        assertEquals(currentPlayer.getNumMinions(), 2);
        assertEquals(waitingPlayer.getNumMinions(), 1);
        assertEquals(board.data_.getCurrentPlayer().getMana(), 8);
        assertEquals(board.data_.getWaitingPlayer().getMana(), 8);
        assertEquals(board.data_.getCurrentPlayer().getHero().getHealth(), 30);
        assertEquals(board.data_.getWaitingPlayer().getHero().getHealth(), 30);
        assertEquals(currentPlayer.getMinions().get(0).getTotalHealth(), 2);
        assertEquals(currentPlayer.getMinions().get(1).getTotalHealth(), 7);
        assertEquals(waitingPlayer.getMinions().get(0).getTotalHealth(), 2);

        assertEquals(currentPlayer.getMinions().get(0).getTotalAttack(), 2);
        assertEquals(currentPlayer.getMinions().get(1).getTotalAttack(), 7);
        assertEquals(waitingPlayer.getMinions().get(0).getTotalAttack(), 2);
    }

    @Test
    public void test1() throws HSException {
        Card theCard = board.data_.getCurrentPlayer().getHand().get(0);
        HearthTreeNode ret = theCard.useOn(PlayerSide.CURRENT_PLAYER, 2, board, deck, null);

        assertFalse(ret == null);
        PlayerModel currentPlayer = board.data_.modelForSide(PlayerSide.CURRENT_PLAYER);
        PlayerModel waitingPlayer = board.data_.modelForSide(PlayerSide.WAITING_PLAYER);

        assertEquals(board.data_.getCurrentPlayer().getHand().size(), 0);
        assertEquals(currentPlayer.getNumMinions(), 3);
        assertEquals(waitingPlayer.getNumMinions(), 1);
        assertEquals(board.data_.getCurrentPlayer().getMana(), 7);
        assertEquals(board.data_.getWaitingPlayer().getMana(), 8);
        assertEquals(board.data_.getCurrentPlayer().getHero().getHealth(), 30);
        assertEquals(board.data_.getWaitingPlayer().getHero().getHealth(), 30);
        assertEquals(currentPlayer.getMinions().get(0).getTotalHealth(), 2);
        assertEquals(currentPlayer.getMinions().get(1).getTotalHealth(), 7);
        assertEquals(currentPlayer.getMinions().get(2).getTotalHealth(), 1);
        assertEquals(waitingPlayer.getMinions().get(0).getTotalHealth(), 2);

        assertEquals(currentPlayer.getMinions().get(0).getTotalAttack(), 2);
        assertEquals(currentPlayer.getMinions().get(1).getTotalAttack(), 7);
        assertEquals(currentPlayer.getMinions().get(2).getTotalAttack(), 3);
        assertEquals(waitingPlayer.getMinions().get(0).getTotalAttack(), 2);
    }

    @Test
    public void test2() throws HSException {

        //In this test, player1 goes first.  It uses the Stranglethorn Tiger to attack the hero, which removes stealth from
        // the tiger.  Then, player0 plays a turn in which it is able to kill the tiger and hit the player1's hero for 6.

        BruteForceSearchAI ai0 = BruteForceSearchAI.buildStandardAI1();
        List<HearthActionBoardPair> ab = ai0.playTurn(0, board.data_);
        BoardModel resBoard0 = ab.get(ab.size() - 1).board;
        PlayerModel currentPlayer = resBoard0.modelForSide(PlayerSide.CURRENT_PLAYER);
        PlayerModel waitingPlayer = resBoard0.modelForSide(PlayerSide.WAITING_PLAYER);

        assertEquals(resBoard0.getCurrentPlayer().getHand().size(), 0);
        assertEquals(currentPlayer.getNumMinions(), 3);
        assertEquals(waitingPlayer.getNumMinions(), 0);
        assertEquals(resBoard0.getCurrentPlayer().getMana(), 7);
        assertEquals(resBoard0.getWaitingPlayer().getMana(), 8);
        assertEquals(resBoard0.getCurrentPlayer().getHero().getHealth(), 30);
        assertEquals(resBoard0.getWaitingPlayer().getHero().getHealth(), 28);
        assertEquals(currentPlayer.getMinions().get(0).getTotalHealth(), 1);
        assertEquals(currentPlayer.getMinions().get(1).getTotalHealth(), 2);
        assertEquals(currentPlayer.getMinions().get(2).getTotalHealth(), 5);

        assertEquals(currentPlayer.getMinions().get(0).getTotalAttack(), 3);
        assertEquals(currentPlayer.getMinions().get(1).getTotalAttack(), 2);
        assertEquals(currentPlayer.getMinions().get(2).getTotalAttack(), 7);


        int origHealthMinion1 = 2;
        int origHealthMinion2 = 5;

        BoardModel resBoard1 = Game.endTurn(resBoard0);
        currentPlayer = resBoard1.modelForSide(PlayerSide.CURRENT_PLAYER);
        waitingPlayer = resBoard1.modelForSide(PlayerSide.WAITING_PLAYER);

        log.info("Raid Leader Health = " + currentPlayer.getMinions().get(1).getHealth());
        log.info("Boulderfist Ogre Health = " + currentPlayer.getMinions().get(2).getHealth());

        assertTrue((currentPlayer.getMinions().get(1).getHealth() != origHealthMinion1)
                ^ (currentPlayer.getMinions().get(2).getHealth() != origHealthMinion2));
    }
}
