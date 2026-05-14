## MongoDB

- First install MongoDB from [mongodb.com](https://mongodb.com)

You may also want to install MongoDB Compass in order to visualise the database from [mongodb.com/try/download/compass](https://mongodb.com/try/download/compass)

Once installed, on Linux you can use the commands `sudo systemctl start mongod` and 
`sudo systemctl stop mongod` to start and stop MongoDB.

If you want to change the URI for the database you can edit the field in the 
config named `mongoUri`

## Bot Setup

 - In order to have the bot work, you need to create a [discord bot](https://discord.com/developers/applications), you can do this
by clicking `New Application`

 - Copy the bot token and put it in the `token` field of the config. If you missed it,
you can generate a new one by going to Overview>Bot and clicking `Reset Token`

## Server Setup

- Make a new Discord server by clicking the `Add Server` icon and then clicking `Create my Own`

- To add the webhook, go into the new servers `Server Settings` page, click `Integrations` and then `Webhooks`,
click `New Webhook`, then click the icon that appears, and click `Copy Webhook URL`. Paste this into the field named `webhook` in the config file.

- Click the settings icon next to your name in the bottom left, click `Developer` and 
make sure `Developer Mode` is checked. This makes sure that you can see the `Copy Server Id` and 
`Copy Channel Id` menu items.

 - Click your new servers name in the top left, and click `Copy Server Id`, then in the config file, paste this
into the `guildId` field.

 - Then, click the channel you want the Discord to Minecraft chat to be in, and click `Copy Channel Id`. 
Paste this into the `channelId` field in the config.

## In Game

If you set it up correctly, you should not get Discord or MongoDB related errors in the output when running.

You can use the `/scraper <on|off>` command to configure whether the scraper should run when ingame.

## Other Config Fields

`waitMinutesBetweenScrapes` and `scrapeByDefault` have no implementation and so will not do anything.