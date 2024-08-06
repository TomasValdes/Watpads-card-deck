import Player;

import java.util.*;
import java.util.stream.Collectors;

public class BotGame {
  private final ArrayList<CARDS> cardOptions = new ArrayList<>(Arrays.asList(CARDS.ROCK, CARDS.PAPER, CARDS.SCISSORS));
  private final short HAND_SIZE = 3;
  private final short CARDS_TO_REVEAL = 2;
  private final Random random;
  private Player player; // the object of the player playing on the site
  private Player bot; // the object of the bot (completely controlled in this class
  private LinkedList<CARDS> startingDeck; // 
  private LinkedList<CARDS> currentDeck; // the deck after cards have been split up (gets cloned off of starting deck)
  
  private Player revealCardTo = null; // dictate the player to reveal the top two cards to

  private GAME_STATE state; // the current state of the game

  private short cardCounter = 0;

  public enum GAME_STATE {
    CARD_DRAFT,
    PLAYING_CARDS,
    REVEAL_CARDS,
    ROUND_RESULTS
  }

  public enum CARDS {
    ROCK(0), 
    PAPER(1), 
    SCISSORS(2);

    private int key;

    CARDS(int key) {
      this.key = key;
    }

    public int getValue() {
      return key;
    }
  }

  public enum RESULT {
    TIE, P1, P2
  }

  private final RESULT[][] movesToResult = {
    {RESULT.TIE, RESULT.P2, RESULT.P1},
    {RESULT.P1, RESULT.TIE, RESULT.P2},
    {RESULT.P2, RESULT.P1, RESULT.TIE},
  };

  /**
   * @param p1 the player who is playing
   * Creates a new game between a player and a bot
   */ 
  public BotGame(Player p1) {
    player = p1;
    bot = new Player();
    startingDeck = new LinkedList<>(cardOptions);
    state = CARD_DRAFT;
    random = new Random();
  }

  /**
   * @param p1 the player who is playing
   * @param rand the random number generator to be used.
   * SHOULD ONLY BE USED FOR DEBUG.
   * Controllable game that is repeatable for testing purposes.
   * Constructs debugging game between player and a bot.
   */ 
  protected BotGame(Player p1, Random rand) {
    player = p1;
    bot = new Player();
    startingDeck = new LinkedList<>(cardOptions);
    state = CARD_DRAFT;
    random = rand;
  }

  /**
   * @param card the card selected by the user
   * Drafts the card into the starting deck then has the bot do so as well.
   * After the bot drafts its cards, the cards are distributed and the game state changes to playing cards
   */ 
  public void playerDraftCard(CARDS card) {
    startingDeck.add(card);
    cardCounter++;
    if (cardCounter == HAND_SIZE) {
      draftCardBot();
      distributeCards();
      state = GAME_STATE.PLAYING_CARDS;
      cardCounter = 0;
    }

  }

  /**
   * distributes the cards between the players at random
   */ 
  private void distributeCards() {
    Collections.shuffle(startingDeck);

    currentDeck = new LinkedList<>(startingDeck);

    for (int i = 0; i < HAND_SIZE; i++) {
      player.hand.add(currentDeck.pop());
      bot.hand.add(currentDeck.pop());
    }
  }

  /**
   * Determines the winner of the game and is dependent on win condition.
   * @return the winner of the game and null if neither player has won yet. updates game state if appropriate to do so
   */ 
  public Player getWinner() {
   RESULT winner = getWinner(player, bot);
   Player success;
    
    switch (winner) {
      case TIE: 
	if (player.hand.isEmpty()) {
	  // TODO: if debug: assert bot.hand.isEmpty()
	  distributeCards();
	}
        return null;
      break;
      case P1:
	success = player;
	break;
      case P2:
	success = bot;
	break;
    }
    
    // assert success != null
    if (success.move == success.winningCard) {
      state = GAME_STATE.ROUND_RESULTS;
      success.score++;
      return success;
    }
    else {
      revealCardTo = (success == player) ? (player) : (bot);
      state = GAME_STATE.REVEAL_CARDS;
      return null;
    }
  }

  /**
   * @param p1 player 1 relective in {@link #RESULT}
   * @param p2 player 2 reflective in {@link #RESULT}
   * Determines the winner of the standard game without taking into account win conditions
   * @return the winner according to the decision matrix as a result reflecting the order of the parameters
   */ 
  private RESULT getWinner(Player p1, Player p2) {
    return movesToResult[p1.move][p2.move];
  } 

  /**
   * Drafts the cards from the bot randomly
   */ 
  private void draftCardBot() {
    for(int i = 0; i < HAND_SIZE; i++) {
	startingDeck.add(cardOptions.get(random.nextInt(cardOptions.size())));
    }
  }

  /**
   * @param index the index of the card in the players hand to remove
   * plays the card and removes it from the player's hand
   */ 
  public void playCard(int index) {
     player.move = player.hand.remove(index);
  }

  /**
   * @return the options of what cards you can select from
   */
  public List<CARDS> getDraftOptions() {
    return cardOptions;
  }

  public List<CARDS> getRevealedCards(Player player) {
    if (player == revealCardTo) {
      state = GAME_STATE.PLAYING_CARDS;
      return currentDeck.subList(0, CARDS_TO_REVEAL);
    }
    else {
      return null;
    }
    //FIXME: logical error on distributing the cards if a user won without their win condition on the last iteration of card.
  }

  public void play_again() {
    // update some sort of score in db here as well as games played
    
    state = GAME_STATE.CARD_DRAFT;
  }

}

