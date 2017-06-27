<h1>Nitrapi-Minecraft</h1>
This plugin enables the players with the minenitrapi.nitrapi permission <br>
to use /nitrapi commands.<br>
The documentation of the Nitrapi is located <href>on https://doc.nitrado.net</href><br>

<h2>Build the plugin</h2>
To build the plugin all you need is to run "mvn compile assembly:single"

<h2>Authorize the Plugin</h2>
Please visit <href>https://server.nitrado.net/deu/developer/index</href><br>
Click the plus button to create a new application. <br>
Insert name, website, email and a description(of your choice)<br>
<b>The redirect URL has to be http://***YOUR_SERVER_IP***:8080/Callback</b><br><br>
After saving, you can click your application to get the APP ID and the APP SECRET.<br>
<b>You need the values in the next step</b><br>

<h2>Fill your config</h2>
On first run, the plugin will create its config file.<br>
Alternatively you can create the config and insert the values before you start, to save one restart.<br>
The location has to be <b>plugins/Nitrapi-Minecraft/config.yml</b><br>
<pre>
APP_ID: Enter APP_ID here<br>
APP_SECRET: Enter APP_SECRET here<br>
IP: Enter IP here<br>
PORT: 8080<br>
SCOPES: user_info service service_order ssh_keys<br>
CREDENTIALS_PATH: /ftproot/minecraftbukkit/plugins/Nitrapi-Minecraft/</pre>

<b>Insert APP ID, APP Secret and your Server IP in the config file of the plugin.</b><br>
If you don't do this. the plugin will disable at start.<br>

<h2>First Auth</h2>
To authenticate you have to start the server, join the server and <br>
<b>execute /nitrapi auth</b>. Click the link and enter your Nitrado Account Credentials.<br>
If you don't do this, the server will die.
<h2>Have fun</h2>
Current commands you can see if you execute just /nitrapi.