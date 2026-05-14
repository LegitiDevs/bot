# Setup

## MongoDB

- First, install [MongoDB](https://mongodb.com)

You may also want to install [MongoDB Compass](https://mongodb.com/try/download/compass), in order to visualise the database

Once installed, on Linux you can use the commands `sudo systemctl start mongod` and
`sudo systemctl stop mongod` to start and stop MongoDB.

If you want to change the URI for the database you can edit the field in the
config named `mongoUri`

## Bot Setup

- In order for the bot to work, you need to create a [discord bot](https://discord.com/developers/applications)
    - to create a new bot, press `New Application` and fill in the fields.

- Copy the bot token and paste it into the `token` field of the config. If you missed it,
  you can generate a new one by going to `Overview` > `Bot` and clicking `Reset Token`

## Server Setup

- Make a new Discord server by clicking `Add Server` > `Create my Own`

- To add the webhook, go into the new servers `Server Settings` page, click `Integrations` > `Webhooks` > `New Webhook`, click the icon that appears, and then click `Copy Webhook URL`. Paste this into the field named `webhook` in the config file.

- Click the settings icon next to your name in the bottom left, click `Developer` and
  make sure `Developer Mode` is checked. This makes sure that you can see the `Copy Server Id` and
  `Copy Channel Id` menu items.

- Click the new server name in the top left, then `Copy Server Id`. Paste this
  into the `guildId` field.

- Finally, click the channel you want the Discord-Minecraft chat to be in, then `Copy Channel Id`.
  Paste this into the `channelId` field in the config.

## In Game

If done correctly, there should be no MongoDB or Discord related errors in the game log.

To prevent the scraper immediately starting when you join Legitermoose, you can set the `scrape` field in the config. You can also use the `/scraper <on|off>` command in game.

## Other Config Fields

`waitMinutesBetweenScrapes` currently has no implementation and so will not do anything.