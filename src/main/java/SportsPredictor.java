import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.text.*;
import java.net.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

public class SportsPredictor{
  //TODO: Change HISTORICAL_SCORES_FILE_LOCATION to desired location
  public static final String HISTORICAL_SCORES_FILE_LOCATION = "/Users/marleybyers/Personal Projects/java-sports-predictor/additional-info/";
  // How many of the last league games to consider
  public static final int LAST_GAMES = 5;
  public static final int TOTAL_PREM_GAMES = 38;
  public static final int TOTAL_MLS_GAMES = 34;
  public static final int NUM_MLS_TEAMS = 28;
  
  public static void main(String[] args) throws IOException, FileNotFoundException, ParseException, InterruptedException {
    Scanner console = new Scanner(System.in);
    System.out.println("Print the information other than predicted scores?");
    String yN = console.next();
    String league = getLeague(console);
    boolean progress = true;
    boolean printInfo;
    boolean sd = false;
    boolean printMatches = false;
    boolean printAverages = false;
    boolean printForm = false;
    File formFile = new File("FormWeight.txt");
    File matchFile = new File("MatchesPredicted.txt");
    BufferedWriter formOutput = new BufferedWriter(new FileWriter(formFile, true));
    BufferedWriter matchOutput = new BufferedWriter(new FileWriter(matchFile, true));
    Scanner formInput = new Scanner(new File("FormWeight.txt"));
    Scanner matchInput = new Scanner(new File("MatchesPredicted.txt"));
    
    
    // use Standard Deviation
    if (yN.equalsIgnoreCase("sd")) {
      printInfo = false;
      sd = true;
    } else if ((yN.toLowerCase()).contains("av")) {
      printInfo = false;
      printAverages = true;
    } else if ((yN.toLowerCase()).contains("mat")) {
      printInfo = false;
      printMatches = true;
    } else if ((yN.toLowerCase()).contains("fo")) {
      printInfo = false;
      printForm = true;
    } else if (yN.equalsIgnoreCase("n")){
      printInfo = false;
    } else {
      printInfo = true;
    }
    int[] scores1;
    int[] oScores1;
    int season;
    int matchweek = -1;
    String urlTeam1;
    String url1;
    String urlTeam2;
    String url2;
    String team1;
    int position1;
    int gamesPlayed1;
    String mlsConference1 = "";
    String mlsStandingsUrl = "https://www.espn.com/soccer/table/_/league/usa.1";
    Document mlsStandings = Jsoup.connect(mlsStandingsUrl).get();
    
    if (league.equals("MLS")) {
      scores1 = new int[TOTAL_MLS_GAMES];
      oScores1 = new int[TOTAL_MLS_GAMES];
      season = Calendar.getInstance().get(Calendar.YEAR);
      urlTeam1 = getTeamUrlName(console, 1, league);
      url1 = "https://www.espn.com/soccer/team/results/_/id/" + urlTeam1.substring(0, urlTeam1.indexOf("/")) + "/league/USA.1"; // URL with Results and official team name
      System.out.println(url1);
      Document resultsPageToScrape = Jsoup.connect(url1).get();
      team1 = resultsPageToScrape.selectFirst("#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div.flex.justify-between.mt3.mb5.items-center > h1").text().replace(" Results", "");
      position1 = getMLSPosition(mlsStandings, team1, printInfo);
      System.out.println("Position1: " + position1);
      mlsConference1 = getMLSConference(mlsStandings, team1, position1);
      gamesPlayed1 = getMLSGamesPlayed(mlsStandings, mlsConference1, position1, printInfo);
      System.out.println();
      urlTeam2 = getTeamUrlName(console, 2, league);
      url2 = "https://www.espn.com/soccer/team/results/_/id/" + urlTeam1.substring(0, urlTeam1.indexOf("/")) + "/league/USA.1";
    } else { //if (league.equals("Premier League")) {
      scores1 = new int[TOTAL_PREM_GAMES];
      oScores1 = new int[TOTAL_PREM_GAMES];
      season = getSeason();
      matchweek = intFinder("https://www.worldfootball.net/competition/eng-premier-league/", "#navi > div.sitenavi > div.navibox2 > div > ul:nth-child(2) > li:nth-child(1) > a", "/", league);
      urlTeam1 = getTeamUrlName(console, 1, league);
      url1 = "https://www.worldfootball.net/teams/" + urlTeam1 + "/" + season + "/3/";
      System.out.println();
      urlTeam2 = getTeamUrlName(console, 2, league);
      url2 = "https://www.worldfootball.net/teams/" + urlTeam2 + "/" + season + "/3/";
      team1 = getPremTeamName(url1);
      position1 = getPremPosition(team1);
      gamesPlayed1 = getPremGamesPlayed(position1);
    }
    
    Arrays.fill(scores1, -1);
    Arrays.fill(oScores1, -1);
    int[] wld1 = new int [3];
    String[] oppositions1 = new String[LAST_GAMES];
    String[] locations1 = new String[LAST_GAMES];
    int[] expected1 = new int[9];
    
    
    int form1 = 0;
    double averageScored1 = 0;
    double notAverageScored1 = 0;
    double averageConceded1 = 0;
    double notAverageConceded1 = 0;
    double homeScored1 = 0;
    double homeConceded1 = 0;
    if (gamesPlayed1 > 0) {
      if (league.equals("MLS")) {
        readMLSWebsite(url1, gamesPlayed1, scores1, oScores1, locations1, oppositions1,
            printInfo, printMatches, formInput, formOutput, matchInput, matchFile);
        getRecord(console, team1, scores1, oScores1, wld1, gamesPlayed1, matchweek, printInfo);
      } else { // if (league.equals("Premier League")) {
        readPremWebsite(url1, position1, gamesPlayed1, matchweek, scores1, oScores1,
            locations1, oppositions1, printInfo, printMatches);
        getRecord(console, team1, scores1, oScores1, wld1, gamesPlayed1, matchweek, printInfo);
      }
      
      expectedRecord(team1, scores1, oScores1, expected1, gamesPlayed1, oppositions1, printInfo,
          position1, league, mlsConference1, mlsStandings);
      form1 = rateForm(expected1, printInfo, printForm);
      
      averageScored1 = getAverages(team1, scores1, gamesPlayed1, "scored", printInfo, printAverages);
      homeScored1 = getAverages(team1, scores1, locations1, gamesPlayed1, "scored", printInfo, printAverages, true);
      //double scoredSD1 = calculateSD(scores1, gamesPlayed1, team1, "scored", printInfo, sd);
      notAverageScored1 = awayFromAverage(scores1, gamesPlayed1, team1, "scored", printInfo, printAverages);
      averageConceded1 = getAverages(team1, oScores1, gamesPlayed1, "conceded", printInfo, printAverages);
      homeConceded1 = getAverages(team1, oScores1, locations1, gamesPlayed1, "conceded", printInfo, printAverages, true);
      //double concededSD1 = calculateSD(oScores1, gamesPlayed1, team1, "conceded", printInfo, sd);
      notAverageConceded1 = awayFromAverage(oScores1, gamesPlayed1, team1, "conceded", printInfo, printAverages);
      
      System.out.println();
    }
    
    
    //Second team stuff
    int[] scores2 = new int [38];
    Arrays.fill(scores2, -1);
    int[] oScores2 = new int [38];
    Arrays.fill(oScores2, -1);
    int[] wld2 = new int [3];
    String[] oppositions2 = new String[LAST_GAMES];
    String[] locations2 = new String[LAST_GAMES];
    int[] expected2 = new int[9];
    String team2 = "";
    int position2 = -1;
    String mlsConference2 = "";
    int gamesPlayed2;
    
    int form2 = 0;
    double averageScored2 = 0;
    double notAverageScored2 = 0;
    double averageConceded2 = 0;
    double notAverageConceded2 = 0;
    double awayScored2 = 0;
    double awayConceded2 = 0;
    if (league.equals("MLS")) {
      team2 = getPremTeamName(url2);
      position2 = getMLSPosition(mlsStandings, team2, printInfo);
      mlsConference2 = getMLSConference(mlsStandings, team2, position2);
      gamesPlayed2 = getMLSGamesPlayed(mlsStandings, mlsConference2, position2, printInfo);
      if (gamesPlayed2 > 0) {
        Document pageToScrape = Jsoup.connect("https://www.mlssoccer.com/clubs/" + urlTeam2 + "/").get();
        team2 = pageToScrape.selectFirst("body > div.d3-l-wrap.mls-l-template-C4 > section:nth-child(4) > section > div > div > div > div.mls-o-masthead__title-wrapper > h1").text();
        
        readMLSWebsite(url2, gamesPlayed2, scores2, oScores2,
            locations2, oppositions2, printInfo, printMatches, formInput,
            formOutput, matchInput, matchFile);
      }
    } else { // if (league.equals("Premier League")) {
      team2 = getPremTeamName(url2);
      position2 = getPremPosition(team2);
      gamesPlayed2 = getPremGamesPlayed(position2);
      if (gamesPlayed2 > 0) {
        System.out.println(team2 + " has played " + gamesPlayed2 + " games");
        readPremWebsite(url2, position2, gamesPlayed2, matchweek, scores2, oScores2,
            locations2, oppositions2, printInfo, printMatches);
      }
    }
    
    getRecord(console, team2, scores2, oScores2, wld2, gamesPlayed2, matchweek, printInfo);
    expectedRecord(team2, scores2, oScores2, expected2, gamesPlayed2, oppositions2, printInfo,
        position2, league, mlsConference2, mlsStandings);
    form2 = rateForm(expected2, printInfo, printForm);
    
    averageScored2 = getAverages(team2, scores2, gamesPlayed2, "scored", printInfo, printAverages);
    awayScored2 = getAverages(team2, scores2, locations2, gamesPlayed2, "scored", printInfo, printAverages, false);
    //double scoredSD2 = calculateSD(scores2, gamesPlayed2, team2, "scored", printInfo, sd);
    notAverageScored2 = awayFromAverage(scores2, gamesPlayed2, team2, "scored", printInfo, printAverages);
    averageConceded2 = getAverages(team2, oScores2, gamesPlayed2, "conceded", printInfo, printAverages);
    awayConceded2 = getAverages(team2, oScores2, locations2, gamesPlayed2, "conceded", printInfo, printAverages, false);
    //double concededSD2 = calculateSD(oScores2, gamesPlayed2, team2, "conceded", printInfo, sd);
    notAverageConceded2 = awayFromAverage(oScores2, gamesPlayed2, team2, "conceded", printInfo, printAverages);
    
    //System.out.println("scored: " + averageScored1);
    //System.out.println("hScored: " + homeScored1);
    //System.out.println("conceded2: " + averageConceded2);
    //System.out.println("aConceded: " + awayConceded2);
    
    predictScore(gamesPlayed1, gamesPlayed2, form1, form2, scores1, scores2, averageScored1, averageScored2,
        homeScored1, awayScored2, averageConceded1, averageConceded2, homeConceded1, awayConceded2,
        team1, team2, expected1, expected2, notAverageScored1, notAverageConceded1,
        notAverageScored2, notAverageConceded2, formInput, matchOutput,position1, position2, console);
    
    //checkPremPredictions(formInput, formOutput, matchInput, matchFile, matchweek, season);
  }
  
  /**
   * Gets the url-form of each team's name
   * @param console Scanner to print to
   * @param runs how many times this has been run
   * @param league league the team plays in
   * @return the url-form of a given team's name
   */
  public static String getTeamUrlName(Scanner console, int runs,  String league) {
    if (runs == 1) {
      System.out.println("Home team? ");
      //console.nextLine();
    } else if (runs == 2) {
      System.out.println("Away team? ");
    }
    
    String team = console.nextLine().toLowerCase();
    String urlTeam = "";
    boolean validTeam = false;
    while (!validTeam) {
      validTeam = true;
      if (league.equals("MLS")) {
        if (team.equals("Atlanta") || team.equals("atl")) {
          urlTeam = "18418/atlanta-united-fc";
        } else if (team.contains("aus") || team.equals("atx")) {
          urlTeam = "20906/austin-fc";
        } else if (team.contains("cha") || team.contains("cfc") || team.equals("clt")) {
          urlTeam = "21300/charlotte-fc";
        } else if (team.contains("chi") || (team.equals("fire"))) {
          urlTeam = "182/chicago-fire-fc";
        } else if (team.contains("cin") || (team.equals("fcc"))) {
          urlTeam = "18267/fc-cincinnati";
        } else if (team.contains("colo") || team.contains("rap") || team.equals("col")) {
          urlTeam = "184/colorado-rapids";
        } else if (team.contains("colum") || team.contains("crew") || team.equals("clb")) {
          urlTeam = "183/columbus-crew";
        } else if (team.contains("dc ") || team.equals("dc") || team.equals("dcu")) {
          urlTeam = "193/dc-united";
        } else if (team.contains("dal")) {
          urlTeam = "185/fc-dallas";
        } else if (team.contains("hou") || team.contains("dyn")) {
          urlTeam = "6077/houston-dynamo-fc";
        } else if (team.equals("skc") || team.contains("sporting") || team.contains("kan")) {
          urlTeam = "186/sporting-kansas-city";
        } else if (team.contains("la g") || team.contains("gal") || (team.equals("lag"))) {
          urlTeam = "187/la-galaxy";
        } else if (team.contains("la f") || team.equals("lafc")) {
          urlTeam = "18966/lafc";
        } else if (team.contains("inter") || team.contains("mia") || team.contains(" cf")) {
          urlTeam = "20232/inter-miami-cf";
        } else if (team.contains("min")) {
          urlTeam = "17362/minnesota-united-fc";
        } else if (team.contains("cf ") || team.contains("mont") || team.equals("mtl")) {
          urlTeam = "9720/cf-montreal";
        } else if (team.contains("nas") || team.contains("nsh")) {
          urlTeam = "18986/nashville-sc";
        } else if (team.contains("new e") || team.contains("engl") || team.contains("rev") || team.equals("ner") || team.equals("ne")) {
          urlTeam = "189/new-england-revolution";
        } else if (team.contains("nyc") || team.contains("york c")) {
          urlTeam = "17606/new-york-city-fc";
        } else if (team.contains("nyr") || team.contains("red") || team.equals("rbny") || team.contains("york r")) {
          urlTeam = "190/new-york-red-bulls";
        } else if (team.contains("orl") || team.equals("oc") || (team.contains("ocsc"))) {
          urlTeam = "12011/orlando-city-sc";
        } else if (team.contains("phil") || team.contains("uni") || team.equals("phu")) {
          urlTeam = "10739/philadelphia-union";
        } else if (team.contains("ptf") || team.contains("por") || team.contains("tim")) {
          urlTeam = "9723/portland-timbers";
        } else if (team.contains("real s") || team.contains("salt") || team.equals("rsl")) {
          urlTeam = "4771/real-salt-lake";
        } else if (team.contains("san") || team.contains("jos") || team.contains("eart") || team.contains("sj")) {
          urlTeam = "191/san-jose-earthquakes";
        } else if (team.contains("ssf") || team.contains("sfc") || team.equals("ebfg") || team.contains("sea") || team.contains("sou")) {
          urlTeam = "9726/seattle-sounders-fc";
        } else if (team.contains("tor") || team.equals("tfc")) {
          urlTeam = "7318/toronto-fc";
        } else {
          urlTeam = "9727/vancouver-whitecaps";
        }
        
      } else { // if (league.equals("Premier League") {
        if (team.equalsIgnoreCase("arsenal") || team.equals("ars")) {
          urlTeam = "arsenal-fc";
        } else if (team.contains("bou") || (team.contains("afc"))) {
          urlTeam = "afc-bournemouth";
        } else if (team.contains("villa") || (team.contains("aston") || (team.equals("avl")))) {
          urlTeam = "aston-villa";
        } else if (team.contains("brent") || (team.equals("bre"))) {
          urlTeam = "brentford-fc";
        } else if (team.contains("brighton") || (team.equals("bha"))) {
          urlTeam = "brighton-hove-albion";
        } else if (team.contains("bur")) {
          urlTeam = "burnley-fc";
        } else if (team.equals("che") || team.contains("chel")) {
          urlTeam = "chelsea-fc";
        } else if (team.contains("cry") || team.contains("pala") || team.equals("cp")) {
          urlTeam = "crystal-palace";
        } else if (team.contains("eve")) {
          urlTeam = "everton-fc";
        } else if (team.contains("ful")) {
          urlTeam = "fulham-fc";
        } else if (team.contains("leeds") || (team.equals("lee"))) {
          System.out.println("They got relegated, go again");
          validTeam = false;
          team = (console.nextLine()).toLowerCase();
//          urlTeam = "leeds-united";
        } else if (team.contains("leicester") || team.contains("lester") || (team.equals("lei"))) {
          System.out.println("They got relegated, go again");
          validTeam = false;
          team = (console.nextLine()).toLowerCase();
//          urlTeam = "leicester-city";
        } else if (team.contains("liverpool") || team.equals("liv")) {
          urlTeam = "liverpool-fc";
        } else if (team.contains("luton") || team.equals("ltn") || team.equals("lut")) {
          urlTeam = "luton-town";
        } else if (team.contains("man") || (team.equals("mun") || team.equals("mnu") || team.equals("mnc") || team.equals("mci"))) {
          if (team.contains("u")) {
            urlTeam = "manchester-united";
          } else {
            urlTeam = "manchester-city";
          }
        } else if (team.contains("new")) {
          urlTeam = "newcastle-united";
        } else if (team.contains("nor")) {
          System.out.println("They got relegated, go again");
          validTeam = false;
          team = (console.nextLine()).toLowerCase();
          //urlTeam = "norwich-city";
        } else if (team.contains("not") || team.contains("for")) {
          urlTeam = "nottingham-forest";
        } else if (team.contains("sheffield u") || team.equals("shu")) {
        urlTeam = "sheffield-united";
        } else if (team.contains("sou")) {
          System.out.println("They got relegated, go again");
          validTeam = false;
          team = (console.nextLine()).toLowerCase();
//          urlTeam = "southampton-fc";
        } else if (team.contains("tot")) {
          urlTeam = "tottenham-hotspur";
        } else if (team.contains("wat")) {
          System.out.println("They got relegated, go again");
          validTeam = false;
          team = (console.nextLine()).toLowerCase();
          // urlTeam = "watford-fc";
        } else if (team.contains("wes") || (team.equals("whu"))) {
          urlTeam = "west-ham-united";
        } else {
          urlTeam = "wolverhampton-wanderers";
        }
      }
    }
    return urlTeam;
  }
  
  /**
   * Gets official team name from website
   * @param url website to get name from
   * @return official team name from website
   * @throws IOException if problem getting website
   */
  public static String getPremTeamName(String url) throws IOException {
    Document pageToScrape = Jsoup.connect(url).get();
    String title = pageToScrape.title();
    StringBuilder team = new StringBuilder();
    int arrows = (title.indexOf('»') - 1);
    for (int i = 0; i < arrows; i++) {
      team.append(title.charAt(i));
    }
    return team.toString();
  }
  
  /**
   * Fills out arrays with info from given website
   * @param url website to get data from
   * @param position position of given team in table
   * @param gamesPlayed games team has played
   * @param matchweek matchweek of this game
   * @param scores scores of home team's games
   * @param oScores scores of away team's games
   * @param locations locations of games played
   * @param oppositions team names of oppositions
   * @param printInfo whether to print additional info
   * @param printMatches whether to print info of each match played
   * @throws IOException if error getting website
   */
  public static void readPremWebsite(String url, int position, int gamesPlayed, int matchweek,
                                     int[] scores, int[] oScores, String[] locations,
                                     String[] oppositions, boolean printInfo, boolean printMatches) throws IOException {
    int start;
    int line = 0;
    String prem = "";
    
    Document pageToScrape = Jsoup.connect(url).get();
    String title = pageToScrape.title();
    String team = "";
    String opposition = "";
    int arrows = title.indexOf('»');
    for (int i = 0; i < arrows; i++) {
      team += "" + title.charAt(i);
    }
    
    while (!prem.contains("Premier")) {
      String premFinder = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + line + ") > td > a";
      Element prem1 = pageToScrape.selectFirst(premFinder);
      if (prem1 != null) {
        prem = prem1.text();
      }
      
      // System.out.printf("Prem line is: %s. \n", prem);
      
      line++;
    }
    
    int lastGames = LAST_GAMES;
    
    if (lastGames >= gamesPlayed) {
      lastGames = gamesPlayed - 1;
    }
    
    int gamesCounted = 0;
    start = line + 1;
    String score = "";
    for (int i = 0; i <= matchweek; i++) {
      // Gets the score of the game
      //System.out.println(i + " / " + matchweek);
      String scoreFinder1 = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (i + start) + ") > td:nth-child(7) > a";
      Element scoreElement = pageToScrape.selectFirst(scoreFinder1);
      score = scoreElement.text();
      
      // Gets the team they're playing
      String oppositionFinder = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (i + start) + ") > td:nth-child(6) > a";
      Element oppositionElement = pageToScrape.selectFirst(oppositionFinder);
      opposition = oppositionElement.text();
      
      if (score.contains(" ")) {
        gamesCounted++;
        if (gamesCounted + lastGames - 1 - gamesPlayed >= 0) {
          String locationFinder = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (i + start) + ") > td:nth-child(4)";
          Element locationElement = pageToScrape.selectFirst(locationFinder);
          locations[gamesCounted + lastGames - 1 - gamesPlayed] = locationElement.text();
        }
        
        String tempScore = "";
        String oTempScore = "";
        String result = "";
        int intScore = 0;
        int oIntScore = 0;
        for (int j = 0; j < score.indexOf(" "); j++) {
          result += score.charAt(j);
        }
        
        for (int j = 0; j < result.length(); j++) {
          if (j < result.indexOf(":")) {
            tempScore += result.charAt(j);
            intScore = Integer.parseInt(tempScore);
            scores[i] = intScore;
          } else if (j > result.indexOf(":")) {
            oTempScore += result.charAt(j);
            oIntScore = Integer.parseInt(oTempScore);
            oScores[i] = oIntScore;
          }
        }
        // If the match is in the last _ games, it
        // Checks the file to see if the match it's looking at has
        // been predicted before and if it has, it averages the formWeight
        // with it and removes the prediction line
        //System.out.println("if 0 <= " + (gamesPlayed - gamesCounted) + " && " + (gamesPlayed - gamesCounted) + " < " + lastGames);
        if (gamesPlayed - gamesCounted >= 0 && gamesPlayed - gamesCounted < lastGames) {
          oppositions[(gamesPlayed - gamesCounted)] = opposition;
          //System.out.println("oppositions[" + (gamesPlayed - gamesCounted) + "] = " + opposition);
        }
        
        // Prints all the played matches if you want
        if (printInfo || printMatches) {
          if (intScore > oIntScore) {
            System.out.println(team + "beat " + opposition + ", " +
                intScore + " to " + oIntScore);
          } else if (intScore < oIntScore) {
            System.out.println(team + "lost to " + opposition + ", " +
                intScore + " to " + oIntScore);
          } else {
            System.out.println(team + "drew against " + opposition +
                ", " + intScore + " to " + oIntScore);
          }
        }
      }
    }
    
    //for (int i = matchweek; i >= gamesPlayed - lastGames; i--) {
    //System.out.println("i = " + i);
    //String scoreFinder1 = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (i + start) + ") > td:nth-child(7) > a";
    //Element score1 = pageToScrape.selectFirst(scoreFinder1);
    //String score = score1.text();
    //if (score.contains(" ")) {
    //System.out.println(score);
    //String oppositionFinderB = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (i + start) + ") > td:nth-child(6) > a";
    //Element opposition1B = pageToScrape.selectFirst(oppositionFinderB);
    //String oppositionB = opposition1B.text();
    //oppositions[gamesCounted] = oppositionB;
    //gamesCounted++;
    //if (gamesCounted == lastGames) {
    //i = 0;
    //}
    //}
    //}
    if (printInfo) {
      System.out.println(Arrays.toString(oppositions));
      System.out.println(team + "goals for: " + Arrays.toString(scores));
      System.out.println("Goals against: " + Arrays.toString(oScores));
    }
  }
  
  /**
   * Fills out arrays with info from given website
   * @param url website to get data from
   * @param gamesPlayed games team has played
   * @param scores scores of home team's games
   * @param oScores scores of away team's games
   * @param locations locations of games played
   * @param oppositions team names of oppositions
   * @param printInfo whether to print additional info
   * @param printMatches whether to print info of each match played
   * @throws IOException if error getting website
   */
  public static void readMLSWebsite(String url, int gamesPlayed, int[] scores, int[] oScores, String[] locations,
                                    String[] oppositions, boolean printInfo, boolean printMatches,
                                    Scanner formInput, BufferedWriter formOutput,
                                    Scanner matchInput, File matchFile) throws IOException {
    
    Document pageToScrape = Jsoup.connect(url).get();
    String team = "";
    String opposition = "";
    int position = 0;
    
    int gamesCounted = 0;
    
    int lastGames = LAST_GAMES;
    
    if (lastGames >= gamesPlayed) {
      lastGames = gamesPlayed - 1;
    }
    
    int i = 0;
    int monthVal = 1;
    while (gamesCounted < gamesPlayed) {
      String hTeam = "";
      String aTeam = "";
      String score = "";
      String hTeamFinder = "";
      String aTeamFinder = "";
      String scoreFinder = "";
      // previouslyNull keeps track of whether the last line was a match.
      // If it was a match, then it looks at the next line and if not then
      // it will move on to the next month if current and previously are null.
      boolean previouslyNull = false;
      if (i == 0) {
        hTeamFinder = "#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div:nth-child(3) > div:nth-child(" + monthVal + ") > div.flex > div > div.Table__Scroller > table > tbody > tr > td:nth-child(2) > div > a";
        aTeamFinder = "#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div:nth-child(3) > div:nth-child(" + monthVal + ") > div.flex > div > div.Table__Scroller > table > tbody > tr > td:nth-child(4) > div > a";
        scoreFinder = "#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div:nth-child(3) > div:nth-child(" + monthVal + ") > div.flex > div > div.Table__Scroller > table > tbody > tr > td:nth-child(3) > span > a:nth-child(2)";
        
      } else {
        hTeamFinder = "#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div:nth-child(3) > div:nth-child(" + monthVal + ") > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(" + i + ") > td:nth-child(2) > div > a";
        aTeamFinder = "#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div:nth-child(3) > div:nth-child(" + monthVal + ") > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(" + i + ") > td:nth-child(4) > div > a";
        scoreFinder = "#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div:nth-child(3) > div:nth-child(" + monthVal + ") > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(" + i + ") > td:nth-child(3) > span > a:nth-child(2)";
      }
      
      //try {
      if (pageToScrape.selectFirst(hTeamFinder) == null) {
        if (previouslyNull) {
          //kill the while loop?
          throw new NullPointerException("MLS website had a null element where it shouldn't have");
        } else {
          previouslyNull = true;
          // Moves to the next month and resets the line inside the month
          monthVal++;
          i = 0;
        }
      }
      //} catch (Exception Selector$SelectorParseException){
      
      hTeamFinder = "#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div:nth-child(3) > div:nth-child(" + monthVal + ") > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(" + i + ") > td:nth-child(2) > div > a";
      aTeamFinder = "#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div:nth-child(3) > div:nth-child(" + monthVal + ") > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(" + i + ") > td:nth-child(4) > div > a";
      scoreFinder = "#fittPageContainer > div.StickyContainer > div.page-container.cf > div > div.layout__column.layout__column--1 > section > div > section > div:nth-child(3) > div:nth-child(" + monthVal + ") > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(" + i + ") > td:nth-child(3) > span > a:nth-child(2)";
      
      if (pageToScrape.selectFirst(hTeamFinder) == null) {
        if (previouslyNull) {
          //kill the while loop?
          throw new NullPointerException("MLS website had a null element where it shouldn't have");
        } else {
          previouslyNull = true;
          // Moves to the next month and resets the line inside the month
          monthVal++;
          i = 0;
        }
      }
      
      previouslyNull = false;
      hTeam = pageToScrape.selectFirst(hTeamFinder).text();
      aTeam = pageToScrape.selectFirst(aTeamFinder).text();
      score = pageToScrape.selectFirst(scoreFinder).text();
      i++;
      
      // Gets the team they're playing
      int hScore = Integer.parseInt(score.substring(0, score.indexOf("-") - 1));
      int aScore = Integer.parseInt(score.substring(score.indexOf("-") + 2), score.length());
      String location = "";
      
      int intScore = 0;
      int oIntScore = 0;
      
      if (hTeam.equals(team)) {
        opposition = aTeam;
        location = "H";
        scores[i] = hScore;
        oScores[i] = aScore;
      } else {
        opposition = hTeam;
        location = "A";
        scores[i] = aScore;
        oScores[i] = hScore;
      }
      
      locations[locations.length - 1 - gamesCounted] = location;
      
      
      // If the match is in the last _ games, it
      // Checks the file to see if the match it's looking at has
      // been predicted before and if it has, it averages the formWeight
      // with it and removes the prediction line
      //System.out.println("if 0 <= " + (gamesPlayed - gamesCounted) + " && " + (gamesPlayed - gamesCounted) + " < " + lastGames);
      if (0 <= gamesPlayed - gamesCounted && gamesPlayed - gamesCounted < lastGames) {
        oppositions[gamesPlayed - gamesCounted] = opposition;
        //System.out.println("oppositions[" + (gamesPlayed - gamesCounted) + "] = " + opposition);
      }
      
      // Prints all the played matches if you want
      if (printInfo || printMatches) {
        if (intScore > oIntScore) {
          System.out.println(team + "beat " + opposition + ", " +
              intScore + " to " + oIntScore);
        } else if (intScore < oIntScore) {
          System.out.println(team + "lost to " + opposition + ", " +
              intScore + " to " + oIntScore);
        } else {
          System.out.println(team + "drew against " + opposition +
              ", " + intScore + " to " + oIntScore);
        }
      }
      gamesCounted++;
      //}

//			if (pageToScrape.selectFirst(hTeamFinder) == null) {
//				if (previouslyNull) {
//					i++;
//					//kill the while loop?
//					//throw new NullPointerException("MLS website had a null element where it shouldn't have");
//				} else {
//					//System.out.println()
//					previouslyNull = true;
//					// Moves to the next month and resets the line inside the month
//					monthVal++;
//					i = 0;
//				}
//			} else {				
//				previouslyNull = false;
//				hTeam = pageToScrape.selectFirst(hTeamFinder).text();
//				aTeam = pageToScrape.selectFirst(aTeamFinder).text();
//				score = pageToScrape.selectFirst(scoreFinder).text();
//				i++;
//			
//				// Gets the team they're playing
//				int hScore = Integer.parseInt(score.substring(0, score.indexOf("-") - 1));
//				int aScore = Integer.parseInt(score.substring(score.indexOf("-") + 2), score.length());
//				String location = "";
//				
//				int intScore = 0;
//				int oIntScore = 0;
//				
//				if (hTeam.equals(team)) {
//					opposition = aTeam;
//					location = "H";
//					intScore = hScore;
//					oIntScore = aScore;
//				} else {
//					opposition = hTeam;
//					location = "A";
//					intScore = aScore;
//					oIntScore = hScore;
//				}
//				
//	
//				
//				// If the match is in the last _ games, it
//				// Checks the file to see if the match it's looking at has
//				// been predicted before and if it has, it averages the formWeight
//				// with it and removes the prediction line
//				//System.out.println("if 0 <= " + (gamesPlayed - gamesCounted) + " && " + (gamesPlayed - gamesCounted) + " < " + lastGames);
//				if (0 <= gamesPlayed - gamesCounted && gamesPlayed - gamesCounted < lastGames) {
//					oppositions[gamesPlayed - gamesCounted] = opposition;
//					locations[gamesPlayed - gamesCounted] = location;
//					//System.out.println("oppositions[" + (gamesPlayed - gamesCounted) + "] = " + opposition);
//				}
//				
//				// Prints all the played matches if you want
//				if (printInfo || printMatches) {
//					if (intScore > oIntScore) {
//						System.out.println(team + "beat " + opposition + ", " +
//								intScore + " to " + oIntScore);
//					} else if (intScore < oIntScore) {
//						System.out.println(team + "lost to " + opposition + ", " +
//								intScore + " to " + oIntScore);
//					} else {
//						System.out.println(team + "drew against " + opposition +
//								", " + intScore + " to " + oIntScore);
//					}
//				}
//			}
    }

//		for (int i = matchweek; i >= gamesPlayed - lastGames; i--) {
//			System.out.println("i = " + i);
//			String scoreFinder1 = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (i + start) + ") > td:nth-child(7) > a";
//			Element score1 = pageToScrape.selectFirst(scoreFinder1);
//			String score = score1.text();
//			if (score.contains(" ")) {
//				System.out.println(score);
//				String oppositionFinderB = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (i + start) + ") > td:nth-child(6) > a";
//				Element opposition1B = pageToScrape.selectFirst(oppositionFinderB);
//				String oppositionB = opposition1B.text();
//				oppositions[gamesCounted] = oppositionB;
//				gamesCounted++;
//				if (gamesCounted == lastGames) {
//					i = 0;
//				}
//			}
//		}
    
    
    //System.out.println("done");
    
    if (printInfo) {
      System.out.println(Arrays.toString(oppositions));
      System.out.println(team + "goals for: " + Arrays.toString(scores));
      System.out.println("Goals against: " + Arrays.toString(oScores));
    }
  }
  
  // Gets which place an MLS team is in
  public static int getMLSPosition(Document pageToScrape, String team, boolean printInfo) throws IOException {
    String tempTeam = "";
    
    int line = 1;
    while (!tempTeam.equals(team)) {
      String teamFinder = "";
      if (line == (NUM_MLS_TEAMS / 2) + 1) {
        line = line + 4;
      } else {
        line++;
      }
      
      teamFinder = "#fittPageContainer > div:nth-child(4) > div > div.layout__column.layout__column--1 > section > div > section > section > div.standings__table.InnerLayout__child--dividers > div > div.flex > table > tbody > tr:nth-child(" + line + ") > td > div > span.hide-mobile > a";
      Element teamElement = pageToScrape.selectFirst(teamFinder);
      tempTeam = teamElement.text();
    }
    
    String positionFinder = "#fittPageContainer > div:nth-child(4) > div > div.layout__column.layout__column--1 > section > div > section > section > div.standings__table.InnerLayout__child--dividers > div > div.flex > table > tbody > tr:nth-child(" + line + ") > td > div > span.team-position.ml2.pr3";
    int position = Integer.parseInt(pageToScrape.selectFirst(positionFinder).text());
    return position;
  }
  
  // Gets which conference an MLS team is in
  public static String getMLSConference(Document pageToScrape, String team, int position) throws IOException {
    String tempTeam = "";
    
    int eastElement = position + 1;
    int westElement = position + 16;
    
    String teamFinder = "#fittPageContainer > div:nth-child(4) > div > div.layout__column.layout__column--1 > section > div > section > section > div.standings__table.InnerLayout__child--dividers > div > div.flex > table > tbody > tr:nth-child(" + eastElement + ") > td > div > span.hide-mobile > a";
    tempTeam = pageToScrape.selectFirst(teamFinder).text();
    
    String conference = "";
    
    if (tempTeam.equals(teamFinder)) {
      conference = "Eastern";
    } else {
      teamFinder = "#fittPageContainer > div:nth-child(4) > div > div.layout__column.layout__column--1 > section > div > section > section > div.standings__table.InnerLayout__child--dividers > div > div.flex > table > tbody > tr:nth-child(" + westElement + ") > td > div > span.hide-mobile > a";
      tempTeam = pageToScrape.selectFirst(teamFinder).text();
      if (!tempTeam.equals(team)) {
        throw new IllegalStateException("Didn't find the right team in the expected (given) position");
      }
      conference = "Western";
    }
    
    return conference;
  }
  
  // Gets how many games an MLS team has played
  public static int getMLSGamesPlayed(Document pageToScrape, String conference, int position, boolean printInfo) throws IOException {
    int line;
    String gpFinder = "";
    
    if (conference.equals("Eastern")) {
      line = position + 1;
    } else {
      line = position + 16;
    }
    
    gpFinder = "#fittPageContainer > div:nth-child(4) > div > div.layout__column.layout__column--1 > section > div > section > section > div.standings__table.InnerLayout__child--dividers > div > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(" + line + ") > td:nth-child(1) > span";
    //"#fittPageContainer > div:nth-child(4) > div > div.layout__column.layout__column--1 > section > div > section > section > div.standings__table.InnerLayout__child--dividers > div > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(2) > td:nth-child(1) > span"
    //"#fittPageContainer > div:nth-child(4) > div > div.layout__column.layout__column--1 > section > div > section > section > div.standings__table.InnerLayout__child--dividers > div > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(6) > td:nth-child(1) > span"
    int gamesPlayed = Integer.parseInt(pageToScrape.selectFirst(gpFinder).text());
    return gamesPlayed;
  }
  
  // Gets how many points MLS team has
  public static int getMLSPoints(Document pageToScrape, String conference, int position) throws IOException {
    int line;
    String pointFinder = "";
    
    if (conference.equals("Eastern")) {
      line = position + 1;
    } else {
      line = position + 16;
    }
    
    pointFinder = "#fittPageContainer > div:nth-child(4) > div > div.layout__column.layout__column--1 > section > div > section > section > div.standings__table.InnerLayout__child--dividers > div > div.flex > div > div.Table__Scroller > table > tbody > tr:nth-child(" + line + ") > td:nth-child(8) > span";
    int points = Integer.parseInt(pageToScrape.selectFirst(pointFinder).text());
    return points;
  }
  
  // Gets record and fills out the array for the given team at the given matchweek
  public static void getRecord(Scanner console, String team, int[] scores, int[] oScores,
                               int[] wld, int gamesPlayed, int matchweek, boolean printInfo) {
//		System.out.println("Record going back how many games?");
    int lastGames = 5;
//		lastGames = console.nextInt();
    
    if (lastGames >= gamesPlayed) {
      lastGames = gamesPlayed - 1;
    }
    
    for (int i = matchweek; i >= gamesPlayed - lastGames; i--) {
      if (scores[i] != -1) {
        if (scores[i] > oScores[i]) {
          wld[0]++;
        } else if (scores[i] < oScores[i]) {
          wld[1]++;
        } else if (scores[i] == oScores[i] && scores[i] != -1) {
          wld[2]++;
        }
      }
    }
    if (printInfo) {
      System.out.println(team + " is " + Arrays.toString(wld) + ", [win, loss, draw]");
    }
  }
  
  // Is distinguished by league now
  public static void expectedRecord(String team, int[] scores, int[] oScores,
                                    int[] expected, int gamesPlayed, String[] oppositions,
                                    boolean printInfo, int position, String league, String mlsConference,
                                    Document mlsStandings) throws IOException {
    int lastGames = LAST_GAMES;
    if (lastGames >= gamesPlayed) {
      lastGames = gamesPlayed;
      if (lastGames < 0) {
        lastGames = 0;
      }
    }
    
    int gamesCounted = 0;
    int points;
    if (league.equals("MLS")) {
      points = getMLSPoints(mlsStandings, mlsConference, position);
    } else { // if (league.equals("Premier League") {
      points = getPremPoints(position);
    }
    
    double ppg = 0;
    if (gamesPlayed != 0) {
      ppg = points / gamesPlayed;
    }
    
    int i = scores.length;
    
    System.out.println(gamesPlayed);
    
    while (gamesPlayed != 0 && (i == 38 || (scores[i] == -1 || gamesCounted < lastGames))) {
      i--;
      if (scores[i] != -1) {
        //System.out.println("847: " + i);
        double oppg = 0;
        String opposition = oppositions[gamesCounted];
        int oPosition = 0;
        int oPoints = 0;
        int oGamesPlayed = 0;
        if (league.equals("MLS")) {
          oPosition = getMLSPosition(mlsStandings, opposition, printInfo);
          String oConference = getMLSConference(mlsStandings, opposition, oPosition);
          oPoints = getMLSPoints(mlsStandings, oConference, oPosition);
          oGamesPlayed = getMLSGamesPlayed(mlsStandings, oConference, oPosition, printInfo);
        } else { // if (league.equals("Premier League")) {
          oPosition = getPremPosition(opposition);
          oPoints = getPremPoints(oPosition);
          oGamesPlayed = getPremGamesPlayed(oPosition);
        }
        if (oGamesPlayed == 0) {
          oppg = 0;
        } else {
          oppg = oPoints / oGamesPlayed;
        }
        
        
        //System.out.println(opposition + " is in " + oPosition);
        
        // FIX: Could switch the else ifs and the elses
        
        // it's a win for team
        if (scores[i] > oScores[i]) {
          if (oppg - ppg > .35) {
            expected[0]++;
            //System.out.println(team + " was expected to lose to " + opposition + " but won");
          } else if ((ppg - oppg < .35 && ppg - oppg >= 0) || (oppg - ppg < .35 && oppg - ppg >= 0)) {
            expected[1]++;
            //System.out.println(team + " was expected to draw " + opposition + " but won");
          } else {
            expected[2]++;
            //System.out.println(team + " was expected to beat " + opposition + " and did");
          }
          
          // it's a loss for team
        } else if (scores[i] < oScores[i]) {
          if (ppg - oppg > .35) {
            expected[8]++;
            //System.out.println(team + " was expected to beat " + opposition + " but lost");
          } else if ((ppg - oppg < .35 && ppg - oppg >= 0) || (oppg - ppg < .35 && oppg - ppg >= 0)) {
            expected[7]++;
            //System.out.println(team + " was expected to draw " + opposition + " but lost");
          } else {
            expected[6]++;
            //System.out.println(team + " was expected to lose to " + opposition + " and did");
          }
          
          // it's a tie
        } else if (scores[i] == oScores[i] && scores[i] != -1) {
          if (ppg - oppg > .35) {
            expected[5]++;
            //System.out.println(team + " was expected to beat " + opposition + " but drew");
          } else if (oppg - ppg > .35) {
            expected[3]++;
            //System.out.println(team + " was expected to lose to " + opposition + " but drew");
          } else {
            expected[4]++;
            //System.out.println(team + " was expected to draw " + opposition + " and did");
          }
        }
        gamesCounted++;
      }
      //System.out.println("i: " + i);
      //System.out.println(gamesCounted + " < " + gamesPlayed);
      //System.out.println("scores[" + i + "] = " + scores[i]);
    }
    if (printInfo) {
      System.out.println(team + " is " + Arrays.toString(expected) + ", in order of best result to worst,");
      System.out.println("[win that was expected to be loss, ... , loss that was expected to be win]");
    }
  }
  
  // Scoring and conceding averages disregarding home/away
  public static double getAverages(String team, int[] scores, int gamesPlayed,
                                   String scoCon, boolean printInfo, boolean printAverages) {
    int lastGames = LAST_GAMES;
    if (lastGames >= gamesPlayed) {
      lastGames = gamesPlayed;
    }
    int gamesCounted = 0;
    
    int i = scores.length - 1;
    int total = 0;
    while (gamesPlayed != 0 && (scores[i] == -1 || gamesCounted < lastGames)) {
      if (scores[i] != -1) {
        total += scores[i];
        gamesCounted++;
      }
      //Very rough and bad coding?, it's just to exit the loop
      if (i == 0) {
        break;
      }
      i--;
    }
    double average = 1.0 * total / gamesCounted;
    if (printInfo || printAverages) {
      System.out.println("Average goals " + scoCon + " in last " + gamesCounted + " Games: " + average);
    }
    return average;
  }
  
  // Scoring and conceding averages using home/away
  public static double getAverages(String team, int[] scores, String[] locations,
                                   int gamesPlayed, String scoCon, boolean printInfo, boolean printAverages,
                                   boolean atHome) {
    int lastGames = LAST_GAMES;
    if (lastGames > gamesPlayed) {
      lastGames = gamesPlayed;
    }
    int wrongGames = 0;
    int gamesCounted = 0;
    
    int i = scores.length - 1;
    int total = 0;
    while (wrongGames + gamesCounted < gamesPlayed && (scores[i] == -1 || (gamesCounted + wrongGames) < lastGames)) {
      if (scores[i] != -1) {
        if ((locations[locations.length - 1 - (gamesCounted + wrongGames)].equals("H") && atHome)
            || (locations[locations.length - 1 - (gamesCounted + wrongGames)].equals("A") && !atHome)) {
          total += scores[i];
          gamesCounted++;
        } else {
          wrongGames++;
        }
      }
      
      if (i == 0) { // Very rough and bad code? just to exit the loop
        break;
      }
      i--;
    }
    
    double average = 1.0 * total / gamesCounted;
    if (Double.isNaN(average)) {
      System.out.println("Average is nan");
      average = 0;
    }
    if (printInfo || printAverages) {
      System.out.println("Average goals " + scoCon + " in last " + gamesCounted + " Games: " + average);
    }
    return average;
  }
  
  // Calculates recency factor
  public static int recency(int[] scores, int gamesPlayed, int matchweek) {
    // Int gamesBack is the amount of games elapsed from the matchweek
    // (array index) of the game that calls this method, to the present.
    int gamesBack = 0;
    int gamesCounted = 0;
    int i = 0;
    while (scores[i] == -1 || gamesCounted < gamesPlayed) {
      if (scores[i] != -1) {
        gamesBack++;
      }
      i++;
    }
    gamesBack = gamesPlayed - gamesBack;
    return gamesBack;
  }
  
  // Calculates standard deviation of scores
  public static double calculateSD(int[] scores, int gamesPlayed, String team,
                                   String scoCon, boolean printInfo, boolean sd) {
    
    double standardDeviation = 0.0;
    
    int lastGames = 5;
    if (lastGames >= gamesPlayed) {
      lastGames = gamesPlayed - 1;
    }
    int gamesCounted = 0;
    
    int i = scores.length - 1;
    int total = 0;
    while (scores[i] == -1 || gamesCounted < lastGames) {
      if (scores[i] != -1) {
        total += scores[i];
        gamesCounted++;
      }
      i--;
    }
    
    double mean = 1.0 * total / lastGames;
    
    gamesCounted = 0;
    i = scores.length - 1;
    
    while (scores[i] == -1 || gamesCounted < lastGames) {
      if (scores[i] != -1) {
        standardDeviation += 1.0 * (Math.pow(scores[i] - mean, 2)) / lastGames;
        gamesCounted++;
      }
      i--;
    }
    standardDeviation = Math.sqrt(standardDeviation);
    String consistency = "";
    if (printInfo || sd) {
      if (standardDeviation <= 0.5) {
        consistency = "extremely ";
      } else if (standardDeviation <= .6) {
        consistency = "very ";
      } else if (standardDeviation <= .75) {
        consistency = "pretty ";
      } else if (standardDeviation <= .9) {
        consistency = "";
      } else if (standardDeviation <= 1) {
        consistency = "kinda ";
      } else if (standardDeviation <= 1.2) {
        consistency = "not very ";
      } else {
        consistency = "not ";
      }
      System.out.println(team + " is " + consistency + "consistent. Standard Deviation = "+ standardDeviation);
    }
    return standardDeviation;
  }
  
  // Generates amount of goals away from average, how variable in terms of goals
  public static double awayFromAverage(int[] scores, int gamesPlayed, String team,
                                       String scoCon, boolean printInfo, boolean printAverages) {
    // Just getting average again
    int lastGames = LAST_GAMES;
    if (lastGames >= gamesPlayed) {
      lastGames = gamesPlayed - 1;
    }
    int gamesCounted = 0;
    
    int i = scores.length - 1;
    int total = 0;
    while ((scores[i] == -1 || gamesCounted < lastGames) && gamesPlayed > 0) {
      if (scores[i] != -1) {
        total += scores[i];
        gamesCounted++;
      }
      i--;
    }
    int average = (int) Math.round(1.0 * total / gamesCounted);
    
    // Now seeing the distance from average
    double afa = 0;
    gamesCounted = 0;
    
    i = scores.length - 1;
    while ((scores[i] == -1 || gamesCounted < lastGames) && gamesPlayed > 0) {
      if (scores[i] != -1) {
        //SHOULD ADD OUTLIER THING
        gamesCounted++;
        if (printAverages) {
          System.out.println(team + " " + afa + " += " + average + " - " + scores[i]);
        }
        afa += Math.abs(average - scores[i]);
      }
      i--;
    }
    afa /= lastGames;
    
    if (printInfo || printAverages) {
      System.out.println("Average goals " + scoCon + " apart from the average in last " + lastGames + " Games: " + afa);
    }
    return afa;
  }
  
  // Rates a team's form from 5 (bad 1 point per game) to 65 (good 13 points per game)
  public static int rateForm(int[] expected, boolean printInfo, boolean printForm) {
    int weightedRecord = 0;
    int totalCounted = 0;
    int value = 0;
    for (int i = 0; i < expected.length; i++) {
      value = i + 1;
      if (i > 2) {
        value += 2;
      }
      if (i > 5) {
        value += 2;
      }
      weightedRecord += (value) * expected[i];
      totalCounted += expected[i];
      if (printInfo || printForm) {
        System.out.println(weightedRecord + " = (" + value + ") * " + expected[i]);
      }
    }
    weightedRecord = (int)Math.round(weightedRecord / (1.0 * totalCounted) * LAST_GAMES);
    weightedRecord = LAST_GAMES * 13 + 5 - weightedRecord;
    if (printInfo || printForm) {
      System.out.println("Weighted record = " + weightedRecord + " out of 55");
    }
    return weightedRecord;
  }
  
  // Predicts the score of given teams using previous and current data
  public static void predictScore(int gamesPlayed, int gamesPlayed2, int form,
                                  int form2, int[] scores, int[] oScores, double scored, double scored2,
                                  double hScored, double aScored,
                                  double conceded, double conceded2, double hConceded, double aConceded,
                                  String team, String team2, int[] expected, int[] expected2, double afas1,
                                  double afac1, double afas2, double afac2, Scanner formInput,BufferedWriter
                                      matchOutput, int position1, int position2, Scanner console)
      throws IOException, FileNotFoundException{
    
    // Find Patterns
    File teamFile = new File(HISTORICAL_SCORES_FILE_LOCATION + team + "/" + team2 + ".txt");
    Scanner historicalInput;
    if (teamFile.exists()) {
      historicalInput = new Scanner(teamFile);
    } else {
      System.out.println("   These teams have not played in the last 7 years");
      historicalInput = new Scanner(new File(HISTORICAL_SCORES_FILE_LOCATION + "/NoMatches.txt"));
    }
    
    Stack<Integer> historicalWDL = new Stack<>();
    ArrayList<String> locations = new ArrayList<>();
    ArrayList<Integer> t1S = new ArrayList<>();
    ArrayList<Integer> t2S = new ArrayList<>();
    
    while (historicalInput.hasNextLine()) {
      Scanner lineScan = new Scanner(historicalInput.nextLine());
      String loc = lineScan.next();
      locations.add(loc);
      int score1 = lineScan.nextInt();
      t1S.add(score1);
      int score2 = lineScan.nextInt();
      t2S.add(score2);
      
      //System.out.println("Score: " + score1 + " to " + score2);
      
      if (score1 > score2) {
        historicalWDL.push(3);
        //System.out.println("Win");
      } else if (score1 == score2) {
        historicalWDL.push(2);
        //System.out.println("Draw");
      } else {
        historicalWDL.push(1);
        //System.out.println("Loss");
      }
    }
    
    historicalInput.close();
    
    double historicalRating = 0;
    double homeRating = 0;
    double awayRating = 0;
    int s = historicalWDL.size();
    
    int[] t1Scores = new int[t1S.size()];
    int[] t2Scores = new int[t2S.size()];
    String[] locationsA = new String[locations.size()];
    
    for (int i = 0; i < t1S.size(); i++) {
      t1Scores[i] = t1S.get(t1S.size() - 1 - i);
      t2Scores[i] = t2S.get(t2S.size() - 1 - i);
      locationsA[i] = locations.get(locations.size() - 1 - i);
    }
    
    double max = 0;
    double hMax = 0;
    double aMax = 0;
    double min = 0;
    double hMin = 0;
    double aMin = 0;
    for (int i = 0; i < s; i++) {
      double recencyWeight = 100.0 / (i + 2);
      max += recencyWeight * 3;
      min += recencyWeight;
      double hWDL = historicalWDL.pop();
      historicalRating += hWDL * recencyWeight;
      String l = locationsA[i];
      if (l.equalsIgnoreCase("H")) {
        homeRating += hWDL * recencyWeight;
        hMax += recencyWeight * 3;
        hMin += recencyWeight;
      } else {
        awayRating += hWDL * recencyWeight;
        aMax += recencyWeight * 3;
        aMin += recencyWeight;
      }
      //System.out.println("Historical Rating: " + historicalRating);
    }
    
    historicalRating = ((historicalRating - min) / (max - (min)));
    historicalRating = Math.round(historicalRating * 10000) / 100.0;
    homeRating = ((homeRating - hMin) / (hMax - (hMin)));
    homeRating = Math.round(homeRating * 10000) / 100.0;
    awayRating = ((awayRating - aMin) / (aMax - (aMin)));
    awayRating = Math.round(awayRating * 10000) / 100.0;
    //System.out.println("Historical (Last " + s + " Matches) Rating Out of 100: " + historicalRating);
    //System.out.println("Home Rating Out of 100: " + homeRating);
    //System.out.println("Away Rating Out of 100: " + awayRating);


//		int line = s - 1;
//		while (historicalInput.hasNextLine()) {
//			Scanner lineScan = new Scanner(historicalInput.nextLine());
//			String location = lineScan.next();
//			System.out.println("Location: " + location);
//			locationsA[line] = lineScan.next();
//			t1Scores[line] = lineScan.nextInt();
//			System.out.println("   " + t1Scores[line]);
//			System.out.println();
//			t2Scores[line] = lineScan.nextInt();
//			line--;
//		}
    
    // Historical consecutive goals (pattern) when the teams play
    int consecT1Scores = getConsecutiveGoals(team, t1Scores);
    int consecT2Scores = getConsecutiveGoals(team2, t2Scores);
    
    double WAHG1 = getWeightedAverageHistoricalGoals(team, t1Scores, locationsA, true);
    double WAHG2 = getWeightedAverageHistoricalGoals(team2, t2Scores, locationsA, false);
    double[] WRF = getWeightedResultFrequencies(team, t1Scores, t2Scores, locationsA, true);
//		System.out.println("win rate: " + WRF[0]);
//		System.out.println("draw rate: " + WRF[1]);
//		System.out.println("loss rate: " + WRF[2]);
    
    historicalInput.close();
    
    double scoreConfidence = 0;
    double winOdds = 0;
    double drawOdds = 0;
    double lossOdds = 0;
    double average1 = 0;
    double average2 = 0;
    int score1 = 0;
    int score2 = 0;
    if (gamesPlayed > 0) {
      scoreConfidence = 100 - Math.abs((afas1 + afas2 + afac1 + afac2) * 20.0
          + (scored - conceded2) + (scored2 - conceded) * 4.0);
      
      //REDO THIS
      double wrfTotal = WRF[0] + WRF[1] + WRF[2];
      winOdds = Math.round(10000.0 * WRF[0] / wrfTotal) / 100.0;
      drawOdds = Math.round(10000.0 * WRF[1] / wrfTotal) / 100.0;
      lossOdds = Math.round(10000.0 * WRF[2] / wrfTotal) / 100.0;
      //winOdds = 100 - (Math.pow(50 + (position1 - form) - (position2 - form2), 2) / 50);
      
      
      average1 = ((scored + hScored) / 2.0 + (conceded2 + aConceded) / 2.0) / 2.0;
      System.out.println("average1 = ((" + scored + " + " + hScored + " + ) / 2.0 + (" + conceded2 + " + " + aConceded + ") / 2.0) / 2.0");
      average2 = ((scored2 + aScored) / 2.0 + (conceded + hConceded) / 2.0) / 2.0;
      System.out.println("average2 = ((" + scored2 + " + " + aScored + " + ) / 2.0 + (" + conceded + " + " + hConceded + ") / 2.0) / 2.0");
      if (average1 == 0) {
        average1 = 0.2;
      }
      if (average2 == 0) {
        average2 = 0.2;
      }
      
      //double formWeight = getFormWeight(formInput);
      double formWeight = 32;
      //System.out.println("The form is weighted at: " + formWeight);
      
      score1 = (int)Math.round((average1 * (form / formWeight)));
      System.out.println("score1 = " + average1 + " * " + form + " / 32");
      score1 = (int)Math.round(score1 * .7 + WAHG1 * .3);
      
      score2 = (int)Math.round((average2 * (form2 / formWeight)));
      System.out.println("score2 = " + average2 + " * " + form2 + " / 32");
      score2 = (int)Math.round(score2 * .7 + WAHG2 * .3);
      
      if (consecT1Scores > 2) {
        System.out.println(team + " has scored " + t1Scores[0] + " goals in each of the last " +
            consecT1Scores + " games. Otherwise it would predict " + score1);
        score1 = t1Scores[0];
      }
      if (consecT2Scores > 2) {
        System.out.println(team2 + " has scored " + t2Scores[0] + " goals in each of the last " +
            consecT2Scores + " games. Otherwise it would predict " + score2);
        score2 = t2Scores[0];
      }
      
      //no games played prediction
    } else {
      if (consecT1Scores > 2) {
        score1 = (int)Math.round(consecT1Scores);
        System.out.println(team + " has scored " + t1Scores[0] + " goals in each of the last " +
            consecT1Scores + " games. Otherwise it would predict " + Math.round(WAHG1));
      } else {
        score1 = (int)Math.round(WAHG1);
      }
      if (consecT1Scores > 2) {
        score2 = (int)Math.round(consecT2Scores);
        System.out.println(team + " has scored " + t2Scores[0] + " goals in each of the last " +
            consecT2Scores + " games. Otherwise it would predict " + Math.round(WAHG2));
      } else {
        score2 = (int)Math.round(WAHG2);
      }
      double wrfTotal = WRF[0] + WRF[1] + WRF[2];
      winOdds = Math.round(10000.0 * WRF[0] / wrfTotal) / 100.0;
      drawOdds = Math.round(10000.0 * WRF[1] / wrfTotal) / 100.0;
      lossOdds = Math.round(10000.0 * WRF[2] / wrfTotal) / 100.0;
    }
    
    
    
    //System.out.println(score1 + " = Math.round(" + average1 + " * " + form / 22.5 + "^" + formWeight + ")");
    //System.out.println(score2 + " = Math.round(" + average2 + " * " + form2 / 22.5 + "^" + formWeight + ")");
    System.out.println(team + "(" + position1 + ") against " + team2 + "(" + position2 + ")");
    System.out.println(score1 + " to " + score2);
    System.out.println("Confidence of score: " + scoreConfidence + "%");
    System.out.println("Odds of win: " + winOdds + "%");
    System.out.println("Odds of draw: " + drawOdds + "%");
    System.out.println("Odds of loss: " + lossOdds + "%");
//		System.out.println();
//		System.out.println("Append Predictions to File?");
//		
//		long millis = System.currentTimeMillis();   
//	    java.util.Date date = new java.util.Date(millis);
//		
//		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");  
//		String strDate = dateFormat.format(date);  
//		
//		if (((console.nextLine()).toLowerCase()).charAt(0) == 'y') {
//			matchOutput.append(strDate);
//			matchOutput.newLine();
//			matchOutput.append(team);
//			matchOutput.newLine();
//			matchOutput.append(team2);
//			matchOutput.newLine();
//			matchOutput.append(average1 + " " + form);
//			matchOutput.newLine();
//			matchOutput.append(average2 + " " + form2);
//			matchOutput.newLine();
//			matchOutput.close();
//		}
  }
  
  /**
   * Finds an int on the page and returns it
   * @param url url to get int from
   * @param finder location of int on page
   * @param toRemove extra stuff to remove from page info
   * @param league league to see what to do
   * @return 0 if MLS and if PL, then return int at given place on given page
   * @throws IOException if error getting page
   */
  public static int intFinder(String url, String finder, String toRemove, String league) throws IOException {
    Document pageToScrape = Jsoup.connect(url).get();
    if (league.equals("Premier League")) {
      String found = "";
      if (!toRemove.equals("/")) {
        Element found1 = pageToScrape.selectFirst(finder);
        found = found1.text();
      } else if(toRemove.equals("/")) {
        Element link = pageToScrape.select(finder).first();
        String absHref = link.attr("abs:href");
        found += absHref.charAt(77);
        found += absHref.charAt(78);
      }
      
      if (found.contains(toRemove)) {
        found = found.replace(toRemove, "");
        //			System.out.println(found);
      }
      int foundInt = Integer.parseInt(found);
      return foundInt;
    } else {
      return 0;
    }
  }
  
  /**
   * Finds an int on the page and returns it
   * @param finder location of int on page
   * @param toRemove extra stuff to remove from page info
   * @return int at given place on given page
   * @throws IOException if error getting page
   */
  public static int intFinderMLS(Document pageToScrape, String finder, String toRemove) throws IOException {
    String found = "";
    if (!toRemove.equals("/")) {
      Element found1 = pageToScrape.selectFirst(finder);
      found = found1.text();
    } else if(toRemove.equals("/")) {
      Element link = pageToScrape.select(finder).first();
      String absHref = link.attr("abs:href");
      found += absHref.charAt(77);
      found += absHref.charAt(78);
    }
    
    if (found.contains(toRemove)) {
      found = found.replace(toRemove, "");
//			System.out.println(found);
    }
    int foundInt = Integer.parseInt(found);
    return foundInt;
  }
  
  /**
   * Gets how many games premier league team at position have played
   * @param position place on table to check
   * @return amount of games played by team at position
   * @throws IOException if error getting page
   */
  public static int getPremGamesPlayed(int position) throws IOException {
    int year = getSeason();
    String seasonYears = (year - 1) + "-" + year;
    String url = "https://www.worldfootball.net/schedule/eng-premier-league-" + seasonYears + "-spieltag/";
    
    Document pageToScrape = Jsoup.connect(url).get();
    String gamesFinder = "#site > div.white > div.content > div.portfolio > div:nth-child(7) > div > table.standard_tabelle > tbody > tr:nth-child(" + (position + 1) + ") > td:nth-child(4)";
    Element gamesPlayedElement = pageToScrape.selectFirst(gamesFinder);
    
    int gamesPlayed;
    if (gamesPlayedElement != null) {
      gamesPlayed = Integer.parseInt(gamesPlayedElement.text());
    } else {
      gamesPlayed = 0;
      //System.out.println("No Games Played This Season");
    }
    
    return gamesPlayed;
  }
  
  // Gets the position of a given team in the table
  public static int getPremPosition(String givenTeam) throws IOException {
    int year = getSeason();
    String seasonYears = (year - 1) + "-" + year;
    String url = "https://www.worldfootball.net/schedule/eng-premier-league-" + seasonYears + "-spieltag/";
    String team = "";
    int position = 0;
    String[] teamPositions = new String[20];
    Document pageToScrape = Jsoup.connect(url).get();
    
    for (position = 1; position < 21; position++) {
      String teamFinder = "#site > div.white > div.content > div.portfolio > div:nth-child(7) > div > table.standard_tabelle > tbody > tr:nth-child(" + (position + 1) + ") > td:nth-child(3) > a";
      Element team1 = pageToScrape.selectFirst(teamFinder);
      team = team1.text();
      
      if (team.equals(givenTeam)) {
        //System.out.println(givenTeam + " position = " + position);
        return position;
      }
    }
    //System.out.println(givenTeam + " position = " + position + "???");
    return position;
  }
  
  /**
   * Gets how many points premier league team at position has
   * @param position place on table to check
   * @return amount of points team at position has
   * @throws IOException if error getting page
   */
  public static int getPremPoints(int position) throws IOException {
    int year = getSeason();
    String seasonYears = (year - 1) + "-" + year;
    String url = "https://www.worldfootball.net/schedule/eng-premier-league-" + seasonYears + "-spieltag/";
    
    Document pageToScrape = Jsoup.connect(url).get();
    String gamesFinder = "#site > div.white > div.content > div.portfolio > div:nth-child(7) > div > table.standard_tabelle > tbody > tr:nth-child(" + (position + 1) + ") > td:nth-child(10)";
    //					  #site > div.white > div.content > div.portfolio > div:nth-child(7) > div > table.standard_tabelle > tbody > tr:nth-child(2) > td:nth-child(10)
    // 					  #site > div.white > div.content > div.portfolio > div:nth-child(7) > div > table.standard_tabelle > tbody > tr:nth-child(3) > td:nth-child(10)
    Element gamesPlayedElement = pageToScrape.selectFirst(gamesFinder);
    int gamesPlayed;
    if (gamesPlayedElement != null) {
      gamesPlayed = Integer.parseInt(gamesPlayedElement.text());
    } else {
      gamesPlayed = 0;
      System.out.println("No Games Played This Season");
    }
    //??????
    return gamesPlayed;
  }
  
  // Gets the year of the season
  public static int getSeason() {
    Calendar cal = Calendar.getInstance();
    int season = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;
    //System.out.println(month);
    if (month > 6) {
      season++;
    }
    return season;
  }
  
  // Gets the league to predict
  public static String getLeague(Scanner console) {
    System.out.println("MLS or Premier League?");
    String league = "";
    boolean exists = false;
    boolean runOne = true;
    while (!exists) {
      league = console.nextLine().toLowerCase();
      if (runOne)
        league = console.nextLine().toLowerCase();
      if (league.equalsIgnoreCase("mls") || league.contains("maj")) {
        league = "MLS";
        exists = true;
      } else if (league.equalsIgnoreCase("pl") || league.contains("prem")) {
        league = "Premier League";
        exists = true;
      } else {
        System.out.println("What");
        runOne = false;
      }
    }
    return league;
  }
  
  /**
   * Writes to a file about whether predicted info was correct
   * @param formInput file to check form info from
   * @param formOutput file to write form info to
   * @param matchInput file to check info from
   * @param matchFile file to write match info to
   * @param matchweek matchweek of match being played
   * @param season season year
   * @throws FileNotFoundException if the file doesn't exist
   * @throws IOException if there's an error getting website
   * @throws ParseException there's an error parsing
   */
  public static void checkPremPredictions(Scanner formInput, BufferedWriter formOutput,
                                          Scanner matchInput, File matchFile, int matchweek,
                                          int season)
      throws FileNotFoundException, IOException, ParseException {
    int teamsChecked = 0;
    while (matchInput.hasNextLine()) {
      boolean dontPrint = false;
      String dateString = matchInput.nextLine();
      Date date = new SimpleDateFormat("dd-MM-yyyy").parse(dateString);
      //System.out.println("Should add a date thing was predicted and whether it was right print");
      //System.out.println("also maybe use the day the game was played insteaad of the current day");
      long daysBack = getDateDiff(date);
      if (daysBack > 10) {
        dontPrint = true;
      }
      teamsChecked++;
      //System.out.println("Teams Checked: " + teamsChecked);
      String team = matchInput.nextLine();
      //System.out.println("   Checking team: " + team);
      String predictedOpposition = matchInput.nextLine();
      String fileLine3 = matchInput.nextLine();
      String fileLine4 = matchInput.nextLine();
      String urlTeam = (team.replace(" ", "-")).toLowerCase();
      String url = "https://www.worldfootball.net/teams/" + urlTeam + "/" + season + "/3/";
      int start;
      int line = 0;
      String prem = "";
      int position = getPremPosition(team);
      //System.out.println("team = " + team);
      //System.out.println(url);
      //System.out.println("predicted opposition = " + predictedOpposition);
      //System.out.println("position = " + position);
      int gamesPlayed = getPremGamesPlayed(position);
      Document pageToScrape = Jsoup.connect(url).get();
      String opposition = "";
      
      // Finds where the scores start on the website
      while (!prem.contains("Premier")) {
        String premFinder = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + line + ") > td > a";
        Element prem1 = pageToScrape.selectFirst(premFinder);
        if (prem1 != null) {
          prem = prem1.text();
        }
        line++;
      }
      // Check if there are 5 last games
      int lastGames = 5;
      if (lastGames >= gamesPlayed) {
        lastGames = gamesPlayed - 1;
      }
      
      start = line + 1;
      for (int i = gamesPlayed - lastGames; i <= matchweek; i++) {
        // Gets the score of the game
        //System.out.println(i + " / " + matchweek);
        String scoreFinder1 = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (i + start) + ") > td:nth-child(7) > a";
        Element score1 = pageToScrape.selectFirst(scoreFinder1);
        String score = score1.text();
        // The space means that the game was played
        if (score.contains(" ")) {
          // Gets the team they're playing
          String oppositionFinder = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (i + start) + ") > td:nth-child(6) > a";
          Element opposition1 = pageToScrape.selectFirst(oppositionFinder);
          opposition = opposition1.text();
          //System.out.println(opposition);
          
          if (opposition.equals(predictedOpposition)) {
            //System.out.println("Found matching opposition!");
            dontPrint = true;
            String tempScore = "";
            String oTempScore = "";
            String result = "";
            int intScore = 0;
            int oIntScore = 0;
            for (int j = 0; j < score.indexOf(" "); j++) {
              result += score.charAt(j);
            }
            for (int j = 0; j < result.length(); j++) {
              if (j < result.indexOf(":")) {
                tempScore += result.charAt(j);
                intScore = Integer.parseInt(tempScore);
              } else if (j > result.indexOf(":")) {
                oTempScore += result.charAt(j);
                oIntScore = Integer.parseInt(oTempScore);
              }
            }
            
            
            Scanner lineScan = new Scanner(fileLine3);
            double average1 = lineScan.nextDouble();
            int form1 = lineScan.nextInt();
            
            Scanner lineScan2 = new Scanner(fileLine4);
            double average2 = lineScan2.nextDouble();
            int form2 = lineScan2.nextInt();
            
            if (form1 == 0) {
              form1 = 1;
            }
            if (form2 == 0) {
              form2 = 1;
            }
            //System.out.println("iS and a1 and form1: " + intScore + " " + average1 + " " + form1);
            //System.out.print(Math.log10(intScore / average1) + " / " + Math.log10(form1) + " = ");
            double formWeight = (Math.log10(intScore / average1) / (Math.log10(form1)));
            //System.out.println("FormWeight = " + formWeight);
            
            //System.out.println("oIS and a2 and form2: " + oIntScore + " " + average2 + " " + form2);
            //System.out.print(Math.log10(oIntScore / average2) + " / " + Math.log10(form2) + " = ");
            double formWeight2 = (Math.log10(oIntScore / average2) / (Math.log10(form2)));
            //System.out.println("FormWeight2 = " + formWeight2);
            
            formOutput.append(formWeight + " " + formWeight2);
            formOutput.newLine();
          }
          
          File tempFile = new File("myTempFile.txt");
          
          //BufferedReader reader = new BufferedReader(new FileReader(matchFile));
          BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, true));
          Scanner fileScanner = new Scanner(matchFile);
          
          int teamNumber = 0;
          while (fileScanner.hasNextLine()) {
            teamNumber++;
            for (int j = 0; j < 5; j++) {
              String rewrittenLine = fileScanner.nextLine();
              if (teamNumber == teamsChecked && dontPrint) {
                //System.out.println("Was told not to print this line");
              } else {
                writer.write(rewrittenLine);
                writer.newLine();
                //System.out.println("Wrote: " + rewrittenLine);
              }
            }
          }
          
          writer.close();
          //reader.close();
          boolean successful = tempFile.renameTo(matchFile);
          //reuseLines(matchFile, team, predictedOpposition, fileLine3, fileLine4);
					
/*					If the match is in the last _ games, it
					// Checks the file to see if the match it's looking at has
					// been predicted before and if it has, it averages the formWeight
					// with it and removes the prediction line
					
					// Prints all the played matches if you want
					if (printInfo || printMatches) {
						if (intScore > oIntScore) {
							System.out.println(team + "beat " + opposition + ", " +
									intScore + " to " + oIntScore);
						} else if (intScore < oIntScore) {
							System.out.println(team + "lost to " + opposition + ", " +
									intScore + " to " + oIntScore);
						} else {
							System.out.println(team + "drew against " + opposition +
									", " + intScore + " to " + oIntScore);
						} 
					} */
        }
      }
    }
    formOutput.close();
  }
  
  /**
   * Gets difference between given date and now
   * @param date2 date to get difference
   * @return difference between now and date
   */
  public static long getDateDiff(Date date2) {
    long millis = System.currentTimeMillis();
    
    // creating a new object of the class Date
    java.util.Date date = new java.util.Date(millis);
    long diffInMillies = millis - date2.getTime();
    return TimeUnit.DAYS.convert(diffInMillies,TimeUnit.MILLISECONDS);
  }
  
  /**
   * Uses past predictions to reweight the form for new predictions
   * @param formInput form file to get weight from
   * @return how much the form should be weighted
   */
  public static double getFormWeight(Scanner formInput) {
    double sum = 0;
    int count = 0;
    while (formInput.hasNextDouble()) {
      double temp = formInput.nextDouble();
      // Sometimes it generates negative infinity if the actual score is 0
      if (temp == Double.NEGATIVE_INFINITY) {
        temp = -0.35;
      } else if (temp == Double.POSITIVE_INFINITY) {
        temp = 0.35;
      }
      sum += temp;
      count++;
    }
    double average = sum / count;
    return average;
  }
  
  /**
   * Gets the amount of consecutive goals (scored exactly once in 4 consecutive games)
   * @param team the team to check goals of
   * @param scores scores to check for consecutives from
   * @return the amount of consecutive goals (scored exactly once in 4 consecutive games)
   */
  public static int getConsecutiveGoals(String team, int[] scores) {
    int n = 1;
    int consecutive = 1;
    
    // Limited because it won't count the last (least recent) game but I
    // didn't think that mattered enough to need to be fixed
    while (scores[0] == scores[n] && n + 1 < scores.length) {
      consecutive++;
      n++;
    }
    return consecutive;
  }
  
  // Gets the weighted (by recency) frequency of each result (WDL) against the other team
  public static double[] getWeightedResultFrequencies(String team, int[] scores, int[] oScores,
                                                      String[] locations, boolean atHome) {
    double[] WRF = new double[3];
    int l = scores.length;
    double total = 0;
    double[] weights = new double[l];
    for (int i = 0; i < l; i++) {
      if (locations[i].equals("H")) {
        weights[i] = 1.0 / (i + 1);
        total += weights[i];
      }
    }
    int result;
    for (int i = 0; i < l; i++) {
      if (scores[i] > oScores[i]) { result = 0;
      } else if (scores[i] == oScores[i]) { result = 1;
      } else { result = 2; }
      if (locations[i].equals("H")) {
        WRF[result] += (weights[i] / total);
      }
    }
    
    return WRF;
  }
  
  // Gets the weighted (by recency) average of a team's historical goals against the other team
  public static double getWeightedAverageHistoricalGoals(String team, int[] scores,
                                                         String[] locations, boolean atHome) {
    double WAHG = 0;
    int l = scores.length;
    double total = 0;
    double[] weights = new double[l];
    for (int i = 0; i < l; i++) {
      if ((locations[i].equals("H") && atHome) || (locations[i].equals("A") && !atHome)) {
        weights[i] = 1.0 / (i + 1);
        total += weights[i];
      }
    }
    for (int i = 0; i < l; i++) {
      if ((locations[i].equals("H") && atHome) || (locations[i].equals("A") && !atHome)) {
        WAHG += scores[i] * (1.0 * weights[i] / total);
      } else {
        WAHG += scores[i] * (1.0 * weights[i] / total);
      }
    }
    
    return WAHG;
  }
  
  
  
  // Unused
  // Gets scores of the last "lastGames" games
  public static int[] lastNGames(int[] scores, int gamesPlayed, int lastGames) {
    int[] lastScores = new int[lastGames];
    if (lastGames >= gamesPlayed) {
      lastGames = gamesPlayed - 1;
    }
    
    int gamesCounted = 0;
    
    int i = scores.length - 1;
    while (scores[i] == -1 || gamesCounted < lastGames) {
      if (scores[i] != -1) {
        lastScores[gamesCounted] = scores[i];
        gamesCounted++;
      }
      i--;
    }
    return lastScores;
  }
}