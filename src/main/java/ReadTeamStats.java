import java.util.*;
import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

// Read Teams' records against each other
// Write the stuff to files
public class ReadTeamStats {
  //TODO: Change FILE_LOCATION to desired location of historical score files
  public static final String FILE_LOCATION = "/Users/marleybyers/Personal Projects/java-sports-predictor/additional-info/";
  public static final int START_YEAR = 2015;
  public static final int END_YEAR = 2022;
  public static final String[] TEAM_URL_NAMES = new String[] {"afc-bournemouth",
      "arsenal-fc", "aston-villa", "brentford-fc", "brighton-hove-albion",
      "burnley-fc", "chelsea-fc", "crystal-palace", "everton-fc", "fulham-fc",
      "leeds-united", "leicester-city", "liverpool-fc", "luton-town", "manchester-city",
      "manchester-united", "newcastle-united", "norwich-city", "nottingham-forest",
      "sheffield-united", "southampton-fc", "tottenham-hotspur", "watford-fc", "west-ham-united",
      "wolverhampton-wanderers"};
  public static final String[] TEAM_NAMES = new String[] {"AFC Bournemouth",
      "Arsenal FC", "Aston Villa", "Brentford FC", "Brighton & Hove Albion",
      "Burnley FC", "Chelsea FC", "Crystal Palace", "Everton FC", "Fulham FC",
      "Leeds United", "Leicester City", "Liverpool FC", "Luton Town", "Manchester City",
      "Manchester United", "Newcastle United", "Norwich City", "Nottingham Forest",
      "Southampton FC", "Sheffield United", "Tottenham Hotspur", "Watford FC", "West Ham United",
      "Wolverhampton Wanderers"};
  
  public static void main(String[] args) throws IOException {
    //Scanner formInput = new Scanner(new File("FormWeight.txt"));
    Scanner console = new Scanner(System.in);
    System.out.println("Delete Files (1) or Create New Files (2)");
    String answer = console.nextLine();
    boolean deleteFiles = answer.equalsIgnoreCase("delete") || answer.equals("1");
    
    for (int i = 0; i < TEAM_URL_NAMES.length; i++) {
      String urlName = TEAM_URL_NAMES[i];
      String teamName = TEAM_NAMES[i];
      System.out.println("Now Compiling: " + teamName);
      getHistoricalScores(urlName, teamName, deleteFiles);
    }
    System.out.println("Done");
  }
  
  // Prints historical scores in corresponding files
  public static void getHistoricalScores(String urlName, String team, boolean deleteFiles) throws IOException {
    int year = START_YEAR;
    while (year <= END_YEAR) {
      String url = "https://www.worldfootball.net/teams/" + urlName + "/" + year + "/3/";
      Document pageToScrape = Jsoup.connect(url).get();
      
      // Looking for the line where premier league? season starts
      String leagueTitle = "";
      int line = 0;
      int runs = 1;
      
      // Weird thing in the 2020/2021 season where it had 2 prem and champ seasons kinda so it runs twice
      if (year == 2021) {
        runs = 2;
      }
      for (int i = 0; i < runs; i++) {
        while (!leagueTitle.contains("Championship") && !leagueTitle.contains("Premier") && line < 100) {
          String leagueTitleFinder = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + line + ") > td > a";
          //"#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(41) > td > a > b"
          Element leagueTitleElement = pageToScrape.selectFirst(leagueTitleFinder);
          if (leagueTitleElement != null) {
            leagueTitle = leagueTitleElement.text();
          }
          
          //System.out.printf("Line is: %s. \n", line);
          
          line++;
        }
        
        // Reads each line
        // If the header of a group is premier league or ...? then it records the score stuff
        Element scoreElement = pageToScrape.selectFirst("#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(0) > td:nth-child(7) > a");
        Boolean fencepost = true;
        while (scoreElement != null || (fencepost && line < 99)) {
          // Gets the score of the game
          leagueTitle = "";
          fencepost = false;
          String scoreFinder = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (line + 1) + ") > td:nth-child(7) > a";
          scoreElement = pageToScrape.selectFirst(scoreFinder);
          String score = scoreElement.text();
          // Gets the team they're playing
          String opposition = selectorToText("#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (line + 1) + ") > td:nth-child(6) > a", pageToScrape);
          if (score.contains(" ")) {
            if (deleteFiles) {
              deleteCreatedFiles(team, opposition);
            } else {
              // Finds home or away
              String place = selectorToText("#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (line + 1) + ") > td:nth-child(4)", pageToScrape);
              
              // Separates team and opposition score from the rest of the text
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
              printToFile(team, opposition, place,
                  intScore, oIntScore);
              // Prints all the played matches if you want
              boolean printInfo = false;
              boolean printMatches = false;
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
          line++;
          scoreFinder = "#site > div.white > div.content > div.portfolio > div.box > div > table > tbody > tr:nth-child(" + (line + 1) + ") > td:nth-child(7) > a";
          scoreElement = pageToScrape.selectFirst(scoreFinder);
        }
      }
      year++;
    }
  }
  
  // Converts String of Selector Path on the given document into the text it represents
  // Given that the element isn't null
  private static String selectorToText(String selector, Document pageToScrape) {
    Element e = pageToScrape.selectFirst(selector);
    return e.text();
  }
  
  // Prints to team v opposition file in form "place score oScore" ex: h 0 1
  private static void printToFile(String team, String opposition, String place,
                                  int score, int oScore) throws IOException {
    File teamFile = new File(FILE_LOCATION + team + "/" + opposition + ".txt");
    if (!teamFile.exists()) { teamFile.createNewFile(); }
    BufferedWriter teamOutput = new BufferedWriter(new FileWriter(teamFile, true));
    
    teamOutput.append(place + " " + score + " " + oScore);
    //System.out.println("      " + place + " " + score + " " + oScore);
    teamOutput.newLine();
    teamOutput.close();
  }
  
  // Deletes every file in every folder to be recreated
  private static void deleteCreatedFiles(String team, String opposition) {
    File teamFile = new File(FILE_LOCATION + team + "/" + opposition + ".txt");
    if (teamFile.exists()) {
      teamFile.delete();
    }
  }
}