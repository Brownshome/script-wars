<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="staticBean" class="brownshome.scriptwars.site.StaticBean"/>

<h2>About</h2>
<p>
Tanks is a simple game of stealth and tactics. You move around the map trying to 
find the enemy tanks and shoot them while avoiding getting shot. You are only sent 
the positions of tanks that you can see but are sent bullets at all times. This 
means that shooting will let the other tanks know where you are.
</p><p>
This game is classed as easy because there is not much data to handle and an 
effective bot can be made with less than one hundred lines of code. This does not 
mean that complex bots are not possible. There are many tricks and strategies that
a tank can use to outsmart their opponent.
</p>
<hr>

<h2>Rules</h2>
<p>
Each tick every tank can move in any direction or fire a shot
in any direction. The tank moves 1 space per turn and each shot moves 
<c:out value="${staticBean.tankGameShotSpeed}"/> spaces per turn.
</p><p>
You can see an enemy is there is a rectangle that can be drawn that contains both 
you and the other tanks that contains no walls.
<br>
INSERT PICS
</p><p>
Tanks have an ammo restraint to make trigger happy tanks less effective. Each 
tank has a maximum of ${staticBean.tankGameAmmo} ammo that is
regenerated once every ${staticBean.tankGameRegen} three ticks. This means overall
you can only fire once every three ticks.
</p><p>
On death, you will be sent one set of data with the <code>isAlive</code> byte set to
zero. <strong>There will be no other data in this dataset so do not attempt to read
any, seriously, we are NOT hiding anything in this data.</strong>
</p><p>
One point is gained for every kill and one point is lost for every death. I know it is
cruel, but a zero sum game is the only way to avoid exploitation by you clever people.
</p>
<hr>

<h2>Game Tick</h2>
<div class="media">
	<div class="media-left"><h1>1.</h1></div>
	<div class="media-body media-middle"><p>
	The tanks are moved by each player. If two tanks try to move into the same spot
	they neither of them will move. Keep a look out for this to avoid getting your 
	tank stuck in loops.
	</p></div>
</div>
<div class="media">
	<div class="media-left"><h1>2.</h1></div>
	<div class="media-body media-middle"><p>
	Shots move X spaces. Any tanks on spaces that the shot moves into will be 
	destroyed and lose a point while the tank than shot the shot will gain a point. 
	Shots can pass through each other and can be in the same space.
	</p></div>
</div>
<div class="media">
	<div class="media-left"><h1>3.</h1></div>
	<div class="media-body media-middle"><p>
	Tanks fire shots. The shot appears in the space in-front of the tank killing 
	any tank that is in that spot instantly. All tanks fire at the same time, so
	two tanks next to each other shooting will both die instantly.
	</p></div>
</div>

<h3>Data protocol</h3>
<p>Each tick the following data is sent:</p>
<ul>
	<li><p>A byte indicating if the player is alive. A value of 1 indicates alive
	and a value of 0 indicates that the player is dead. If the player is dead
	the following data is omitted.</p></li>
	<li><p>Two bytes containing the position of the player in the format (x, y)</p></li>
	<li><p>Two bytes containing the width and then the height of the game grid</p></li>
	<li><p>X * Y booleans containing the game world. Sent row by row with the first
	boolean being the top left corner and the last boolean being the bottom right
	corner. A value of true means that there is a wall in that grid cell.</p></li>
	<li><p>A single byte containing the number of visible players followed by an 
	x byte and a y byte for each player.</p></li>
	<li><p>A single byte  containing the number of shots on the grid followed by
	an x, a y and an byte representing a direction for each shot.</p></li>
</ul>
<p>The response is expected to be 2 bytes. The first byte is the action and the second
byte is the direction value.</p>
<table class="table table-compact">
	<tr>
		<th>Value</th>
		<th>Meaning</th>
	</tr><tr>
		<td>0</td><td>No action (No direction required)</td>
	</tr><tr>
		<td>1</td><td>Move</td>
	</tr><tr>	
		<td>2</td><td>Shoot</td>
	</tr>
</table>
<p>The direction values are as follows</p>
<table class="table table-compact">
	<tr>
		<th>Value</th>
		<th>Meaning</th>
	</tr><tr>
		<td>0</td><td>UP</td>
	</tr><tr>
		<td>1</td><td>DOWN</td>
	</tr><tr>
		<td>2</td><td>LEFT</td>
	</tr><tr>
		<td>3</td><td>RIGHT</td>
	</tr>
</table>
<h3>Example Script</h3>
<p>This is an example AI script with the code for reading data from the server filled in. It has
the AI logic missing. Feel free to use your own method of reading the data if you see fit.</p>

<!-- Formatted using https://tohtml.com/java/ -->
<!-- 
import brownshome.scriptwars.client.Network;

public class AI {
    static final int TANK = -3;
    static final int SPACE = -2;
    static final int WALL = -1;
    static final int UP = 0;
    static final int DOWN = 1;
    static final int LEFT = 2;
    static final int RIGHT = 3;
    
    public static void main(String[] args) {
        Network.connect(Integer.parseInt(args[0]), &quot;52.65.69.217&quot;, 35565, &quot;John Smith&quot;);
        
        loop:
        while(Network.nextTick()) {
            boolean isAlive = Network.getByte() == 1;   //Is the player alive
            if(isAlive) {
                int x = Network.getByte();              //X position
                int y = Network.getByte();              //Y position
                int width = Network.getByte();          //game width
                int height = Network.getByte();         //game height
                
                int[][] grid = new int[height][width];
                
                for(int row = 0; row &lt; grid.length; row++) {
                    for(int column = 0; column &lt; grid[row].length; column++) {
                        if(Network.getBoolean()) {      //Is wall
                            grid[row][column] = WALL;
                        } else {
                            grid[row][column] = SPACE;
                        }
                    }
                }
                
                int tanks = Network.getByte();          //Number of tanks
                for(int i = 0; i &lt; tanks; i++) {
                    int tankX = Network.getByte();      //Tank x
                    int tankY = Network.getByte();      //Tank y
                    grid[tankY][tankX] = TANK;
                }
                
                int shots = Network.getByte();          //Number of Shots
                for(int i = 0; i &lt; shots; i++) {
                    int shotX = Network.getByte();      //Shot x
                    int shotY = Network.getByte();      //Shot y
                                                        //Shot direction
                    grid[shotX][shotY] = Network.getByte();
                }
                
                //INSERT AI CODE HERE
                
                Network.sendByte(1);                    //Action Byte, in this case MOVE
                Network.sendByte(1);                    //Direction Byte, in this case DOWN
            } else {
                Network.sendByte(0);                    //Send stuff always, or get dropped
                System.out.println(&quot;We Are Dead&quot;);
            }
        }
    }
}
 -->

<pre style='color:#000000;background:#ffffff;'><span style='color:#800000; font-weight:bold; '>import</span><span style='color:#004a43; '> brownshome</span><span style='color:#808030; '>.</span><span style='color:#004a43; '>scriptwars</span><span style='color:#808030; '>.</span><span style='color:#004a43; '>client</span><span style='color:#808030; '>.</span><span style='color:#004a43; '>Network</span><span style='color:#800080; '>;</span>

<span style='color:#800000; font-weight:bold; '>public</span> <span style='color:#800000; font-weight:bold; '>class</span> AI <span style='color:#800080; '>{</span>
    <span style='color:#800000; font-weight:bold; '>static</span> <span style='color:#800000; font-weight:bold; '>final</span> <span style='color:#bb7977; '>int</span> TANK <span style='color:#808030; '>=</span> <span style='color:#808030; '>-</span><span style='color:#008c00; '>3</span><span style='color:#800080; '>;</span>
    <span style='color:#800000; font-weight:bold; '>static</span> <span style='color:#800000; font-weight:bold; '>final</span> <span style='color:#bb7977; '>int</span> SPACE <span style='color:#808030; '>=</span> <span style='color:#808030; '>-</span><span style='color:#008c00; '>2</span><span style='color:#800080; '>;</span>
    <span style='color:#800000; font-weight:bold; '>static</span> <span style='color:#800000; font-weight:bold; '>final</span> <span style='color:#bb7977; '>int</span> WALL <span style='color:#808030; '>=</span> <span style='color:#808030; '>-</span><span style='color:#008c00; '>1</span><span style='color:#800080; '>;</span>
    <span style='color:#800000; font-weight:bold; '>static</span> <span style='color:#800000; font-weight:bold; '>final</span> <span style='color:#bb7977; '>int</span> UP <span style='color:#808030; '>=</span> <span style='color:#008c00; '>0</span><span style='color:#800080; '>;</span>
    <span style='color:#800000; font-weight:bold; '>static</span> <span style='color:#800000; font-weight:bold; '>final</span> <span style='color:#bb7977; '>int</span> DOWN <span style='color:#808030; '>=</span> <span style='color:#008c00; '>1</span><span style='color:#800080; '>;</span>
    <span style='color:#800000; font-weight:bold; '>static</span> <span style='color:#800000; font-weight:bold; '>final</span> <span style='color:#bb7977; '>int</span> LEFT <span style='color:#808030; '>=</span> <span style='color:#008c00; '>2</span><span style='color:#800080; '>;</span>
    <span style='color:#800000; font-weight:bold; '>static</span> <span style='color:#800000; font-weight:bold; '>final</span> <span style='color:#bb7977; '>int</span> RIGHT <span style='color:#808030; '>=</span> <span style='color:#008c00; '>3</span><span style='color:#800080; '>;</span>
    
    <span style='color:#800000; font-weight:bold; '>public</span> <span style='color:#800000; font-weight:bold; '>static</span> <span style='color:#bb7977; '>void</span> main<span style='color:#808030; '>(</span><span style='color:#bb7977; font-weight:bold; '>String</span><span style='color:#808030; '>[</span><span style='color:#808030; '>]</span> args<span style='color:#808030; '>)</span> <span style='color:#800080; '>{</span>
        Network<span style='color:#808030; '>.</span>connect<span style='color:#808030; '>(</span><span style='color:#bb7977; font-weight:bold; '>Integer</span><span style='color:#808030; '>.</span>parseInt<span style='color:#808030; '>(</span>args<span style='color:#808030; '>[</span><span style='color:#008c00; '>0</span><span style='color:#808030; '>]</span><span style='color:#808030; '>)</span><span style='color:#808030; '>,</span> <span style='color:#0000e6; '>"52.65.69.217"</span><span style='color:#808030; '>,</span> <span style='color:#008c00; '>35565</span><span style='color:#808030; '>,</span> <span style='color:#0000e6; '>"John Smith"</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>
        
        loop<span style='color:#808030; '>:</span>
        <span style='color:#800000; font-weight:bold; '>while</span><span style='color:#808030; '>(</span>Network<span style='color:#808030; '>.</span>nextTick<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#808030; '>)</span> <span style='color:#800080; '>{</span>
            <span style='color:#bb7977; '>boolean</span> isAlive <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span> <span style='color:#808030; '>=</span><span style='color:#808030; '>=</span> <span style='color:#008c00; '>1</span><span style='color:#800080; '>;</span>   <span style='color:#696969; '>//Is the player alive</span>
            <span style='color:#800000; font-weight:bold; '>if</span><span style='color:#808030; '>(</span>isAlive<span style='color:#808030; '>)</span> <span style='color:#800080; '>{</span>
                <span style='color:#bb7977; '>int</span> x <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>              <span style='color:#696969; '>//X position</span>
                <span style='color:#bb7977; '>int</span> y <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>              <span style='color:#696969; '>//Y position</span>
                <span style='color:#bb7977; '>int</span> width <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>          <span style='color:#696969; '>//game width</span>
                <span style='color:#bb7977; '>int</span> height <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>         <span style='color:#696969; '>//game height</span>
                
                <span style='color:#bb7977; '>int</span><span style='color:#808030; '>[</span><span style='color:#808030; '>]</span><span style='color:#808030; '>[</span><span style='color:#808030; '>]</span> grid <span style='color:#808030; '>=</span> <span style='color:#800000; font-weight:bold; '>new</span> <span style='color:#bb7977; '>int</span><span style='color:#808030; '>[</span>height<span style='color:#808030; '>]</span><span style='color:#808030; '>[</span>width<span style='color:#808030; '>]</span><span style='color:#800080; '>;</span>
                
                <span style='color:#800000; font-weight:bold; '>for</span><span style='color:#808030; '>(</span><span style='color:#bb7977; '>int</span> row <span style='color:#808030; '>=</span> <span style='color:#008c00; '>0</span><span style='color:#800080; '>;</span> row <span style='color:#808030; '>&lt;</span> grid<span style='color:#808030; '>.</span>length<span style='color:#800080; '>;</span> row<span style='color:#808030; '>+</span><span style='color:#808030; '>+</span><span style='color:#808030; '>)</span> <span style='color:#800080; '>{</span>
                    <span style='color:#800000; font-weight:bold; '>for</span><span style='color:#808030; '>(</span><span style='color:#bb7977; '>int</span> column <span style='color:#808030; '>=</span> <span style='color:#008c00; '>0</span><span style='color:#800080; '>;</span> column <span style='color:#808030; '>&lt;</span> grid<span style='color:#808030; '>[</span>row<span style='color:#808030; '>]</span><span style='color:#808030; '>.</span>length<span style='color:#800080; '>;</span> column<span style='color:#808030; '>+</span><span style='color:#808030; '>+</span><span style='color:#808030; '>)</span> <span style='color:#800080; '>{</span>
                        <span style='color:#800000; font-weight:bold; '>if</span><span style='color:#808030; '>(</span>Network<span style='color:#808030; '>.</span>getBoolean<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#808030; '>)</span> <span style='color:#800080; '>{</span>      <span style='color:#696969; '>//Is wall</span>
                            grid<span style='color:#808030; '>[</span>row<span style='color:#808030; '>]</span><span style='color:#808030; '>[</span>column<span style='color:#808030; '>]</span> <span style='color:#808030; '>=</span> WALL<span style='color:#800080; '>;</span>
                        <span style='color:#800080; '>}</span> <span style='color:#800000; font-weight:bold; '>else</span> <span style='color:#800080; '>{</span>
                            grid<span style='color:#808030; '>[</span>row<span style='color:#808030; '>]</span><span style='color:#808030; '>[</span>column<span style='color:#808030; '>]</span> <span style='color:#808030; '>=</span> SPACE<span style='color:#800080; '>;</span>
                        <span style='color:#800080; '>}</span>
                    <span style='color:#800080; '>}</span>
                <span style='color:#800080; '>}</span>
                
                <span style='color:#bb7977; '>int</span> tanks <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>          <span style='color:#696969; '>//Number of tanks</span>
                <span style='color:#800000; font-weight:bold; '>for</span><span style='color:#808030; '>(</span><span style='color:#bb7977; '>int</span> i <span style='color:#808030; '>=</span> <span style='color:#008c00; '>0</span><span style='color:#800080; '>;</span> i <span style='color:#808030; '>&lt;</span> tanks<span style='color:#800080; '>;</span> i<span style='color:#808030; '>+</span><span style='color:#808030; '>+</span><span style='color:#808030; '>)</span> <span style='color:#800080; '>{</span>
                    <span style='color:#bb7977; '>int</span> tankX <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>      <span style='color:#696969; '>//Tank x</span>
                    <span style='color:#bb7977; '>int</span> tankY <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>      <span style='color:#696969; '>//Tank y</span>
                    grid<span style='color:#808030; '>[</span>tankY<span style='color:#808030; '>]</span><span style='color:#808030; '>[</span>tankX<span style='color:#808030; '>]</span> <span style='color:#808030; '>=</span> TANK<span style='color:#800080; '>;</span>
                <span style='color:#800080; '>}</span>
                
                <span style='color:#bb7977; '>int</span> shots <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>          <span style='color:#696969; '>//Number of Shots</span>
                <span style='color:#800000; font-weight:bold; '>for</span><span style='color:#808030; '>(</span><span style='color:#bb7977; '>int</span> i <span style='color:#808030; '>=</span> <span style='color:#008c00; '>0</span><span style='color:#800080; '>;</span> i <span style='color:#808030; '>&lt;</span> shots<span style='color:#800080; '>;</span> i<span style='color:#808030; '>+</span><span style='color:#808030; '>+</span><span style='color:#808030; '>)</span> <span style='color:#800080; '>{</span>
                    <span style='color:#bb7977; '>int</span> shotX <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>      <span style='color:#696969; '>//Shot x</span>
                    <span style='color:#bb7977; '>int</span> shotY <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>      <span style='color:#696969; '>//Shot y</span>
                                                        <span style='color:#696969; '>//Shot direction</span>
                    grid<span style='color:#808030; '>[</span>shotX<span style='color:#808030; '>]</span><span style='color:#808030; '>[</span>shotY<span style='color:#808030; '>]</span> <span style='color:#808030; '>=</span> Network<span style='color:#808030; '>.</span>getByte<span style='color:#808030; '>(</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>
                <span style='color:#800080; '>}</span>
                
                <span style='color:#696969; '>//INSERT AI CODE HERE</span>
                
                Network<span style='color:#808030; '>.</span>sendByte<span style='color:#808030; '>(</span><span style='color:#008c00; '>1</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>                    <span style='color:#696969; '>//Action Byte, in this case MOVE</span>
                Network<span style='color:#808030; '>.</span>sendByte<span style='color:#808030; '>(</span><span style='color:#008c00; '>1</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>                    <span style='color:#696969; '>//Direction Byte, in this case DOWN</span>
            <span style='color:#800080; '>}</span> <span style='color:#800000; font-weight:bold; '>else</span> <span style='color:#800080; '>{</span>
                Network<span style='color:#808030; '>.</span>sendByte<span style='color:#808030; '>(</span><span style='color:#008c00; '>0</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>                    <span style='color:#696969; '>//Send stuff always, or get dropped</span>
                <span style='color:#bb7977; font-weight:bold; '>System</span><span style='color:#808030; '>.</span>out<span style='color:#808030; '>.</span>println<span style='color:#808030; '>(</span><span style='color:#0000e6; '>"We Are Dead"</span><span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>
            <span style='color:#800080; '>}</span>
        <span style='color:#800080; '>}</span>
    <span style='color:#800080; '>}</span>
<span style='color:#800080; '>}</span>
</pre>